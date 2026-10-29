package com.example.paw_match.ui.chat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paw_match.MainActivity;
import com.example.paw_match.R;
import com.example.paw_match.data.model.ConversacionChat;
import com.example.paw_match.data.model.MensajeChat;
import com.example.paw_match.data.model.UsuarioLocal;
import com.example.paw_match.data.repository.PawMatchRepository;
import com.example.paw_match.ui.shared.PawMatchViewModel;
import java.util.Collections;
import java.util.List;

public class ChatFragment extends Fragment {
    private static final String SOLICITUD_ID = "solicitud_id";

    public ChatFragment() { super(R.layout.fragment_chat); }

    public static ChatFragment newInstance(String solicitudId) {
        Bundle args = new Bundle();
        args.putString(SOLICITUD_ID, solicitudId);
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String solicitudId = getArguments() == null ? null : getArguments().getString(SOLICITUD_ID);
        PawMatchViewModel vm = new ViewModelProvider(requireActivity()).get(PawMatchViewModel.class);
        RecyclerView recycler = view.findViewById(R.id.recyclerChatMessages);
        LinearLayoutManager layout = new LinearLayoutManager(requireContext());
        layout.setStackFromEnd(true);
        recycler.setLayoutManager(layout);
        MensajesAdapter adapter = new MensajesAdapter();
        recycler.setAdapter(adapter);
        EditText input = view.findViewById(R.id.etChatMessage);
        View send = view.findViewById(R.id.btnSendMessage);
        TextView name = view.findViewById(R.id.tvChatHeaderName);
        TextView pet = view.findViewById(R.id.tvChatHeaderPet);
        ImageView avatar = view.findViewById(R.id.imgChatHeaderAvatar);
        TextView intro = view.findViewById(R.id.tvChatIntro);

        Runnable actualizarEnvio = () -> {
            boolean habilitado = vm.obtenerChat(solicitudId) != null && !input.getText().toString().trim().isEmpty();
            send.setEnabled(habilitado);
            send.setAlpha(habilitado ? 1f : 0.4f);
        };
        Runnable enviar = () -> {
            String texto = input.getText().toString();
            if (texto.trim().isEmpty()) return;
            if (vm.enviarMensajeChat(solicitudId, texto) == PawMatchRepository.Resultado.OK) input.setText("");
            else Toast.makeText(requireContext(), R.string.chat_unavailable, Toast.LENGTH_SHORT).show();
        };
        send.setOnClickListener(v -> enviar.run());
        input.setOnEditorActionListener((v, action, event) -> {
            if (action != EditorInfo.IME_ACTION_SEND) return false;
            enviar.run();
            return true;
        });
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { actualizarEnvio.run(); }
            @Override public void afterTextChanged(Editable s) {}
        });
        view.findViewById(R.id.btnChatBack).setOnClickListener(v -> {
            requireActivity().getSystemService(InputMethodManager.class)
                    .hideSoftInputFromWindow(input.getWindowToken(), 0);
            if (getParentFragmentManager().getBackStackEntryCount() > 0) getParentFragmentManager().popBackStack();
            else ((MainActivity) requireActivity()).abrirSeccion(R.id.nav_chats);
        });

        vm.observarChats().observe(getViewLifecycleOwner(), ignored -> {
            ConversacionChat chat = vm.obtenerChat(solicitudId);
            UsuarioLocal usuario = vm.obtenerUsuarioActual();
            boolean habilitado = chat != null && usuario != null;
            view.findViewById(R.id.chatComposer).setVisibility(habilitado ? View.VISIBLE : View.GONE);
            recycler.setVisibility(habilitado ? View.VISIBLE : View.GONE);
            avatar.setVisibility(habilitado ? View.VISIBLE : View.GONE);
            pet.setVisibility(habilitado ? View.VISIBLE : View.GONE);
            if (!habilitado) {
                name.setText(R.string.chat_title);
                pet.setText("");
                avatar.setImageDrawable(null);
                intro.setText(R.string.chat_unavailable);
                intro.setVisibility(View.VISIBLE);
                adapter.submitList(Collections.emptyList());
            } else {
                name.setText(chat.obtenerInterlocutor(usuario).getNombre());
                pet.setText(getString(R.string.chat_pet_context, chat.getMascota().getNombre()));
                ChatImages.mostrar(avatar, chat.getMascota());
                adapter.setUsuarioId(usuario.getId());
                List<MensajeChat> mensajes = vm.obtenerMensajesChat(solicitudId);
                intro.setText(R.string.chat_start);
                intro.setVisibility(mensajes.isEmpty() ? View.VISIBLE : View.GONE);
                adapter.submitList(mensajes, () -> {
                    if (!mensajes.isEmpty()) recycler.scrollToPosition(mensajes.size() - 1);
                });
            }
            actualizarEnvio.run();
        });

        View host = requireActivity().findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            int teclado = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            v.setPadding(0, 0, 0, Math.max(0, teclado - host.getPaddingBottom()));
            if (adapter.getItemCount() > 0) recycler.post(() -> recycler.scrollToPosition(adapter.getItemCount() - 1));
            return insets;
        });
        ViewCompat.requestApplyInsets(view);
    }
}
