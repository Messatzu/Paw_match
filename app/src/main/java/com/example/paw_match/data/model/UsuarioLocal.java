package com.example.paw_match.data.model;

/** Datos de la cuenta local; las credenciales se conservan únicamente en el repositorio. */
public final class UsuarioLocal {
    public static final String ADOPTANTE = "adoptante";
    public static final String PUBLICADOR = "publicador";

    private final long id;
    private final String nombre;
    private final String email;
    private final String ubicacion;
    private final String rol;
    private final String descripcion;
    private final String fotoUri;

    public UsuarioLocal(long id, String nombre, String email, String ubicacion, String rol) {
        this(id, nombre, email, ubicacion, rol, "", null);
    }

    public UsuarioLocal(long id, String nombre, String email, String ubicacion, String rol,
                        String descripcion, String fotoUri) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.ubicacion = ubicacion;
        this.rol = rol;
        this.descripcion = descripcion == null ? "" : descripcion;
        this.fotoUri = fotoUri;
    }

    public long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getUbicacion() { return ubicacion; }
    public String getRol() { return rol; }
    public String getDescripcion() { return descripcion; }
    public String getFotoUri() { return fotoUri; }
    public boolean esPublicador() { return PUBLICADOR.equals(rol); }
}
