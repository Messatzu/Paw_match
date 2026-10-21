package com.example.paw_match.ui.explorar

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.paw_match.R
import com.example.paw_match.ui.detalle.DetalleMascotaFragment
import com.example.paw_match.ui.shared.PawMatchViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Fragment de Explorar que utiliza directamente el layout XML `fragment_explorar.xml`.
 */
class ExplorarFragment : Fragment(R.layout.fragment_explorar) {

    private lateinit var viewModel: PawMatchViewModel
    private var indiceActual = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[PawMatchViewModel::class.java]

        actualizarVistaMascota(view)

        // Botón '+' para ver detalles de la mascota actual
        view.findViewById<View>(R.id.btn_ver_detalles)?.setOnClickListener {
            val mascotas = viewModel.obtenerMascotas()
            if (mascotas.isNotEmpty()) {
                val mascota = mascotas[indiceActual % mascotas.size]
                parentFragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.pm_contenedor, DetalleMascotaFragment.newInstance(mascota.id))
                    .addToBackStack("detalle")
                    .commit()
            }
        }

        // Botón Pasar
        view.findViewById<View>(R.id.btn_pasar)?.setOnClickListener {
            val mascotas = viewModel.obtenerMascotas()
            if (mascotas.isNotEmpty()) {
                indiceActual = (indiceActual + 1) % mascotas.size
                actualizarVistaMascota(view)
            }
        }

        // Botón Me interesa
        view.findViewById<View>(R.id.btn_me_interesa)?.setOnClickListener {
            val mascotas = viewModel.obtenerMascotas()
            if (mascotas.isNotEmpty()) {
                val mascota = mascotas[indiceActual % mascotas.size]
                viewModel.crearSolicitud(mascota.id)
                Toast.makeText(
                    requireContext(),
                    "Interés registrado por ${mascota.nombre}",
                    Toast.LENGTH_SHORT
                ).show()
                indiceActual = (indiceActual + 1) % mascotas.size
                actualizarVistaMascota(view)
            }
        }

        // Bottom Navigation Bar
        view.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explorar -> true
                R.id.nav_solicitudes -> {
                    Toast.makeText(requireContext(), "Sección Solicitudes", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_perfil -> {
                    Toast.makeText(requireContext(), "Sección Perfil", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun actualizarVistaMascota(view: View) {
        val mascotas = viewModel.obtenerMascotas()
        if (mascotas.isEmpty()) return
        val mascota = mascotas[indiceActual % mascotas.size]

        view.findViewById<TextView>(R.id.tv_nombre_mascota)?.text = mascota.nombre
        view.findViewById<TextView>(R.id.tv_detalles_mascota)?.text = "${mascota.edad} • ${mascota.ciudad}"

        // Resolver la imagen drawable de la mascota
        val imagenNombre = mascota.imagenNombre
        val context = requireContext()
        val resId = if (!imagenNombre.isNullOrEmpty()) {
            val id = context.resources.getIdentifier(imagenNombre, "drawable", context.packageName)
            if (id != 0) id else R.drawable.ic_luna_img
        } else {
            R.drawable.ic_luna_img
        }
        view.findViewById<ImageView>(R.id.img_mascota)?.setImageResource(resId)
    }

    companion object {
        fun newInstance(): ExplorarFragment = ExplorarFragment()
    }
}
