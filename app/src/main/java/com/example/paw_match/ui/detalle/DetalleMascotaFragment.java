package com.example.paw_match.ui.detalle;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.paw_match.MainActivity;
import com.example.paw_match.R;
import com.example.paw_match.data.model.DetalleMascota;
import com.example.paw_match.data.model.EstadoSolicitud;
import com.example.paw_match.data.model.Mascota;
import com.example.paw_match.data.model.MensajeChat;
import com.example.paw_match.data.model.UsuarioLocal;
import com.example.paw_match.data.repository.PawMatchRepository;
import com.example.paw_match.ui.shared.PawMatchViewModel;

/** La misma ficha acompaña a la mascota desde Explorar hasta el historial. */
public class DetalleMascotaFragment extends Fragment {
    private static final String MASCOTA_ID = "mascota_id";
    private static final String SOLICITUD_ID = "solicitud_id";
    private static final String ORIGEN = "origen";

    public DetalleMascotaFragment() { super(R.layout.fragment_history_detail); }

    public static DetalleMascotaFragment newInstance(String mascotaId) {
        return crear(mascotaId, null, R.id.nav_explorar);
    }
    public static DetalleMascotaFragment desdeSolicitud(String mascotaId, String solicitudId) {
        return crear(mascotaId, solicitudId, R.id.nav_solicitudes);
    }
    public static DetalleMascotaFragment desdeHistorial(String mascotaId, String solicitudId) {
        return crear(mascotaId, solicitudId, R.id.nav_perfil);
    }
    private static DetalleMascotaFragment crear(String mascotaId, String solicitudId, int origen) {
        Bundle args = new Bundle();
        args.putString(MASCOTA_ID, mascotaId);
        args.putString(SOLICITUD_ID, solicitudId);
        args.putInt(ORIGEN, origen);
        DetalleMascotaFragment fragment = new DetalleMascotaFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments() == null ? new Bundle() : getArguments();
        String mascotaId = args.getString(MASCOTA_ID);
        String solicitudId = args.getString(SOLICITUD_ID);
        int origen = args.getInt(ORIGEN, R.id.nav_explorar);
        PawMatchViewModel vm = new ViewModelProvider(requireActivity()).get(PawMatchViewModel.class);
        MainActivity activity = (MainActivity) requireActivity();
        activity.configurarNavegacion(view.findViewById(R.id.bottom_navigation), origen);
        view.findViewById(R.id.btnHistoryBack).setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0)
                getParentFragmentManager().popBackStack();
            else activity.abrirSeccion(origen);
        });
        view.findViewById(R.id.btnPetDetailRequest).setOnClickListener(v -> {
            PawMatchRepository.Resultado resultado = vm.crearSolicitud(mascotaId);
            DetalleMascota detalle = vm.obtenerDetalleMascota(mascotaId, solicitudId);
            String mensaje = resultado == PawMatchRepository.Resultado.OK && detalle != null
                    ? getString(R.string.pet_adoption_requested, detalle.getMascota().getNombre())
                    : getString(resultado == PawMatchRepository.Resultado.DUPLICADA ? R.string.pm_duplicada : R.string.pm_error);
            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
        });
        view.findViewById(R.id.btnHistoryOpenChat).setOnClickListener(v -> {
            DetalleMascota actual = vm.obtenerDetalleMascota(mascotaId, solicitudId);
            if (actual != null && actual.getChat() != null) activity.abrirChat(actual.getChat().getSolicitudId());
        });
        view.findViewById(R.id.btnPetDetailAccept).setOnClickListener(v ->
                responder(vm, mascotaId, solicitudId, EstadoSolicitud.APROBADA));
        view.findViewById(R.id.btnPetDetailReject).setOnClickListener(v ->
                responder(vm, mascotaId, solicitudId, EstadoSolicitud.RECHAZADA));

        vm.observarSolicitudes().observe(getViewLifecycleOwner(), ignored -> {
            DetalleMascota detalle = vm.obtenerDetalleMascota(mascotaId, solicitudId);
            if (origen == R.id.nav_perfil && vm.obtenerDetalleHistorial(mascotaId, solicitudId) == null)
                detalle = null;
            visible(view, R.id.historyDetailContent, detalle != null);
            visible(view, R.id.tvHistoryDetailUnavailable, detalle == null);
            if (detalle != null) mostrar(view, detalle, vm.obtenerUsuarioActual());
        });
    }

    private void responder(PawMatchViewModel vm, String mascotaId, String solicitudId, EstadoSolicitud decision) {
        DetalleMascota detalle = vm.obtenerDetalleMascota(mascotaId, solicitudId);
        if (detalle == null || !detalle.puedeResponder() || detalle.getSolicitud() == null) return;
        PawMatchRepository.Resultado resultado = vm.resolverSolicitud(detalle.getSolicitud().getId(), decision);
        Toast.makeText(requireContext(), resultado == PawMatchRepository.Resultado.OK
                ? decision == EstadoSolicitud.APROBADA ? R.string.pm_request_accepted : R.string.pm_request_rejected
                : R.string.pm_error, Toast.LENGTH_SHORT).show();
    }

    private void mostrar(View view, DetalleMascota detalle, UsuarioLocal usuario) {
        Mascota mascota = detalle.getMascota();
        boolean aprobada = detalle.getEstado() == DetalleMascota.Estado.APROBADA;
        boolean baja = detalle.getEstado() == DetalleMascota.Estado.DADA_DE_BAJA;
        text(view, R.id.tvHistoryDetailTitle, aprobada ? getString(R.string.history_detail_adoption_title)
                : baja ? getString(R.string.history_detail_publication_title)
                : getString(R.string.pet_detail_title, mascota.getNombre()));
        text(view, R.id.tvHistoryPetName, mascota.getNombre());
        text(view, R.id.tvHistoryPetSummary, getString(R.string.history_detail_pet_summary,
                mascota.getEspecie(), mascota.getEdad(), mascota.getTamano(), mascota.getCiudad()));
        text(view, R.id.tvHistoryPetBirth, getString(R.string.history_detail_labeled_value,
                getString(R.string.pet_birth), mascota.getNacimiento()));
        text(view, R.id.tvHistoryPetWeight, getString(R.string.history_detail_labeled_value,
                getString(R.string.pet_weight), mascota.getPeso()));
        text(view, R.id.tvPetDetailAge, getString(R.string.history_detail_labeled_value,
                getString(R.string.pet_age), mascota.getEdad()));
        text(view, R.id.tvPetDetailCity, getString(R.string.history_detail_labeled_value,
                getString(R.string.pet_city), mascota.getCiudad()));
        text(view, R.id.tvPetDetailAbout, getString(R.string.pet_about, mascota.getNombre()));
        text(view, R.id.btnPetDetailRequest, getString(R.string.pet_adopt_action, mascota.getNombre()));
        text(view, R.id.tvHistoryPetTemperament, mascota.getTemperamento());
        text(view, R.id.tvHistoryPetStory, mascota.getHistoria());
        ImageView photo = view.findViewById(R.id.imgHistoryPet);
        int imageId = getResources().getIdentifier(mascota.getImagenNombre(), "drawable", requireContext().getPackageName());
        photo.setImageResource(imageId == 0 ? R.drawable.ic_paw_logo : imageId);
        photo.setScaleType(imageId == 0 ? ImageView.ScaleType.FIT_CENTER : ImageView.ScaleType.CENTER_CROP);
        photo.setContentDescription(getString(R.string.profile_history_pet_photo, mascota.getNombre()));

        int estado;
        int explicacion;
        switch (detalle.getEstado()) {
            case PENDIENTE:
                estado = usuario.esPublicador() ? R.string.pm_pendiente : R.string.pet_detail_request_sent;
                explicacion = usuario.esPublicador() ? R.string.pet_detail_pending_publisher : R.string.pet_detail_pending_adopter;
                break;
            case APROBADA:
                estado = R.string.profile_history_adopted;
                explicacion = usuario.esPublicador() ? R.string.pet_detail_accepted_publisher : R.string.pet_detail_accepted_adopter;
                break;
            case RECHAZADA:
                estado = R.string.pm_rechazada;
                explicacion = R.string.pet_detail_rejected;
                break;
            case ADOPTADA:
                estado = R.string.profile_history_adopted;
                explicacion = R.string.pet_detail_already_adopted;
                break;
            case DADA_DE_BAJA:
                estado = R.string.profile_history_withdrawn;
                explicacion = R.string.history_detail_no_adopter;
                break;
            default:
                estado = R.string.pet_detail_available;
                explicacion = R.string.pet_detail_available_body;
        }
        text(view, R.id.tvHistoryDetailStatus, getString(estado));
        text(view, R.id.tvPetDetailStateBody, getString(explicacion));
        ((TextView) view.findViewById(R.id.tvHistoryDetailStatus)).setTextColor(requireContext().getColor(
                detalle.getEstado() == DetalleMascota.Estado.RECHAZADA ? R.color.paw_text_error : R.color.paw_primary));
        visible(view, R.id.btnPetDetailRequest, detalle.puedeSolicitar());
        visible(view, R.id.petDetailDecisionActions, detalle.puedeResponder());

        UsuarioLocal publicador = detalle.getPublicador();
        text(view, R.id.tvPetPublisherName, publicador == null ? detalle.getRefugio().getNombre() : publicador.getNombre());
        text(view, R.id.tvPetPublisherContact, publicador == null ? detalle.getRefugio().getContacto() : publicador.getEmail());
        visible(view, R.id.tvPetPublisherLocation, publicador != null);
        if (publicador != null) text(view, R.id.tvPetPublisherLocation, publicador.getUbicacion());

        UsuarioLocal solicitante = detalle.getSolicitante();
        visible(view, R.id.petDetailAdopterCard, detalle.getSolicitud() != null || baja);
        text(view, R.id.tvPetDetailApplicantTitle, getString(aprobada || baja
                ? R.string.history_detail_adopter_title : R.string.pm_request_applicant));
        visible(view, R.id.historyAdopterInfo, solicitante != null && !baja);
        visible(view, R.id.tvHistoryNoAdopter, solicitante == null || baja);
        text(view, R.id.tvHistoryNoAdopter, getString(baja
                ? R.string.history_detail_no_adopter : R.string.history_detail_adopter_unavailable));
        if (solicitante != null) {
            text(view, R.id.tvHistoryAdopterName, solicitante.getNombre());
            text(view, R.id.tvHistoryAdopterEmail, solicitante.getEmail());
            text(view, R.id.tvHistoryAdopterLocation, solicitante.getUbicacion());
        }
        visible(view, R.id.petDetailChatCard, aprobada || baja);
        visible(view, R.id.btnHistoryOpenChat, detalle.getChat() != null);
        if (detalle.getChat() == null) {
            text(view, R.id.tvHistoryChatPreview, getString(baja ? R.string.history_detail_no_chat : R.string.chat_unavailable));
        } else {
            MensajeChat ultimo = detalle.getChat().getUltimoMensaje();
            String preview = ultimo == null ? getString(R.string.chat_start)
                    : ultimo.getEmisorId() == usuario.getId()
                    ? getString(R.string.chat_message_from_me, ultimo.getTexto()) : ultimo.getTexto();
            text(view, R.id.tvHistoryChatPreview, preview);
        }
    }

    private static void text(View view, int id, String value) { ((TextView) view.findViewById(id)).setText(value); }
    private static void visible(View view, int id, boolean show) { view.findViewById(id).setVisibility(show ? View.VISIBLE : View.GONE); }
}
