package com.example.paw_match.data.model;

/** Mensaje inmutable de una conversación habilitada por una solicitud. */
public final class MensajeChat {
    private final String id;
    private final String solicitudId;
    private final long emisorId;
    private final String texto;
    private final long fecha;

    public MensajeChat(String id, String solicitudId, long emisorId, String texto, long fecha) {
        this.id = id;
        this.solicitudId = solicitudId;
        this.emisorId = emisorId;
        this.texto = texto;
        this.fecha = fecha;
    }

    public String getId() { return id; }
    public String getSolicitudId() { return solicitudId; }
    public long getEmisorId() { return emisorId; }
    public String getTexto() { return texto; }
    public long getFecha() { return fecha; }
}
