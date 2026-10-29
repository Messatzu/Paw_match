package com.example.paw_match.ui.shared;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import com.example.paw_match.R;

/** Usa el mismo banner en ambos temas; el modo oscuro adapta únicamente sus colores. */
public class BrandBannerView extends AppCompatImageView {
    public BrandBannerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        boolean dark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        if (dark) {
            int primary = context.getColor(R.color.paw_primary);
            int text = context.getColor(R.color.paw_text_primary);
            int accent = context.getColor(R.color.paw_accent);
            float[] matrix = new float[20];
            mapearCanal(matrix, 0, Color.red(primary), Color.red(text), Color.red(accent));
            mapearCanal(matrix, 5, Color.green(primary), Color.green(text), Color.green(accent));
            mapearCanal(matrix, 10, Color.blue(primary), Color.blue(text), Color.blue(accent));
            matrix[18] = 1f; // Conserva el alfa original: las formas y sus bordes no cambian.
            setColorFilter(new ColorMatrixColorFilter(matrix));
        }
    }

    private static void mapearCanal(float[] matrix, int offset, int primary, int text, int accent) {
        // Colores de referencia del PNG: verde (3,102,100), texto (36,50,59), coral (241,128,102).
        // Sus componentes rojo/verde permiten adaptar la paleta sin reemplazar letras ni siluetas.
        float primaryRed = 3f - 36f;
        float primaryGreen = 102f - 50f;
        float accentRed = 241f - 36f;
        float accentGreen = 128f - 50f;
        float determinant = primaryRed * accentGreen - accentRed * primaryGreen;
        float primaryDelta = primary - text;
        float accentDelta = accent - text;
        matrix[offset] = (primaryDelta * accentGreen - accentDelta * primaryGreen) / determinant;
        matrix[offset + 1] = (accentDelta * primaryRed - primaryDelta * accentRed) / determinant;
        matrix[offset + 4] = text - 36f * matrix[offset] - 50f * matrix[offset + 1];
    }
}
