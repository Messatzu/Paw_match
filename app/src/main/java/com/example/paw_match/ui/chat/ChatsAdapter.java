package com.example.paw_match.ui.chat;

import android.text.format.DateFormat;
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
import com.example.paw_match.data.model.ConversacionChat;
import com.example.paw_match.data.model.MensajeChat;
import com.example.paw_match.data.model.UsuarioLocal;
import java.util.Date;
import java.util.Objects;

final class ChatsAdapter extends ListAdapter<ConversacionChat, ChatsAdapter.Holder> {
    interface AbrirChat { void abrir(String solicitudId); }
    private final AbrirChat abrir;
    private UsuarioLocal usuario;

    ChatsAdapter(AbrirChat abrir) {
        super(new DiffUtil.ItemCallback<ConversacionChat>() {
            @Override public boolean areItemsTheSame(@NonNull ConversacionChat a, @NonNull ConversacionChat b) {
                return a.getSolicitudId().equals(b.getSolicitudId());
            }
            @Override public boolean areContentsTheSame(@NonNull ConversacionChat a, @NonNull ConversacionChat b) {
                return Objects.equals(a.getUltimoMensaje() == null ? null : a.getUltimoMensaje().getId(),
                        b.getUltimoMensaje() == null ? null : b.getUltimoMensaje().getId());
            }
        });
        this.abrir = abrir;
    }

    void setUsuario(UsuarioLocal usuario) { this.usuario = usuario; }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_conversation, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        ConversacionChat chat = getItem(position);
        UsuarioLocal otro = chat.obtenerInterlocutor(usuario);
        android.content.Context context = holder.itemView.getContext();
        String nombre = otro == null ? context.getString(R.string.chat_title) : otro.getNombre();
        holder.name.setText(nombre);
        holder.pet.setText(context.getString(R.string.chat_pet_context, chat.getMascota().getNombre()));
        ChatImages.mostrar(holder.avatar, chat.getMascota());
        MensajeChat ultimo = chat.getUltimoMensaje();
        if (ultimo == null) {
            holder.preview.setText(R.string.chat_start);
            holder.time.setVisibility(View.GONE);
        } else {
            holder.preview.setText(usuario != null && ultimo.getEmisorId() == usuario.getId()
                    ? context.getString(R.string.chat_message_from_me, ultimo.getTexto()) : ultimo.getTexto());
            holder.time.setText(DateFormat.getTimeFormat(context).format(new Date(ultimo.getFecha())));
            holder.time.setVisibility(View.VISIBLE);
        }
        holder.itemView.setContentDescription(context.getString(R.string.chat_conversation_description,
                nombre, chat.getMascota().getNombre()));
        holder.itemView.setOnClickListener(v -> abrir.abrir(chat.getSolicitudId()));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ImageView avatar;
        final TextView name, pet, preview, time;
        Holder(View view) {
            super(view);
            avatar = view.findViewById(R.id.imgChatAvatar);
            name = view.findViewById(R.id.tvChatName);
            pet = view.findViewById(R.id.tvChatPet);
            preview = view.findViewById(R.id.tvChatLastMessage);
            time = view.findViewById(R.id.tvChatLastTime);
        }
    }
}
