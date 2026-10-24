package com.example.paw_match.ui.publisher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paw_match.R;
import com.example.paw_match.data.model.Mascota;
import com.example.paw_match.ui.shared.PetImages;

public class PublisherPetAdapter extends ListAdapter<Mascota, PublisherPetAdapter.PetViewHolder> {
    public interface DetailListener { void onDetail(Mascota mascota); }
    private final DetailListener detailListener;

    public PublisherPetAdapter(DetailListener detailListener) {
        super(new DiffUtil.ItemCallback<Mascota>() {
            @Override public boolean areItemsTheSame(@NonNull Mascota anterior, @NonNull Mascota nueva) {
                return anterior.getId().equals(nueva.getId());
            }
            @Override public boolean areContentsTheSame(@NonNull Mascota anterior, @NonNull Mascota nueva) {
                return anterior == nueva;
            }
        });
        this.detailListener = detailListener;
    }

    @NonNull
    @Override public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PetViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_published_pet, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Mascota mascota = getItem(position);
        holder.nombre.setText(mascota.getNombre());
        holder.detalles.setText(holder.itemView.getContext().getString(R.string.pet_summary,
                mascota.getEdad(), mascota.getCiudad()));
        holder.tituloDescripcion.setText(holder.itemView.getContext().getString(
                R.string.pet_about, mascota.getNombre()));
        holder.descripcion.setText(mascota.getHistoria());
        PetImages.mostrar(holder.foto, mascota);
        holder.foto.setContentDescription(holder.itemView.getContext().getString(
                R.string.profile_history_pet_photo, mascota.getNombre()));
        holder.itemView.setOnClickListener(v -> detailListener.onDetail(mascota));
    }

    static class PetViewHolder extends RecyclerView.ViewHolder {
        final ImageView foto;
        final TextView nombre, detalles, tituloDescripcion, descripcion;

        PetViewHolder(@NonNull View view) {
            super(view);
            foto = view.findViewById(R.id.imgPetItem);
            nombre = view.findViewById(R.id.tvPetNameItem);
            detalles = view.findViewById(R.id.tvPetDetailsItem);
            tituloDescripcion = view.findViewById(R.id.tvPetDescriptionTitle);
            descripcion = view.findViewById(R.id.tvPetDescriptionItem);
        }
    }
}
