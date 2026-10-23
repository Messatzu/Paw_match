package com.example.paw_match.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

    public ProfileFragment() { super(R.layout.fragment_profile); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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

        btnLogout.setOnClickListener(v -> auth.cerrarSesion());

        auth.observarEstado().observe(getViewLifecycleOwner(), estado -> {
            btnLogout.setEnabled(!estado.cargando);
            btnLogout.setText(estado.cargando ? R.string.profile_logging_out : R.string.profile_logout);
            tvProfileError.setVisibility(estado.error == 0 ? View.GONE : View.VISIBLE);
            if (estado.error != 0) tvProfileError.setText(estado.error);

            UsuarioLocal usuario = estado.usuario;
            if (usuario == null) return;
            tvUserName.setText(usuario.getNombre());
            tvUserEmail.setText(usuario.getEmail());
            tvUserLocation.setText(getString(R.string.profile_location, usuario.getUbicacion()));
            tvUserBio.setText(R.string.profile_no_bio);
            tvUserRole.setText(usuario.esPublicador()
                    ? R.string.profile_role_publicador : R.string.profile_role_adoptante);
            ((MainActivity) requireActivity()).configurarNavegacion(
                    view.findViewById(R.id.bottom_navigation), R.id.nav_perfil);
            actualizarHistorial.run();
        });
    }
}
