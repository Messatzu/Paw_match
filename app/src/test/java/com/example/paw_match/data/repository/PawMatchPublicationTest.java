package com.example.paw_match.data.repository;

import com.example.paw_match.data.model.DetalleMascota;
import com.example.paw_match.data.model.EstadoSolicitud;
import com.example.paw_match.data.model.Mascota;
import com.example.paw_match.data.model.UsuarioLocal;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class PawMatchPublicationTest {
    private final PawMatchRepository repository = new PawMatchRepository();
    private final UsuarioLocal publicador = cuenta(22, "Refugio", UsuarioLocal.PUBLICADOR);
    private final UsuarioLocal otroPublicador = cuenta(23, "Otro refugio", UsuarioLocal.PUBLICADOR);
    private final UsuarioLocal adoptante = cuenta(11, "Ana", UsuarioLocal.ADOPTANTE);
    private static final String FOTO = "content://fotos/mascotas/roco";

    private static UsuarioLocal cuenta(long id, String nombre, String rol) {
        return new UsuarioLocal(id, nombre, id + "@ejemplo.com", "Santa Fe", rol);
    }

    private Mascota publicar(UsuarioLocal cuenta) {
        assertEquals(PawMatchRepository.Resultado.OK,
                repository.publicarMascota("Roco", "8 meses", "Rosario", "Juguetón y cariñoso", FOTO, cuenta));
        return repository.obtenerMascotasPublicadas(cuenta).get(0);
    }

    private String solicitar(Mascota mascota) {
        assertEquals(PawMatchRepository.Resultado.OK,
                repository.crearSolicitud(mascota.getId(), repository.obtenerAdoptanteDemo().getId(), adoptante));
        List<com.example.paw_match.data.model.SolicitudAdopcion> solicitudes = repository.obtenerSolicitudesVisibles(adoptante);
        return solicitudes.get(solicitudes.size() - 1).getId();
    }

    @Test public void publicarGuardaEdadYUbicacionSeparadasYActualizaPublicacionesYExplorar() {
        List<Mascota> antes = repository.obtenerMascotas();
        int[] cambios = {0};
        repository.addListener(() -> cambios[0]++);

        assertEquals(PawMatchRepository.Resultado.OK, repository.publicarMascota(
                "  Roco  ", "  8 meses  ", "  Rosario  ", "  Juguetón y cariñoso  ", "  " + FOTO + "  ", publicador));

        Mascota mascota = repository.obtenerMascotasPublicadas(publicador).get(0);
        assertEquals("Roco", mascota.getNombre());
        assertEquals("8 meses", mascota.getEdad());
        assertEquals("Rosario", mascota.getCiudad());
        assertEquals("Juguetón y cariñoso", mascota.getHistoria());
        assertEquals(FOTO, mascota.getImagenUri());
        assertSame(mascota, repository.buscarMascota(mascota.getId()));
        assertSame(mascota, repository.obtenerMascotasParaExplorar(adoptante).get(0));
        assertEquals(7, repository.obtenerMascotasPublicadas(publicador).size());
        assertEquals(7, repository.obtenerMascotas().size());
        assertEquals(6, antes.size());
        assertEquals(1, cambios[0]);
    }

    @Test public void detalleDePublicacionConservaSusDatosYLaCuentaQuePublico() {
        Mascota mascota = publicar(publicador);

        for (UsuarioLocal usuario : new UsuarioLocal[]{publicador, adoptante}) {
            DetalleMascota detalle = repository.obtenerDetalleMascota(mascota.getId(), null, usuario);
            assertNotNull(detalle);
            assertSame(mascota, detalle.getMascota());
            assertSame(publicador, detalle.getPublicador());
            assertEquals(publicador.getNombre(), detalle.getRefugio().getNombre());
            assertEquals(publicador.getEmail(), detalle.getRefugio().getContacto());
            assertEquals(DetalleMascota.Estado.DISPONIBLE, detalle.getEstado());
            assertEquals(!usuario.esPublicador(), detalle.puedeSolicitar());
            assertFalse(detalle.puedeResponder());
            assertNull(detalle.getSolicitud());
            assertEquals("", detalle.getMascota().getPeso());
            assertEquals("", detalle.getMascota().getNacimiento());
        }
    }

    @Test public void publicacionesNuevasPertenecenAlPublicadorQueLasCreo() {
        Mascota propia = publicar(publicador);
        Mascota ajena = publicar(otroPublicador);

        assertSame(propia, repository.obtenerMascotasPublicadas(publicador).get(0));
        assertSame(ajena, repository.obtenerMascotasPublicadas(otroPublicador).get(0));
        assertEquals(7, repository.obtenerMascotasPublicadas(publicador).size());
        assertEquals(7, repository.obtenerMascotasPublicadas(otroPublicador).size());
        assertEquals(repository.obtenerMascotasPublicadas(publicador), repository.obtenerMascotasPublicadas(
                cuenta(publicador.getId(), publicador.getNombre(), UsuarioLocal.PUBLICADOR)));
        assertTrue(repository.obtenerMascotasPublicadas(adoptante).isEmpty());
        assertTrue(repository.obtenerMascotasPublicadas(null).isEmpty());
    }

    @Test public void publicarRequiereUnPublicadorValidoYTodosLosCampos() {
        int[] cambios = {0};
        repository.addListener(() -> cambios[0]++);
        for (UsuarioLocal usuario : new UsuarioLocal[]{null, adoptante,
                cuenta(0, "Sin cuenta", UsuarioLocal.PUBLICADOR), cuenta(22, "Rol inválido", "otro")}) {
            assertEquals(PawMatchRepository.Resultado.NO_AUTORIZADO,
                    repository.publicarMascota("Roco", "8 meses", "Rosario", "Descripción", FOTO, usuario));
        }
        for (int campo = 0; campo < 5; campo++) {
            for (String vacio : new String[]{null, "", "  \n\t"}) {
                String[] datos = {"Roco", "8 meses", "Rosario", "Descripción", FOTO};
                datos[campo] = vacio;
                assertEquals(PawMatchRepository.Resultado.DATOS_INVALIDOS,
                        repository.publicarMascota(datos[0], datos[1], datos[2], datos[3], datos[4], publicador));
            }
        }
        assertEquals(6, repository.obtenerMascotas().size());
        assertEquals(0, cambios[0]);
    }

    @Test public void solicitudesDeUnaMascotaNuevaLleganASuPublicadorYConservanAdopcionYChat() {
        Mascota mascota = publicar(publicador);
        String solicitud = solicitar(mascota);

        assertEquals(1, repository.obtenerSolicitudesVisibles(publicador).size());
        assertTrue(repository.obtenerSolicitudesVisibles(otroPublicador).isEmpty());
        assertNull(repository.obtenerDetalleMascota(mascota.getId(), solicitud, otroPublicador));
        assertEquals(PawMatchRepository.Resultado.NO_AUTORIZADO,
                repository.resolverSolicitud(solicitud, EstadoSolicitud.APROBADA, otroPublicador));
        assertEquals(PawMatchRepository.Resultado.OK,
                repository.resolverSolicitud(solicitud, EstadoSolicitud.APROBADA, publicador));

        assertNotNull(repository.obtenerChat(solicitud, adoptante));
        assertSame(repository.obtenerChat(solicitud, adoptante), repository.obtenerChat(solicitud, publicador));
        assertEquals(mascota.getId(), repository.obtenerHistorialPerfil(publicador).get(0).getMascota().getId());
        assertEquals(mascota.getId(), repository.obtenerHistorialPerfil(adoptante).get(0).getMascota().getId());
        assertTrue(repository.obtenerHistorialPerfil(otroPublicador).isEmpty());
        assertEquals(6, repository.obtenerMascotasPublicadas(publicador).size());
    }

    @Test public void cancelarSolicitudDeMascotaNuevaLaDevuelveAExplorar() {
        Mascota mascota = publicar(publicador);
        String solicitud = solicitar(mascota);

        assertEquals(PawMatchRepository.Resultado.OK, repository.cancelarSolicitud(solicitud, adoptante));

        assertSame(mascota, repository.obtenerMascotasParaExplorar(adoptante).get(0));
        assertSame(mascota, repository.obtenerMascotasPublicadas(publicador).get(0));
        assertTrue(repository.obtenerSolicitudesVisibles(publicador).isEmpty());
    }

    @Test public void publicacionesDadasDeBajaSoloQuedanEnElHistorialDeSuPublicador() {
        Mascota mascota = publicar(publicador);
        assertEquals(PawMatchRepository.Resultado.OK,
                repository.registrarBajaEnHistorial(mascota.getId(), mascota.getRefugioId()));

        assertEquals(6, repository.obtenerMascotasPublicadas(publicador).size());
        assertEquals(6, repository.obtenerMascotasParaExplorar(adoptante).size());
        assertEquals(mascota.getId(), repository.obtenerHistorialPerfil(publicador).get(0).getMascota().getId());
        assertTrue(repository.obtenerHistorialPerfil(otroPublicador).isEmpty());
    }

    @Test public void variasPublicacionesNoSeSobrescribenYReiniciarRestauraLaDemo() {
        Mascota primera = publicar(publicador);
        Mascota segunda = publicar(publicador);
        assertNotEquals(primera.getId(), segunda.getId());
        assertSame(segunda, repository.obtenerMascotasPublicadas(publicador).get(0));
        assertSame(primera, repository.obtenerMascotasPublicadas(publicador).get(1));
        assertEquals(8, repository.obtenerMascotas().size());

        repository.reiniciarDemo();

        assertEquals(6, repository.obtenerMascotas().size());
        assertEquals(6, repository.obtenerMascotasPublicadas(publicador).size());
        assertNull(repository.buscarMascota(primera.getId()));
        assertNull(repository.obtenerDetalleMascota(segunda.getId(), null, publicador));
    }
}
