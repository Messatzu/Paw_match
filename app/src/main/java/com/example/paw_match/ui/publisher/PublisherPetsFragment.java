package com.example.paw_match.ui.publisher;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paw_match.R;
import com.example.paw_match.MainActivity;
import com.example.paw_match.data.model.Mascota;
import com.example.paw_match.ui.detalle.DetalleMascotaFragment;
import com.example.paw_match.ui.shared.PawMatchViewModel;
import java.util.List;

public class PublisherPetsFragment extends Fragment {

    public PublisherPetsFragment() { super(R.layout.fragment_publisher_pets); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PawMatchViewModel vm = new ViewModelProvider(requireActivity()).get(PawMatchViewModel.class);
        ((MainActivity) requireActivity()).configurarNavegacion(
                view.findViewById(R.id.bottom_navigation), R.id.nav_explorar);
        PublisherPetAdapter adapter = new PublisherPetAdapter(mascota -> {
            if (getParentFragmentManager().isStateSaved()) return;
            getParentFragmentManager().beginTransaction().setReorderingAllowed(true)
                    .replace(getId(), DetalleMascotaFragment.newInstance(mascota.getId()))
                    .addToBackStack("detalle_publicacion").commit();
        });
        RecyclerView mascotas = view.findViewById(R.id.recyclerPublishedPets);
        mascotas.setLayoutManager(new LinearLayoutManager(requireContext()));
        mascotas.setAdapter(adapter);
        vm.observarMascotas().observe(getViewLifecycleOwner(), ignored -> {
            List<Mascota> publicadas = vm.obtenerMascotasPublicadas();
            adapter.submitList(publicadas);
            mascotas.setVisibility(publicadas.isEmpty() ? View.GONE : View.VISIBLE);
            view.findViewById(R.id.emptyPublishedPets).setVisibility(publicadas.isEmpty() ? View.VISIBLE : View.GONE);
            TextView contador = view.findViewById(R.id.tvPublishedPetsCount);
            contador.setText(getResources().getQuantityString(R.plurals.pet_publications_count,
                    publicadas.size(), publicadas.size()));
        });
        view.findViewById(R.id.fabAddPet).setOnClickListener(v -> {
            if (getParentFragmentManager().isStateSaved()) return;
            getParentFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(getId(), AddPetFragment.class, null)
                    .addToBackStack(null)
                    .commit();
        });

    }
}
