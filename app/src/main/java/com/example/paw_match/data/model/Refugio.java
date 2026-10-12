package com.example.paw_match.data.model;

/** Modelo inmutable: los cambios de estado crean un nuevo objeto. */
public final class Refugio {
    private final String id;
    private final String nombre;
    private final String contacto;

    public Refugio(String id, String nombre, String contacto) {
        this.id = id;
        this.nombre = nombre;
        this.contacto = contacto;
    }

    public String getId() { return id; }

    public String getNombre() { return nombre; }

    public String getContacto() { return contacto; }
}
