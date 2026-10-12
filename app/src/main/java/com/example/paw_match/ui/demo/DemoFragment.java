package com.example.paw_match.ui.demo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.paw_match.R;
import com.example.paw_match.data.model.*;
import com.example.paw_match.data.repository.PawMatchRepository;
import com.example.paw_match.ui.shared.PawMatchViewModel;
import java.util.Collections;
import java.util.List;

/** Pantalla de diagnóstico temporal. Matías y Lucas reemplazan cada destino por su Fragment. */
public class DemoFragment extends Fragment {
    public static final String EXPLORAR = "explorar", SOLICITUDES = "solicitudes", REFUGIO = "refugio";
    private PawMatchViewModel vm;
    public DemoFragment() { super(R.layout.fragment_pm_demo); }
    public static DemoFragment nueva(String destino) {
        DemoFragment fragment = new DemoFragment();
        Bundle args = new Bundle(); args.putString("destino", destino); fragment.setArguments(args);
        return fragment;
    }
    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vm = new ViewModelProvider(requireActivity()).get(PawMatchViewModel.class);
        String destino = requireArguments().getString("destino", EXPLORAR);
        boolean explorar = EXPLORAR.equals(destino), refugio = REFUGIO.equals(destino);
        ((TextView) view.findViewById(R.id.pm_titulo)).setText(explorar ? R.string.pm_explorar : refugio ? R.string.pm_panel : R.string.pm_solicitudes);
        view.findViewById(R.id.pm_crear).setVisibility(explorar ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.pm_aprobar).setVisibility(refugio ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.pm_rechazar).setVisibility(refugio ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.pm_crear).setOnClickListener(v -> {
            for (Mascota m : vm.obtenerMascotas()) {
                boolean existe = false;
                for (SolicitudAdopcion s : actuales())
                    if (s.getMascotaId().equals(m.getId()) && s.getAdoptanteId().equals(vm.obtenerAdoptanteDemo().getId())) existe = true;
                if (!existe) { informar(vm.crearSolicitud(m.getId())); return; }
            }
            Toast.makeText(requireContext(), R.string.pm_sin_mascotas, Toast.LENGTH_SHORT).show();
        });
        view.findViewById(R.id.pm_aprobar).setOnClickListener(v -> resolverPrimera(EstadoSolicitud.APROBADA));
        view.findViewById(R.id.pm_rechazar).setOnClickListener(v -> resolverPrimera(EstadoSolicitud.RECHAZADA));
        vm.observarSolicitudes().observe(getViewLifecycleOwner(), lista -> {
            StringBuilder texto = new StringBuilder();
            if (explorar) {
                for (Mascota m : vm.obtenerMascotas()) texto.append(m.getNombre()).append(" · ").append(m.getEspecie()).append(" · ").append(m.getEdad()).append("\n");
            } else {
                if (refugio) {
                    Adoptante a = vm.obtenerAdoptanteDemo();
                    texto.append(a.getNombre()).append(" · ").append(a.getLocalidad()).append("\n").append(a.getPresentacion()).append("\n\n");
                }
                boolean hay = false;
                for (SolicitudAdopcion s : lista) {
                    Mascota m = vm.buscarMascota(s.getMascotaId());
                    if (m == null || (refugio && !m.getRefugioId().equals(vm.obtenerRefugioDemo().getId()))
                        || (!refugio && !s.getAdoptanteId().equals(vm.obtenerAdoptanteDemo().getId()))) continue;
                    hay = true;
                    int estado = s.getEstado() == EstadoSolicitud.PENDIENTE ? R.string.pm_pendiente : s.getEstado() == EstadoSolicitud.APROBADA ? R.string.pm_aprobada : R.string.pm_rechazada;
                    texto.append(m.getNombre()).append(" · ").append(getString(estado)).append("\n");
                    if (!refugio) {
                        String contacto = vm.obtenerContactoHabilitado(s.getId());
                        if (contacto != null) texto.append(getString(R.string.pm_contacto, contacto)).append("\n");
                    }
                    texto.append("\n");
                }
                if (!hay) texto.append(getString(R.string.pm_sin_solicitudes));
            }
            ((TextView) view.findViewById(R.id.pm_contenido)).setText(texto.toString());
        });
    }
    private List<SolicitudAdopcion> actuales() {
        List<SolicitudAdopcion> lista = vm.observarSolicitudes().getValue();
        return lista == null ? Collections.emptyList() : lista;
    }
    private void resolverPrimera(EstadoSolicitud estado) {
        for (SolicitudAdopcion s : actuales()) {
            Mascota m = vm.buscarMascota(s.getMascotaId());
            if (s.getEstado() == EstadoSolicitud.PENDIENTE && m != null && m.getRefugioId().equals(vm.obtenerRefugioDemo().getId())) {
                informar(vm.resolverSolicitud(s.getId(), estado)); return;
            }
        }
        Toast.makeText(requireContext(), R.string.pm_sin_pendientes, Toast.LENGTH_SHORT).show();
    }
    private void informar(PawMatchRepository.Resultado resultado) {
        int mensaje;
        switch (resultado) {
            case OK: mensaje = R.string.pm_ok; break;
            case DUPLICADA: mensaje = R.string.pm_duplicada; break;
            case YA_RESUELTA: mensaje = R.string.pm_ya_resuelta; break;
            default: mensaje = R.string.pm_error;
        }
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
    }
}
