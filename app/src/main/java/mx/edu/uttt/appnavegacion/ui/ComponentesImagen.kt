package mx.edu.uttt.appnavegacion.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import android.content.Context
import mx.edu.uttt.appnavegacion.domain.model.Diseno
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import mx.edu.uttt.appnavegacion.R

/**
 * Devuelve todas las fotos de un diseño que existan en res/drawable:
 *   diseno_pabellon.jpg (principal), diseno_pabellon_2.jpg, diseno_pabellon_3.jpg ... hasta _6.
 * Para ampliar la galería solo hay que copiar más fotos con ese nombre.
 */
fun resolverImagenes(context: Context, base: String): List<Int> {
    val recursos = context.resources
    val paquete = context.packageName
    val principal = recursos.getIdentifier(base, "drawable", paquete)
        .takeIf { it != 0 } ?: R.drawable.logo_app
    val extras = (2..6).mapNotNull { n ->
        recursos.getIdentifier("${base}_$n", "drawable", paquete).takeIf { it != 0 }
    }
    return listOf(principal) + extras
}

@Composable
fun rememberImagenesDiseno(base: String): List<Int> {
    val context = LocalContext.current
    return remember(base) { resolverImagenes(context, base) }
}

/** Galería deslizable con indicadores; al tocar una foto se abre en pantalla completa con zoom. */
@Composable
fun GaleriaDiseno(
    imagenes: List<Int>,
    descripcion: String,
    modifier: Modifier = Modifier
) {
    var visorAbierto by rememberSaveable { mutableStateOf<Int?>(null) }
    val pagerState = rememberPagerState(pageCount = { imagenes.size })

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(14.dp))
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { pagina ->
            Image(
                painter = painterResource(id = imagenes[pagina]),
                contentDescription = "$descripcion (foto ${pagina + 1} de ${imagenes.size})",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { visorAbierto = pagina }
            )
        }
        if (imagenes.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
            ) {
                repeat(imagenes.size) { i ->
                    Box(
                        modifier = Modifier
                            .padding(3.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == pagerState.currentPage) Color.White
                                else Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }

    visorAbierto?.let { indice ->
        VisorImagen(
            recurso = imagenes[indice.coerceIn(0, imagenes.lastIndex)],
            descripcion = descripcion,
            onCerrar = { visorAbierto = null }
        )
    }
}

@Composable
fun VisorImagen(recurso: Int, descripcion: String, onCerrar: () -> Unit) {
    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var escala by remember { mutableFloatStateOf(1f) }
        var desplazamiento by remember { mutableStateOf(Offset.Zero) }

        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            Image(
                painter = painterResource(id = recurso),
                contentDescription = descripcion,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            escala = (escala * zoom).coerceIn(1f, 4f)
                            desplazamiento = if (escala == 1f) Offset.Zero else desplazamiento + pan
                        }
                    }
                    .graphicsLayer(
                        scaleX = escala,
                        scaleY = escala,
                        translationX = desplazamiento.x,
                        translationY = desplazamiento.y
                    )
            )
            IconButton(
                onClick = onCerrar,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(8.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
            }
        }
    }
}

data class PaginaPortafolio(
    val recurso: Int,
    val titulo: String,
    val subtitulo: String,
    val disenoId: Int? = null
)

/** Galería deslizable de portada: muestra el portafolio completo y lleva al diseño al tocarla. */
@Composable
fun CarruselPortafolio(
    paginas: List<PaginaPortafolio>,
    onClickPagina: (PaginaPortafolio) -> Unit,
    modifier: Modifier = Modifier
) {
    if (paginas.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { paginas.size })
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(14.dp))
    ) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { i ->
            val pagina = paginas[i]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = pagina.disenoId != null) { onClickPagina(pagina) }
            ) {
                Image(
                    painter = painterResource(id = pagina.recurso),
                    contentDescription = pagina.titulo,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 22.dp)
                ) {
                    Text(
                        text = pagina.titulo,
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = pagina.subtitulo,
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
        ) {
            repeat(paginas.size) { i ->
                Box(
                    modifier = Modifier
                        .padding(3.dp)
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(
                            if (i == pagerState.currentPage) Color.White
                            else Color.White.copy(alpha = 0.5f)
                        )
                )
            }
        }
    }
}

/** Carrusel horizontal de recomendaciones. */
@Composable
fun CarruselRecomendados(
    titulo: String,
    disenos: List<Diseno>,
    onClickDiseno: (Diseno) -> Unit,
    modifier: Modifier = Modifier
) {
    if (disenos.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(disenos, key = { it.id }) { otro ->
                val foto = rememberImagenesDiseno(otro.imagenRecurso).first()
                Card(
                    modifier = Modifier
                        .width(220.dp)
                        .clickable { onClickDiseno(otro) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        Image(
                            painter = painterResource(id = foto),
                            contentDescription = otro.nombre,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = otro.nombre,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = otro.estilo,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}
