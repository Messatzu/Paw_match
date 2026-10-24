package com.example.paw_match.ui.publisher;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.paw_match.MainActivity;
import com.example.paw_match.R;
import com.example.paw_match.data.repository.PawMatchRepository;
import com.example.paw_match.ui.shared.PawMatchViewModel;

public class AddPetFragment extends Fragment {

    private static final String SELECTED_PHOTO = "selected_photo";
    private Uri imageUri;
    private final ActivityResultLauncher<String[]> seleccionarFoto = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                try {
                    requireContext().getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) {
                    // Algunos proveedores solo conceden acceso temporal a la foto.
                }
                imageUri = uri;
                if (getView() != null) mostrarFotoSeleccionada(getView());
            });

    public AddPetFragment() { super(R.layout.fragment_add_pet); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PawMatchViewModel vm = new ViewModelProvider(requireActivity()).get(PawMatchViewModel.class);
        if (savedInstanceState != null) {
            String savedPhoto = savedInstanceState.getString(SELECTED_PHOTO);
            if (savedPhoto != null) imageUri = Uri.parse(savedPhoto);
        }
        mostrarFotoSeleccionada(view);
        Button btnSelectPhoto = view.findViewById(R.id.btnSelectPhoto);
        EditText etPetName = view.findViewById(R.id.etPetName);
        EditText etPetAge = view.findViewById(R.id.etPetAge);
        EditText etPetLocation = view.findViewById(R.id.etPetLocation);
        EditText etPetDescription = view.findViewById(R.id.etPetDescription);
        Button btnSavePet = view.findViewById(R.id.btnSavePet);

        btnSelectPhoto.setOnClickListener(v -> seleccionarFoto.launch(new String[]{"image/*"}));

        btnSavePet.setOnClickListener(v -> {
            String name = etPetName.getText().toString().trim();
            String age = etPetAge.getText().toString().trim();
            String location = etPetLocation.getText().toString().trim();
            String desc = etPetDescription.getText().toString().trim();

            if (name.isEmpty() || age.isEmpty() || location.isEmpty() || desc.isEmpty() || imageUri == null) {
                Toast.makeText(requireContext(), R.string.pet_fields_required, Toast.LENGTH_SHORT).show();
                return;
            }
            btnSavePet.setEnabled(false);
            PawMatchRepository.Resultado resultado = vm.publicarMascota(name, age, location, desc, imageUri.toString());
            if (resultado == PawMatchRepository.Resultado.OK) {
                Toast.makeText(requireContext(), R.string.pet_published, Toast.LENGTH_SHORT).show();
                ((MainActivity) requireActivity()).abrirSeccion(R.id.nav_explorar);
            } else {
                btnSavePet.setEnabled(true);
                Toast.makeText(requireContext(), R.string.pm_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (imageUri != null) outState.putString(SELECTED_PHOTO, imageUri.toString());
        super.onSaveInstanceState(outState);
    }

    private void mostrarFotoSeleccionada(View view) {
        if (imageUri == null) return;
        ImageView preview = view.findViewById(R.id.imgPetPreview);
        try {
            preview.setImageURI(imageUri);
            if (preview.getDrawable() != null) return;
        } catch (SecurityException | IllegalArgumentException ignored) {
            // Permite elegir otra foto cuando el documento anterior ya no es accesible.
        }
        imageUri = null;
        preview.setImageDrawable(null);
        Toast.makeText(requireContext(), R.string.pet_photo_unavailable, Toast.LENGTH_SHORT).show();
    }
}
