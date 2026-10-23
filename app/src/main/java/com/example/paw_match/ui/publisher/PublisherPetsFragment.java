package com.example.paw_match.ui.publisher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.paw_match.R;
import com.example.paw_match.MainActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PublisherPetsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_publisher_pets, container, false);

        FloatingActionButton fabAddPet = view.findViewById(R.id.fabAddPet);
        ((MainActivity) requireActivity()).configurarNavegacion(
                view.findViewById(R.id.bottom_navigation), R.id.nav_explorar);

        fabAddPet.setOnClickListener(v -> {
            // Navegar al formulario de registrar nueva mascota
            getParentFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(getId(), AddPetFragment.class, null)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
