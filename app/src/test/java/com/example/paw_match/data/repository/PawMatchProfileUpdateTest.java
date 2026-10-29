package com.example.paw_match.data.repository;

import com.example.paw_match.data.model.ConversacionChat;
import com.example.paw_match.data.model.DetalleMascota;
import com.example.paw_match.data.model.EstadoSolicitud;
import com.example.paw_match.data.model.Mascota;
import com.example.paw_match.data.model.UsuarioLocal;
import org.junit.Test;
import static org.junit.Assert.*;

public class PawMatchProfileUpdateTest {
    private final PawMatchRepository repository = new PawMatchRepository();
    private final UsuarioLocal adoptante = cuenta(11, "Ana", "Santa Fe", UsuarioLocal.ADOPTANTE);
    private final UsuarioLocal publicador = cuenta(22, "Refugio", "Santa Fe", UsuarioLocal.PUBLICADOR);

    private static UsuarioLocal cuenta(long id, String nombre, String ciudad, String rol) {
        return new UsuarioLocal(id, nombre, id + "@ejemplo.com", ciudad, rol);
    }

    private String solicitar(String mascotaId) {
        assertEquals(PawMatchRepository.Resultado.OK,
                repository.crearSolicitud(mascotaId, repository.obtenerAdoptanteDemo().getId(), adoptante));
        return repository.obtenerSolicitudesVisibles(adoptante).get(0).getId();
    }

    @Test public void actualizarAdoptanteReflejaSuPerfilEnSolicitudesHistorialYChat() {
        String solicitud = solicitar("m1");
        repository.resolverSolicitud(solicitud, EstadoSolicitud.APROBADA, publicador);
        repository.enviarMensajeChat(solicitud, adoptante, "Hola");
        ConversacionChat antes = repository.obtenerChat(solicitud, publicador);
        UsuarioLocal actualizado = new UsuarioLocal(11, "Ana María", adoptante.getEmail(), "Rosario",
                UsuarioLocal.ADOPTANTE, "Tengo un patio amplio", "file:///perfil/ana.png");

        repository.actualizarPerfilUsuario(actualizado);

        assertSame(actualizado, repository.obtenerSolicitante(solicitud, publicador));
        assertSame(actualizado, repository.obtenerDetalleMascota("m1", solicitud, publicador).getSolicitante());
        assertSame(actualizado, repository.obtenerHistorialPerfil(publicador).get(0).getAdoptante());
        ConversacionChat chat = repository.obtenerChat(solicitud, publicador);
        assertSame(actualizado, chat.obtenerInterlocutor(publicador));
        assertEquals("Hola", chat.getUltimoMensaje().getTexto());
        assertEquals("Hola", repository.obtenerMensajesChat(solicitud, actualizado).get(0).getTexto());
        assertEquals("Ana", antes.obtenerInterlocutor(publicador).getNombre());
        assertEquals(solicitud, chat.getSolicitudId());
    }

    @Test public void actualizarPublicadorReflejaSuPerfilEnPublicacionesYEnElChat() {
        assertEquals(PawMatchRepository.Resultado.OK, repository.publicarMascota(
                "Roco", "8 meses", "Santa Fe", "Cariñoso", "content://fotos/roco", publicador));
        Mascota mascota = repository.obtenerMascotasPublicadas(publicador).get(0);
        String solicitud = solicitar(mascota.getId());
        repository.resolverSolicitud(solicitud, EstadoSolicitud.APROBADA, publicador);
        UsuarioLocal actualizado = new UsuarioLocal(22, "Patitas felices", publicador.getEmail(), "Rosario",
                UsuarioLocal.PUBLICADOR, "Rescatamos mascotas", "file:///perfil/refugio.png");

        repository.actualizarPerfilUsuario(actualizado);

        DetalleMascota detalle = repository.obtenerDetalleMascota(mascota.getId(), solicitud, adoptante);
        assertSame(actualizado, detalle.getPublicador());
        assertEquals("Patitas felices", detalle.getRefugio().getNombre());
        assertSame(actualizado, repository.obtenerChat(solicitud, adoptante).obtenerInterlocutor(adoptante));
        assertEquals(1, repository.obtenerHistorialPerfil(actualizado).size());
        assertEquals("Santa Fe", detalle.getMascota().getCiudad());
    }

    @Test public void actualizarPerfilNoCambiaIdentidadesNiPermisosDeOtrasCuentas() {
        String solicitud = solicitar("m1");
        repository.resolverSolicitud(solicitud, EstadoSolicitud.APROBADA, publicador);
        ConversacionChat antes = repository.obtenerChat(solicitud, adoptante);
        for (UsuarioLocal ajeno : new UsuarioLocal[]{null,
                cuenta(33, "Otra persona", "Rosario", UsuarioLocal.ADOPTANTE),
                cuenta(11, "Otro rol", "Rosario", UsuarioLocal.PUBLICADOR),
                cuenta(0, "Sin sesión", "Rosario", UsuarioLocal.ADOPTANTE)}) {
            repository.actualizarPerfilUsuario(ajeno);
        }

        assertSame(adoptante, repository.obtenerSolicitante(solicitud, publicador));
        assertSame(antes, repository.obtenerChat(solicitud, adoptante));
        assertSame(publicador, antes.obtenerInterlocutor(adoptante));
    }
}
