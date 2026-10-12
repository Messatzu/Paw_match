package com.example.paw_match.data.mock;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.example.paw_match.data.model.*;

/** Datos ficticios. imagenNombre queda preparado para recursos drawable futuros. */
public final class DatosDemo {
    private DatosDemo() {}
    public static final Adoptante ADOPTANTE = new Adoptante(
            "adoptante-1", "Alex Demo", "Santa Fe", "Busco un compañero y dispongo de tiempo para cuidarlo.");
    public static final Refugio REFUGIO = new Refugio(
            "refugio-1", "Refugio Patitas Demo", "contacto@example.invalid (dato ficticio)");
    public static List<Mascota> mascotas() {
        return Collections.unmodifiableList(Arrays.asList(
            new Mascota("m1", "Luna", "Perro", "2 años", "Mediano", "Tranquila", "Disfruta los paseos y la compañía.", "mascota_luna", REFUGIO.getId()),
            new Mascota("m2", "Milo", "Gato", "1 año", "Pequeño", "Curioso", "Le gusta explorar y jugar.", "mascota_milo", REFUGIO.getId()),
            new Mascota("m3", "Toby", "Perro", "4 años", "Grande", "Sociable", "Busca una familia que lo acompañe.", "mascota_toby", REFUGIO.getId()),
            new Mascota("m4", "Nina", "Gato", "3 años", "Pequeño", "Cariñosa", "Prefiere los espacios tranquilos.", "mascota_nina", REFUGIO.getId()),
            new Mascota("m5", "Coco", "Perro", "8 meses", "Mediano", "Juguetón", "Tiene mucha energía para compartir.", "mascota_coco", REFUGIO.getId()),
            new Mascota("m6", "Simba", "Gato", "5 años", "Mediano", "Calmo", "Disfruta descansar cerca de las personas.", "mascota_simba", REFUGIO.getId())
        ));
    }
}
