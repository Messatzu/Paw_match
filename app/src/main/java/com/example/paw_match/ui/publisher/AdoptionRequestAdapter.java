package com.example.paw_match.ui.publisher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paw_match.R;
import com.example.paw_match.data.model.Adoptante;
import com.example.paw_match.data.model.EstadoSolicitud;
import com.example.paw_match.data.model.Mascota;
import com.example.paw_match.data.model.SolicitudAdopcion;

public class AdoptionRequestAdapter extends ListAdapter<AdoptionRequestAdapter.Item, AdoptionRequestAdapter.RequestViewHolder> {

    public static final class Item {
        final SolicitudAdopcion solicitud;
        final Mascota mascota;
        final Adoptante adoptante;

        Item(SolicitudAdopcion solicitud, Mascota mascota, Adoptante adoptante) {
            this.solicitud = solicitud;
            this.mascota = mascota;
            this.adoptante = adoptante;
        }
    }

    public interface DecisionListener {
        void onDecision(SolicitudAdopcion solicitud, EstadoSolicitud estado);
    }
    public interface DetailListener { void onDetail(SolicitudAdopcion solicitud); }

    private boolean publicador;
    private final DecisionListener listener;
    private final DetailListener detailListener;

    public AdoptionRequestAdapter(boolean publicador, DecisionListener listener, DetailListener detailListener) {
        super(new DiffUtil.ItemCallback<Item>() {
            @Override public boolean areItemsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
                return oldItem.solicitud.getId().equals(newItem.solicitud.getId());
            }
            @Override public boolean areContentsTheSame(@NonNull Item oldItem, @NonNull Item newItem) {
                return oldItem.solicitud == newItem.solicitud && oldItem.mascota == newItem.mascota
                        && oldItem.adoptante == newItem.adoptante;
            }
        });
        this.publicador = publicador;
        this.listener = listener;
        this.detailListener = detailListener;
    }

    void setPublicador(boolean publicador) {
        if (this.publicador != publicador) {
            this.publicador = publicador;
            notifyItemRangeChanged(0, getItemCount());
        }
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_publisher_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Item item = getItem(position);
        android.content.Context context = holder.itemView.getContext();
        int imageId = context.getResources().getIdentifier(item.mascota.getImagenNombre(),
                "drawable", context.getPackageName());
        holder.imgPet.setScaleType(imageId == 0 ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
        int imagePadding = imageId == 0 ? Math.round(24 * context.getResources().getDisplayMetrics().density) : 0;
        holder.imgPet.setPadding(imagePadding, imagePadding, imagePadding, imagePadding);
        holder.imgPet.setImageResource(imageId == 0 ? R.drawable.ic_paw_logo : imageId);
        holder.imgPet.setContentDescription(context.getString(R.string.pm_request_pet_photo,
                item.mascota.getNombre()));
        holder.tvTitle.setText(item.mascota.getNombre());
        holder.tvPetDetails.setText(context.getString(R.string.pm_request_pet_details,
                item.mascota.getEdad(), item.mascota.getCiudad()));
        holder.tvName.setText(item.adoptante.getNombre());
        holder.tvLocation.setText(context.getString(R.string.pm_request_applicant_location,
                item.adoptante.getLocalidad()));
        holder.tvBio.setText(item.adoptante.getPresentacion());
        holder.tvBio.setVisibility(item.adoptante.getPresentacion().isEmpty() ? View.GONE : View.VISIBLE);
        holder.itemView.setOnClickListener(v -> detailListener.onDetail(item.solicitud));
        holder.imgPet.setOnClickListener(v -> detailListener.onDetail(item.solicitud));
        holder.tvTitle.setOnClickListener(v -> detailListener.onDetail(item.solicitud));
        int status = item.solicitud.getEstado() == EstadoSolicitud.APROBADA ? R.string.pm_aprobada
                : item.solicitud.getEstado() == EstadoSolicitud.RECHAZADA ? R.string.pm_rechazada
                : R.string.pm_pendiente;
        holder.tvStatus.setText(status);
        boolean pendiente = publicador && item.solicitud.getEstado() == EstadoSolicitud.PENDIENTE;
        holder.actions.setVisibility(pendiente ? View.VISIBLE : View.GONE);
        holder.btnAccept.setContentDescription(context.getString(R.string.pm_request_accept_for,
                item.adoptante.getNombre(), item.mascota.getNombre()));
        holder.btnReject.setContentDescription(context.getString(R.string.pm_request_reject_for,
                item.adoptante.getNombre(), item.mascota.getNombre()));
        holder.btnAccept.setOnClickListener(v -> listener.onDecision(item.solicitud, EstadoSolicitud.APROBADA));
        holder.btnReject.setOnClickListener(v -> listener.onDecision(item.solicitud, EstadoSolicitud.RECHAZADA));
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPet;
        TextView tvTitle, tvPetDetails, tvName, tvLocation, tvBio, tvStatus;
        View actions;
        Button btnAccept, btnReject;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPet = itemView.findViewById(R.id.imgRequestPet);
            tvTitle = itemView.findViewById(R.id.tvPetRequestTitle);
            tvName = itemView.findViewById(R.id.tvApplicantName);
            tvPetDetails = itemView.findViewById(R.id.tvPetRequestDetails);
            tvLocation = itemView.findViewById(R.id.tvApplicantLocation);
            tvBio = itemView.findViewById(R.id.tvApplicantBio);
            tvStatus = itemView.findViewById(R.id.tvRequestStatus);
            actions = itemView.findViewById(R.id.requestActions);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
