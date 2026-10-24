package com.example.paw_match.ui.publisher;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paw_match.MainActivity;
import com.example.paw_match.R;
import com.example.paw_match.data.model.EstadoSolicitud;
import com.example.paw_match.data.model.Adoptante;
import com.example.paw_match.data.model.UsuarioLocal;
import com.example.paw_match.data.model.Mascota;
import com.example.paw_match.data.model.SolicitudAdopcion;
import com.example.paw_match.data.repository.PawMatchRepository;
import com.example.paw_match.ui.shared.PawMatchViewModel;
import com.example.paw_match.ui.detalle.DetalleMascotaFragment;
import java.util.ArrayList;
import java.util.List;
import com.google.android.material.snackbar.Snackbar;

/** Cola del publicador; el adoptante puede consultar sus solicitudes y cancelar las pendientes. */
public class PublisherRequestsFragment extends Fragment {
    public PublisherRequestsFragment() { super(R.layout.fragment_publisher_requests); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PawMatchViewModel vm = new ViewModelProvider(requireActivity()).get(PawMatchViewModel.class);
        AdoptionRequestAdapter adapter = new AdoptionRequestAdapter(vm.esModoRefugio(), (solicitud, decision) -> {
            PawMatchRepository.Resultado resultado = vm.resolverSolicitud(solicitud.getId(), decision);
            int message = resultado == PawMatchRepository.Resultado.OK
                    ? (decision == EstadoSolicitud.APROBADA
                    ? R.string.pm_request_accepted : R.string.pm_request_rejected)
                    : R.string.pm_error;
            if (resultado == PawMatchRepository.Resultado.OK && decision == EstadoSolicitud.APROBADA
                    && vm.obtenerChat(solicitud.getId()) != null) {
                MainActivity activity = (MainActivity) requireActivity();
                Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(requireContext().getColor(R.color.paw_snackbar_background))
                        .setTextColor(requireContext().getColor(R.color.paw_on_header))
                        .setActionTextColor(requireContext().getColor(R.color.paw_accent))
                        .setAnchorView(view.findViewById(R.id.bottom_navigation))
                        .setAction(R.string.chat_open, v -> activity.abrirChat(solicitud.getId()))
                        .show();
            } else {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        }, solicitud -> {
            if (getParentFragmentManager().isStateSaved()) return;
            getParentFragmentManager().beginTransaction().setReorderingAllowed(true)
                    .replace(getId(), DetalleMascotaFragment.desdeSolicitud(
                            solicitud.getMascotaId(), solicitud.getId()))
                    .addToBackStack("detalle_solicitud").commit();
        }, solicitud -> {
            PawMatchRepository.Resultado resultado = vm.cancelarSolicitud(solicitud.getId());
            int mensaje = resultado == PawMatchRepository.Resultado.OK ? R.string.pm_request_cancelled
                    : resultado == PawMatchRepository.Resultado.YA_RESUELTA
                    ? R.string.pm_request_cancel_resolved : R.string.pm_error;
            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
        });
        RecyclerView recycler = view.findViewById(R.id.recyclerRequests);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);
        Runnable actualizar = () -> {
            boolean publicador = vm.esModoRefugio();
            adapter.setPublicador(publicador);
            ((TextView) view.findViewById(R.id.tvRequestsTitle)).setText(publicador
                    ? R.string.pm_pending_requests_title : R.string.pm_my_requests_title);
            ((TextView) view.findViewById(R.id.tvEmptyRequestsTitle)).setText(publicador
                    ? R.string.pm_no_pending_requests_title : R.string.pm_sin_solicitudes);
            ((TextView) view.findViewById(R.id.tvEmptyRequestsBody)).setText(publicador
                    ? R.string.pm_no_pending_requests_body : R.string.pm_no_sent_requests_body);
            List<AdoptionRequestAdapter.Item> items = new ArrayList<>();
            for (SolicitudAdopcion solicitud : vm.obtenerSolicitudesVisibles()) {
                Mascota mascota = vm.buscarMascota(solicitud.getMascotaId());
                UsuarioLocal cuenta = vm.obtenerSolicitante(solicitud.getId());
                Adoptante adoptante = cuenta == null ? vm.obtenerAdoptanteDemo()
                        : new Adoptante(Long.toString(cuenta.getId()), cuenta.getNombre(), cuenta.getUbicacion(), cuenta.getDescripcion());
                if (mascota != null) items.add(new AdoptionRequestAdapter.Item(solicitud,
                        mascota, adoptante));
            }
            adapter.submitList(items);
            recycler.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
            view.findViewById(R.id.emptyRequests).setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            TextView count = view.findViewById(R.id.tvRequestsCount);
            count.setText(getResources().getQuantityString(publicador
                    ? R.plurals.pm_pending_requests_count : R.plurals.pm_requests_count,
                    items.size(), items.size()));
        };
        vm.observarSolicitudes().observe(getViewLifecycleOwner(), ignored -> actualizar.run());
        vm.observarModoRefugio().observe(getViewLifecycleOwner(), ignored -> {
            ((MainActivity) requireActivity()).configurarNavegacion(
                    view.findViewById(R.id.bottom_navigation), R.id.nav_solicitudes);
            actualizar.run();
        });
    }
}
