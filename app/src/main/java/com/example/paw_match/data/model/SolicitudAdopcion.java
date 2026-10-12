package com.example.paw_match.data.model;

/** Modelo inmutable: los cambios de estado crean un nuevo objeto. */
public final class SolicitudAdopcion {
    private final String id;
    private final String mascotaId;
    private final String adoptanteId;
    private final EstadoSolicitud estado;

    public SolicitudAdopcion(String id, String mascotaId, String adoptanteId, EstadoSolicitud estado) {
        this.id = id;
        this.mascotaId = mascotaId;
        this.adoptanteId = adoptanteId;
        this.estado = estado;
    }

    public String getId() { return id; }

    public String getMascotaId() { return mascotaId; }

    public String getAdoptanteId() { return adoptanteId; }

    public EstadoSolicitud getEstado() { return estado; }
}
