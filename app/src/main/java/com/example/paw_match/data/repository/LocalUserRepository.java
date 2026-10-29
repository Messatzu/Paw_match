package com.example.paw_match.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Patterns;
import androidx.annotation.Nullable;
import com.example.paw_match.data.model.UsuarioLocal;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Cuentas y sesión de prueba persistentes. Sus operaciones deben ejecutarse fuera del hilo UI. */
public final class LocalUserRepository implements AutoCloseable {
    private static final int PASSWORD_ITERATIONS = 120_000;
    private final Database database;
    private final Context context;

    public LocalUserRepository(Context context) {
        this(context, "paw_match_users.db");
    }

    // Permite verificar la persistencia en una base aislada sin borrar las cuentas de la app.
    LocalUserRepository(Context context, String databaseName) {
        this.context = context.getApplicationContext();
        database = new Database(this.context, databaseName);
    }

    /** Registra la cuenta e inicia su sesión. Devuelve null si el correo ya está registrado. */
    @Nullable
    public UsuarioLocal registrar(String nombre, String email, String password,
                                  String ubicacion, String rol) {
        nombre = nombre.trim();
        email = normalizarEmail(email);
        ubicacion = ubicacion.trim();
        if (nombre.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()
                || password.length() < 6 || ubicacion.isEmpty()
                || (!UsuarioLocal.ADOPTANTE.equals(rol) && !UsuarioLocal.PUBLICADOR.equals(rol))) {
            throw new IllegalArgumentException("Datos de registro inválidos");
        }

        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        ContentValues values = new ContentValues();
        values.put("name", nombre);
        values.put("email", email);
        values.put("password_hash", derivarPassword(password, salt));
        values.put("password_salt", salt);
        values.put("location", ubicacion);
        values.put("role", rol);

        SQLiteDatabase db = database.getWritableDatabase();
        db.beginTransaction();
        try {
            long id;
            try {
                id = db.insertOrThrow("users", null, values);
            } catch (SQLiteConstraintException duplicate) {
                return null;
            }
            guardarSesion(db, id);
            db.setTransactionSuccessful();
            return new UsuarioLocal(id, nombre, email, ubicacion, rol);
        } finally {
            db.endTransaction();
        }
    }

    /** Valida las credenciales exactas e inicia una sesión sin modificar las otras cuentas. */
    @Nullable
    public UsuarioLocal iniciarSesion(String email, String password) {
        SQLiteDatabase db = database.getWritableDatabase();
        UsuarioLocal usuario;
        try (Cursor cursor = db.query("users", null, "email = ?",
                new String[]{normalizarEmail(email)}, null, null, null)) {
            if (!cursor.moveToFirst()) return null;
            byte[] salt = cursor.getBlob(cursor.getColumnIndexOrThrow("password_salt"));
            byte[] storedHash = cursor.getBlob(cursor.getColumnIndexOrThrow("password_hash"));
            if (!MessageDigest.isEqual(storedHash, derivarPassword(password, salt))) return null;
            usuario = leerUsuario(cursor);
        }
        guardarSesion(db, usuario.getId());
        return usuario;
    }

    @Nullable
    public UsuarioLocal obtenerUsuarioActual() {
        try (Cursor cursor = database.getReadableDatabase().rawQuery(
                "SELECT users.* FROM users INNER JOIN session ON users._id = session.user_id"
                        + " WHERE session._id = 1", null)) {
            return cursor.moveToFirst() ? leerUsuario(cursor) : null;
        }
    }

    /** Cierra únicamente la sesión: la cuenta sigue disponible para volver a ingresar. */
    public void cerrarSesion() {
        database.getWritableDatabase().delete("session", null, null);
    }

    public UsuarioLocal actualizarPerfil(String nombre, String ubicacion, String descripcion) {
        if (nombre == null || nombre.trim().isEmpty() || ubicacion == null || ubicacion.trim().isEmpty()
                || descripcion == null) throw new IllegalArgumentException("Datos de perfil inválidos");
        ContentValues values = new ContentValues();
        values.put("name", nombre.trim());
        values.put("location", ubicacion.trim());
        values.put("description", descripcion.trim());
        return actualizarUsuarioActual(values, null);
    }

    /** Copia la foto al almacenamiento privado para conservarla después de cerrar la app. */
    public UsuarioLocal actualizarFotoPerfil(Uri origen) {
        UsuarioLocal actual = obtenerUsuarioActual();
        if (actual == null) throw new IllegalStateException("No hay una sesión activa");
        if (origen == null) throw new IllegalArgumentException("Falta la foto de perfil");
        File directorio = new File(context.getFilesDir(), "profile_photos");
        File nueva = null;
        try {
            if (!directorio.isDirectory() && !directorio.mkdirs())
                throw new IOException("No se pudo crear el directorio de fotos");
            nueva = File.createTempFile("profile_" + actual.getId() + "_", ".img", directorio);
            try (InputStream entrada = context.getContentResolver().openInputStream(origen);
                 FileOutputStream salida = new FileOutputStream(nueva)) {
                if (entrada == null) throw new IOException("No se pudo abrir la foto");
                byte[] buffer = new byte[8192];
                int leidos;
                while ((leidos = entrada.read(buffer)) != -1) salida.write(buffer, 0, leidos);
            }
            BitmapFactory.Options opciones = new BitmapFactory.Options();
            opciones.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(nueva.getAbsolutePath(), opciones);
            if (opciones.outWidth <= 0 || opciones.outHeight <= 0)
                throw new IOException("El archivo no es una imagen válida");
            ContentValues values = new ContentValues();
            values.put("photo_uri", Uri.fromFile(nueva).toString());
            UsuarioLocal actualizado = actualizarUsuarioActual(values, actual.getId());
            eliminarFotoAnterior(actual.getFotoUri(), directorio, nueva);
            return actualizado;
        } catch (IOException | RuntimeException exception) {
            if (nueva != null) nueva.delete();
            throw new IllegalStateException("No se pudo guardar la foto de perfil", exception);
        }
    }

    private UsuarioLocal actualizarUsuarioActual(ContentValues values, @Nullable Long cuentaEsperada) {
        SQLiteDatabase db = database.getWritableDatabase();
        db.beginTransaction();
        try {
            UsuarioLocal actual = obtenerUsuarioActual();
            if (actual == null || (cuentaEsperada != null && actual.getId() != cuentaEsperada))
                throw new IllegalStateException("La sesión ya no está disponible");
            if (db.update("users", values, "_id = ?", new String[]{Long.toString(actual.getId())}) != 1)
                throw new IllegalStateException("No se pudo actualizar el perfil");
            UsuarioLocal actualizado = obtenerUsuarioActual();
            db.setTransactionSuccessful();
            return actualizado;
        } finally {
            db.endTransaction();
        }
    }

    private static void eliminarFotoAnterior(String fotoUri, File directorio, File nueva) {
        if (fotoUri == null) return;
        Uri uri = Uri.parse(fotoUri);
        if (!"file".equals(uri.getScheme()) || uri.getPath() == null) return;
        File anterior = new File(uri.getPath());
        try {
            if (directorio.equals(anterior.getParentFile()) && !anterior.equals(nueva)) anterior.delete();
        } catch (SecurityException ignored) {
            // La limpieza de una foto anterior no invalida el perfil que ya se guardó.
        }
    }

    private static void guardarSesion(SQLiteDatabase db, long userId) {
        ContentValues session = new ContentValues();
        session.put("_id", 1);
        session.put("user_id", userId);
        db.replaceOrThrow("session", null, session);
    }

    private static UsuarioLocal leerUsuario(Cursor cursor) {
        return new UsuarioLocal(cursor.getLong(cursor.getColumnIndexOrThrow("_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getString(cursor.getColumnIndexOrThrow("email")),
                cursor.getString(cursor.getColumnIndexOrThrow("location")),
                cursor.getString(cursor.getColumnIndexOrThrow("role")),
                cursor.getString(cursor.getColumnIndexOrThrow("description")),
                cursor.getString(cursor.getColumnIndexOrThrow("photo_uri")));
    }

    private static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static byte[] derivarPassword(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PASSWORD_ITERATIONS, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("No se pudo procesar la contraseña", exception);
        } finally {
            spec.clearPassword();
        }
    }

    @Override
    public void close() { database.close(); }

    private static final class Database extends SQLiteOpenHelper {
        Database(Context context, String name) { super(context, name, null, 3); }

        @Override
        public void onConfigure(SQLiteDatabase db) { db.setForeignKeyConstraintsEnabled(true); }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE users (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " name TEXT NOT NULL, email TEXT NOT NULL UNIQUE,"
                    + " password_hash BLOB NOT NULL, password_salt BLOB NOT NULL,"
                    + " location TEXT NOT NULL, role TEXT NOT NULL"
                    + " CHECK (role IN ('adoptante', 'publicador')),"
                    + " description TEXT NOT NULL DEFAULT '', photo_uri TEXT)");
            db.execSQL("CREATE TABLE session (_id INTEGER PRIMARY KEY CHECK (_id = 1),"
                    + " user_id INTEGER NOT NULL REFERENCES users(_id))");
            prepararCuentasDePrueba(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 2) {
                db.execSQL("ALTER TABLE users ADD COLUMN description TEXT NOT NULL DEFAULT ''");
                db.execSQL("ALTER TABLE users ADD COLUMN photo_uri TEXT");
            }
            if (oldVersion < 3) prepararCuentasDePrueba(db);
        }

        private static void prepararCuentasDePrueba(SQLiteDatabase db) {
            prepararCuentaDePrueba(db, "adoptante.prueba@pawmatch.local", "a@pm.test",
                    UsuarioLocal.ADOPTANTE, "Alex Demo");
            prepararCuentaDePrueba(db, "publicador.prueba@pawmatch.local", "p@pm.test",
                    UsuarioLocal.PUBLICADOR, "Refugio Patitas Demo");
        }

        private static void prepararCuentaDePrueba(SQLiteDatabase db, String emailAnterior,
                                                   String emailNuevo, String rol, String nombre) {
            try (Cursor existente = db.query("users", new String[]{"_id"}, "email = ?",
                    new String[]{emailNuevo}, null, null, null)) {
                if (existente.moveToFirst()) return;
            }
            Long idAnterior = null;
            try (Cursor anterior = db.query("users", new String[]{"_id"}, "email = ? AND role = ?",
                    new String[]{emailAnterior, rol}, null, null, null)) {
                if (anterior.moveToFirst()) idAnterior = anterior.getLong(0);
            }
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            ContentValues values = new ContentValues();
            values.put("email", emailNuevo);
            values.put("password_hash", derivarPassword("123456", salt));
            values.put("password_salt", salt);
            if (idAnterior != null) {
                db.update("users", values, "_id = ?", new String[]{Long.toString(idAnterior)});
            } else {
                values.put("name", nombre);
                values.put("location", "Santa Fe");
                values.put("role", rol);
                db.insertOrThrow("users", null, values);
            }
        }
    }
}
