package com.example.paw_match.ui.detalle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.paw_match.R
import com.example.paw_match.data.model.Mascota
import com.example.paw_match.ui.shared.PawMatchViewModel
import com.example.paw_match.ui.theme.Paw_matchTheme

/**
 * Fragmento que muestra los detalles completos de una mascota seleccionada.
 */
class DetalleMascotaFragment : Fragment() {

    private lateinit var viewModel: PawMatchViewModel
    private var mascotaId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mascotaId = arguments?.getString(ARG_MASCOTA_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[PawMatchViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Paw_matchTheme {
                    val id = mascotaId
                    val mascota: Mascota = if (id != null) {
                        viewModel.buscarMascota(id) ?: viewModel.obtenerMascotas().firstOrNull() ?: Mascota(
                            "m1", "Luna", "Perro", "2 años", "Mediano", "Tranquila y cariñosa",
                            "Luna fue rescatada a los 6 meses. Disfruta los paseos en parques y convive con niños.",
                            "mascota_luna", "refugio-1", "14 kg", "Marzo 2022", "Santa Fe"
                        )
                    } else {
                        viewModel.obtenerMascotas().firstOrNull() ?: Mascota(
                            "m1", "Luna", "Perro", "2 años", "Mediano", "Tranquila y cariñosa",
                            "Luna fue rescatada a los 6 meses. Disfruta los paseos en parques y convive con niños.",
                            "mascota_luna", "refugio-1", "14 kg", "Marzo 2022", "Santa Fe"
                        )
                    }

                    // Resolver imagen drawable
                    val context = requireContext()
                    val imagenNombre = mascota.imagenNombre
                    val imagenResId = if (!imagenNombre.isNullOrEmpty()) {
                        val resId = context.resources.getIdentifier(imagenNombre, "drawable", context.packageName)
                        if (resId != 0) resId else R.drawable.mascota_luna
                    } else {
                        R.drawable.mascota_luna
                    }

                    DetalleMascotaScreen(
                        mascota = mascota,
                        imagenResId = imagenResId,
                        onVolverClick = {
                            requireActivity().supportFragmentManager.popBackStack()
                        },
                        onMeInteresaClick = {
                            viewModel.crearSolicitud(mascota.id)
                            Toast.makeText(
                                requireContext(),
                                "¡Solicitud registrada para adoptar a ${mascota.nombre}!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }
    }

    companion object {
        private const val ARG_MASCOTA_ID = "mascota_id"

        fun newInstance(mascotaId: String): DetalleMascotaFragment {
            val fragment = DetalleMascotaFragment()
            val args = Bundle()
            args.putString(ARG_MASCOTA_ID, mascotaId)
            fragment.arguments = args
            return fragment
        }
    }
}
