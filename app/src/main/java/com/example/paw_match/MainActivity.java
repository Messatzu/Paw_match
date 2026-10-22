package com.example.paw_match;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.paw_match.ui.demo.DemoFragment;
import com.example.paw_match.ui.explorar.ExplorarFragment;
import com.example.paw_match.ui.shared.PawMatchViewModel;

public class MainActivity extends AppCompatActivity {
    private PawMatchViewModel vm;
    private boolean restaurando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        vm = new ViewModelProvider(this).get(PawMatchViewModel.class);
        if (savedInstanceState != null && savedInstanceState.getBoolean("pm_refugio") != vm.esModoRefugio())
            vm.cambiarModo();
        restaurando = savedInstanceState != null;
        findViewById(R.id.pm_cambiar_modo).setOnClickListener(v -> vm.cambiarModo());
        findViewById(R.id.pm_explorar).setOnClickListener(v -> abrir(DemoFragment.EXPLORAR));
        findViewById(R.id.pm_solicitudes).setOnClickListener(v -> abrir(DemoFragment.SOLICITUDES));
        findViewById(R.id.pm_reiniciar).setOnClickListener(v -> vm.reiniciarDemo());
        vm.observarModoRefugio().observe(this, refugio -> {
            boolean activo = Boolean.TRUE.equals(refugio);
            ((TextView) findViewById(R.id.pm_modo)).setText(activo ? R.string.pm_modo_refugio : R.string.pm_modo_adoptante);
            findViewById(R.id.pm_explorar).setVisibility(activo ? View.GONE : View.VISIBLE);
            findViewById(R.id.pm_solicitudes).setVisibility(activo ? View.GONE : View.VISIBLE);
            if (!restaurando) abrir(activo ? DemoFragment.REFUGIO : DemoFragment.EXPLORAR);
            restaurando = false;
        });
    }

    private void abrir(String destino) {
        if (getSupportFragmentManager().isStateSaved()) return;
        Fragment fragment = DemoFragment.EXPLORAR.equals(destino)
                ? ExplorarFragment.Companion.newInstance()
                : DemoFragment.nueva(destino);
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                .replace(R.id.pm_contenedor, fragment).commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("pm_refugio", vm.esModoRefugio());
        super.onSaveInstanceState(outState);
    }
}
