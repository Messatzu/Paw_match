package com.example.paw_match.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.util.Patterns;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.paw_match.R;
import com.example.paw_match.data.model.UsuarioLocal;

public class RegisterFragment extends Fragment {

    public RegisterFragment() { super(R.layout.fragment_register); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LocalAuthViewModel auth = new ViewModelProvider(requireActivity()).get(LocalAuthViewModel.class);

        EditText etNombre = view.findViewById(R.id.etNombre);
        EditText etEmailReg = view.findViewById(R.id.etEmailReg);
        EditText etPasswordReg = view.findViewById(R.id.etPasswordReg);
        EditText etUserLocation = view.findViewById(R.id.etUserLocation);
        RadioGroup rgRole = view.findViewById(R.id.rgRole);
        Button btnRegister = view.findViewById(R.id.btnRegister);
        Button btnBackToLogin = view.findViewById(R.id.btnBackToLogin);
        TextView tvAuthError = view.findViewById(R.id.tvAuthError);

        btnRegister.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String email = etEmailReg.getText().toString().trim();
            String password = etPasswordReg.getText().toString();
            String ubicacion = etUserLocation.getText().toString().trim();

            int selectedRoleId = rgRole.getCheckedRadioButtonId();
            String rol = selectedRoleId == R.id.rbPublicador
                    ? UsuarioLocal.PUBLICADOR : UsuarioLocal.ADOPTANTE;

            if (nombre.isEmpty()) {
                etNombre.setError(getString(R.string.auth_name_required));
                etNombre.requestFocus();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmailReg.setError(getString(R.string.auth_email_invalid));
                etEmailReg.requestFocus();
                return;
            }
            if (password.length() < 6) {
                etPasswordReg.setError(getString(R.string.auth_password_too_short));
                etPasswordReg.requestFocus();
                return;
            }
            if (ubicacion.isEmpty()) {
                etUserLocation.setError(getString(R.string.auth_location_required));
                etUserLocation.requestFocus();
                return;
            }
            auth.registrar(nombre, email, password, ubicacion, rol);
        });

        btnBackToLogin.setOnClickListener(v -> {
            auth.limpiarError();
            getParentFragmentManager().popBackStack();
        });

        auth.observarEstado().observe(getViewLifecycleOwner(), estado -> {
            btnRegister.setEnabled(!estado.cargando);
            btnBackToLogin.setEnabled(!estado.cargando);
            btnRegister.setText(estado.cargando ? R.string.auth_registering : R.string.auth_register);
            tvAuthError.setVisibility(estado.error == 0 ? View.GONE : View.VISIBLE);
            if (estado.error != 0) tvAuthError.setText(estado.error);
        });
    }
}
