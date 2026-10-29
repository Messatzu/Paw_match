package com.example.paw_match.data.mock;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.example.paw_match.data.model.*;

/** Datos ficticios de prueba para la demostración de PawMatch. */
public final class DatosDemo {
    private DatosDemo() {}
    public static final Adoptante ADOPTANTE = new Adoptante(
            "adoptante-1", "Alex Demo", "Santa Fe", "Busco un compañero y dispongo de tiempo para cuidarlo.");
    public static final Refugio REFUGIO = new Refugio(
            "refugio-1", "Refugio Patitas Demo", "contacto@example.invalid (dato ficticio)");
    public static List<Mascota> mascotas() {
        return Collections.unmodifiableList(Arrays.asList(
            new Mascota("m1", "Luna", "Perro", "2 años", "Mediano", "Tranquila y cariñosa", "Luna fue rescatada a los 6 meses. Disfruta enormemente los paseos en parques y convive alegremente con niños.", "foto_luna", REFUGIO.getId(), "14 kg", "Marzo 2022", "Santa Fe"),
            new Mascota("m2", "Milo", "Gato", "1 año", "Pequeño", "Curioso y juguetón", "Milo es un gatito de interior con mucha curiosidad. Le encantan los rascadores altos y tomar sol junto a la ventana.", "foto_milo", REFUGIO.getId(), "4 kg", "Abril 2023", "Santa Fe"),
            new Mascota("m3", "Toby", "Perro", "4 años", "Grande", "Sociable y leal", "Toby es un perro muy protector y alegre. Esta castrado, vacunado y listo para sumarse a un hogar con patio.", "foto_toby", REFUGIO.getId(), "28 kg", "Enero 2020", "Rosario"),
            new Mascota("m4", "Nina", "Gato", "3 años", "Pequeño", "Tranquila y mimosa", "Nina prefiere ambientes pacíficos y sin ruidos fuertes. Se adapta rápido a personas adultas o adultos mayores.", "foto_nina", REFUGIO.getId(), "3.5 kg", "Septiembre 2021", "Santa Fe"),
            new Mascota("m5", "Coco", "Perro", "8 meses", "Mediano", "Juguetón y activo", "Coco es un cachorrón con energía inagotable. Aprende rápido órdenes básicas y busca una familia activa.", "foto_coco", REFUGIO.getId(), "10 kg", "Julio 2023", "Santa Fe"),
            new Mascota("m6", "Simba", "Gato", "5 años", "Mediano", "Calmo y afectuoso", "Simba busca tranquilidad y cariño. Le encanta dormir la siesta al lado de sus cuidadores y es muy manso.", "foto_simba", REFUGIO.getId(), "5.2 kg", "Mayo 2019", "Paraná")
        ));
    }
}
