package com.example.paw_match.ui.publisher;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.paw_match.R;

public class AddPetFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String SELECTED_PHOTO = "selected_photo";
    private ImageView imgPetPreview;
    private Uri imageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_pet, container, false);

        imgPetPreview = view.findViewById(R.id.imgPetPreview);
        if (savedInstanceState != null) {
            String savedPhoto = savedInstanceState.getString(SELECTED_PHOTO);
            if (savedPhoto != null) imageUri = Uri.parse(savedPhoto);
        }
        if (imageUri != null) imgPetPreview.setImageURI(imageUri);
        Button btnSelectPhoto = view.findViewById(R.id.btnSelectPhoto);
        EditText etPetName = view.findViewById(R.id.etPetName);
        EditText etPetAge = view.findViewById(R.id.etPetAge);
        EditText etPetDescription = view.findViewById(R.id.etPetDescription);
        Button btnSavePet = view.findViewById(R.id.btnSavePet);

        btnSelectPhoto.setOnClickListener(v -> openGallery());

        btnSavePet.setOnClickListener(v -> {
            String name = etPetName.getText().toString().trim();
            String age = etPetAge.getText().toString().trim();
            String desc = etPetDescription.getText().toString().trim();

            if (name.isEmpty() || age.isEmpty() || desc.isEmpty() || imageUri == null) {
                Toast.makeText(getContext(), R.string.pet_fields_required, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), R.string.pet_published, Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return view;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (imageUri != null) outState.putString(SELECTED_PHOTO, imageUri.toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imgPetPreview.setImageURI(imageUri);
        }
    }
}
