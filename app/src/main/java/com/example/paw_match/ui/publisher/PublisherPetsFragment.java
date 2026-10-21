package com.example.paw_match.ui.publisher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.paw_match.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PublisherPetsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_publisher_pets, container, false);

        FloatingActionButton fabAddPet = view.findViewById(R.id.fabAddPet);

        fabAddPet.setOnClickListener(v -> {
            // Navegar al formulario de registrar nueva mascota
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new AddPetFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
