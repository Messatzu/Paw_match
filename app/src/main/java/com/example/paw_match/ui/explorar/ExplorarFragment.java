package com.example.paw_match.ui.explorar;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.paw_match.MainActivity;
import com.example.paw_match.R;
import com.example.paw_match.data.model.Mascota;
import com.example.paw_match.data.repository.PawMatchRepository;
import com.example.paw_match.ui.detalle.DetalleMascotaFragment;
import com.example.paw_match.ui.shared.PawMatchViewModel;
import com.example.paw_match.ui.shared.PetImages;

import java.util.List;

/** Pantalla de Explorar con su layout XML. */
public class ExplorarFragment extends Fragment {

    private static final String INDICE_MASCOTA = "indice_mascota";

    private PawMatchViewModel viewModel;
    private int indiceActual = 0;

    public ExplorarFragment() {
        super(R.layout.fragment_explorar);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PawMatchViewModel.class);
        if (savedInstanceState != null) {
            indiceActual = savedInstanceState.getInt(INDICE_MASCOTA);
        }

        viewModel.observarMascotas().observe(getViewLifecycleOwner(),
                ignored -> actualizarVistaMascota(view));

        View.OnClickListener abrirDetalle = v -> abrirDetalleMascota();
        view.findViewById(R.id.card_mascota).setOnClickListener(abrirDetalle);
        view.findViewById(R.id.img_mascota).setOnClickListener(abrirDetalle);
        view.findViewById(R.id.btn_ver_detalles).setOnClickListener(abrirDetalle);

        view.findViewById(R.id.btn_pasar).setOnClickListener(v -> {
            List<Mascota> mascotas = viewModel.obtenerMascotasParaExplorar();
            if (!mascotas.isEmpty()) {
                indiceActual = (indiceActual + 1) % mascotas.size();
                actualizarVistaMascota(view);
            }
        });

        view.findViewById(R.id.btn_me_interesa).setOnClickListener(v -> {
            List<Mascota> mascotas = viewModel.obtenerMascotasParaExplorar();
            if (mascotas.isEmpty()) return;

            Mascota mascota = mascotas.get(indiceActual % mascotas.size());
            PawMatchRepository.Resultado resultado = viewModel.crearSolicitud(mascota.getId());
            int mensaje;
            switch (resultado) {
                case OK:
                    mensaje = R.string.pm_ok;
                    break;
                case DUPLICADA:
                    mensaje = R.string.pm_duplicada;
                    break;
                default:
                    mensaje = R.string.pm_error;
                    break;
            }
            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
            actualizarVistaMascota(view);
        });

        ((MainActivity) requireActivity()).configurarNavegacion(
                view.findViewById(R.id.bottom_navigation), R.id.nav_explorar);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(INDICE_MASCOTA, indiceActual);
        super.onSaveInstanceState(outState);
    }

    private void abrirDetalleMascota() {
        List<Mascota> mascotas = viewModel.obtenerMascotasParaExplorar();
        if (mascotas.isEmpty()) return;

        Mascota mascota = mascotas.get(indiceActual % mascotas.size());
        getParentFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.pm_contenedor, DetalleMascotaFragment.newInstance(mascota.getId()))
                .addToBackStack("detalle")
                .commit();
    }

    private void actualizarVistaMascota(View view) {
        List<Mascota> mascotas = viewModel.obtenerMascotasParaExplorar();
        boolean vacio = mascotas.isEmpty();
        view.findViewById(R.id.card_mascota).setVisibility(vacio ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.layout_botones_accion).setVisibility(vacio ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.tv_explorar_vacio).setVisibility(vacio ? View.VISIBLE : View.GONE);
        if (vacio) {
            indiceActual = 0;
            return;
        }

        indiceActual %= mascotas.size();
        Mascota mascota = mascotas.get(indiceActual);
        TextView nombre = view.findViewById(R.id.tv_nombre_mascota);
        TextView detalles = view.findViewById(R.id.tv_detalles_mascota);
        ImageView imagen = view.findViewById(R.id.img_mascota);

        nombre.setText(mascota.getNombre());
        detalles.setText(getString(R.string.pet_summary, mascota.getEdad(), mascota.getCiudad()));

        PetImages.mostrar(imagen, mascota);
    }
}
