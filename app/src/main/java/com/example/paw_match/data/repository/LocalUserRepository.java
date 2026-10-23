package com.example.paw_match.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Patterns;
import androidx.annotation.Nullable;
import com.example.paw_match.data.model.UsuarioLocal;
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

    public LocalUserRepository(Context context) {
        this(context, "paw_match_users.db");
    }

    // Permite verificar la persistencia en una base aislada sin borrar las cuentas de la app.
    LocalUserRepository(Context context, String databaseName) {
        database = new Database(context.getApplicationContext(), databaseName);
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
                cursor.getString(cursor.getColumnIndexOrThrow("role")));
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
        Database(Context context, String name) { super(context, name, null, 1); }

        @Override
        public void onConfigure(SQLiteDatabase db) { db.setForeignKeyConstraintsEnabled(true); }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE users (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " name TEXT NOT NULL, email TEXT NOT NULL UNIQUE,"
                    + " password_hash BLOB NOT NULL, password_salt BLOB NOT NULL,"
                    + " location TEXT NOT NULL, role TEXT NOT NULL"
                    + " CHECK (role IN ('adoptante', 'publicador')))");
            db.execSQL("CREATE TABLE session (_id INTEGER PRIMARY KEY CHECK (_id = 1),"
                    + " user_id INTEGER NOT NULL REFERENCES users(_id))");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // La primera versión no tiene migraciones; futuras versiones conservarán las cuentas.
        }
    }
}
