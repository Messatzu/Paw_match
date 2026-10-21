package com.example.paw_match.ui.publisher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paw_match.R;
import java.util.List;

public class AdoptionRequestAdapter extends RecyclerView.Adapter<AdoptionRequestAdapter.RequestViewHolder> {

    private List<String> requestList; // Puedes reemplazar esto con tu modelo de solicitudes completo

    public AdoptionRequestAdapter(List<String> requestList) {
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_publisher_requests, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        // Imagen temporal de la mascota solicitada
        holder.imgPet.setImageResource(R.drawable.ic_launcher_background);

        holder.tvTitle.setText("Solicitud para: Luna");
        holder.tvName.setText("Nombre: Carlos Gómez");
        holder.tvEmail.setText("Correo: carlos@ejemplo.com");
        holder.tvLocation.setText("Vive en: Santa Fe");
        holder.tvBio.setText("Bio: Tengo patio grande y trabajo desde casa.");

        // Indicador de si ya adoptó antes
        holder.tvHistory.setText("🐾 Historial: Ya adoptó una mascota anteriormente en la app.");

        holder.btnAccept.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Solicitud Aceptada", Toast.LENGTH_SHORT).show();
            // TODO: Petición a PHP para aprobar solicitud
        });

        holder.btnReject.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Solicitud Rechazada", Toast.LENGTH_SHORT).show();
            // TODO: Petición a PHP para rechazar solicitud
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPet;
        TextView tvTitle, tvName, tvEmail, tvLocation, tvBio, tvHistory;
        Button btnAccept, btnReject;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPet = itemView.findViewById(R.id.imgRequestPet);
            tvTitle = itemView.findViewById(R.id.tvPetRequestTitle);
            tvName = itemView.findViewById(R.id.tvApplicantName);
            tvEmail = itemView.findViewById(R.id.tvApplicantEmail);
            tvLocation = itemView.findViewById(R.id.tvApplicantLocation);
            tvBio = itemView.findViewById(R.id.tvApplicantBio);
            tvHistory = itemView.findViewById(R.id.tvAdoptionHistory);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
