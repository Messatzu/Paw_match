package com.example.paw_match.data.repository;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.example.paw_match.data.model.UsuarioLocal;
import java.util.UUID;
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

    private void reabrirRepositorio() {
        repository.close();
        repository = new LocalUserRepository(context, databaseName);
    }
}
