package com.example.paw_match.data.model;

/** Modelo inmutable: los cambios de estado crean un nuevo objeto. */
public final class Mascota {
    private final String id;
    private final String nombre;
    private final String especie;
    private final String edad;
    private final String tamano;
    private final String temperamento;
    private final String historia;
    private final String imagenNombre;
    private final String refugioId;

    public Mascota(String id, String nombre, String especie, String edad, String tamano, String temperamento, String historia, String imagenNombre, String refugioId) {
        this.id = id;
        this.nombre = nombre;
        this.especie = especie;
        this.edad = edad;
        this.tamano = tamano;
        this.temperamento = temperamento;
        this.historia = historia;
        this.imagenNombre = imagenNombre;
        this.refugioId = refugioId;
    }

    public String getId() { return id; }

    public String getNombre() { return nombre; }

    public String getEspecie() { return especie; }

    public String getEdad() { return edad; }

    public String getTamano() { return tamano; }

    public String getTemperamento() { return temperamento; }

    public String getHistoria() { return historia; }

    public String getImagenNombre() { return imagenNombre; }

    public String getRefugioId() { return refugioId; }
}
