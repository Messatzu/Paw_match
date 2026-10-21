package com.example.paw_match.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.paw_match.R;

public class RegisterFragment extends Fragment {

    private EditText etNombre, etEmailReg, etPasswordReg;
    private RadioGroup rgRole;
    private Button btnRegister;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        etNombre = view.findViewById(R.id.etNombre);
        etEmailReg = view.findViewById(R.id.etEmailReg);
        etPasswordReg = view.findViewById(R.id.etPasswordReg);
        rgRole = view.findViewById(R.id.rgRole);
        btnRegister = view.findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String email = etEmailReg.getText().toString().trim();
            String password = etPasswordReg.getText().toString().trim();

            int selectedRoleId = rgRole.getCheckedRadioButtonId();
            String rol = (selectedRoleId == R.id.rbPublicador) ? "publicador" : "adoptante";

            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: Enviar datos vía POST al backend PHP para registrar en la base de datos
                Toast.makeText(getContext(), "Registrando como " + rol + "...", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
