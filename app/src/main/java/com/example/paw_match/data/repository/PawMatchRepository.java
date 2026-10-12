package com.example.paw_match.data.repository;

import com.example.paw_match.data.mock.DatosDemo;
import com.example.paw_match.data.model.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    public enum Resultado { OK, DUPLICADA, NO_ENCONTRADA, DATOS_INVALIDOS, YA_RESUELTA }
    private final List<Mascota> mascotas = DatosDemo.mascotas();
    private final List<SolicitudAdopcion> solicitudes = new ArrayList<>();
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
    public Resultado crearSolicitud(String mascotaId, String adoptanteId) {
        if (buscarMascota(mascotaId) == null || !DatosDemo.ADOPTANTE.getId().equals(adoptanteId))
            return Resultado.DATOS_INVALIDOS;
        for (SolicitudAdopcion s : solicitudes)
            if (s.getMascotaId().equals(mascotaId) && s.getAdoptanteId().equals(adoptanteId))
                return Resultado.DUPLICADA;
        solicitudes.add(new SolicitudAdopcion(UUID.randomUUID().toString(), mascotaId,
                adoptanteId, EstadoSolicitud.PENDIENTE));
        notificar();
        return Resultado.OK;
    }
    public Resultado resolverSolicitud(String solicitudId, EstadoSolicitud nuevoEstado) {
        if (nuevoEstado != EstadoSolicitud.APROBADA && nuevoEstado != EstadoSolicitud.RECHAZADA)
            return Resultado.DATOS_INVALIDOS;
        for (int i = 0; i < solicitudes.size(); i++) {
            SolicitudAdopcion actual = solicitudes.get(i);
            if (!actual.getId().equals(solicitudId)) continue;
            if (actual.getEstado() != EstadoSolicitud.PENDIENTE) return Resultado.YA_RESUELTA;
            solicitudes.set(i, new SolicitudAdopcion(actual.getId(), actual.getMascotaId(),
                    actual.getAdoptanteId(), nuevoEstado));
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
    public void reiniciarDemo() { solicitudes.clear(); notificar(); }
}
