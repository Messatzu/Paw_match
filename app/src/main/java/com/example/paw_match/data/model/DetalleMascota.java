package com.example.paw_match.data.model;

/** Información visible de una mascota y de la solicitud de la cuenta actual. */
public final class DetalleMascota {
    public enum Estado { DISPONIBLE, PENDIENTE, APROBADA, RECHAZADA, ADOPTADA, DADA_DE_BAJA }

    private final Mascota mascota;
    private final Refugio refugio;
    private final UsuarioLocal publicador;
    private final UsuarioLocal solicitante;
    private final SolicitudAdopcion solicitud;
    private final ConversacionChat chat;
    private final Estado estado;
    private final boolean puedeSolicitar;
    private final boolean puedeResponder;

    public DetalleMascota(Mascota mascota, Refugio refugio, UsuarioLocal publicador,
                          UsuarioLocal solicitante, SolicitudAdopcion solicitud, ConversacionChat chat,
                          Estado estado, boolean puedeSolicitar, boolean puedeResponder) {
        this.mascota = mascota;
        this.refugio = refugio;
        this.publicador = publicador;
        this.solicitante = solicitante;
        this.solicitud = solicitud;
        this.chat = chat;
        this.estado = estado;
        this.puedeSolicitar = puedeSolicitar;
        this.puedeResponder = puedeResponder;
    }

    public Mascota getMascota() { return mascota; }
    public Refugio getRefugio() { return refugio; }
    public UsuarioLocal getPublicador() { return publicador; }
    public UsuarioLocal getSolicitante() { return solicitante; }
    public SolicitudAdopcion getSolicitud() { return solicitud; }
    public ConversacionChat getChat() { return chat; }
    public Estado getEstado() { return estado; }
    public boolean puedeSolicitar() { return puedeSolicitar; }
    public boolean puedeResponder() { return puedeResponder; }
}
