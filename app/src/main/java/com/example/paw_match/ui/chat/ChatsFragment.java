package com.example.paw_match.ui.chat;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.paw_match.MainActivity;
import com.example.paw_match.R;
import com.example.paw_match.ui.shared.PawMatchViewModel;

public class ChatsFragment extends Fragment {
    public ChatsFragment() { super(R.layout.fragment_chats); }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PawMatchViewModel vm = new ViewModelProvider(requireActivity()).get(PawMatchViewModel.class);
        ChatsAdapter adapter = new ChatsAdapter(id -> ((MainActivity) requireActivity()).abrirChat(id));
        RecyclerView recycler = view.findViewById(R.id.recyclerChats);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);
        ((MainActivity) requireActivity()).configurarNavegacion(
                view.findViewById(R.id.bottom_navigation), R.id.nav_chats);
        vm.observarChats().observe(getViewLifecycleOwner(), chats -> {
            adapter.setUsuario(vm.obtenerUsuarioActual());
            adapter.submitList(chats);
            boolean vacio = chats == null || chats.isEmpty();
            recycler.setVisibility(vacio ? View.GONE : View.VISIBLE);
            view.findViewById(R.id.emptyChats).setVisibility(vacio ? View.VISIBLE : View.GONE);
        });
    }
}
