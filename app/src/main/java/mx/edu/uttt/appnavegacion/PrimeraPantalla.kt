package mx.edu.uttt.appnavegacion

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import mx.edu.uttt.appnavegacion.data.preferences.ModoTema
import mx.edu.uttt.appnavegacion.domain.model.Diseno
import mx.edu.uttt.appnavegacion.ui.ArquitecturaViewModel
import mx.edu.uttt.appnavegacion.ui.CarruselPortafolio
import mx.edu.uttt.appnavegacion.ui.CarruselRecomendados
import mx.edu.uttt.appnavegacion.ui.FLUJO_ESTADOS
import mx.edu.uttt.appnavegacion.ui.GALERIA_INSPIRACION
import mx.edu.uttt.appnavegacion.ui.VisorImagen
import mx.edu.uttt.appnavegacion.ui.galeriaInspiracion
import mx.edu.uttt.appnavegacion.ui.PaginaPortafolio
import mx.edu.uttt.appnavegacion.ui.resolverImagenes
import mx.edu.uttt.appnavegacion.ui.rememberImagenesDiseno
import mx.edu.uttt.appnavegacion.ui.theme.TextoSobreOscuro
import mx.edu.uttt.appnavegacion.ui.theme.TextoSubtituloOscuro
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PrimeraPantalla(
    navController: NavController,
    modoTema: ModoTema,
    onCambiarModo: (ModoTema) -> Unit,
    viewModel: ArquitecturaViewModel = hiltViewModel()
) {
    val disenos by viewModel.disenos.collectAsStateWithLifecycle()
    val pedidos by viewModel.pedidos.collectAsStateWithLifecycle()
    val remotePosts by viewModel.remotePosts.collectAsStateWithLifecycle()
    val isLoadingPosts by viewModel.isLoadingPosts.collectAsStateWithLifecycle()

    var busqueda by rememberSaveable { mutableStateOf("") }
    var estiloFiltro by rememberSaveable { mutableStateOf<String?>(null) }
    var seccion by rememberSaveable { mutableIntStateOf(0) }
    var categoriaGaleria by rememberSaveable { mutableStateOf<String?>(null) }
    var fotoGaleriaAbierta by rememberSaveable { mutableStateOf<Int?>(null) }
    var menuTemaAbierto by rememberSaveable { mutableStateOf(false) }
    var idPedidoAEliminar by rememberSaveable { mutableStateOf<Int?>(null) }

    val formatoMoneda = remember {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX"))
    }

    val context = LocalContext.current
    val paginasPortafolio = remember(disenos) {
        listOf(
            PaginaPortafolio(
                recurso = resolverImagenes(context, "catalogo_conjunto").first(),
                titulo = "Vista en Conjunto — Portafolio Residencial",
                subtitulo = "Desliza para conocer cada diseño"
            )
        ) + disenos.map { d ->
            PaginaPortafolio(
                recurso = resolverImagenes(context, d.imagenRecurso).first(),
                titulo = d.nombre,
                subtitulo = "${d.estilo} • desde ${d.areaBaseM2.toInt()} m²",
                disenoId = d.id
            )
        }
    }
    val recomendados = remember(disenos) {
        val estilosFavoritos = disenos.filter { it.favorito }.map { it.estilo }.toSet()
        disenos.filter { !it.favorito }
            .ifEmpty { disenos }
            .sortedByDescending { it.estilo in estilosFavoritos }
    }

    val estilos = remember(disenos) { disenos.map { it.estilo }.distinct() }
    val favoritos = remember(disenos) { disenos.filter { it.favorito } }
    val disenosFiltrados = remember(disenos, busqueda, estiloFiltro) {
        val q = busqueda.trim()
        disenos.filter { d ->
            (estiloFiltro == null || d.estilo == estiloFiltro) &&
                (q.isEmpty() ||
                    d.nombre.contains(q, ignoreCase = true) ||
                    d.estilo.contains(q, ignoreCase = true) ||
                    d.resumenCorto.contains(q, ignoreCase = true))
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Horizontal + WindowInsetsSides.Top
                            )
                        )
                        .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_app),
                            contentDescription = stringResource(id = R.string.desc_logo),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.app_name),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextoSobreOscuro
                            )
                            Text(
                                text = "Diseños, Cotización Regional y Pedidos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextoSubtituloOscuro
                            )
                        }
                        Box {
                            IconButton(onClick = { menuTemaAbierto = true }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Apariencia",
                                    tint = TextoSobreOscuro
                                )
                            }
                            DropdownMenu(
                                expanded = menuTemaAbierto,
                                onDismissRequest = { menuTemaAbierto = false }
                            ) {
                                Text(
                                    text = "Apariencia",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                                ModoTema.entries.forEach { modo ->
                                    DropdownMenuItem(
                                        text = { Text(etiquetaModo(modo)) },
                                        leadingIcon = {
                                            RadioButton(selected = modo == modoTema, onClick = null)
                                        },
                                        onClick = {
                                            onCambiarModo(modo)
                                            menuTemaAbierto = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(id = R.string.lbl_autor),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSubtituloOscuro
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = seccion == 0,
                    onClick = { seccion = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Catálogo") }
                )
                NavigationBarItem(
                    selected = seccion == 3,
                    onClick = { seccion = 3 },
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text("Galería") }
                )
                NavigationBarItem(
                    selected = seccion == 1,
                    onClick = { seccion = 1 },
                    icon = {
                        BadgedBox(badge = { if (favoritos.isNotEmpty()) Badge { Text("${favoritos.size}") } }) {
                            Icon(Icons.Default.Favorite, contentDescription = null)
                        }
                    },
                    label = { Text("Favoritos") }
                )
                NavigationBarItem(
                    selected = seccion == 2,
                    onClick = { seccion = 2 },
                    icon = {
                        BadgedBox(badge = { if (pedidos.isNotEmpty()) Badge { Text("${pedidos.size}") } }) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                        }
                    },
                    label = { Text("Pedidos") }
                )
                NavigationBarItem(
                    selected = seccion == 4,
                    onClick = { seccion = 4 },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Web API") }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (seccion) {
                // ---------------- CATÁLOGO ----------------
                0 -> {
                    item {
                        CarruselPortafolio(
                            paginas = paginasPortafolio,
                            onClickPagina = { pagina ->
                                pagina.disenoId?.let {
                                    navController.navigate(AppScreens.SegundaPantalla.crearRuta(it))
                                }
                            }
                        )
                    }

                    item {
                        CarruselRecomendados(
                            titulo = if (disenos.any { it.favorito }) "Recomendados para ti" else "Te puede interesar",
                            disenos = recomendados,
                            onClickDiseno = {
                                navController.navigate(AppScreens.SegundaPantalla.crearRuta(it.id))
                            }
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = busqueda,
                            onValueChange = { busqueda = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Buscar diseño o estilo") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (busqueda.isNotEmpty()) {
                                    IconButton(onClick = { busqueda = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Borrar búsqueda")
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = estiloFiltro == null,
                                onClick = { estiloFiltro = null },
                                label = { Text("Todos") }
                            )
                            estilos.forEach { estilo ->
                                FilterChip(
                                    selected = estiloFiltro == estilo,
                                    onClick = { estiloFiltro = if (estiloFiltro == estilo) null else estilo },
                                    label = { Text(estilo) }
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Diseños Disponibles (${disenosFiltrados.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (disenos.isEmpty()) {
                        item { Text(text = "No hay datos") }
                    } else if (disenosFiltrados.isEmpty()) {
                        item {
                            EstadoVacio(
                                icono = Icons.Default.Search,
                                titulo = "Sin resultados",
                                mensaje = "Prueba con otra palabra o quita el filtro de estilo."
                            )
                        }
                    }

                    items(disenosFiltrados, key = { it.id }) { diseno ->
                        TarjetaDisenoAmpliada(
                            diseno = diseno,
                            formatoMoneda = formatoMoneda,
                            onPedirOCotizar = {
                                navController.navigate(AppScreens.SegundaPantalla.crearRuta(diseno.id))
                            },
                            onFavorito = { viewModel.cambiarFavorito(diseno) }
                        )
                    }
                }

                // ---------------- FAVORITOS ----------------
                1 -> {
                    item {
                        Text(
                            text = "Mis favoritos (${favoritos.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (favoritos.isEmpty()) {
                        item {
                            EstadoVacio(
                                icono = Icons.Default.FavoriteBorder,
                                titulo = "Aún no tienes favoritos",
                                mensaje = "Toca el corazón de un diseño en el catálogo para guardarlo aquí."
                            )
                        }
                    }
                    items(favoritos, key = { "fav_${it.id}" }) { diseno ->
                        TarjetaDisenoAmpliada(
                            diseno = diseno,
                            formatoMoneda = formatoMoneda,
                            onPedirOCotizar = {
                                navController.navigate(AppScreens.SegundaPantalla.crearRuta(diseno.id))
                            },
                            onFavorito = { viewModel.cambiarFavorito(diseno) }
                        )
                    }
                }

                // ---------------- PEDIDOS ----------------
                2 -> {
                    item {
                        Text(
                            text = "Pedidos y Cotizaciones (${pedidos.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (pedidos.isNotEmpty()) {
                            Text(
                                text = "Suma estimada: ${formatoMoneda.format(pedidos.sumOf { it.costoEstimadoTotal })}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    if (pedidos.isEmpty()) {
                        item {
                            EstadoVacio(
                                icono = Icons.AutoMirrored.Filled.List,
                                titulo = "Sin pedidos todavía",
                                mensaje = "Elige un diseño, cotízalo y guárdalo para verlo aquí."
                            )
                        }
                    }
                    items(pedidos, key = { "ped_${it.id}" }) { pedido ->
                        val siguiente = FLUJO_ESTADOS.getOrNull(FLUJO_ESTADOS.indexOf(pedido.estadoPedido) + 1)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "[${etiquetaEstado(pedido.estadoPedido)}] ${pedido.nombreCliente}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "${pedido.lugar} (${pedido.region}) • ${pedido.areaSolicitadaM2.toInt()} m²",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            text = "Total estimado: ${formatoMoneda.format(pedido.costoEstimadoTotal)}",
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(onClick = { idPedidoAEliminar = pedido.id }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar registro",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                if (siguiente != null) {
                                    TextButton(onClick = { viewModel.avanzarEstado(pedido) }) {
                                        Text("Pasar a ${etiquetaEstado(siguiente)}")
                                    }
                                }
                            }
                        }
                    }
                }

                // ---------------- GALERÍA ----------------
                3 -> galeriaInspiracion(
                    categoriaSeleccionada = categoriaGaleria,
                    onCategoria = { categoriaGaleria = it },
                    onAbrirFoto = { fotoGaleriaAbierta = it }
                )

                // ---------------- WEB API (JSONPlaceholder) ----------------
                4 -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Posts de JSONPlaceholder",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { viewModel.cargarPostsRemotos() },
                                enabled = !isLoadingPosts
                            ) {
                                Text(if (isLoadingPosts) "Cargando..." else "Actualizar")
                            }
                        }
                    }

                    if (isLoadingPosts) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else if (remotePosts.isEmpty()) {
                        item {
                            Text("No se encontraron publicaciones o sin conexión a internet.")
                        }
                    } else {
                        items(remotePosts, key = { it.id }) { post ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${post.id}. ${post.title}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = post.body,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }

    // Visor de foto a pantalla completa (con zoom)
    fotoGaleriaAbierta?.let { indice ->
        val foto = GALERIA_INSPIRACION.getOrNull(indice)
        if (foto != null) {
            VisorImagen(
                recurso = foto.recurso,
                descripcion = foto.titulo,
                onCerrar = { fotoGaleriaAbierta = null }
            )
        } else {
            fotoGaleriaAbierta = null
        }
    }

    // Confirmación antes de borrar
    val pedidoAEliminar = pedidos.find { it.id == idPedidoAEliminar }
    if (pedidoAEliminar != null) {
        AlertDialog(
            onDismissRequest = { idPedidoAEliminar = null },
            title = { Text("Eliminar registro") },
            text = {
                Text("¿Eliminar el registro de ${pedidoAEliminar.nombreCliente}? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarPedido(pedidoAEliminar)
                    idPedidoAEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { idPedidoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

private fun etiquetaModo(modo: ModoTema): String = when (modo) {
    ModoTema.SISTEMA -> "Automático (según el sistema)"
    ModoTema.CLARO -> "Claro"
    ModoTema.OSCURO -> "Oscuro"
}

private fun etiquetaEstado(estado: String): String = when (estado) {
    "COTIZADO" -> "Cotizado"
    "SOLICITADO" -> "Solicitado"
    "EN_PROCESO" -> "En proceso"
    "ENTREGADO" -> "Entregado"
    else -> estado
}

@Composable
private fun TarjetaDisenoAmpliada(
    diseno: Diseno,
    formatoMoneda: NumberFormat,
    onPedirOCotizar: () -> Unit,
    onFavorito: () -> Unit
) {
    val resId = rememberImagenesDiseno(diseno.imagenRecurso).first()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPedirOCotizar() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(165.dp)) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = diseno.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = onFavorito,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (diseno.favorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (diseno.favorito) "Quitar de favoritos" else "Agregar a favoritos",
                        tint = if (diseno.favorito) Color(0xFFFF6B6B) else Color.White
                    )
                }
            }
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = diseno.estilo.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Desde ${formatoMoneda.format(diseno.costoBaseM2)}/m²",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = diseno.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = diseno.resumenCorto.ifBlank { diseno.descripcion },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${diseno.areaBaseM2.toInt()} m² base • ${diseno.niveles} niv. • ${diseno.recamaras} rec. • ${diseno.banos} baños",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onPedirOCotizar, modifier = Modifier.weight(1f)) {
                        Text("Cotizar")
                    }
                    Button(onClick = onPedirOCotizar, modifier = Modifier.weight(1f)) {
                        Text("Pedir Diseño")
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadoVacio(icono: ImageVector, titulo: String, mensaje: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
