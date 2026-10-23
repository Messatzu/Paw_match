package com.example.paw_match.data.model;

/** Una mascota que forma parte del historial de adopciones o publicaciones del perfil. */
public final class HistorialMascota {
    public enum Estado { ADOPTADA, PUBLICACION_DADA_DE_BAJA }

    private final Mascota mascota;
    private final Estado estado;
    private final String solicitudId;
    private final UsuarioLocal adoptante;

    public HistorialMascota(Mascota mascota, Estado estado) {
        this(mascota, estado, null, null);
    }

    public HistorialMascota(Mascota mascota, Estado estado, String solicitudId, UsuarioLocal adoptante) {
        this.mascota = mascota;
        this.estado = estado;
        this.solicitudId = solicitudId;
        this.adoptante = adoptante;
    }

    public Mascota getMascota() { return mascota; }
    public Estado getEstado() { return estado; }
    public String getSolicitudId() { return solicitudId; }
    public UsuarioLocal getAdoptante() { return adoptante; }
}
