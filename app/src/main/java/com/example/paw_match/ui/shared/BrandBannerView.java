package com.example.paw_match.ui.shared;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;
import com.example.paw_match.R;

/** Conserva el banner original en claro y dibuja una versión nítida y centrada en oscuro. */
public class BrandBannerView extends AppCompatImageView {
    // Proporción visible del PNG original, sin sus márgenes transparentes.
    private static final float LIGHT_LOGO_WIDTH_RATIO = 1832f / 2172f;
    private static final float LIGHT_LOGO_HEIGHT_RATIO = 400f / 724f;

    private final Paint lettering = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect wordBounds = new Rect();
    private final Drawable mark;

    public BrandBannerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mark = AppCompatResources.getDrawable(context, R.drawable.ic_brand_mark);
        lettering.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        lettering.setTextSize(48f);
        lettering.getTextBounds("PawMatch", 0, 8, wordBounds);
    }

    @Override protected void onDraw(@NonNull Canvas canvas) {
        boolean dark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        if (!dark || mark == null) {
            super.onDraw(canvas);
            return;
        }
        float iconWidth = 49f;
        float iconHeight = 64f;
        float gap = 14f;
        float width = iconWidth + gap + wordBounds.width();
        float availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        float availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        if (availableWidth <= 0 || availableHeight <= 0) return;
        float logoWidth = availableWidth;
        float logoHeight = availableHeight;
        Drawable lightLogo = getDrawable();
        if (lightLogo != null && lightLogo.getIntrinsicWidth() > 0
                && lightLogo.getIntrinsicHeight() > 0) {
            float lightScale = Math.min(availableWidth / lightLogo.getIntrinsicWidth(),
                    availableHeight / lightLogo.getIntrinsicHeight());
            logoWidth = lightLogo.getIntrinsicWidth() * lightScale * LIGHT_LOGO_WIDTH_RATIO;
            logoHeight = lightLogo.getIntrinsicHeight() * lightScale * LIGHT_LOGO_HEIGHT_RATIO;
        }
        int saved = canvas.save();
        canvas.translate(getPaddingLeft() + (availableWidth - logoWidth) / 2f,
                getPaddingTop() + (availableHeight - logoHeight) / 2f);
        canvas.scale(logoWidth / width, logoHeight / iconHeight);
        mark.setBounds(0, 0, (int) iconWidth, (int) iconHeight);
        mark.draw(canvas);
        float x = iconWidth + gap - wordBounds.left;
        float baseline = iconHeight / 2f - (wordBounds.top + wordBounds.bottom) / 2f;
        lettering.setColor(getContext().getColor(R.color.paw_primary));
        canvas.drawText("Paw", x, baseline, lettering);
        lettering.setColor(getContext().getColor(R.color.paw_text_primary));
        canvas.drawText("Match", x + lettering.measureText("Paw"), baseline, lettering);
        canvas.restoreToCount(saved);
    }
}
