package com.example.paw_match.ui.chat;

import android.widget.ImageView;
import com.example.paw_match.R;
import com.example.paw_match.data.model.Mascota;

final class ChatImages {
    private ChatImages() {}

    static void mostrar(ImageView image, Mascota mascota) {
        android.content.Context context = image.getContext();
        int id = context.getResources().getIdentifier(mascota.getImagenNombre(), "drawable", context.getPackageName());
        image.setImageResource(id == 0 ? R.drawable.ic_paw_logo : id);
        image.setScaleType(id == 0 ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
        int padding = id == 0 ? Math.round(8 * context.getResources().getDisplayMetrics().density) : 0;
        image.setPadding(padding, padding, padding, padding);
    }
}
