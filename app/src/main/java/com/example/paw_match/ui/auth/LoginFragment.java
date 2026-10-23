package com.example.paw_match.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.paw_match.R;

public class LoginFragment extends Fragment {

    public LoginFragment() { super(R.layout.fragment_login); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LocalAuthViewModel auth = new ViewModelProvider(requireActivity()).get(LocalAuthViewModel.class);

        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPassword = view.findViewById(R.id.etPassword);
        Button btnLogin = view.findViewById(R.id.btnLogin);
        Button btnGoToRegister = view.findViewById(R.id.btnGoToRegister);
        TextView tvAuthError = view.findViewById(R.id.tvAuthError);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError(getString(R.string.auth_email_invalid));
                etEmail.requestFocus();
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError(getString(R.string.auth_password_required));
                etPassword.requestFocus();
                return;
            }
            auth.iniciarSesion(email, password);
        });

        btnGoToRegister.setOnClickListener(v -> {
            auth.limpiarError();
            getParentFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(getId(), new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });

        auth.observarEstado().observe(getViewLifecycleOwner(), estado -> {
            btnLogin.setEnabled(!estado.cargando);
            btnGoToRegister.setEnabled(!estado.cargando);
            btnLogin.setText(estado.cargando ? R.string.auth_signing_in : R.string.auth_sign_in);
            tvAuthError.setVisibility(estado.error == 0 ? View.GONE : View.VISIBLE);
            if (estado.error != 0) tvAuthError.setText(estado.error);
        });
    }
}
