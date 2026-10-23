package com.example.paw_match.data.repository;

import com.example.paw_match.data.mock.DatosDemo;
import com.example.paw_match.data.model.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Fuente única de datos de la demo. Se usa desde el hilo principal.
 * No conserva datos después de la muerte del proceso. No es autenticación real.
 */
public final class PawMatchRepository {
    private static final PawMatchRepository INSTANCE = new PawMatchRepository();
    public static PawMatchRepository getInstance() { return INSTANCE; }
    // Constructor de paquete para pruebas aisladas; la aplicación usa getInstance().
    PawMatchRepository() {}

    public interface Listener { void onChanged(); }
    public enum Resultado { OK, DUPLICADA, NO_ENCONTRADA, DATOS_INVALIDOS, YA_RESUELTA, NO_AUTORIZADO }
    private final List<Mascota> mascotas = DatosDemo.mascotas();
    private final List<SolicitudAdopcion> solicitudes = new ArrayList<>();
    private final List<HistorialMascota> bajasEnHistorial = new ArrayList<>();
    private final Map<String, UsuarioLocal> cuentasSolicitantes = new HashMap<>();
    private final Map<String, UsuarioLocal> cuentasPublicadoras = new HashMap<>();
    private final Map<String, ConversacionChat> conversaciones = new LinkedHashMap<>();
    private final Map<String, List<MensajeChat>> mensajes = new HashMap<>();
    private final List<Listener> listeners = new ArrayList<>();

    public void addListener(Listener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }
    public void removeListener(Listener listener) { listeners.remove(listener); }
    private void notificar() {
        for (Listener listener : new ArrayList<>(listeners)) listener.onChanged();
    }
    public List<Mascota> obtenerMascotas() { return mascotas; }
    public Adoptante obtenerAdoptanteDemo() { return DatosDemo.ADOPTANTE; }
    public Refugio obtenerRefugioDemo() { return DatosDemo.REFUGIO; }
    public Mascota buscarMascota(String id) {
        for (Mascota mascota : mascotas) if (mascota.getId().equals(id)) return mascota;
        return null;
    }
    public List<SolicitudAdopcion> obtenerSolicitudes() {
        return Collections.unmodifiableList(new ArrayList<>(solicitudes));
    }
    public List<SolicitudAdopcion> obtenerSolicitudesAdoptante(String adoptanteId) {
        List<SolicitudAdopcion> lista = new ArrayList<>();
        for (SolicitudAdopcion s : solicitudes)
            if (s.getAdoptanteId().equals(adoptanteId)) lista.add(s);
        return Collections.unmodifiableList(lista);
    }
    public List<SolicitudAdopcion> obtenerSolicitudesRefugio(String refugioId) {
        List<SolicitudAdopcion> lista = new ArrayList<>();
        for (SolicitudAdopcion s : solicitudes) {
            Mascota m = buscarMascota(s.getMascotaId());
            if (m != null && m.getRefugioId().equals(refugioId)) lista.add(s);
        }
        return Collections.unmodifiableList(lista);
    }
    public List<SolicitudAdopcion> obtenerSolicitudesPendientesRefugio(String refugioId) {
        List<SolicitudAdopcion> pendientes = new ArrayList<>();
        for (SolicitudAdopcion solicitud : obtenerSolicitudesRefugio(refugioId))
            if (solicitud.getEstado() == EstadoSolicitud.PENDIENTE) pendientes.add(solicitud);
        return Collections.unmodifiableList(pendientes);
    }
    private boolean mismaCuenta(UsuarioLocal a, UsuarioLocal b) {
        return a != null && b != null && a.getId() == b.getId() && a.getRol().equals(b.getRol());
    }
    private boolean cuentaValida(UsuarioLocal usuario) {
        return usuario != null && usuario.getId() > 0 && (usuario.esPublicador()
                || UsuarioLocal.ADOPTANTE.equals(usuario.getRol()));
    }
    private SolicitudAdopcion solicitudVisible(String id, UsuarioLocal usuario) {
        if (!cuentaValida(usuario)) return null;
        for (SolicitudAdopcion solicitud : solicitudes) {
            if (!solicitud.getId().equals(id)) continue;
            if (mismaCuenta(cuentasSolicitantes.get(id), usuario)) return solicitud;
            Mascota mascota = buscarMascota(solicitud.getMascotaId());
            if (usuario.esPublicador() && mascota != null
                    && mascota.getRefugioId().equals(DatosDemo.REFUGIO.getId())
                    && (solicitud.getEstado() == EstadoSolicitud.PENDIENTE
                    || mismaCuenta(cuentasPublicadoras.get(id), usuario))) return solicitud;
        }
        return null;
    }
    public List<SolicitudAdopcion> obtenerSolicitudesVisibles(UsuarioLocal usuario) {
        if (!cuentaValida(usuario)) return Collections.emptyList();
        List<SolicitudAdopcion> visibles = new ArrayList<>();
        for (SolicitudAdopcion solicitud : solicitudes)
            if ((!usuario.esPublicador() || solicitud.getEstado() == EstadoSolicitud.PENDIENTE)
                    && solicitudVisible(solicitud.getId(), usuario) != null) visibles.add(solicitud);
        return Collections.unmodifiableList(visibles);
    }
    public UsuarioLocal obtenerSolicitante(String solicitudId, UsuarioLocal usuario) {
        return solicitudVisible(solicitudId, usuario) == null ? null : cuentasSolicitantes.get(solicitudId);
    }
    private boolean publicacionDadaDeBaja(String mascotaId) {
        for (HistorialMascota baja : bajasEnHistorial)
            if (baja.getMascota().getId().equals(mascotaId)) return true;
        return false;
    }
    private boolean mascotaAdoptada(String mascotaId) {
        for (SolicitudAdopcion solicitud : solicitudes)
            if (solicitud.getMascotaId().equals(mascotaId)
                    && solicitud.getEstado() == EstadoSolicitud.APROBADA) return true;
        return false;
    }
    public DetalleMascota obtenerDetalleMascota(String mascotaId, String solicitudId, UsuarioLocal usuario) {
        Mascota mascota = buscarMascota(mascotaId);
        if (mascota == null || !cuentaValida(usuario)) return null;
        SolicitudAdopcion solicitud = null;
        if (solicitudId != null) {
            solicitud = solicitudVisible(solicitudId, usuario);
            if (solicitud == null || !solicitud.getMascotaId().equals(mascotaId)) return null;
        } else {
            for (SolicitudAdopcion candidata : solicitudes)
                if (candidata.getMascotaId().equals(mascotaId)
                        && solicitudVisible(candidata.getId(), usuario) != null) {
                    solicitud = candidata;
                    break;
                }
        }
        DetalleMascota.Estado estado;
        if (publicacionDadaDeBaja(mascotaId)) estado = DetalleMascota.Estado.DADA_DE_BAJA;
        else if (solicitud != null) {
            estado = solicitud.getEstado() == EstadoSolicitud.APROBADA ? DetalleMascota.Estado.APROBADA
                    : solicitud.getEstado() == EstadoSolicitud.RECHAZADA ? DetalleMascota.Estado.RECHAZADA
                    : DetalleMascota.Estado.PENDIENTE;
        } else estado = mascotaAdoptada(mascotaId) ? DetalleMascota.Estado.ADOPTADA : DetalleMascota.Estado.DISPONIBLE;
        String id = solicitud == null ? null : solicitud.getId();
        ConversacionChat chat = estado == DetalleMascota.Estado.APROBADA ? obtenerChat(id, usuario) : null;
        return new DetalleMascota(mascota, DatosDemo.REFUGIO, cuentasPublicadoras.get(id),
                cuentasSolicitantes.get(id), solicitud, chat, estado,
                !usuario.esPublicador() && estado == DetalleMascota.Estado.DISPONIBLE,
                usuario.esPublicador() && estado == DetalleMascota.Estado.PENDIENTE);
    }
    public List<HistorialMascota> obtenerHistorialAdoptante(String adoptanteId) {
        List<HistorialMascota> historial = new ArrayList<>();
        for (SolicitudAdopcion solicitud : obtenerSolicitudesAdoptante(adoptanteId)) {
            Mascota mascota = buscarMascota(solicitud.getMascotaId());
            if (solicitud.getEstado() == EstadoSolicitud.APROBADA && mascota != null)
                historial.add(new HistorialMascota(mascota, HistorialMascota.Estado.ADOPTADA,
                        solicitud.getId(), cuentasSolicitantes.get(solicitud.getId())));
        }
        return Collections.unmodifiableList(historial);
    }
    public List<HistorialMascota> obtenerHistorialRefugio(String refugioId) {
        List<HistorialMascota> historial = new ArrayList<>();
        for (SolicitudAdopcion solicitud : obtenerSolicitudesRefugio(refugioId)) {
            Mascota mascota = buscarMascota(solicitud.getMascotaId());
            if (solicitud.getEstado() == EstadoSolicitud.APROBADA && mascota != null)
                historial.add(new HistorialMascota(mascota, HistorialMascota.Estado.ADOPTADA,
                        solicitud.getId(), cuentasSolicitantes.get(solicitud.getId())));
        }
        for (HistorialMascota baja : bajasEnHistorial)
            if (baja.getMascota().getRefugioId().equals(refugioId)) historial.add(baja);
        return Collections.unmodifiableList(historial);
    }
    /** El perfil y su detalle comparten las mismas cuentas participantes que el chat. */
    public List<HistorialMascota> obtenerHistorialPerfil(UsuarioLocal usuario) {
        if (usuario == null || usuario.getId() <= 0
                || (!usuario.esPublicador() && !UsuarioLocal.ADOPTANTE.equals(usuario.getRol())))
            return Collections.emptyList();
        List<HistorialMascota> registros = usuario.esPublicador()
                ? obtenerHistorialRefugio(DatosDemo.REFUGIO.getId())
                : obtenerHistorialAdoptante(DatosDemo.ADOPTANTE.getId());
        List<HistorialMascota> visibles = new ArrayList<>();
        for (HistorialMascota registro : registros) {
            if (registro.getEstado() == HistorialMascota.Estado.PUBLICACION_DADA_DE_BAJA
                    && usuario.esPublicador()) visibles.add(registro);
            else if (obtenerChat(registro.getSolicitudId(), usuario) != null) visibles.add(registro);
        }
        return Collections.unmodifiableList(visibles);
    }
    public HistorialMascota obtenerDetalleHistorial(String mascotaId, String solicitudId, UsuarioLocal usuario) {
        for (HistorialMascota registro : obtenerHistorialPerfil(usuario))
            if (registro.getMascota().getId().equals(mascotaId)
                    && Objects.equals(registro.getSolicitudId(), solicitudId)) return registro;
        return null;
    }
    /** Registra una baja ya realizada para mostrarla en el perfil; no ejecuta la baja de la publicación. */
    public Resultado registrarBajaEnHistorial(String mascotaId, String refugioId) {
        Mascota mascota = buscarMascota(mascotaId);
        if (mascota == null || !mascota.getRefugioId().equals(refugioId)) return Resultado.DATOS_INVALIDOS;
        for (HistorialMascota baja : bajasEnHistorial)
            if (baja.getMascota().getId().equals(mascotaId)) return Resultado.DUPLICADA;
        for (SolicitudAdopcion solicitud : solicitudes)
            if (solicitud.getMascotaId().equals(mascotaId) && solicitud.getEstado() == EstadoSolicitud.APROBADA)
                return Resultado.YA_RESUELTA;
        bajasEnHistorial.add(new HistorialMascota(mascota, HistorialMascota.Estado.PUBLICACION_DADA_DE_BAJA));
        notificar();
        return Resultado.OK;
    }
    public Resultado crearSolicitud(String mascotaId, String adoptanteId) {
        return crearSolicitudInterna(mascotaId, adoptanteId, null);
    }
    public Resultado crearSolicitud(String mascotaId, String adoptanteId, UsuarioLocal cuenta) {
        if (cuenta == null || cuenta.getId() <= 0 || !UsuarioLocal.ADOPTANTE.equals(cuenta.getRol()))
            return Resultado.NO_AUTORIZADO;
        return crearSolicitudInterna(mascotaId, adoptanteId, cuenta);
    }
    private Resultado crearSolicitudInterna(String mascotaId, String adoptanteId, UsuarioLocal cuenta) {
        if (buscarMascota(mascotaId) == null || !DatosDemo.ADOPTANTE.getId().equals(adoptanteId))
            return Resultado.DATOS_INVALIDOS;
        if (publicacionDadaDeBaja(mascotaId) || mascotaAdoptada(mascotaId)) return Resultado.YA_RESUELTA;
        for (SolicitudAdopcion s : solicitudes)
            if (s.getMascotaId().equals(mascotaId) && s.getAdoptanteId().equals(adoptanteId)
                    && (cuenta == null ? cuentasSolicitantes.get(s.getId()) == null
                    : mismaCuenta(cuentasSolicitantes.get(s.getId()), cuenta)))
                return Resultado.DUPLICADA;
        String id = UUID.randomUUID().toString();
        solicitudes.add(new SolicitudAdopcion(id, mascotaId, adoptanteId, EstadoSolicitud.PENDIENTE));
        if (cuenta != null) cuentasSolicitantes.put(id, cuenta);
        notificar();
        return Resultado.OK;
    }
    public Resultado resolverSolicitud(String solicitudId, EstadoSolicitud nuevoEstado) {
        return resolverSolicitudInterna(solicitudId, nuevoEstado, null);
    }
    public Resultado resolverSolicitud(String solicitudId, EstadoSolicitud nuevoEstado, UsuarioLocal cuenta) {
        if (cuenta == null || cuenta.getId() <= 0 || !cuenta.esPublicador()) return Resultado.NO_AUTORIZADO;
        return resolverSolicitudInterna(solicitudId, nuevoEstado, cuenta);
    }
    private Resultado resolverSolicitudInterna(String solicitudId, EstadoSolicitud nuevoEstado, UsuarioLocal cuenta) {
        if (nuevoEstado != EstadoSolicitud.APROBADA && nuevoEstado != EstadoSolicitud.RECHAZADA)
            return Resultado.DATOS_INVALIDOS;
        for (int i = 0; i < solicitudes.size(); i++) {
            SolicitudAdopcion actual = solicitudes.get(i);
            if (!actual.getId().equals(solicitudId)) continue;
            if (actual.getEstado() != EstadoSolicitud.PENDIENTE) return Resultado.YA_RESUELTA;
            Mascota mascota = buscarMascota(actual.getMascotaId());
            if (cuenta != null && (mascota == null
                    || !mascota.getRefugioId().equals(DatosDemo.REFUGIO.getId()))) return Resultado.NO_AUTORIZADO;
            if (publicacionDadaDeBaja(actual.getMascotaId()) || mascotaAdoptada(actual.getMascotaId()))
                return Resultado.YA_RESUELTA;
            solicitudes.set(i, new SolicitudAdopcion(actual.getId(), actual.getMascotaId(),
                    actual.getAdoptanteId(), nuevoEstado));
            if (cuenta != null) cuentasPublicadoras.put(solicitudId, cuenta);
            UsuarioLocal solicitante = cuentasSolicitantes.get(solicitudId);
            if (nuevoEstado == EstadoSolicitud.APROBADA && solicitante != null && cuenta != null) {
                conversaciones.put(solicitudId, new ConversacionChat(solicitudId, mascota, solicitante, cuenta, null));
            }
            if (nuevoEstado == EstadoSolicitud.APROBADA) {
                for (int otra = 0; otra < solicitudes.size(); otra++) {
                    SolicitudAdopcion pendiente = solicitudes.get(otra);
                    if (pendiente.getMascotaId().equals(actual.getMascotaId())
                            && pendiente.getEstado() == EstadoSolicitud.PENDIENTE) {
                        solicitudes.set(otra, new SolicitudAdopcion(pendiente.getId(), pendiente.getMascotaId(),
                                pendiente.getAdoptanteId(), EstadoSolicitud.RECHAZADA));
                        if (cuenta != null) cuentasPublicadoras.put(pendiente.getId(), cuenta);
                    }
                }
            }
            notificar();
            return Resultado.OK;
        }
        return Resultado.NO_ENCONTRADA;
    }
    public String obtenerContactoHabilitado(String solicitudId, String adoptanteId) {
        for (SolicitudAdopcion s : solicitudes) {
            if (s.getId().equals(solicitudId) && s.getAdoptanteId().equals(adoptanteId)
                    && s.getEstado() == EstadoSolicitud.APROBADA) {
                Mascota m = buscarMascota(s.getMascotaId());
                if (m != null && m.getRefugioId().equals(DatosDemo.REFUGIO.getId()))
                    return DatosDemo.REFUGIO.getContacto();
            }
        }
        return null;
    }

    /** Tanto la lectura como el envío verifican aprobación, identidad y rol del participante. */
    public ConversacionChat obtenerChat(String solicitudId, UsuarioLocal usuario) {
        ConversacionChat chat = conversaciones.get(solicitudId);
        if (chat == null || !chat.incluye(usuario)) return null;
        for (SolicitudAdopcion solicitud : solicitudes)
            if (solicitud.getId().equals(solicitudId) && solicitud.getEstado() == EstadoSolicitud.APROBADA)
                return chat;
        return null;
    }
    public List<ConversacionChat> obtenerChats(UsuarioLocal usuario) {
        List<ConversacionChat> lista = new ArrayList<>();
        for (ConversacionChat chat : conversaciones.values())
            if (obtenerChat(chat.getSolicitudId(), usuario) != null) lista.add(chat);
        Collections.reverse(lista);
        return Collections.unmodifiableList(lista);
    }
    public List<MensajeChat> obtenerMensajesChat(String solicitudId, UsuarioLocal usuario) {
        if (obtenerChat(solicitudId, usuario) == null) return Collections.emptyList();
        List<MensajeChat> lista = mensajes.get(solicitudId);
        return lista == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(lista));
    }
    public Resultado enviarMensajeChat(String solicitudId, UsuarioLocal usuario, String texto) {
        ConversacionChat chat = obtenerChat(solicitudId, usuario);
        if (chat == null) return Resultado.NO_AUTORIZADO;
        if (texto == null || texto.trim().isEmpty()) return Resultado.DATOS_INVALIDOS;
        MensajeChat mensaje = new MensajeChat(UUID.randomUUID().toString(), solicitudId,
                usuario.getId(), texto.trim(), System.currentTimeMillis());
        mensajes.computeIfAbsent(solicitudId, ignored -> new ArrayList<>()).add(mensaje);
        conversaciones.remove(solicitudId);
        conversaciones.put(solicitudId, chat.conUltimoMensaje(mensaje));
        notificar();
        return Resultado.OK;
    }
    public void reiniciarDemo() {
        solicitudes.clear();
        bajasEnHistorial.clear();
        cuentasSolicitantes.clear();
        cuentasPublicadoras.clear();
        conversaciones.clear();
        mensajes.clear();
        notificar();
    }
}
