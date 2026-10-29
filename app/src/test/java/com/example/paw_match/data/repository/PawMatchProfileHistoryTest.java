package com.example.paw_match.data.repository;

import com.example.paw_match.data.model.EstadoSolicitud;
import com.example.paw_match.data.model.HistorialMascota;
import com.example.paw_match.data.model.UsuarioLocal;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class PawMatchProfileHistoryTest {
    private static UsuarioLocal cuenta(long id, String nombre, String rol) {
        return new UsuarioLocal(id, nombre, id + "@ejemplo.com", "Santa Fe", rol);
    }

    private static String solicitar(PawMatchRepository repository, String mascota, UsuarioLocal adoptante) {
        assertEquals(PawMatchRepository.Resultado.OK,
                repository.crearSolicitud(mascota, repository.obtenerAdoptanteDemo().getId(), adoptante));
        List<com.example.paw_match.data.model.SolicitudAdopcion> solicitudes = repository.obtenerSolicitudes();
        return solicitudes.get(solicitudes.size() - 1).getId();
    }

    @Test public void historialDelAdoptanteIncluyeSoloSusAdopcionesAprobadas() {
        PawMatchRepository repository = new PawMatchRepository();
        String adoptante = repository.obtenerAdoptanteDemo().getId();
        repository.crearSolicitud("m1", adoptante);
        repository.crearSolicitud("m2", adoptante);
        repository.crearSolicitud("m3", adoptante);
        repository.resolverSolicitud(repository.obtenerSolicitudes().get(0).getId(), EstadoSolicitud.APROBADA);
        repository.resolverSolicitud(repository.obtenerSolicitudes().get(1).getId(), EstadoSolicitud.RECHAZADA);

        List<HistorialMascota> historial = repository.obtenerHistorialAdoptante(adoptante);
        assertEquals(1, historial.size());
        assertEquals("m1", historial.get(0).getMascota().getId());
        assertEquals(HistorialMascota.Estado.ADOPTADA, historial.get(0).getEstado());
        assertTrue(repository.obtenerHistorialAdoptante("otro-adoptante").isEmpty());
    }

    @Test public void historialDelPublicadorIncluyeAdoptadasYBajasDeSuRefugio() {
        PawMatchRepository repository = new PawMatchRepository();
        String adoptante = repository.obtenerAdoptanteDemo().getId();
        String refugio = repository.obtenerRefugioDemo().getId();
        repository.crearSolicitud("m1", adoptante);
        repository.resolverSolicitud(repository.obtenerSolicitudes().get(0).getId(), EstadoSolicitud.APROBADA);
        assertEquals(PawMatchRepository.Resultado.YA_RESUELTA, repository.registrarBajaEnHistorial("m1", refugio));
        assertEquals(PawMatchRepository.Resultado.OK, repository.registrarBajaEnHistorial("m2", refugio));

        List<HistorialMascota> historial = repository.obtenerHistorialRefugio(refugio);
        assertEquals(2, historial.size());
        assertEquals(HistorialMascota.Estado.ADOPTADA, historial.get(0).getEstado());
        assertEquals("m1", historial.get(0).getMascota().getId());
        assertEquals(HistorialMascota.Estado.PUBLICACION_DADA_DE_BAJA, historial.get(1).getEstado());
        assertEquals("m2", historial.get(1).getMascota().getId());
        assertTrue(repository.obtenerHistorialRefugio("otro-refugio").isEmpty());
        assertEquals(1, repository.obtenerHistorialAdoptante(adoptante).size());
    }

    @Test public void bajasValidanPropietarioYDuplicadosYElReinicioLimpiaElHistorial() {
        PawMatchRepository repository = new PawMatchRepository();
        String refugio = repository.obtenerRefugioDemo().getId();
        assertEquals(PawMatchRepository.Resultado.DATOS_INVALIDOS,
                repository.registrarBajaEnHistorial("inexistente", refugio));
        assertEquals(PawMatchRepository.Resultado.DATOS_INVALIDOS,
                repository.registrarBajaEnHistorial("m1", "otro-refugio"));
        int[] notificaciones = {0};
        repository.addListener(() -> notificaciones[0]++);
        assertEquals(PawMatchRepository.Resultado.OK, repository.registrarBajaEnHistorial("m1", refugio));
        assertEquals(PawMatchRepository.Resultado.DUPLICADA, repository.registrarBajaEnHistorial("m1", refugio));
        List<HistorialMascota> antes = repository.obtenerHistorialRefugio(refugio);
        repository.reiniciarDemo();
        assertTrue(repository.obtenerHistorialRefugio(refugio).isEmpty());
        assertEquals(1, antes.size());
        assertEquals(2, notificaciones[0]);
    }

    @Test public void detalleVinculaLaMascotaConSuAdoptanteRealYLaMismaConversacion() {
        PawMatchRepository repository = new PawMatchRepository();
        UsuarioLocal adoptante = cuenta(11, "Ana", UsuarioLocal.ADOPTANTE);
        UsuarioLocal publicador = cuenta(22, "Refugio", UsuarioLocal.PUBLICADOR);
        String solicitud = solicitar(repository, "m1", adoptante);
        assertNull(repository.obtenerDetalleHistorial("m1", solicitud, publicador));
        assertTrue(repository.obtenerHistorialPerfil(adoptante).isEmpty());
        repository.resolverSolicitud(solicitud, EstadoSolicitud.APROBADA, publicador);
        repository.enviarMensajeChat(solicitud, publicador, "Hola Ana");

        for (UsuarioLocal usuario : new UsuarioLocal[]{adoptante, publicador}) {
            HistorialMascota detalle = repository.obtenerDetalleHistorial("m1", solicitud, usuario);
            assertNotNull(detalle);
            assertEquals("Luna", detalle.getMascota().getNombre());
            assertEquals(HistorialMascota.Estado.ADOPTADA, detalle.getEstado());
            assertSame(adoptante, detalle.getAdoptante());
            assertEquals("11@ejemplo.com", detalle.getAdoptante().getEmail());
            assertEquals(solicitud, detalle.getSolicitudId());
            assertEquals("Hola Ana", repository.obtenerChat(detalle.getSolicitudId(), usuario)
                    .getUltimoMensaje().getTexto());
            assertEquals(1, repository.obtenerHistorialPerfil(usuario).size());
        }
    }

    @Test public void cadaAdoptanteVeSoloSuAdopcionYElDetalleValidaAmbosIdentificadores() {
        PawMatchRepository repository = new PawMatchRepository();
        UsuarioLocal ana = cuenta(11, "Ana", UsuarioLocal.ADOPTANTE);
        UsuarioLocal juan = cuenta(12, "Juan", UsuarioLocal.ADOPTANTE);
        UsuarioLocal publicador = cuenta(22, "Refugio", UsuarioLocal.PUBLICADOR);
        String luna = solicitar(repository, "m1", ana);
        String milo = solicitar(repository, "m2", juan);
        repository.resolverSolicitud(luna, EstadoSolicitud.APROBADA, publicador);
        repository.resolverSolicitud(milo, EstadoSolicitud.APROBADA, publicador);

        assertEquals("m1", repository.obtenerHistorialPerfil(ana).get(0).getMascota().getId());
        assertEquals("m2", repository.obtenerHistorialPerfil(juan).get(0).getMascota().getId());
        assertEquals(1, repository.obtenerHistorialPerfil(ana).size());
        assertEquals(1, repository.obtenerHistorialPerfil(juan).size());
        assertEquals(2, repository.obtenerHistorialPerfil(publicador).size());
        assertNull(repository.obtenerDetalleHistorial("m1", luna, juan));
        assertNull(repository.obtenerDetalleHistorial("m2", milo, ana));
        assertNull(repository.obtenerDetalleHistorial("m1", milo, publicador));
        assertNull(repository.obtenerDetalleHistorial("m2", luna, publicador));
        assertNull(repository.obtenerDetalleHistorial("m1", null, publicador));
        assertNull(repository.obtenerDetalleHistorial("inexistente", luna, publicador));
        assertSame(juan, repository.obtenerDetalleHistorial("m2", milo, publicador).getAdoptante());
    }

    @Test public void otrasCuentasNoPuedenLeerElDetalleDeUnaAdopcion() {
        PawMatchRepository repository = new PawMatchRepository();
        UsuarioLocal adoptante = cuenta(11, "Ana", UsuarioLocal.ADOPTANTE);
        UsuarioLocal publicador = cuenta(22, "Refugio", UsuarioLocal.PUBLICADOR);
        String solicitud = solicitar(repository, "m1", adoptante);
        repository.resolverSolicitud(solicitud, EstadoSolicitud.APROBADA, publicador);
        for (UsuarioLocal usuario : new UsuarioLocal[]{null,
                cuenta(33, "Otro adoptante", UsuarioLocal.ADOPTANTE),
                cuenta(44, "Otro publicador", UsuarioLocal.PUBLICADOR),
                cuenta(11, "Rol incorrecto", UsuarioLocal.PUBLICADOR),
                cuenta(0, "Sin cuenta", UsuarioLocal.ADOPTANTE)}) {
            assertTrue(repository.obtenerHistorialPerfil(usuario).isEmpty());
            assertNull(repository.obtenerDetalleHistorial("m1", solicitud, usuario));
        }
    }

    @Test public void solicitudRechazadaNoTieneDetalleDeAdopcion() {
        PawMatchRepository repository = new PawMatchRepository();
        UsuarioLocal adoptante = cuenta(11, "Ana", UsuarioLocal.ADOPTANTE);
        UsuarioLocal publicador = cuenta(22, "Refugio", UsuarioLocal.PUBLICADOR);
        String solicitud = solicitar(repository, "m1", adoptante);
        repository.resolverSolicitud(solicitud, EstadoSolicitud.RECHAZADA, publicador);
        for (UsuarioLocal usuario : new UsuarioLocal[]{adoptante, publicador}) {
            assertTrue(repository.obtenerHistorialPerfil(usuario).isEmpty());
            assertNull(repository.obtenerDetalleHistorial("m1", solicitud, usuario));
        }
    }

    @Test public void detalleDeBajaConservaLaMascotaSinInventarAdoptanteNiChat() {
        PawMatchRepository repository = new PawMatchRepository();
        UsuarioLocal adoptante = cuenta(11, "Ana", UsuarioLocal.ADOPTANTE);
        UsuarioLocal publicador = cuenta(22, "Refugio", UsuarioLocal.PUBLICADOR);
        repository.registrarBajaEnHistorial("m2", repository.obtenerRefugioDemo().getId());

        HistorialMascota detalle = repository.obtenerDetalleHistorial("m2", null, publicador);
        assertNotNull(detalle);
        assertEquals("Milo", detalle.getMascota().getNombre());
        assertEquals(HistorialMascota.Estado.PUBLICACION_DADA_DE_BAJA, detalle.getEstado());
        assertNull(detalle.getAdoptante());
        assertNull(detalle.getSolicitudId());
        assertNull(repository.obtenerChat(detalle.getSolicitudId(), publicador));
        assertNull(repository.obtenerDetalleHistorial("m2", null, adoptante));
        assertTrue(repository.obtenerHistorialPerfil(adoptante).isEmpty());
    }

    @Test public void unDetalleAnteriorDejaDeEstarDisponibleAlReiniciarLosDatos() {
        PawMatchRepository repository = new PawMatchRepository();
        UsuarioLocal adoptante = cuenta(11, "Ana", UsuarioLocal.ADOPTANTE);
        UsuarioLocal publicador = cuenta(22, "Refugio", UsuarioLocal.PUBLICADOR);
        String solicitud = solicitar(repository, "m1", adoptante);
        repository.resolverSolicitud(solicitud, EstadoSolicitud.APROBADA, publicador);
        HistorialMascota antes = repository.obtenerDetalleHistorial("m1", solicitud, publicador);
        repository.reiniciarDemo();

        assertNotNull(antes);
        assertEquals("Luna", antes.getMascota().getNombre());
        assertNull(repository.obtenerDetalleHistorial("m1", solicitud, publicador));
        assertTrue(repository.obtenerHistorialPerfil(publicador).isEmpty());
        assertNull(repository.obtenerChat(antes.getSolicitudId(), publicador));
    }
}
