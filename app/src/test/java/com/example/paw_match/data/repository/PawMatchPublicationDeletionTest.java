package com.example.paw_match.data.repository;

import com.example.paw_match.data.model.ConversacionChat;
import com.example.paw_match.data.model.DetalleMascota;
import com.example.paw_match.data.model.EstadoSolicitud;
import com.example.paw_match.data.model.HistorialMascota;
import com.example.paw_match.data.model.Mascota;
import com.example.paw_match.data.model.SolicitudAdopcion;
import com.example.paw_match.data.model.UsuarioLocal;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class PawMatchPublicationDeletionTest {
    private final PawMatchRepository repository = new PawMatchRepository();
    private final UsuarioLocal publicador = cuenta(22, UsuarioLocal.PUBLICADOR);
    private final UsuarioLocal otroPublicador = cuenta(23, UsuarioLocal.PUBLICADOR);
    private final UsuarioLocal adoptante = cuenta(11, UsuarioLocal.ADOPTANTE);
    private final UsuarioLocal otroAdoptante = cuenta(12, UsuarioLocal.ADOPTANTE);

    private static UsuarioLocal cuenta(long id, String rol) {
        return new UsuarioLocal(id, "Usuario " + id, id + "@ejemplo.com", "Santa Fe", rol);
    }

    private Mascota publicar() {
        assertEquals(PawMatchRepository.Resultado.OK, repository.publicarMascota(
                "Roco", "8 meses", "Rosario", "Cariñoso", "content://fotos/roco", publicador));
        return repository.obtenerMascotasPublicadas(publicador).get(0);
    }

    private String solicitar(String mascotaId, UsuarioLocal usuario) {
        assertEquals(PawMatchRepository.Resultado.OK,
                repository.crearSolicitud(mascotaId, repository.obtenerAdoptanteDemo().getId(), usuario));
        List<SolicitudAdopcion> solicitudes = repository.obtenerSolicitudes();
        return solicitudes.get(solicitudes.size() - 1).getId();
    }

    @Test public void eliminarRetiraLaPublicacionYLaConservaEnElHistorialDelPublicador() {
        Mascota mascota = publicar();
        int[] cambios = {0};
        repository.addListener(() -> cambios[0]++);

        assertEquals(PawMatchRepository.Resultado.OK, repository.eliminarPublicacion(mascota.getId(), publicador));

        assertEquals(6, repository.obtenerMascotasPublicadas(publicador).size());
        assertEquals(6, repository.obtenerMascotasParaExplorar(adoptante).size());
        HistorialMascota historial = repository.obtenerDetalleHistorial(mascota.getId(), null, publicador);
        assertNotNull(historial);
        assertSame(mascota, historial.getMascota());
        assertEquals(HistorialMascota.Estado.PUBLICACION_DADA_DE_BAJA, historial.getEstado());
        assertNull(historial.getAdoptante());
        assertTrue(repository.obtenerHistorialPerfil(otroPublicador).isEmpty());
        assertTrue(repository.obtenerHistorialPerfil(adoptante).isEmpty());
        DetalleMascota detalle = repository.obtenerDetalleMascota(mascota.getId(), null, adoptante);
        assertEquals(DetalleMascota.Estado.DADA_DE_BAJA, detalle.getEstado());
        assertFalse(detalle.puedeSolicitar());
        assertFalse(detalle.puedeResponder());
        assertNull(detalle.getChat());
        assertEquals(1, cambios[0]);
    }

    @Test public void eliminarCierraSoloLasSolicitudesPendientesDeEsaMascota() {
        Mascota mascota = publicar();
        Mascota otra = publicar();
        String primera = solicitar(mascota.getId(), adoptante);
        String segunda = solicitar(mascota.getId(), otroAdoptante);
        String ajena = solicitar(otra.getId(), adoptante);
        List<SolicitudAdopcion> antes = repository.obtenerSolicitudesVisibles(publicador);

        assertEquals(PawMatchRepository.Resultado.OK, repository.eliminarPublicacion(mascota.getId(), publicador));

        assertEquals(1, repository.obtenerSolicitudesVisibles(publicador).size());
        assertEquals(ajena, repository.obtenerSolicitudesVisibles(publicador).get(0).getId());
        assertEquals(EstadoSolicitud.PENDIENTE, repository.obtenerSolicitudesVisibles(publicador).get(0).getEstado());
        assertEquals(EstadoSolicitud.RECHAZADA, repository.obtenerSolicitudesVisibles(adoptante).get(0).getEstado());
        assertEquals(EstadoSolicitud.RECHAZADA, repository.obtenerSolicitudesVisibles(otroAdoptante).get(0).getEstado());
        assertEquals(DetalleMascota.Estado.DADA_DE_BAJA,
                repository.obtenerDetalleMascota(mascota.getId(), primera, adoptante).getEstado());
        assertEquals(DetalleMascota.Estado.DADA_DE_BAJA,
                repository.obtenerDetalleMascota(mascota.getId(), segunda, otroAdoptante).getEstado());
        assertEquals(PawMatchRepository.Resultado.YA_RESUELTA,
                repository.resolverSolicitud(primera, EstadoSolicitud.APROBADA, publicador));
        assertEquals(PawMatchRepository.Resultado.YA_RESUELTA, repository.crearSolicitud(
                mascota.getId(), repository.obtenerAdoptanteDemo().getId(), cuenta(13, UsuarioLocal.ADOPTANTE)));
        assertTrue(repository.obtenerChats(publicador).isEmpty());
        assertEquals(EstadoSolicitud.PENDIENTE, antes.get(0).getEstado());
    }

    @Test public void soloElPublicadorPropietarioPuedeEliminarLaPublicacion() {
        Mascota mascota = publicar();
        int[] cambios = {0};
        repository.addListener(() -> cambios[0]++);

        for (UsuarioLocal usuario : new UsuarioLocal[]{null, adoptante, otroPublicador,
                cuenta(publicador.getId(), UsuarioLocal.ADOPTANTE), cuenta(0, UsuarioLocal.PUBLICADOR),
                cuenta(publicador.getId(), "otro")}) {
            assertEquals(PawMatchRepository.Resultado.NO_AUTORIZADO,
                    repository.eliminarPublicacion(mascota.getId(), usuario));
        }
        assertEquals(PawMatchRepository.Resultado.NO_ENCONTRADA,
                repository.eliminarPublicacion("inexistente", publicador));

        assertSame(mascota, repository.obtenerMascotasPublicadas(publicador).get(0));
        assertSame(mascota, repository.obtenerMascotasParaExplorar(adoptante).get(0));
        assertTrue(repository.obtenerHistorialPerfil(publicador).isEmpty());
        assertEquals(0, cambios[0]);
    }

    @Test public void unaPublicacionAdoptadaConservaSuHistorialYSuChat() {
        Mascota mascota = publicar();
        String solicitud = solicitar(mascota.getId(), adoptante);
        repository.resolverSolicitud(solicitud, EstadoSolicitud.APROBADA, publicador);
        repository.enviarMensajeChat(solicitud, publicador, "Hola");
        ConversacionChat chat = repository.obtenerChat(solicitud, publicador);
        int[] cambios = {0};
        repository.addListener(() -> cambios[0]++);

        assertEquals(PawMatchRepository.Resultado.YA_RESUELTA,
                repository.eliminarPublicacion(mascota.getId(), publicador));

        assertEquals(1, repository.obtenerHistorialPerfil(publicador).size());
        assertEquals(HistorialMascota.Estado.ADOPTADA, repository.obtenerHistorialPerfil(publicador).get(0).getEstado());
        assertEquals(1, repository.obtenerHistorialPerfil(adoptante).size());
        assertSame(chat, repository.obtenerChat(solicitud, publicador));
        assertSame(chat, repository.obtenerChat(solicitud, adoptante));
        assertEquals("Hola", repository.obtenerMensajesChat(solicitud, adoptante).get(0).getTexto());
        assertEquals(DetalleMascota.Estado.APROBADA,
                repository.obtenerDetalleMascota(mascota.getId(), solicitud, adoptante).getEstado());
        assertEquals(0, cambios[0]);
    }

    @Test public void eliminarDosVecesNoDuplicaElHistorialNiNotificaDeNuevo() {
        Mascota mascota = publicar();
        int[] cambios = {0};
        repository.addListener(() -> cambios[0]++);

        assertEquals(PawMatchRepository.Resultado.OK, repository.eliminarPublicacion(mascota.getId(), publicador));
        assertEquals(PawMatchRepository.Resultado.DUPLICADA,
                repository.eliminarPublicacion(mascota.getId(), cuenta(publicador.getId(), UsuarioLocal.PUBLICADOR)));

        assertEquals(1, repository.obtenerHistorialPerfil(publicador).size());
        assertEquals(1, cambios[0]);
    }

    @Test public void eliminarUnaMascotaDemoRegistraQuienLaDioDeBaja() {
        solicitar("m1", adoptante);

        assertEquals(PawMatchRepository.Resultado.OK, repository.eliminarPublicacion("m1", publicador));

        assertEquals(5, repository.obtenerMascotasPublicadas(publicador).size());
        assertEquals(5, repository.obtenerMascotasPublicadas(otroPublicador).size());
        assertTrue(repository.obtenerSolicitudesVisibles(publicador).isEmpty());
        assertEquals(1, repository.obtenerHistorialPerfil(publicador).size());
        assertEquals("m1", repository.obtenerHistorialPerfil(publicador).get(0).getMascota().getId());
        assertTrue(repository.obtenerHistorialPerfil(otroPublicador).isEmpty());
        assertNull(repository.obtenerDetalleHistorial("m1", null, otroPublicador));
    }
}
