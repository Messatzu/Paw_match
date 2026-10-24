package com.example.paw_match.data.repository;

import android.content.Context;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.util.Patterns;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.paw_match.data.model.UsuarioLocal;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class LocalUserRepositoryTest {
    private Context context;
    private String databaseName;
    private LocalUserRepository repository;
    private final List<File> archivosTemporales = new ArrayList<>();

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        databaseName = "test_users_" + UUID.randomUUID() + ".db";
        repository = new LocalUserRepository(context, databaseName);
    }

    @After
    public void tearDown() {
        repository.close();
        context.deleteDatabase(databaseName);
        for (File archivo : archivosTemporales) archivo.delete();
    }

    @Test
    public void cuentaYSesionPersistenYLogoutNoBorraElUsuario() {
        UsuarioLocal creado = repository.registrar("  Refugio de prueba  ",
                "  REFUGIO@Ejemplo.com  ", "clave123", "  Santa Fe  ", UsuarioLocal.PUBLICADOR);
        assertNotNull(creado);
        long id = creado.getId();

        reabrirRepositorio();
        UsuarioLocal restaurado = repository.obtenerUsuarioActual();
        assertNotNull(restaurado);
        assertEquals(id, restaurado.getId());
        assertEquals("Refugio de prueba", restaurado.getNombre());
        assertEquals("refugio@ejemplo.com", restaurado.getEmail());
        assertEquals("Santa Fe", restaurado.getUbicacion());
        assertTrue(restaurado.esPublicador());

        repository.cerrarSesion();
        reabrirRepositorio();
        assertNull(repository.obtenerUsuarioActual());
        UsuarioLocal ingresado = repository.iniciarSesion("REFUGIO@EJEMPLO.COM", "clave123");
        assertNotNull(ingresado);
        assertEquals(id, ingresado.getId());
    }

    @Test
    public void rechazaCorreoDuplicadoSinSobrescribirLaCuenta() {
        UsuarioLocal original = repository.registrar("Adoptante", "persona@ejemplo.com",
                "original123", "Rosario", UsuarioLocal.ADOPTANTE);
        assertNotNull(original);
        repository.cerrarSesion();

        assertNull(repository.registrar("Otra persona", " PERSONA@EJEMPLO.COM ",
                "otra123", "Santa Fe", UsuarioLocal.PUBLICADOR));
        assertNull(repository.obtenerUsuarioActual());
        assertNull(repository.iniciarSesion("persona@ejemplo.com", "otra123"));
        UsuarioLocal ingresado = repository.iniciarSesion("persona@ejemplo.com", "original123");
        assertNotNull(ingresado);
        assertEquals("Adoptante", ingresado.getNombre());
        assertEquals("Rosario", ingresado.getUbicacion());
        assertFalse(ingresado.esPublicador());
    }

    @Test
    public void validaCredencialesExactasYPermiteVariasCuentas() {
        repository.registrar("Uno", "uno@ejemplo.com", "  clave123  ",
                "Santa Fe", UsuarioLocal.ADOPTANTE);
        repository.registrar("Dos", "dos@ejemplo.com", "otraClave123",
                "Rosario", UsuarioLocal.PUBLICADOR);
        repository.cerrarSesion();

        assertNull(repository.iniciarSesion("inexistente@ejemplo.com", "  clave123  "));
        assertNull(repository.iniciarSesion("uno@ejemplo.com", "clave123"));
        assertNull(repository.obtenerUsuarioActual());
        UsuarioLocal uno = repository.iniciarSesion("uno@ejemplo.com", "  clave123  ");
        assertNotNull(uno);
        assertEquals("Uno", uno.getNombre());
        assertEquals(UsuarioLocal.ADOPTANTE, uno.getRol());
        UsuarioLocal dos = repository.iniciarSesion("dos@ejemplo.com", "otraClave123");
        assertNotNull(dos);
        assertEquals("Dos", dos.getNombre());
        assertEquals(UsuarioLocal.PUBLICADOR, dos.getRol());
    }

    @Test public void edicionDePerfilPersisteYConservaLaCuentaYLasCredenciales() {
        UsuarioLocal original = repository.registrar("Ana", "ana@ejemplo.com", "clave123",
                "Santa Fe", UsuarioLocal.ADOPTANTE);

        UsuarioLocal actualizado = repository.actualizarPerfil("  Ana María  ", "  Rosario  ", "  Tengo un patio  ");

        assertEquals(original.getId(), actualizado.getId());
        assertEquals(original.getEmail(), actualizado.getEmail());
        assertEquals(original.getRol(), actualizado.getRol());
        assertEquals("Ana María", actualizado.getNombre());
        assertEquals("Rosario", actualizado.getUbicacion());
        assertEquals("Tengo un patio", actualizado.getDescripcion());
        reabrirRepositorio();
        assertEquals("Tengo un patio", repository.obtenerUsuarioActual().getDescripcion());
        repository.cerrarSesion();
        UsuarioLocal ingresado = repository.iniciarSesion("ana@ejemplo.com", "clave123");
        assertNotNull(ingresado);
        assertEquals(original.getId(), ingresado.getId());
        assertEquals("Ana María", ingresado.getNombre());
        assertEquals("Rosario", ingresado.getUbicacion());
    }

    @Test public void edicionSoloModificaLaCuentaActivaYPermiteVaciarLaDescripcion() {
        repository.registrar("Ana", "ana@ejemplo.com", "clave123", "Santa Fe", UsuarioLocal.ADOPTANTE);
        repository.actualizarPerfil("Ana María", "Rosario", "Mi descripción");
        repository.registrar("Refugio", "refugio@ejemplo.com", "clave456", "Santa Fe", UsuarioLocal.PUBLICADOR);
        assertEquals("", repository.obtenerUsuarioActual().getDescripcion());
        repository.actualizarPerfil("Patitas", "Paraná", "Rescatamos mascotas");

        UsuarioLocal ana = repository.iniciarSesion("ana@ejemplo.com", "clave123");
        assertEquals("Ana María", ana.getNombre());
        assertEquals("Mi descripción", ana.getDescripcion());
        repository.actualizarPerfil(ana.getNombre(), ana.getUbicacion(), "  ");
        assertEquals("", repository.obtenerUsuarioActual().getDescripcion());
        UsuarioLocal refugio = repository.iniciarSesion("refugio@ejemplo.com", "clave456");
        assertEquals("Patitas", refugio.getNombre());
        assertEquals("Paraná", refugio.getUbicacion());
        assertEquals("Rescatamos mascotas", refugio.getDescripcion());
    }

    @Test public void edicionValidaNombreUbicacionYSesion() {
        repository.registrar("Ana", "ana@ejemplo.com", "clave123", "Santa Fe", UsuarioLocal.ADOPTANTE);
        for (String[] datos : new String[][]{{null, "Rosario", "Bio"}, {"  ", "Rosario", "Bio"},
                {"Ana", null, "Bio"}, {"Ana", "  ", "Bio"}, {"Ana", "Rosario", null}}) {
            assertThrows(IllegalArgumentException.class,
                    () -> repository.actualizarPerfil(datos[0], datos[1], datos[2]));
        }
        assertEquals("Ana", repository.obtenerUsuarioActual().getNombre());
        assertEquals("Santa Fe", repository.obtenerUsuarioActual().getUbicacion());
        repository.cerrarSesion();
        assertThrows(IllegalStateException.class, () -> repository.actualizarPerfil("Otro", "Rosario", "Bio"));
        assertThrows(IllegalStateException.class, () -> repository.actualizarFotoPerfil(Uri.parse("content://foto")));
    }

    @Test public void fotoSeCopiaYSeConservaAlReabrirYEditarElTexto() throws Exception {
        repository.registrar("Ana", "ana@ejemplo.com", "clave123", "Santa Fe", UsuarioLocal.ADOPTANTE);
        File original = crearFoto(Color.GREEN);
        UsuarioLocal actualizado = repository.actualizarFotoPerfil(Uri.fromFile(original));
        File copia = registrarFotoGuardada(actualizado);
        assertNotEquals(Uri.fromFile(original).toString(), actualizado.getFotoUri());
        assertTrue(copia.isFile());
        assertTrue(original.delete());

        repository.actualizarPerfil("Ana María", "Rosario", "Mi descripción");
        reabrirRepositorio();
        assertEquals(actualizado.getFotoUri(), repository.obtenerUsuarioActual().getFotoUri());
        assertTrue(copia.isFile());

        File reemplazo = crearFoto(Color.BLUE);
        UsuarioLocal conOtraFoto = repository.actualizarFotoPerfil(Uri.fromFile(reemplazo));
        File nueva = registrarFotoGuardada(conOtraFoto);
        assertFalse(copia.exists());
        assertTrue(nueva.isFile());
        assertEquals("Ana María", conOtraFoto.getNombre());
        assertEquals("Mi descripción", conOtraFoto.getDescripcion());
    }

    @Test public void fotoInvalidaConservaLaFotoAnterior() throws Exception {
        repository.registrar("Ana", "ana@ejemplo.com", "clave123", "Santa Fe", UsuarioLocal.ADOPTANTE);
        UsuarioLocal conFoto = repository.actualizarFotoPerfil(Uri.fromFile(crearFoto(Color.GREEN)));
        File anterior = registrarFotoGuardada(conFoto);
        File invalida = File.createTempFile("perfil_invalido_", ".txt", context.getCacheDir());
        archivosTemporales.add(invalida);
        try (FileOutputStream salida = new FileOutputStream(invalida)) {
            salida.write("Esto no es una foto".getBytes(StandardCharsets.UTF_8));
        }

        assertThrows(IllegalStateException.class, () -> repository.actualizarFotoPerfil(Uri.fromFile(invalida)));

        assertEquals(conFoto.getFotoUri(), repository.obtenerUsuarioActual().getFotoUri());
        assertTrue(anterior.isFile());
    }

    @Test public void migracionDeVersionUnoConservaCuentaSesionYCredenciales() throws Exception {
        repository.close();
        byte[] salt = new byte[16];
        PBEKeySpec password = new PBEKeySpec("clave123".toCharArray(), salt, 120_000, 256);
        byte[] hash;
        try {
            hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(password).getEncoded();
        } finally {
            password.clearPassword();
        }
        try (SQLiteDatabase db = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null)) {
            db.execSQL("CREATE TABLE users (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL,"
                    + " email TEXT NOT NULL UNIQUE, password_hash BLOB NOT NULL, password_salt BLOB NOT NULL,"
                    + " location TEXT NOT NULL, role TEXT NOT NULL CHECK (role IN ('adoptante', 'publicador')))");
            db.execSQL("CREATE TABLE session (_id INTEGER PRIMARY KEY CHECK (_id = 1),"
                    + " user_id INTEGER NOT NULL REFERENCES users(_id))");
            ContentValues values = new ContentValues();
            values.put("_id", 15);
            values.put("name", "Ana");
            values.put("email", "ana@ejemplo.com");
            values.put("password_hash", hash);
            values.put("password_salt", salt);
            values.put("location", "Santa Fe");
            values.put("role", UsuarioLocal.ADOPTANTE);
            db.insertOrThrow("users", null, values);
            db.execSQL("INSERT INTO session (_id, user_id) VALUES (1, 15)");
            db.setVersion(1);
        }
        repository = new LocalUserRepository(context, databaseName);

        UsuarioLocal migrado = repository.obtenerUsuarioActual();

        assertNotNull(migrado);
        assertEquals(15, migrado.getId());
        assertEquals("Ana", migrado.getNombre());
        assertEquals("", migrado.getDescripcion());
        assertNull(migrado.getFotoUri());
        assertNotNull(repository.iniciarSesion("ana@ejemplo.com", "clave123"));
        repository.actualizarPerfil("Ana María", "Rosario", "Descripción nueva");
        reabrirRepositorio();
        assertEquals("Descripción nueva", repository.obtenerUsuarioActual().getDescripcion());
    }

    @Test public void accesosDePruebaSonCortosYPersistenSinIniciarSesionAutomatica() {
        assertNull(repository.obtenerUsuarioActual());
        assertTrue(Patterns.EMAIL_ADDRESS.matcher("a@pm.test").matches());
        assertTrue(Patterns.EMAIL_ADDRESS.matcher("p@pm.test").matches());

        UsuarioLocal adoptante = repository.iniciarSesion("a@pm.test", "123456");
        assertNotNull(adoptante);
        assertEquals(UsuarioLocal.ADOPTANTE, adoptante.getRol());
        UsuarioLocal publicador = repository.iniciarSesion("p@pm.test", "123456");
        assertNotNull(publicador);
        assertEquals(UsuarioLocal.PUBLICADOR, publicador.getRol());
        assertNotEquals(adoptante.getId(), publicador.getId());

        reabrirRepositorio();
        assertEquals(publicador.getId(), repository.obtenerUsuarioActual().getId());
        repository.cerrarSesion();
        assertNull(repository.iniciarSesion("p@pm.test", "incorrecta"));
        assertNull(repository.obtenerUsuarioActual());
        assertNotNull(repository.iniciarSesion("a@pm.test", "123456"));
    }

    @Test public void migracionAcortaCredencialesAnterioresConservandoPerfilIdYSesion() {
        UsuarioLocal adoptante = repository.registrar("Adoptante de prueba", "adoptante.prueba@pawmatch.local",
                "PawMatch123", "Rosario", UsuarioLocal.ADOPTANTE);
        repository.actualizarPerfil("Ana", "Rosario", "Mi descripción");
        UsuarioLocal publicador = repository.registrar("Refugio de prueba", "publicador.prueba@pawmatch.local",
                "PawMatch123", "Santa Fe", UsuarioLocal.PUBLICADOR);
        repository.actualizarPerfil("Patitas", "Santa Fe", "Mi refugio");

        reabrirComoVersionDos(true);

        UsuarioLocal actual = repository.obtenerUsuarioActual();
        assertNotNull(actual);
        assertEquals(publicador.getId(), actual.getId());
        assertEquals("p@pm.test", actual.getEmail());
        assertEquals("Patitas", actual.getNombre());
        assertEquals("Mi refugio", actual.getDescripcion());
        assertNotNull(repository.iniciarSesion("p@pm.test", "123456"));
        UsuarioLocal ana = repository.iniciarSesion("a@pm.test", "123456");
        assertNotNull(ana);
        assertEquals(adoptante.getId(), ana.getId());
        assertEquals("Ana", ana.getNombre());
        assertEquals("Rosario", ana.getUbicacion());
        assertEquals("Mi descripción", ana.getDescripcion());
        assertNull(repository.iniciarSesion("adoptante.prueba@pawmatch.local", "PawMatch123"));
        assertNull(repository.iniciarSesion("publicador.prueba@pawmatch.local", "PawMatch123"));
        assertEquals(ana.getId(), repository.obtenerUsuarioActual().getId());
    }

    @Test public void migracionRespetaOtrasCuentasYLosPerfilesDePruebaYaExistentes() {
        UsuarioLocal demo = repository.iniciarSesion("a@pm.test", "123456");
        repository.actualizarPerfil("Alex editado", "Rosario", "Mi presentación");
        UsuarioLocal otra = repository.registrar("Persona", "persona@ejemplo.com", "otraClave123",
                "Paraná", UsuarioLocal.ADOPTANTE);

        reabrirComoVersionDos(false);

        assertEquals(otra.getId(), repository.obtenerUsuarioActual().getId());
        assertNotNull(repository.iniciarSesion("persona@ejemplo.com", "otraClave123"));
        UsuarioLocal conservado = repository.iniciarSesion("a@pm.test", "123456");
        assertNotNull(conservado);
        assertEquals(demo.getId(), conservado.getId());
        assertEquals("Alex editado", conservado.getNombre());
        assertEquals("Rosario", conservado.getUbicacion());
        assertEquals("Mi presentación", conservado.getDescripcion());
    }

    private void reabrirComoVersionDos(boolean quitarAccesosNuevos) {
        repository.close();
        try (SQLiteDatabase db = SQLiteDatabase.openDatabase(context.getDatabasePath(databaseName).getAbsolutePath(),
                null, SQLiteDatabase.OPEN_READWRITE)) {
            if (quitarAccesosNuevos)
                db.delete("users", "email IN (?, ?)", new String[]{"a@pm.test", "p@pm.test"});
            db.setVersion(2);
        }
        repository = new LocalUserRepository(context, databaseName);
    }

    private File crearFoto(int color) throws Exception {
        File foto = File.createTempFile("perfil_original_", ".png", context.getCacheDir());
        archivosTemporales.add(foto);
        Bitmap bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(color);
        try (FileOutputStream salida = new FileOutputStream(foto)) {
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, salida));
        } finally {
            bitmap.recycle();
        }
        return foto;
    }

    private File registrarFotoGuardada(UsuarioLocal usuario) {
        File foto = new File(Uri.parse(usuario.getFotoUri()).getPath());
        archivosTemporales.add(foto);
        return foto;
    }

    private void reabrirRepositorio() {
        repository.close();
        repository = new LocalUserRepository(context, databaseName);
    }
}
