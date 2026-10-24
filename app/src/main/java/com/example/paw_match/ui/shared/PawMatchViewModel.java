package com.example.paw_match.ui.shared;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.paw_match.data.model.*;
import com.example.paw_match.data.repository.PawMatchRepository;
import java.util.List;

/** Todos los fragments piden este ViewModel con requireActivity(). */
public class PawMatchViewModel extends ViewModel {
    private final PawMatchRepository repository = PawMatchRepository.getInstance();
    private final MutableLiveData<List<SolicitudAdopcion>> solicitudes = new MutableLiveData<>();
    private final MutableLiveData<List<Mascota>> mascotas = new MutableLiveData<>();
    private final MutableLiveData<Boolean> modoRefugio = new MutableLiveData<>(false);
    private final MutableLiveData<List<ConversacionChat>> chats = new MutableLiveData<>();
    private UsuarioLocal usuarioActual;
    private final PawMatchRepository.Listener listener = () -> {
        mascotas.setValue(repository.obtenerMascotas());
        solicitudes.setValue(repository.obtenerSolicitudes());
        chats.setValue(repository.obtenerChats(usuarioActual));
    };
    public PawMatchViewModel() {
        repository.addListener(listener);
        listener.onChanged();
    }
    public LiveData<List<SolicitudAdopcion>> observarSolicitudes() { return solicitudes; }
    public LiveData<List<Mascota>> observarMascotas() { return mascotas; }
    public LiveData<Boolean> observarModoRefugio() { return modoRefugio; }
    public LiveData<List<ConversacionChat>> observarChats() { return chats; }
    public UsuarioLocal obtenerUsuarioActual() { return usuarioActual; }
    public void definirUsuarioActual(UsuarioLocal usuario) {
        usuarioActual = usuario;
        repository.actualizarPerfilUsuario(usuario);
        definirModoRefugio(usuario != null && usuario.esPublicador());
        chats.setValue(repository.obtenerChats(usuario));
        solicitudes.setValue(repository.obtenerSolicitudes());
        mascotas.setValue(repository.obtenerMascotas());
    }
    public boolean esModoRefugio() { return Boolean.TRUE.equals(modoRefugio.getValue()); }
    public void definirModoRefugio(boolean refugio) {
        if (refugio != esModoRefugio()) modoRefugio.setValue(refugio);
    }
    public List<SolicitudAdopcion> obtenerSolicitudesVisibles() {
        return repository.obtenerSolicitudesVisibles(usuarioActual);
    }
    public UsuarioLocal obtenerSolicitante(String solicitudId) {
        return repository.obtenerSolicitante(solicitudId, usuarioActual);
    }
    public DetalleMascota obtenerDetalleMascota(String mascotaId, String solicitudId) {
        return repository.obtenerDetalleMascota(mascotaId, solicitudId, usuarioActual);
    }
    public List<Mascota> obtenerMascotas() { return repository.obtenerMascotas(); }
    public List<Mascota> obtenerMascotasPublicadas() {
        return repository.obtenerMascotasPublicadas(usuarioActual);
    }
    public PawMatchRepository.Resultado publicarMascota(String nombre, String edad, String ubicacion,
                                                       String descripcion, String fotoUri) {
        return repository.publicarMascota(nombre, edad, ubicacion, descripcion, fotoUri, usuarioActual);
    }
    public List<Mascota> obtenerMascotasParaExplorar() {
        return repository.obtenerMascotasParaExplorar(usuarioActual);
    }
    public List<HistorialMascota> obtenerHistorialPerfil() {
        return repository.obtenerHistorialPerfil(usuarioActual);
    }
    public HistorialMascota obtenerDetalleHistorial(String mascotaId, String solicitudId) {
        return repository.obtenerDetalleHistorial(mascotaId, solicitudId, usuarioActual);
    }
    public Mascota buscarMascota(String id) { return repository.buscarMascota(id); }
    public Adoptante obtenerAdoptanteDemo() { return repository.obtenerAdoptanteDemo(); }
    public Refugio obtenerRefugioDemo() { return repository.obtenerRefugioDemo(); }
    public PawMatchRepository.Resultado crearSolicitud(String mascotaId) {
        if (esModoRefugio()) return PawMatchRepository.Resultado.DATOS_INVALIDOS;
        return repository.crearSolicitud(mascotaId, obtenerAdoptanteDemo().getId(), usuarioActual);
    }
    public PawMatchRepository.Resultado cancelarSolicitud(String solicitudId) {
        if (esModoRefugio()) return PawMatchRepository.Resultado.NO_AUTORIZADO;
        return repository.cancelarSolicitud(solicitudId, usuarioActual);
    }
    public PawMatchRepository.Resultado resolverSolicitud(String id, EstadoSolicitud estado) {
        if (!esModoRefugio()) return PawMatchRepository.Resultado.DATOS_INVALIDOS;
        return repository.resolverSolicitud(id, estado, usuarioActual);
    }
    public ConversacionChat obtenerChat(String solicitudId) { return repository.obtenerChat(solicitudId, usuarioActual); }
    public List<MensajeChat> obtenerMensajesChat(String solicitudId) {
        return repository.obtenerMensajesChat(solicitudId, usuarioActual);
    }
    public PawMatchRepository.Resultado enviarMensajeChat(String solicitudId, String texto) {
        return repository.enviarMensajeChat(solicitudId, usuarioActual, texto);
    }
    @Override protected void onCleared() { repository.removeListener(listener); }
}
