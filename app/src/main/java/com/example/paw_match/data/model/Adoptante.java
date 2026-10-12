package com.example.paw_match.data.model;

/** Modelo inmutable: los cambios de estado crean un nuevo objeto. */
public final class Adoptante {
    private final String id;
    private final String nombre;
    private final String localidad;
    private final String presentacion;

    public Adoptante(String id, String nombre, String localidad, String presentacion) {
        this.id = id;
        this.nombre = nombre;
        this.localidad = localidad;
        this.presentacion = presentacion;
    }

    public String getId() { return id; }

    public String getNombre() { return nombre; }

    public String getLocalidad() { return localidad; }

    public String getPresentacion() { return presentacion; }
}
