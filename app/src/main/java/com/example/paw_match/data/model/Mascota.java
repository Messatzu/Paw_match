package com.example.paw_match.data.model;

/** Modelo inmutable con información completa de la mascota. */
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
    private final String peso;
    private final String nacimiento;
    private final String ciudad;

    public Mascota(String id, String nombre, String especie, String edad, String tamano, String temperamento, String historia, String imagenNombre, String refugioId) {
        this(id, nombre, especie, edad, tamano, temperamento, historia, imagenNombre, refugioId, "12 kg", "Marzo 2022", "Santa Fe");
    }

    public Mascota(String id, String nombre, String especie, String edad, String tamano, String temperamento, String historia, String imagenNombre, String refugioId, String peso, String nacimiento, String ciudad) {
        this.id = id;
        this.nombre = nombre;
        this.especie = especie;
        this.edad = edad;
        this.tamano = tamano;
        this.temperamento = temperamento;
        this.historia = historia;
        this.imagenNombre = imagenNombre;
        this.refugioId = refugioId;
        this.peso = peso;
        this.nacimiento = nacimiento;
        this.ciudad = ciudad;
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
    public String getPeso() { return peso != null ? peso : "12 kg"; }
    public String getNacimiento() { return nacimiento != null ? nacimiento : "Marzo 2022"; }
    public String getCiudad() { return ciudad != null ? ciudad : "Santa Fe"; }
}
