package com.example.paw_match.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paw_match.R;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        TextView tvUserName = view.findViewById(R.id.tvUserName);
        TextView tvUserEmail = view.findViewById(R.id.tvUserEmail);
        TextView tvUserLocation = view.findViewById(R.id.tvUserLocation);
        TextView tvUserBio = view.findViewById(R.id.tvUserBio);
        RecyclerView recyclerAdoptedPets = view.findViewById(R.id.recyclerAdoptedPets);

        // TODO: Cargar datos reales del usuario logueado (Nombre, Email, Ubicación, Biografía)
        tvUserName.setText("Matías Martínez");
        tvUserEmail.setText("matias@ejemplo.com");
        tvUserLocation.setText("📍 Santa Fe, Argentina");
        tvUserBio.setText("Me encantan los perros y gatos. ¡Buscando dar amor a un nuevo amigo!");

        // Configurar RecyclerView para mostrar las mascotas que ya adoptó
        recyclerAdoptedPets.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        // Aquí puedes usar tu adaptador de mascotas para mostrar las adoptadas

        return view;
    }
}