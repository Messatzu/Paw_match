package com.example.paw_match.data.repository;
import com.example.paw_match.data.model.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class PawMatchRepositoryTest {
    @Test public void evitaDuplicadosYValidaReferencias() {
        PawMatchRepository r = new PawMatchRepository();
        String a = r.obtenerAdoptanteDemo().getId();
        assertEquals(PawMatchRepository.Resultado.DATOS_INVALIDOS, r.crearSolicitud("inexistente", a));
        assertEquals(PawMatchRepository.Resultado.DATOS_INVALIDOS, r.crearSolicitud("m1", "otro"));
        assertEquals(PawMatchRepository.Resultado.OK, r.crearSolicitud("m1", a));
        assertEquals(PawMatchRepository.Resultado.DUPLICADA, r.crearSolicitud("m1", a));
        assertEquals(1, r.obtenerSolicitudes().size());
    }
    @Test public void contactoSoloTrasAprobarYParaElSolicitante() {
        PawMatchRepository r = new PawMatchRepository();
        String a = r.obtenerAdoptanteDemo().getId();
        r.crearSolicitud("m1", a);
        String id = r.obtenerSolicitudes().get(0).getId();
        assertNull(r.obtenerContactoHabilitado(id, a));
        assertEquals(PawMatchRepository.Resultado.OK, r.resolverSolicitud(id, EstadoSolicitud.APROBADA));
        assertNotNull(r.obtenerContactoHabilitado(id, a));
        assertNull(r.obtenerContactoHabilitado(id, "otro"));
        assertEquals(PawMatchRepository.Resultado.YA_RESUELTA, r.resolverSolicitud(id, EstadoSolicitud.RECHAZADA));
    }
    @Test public void rechazoNoHabilitaContactoYReinicioNotifica() {
        PawMatchRepository r = new PawMatchRepository();
        int[] cambios = {0};
        r.addListener(() -> cambios[0]++);
        String a = r.obtenerAdoptanteDemo().getId();
        r.crearSolicitud("m1", a);
        String id = r.obtenerSolicitudes().get(0).getId();
        assertEquals(PawMatchRepository.Resultado.DATOS_INVALIDOS, r.resolverSolicitud(id, null));
        assertEquals(PawMatchRepository.Resultado.DATOS_INVALIDOS, r.resolverSolicitud(id, EstadoSolicitud.PENDIENTE));
        assertEquals(PawMatchRepository.Resultado.NO_ENCONTRADA, r.resolverSolicitud("otro", EstadoSolicitud.APROBADA));
        r.resolverSolicitud(id, EstadoSolicitud.RECHAZADA);
        assertNull(r.obtenerContactoHabilitado(id, a));
        r.reiniciarDemo();
        assertTrue(r.obtenerSolicitudes().isEmpty());
        assertEquals(3, cambios[0]);
        assertEquals(6, r.obtenerMascotas().size());
    }
    @Test public void snapshotsNoCambianYFiltrosRespetanIds() {
        PawMatchRepository r = new PawMatchRepository();
        java.util.List<SolicitudAdopcion> antes = r.obtenerSolicitudes();
        r.crearSolicitud("m1", r.obtenerAdoptanteDemo().getId());
        assertTrue(antes.isEmpty());
        assertTrue(r.obtenerSolicitudesAdoptante("otro").isEmpty());
        assertTrue(r.obtenerSolicitudesRefugio("otro").isEmpty());
        assertEquals(1, r.obtenerSolicitudesRefugio(r.obtenerRefugioDemo().getId()).size());
    }
}
