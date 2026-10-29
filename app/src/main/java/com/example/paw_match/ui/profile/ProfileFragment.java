package com.example.paw_match.ui.profile;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paw_match.R;
import com.example.paw_match.MainActivity;
import com.example.paw_match.data.model.UsuarioLocal;
import com.example.paw_match.data.model.HistorialMascota;
import com.example.paw_match.ui.auth.LocalAuthViewModel;
import com.example.paw_match.ui.detalle.DetalleMascotaFragment;
import com.example.paw_match.ui.shared.PawMatchViewModel;
import java.util.List;

public class ProfileFragment extends Fragment {
    private static final String EDITANDO_PERFIL = "editando_perfil";
    private static final String ESPERANDO_GUARDADO = "esperando_guardado";
    private boolean editando;
    private boolean esperandoGuardado;
    private final ActivityResultLauncher<String> seleccionarFoto = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                LocalAuthViewModel auth = new ViewModelProvider(requireActivity()).get(LocalAuthViewModel.class);
                LocalAuthViewModel.Estado estado = auth.observarEstado().getValue();
                if (estado != null && estado.usuario != null && !estado.cargando) auth.actualizarFotoPerfil(uri);
            });

    public ProfileFragment() { super(R.layout.fragment_profile); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            editando = savedInstanceState.getBoolean(EDITANDO_PERFIL);
            esperandoGuardado = savedInstanceState.getBoolean(ESPERANDO_GUARDADO);
        }
        LocalAuthViewModel auth = new ViewModelProvider(requireActivity()).get(LocalAuthViewModel.class);
        PawMatchViewModel vm = new ViewModelProvider(requireActivity()).get(PawMatchViewModel.class);
        AdoptedPetAdapter historyAdapter = new AdoptedPetAdapter(registro -> {
            if (getParentFragmentManager().isStateSaved()) return;
            getParentFragmentManager().beginTransaction().setReorderingAllowed(true)
                    .replace(getId(), DetalleMascotaFragment.desdeHistorial(
                            registro.getMascota().getId(), registro.getSolicitudId()))
                    .addToBackStack("history_detail").commit();
        });
        RecyclerView history = view.findViewById(R.id.recyclerProfileHistory);
        history.setLayoutManager(new LinearLayoutManager(requireContext()));
        history.setAdapter(historyAdapter);
        TextView historyTitle = view.findViewById(R.id.tvProfileHistoryTitle);
        TextView historyEmpty = view.findViewById(R.id.tvNoAdoptions);
        Runnable actualizarHistorial = () -> {
            LocalAuthViewModel.Estado estado = auth.observarEstado().getValue();
            if (estado == null || estado.usuario == null) return;
            boolean publicador = estado.usuario.esPublicador();
            List<HistorialMascota> registros = vm.obtenerHistorialPerfil();
            historyTitle.setText(publicador ? R.string.profile_publication_history_title
                    : R.string.profile_adoption_history_title);
            historyEmpty.setText(publicador ? R.string.profile_publication_history_empty
                    : R.string.profile_no_adoptions);
            historyEmpty.setVisibility(registros.isEmpty() ? View.VISIBLE : View.GONE);
            history.setVisibility(registros.isEmpty() ? View.GONE : View.VISIBLE);
            historyAdapter.submitList(registros);
        };
        vm.observarSolicitudes().observe(getViewLifecycleOwner(), ignored -> actualizarHistorial.run());

        TextView tvUserName = view.findViewById(R.id.tvUserName);
        TextView tvUserEmail = view.findViewById(R.id.tvUserEmail);
        TextView tvUserLocation = view.findViewById(R.id.tvUserLocation);
        TextView tvUserBio = view.findViewById(R.id.tvUserBio);
        TextView tvUserRole = view.findViewById(R.id.tvUserRole);
        TextView tvProfileError = view.findViewById(R.id.tvProfileError);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);
        Button btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        Button btnCancelProfile = view.findViewById(R.id.btnCancelProfile);
        EditText nombre = view.findViewById(R.id.etProfileName);
        EditText ubicacion = view.findViewById(R.id.etProfileLocation);
        EditText descripcion = view.findViewById(R.id.etProfileDescription);
        ImageView foto = view.findViewById(R.id.imgUserPhoto);
        TextView fotoHint = view.findViewById(R.id.tvProfilePhotoHint);

        btnLogout.setOnClickListener(v -> auth.cerrarSesion());
        foto.setTooltipText(getString(R.string.profile_change_photo));
        foto.setOnClickListener(v -> seleccionarFoto.launch("image/*"));
        btnEditProfile.setOnClickListener(v -> {
            LocalAuthViewModel.Estado actual = auth.observarEstado().getValue();
            if (actual == null || actual.usuario == null || actual.cargando) return;
            nombre.setText(actual.usuario.getNombre());
            ubicacion.setText(actual.usuario.getUbicacion());
            descripcion.setText(actual.usuario.getDescripcion());
            nombre.setError(null);
            ubicacion.setError(null);
            editando = true;
            auth.limpiarError();
            actualizarEdicion(view);
            nombre.requestFocus();
        });
        btnCancelProfile.setOnClickListener(v -> {
            editando = false;
            esperandoGuardado = false;
            auth.limpiarError();
            ocultarTeclado(view);
            actualizarEdicion(view);
        });
        btnSaveProfile.setOnClickListener(v -> {
            String nuevoNombre = nombre.getText().toString().trim();
            String nuevaUbicacion = ubicacion.getText().toString().trim();
            if (nuevoNombre.isEmpty()) {
                nombre.setError(getString(R.string.auth_name_required));
                nombre.requestFocus();
                return;
            }
            if (nuevaUbicacion.isEmpty()) {
                ubicacion.setError(getString(R.string.auth_location_required));
                ubicacion.requestFocus();
                return;
            }
            esperandoGuardado = true;
            ocultarTeclado(view);
            auth.actualizarPerfil(nuevoNombre, nuevaUbicacion, descripcion.getText().toString().trim());
        });
        actualizarEdicion(view);

        auth.observarEstado().observe(getViewLifecycleOwner(), estado -> {
            btnLogout.setEnabled(!estado.cargando);
            btnLogout.setText(estado.operacion == LocalAuthViewModel.Operacion.CERRAR_SESION
                    ? R.string.profile_logging_out : R.string.profile_logout);
            btnEditProfile.setEnabled(!estado.cargando);
            btnSaveProfile.setEnabled(!estado.cargando);
            btnCancelProfile.setEnabled(!estado.cargando);
            nombre.setEnabled(!estado.cargando);
            ubicacion.setEnabled(!estado.cargando);
            descripcion.setEnabled(!estado.cargando);
            foto.setEnabled(!estado.cargando);
            btnSaveProfile.setText(estado.operacion == LocalAuthViewModel.Operacion.ACTUALIZAR_PERFIL
                    ? R.string.profile_saving : R.string.profile_save);
            fotoHint.setText(estado.operacion == LocalAuthViewModel.Operacion.ACTUALIZAR_FOTO
                    ? R.string.profile_photo_saving : R.string.profile_photo_hint);
            tvProfileError.setVisibility(estado.error == 0 ? View.GONE : View.VISIBLE);
            if (estado.error != 0) tvProfileError.setText(estado.error);

            UsuarioLocal usuario = estado.usuario;
            if (usuario == null) return;
            if (esperandoGuardado && !estado.cargando) {
                esperandoGuardado = false;
                if (estado.error == 0 && usuario.getNombre().equals(nombre.getText().toString().trim())
                        && usuario.getUbicacion().equals(ubicacion.getText().toString().trim())
                        && usuario.getDescripcion().equals(descripcion.getText().toString().trim())) {
                    editando = false;
                    Toast.makeText(requireContext(), R.string.profile_updated, Toast.LENGTH_SHORT).show();
                }
            }
            tvUserName.setText(usuario.getNombre());
            tvUserEmail.setText(usuario.getEmail());
            tvUserLocation.setText(getString(R.string.profile_location, usuario.getUbicacion()));
            tvUserBio.setText(usuario.getDescripcion().isEmpty()
                    ? getString(R.string.profile_no_bio) : usuario.getDescripcion());
            mostrarFoto(foto, usuario.getFotoUri());
            actualizarEdicion(view);
            tvUserRole.setText(usuario.esPublicador()
                    ? R.string.profile_role_publicador : R.string.profile_role_adoptante);
            ((MainActivity) requireActivity()).configurarNavegacion(
                    view.findViewById(R.id.bottom_navigation), R.id.nav_perfil);
            actualizarHistorial.run();
        });
    }

    @Override public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(EDITANDO_PERFIL, editando);
        outState.putBoolean(ESPERANDO_GUARDADO, esperandoGuardado);
        super.onSaveInstanceState(outState);
    }

    private void actualizarEdicion(View view) {
        view.findViewById(R.id.profileEditForm).setVisibility(editando ? View.VISIBLE : View.GONE);
        for (int id : new int[]{R.id.btnEditProfile, R.id.tvUserLocation, R.id.tvUserBio})
            view.findViewById(id).setVisibility(editando ? View.GONE : View.VISIBLE);
    }

    private void ocultarTeclado(View view) {
        InputMethodManager teclado = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (teclado != null) teclado.hideSoftInputFromWindow(view.getWindowToken(), 0);
        view.clearFocus();
    }

    private static void mostrarFoto(ImageView foto, String fotoUri) {
        if (fotoUri != null && !fotoUri.isEmpty()) {
            try {
                foto.setImageURI(Uri.parse(fotoUri));
                if (foto.getDrawable() != null) return;
            } catch (SecurityException | IllegalArgumentException ignored) {
                // Una foto inaccesible vuelve a usar el avatar predeterminado.
            }
        }
        foto.setImageResource(R.mipmap.ic_launcher_round);
    }
}
