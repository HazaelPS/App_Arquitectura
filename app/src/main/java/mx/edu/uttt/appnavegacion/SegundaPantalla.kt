package mx.edu.uttt.appnavegacion

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import mx.edu.uttt.appnavegacion.ui.ArquitecturaViewModel
import mx.edu.uttt.appnavegacion.ui.CarruselRecomendados
import mx.edu.uttt.appnavegacion.ui.GaleriaDiseno
import mx.edu.uttt.appnavegacion.ui.rememberImagenesDiseno
import mx.edu.uttt.appnavegacion.ui.theme.TextoSobreOscuro
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegundaPantalla(
    navController: NavController,
    disenoId: Int,
    viewModel: ArquitecturaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val disenos by viewModel.disenos.collectAsStateWithLifecycle()
    val mensajeEstado by viewModel.mensajeEstado.collectAsStateWithLifecycle()
    val diseno = disenos.find { it.id == disenoId }
    val snackbarHostState = remember { SnackbarHostState() }

    // rememberSaveable: el estado sobrevive al giro de pantalla (la Activity se recrea)
    var nombreCliente by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var lugar by rememberSaveable { mutableStateOf("Tula de Allende, Hidalgo") }
    var regionSeleccionada by rememberSaveable {
        mutableStateOf(viewModel.regionesDisponibles.first().region)
    }
    // null = el usuario aún no editó el área; se usa el área base del diseño
    var areaEditada by rememberSaveable { mutableStateOf<String?>(null) }
    var acabadoSeleccionado by rememberSaveable { mutableStateOf("ESTANDAR") }
    var notas by rememberSaveable { mutableStateOf("") }
    var menuRegionExpandido by rememberSaveable { mutableStateOf(false) }
    var intentoGuardar by rememberSaveable { mutableStateOf(false) }

    val areaTexto = areaEditada ?: diseno?.areaBaseM2?.toInt()?.toString() ?: "150"
    val formatoMoneda = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX")) }
    val areaNumerica = areaTexto.toDoubleOrNull() ?: 0.0

    val costoEstimado = if (diseno != null && areaNumerica > 0.0) {
        viewModel.calcularCostoAproximado(
            costoBaseM2 = diseno.costoBaseM2,
            areaM2 = areaNumerica,
            region = regionSeleccionada,
            nivelAcabados = acabadoSeleccionado
        )
    } else 0.0

    val nombreValido = nombreCliente.trim().length >= 2
    val lugarValido = lugar.trim().isNotEmpty()
    val areaValida = areaNumerica > 0.0
    val telefonoValido = telefono.isEmpty() || telefono.length == 10
    val formularioValido = nombreValido && lugarValido && areaValida && telefonoValido

    // Mensajes de confirmación o error como Snackbar
    LaunchedEffect(mensajeEstado) {
        mensajeEstado?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = diseno?.nombre ?: "Cotizar y Pedir Diseño",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_flecha_regresar),
                            contentDescription = stringResource(id = R.string.desc_flecha_regresar)
                        )
                    }
                },
                actions = {
                    if (diseno != null) {
                        IconButton(onClick = { viewModel.cambiarFavorito(diseno) }) {
                            Icon(
                                imageVector = if (diseno.favorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (diseno.favorito) "Quitar de favoritos" else "Agregar a favoritos"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = TextoSobreOscuro,
                    navigationIconContentColor = TextoSobreOscuro,
                    actionIconContentColor = TextoSobreOscuro
                )
            )
        }
    ) { paddingValues ->
        if (diseno == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp)
            ) {
                Text("Cargando diseño...")
            }
            return@Scaffold
        }

        val imagenes = rememberImagenesDiseno(diseno.imagenRecurso)

        val factorRegion = viewModel.regionesDisponibles
            .find { it.region == regionSeleccionada }?.factorCosto ?: 1.0
        val factorAcabado = viewModel.factoresAcabado[acabadoSeleccionado] ?: 1.0

        fun guardar(tipo: String) {
            intentoGuardar = true
            if (!formularioValido) return
            viewModel.registrarPedidoOSolicitud(
                diseno = diseno,
                nombreCliente = nombreCliente,
                telefono = telefono,
                lugar = lugar,
                region = regionSeleccionada,
                areaSolicitadaM2 = areaNumerica,
                nivelAcabados = acabadoSeleccionado,
                tipoSolicitud = tipo,
                notas = notas
            )
            // Se limpian los datos personales para no duplicar el registro sin querer
            nombreCliente = ""
            telefono = ""
            notas = ""
            intentoGuardar = false
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GaleriaDiseno(imagenes = imagenes, descripcion = diseno.nombre)

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(onClick = {}, label = { Text(diseno.estilo) })
                AssistChip(onClick = {}, label = { Text("${diseno.areaBaseM2.toInt()} m² base") })
                AssistChip(onClick = {}, label = { Text("${diseno.niveles} niveles") })
                AssistChip(onClick = {}, label = { Text("${diseno.recamaras} recámaras") })
                AssistChip(onClick = {}, label = { Text("${diseno.banos} baños") })
            }

            Text(text = diseno.descripcion, style = MaterialTheme.typography.bodyMedium)

            HorizontalDivider()

            Text(
                text = "Cotización por Lugar y Región",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = nombreCliente,
                onValueChange = { nombreCliente = it },
                label = { Text("Tu nombre completo") },
                isError = intentoGuardar && !nombreValido,
                supportingText = {
                    if (intentoGuardar && !nombreValido) Text("Escribe al menos 2 letras")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = { nuevo -> telefono = nuevo.filter { it.isDigit() }.take(10) },
                label = { Text("Teléfono o WhatsApp (10 dígitos, opcional)") },
                isError = !telefonoValido,
                supportingText = {
                    if (!telefonoValido) Text("Debe tener 10 dígitos (${telefono.length}/10)")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = lugar,
                onValueChange = { lugar = it },
                label = { Text("Lugar / Municipio / Ciudad del terreno") },
                isError = intentoGuardar && !lugarValido,
                supportingText = {
                    if (intentoGuardar && !lugarValido) Text("Indica dónde se construirá")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = menuRegionExpandido,
                onExpandedChange = { menuRegionExpandido = !menuRegionExpandido }
            ) {
                OutlinedTextField(
                    value = regionSeleccionada,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Región") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuRegionExpandido)
                    },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = menuRegionExpandido,
                    onDismissRequest = { menuRegionExpandido = false }
                ) {
                    viewModel.regionesDisponibles.forEach { reg ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(reg.region, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${reg.lugaresEjemplo} (Factor x${reg.factorCosto})",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            onClick = {
                                regionSeleccionada = reg.region
                                menuRegionExpandido = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = areaTexto,
                onValueChange = { nuevo ->
                    val limpio = nuevo.replace(',', '.')
                    if (limpio.matches(Regex("^\\d{0,6}(\\.\\d{0,2})?$"))) areaEditada = limpio
                },
                label = { Text("Metros cuadrados a construir (m²)") },
                isError = intentoGuardar && !areaValida,
                supportingText = {
                    if (intentoGuardar && !areaValida) Text("Debe ser mayor a 0")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("INTERES_SOCIAL", "ESTANDAR", "RESIDENCIAL_PLUS", "LUJO").forEach { nivel ->
                    FilterChip(
                        selected = acabadoSeleccionado == nivel,
                        onClick = { acabadoSeleccionado = nivel },
                        label = { Text(text = etiquetaAcabado(nivel)) }
                    )
                }
            }

            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Notas adicionales (terreno, topografía, tiempos)") },
                modifier = Modifier.fillMaxWidth()
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Cotización Aproximada", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = formatoMoneda.format(costoEstimado),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Región: $regionSeleccionada • Acabados: ${etiquetaAcabado(acabadoSeleccionado)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${formatoMoneda.format(diseno.costoBaseM2)}/m² × $areaTexto m² × región x$factorRegion × acabados x$factorAcabado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { guardar("COTIZACION") },
                    modifier = Modifier.weight(1f)
                ) { Text("Guardar Cotización") }

                Button(
                    onClick = { guardar("PEDIDO_DISENO") },
                    modifier = Modifier.weight(1f)
                ) { Text("Pedir Diseño") }
            }

            TextButton(
                onClick = {
                    val texto = "ArquiStudio MX\n" +
                        "Diseño: ${diseno.nombre}\n" +
                        "Lugar: ${lugar.trim()} ($regionSeleccionada)\n" +
                        "Superficie: $areaTexto m²\n" +
                        "Acabados: ${etiquetaAcabado(acabadoSeleccionado)}\n" +
                        "Cotización aproximada: ${formatoMoneda.format(costoEstimado)}"
                    val envio = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, texto)
                    }
                    context.startActivity(Intent.createChooser(envio, "Compartir cotización"))
                },
                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compartir cotización")
            }

            val otros = disenos
                .filter { it.id != diseno.id }
                .sortedByDescending { it.estilo == diseno.estilo }
            if (otros.isNotEmpty()) {
                HorizontalDivider()
                CarruselRecomendados(
                    titulo = "Otros diseños que te pueden gustar",
                    disenos = otros,
                    onClickDiseno = { navController.navigate(AppScreens.SegundaPantalla.crearRuta(it.id)) }
                )
            }
        }
    }
}

private fun etiquetaAcabado(nivel: String): String = when (nivel) {
    "INTERES_SOCIAL" -> "Básico"
    "ESTANDAR" -> "Estándar"
    "RESIDENCIAL_PLUS" -> "Plus"
    else -> "Lujo"
}
