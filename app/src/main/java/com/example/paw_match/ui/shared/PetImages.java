package com.example.paw_match.ui.shared;

import android.net.Uri;
import android.widget.ImageView;
import com.example.paw_match.R;
import com.example.paw_match.data.model.Mascota;

/** Muestra tanto las fotos elegidas por el publicador como las fotos de la demo. */
public final class PetImages {
    private PetImages() {}

    public static void mostrar(ImageView imagen, Mascota mascota) {
        String fotoUri = mascota.getImagenUri();
        if (fotoUri != null && !fotoUri.isEmpty()) {
            try {
                imagen.setImageURI(Uri.parse(fotoUri));
                if (imagen.getDrawable() != null) {
                    configurar(imagen, true);
                    return;
                }
            } catch (SecurityException | IllegalArgumentException ignored) {
                // Una foto que dejó de estar disponible utiliza la imagen alternativa.
            }
        }
        String nombre = mascota.getImagenNombre();
        int id = nombre == null || nombre.isEmpty() ? 0 : imagen.getResources().getIdentifier(
                nombre, "drawable", imagen.getContext().getPackageName());
        imagen.setImageResource(id == 0 ? R.drawable.ic_paw_logo : id);
        configurar(imagen, id != 0);
    }

    private static void configurar(ImageView imagen, boolean tieneFoto) {
        imagen.setScaleType(tieneFoto ? ImageView.ScaleType.CENTER_CROP : ImageView.ScaleType.FIT_CENTER);
        int padding = tieneFoto ? 0 : Math.round(8 * imagen.getResources().getDisplayMetrics().density);
        imagen.setPadding(padding, padding, padding, padding);
    }
}
