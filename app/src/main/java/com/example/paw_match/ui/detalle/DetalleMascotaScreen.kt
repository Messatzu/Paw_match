package com.example.paw_match.ui.detalle

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paw_match.R
import com.example.paw_match.data.model.Mascota
import com.example.paw_match.ui.theme.PawBackgroundCream
import com.example.paw_match.ui.theme.PawBorderLight
import com.example.paw_match.ui.theme.PawTealPrimary
import com.example.paw_match.ui.theme.PawTextDark
import com.example.paw_match.ui.theme.PawTextSecondary
import com.example.paw_match.ui.theme.PawWhite
import com.example.paw_match.ui.theme.Paw_matchTheme

/**
 * Pantalla de Detalle Completo de una Mascota.
 */
@Composable
fun DetalleMascotaScreen(
    mascota: Mascota = Mascota(
        "m1", "Luna", "Perro", "2 años", "Mediano", "Tranquila y cariñosa",
        "Luna fue rescatada a los 6 meses. Disfruta enormemente los paseos en parques y convive alegremente con niños.",
        "mascota_luna", "refugio-1", "14 kg", "Marzo 2022", "Santa Fe"
    ),
    imagenResId: Int = R.drawable.mascota_luna,
    onVolverClick: () -> Unit = {},
    onMeInteresaClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = PawBackgroundCream,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onVolverClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(PawWhite, CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = "Volver",
                        tint = PawTealPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Detalle de ${mascota.nombre}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawTextDark
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PawWhite)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = onMeInteresaClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PawTealPrimary,
                        contentColor = PawWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "Me interesa adoptar a ${mascota.nombre}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Imagen Principal con Bordes Redondeados
            Card(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, PawBorderLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Image(
                    painter = painterResource(id = imagenResId),
                    contentDescription = mascota.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nombre y Especie
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = mascota.nombre,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawTextDark
                    )
                    Text(
                        text = "${mascota.especie} • ${mascota.tamano}",
                        fontSize = 16.sp,
                        color = PawTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Grid / Fichas de Métricas (Nacimiento, Ciudad, Peso, Tamaño)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FichaMetrica(
                    titulo = "Nacimiento",
                    valor = mascota.nacimiento,
                    modifier = Modifier.weight(1f)
                )
                FichaMetrica(
                    titulo = "Ciudad",
                    valor = mascota.ciudad,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FichaMetrica(
                    titulo = "Peso",
                    valor = mascota.peso,
                    modifier = Modifier.weight(1f)
                )
                FichaMetrica(
                    titulo = "Edad",
                    valor = mascota.edad,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sección: Temperamento
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PawWhite),
                border = BorderStroke(1.dp, PawBorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Temperamento",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawTextDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mascota.temperamento,
                        fontSize = 15.sp,
                        color = PawTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sección: Historia / Biografía
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PawWhite),
                border = BorderStroke(1.dp, PawBorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sobre ${mascota.nombre}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawTextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = mascota.historia,
                        fontSize = 15.sp,
                        color = PawTextSecondary,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Ficha individual de métrica (Nacimiento, Ciudad, Peso, etc.)
 */
@Composable
private fun FichaMetrica(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PawWhite),
        border = BorderStroke(1.dp, PawBorderLight),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = titulo,
                fontSize = 13.sp,
                color = PawTextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = valor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PawTextDark
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun DetalleMascotaScreenPreview() {
    Paw_matchTheme {
        DetalleMascotaScreen()
    }
}
