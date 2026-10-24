package com.example.paw_match.ui.profile;

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
import com.example.paw_match.data.model.HistorialMascota;
import com.example.paw_match.data.model.Mascota;
import com.example.paw_match.ui.shared.PetImages;
import java.util.Objects;

public class AdoptedPetAdapter extends ListAdapter<HistorialMascota, AdoptedPetAdapter.ViewHolder> {

    public interface AbrirDetalle { void abrir(HistorialMascota registro); }
    private final AbrirDetalle abrirDetalle;

    public AdoptedPetAdapter(AbrirDetalle abrirDetalle) {
        super(new DiffUtil.ItemCallback<HistorialMascota>() {
            @Override public boolean areItemsTheSame(@NonNull HistorialMascota oldItem, @NonNull HistorialMascota newItem) {
                return oldItem.getMascota().getId().equals(newItem.getMascota().getId())
                        && oldItem.getEstado() == newItem.getEstado()
                        && Objects.equals(oldItem.getSolicitudId(), newItem.getSolicitudId());
            }
            @Override public boolean areContentsTheSame(@NonNull HistorialMascota oldItem, @NonNull HistorialMascota newItem) {
                return oldItem.getMascota() == newItem.getMascota() && oldItem.getEstado() == newItem.getEstado()
                        && oldItem.getAdoptante() == newItem.getAdoptante();
            }
        });
        this.abrirDetalle = abrirDetalle;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_adopted_pet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistorialMascota registro = getItem(position);
        Mascota mascota = registro.getMascota();
        android.content.Context context = holder.itemView.getContext();
        holder.tvName.setText(mascota.getNombre());
        holder.tvDetails.setText(context.getString(R.string.profile_history_pet_details,
                mascota.getEdad(), mascota.getCiudad()));
        boolean adoptada = registro.getEstado() == HistorialMascota.Estado.ADOPTADA;
        holder.tvStatus.setText(adoptada ? R.string.profile_history_adopted : R.string.profile_history_withdrawn);
        holder.tvStatus.setTextColor(context.getColor(adoptada ? R.color.paw_primary : R.color.paw_text_primary));
        PetImages.mostrar(holder.imgPet, mascota);
        holder.imgPet.setContentDescription(context.getString(R.string.profile_history_pet_photo, mascota.getNombre()));
        holder.itemView.setOnClickListener(v -> abrirDetalle.abrir(registro));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPet;
        TextView tvName, tvDetails, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPet = itemView.findViewById(R.id.imgAdoptedPet);
            tvName = itemView.findViewById(R.id.tvAdoptedPetName);
            tvDetails = itemView.findViewById(R.id.tvHistoryPetDetails);
            tvStatus = itemView.findViewById(R.id.tvHistoryStatus);
        }
    }
}
