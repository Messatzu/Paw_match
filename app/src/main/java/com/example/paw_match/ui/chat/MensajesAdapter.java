package com.example.paw_match.ui.chat;

import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paw_match.R;
import com.example.paw_match.data.model.MensajeChat;
import java.util.Date;

final class MensajesAdapter extends ListAdapter<MensajeChat, MensajesAdapter.Holder> {
    private long usuarioId;

    MensajesAdapter() {
        super(new DiffUtil.ItemCallback<MensajeChat>() {
            @Override public boolean areItemsTheSame(@NonNull MensajeChat a, @NonNull MensajeChat b) {
                return a.getId().equals(b.getId());
            }
            @Override public boolean areContentsTheSame(@NonNull MensajeChat a, @NonNull MensajeChat b) {
                return a.getTexto().equals(b.getTexto()) && a.getEmisorId() == b.getEmisorId()
                        && a.getFecha() == b.getFecha();
            }
        });
    }

    void setUsuarioId(long usuarioId) {
        if (this.usuarioId != usuarioId) {
            this.usuarioId = usuarioId;
            notifyItemRangeChanged(0, getItemCount());
        }
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        MensajeChat mensaje = getItem(position);
        boolean propio = mensaje.getEmisorId() == usuarioId;
        FrameLayout.LayoutParams layout = (FrameLayout.LayoutParams) holder.bubble.getLayoutParams();
        layout.gravity = propio ? Gravity.END : Gravity.START;
        holder.bubble.setLayoutParams(layout);
        holder.bubble.setBackgroundResource(propio ? R.drawable.bg_chat_message_sent : R.drawable.bg_chat_message_received);
        holder.text.setText(mensaje.getTexto());
        holder.time.setText(DateFormat.getTimeFormat(holder.itemView.getContext()).format(new Date(mensaje.getFecha())));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final View bubble;
        final TextView text, time;
        Holder(View view) {
            super(view);
            bubble = view.findViewById(R.id.chatMessageBubble);
            text = view.findViewById(R.id.tvChatMessageText);
            time = view.findViewById(R.id.tvChatMessageTime);
        }
    }
}
