package com.example.paw_match.data.repository;

import com.example.paw_match.data.model.EstadoSolicitud;
import com.example.paw_match.data.model.Mascota;
import com.example.paw_match.data.model.SolicitudAdopcion;
import com.example.paw_match.data.model.UsuarioLocal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;

public class PawMatchExploreTest {
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

    private List<String> mascotasParaExplorar(UsuarioLocal usuario) {
        return repository.obtenerMascotasParaExplorar(usuario).stream()
                .map(Mascota::getId).collect(Collectors.toList());
    }

    @Test public void enviarSolicitudQuitaLaMascotaDeExplorarYLaConservaEnSolicitudes() {
        List<Mascota> antes = repository.obtenerMascotasParaExplorar(ana);
        assertEquals(repository.obtenerMascotas(), antes);

        String solicitudId = solicitar("m1", ana);

        assertEquals(Arrays.asList("m2", "m3", "m4", "m5", "m6"), mascotasParaExplorar(ana));
        List<SolicitudAdopcion> solicitudes = repository.obtenerSolicitudesVisibles(ana);
        assertEquals(1, solicitudes.size());
        assertEquals(solicitudId, solicitudes.get(0).getId());
        assertEquals("m1", solicitudes.get(0).getMascotaId());
        assertEquals(EstadoSolicitud.PENDIENTE, solicitudes.get(0).getEstado());
        assertNotNull(repository.obtenerDetalleMascota("m1", solicitudId, ana));
        assertEquals(solicitudId, repository.obtenerSolicitudesVisibles(publicador).get(0).getId());
        assertEquals(6, repository.obtenerMascotas().size());
        assertEquals(6, antes.size());
    }

    @Test public void cadaAdoptanteSoloDejaDeVerLasMascotasQueYaSolicito() {
        solicitar("m1", ana);
        solicitar("m2", juan);

        assertEquals(Arrays.asList("m2", "m3", "m4", "m5", "m6"), mascotasParaExplorar(ana));
        assertEquals(Arrays.asList("m1", "m3", "m4", "m5", "m6"), mascotasParaExplorar(juan));
        assertEquals(mascotasParaExplorar(ana),
                mascotasParaExplorar(cuenta(ana.getId(), UsuarioLocal.ADOPTANTE)));
    }

    @Test public void solicitudesResueltasNoVuelvenAOfrecerseAlMismoAdoptante() {
        String aprobada = solicitar("m1", ana);
        String rechazada = solicitar("m2", ana);
        assertEquals(PawMatchRepository.Resultado.OK,
                repository.resolverSolicitud(aprobada, EstadoSolicitud.APROBADA, publicador));
        assertEquals(PawMatchRepository.Resultado.OK,
                repository.resolverSolicitud(rechazada, EstadoSolicitud.RECHAZADA, publicador));

        assertEquals(Arrays.asList("m3", "m4", "m5", "m6"), mascotasParaExplorar(ana));
        assertEquals(Arrays.asList("m2", "m3", "m4", "m5", "m6"), mascotasParaExplorar(juan));
        assertEquals(2, repository.obtenerSolicitudesVisibles(ana).size());
    }

    @Test public void explorarExcluyeMascotasAdoptadasYDadasDeBaja() {
        String solicitudId = solicitar("m1", juan);
        assertEquals(PawMatchRepository.Resultado.OK,
                repository.resolverSolicitud(solicitudId, EstadoSolicitud.APROBADA, publicador));
        assertEquals(PawMatchRepository.Resultado.OK, repository.registrarBajaEnHistorial(
                "m2", repository.obtenerRefugioDemo().getId()));

        assertEquals(Arrays.asList("m3", "m4", "m5", "m6"), mascotasParaExplorar(ana));
        assertTrue(repository.obtenerSolicitudesVisibles(ana).isEmpty());
        assertEquals(6, repository.obtenerMascotas().size());
    }

    @Test public void solicitarTodasLasMascotasDejaExplorarVacioSinPerderSolicitudes() {
        for (Mascota mascota : repository.obtenerMascotas()) solicitar(mascota.getId(), ana);

        assertTrue(repository.obtenerMascotasParaExplorar(ana).isEmpty());
        assertEquals(6, repository.obtenerSolicitudesVisibles(ana).size());
        assertEquals(6, repository.obtenerSolicitudesVisibles(publicador).size());
        assertEquals(6, repository.obtenerMascotasParaExplorar(juan).size());

        repository.reiniciarDemo();

        assertEquals(6, repository.obtenerMascotasParaExplorar(ana).size());
        assertTrue(repository.obtenerSolicitudesVisibles(ana).isEmpty());
    }

    @Test public void explorarRequiereUnaCuentaAdoptanteValida() {
        assertTrue(repository.obtenerMascotasParaExplorar(null).isEmpty());
        assertTrue(repository.obtenerMascotasParaExplorar(publicador).isEmpty());
        assertTrue(repository.obtenerMascotasParaExplorar(cuenta(0, UsuarioLocal.ADOPTANTE)).isEmpty());
        assertTrue(repository.obtenerMascotasParaExplorar(cuenta(11, "otro")).isEmpty());
    }
}
