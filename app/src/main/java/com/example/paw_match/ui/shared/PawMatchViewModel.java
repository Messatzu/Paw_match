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
    private final MutableLiveData<Boolean> modoRefugio = new MutableLiveData<>(false);
    private final PawMatchRepository.Listener listener = () ->
            solicitudes.setValue(repository.obtenerSolicitudes());
    public PawMatchViewModel() {
        repository.addListener(listener);
        listener.onChanged();
    }
    public LiveData<List<SolicitudAdopcion>> observarSolicitudes() { return solicitudes; }
    public LiveData<Boolean> observarModoRefugio() { return modoRefugio; }
    public boolean esModoRefugio() { return Boolean.TRUE.equals(modoRefugio.getValue()); }
    public void cambiarModo() { modoRefugio.setValue(!esModoRefugio()); }
    public List<Mascota> obtenerMascotas() { return repository.obtenerMascotas(); }
    public Mascota buscarMascota(String id) { return repository.buscarMascota(id); }
    public Adoptante obtenerAdoptanteDemo() { return repository.obtenerAdoptanteDemo(); }
    public Refugio obtenerRefugioDemo() { return repository.obtenerRefugioDemo(); }
    public PawMatchRepository.Resultado crearSolicitud(String mascotaId) {
        if (esModoRefugio()) return PawMatchRepository.Resultado.DATOS_INVALIDOS;
        return repository.crearSolicitud(mascotaId, obtenerAdoptanteDemo().getId());
    }
    public PawMatchRepository.Resultado resolverSolicitud(String id, EstadoSolicitud estado) {
        if (!esModoRefugio()) return PawMatchRepository.Resultado.DATOS_INVALIDOS;
        return repository.resolverSolicitud(id, estado);
    }
    public String obtenerContactoHabilitado(String solicitudId) {
        return repository.obtenerContactoHabilitado(solicitudId, obtenerAdoptanteDemo().getId());
    }
    public void reiniciarDemo() { repository.reiniciarDemo(); }
    @Override protected void onCleared() { repository.removeListener(listener); }
}
