package com.example.paw_match;

import android.os.Bundle;
import android.content.res.ColorStateList;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import android.widget.ImageButton;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import com.example.paw_match.ui.auth.LocalAuthViewModel;
import com.example.paw_match.ui.auth.LoginFragment;
import com.example.paw_match.ui.auth.RegisterFragment;
import com.example.paw_match.ui.chat.ChatFragment;
import com.example.paw_match.ui.chat.ChatsFragment;
import com.example.paw_match.ui.explorar.ExplorarFragment;
import com.example.paw_match.ui.profile.ProfileFragment;
import com.example.paw_match.ui.publisher.PublisherPetsFragment;
import com.example.paw_match.ui.publisher.PublisherRequestsFragment;
import com.example.paw_match.ui.shared.PawMatchViewModel;
import com.example.paw_match.ui.theme.ThemeChangeViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private static final String APPEARANCE_PREFERENCES = "pawmatch_appearance";
    private static final String DARK_MODE = "dark_mode";
    private PawMatchViewModel vm;
    private LocalAuthViewModel auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences appearance = getSharedPreferences(APPEARANCE_PREFERENCES, MODE_PRIVATE);
        AppCompatDelegate.setDefaultNightMode(appearance.getBoolean(DARK_MODE, false)
                ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        vm = new ViewModelProvider(this).get(PawMatchViewModel.class);
        auth = new ViewModelProvider(this).get(LocalAuthViewModel.class);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ThemeChangeViewModel themeState = new ViewModelProvider(this).get(ThemeChangeViewModel.class);
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override public void onFragmentResumed(@NonNull FragmentManager manager, @NonNull Fragment fragment) {
                themeState.restaurarFormulario(fragment.getView());
            }
        }, false);
        boolean oscuro = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        ImageButton themeToggle = findViewById(R.id.btnToggleTheme);
        themeToggle.setImageResource(oscuro ? R.drawable.ic_theme_sun : R.drawable.ic_theme_moon);
        themeToggle.setContentDescription(getString(oscuro
                ? R.string.appearance_enable_light : R.string.appearance_enable_dark));
        themeToggle.setTooltipText(themeToggle.getContentDescription());
        themeToggle.setOnClickListener(v -> {
            Fragment actual = getSupportFragmentManager().findFragmentById(R.id.pm_contenedor);
            themeState.conservarFormulario(actual == null ? null : actual.getView());
            themeToggle.setEnabled(false);
            appearance.edit().putBoolean(DARK_MODE, !oscuro).apply();
            AppCompatDelegate.setDefaultNightMode(oscuro
                    ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            // Las barras ya se contemplan en este contenedor; evita agregarlas otra vez en el menú.
            return new WindowInsetsCompat.Builder(insets)
                    .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.NONE)
                    .build();
        });
        auth.observarEstado().observe(this, estado -> {
            findViewById(R.id.authProgress).setVisibility(estado.inicializado ? View.GONE : View.VISIBLE);
            findViewById(R.id.pm_contenedor).setVisibility(estado.inicializado ? View.VISIBLE : View.INVISIBLE);
            if (!estado.inicializado) return;
            vm.definirUsuarioActual(estado.usuario);
            Fragment actual = getSupportFragmentManager().findFragmentById(R.id.pm_contenedor);
            boolean pantallaAuth = actual instanceof LoginFragment || actual instanceof RegisterFragment;
            if (estado.usuario == null && (actual == null || !pantallaAuth)) {
                mostrarRaiz(LoginFragment.class);
            } else if (estado.usuario != null && (actual == null || pantallaAuth)) {
                mostrarRaiz(vm.esModoRefugio() ? PublisherRequestsFragment.class : ExplorarFragment.class);
            }
        });
    }

    public void configurarNavegacion(BottomNavigationView navigation, int selectedItem) {
        navigation.setItemActiveIndicatorColor(ColorStateList.valueOf(getColor(R.color.paw_surface_variant)));
        navigation.getMenu().findItem(R.id.nav_explorar).setTitle(
                vm.esModoRefugio() ? R.string.pm_publications : R.string.pm_explorar);
        navigation.setSelectedItemId(selectedItem);
        navigation.setOnItemSelectedListener(item -> {
            abrirSeccion(item.getItemId());
            return true;
        });
    }

    public void abrirSeccion(int section) {
        LocalAuthViewModel.Estado estado = auth.observarEstado().getValue();
        if (estado == null || estado.usuario == null) return;
        Class<? extends Fragment> destino;
        if (section == R.id.nav_perfil) destino = ProfileFragment.class;
        else if (section == R.id.nav_chats) destino = ChatsFragment.class;
        else if (section == R.id.nav_solicitudes) destino = PublisherRequestsFragment.class;
        else destino = vm.esModoRefugio() ? PublisherPetsFragment.class : ExplorarFragment.class;
        Fragment actual = getSupportFragmentManager().findFragmentById(R.id.pm_contenedor);
        if (actual == null || actual.getClass() != destino) mostrarRaiz(destino);
    }

    public void abrirChat(String solicitudId) {
        if (vm.obtenerChat(solicitudId) == null) {
            Toast.makeText(this, R.string.chat_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        FragmentManager manager = getSupportFragmentManager();
        if (manager.isStateSaved()) return;
        manager.beginTransaction().setReorderingAllowed(true)
                .replace(R.id.pm_contenedor, ChatFragment.newInstance(solicitudId))
                .addToBackStack("chat").commit();
    }

    private void mostrarRaiz(Class<? extends Fragment> destino) {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.isStateSaved()) return;
        getSystemService(InputMethodManager.class).hideSoftInputFromWindow(
                findViewById(R.id.main).getWindowToken(), 0);
        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        manager.beginTransaction().setReorderingAllowed(true)
                .replace(R.id.pm_contenedor, destino, null).commit();
    }
}
