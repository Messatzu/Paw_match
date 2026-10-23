package com.example.paw_match.data.model;

/** Una conversación por solicitud, con las cuentas reales que la enviaron y aceptaron. */
public final class ConversacionChat {
    private final String solicitudId;
    private final Mascota mascota;
    private final UsuarioLocal adoptante;
    private final UsuarioLocal publicador;
    private final MensajeChat ultimoMensaje;

    public ConversacionChat(String solicitudId, Mascota mascota, UsuarioLocal adoptante,
                            UsuarioLocal publicador, MensajeChat ultimoMensaje) {
        this.solicitudId = solicitudId;
        this.mascota = mascota;
        this.adoptante = adoptante;
        this.publicador = publicador;
        this.ultimoMensaje = ultimoMensaje;
    }

    public String getSolicitudId() { return solicitudId; }
    public Mascota getMascota() { return mascota; }
    public MensajeChat getUltimoMensaje() { return ultimoMensaje; }

    public boolean incluye(UsuarioLocal usuario) {
        return usuario != null && ((usuario.getId() == adoptante.getId()
                && UsuarioLocal.ADOPTANTE.equals(usuario.getRol()))
                || (usuario.getId() == publicador.getId() && usuario.esPublicador()));
    }

    public UsuarioLocal obtenerInterlocutor(UsuarioLocal usuario) {
        if (!incluye(usuario)) return null;
        return usuario.esPublicador() ? adoptante : publicador;
    }

    public ConversacionChat conUltimoMensaje(MensajeChat mensaje) {
        return new ConversacionChat(solicitudId, mascota, adoptante, publicador, mensaje);
    }
}
