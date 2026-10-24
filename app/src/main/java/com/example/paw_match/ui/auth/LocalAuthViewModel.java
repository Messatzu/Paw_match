package com.example.paw_match.ui.auth;

import android.app.Application;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.paw_match.R;
import com.example.paw_match.data.model.UsuarioLocal;
import com.example.paw_match.data.repository.LocalUserRepository;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/** Comparte la sesión entre pantallas y conserva las operaciones al girar el dispositivo. */
public final class LocalAuthViewModel extends AndroidViewModel {
    public enum Operacion { NINGUNA, AUTENTICACION, CERRAR_SESION, ACTUALIZAR_PERFIL, ACTUALIZAR_FOTO }

    public static final class Estado {
        public final boolean cargando;
        public final boolean inicializado;
        public final UsuarioLocal usuario;
        public final int error;
        public final Operacion operacion;

        private Estado(boolean cargando, boolean inicializado, UsuarioLocal usuario, int error) {
            this(cargando, inicializado, usuario, error, Operacion.NINGUNA);
        }

        private Estado(boolean cargando, boolean inicializado, UsuarioLocal usuario, int error, Operacion operacion) {
            this.cargando = cargando;
            this.inicializado = inicializado;
            this.usuario = usuario;
            this.error = error;
            this.operacion = operacion;
        }
    }

    private final LocalUserRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Estado> estado = new MutableLiveData<>(
            new Estado(true, false, null, 0));

    public LocalAuthViewModel(@NonNull Application application) {
        super(application);
        repository = new LocalUserRepository(application);
        executor.execute(() -> publicar(() -> new Estado(false, true,
                repository.obtenerUsuarioActual(), 0), null));
    }

    public LiveData<Estado> observarEstado() { return estado; }

    public void registrar(String nombre, String email, String password, String ubicacion, String rol) {
        ejecutar(() -> {
            UsuarioLocal usuario = repository.registrar(nombre, email, password, ubicacion, rol);
            return new Estado(false, true, usuario,
                    usuario == null ? R.string.auth_email_exists : 0);
        });
    }

    public void iniciarSesion(String email, String password) {
        ejecutar(() -> {
            UsuarioLocal usuario = repository.iniciarSesion(email, password);
            return new Estado(false, true, usuario,
                    usuario == null ? R.string.auth_invalid_credentials : 0);
        });
    }

    public void cerrarSesion() {
        ejecutar(Operacion.CERRAR_SESION, () -> {
            repository.cerrarSesion();
            return new Estado(false, true, null, 0);
        });
    }

    public void actualizarPerfil(String nombre, String ubicacion, String descripcion) {
        ejecutar(Operacion.ACTUALIZAR_PERFIL, () -> new Estado(false, true,
                repository.actualizarPerfil(nombre, ubicacion, descripcion), 0));
    }

    public void actualizarFotoPerfil(Uri foto) {
        ejecutar(Operacion.ACTUALIZAR_FOTO, () -> new Estado(false, true,
                repository.actualizarFotoPerfil(foto), 0));
    }

    public void limpiarError() {
        Estado actual = estado.getValue();
        if (actual != null && actual.error != 0) {
            estado.setValue(new Estado(actual.cargando, actual.inicializado, actual.usuario, 0, actual.operacion));
        }
    }

    private void ejecutar(Supplier<Estado> operacion) {
        ejecutar(Operacion.AUTENTICACION, operacion);
    }

    private void ejecutar(Operacion tipo, Supplier<Estado> operacion) {
        Estado actual = estado.getValue();
        if (actual == null || actual.cargando) return;
        estado.setValue(new Estado(true, true, actual.usuario, 0, tipo));
        executor.execute(() -> publicar(operacion, actual.usuario,
                tipo == Operacion.ACTUALIZAR_FOTO ? R.string.profile_photo_error : R.string.auth_storage_error));
    }

    private void publicar(Supplier<Estado> operacion, UsuarioLocal usuarioAnterior) {
        publicar(operacion, usuarioAnterior, R.string.auth_storage_error);
    }

    private void publicar(Supplier<Estado> operacion, UsuarioLocal usuarioAnterior, int error) {
        try {
            estado.postValue(operacion.get());
        } catch (RuntimeException exception) {
            Log.e("LocalAuth", "No se pudo completar la operación local", exception);
            estado.postValue(new Estado(false, true, usuarioAnterior, error));
        }
    }

    @Override
    protected void onCleared() {
        executor.execute(repository::close);
        executor.shutdown();
    }
}
