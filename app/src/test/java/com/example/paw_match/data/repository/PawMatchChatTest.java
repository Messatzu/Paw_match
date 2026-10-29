package com.example.paw_match.data.repository;

import com.example.paw_match.data.model.ConversacionChat;
import com.example.paw_match.data.model.EstadoSolicitud;
import com.example.paw_match.data.model.MensajeChat;
import com.example.paw_match.data.model.UsuarioLocal;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class PawMatchChatTest {
    private final PawMatchRepository repository = new PawMatchRepository();
    private final UsuarioLocal adoptante = cuenta(11, "Ana", UsuarioLocal.ADOPTANTE);
    private final UsuarioLocal publicador = cuenta(22, "Refugio de prueba", UsuarioLocal.PUBLICADOR);

    private static UsuarioLocal cuenta(long id, String nombre, String rol) {
        return new UsuarioLocal(id, nombre, id + "@ejemplo.com", "Santa Fe", rol);
    }
    private String solicitar(String mascota) {
        assertEquals(PawMatchRepository.Resultado.OK,
                repository.crearSolicitud(mascota, repository.obtenerAdoptanteDemo().getId(), adoptante));
        List<com.example.paw_match.data.model.SolicitudAdopcion> lista = repository.obtenerSolicitudes();
        return lista.get(lista.size() - 1).getId();
    }
    private void aprobar(String id) {
        assertEquals(PawMatchRepository.Resultado.OK,
                repository.resolverSolicitud(id, EstadoSolicitud.APROBADA, publicador));
    }

    @Test public void solicitudPendienteNoHabilitaChatParaNinguno() {
        String id = solicitar("m1");
        for (UsuarioLocal usuario : new UsuarioLocal[]{adoptante, publicador}) {
            assertTrue(repository.obtenerChats(usuario).isEmpty());
            assertNull(repository.obtenerChat(id, usuario));
            assertTrue(repository.obtenerMensajesChat(id, usuario).isEmpty());
            assertEquals(PawMatchRepository.Resultado.NO_AUTORIZADO, repository.enviarMensajeChat(id, usuario, "Hola"));
        }
    }

    @Test public void solicitudRechazadaNoHabilitaChat() {
        String id = solicitar("m1");
        repository.resolverSolicitud(id, EstadoSolicitud.RECHAZADA, publicador);
        assertNull(repository.obtenerChat(id, adoptante));
        assertNull(repository.obtenerChat(id, publicador));
        assertEquals(PawMatchRepository.Resultado.NO_AUTORIZADO, repository.enviarMensajeChat(id, adoptante, "Hola"));
        assertEquals(PawMatchRepository.Resultado.NO_AUTORIZADO, repository.enviarMensajeChat(id, publicador, "Hola"));
    }

    @Test public void aprobacionHabilitaLaMismaConversacionSoloParaLasDosCuentas() {
        String id = solicitar("m1");
        aprobar(id);
        ConversacionChat chat = repository.obtenerChat(id, adoptante);
        assertNotNull(chat);
        assertSame(chat, repository.obtenerChat(id, publicador));
        assertEquals("m1", chat.getMascota().getId());
        assertEquals("Refugio de prueba", chat.obtenerInterlocutor(adoptante).getNombre());
        assertEquals("Ana", chat.obtenerInterlocutor(publicador).getNombre());
        assertEquals(1, repository.obtenerChats(adoptante).size());
        assertEquals(1, repository.obtenerChats(publicador).size());
        assertEquals(PawMatchRepository.Resultado.YA_RESUELTA,
                repository.resolverSolicitud(id, EstadoSolicitud.APROBADA, publicador));
        assertEquals(1, repository.obtenerChats(adoptante).size());
    }

    @Test public void cuentasAjenasSesionesNulasYRolesFalsosNoPuedenLeerNiEnviar() {
        String id = solicitar("m1");
        aprobar(id);
        repository.enviarMensajeChat(id, adoptante, "Mensaje privado");
        for (UsuarioLocal usuario : new UsuarioLocal[]{null,
                cuenta(33, "Otro adoptante", UsuarioLocal.ADOPTANTE),
                cuenta(44, "Otro publicador", UsuarioLocal.PUBLICADOR),
                cuenta(11, "Mismo id con otro rol", UsuarioLocal.PUBLICADOR),
                cuenta(22, "Mismo id con otro rol", UsuarioLocal.ADOPTANTE)}) {
            assertTrue(repository.obtenerChats(usuario).isEmpty());
            assertNull(repository.obtenerChat(id, usuario));
            assertTrue(repository.obtenerMensajesChat(id, usuario).isEmpty());
            assertEquals(PawMatchRepository.Resultado.NO_AUTORIZADO,
                    repository.enviarMensajeChat(id, usuario, "No autorizado"));
        }
        assertEquals(1, repository.obtenerMensajesChat(id, adoptante).size());
        assertEquals(PawMatchRepository.Resultado.NO_AUTORIZADO,
                repository.enviarMensajeChat("inexistente", adoptante, "Hola"));
    }

    @Test public void soloUnPublicadorPuedeAceptarYNoSeInventanParticipantesDeLaDemo() {
        String id = solicitar("m1");
        assertEquals(PawMatchRepository.Resultado.NO_AUTORIZADO,
                repository.resolverSolicitud(id, EstadoSolicitud.APROBADA, adoptante));
        assertEquals(PawMatchRepository.Resultado.NO_AUTORIZADO,
                repository.resolverSolicitud(id, EstadoSolicitud.APROBADA, null));
        assertNull(repository.obtenerChat(id, adoptante));
        // Una solicitud antigua sin cuenta local no debe crear un chat con identidades ficticias.
        repository.crearSolicitud("m2", repository.obtenerAdoptanteDemo().getId());
        String antiguo = repository.obtenerSolicitudes().get(1).getId();
        repository.resolverSolicitud(antiguo, EstadoSolicitud.APROBADA, publicador);
        assertNull(repository.obtenerChat(antiguo, publicador));
        assertEquals(PawMatchRepository.Resultado.NO_AUTORIZADO,
                repository.crearSolicitud("m3", repository.obtenerAdoptanteDemo().getId(), publicador));
    }

    @Test public void ambosParticipantesIntercambianMensajesYLasCopiasNoCambian() {
        String id = solicitar("m1");
        aprobar(id);
        List<MensajeChat> vacia = repository.obtenerMensajesChat(id, adoptante);
        assertEquals(PawMatchRepository.Resultado.OK, repository.enviarMensajeChat(id, adoptante, "  Hola\nLuna  "));
        List<MensajeChat> primera = repository.obtenerMensajesChat(id, publicador);
        assertEquals(PawMatchRepository.Resultado.OK, repository.enviarMensajeChat(id, publicador, "Hola, Ana"));
        List<MensajeChat> mensajes = repository.obtenerMensajesChat(id, adoptante);
        assertTrue(vacia.isEmpty());
        assertEquals(1, primera.size());
        assertEquals(2, mensajes.size());
        assertEquals("Hola\nLuna", mensajes.get(0).getTexto());
        assertEquals(adoptante.getId(), mensajes.get(0).getEmisorId());
        assertEquals(publicador.getId(), mensajes.get(1).getEmisorId());
        assertEquals(id, mensajes.get(1).getSolicitudId());
        assertNotEquals(mensajes.get(0).getId(), mensajes.get(1).getId());
        assertTrue(mensajes.get(0).getFecha() > 0);
        try { mensajes.clear(); fail("La copia debe ser de solo lectura"); }
        catch (UnsupportedOperationException esperado) { }
    }

    @Test public void mensajesVaciosNoSeGuardanNiNotifican() {
        String id = solicitar("m1");
        aprobar(id);
        int[] cambios = {0};
        repository.addListener(() -> cambios[0]++);
        for (String texto : new String[]{null, "", " \n\t "})
            assertEquals(PawMatchRepository.Resultado.DATOS_INVALIDOS, repository.enviarMensajeChat(id, adoptante, texto));
        assertEquals(0, cambios[0]);
        assertTrue(repository.obtenerMensajesChat(id, publicador).isEmpty());
        assertNull(repository.obtenerChat(id, publicador).getUltimoMensaje());
        repository.enviarMensajeChat(id, adoptante, "Hola");
        assertEquals(1, cambios[0]);
    }

    @Test public void mensajesSeSeparanPorSolicitudYOrdenanLaListaPorActividad() {
        String uno = solicitar("m1");
        String dos = solicitar("m2");
        aprobar(uno);
        aprobar(dos);
        List<ConversacionChat> antes = repository.obtenerChats(publicador);
        assertEquals(dos, antes.get(0).getSolicitudId());
        repository.enviarMensajeChat(uno, adoptante, "Sobre Luna");
        assertEquals(uno, repository.obtenerChats(publicador).get(0).getSolicitudId());
        assertTrue(repository.obtenerMensajesChat(dos, publicador).isEmpty());
        assertNull(antes.get(1).getUltimoMensaje());
        assertEquals("Sobre Luna", repository.obtenerChat(uno, publicador).getUltimoMensaje().getTexto());
    }

    @Test public void reinicioRevocaAccesoYBorraMensajesDeLaDemo() {
        String id = solicitar("m1");
        aprobar(id);
        repository.enviarMensajeChat(id, adoptante, "Hola");
        repository.reiniciarDemo();
        assertTrue(repository.obtenerChats(adoptante).isEmpty());
        assertNull(repository.obtenerChat(id, publicador));
        assertTrue(repository.obtenerMensajesChat(id, adoptante).isEmpty());
        assertEquals(PawMatchRepository.Resultado.NO_AUTORIZADO, repository.enviarMensajeChat(id, adoptante, "Hola"));
    }
}
