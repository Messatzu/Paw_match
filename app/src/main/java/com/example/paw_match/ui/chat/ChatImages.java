package com.example.paw_match.ui.chat;

import android.widget.ImageView;
import com.example.paw_match.data.model.Mascota;
import com.example.paw_match.ui.shared.PetImages;

final class ChatImages {
    private ChatImages() {}

    static void mostrar(ImageView image, Mascota mascota) {
        PetImages.mostrar(image, mascota);
    }
}
