package com.example.paw_match.ui.explorar

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.paw_match.ui.theme.PawBackgroundCream
import com.example.paw_match.ui.theme.PawBorderLight
import com.example.paw_match.ui.theme.PawTealPrimary
import com.example.paw_match.ui.theme.PawTextDark
import com.example.paw_match.ui.theme.PawTextSecondary
import com.example.paw_match.ui.theme.PawUnselectedGray
import com.example.paw_match.ui.theme.PawWhite
import com.example.paw_match.ui.theme.Paw_matchTheme

/**
 * Componente Principal de la pantalla Explorar según la interfaz de PawMatch.
 */
@Composable
fun ExplorarScreen(
    nombreMascota: String = "Luna",
    detallesMascota: String = "2 años • Santa Fe",
    imagenResId: Int = R.drawable.mascota_luna,
    onVerDetallesClick: () -> Unit = {},
    onPasarClick: () -> Unit = {},
    onMeInteresaClick: () -> Unit = {},
    onTabSelected: (Int) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = PawBackgroundCream,
        bottomBar = {
            PawBottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { index ->
                    selectedTab = index
                    onTabSelected(index)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Top Bar con Logo PawMatch y Título
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_paw_logo),
                        contentDescription = "PawMatch Logo",
                        tint = androidx.compose.ui.graphics.Color.Unspecified,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.pm_app_name),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawTextDark
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(id = R.string.pm_explorar),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawTextDark
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card Principal de la Mascota
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PawWhite),
                border = BorderStroke(1.5.dp, PawBorderLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(270.dp)
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                            .background(PawBorderLight)
                    ) {
                        Image(
                            painter = painterResource(id = imagenResId),
                            contentDescription = nombreMascota,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Fila del Nombre de la mascota con el botón "+" para ver detalles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = nombreMascota,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = PawTextDark
                            )

                            // Botón "+" para abrir la pantalla de detalles
                            IconButton(
                                onClick = onVerDetallesClick,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(PawTealPrimary, CircleShape)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_add_plus),
                                    contentDescription = "Ver Detalles",
                                    tint = PawWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = detallesMascota,
                            fontSize = 15.sp,
                            color = PawTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de Acción (Pasar / Me interesa)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onPasarClick,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, PawBorderLight),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = PawWhite,
                        contentColor = PawTextDark
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.pm_pasar),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onMeInteresaClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PawTealPrimary,
                        contentColor = PawWhite
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.pm_me_interesa),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Barra de Navegación Inferior con 3 destinos: Explorar, Solicitudes, Perfil.
 */
@Composable
fun PawBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val items = listOf(
        Triple(R.string.pm_explorar, R.drawable.ic_nav_explorar, 0),
        Triple(R.string.pm_solicitudes, R.drawable.ic_nav_solicitudes, 1),
        Triple(R.string.pm_perfil, R.drawable.ic_nav_perfil, 2)
    )

    NavigationBar(
        containerColor = PawWhite,
        tonalElevation = 8.dp,
        modifier = Modifier.height(64.dp)
    ) {
        items.forEach { (titleRes, iconRes, index) ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = stringResource(id = titleRes),
                        tint = if (isSelected) PawTealPrimary else PawUnselectedGray,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = titleRes),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) PawTealPrimary else PawUnselectedGray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = PawWhite
                )
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 680)
@Composable
fun ExplorarScreenPreview() {
    Paw_matchTheme {
        ExplorarScreen()
    }
}
