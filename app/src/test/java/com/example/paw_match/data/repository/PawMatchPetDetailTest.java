package com.example.paw_match.data.repository;

import com.example.paw_match.data.model.DetalleMascota;
import com.example.paw_match.data.model.EstadoSolicitud;
import com.example.paw_match.data.model.UsuarioLocal;
import org.junit.Test;
import static org.junit.Assert.*;

public class PawMatchPetDetailTest {
    private final PawMatchRepository repository = new PawMatchRepository();
    private final UsuarioLocal ana = cuenta(11, "Ana", UsuarioLocal.ADOPTANTE);
    private final UsuarioLocal juan = cuenta(12, "Juan", UsuarioLocal.ADOPTANTE);
    private final UsuarioLocal publicador = cuenta(22, "Refugio", UsuarioLocal.PUBLICADOR);

    private static UsuarioLocal cuenta(long id, String nombre, String rol) {
        return new UsuarioLocal(id, nombre, id + "@ejemplo.com", "Santa Fe", rol);
    }
    private String solicitar(String mascota, UsuarioLocal usuario) {
        assertEquals(PawMatchRepository.Resultado.OK,
                repository.crearSolicitud(mascota, repository.obtenerAdoptanteDemo().getId(), usuario));
        return repository.obtenerSolicitudesVisibles(usuario).get(0).getId();
    }
    private DetalleMascota detalle(String mascota, String solicitud, UsuarioLocal usuario) {
        DetalleMascota detalle = repository.obtenerDetalleMascota(mascota, solicitud, usuario);
        assertNotNull(detalle);
        return detalle;
    }

    @Test public void explorarMuestraLaMascotaYQuienLaDaEnAdopcionSinInventarUnaSolicitud() {
        DetalleMascota inicial = detalle("m1", null, ana);
        assertEquals("Luna", inicial.getMascota().getNombre());
        assertEquals("14 kg", inicial.getMascota().getPeso());
        assertEquals(repository.obtenerRefugioDemo().getId(), inicial.getRefugio().getId());
        assertEquals(repository.obtenerRefugioDemo().getNombre(), inicial.getRefugio().getNombre());
        assertEquals(DetalleMascota.Estado.DISPONIBLE, inicial.getEstado());
        assertTrue(inicial.puedeSolicitar());
        assertFalse(inicial.puedeResponder());
        assertNull(inicial.getSolicitud());
        assertNull(inicial.getSolicitante());
        assertNull(inicial.getChat());
        assertFalse(detalle("m1", null, publicador).puedeSolicitar());
    }

    @Test public void laMismaFichaPasaDeDisponibleAPendienteYAprobadaConSuChatExacto() {
        DetalleMascota antes = detalle("m1", null, ana);
        int[] cambios = {0};
        repository.addListener(() -> cambios[0]++);
        String id = solicitar("m1", ana);
        DetalleMascota enviada = detalle("m1", null, ana);
        assertEquals(DetalleMascota.Estado.PENDIENTE, enviada.getEstado());
        assertEquals(id, enviada.getSolicitud().getId());
        assertSame(ana, enviada.getSolicitante());
        assertFalse(enviada.puedeSolicitar());
        assertFalse(enviada.puedeResponder());
        assertNull(enviada.getChat());
        assertTrue(detalle("m1", id, publicador).puedeResponder());
        assertEquals(PawMatchRepository.Resultado.DUPLICADA,
                repository.crearSolicitud("m1", repository.obtenerAdoptanteDemo().getId(), ana));

        repository.resolverSolicitud(id, EstadoSolicitud.APROBADA, publicador);
        repository.enviarMensajeChat(id, publicador, "Hola Ana");
        for (UsuarioLocal usuario : new UsuarioLocal[]{ana, publicador}) {
            DetalleMascota aceptada = detalle("m1", id, usuario);
            assertEquals(DetalleMascota.Estado.APROBADA, aceptada.getEstado());
            assertSame(publicador, aceptada.getPublicador());
            assertEquals(id, aceptada.getChat().getSolicitudId());
            assertEquals("Hola Ana", aceptada.getChat().getUltimoMensaje().getTexto());
            assertFalse(aceptada.puedeSolicitar());
            assertFalse(aceptada.puedeResponder());
            assertEquals(id, repository.obtenerHistorialPerfil(usuario).get(0).getSolicitudId());
        }
        assertEquals(DetalleMascota.Estado.APROBADA, detalle("m1", null, ana).getEstado());
        assertEquals(DetalleMascota.Estado.DISPONIBLE, antes.getEstado());
        assertEquals(DetalleMascota.Estado.PENDIENTE, enviada.getEstado());
        assertEquals(3, cambios[0]);
    }

    @Test public void rechazoActualizaLaFichaSinHabilitarChatNiDuplicarLaSolicitud() {
        String id = solicitar("m2", ana);
        repository.resolverSolicitud(id, EstadoSolicitud.RECHAZADA, publicador);
        for (UsuarioLocal usuario : new UsuarioLocal[]{ana, publicador}) {
            DetalleMascota rechazada = detalle("m2", id, usuario);
            assertEquals(DetalleMascota.Estado.RECHAZADA, rechazada.getEstado());
            assertFalse(rechazada.puedeSolicitar());
            assertFalse(rechazada.puedeResponder());
            assertNull(rechazada.getChat());
        }
        assertTrue(repository.obtenerSolicitudesVisibles(publicador).isEmpty());
        assertEquals(1, repository.obtenerSolicitudesVisibles(ana).size());
    }

    @Test public void solicitudesYDatosPersonalesSeFiltranPorLaCuentaActual() {
        String id = solicitar("m1", ana);
        DetalleMascota ajena = detalle("m1", null, juan);
        assertEquals(DetalleMascota.Estado.DISPONIBLE, ajena.getEstado());
        assertNull(ajena.getSolicitud());
        assertNull(ajena.getSolicitante());
        assertNull(ajena.getChat());
        assertTrue(repository.obtenerSolicitudesVisibles(juan).isEmpty());
        assertNull(repository.obtenerDetalleMascota("m1", id, juan));
        assertNull(repository.obtenerSolicitante(id, juan));
        assertSame(ana, repository.obtenerSolicitante(id, publicador));
        assertSame(ana, repository.obtenerSolicitante(id, ana));
        repository.resolverSolicitud(id, EstadoSolicitud.APROBADA, publicador);
        DetalleMascota adoptada = detalle("m1", null, juan);
        assertEquals(DetalleMascota.Estado.ADOPTADA, adoptada.getEstado());
        assertFalse(adoptada.puedeSolicitar());
        assertNull(adoptada.getSolicitante());
        assertNull(adoptada.getPublicador());
        assertNull(adoptada.getChat());
        assertNull(repository.obtenerDetalleMascota("m1", id,
                cuenta(33, "Otro refugio", UsuarioLocal.PUBLICADOR)));
    }

    @Test public void validarLosIdentificadoresNoAbreOtraMascotaNiUnaSolicitudAjena() {
        String id = solicitar("m1", ana);
        assertNull(repository.obtenerDetalleMascota("inexistente", null, ana));
        assertNull(repository.obtenerDetalleMascota("m2", id, ana));
        assertNull(repository.obtenerDetalleMascota("m1", "inexistente", ana));
        assertNull(repository.obtenerDetalleMascota("m1", id, null));
        assertNull(repository.obtenerDetalleMascota("m1", null, cuenta(0, "Sin cuenta", UsuarioLocal.ADOPTANTE)));
        assertNull(repository.obtenerDetalleMascota("m1", id, cuenta(22, "Rol cambiado", UsuarioLocal.ADOPTANTE)));
        repository.resolverSolicitud(id, EstadoSolicitud.APROBADA, publicador);
        assertNull(repository.obtenerDetalleMascota("m1", id, cuenta(11, "Rol cambiado", UsuarioLocal.PUBLICADOR)));
    }

    @Test public void dosPersonasPuedenSolicitarPeroSoloLaAceptadaObtieneAdopcionYChat() {
        String idAna = solicitar("m1", ana);
        String idJuan = solicitar("m1", juan);
        assertEquals(2, repository.obtenerSolicitudesVisibles(publicador).size());
        repository.resolverSolicitud(idAna, EstadoSolicitud.APROBADA, publicador);
        assertEquals(DetalleMascota.Estado.APROBADA, detalle("m1", idAna, ana).getEstado());
        assertEquals(DetalleMascota.Estado.RECHAZADA, detalle("m1", idJuan, juan).getEstado());
        assertNull(detalle("m1", idJuan, juan).getChat());
        assertTrue(repository.obtenerHistorialPerfil(juan).isEmpty());
        assertTrue(repository.obtenerSolicitudesVisibles(publicador).isEmpty());
        assertEquals(PawMatchRepository.Resultado.YA_RESUELTA,
                repository.resolverSolicitud(idJuan, EstadoSolicitud.APROBADA, publicador));
        assertEquals(PawMatchRepository.Resultado.YA_RESUELTA,
                repository.crearSolicitud("m1", repository.obtenerAdoptanteDemo().getId(),
                        cuenta(13, "Otra persona", UsuarioLocal.ADOPTANTE)));
    }

    @Test public void bajaMuestraSuEstadoYBloqueaNuevasSolicitudes() {
        repository.registrarBajaEnHistorial("m2", repository.obtenerRefugioDemo().getId());
        for (UsuarioLocal usuario : new UsuarioLocal[]{ana, publicador}) {
            DetalleMascota baja = detalle("m2", null, usuario);
            assertEquals(DetalleMascota.Estado.DADA_DE_BAJA, baja.getEstado());
            assertFalse(baja.puedeSolicitar());
            assertFalse(baja.puedeResponder());
            assertNull(baja.getChat());
        }
        assertEquals(PawMatchRepository.Resultado.YA_RESUELTA,
                repository.crearSolicitud("m2", repository.obtenerAdoptanteDemo().getId(), ana));
    }

    @Test public void reiniciarLimpiaEstadoPersonasYChatDelDetalle() {
        String id = solicitar("m1", ana);
        repository.resolverSolicitud(id, EstadoSolicitud.APROBADA, publicador);
        repository.reiniciarDemo();
        assertNull(repository.obtenerDetalleMascota("m1", id, ana));
        DetalleMascota disponible = detalle("m1", null, ana);
        assertEquals(DetalleMascota.Estado.DISPONIBLE, disponible.getEstado());
        assertNull(disponible.getPublicador());
        assertNull(disponible.getSolicitud());
        assertNull(disponible.getSolicitante());
        assertNull(disponible.getChat());
        assertTrue(disponible.puedeSolicitar());
    }
}
