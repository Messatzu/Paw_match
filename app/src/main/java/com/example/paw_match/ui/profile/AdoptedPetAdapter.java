package com.example.paw_match.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paw_match.R;
import com.example.paw_match.data.model.Mascota;
import java.util.List;

public class AdoptedPetAdapter extends RecyclerView.Adapter<AdoptedPetAdapter.ViewHolder> {

    private List<Mascota> adoptedList;

    public AdoptedPetAdapter(List<Mascota> adoptedList) {
        this.adoptedList = adoptedList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_adopted_pet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mascota mascota = adoptedList.get(position);
        holder.tvName.setText(mascota.getNombre());
        // Imagen por defecto temporal o cargando su foto
        holder.imgPet.setImageResource(R.drawable.ic_launcher_background);
    }

    @Override
    public int getItemCount() {
        return adoptedList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPet;
        TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPet = itemView.findViewById(R.id.imgAdoptedPet);
            tvName = itemView.findViewById(R.id.tvAdoptedPetName);
        }
    }
}
