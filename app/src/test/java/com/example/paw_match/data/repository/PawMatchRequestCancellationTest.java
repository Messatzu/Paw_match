package com.example.paw_match.data.repository;

import com.example.paw_match.data.model.ConversacionChat;
import com.example.paw_match.data.model.DetalleMascota;
import com.example.paw_match.data.model.EstadoSolicitud;
import com.example.paw_match.data.model.SolicitudAdopcion;
import com.example.paw_match.data.model.UsuarioLocal;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class PawMatchRequestCancellationTest {
    private final PawMatchRepository repository = new PawMatchRepository();
    private final UsuarioLocal ana = cuenta(11, UsuarioLocal.ADOPTANTE);
    private final UsuarioLocal juan = cuenta(12, UsuarioLocal.ADOPTANTE);
    private final UsuarioLocal publicador = cuenta(22, UsuarioLocal.PUBLICADOR);

    private static UsuarioLocal cuenta(long id, String rol) {
        return new UsuarioLocal(id, "Usuario " + id, id + "@ejemplo.com", "Santa Fe", rol);
    }

    private String solicitar(String mascotaId, UsuarioLocal usuario) {
        assertEquals(PawMatchRepository.Resultado.OK,
                repository.crearSolicitud(mascotaId, repository.obtenerAdoptanteDemo().getId(), usuario));
        List<SolicitudAdopcion> propias = repository.obtenerSolicitudesVisibles(usuario);
        return propias.get(propias.size() - 1).getId();
    }

    @Test public void cancelarQuitaLaSolicitudDeAmbasBandejasYDevuelveLaMascotaAExplorar() {
        String id = solicitar("m1", ana);
        int[] cambios = {0};
        repository.addListener(() -> cambios[0]++);

        assertEquals(PawMatchRepository.Resultado.OK, repository.cancelarSolicitud(id, ana));

        assertTrue(repository.obtenerSolicitudesVisibles(ana).isEmpty());
        assertTrue(repository.obtenerSolicitudesVisibles(publicador).isEmpty());
        assertTrue(repository.obtenerSolicitudesPendientesRefugio(repository.obtenerRefugioDemo().getId()).isEmpty());
        assertTrue(repository.obtenerSolicitudes().isEmpty());
        assertNull(repository.obtenerSolicitante(id, ana));
        assertNull(repository.obtenerDetalleMascota("m1", id, ana));
        assertTrue(repository.obtenerDetalleMascota("m1", null, ana).puedeSolicitar());
        assertEquals(6, repository.obtenerMascotasParaExplorar(ana).size());
        assertEquals("m1", repository.obtenerMascotasParaExplorar(ana).get(0).getId());
        assertEquals(1, cambios[0]);

        assertEquals(PawMatchRepository.Resultado.NO_ENCONTRADA, repository.cancelarSolicitud(id, ana));
        assertEquals(PawMatchRepository.Resultado.NO_ENCONTRADA, repository.cancelarSolicitud("inexistente", ana));
        assertEquals(PawMatchRepository.Resultado.NO_ENCONTRADA,
                repository.resolverSolicitud(id, EstadoSolicitud.APROBADA, publicador));
        assertEquals(1, cambios[0]);
    }

    @Test public void permiteVolverASolicitarDespuesDeCancelarConLaMismaCuenta() {
        String anterior = solicitar("m1", ana);
        assertEquals(PawMatchRepository.Resultado.OK,
                repository.cancelarSolicitud(anterior, cuenta(ana.getId(), UsuarioLocal.ADOPTANTE)));

        String nueva = solicitar("m1", ana);

        assertNotEquals(anterior, nueva);
        assertEquals(1, repository.obtenerSolicitudesVisibles(ana).size());
        assertEquals(nueva, repository.obtenerSolicitudesVisibles(publicador).get(0).getId());
        assertEquals(EstadoSolicitud.PENDIENTE, repository.obtenerSolicitudesVisibles(ana).get(0).getEstado());
        assertEquals(5, repository.obtenerMascotasParaExplorar(ana).size());
    }

    @Test public void soloElAdoptanteQueEnvioLaSolicitudPuedeCancelarla() {
        String id = solicitar("m1", ana);
        int[] cambios = {0};
        repository.addListener(() -> cambios[0]++);

        for (UsuarioLocal usuario : new UsuarioLocal[]{null, juan, publicador,
                cuenta(ana.getId(), UsuarioLocal.PUBLICADOR),
                cuenta(ana.getId(), "otro"), cuenta(0, UsuarioLocal.ADOPTANTE),
                cuenta(-1, UsuarioLocal.ADOPTANTE)}) {
            assertEquals(PawMatchRepository.Resultado.NO_AUTORIZADO, repository.cancelarSolicitud(id, usuario));
        }

        assertEquals(1, repository.obtenerSolicitudesVisibles(ana).size());
        assertEquals(1, repository.obtenerSolicitudesVisibles(publicador).size());
        assertEquals(EstadoSolicitud.PENDIENTE, repository.obtenerSolicitudesVisibles(ana).get(0).getEstado());
        assertEquals(0, cambios[0]);
    }

    @Test public void noCancelaSolicitudesAprobadasNiEliminaLaAdopcionOElChat() {
        String id = solicitar("m1", ana);
        assertEquals(PawMatchRepository.Resultado.OK,
                repository.resolverSolicitud(id, EstadoSolicitud.APROBADA, publicador));
        assertEquals(PawMatchRepository.Resultado.OK, repository.enviarMensajeChat(id, ana, "Hola"));
        ConversacionChat chat = repository.obtenerChat(id, ana);
        int[] cambios = {0};
        repository.addListener(() -> cambios[0]++);

        assertEquals(PawMatchRepository.Resultado.YA_RESUELTA, repository.cancelarSolicitud(id, ana));

        assertEquals(EstadoSolicitud.APROBADA, repository.obtenerSolicitudesVisibles(ana).get(0).getEstado());
        assertEquals(1, repository.obtenerHistorialPerfil(ana).size());
        assertEquals(1, repository.obtenerHistorialPerfil(publicador).size());
        assertSame(chat, repository.obtenerChat(id, ana));
        assertSame(chat, repository.obtenerChat(id, publicador));
        assertEquals("Hola", repository.obtenerMensajesChat(id, ana).get(0).getTexto());
        assertEquals(5, repository.obtenerMascotasParaExplorar(juan).size());
        assertEquals(0, cambios[0]);
    }

    @Test public void unaSolicitudRechazadaYaNoPuedeCancelarse() {
        String id = solicitar("m1", ana);
        assertEquals(PawMatchRepository.Resultado.OK,
                repository.resolverSolicitud(id, EstadoSolicitud.RECHAZADA, publicador));

        assertEquals(PawMatchRepository.Resultado.YA_RESUELTA, repository.cancelarSolicitud(id, ana));

        assertEquals(1, repository.obtenerSolicitudesVisibles(ana).size());
        assertEquals(EstadoSolicitud.RECHAZADA, repository.obtenerSolicitudesVisibles(ana).get(0).getEstado());
        assertFalse(repository.obtenerDetalleMascota("m1", id, ana).puedeSolicitar());
    }

    @Test public void cancelarSoloAfectaALaSolicitudElegida() {
        String cancelada = solicitar("m1", ana);
        String otraPropia = solicitar("m2", ana);
        String ajena = solicitar("m1", juan);

        assertEquals(PawMatchRepository.Resultado.OK, repository.cancelarSolicitud(cancelada, ana));

        assertEquals(1, repository.obtenerSolicitudesVisibles(ana).size());
        assertEquals(otraPropia, repository.obtenerSolicitudesVisibles(ana).get(0).getId());
        assertEquals(1, repository.obtenerSolicitudesVisibles(juan).size());
        assertEquals(ajena, repository.obtenerSolicitudesVisibles(juan).get(0).getId());
        assertEquals(2, repository.obtenerSolicitudesVisibles(publicador).size());
        assertEquals("m1", repository.obtenerMascotasParaExplorar(ana).get(0).getId());
        assertEquals(6, repository.obtenerMascotas().size());
        assertEquals(PawMatchRepository.Resultado.OK,
                repository.resolverSolicitud(ajena, EstadoSolicitud.APROBADA, publicador));
    }

    @Test public void cancelarNoVuelveAMostrarUnaPublicacionDadaDeBaja() {
        String id = solicitar("m1", ana);
        assertEquals(PawMatchRepository.Resultado.OK,
                repository.registrarBajaEnHistorial("m1", repository.obtenerRefugioDemo().getId()));

        assertEquals(PawMatchRepository.Resultado.OK, repository.cancelarSolicitud(id, ana));

        assertTrue(repository.obtenerSolicitudesVisibles(ana).isEmpty());
        assertEquals(5, repository.obtenerMascotasParaExplorar(ana).size());
        assertEquals(DetalleMascota.Estado.DADA_DE_BAJA,
                repository.obtenerDetalleMascota("m1", null, ana).getEstado());
    }
}
