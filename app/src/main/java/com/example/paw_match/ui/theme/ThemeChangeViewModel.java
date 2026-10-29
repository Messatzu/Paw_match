package com.example.paw_match.ui.theme;

import android.view.View;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import com.example.paw_match.R;

/** Conserva únicamente en memoria el campo que no participa del guardado de vistas. */
public class ThemeChangeViewModel extends ViewModel {
    private int fieldId = View.NO_ID;
    private String draft;
    private int selectionStart;
    private int selectionEnd;

    public void conservarFormulario(@Nullable View view) {
        limpiar();
        if (view == null) return;
        for (int id : new int[]{R.id.etPassword, R.id.etPasswordReg}) {
            EditText field = view.findViewById(id);
            if (field == null) continue;
            fieldId = id;
            draft = field.getText().toString();
            selectionStart = field.getSelectionStart();
            selectionEnd = field.getSelectionEnd();
            return;
        }
    }

    public void restaurarFormulario(@Nullable View view) {
        if (draft == null) return;
        EditText field = view == null ? null : view.findViewById(fieldId);
        if (field != null) {
            field.setText(draft);
            field.setSelection(Math.max(0, Math.min(selectionStart, draft.length())),
                    Math.max(0, Math.min(selectionEnd, draft.length())));
        }
        limpiar();
    }

    private void limpiar() {
        draft = null;
        fieldId = View.NO_ID;
    }

    @Override protected void onCleared() { limpiar(); }
}
