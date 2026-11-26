package mx.edu.uttt.appnavegacion.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.edu.uttt.appnavegacion.R

data class FotoGaleria(
    val recurso: Int,
    val titulo: String,
    val categoria: String,
    val descripcion: String,
    val autor: String
)

const val CAT_TORRES = "Torres y volúmenes"
const val CAT_FACHADAS = "Fachadas y texturas"
const val CAT_INTERIORES = "Interiores y espacios"

val CATEGORIAS_GALERIA = listOf(CAT_TORRES, CAT_FACHADAS, CAT_INTERIORES)

/** Fotografías de inspiración (Pexels, uso libre). Para agregar más, súmalas a esta lista. */
val GALERIA_INSPIRACION = listOf(
    FotoGaleria(
        R.drawable.galeria_01, "Balcones en voladizo", CAT_FACHADAS,
        "Balcones de vidrio que salen de la fachada y una gran punta oscura en diagonal. El contraste entre blanco y negro le da ritmo y profundidad al edificio.",
        "David Underland"
    ),
    FotoGaleria(
        R.drawable.galeria_02, "Torre circular al atardecer", CAT_TORRES,
        "Torre redonda con terrazas curvas y una corona de lamas en la azotea. La luz dorada resalta las líneas horizontales de cada nivel.",
        "Rodolfo Acosta"
    ),
    FotoGaleria(
        R.drawable.galeria_03, "Torre de cristal en espiral", CAT_TORRES,
        "Los pisos giran un poco unos sobre otros y forman terrazas curvas. El vidrio refleja el cielo y hace que el edificio se vea ligero.",
        "Pixabay"
    ),
    FotoGaleria(
        R.drawable.galeria_04, "Volumen curvo con aletas verticales", CAT_TORRES,
        "Edificio de formas redondeadas cubierto de aletas blancas y vidrio. Se integra con el jardín y cambia de aspecto según la luz del día.",
        "midlox"
    ),
    FotoGaleria(
        R.drawable.galeria_05, "Gran vestíbulo de nervaduras blancas", CAT_INTERIORES,
        "Un espacio enorme con una estructura de costillas blancas que sube hacia un tragaluz central. Muestra cómo la estructura también puede ser la decoración.",
        "Dylan Spangler"
    ),
    FotoGaleria(
        R.drawable.galeria_06, "Fachada de ladrillo escalonado", CAT_FACHADAS,
        "Balcones de ladrillo desfasados que crean un juego de sombras durante todo el día. Un patrón repetido le da carácter a toda la fachada.",
        "Juan Villarreal"
    ),
    FotoGaleria(
        R.drawable.galeria_07, "Torre torcida en el parque", CAT_TORRES,
        "Torre de vidrio con volúmenes desplazados que parecen girar, rodeada de áreas verdes. La arquitectura moderna convive con el espacio público.",
        "Samy Photography"
    ),
    FotoGaleria(
        R.drawable.galeria_08, "Atrio con forma de cono", CAT_INTERIORES,
        "Un volumen de concreto inclinado domina este vestíbulo de varios niveles. La luz natural dibuja sombras largas sobre el piso.",
        "Muharrem Alper"
    ),
    FotoGaleria(
        R.drawable.galeria_09, "Pórtico de columnas de piedra", CAT_INTERIORES,
        "Corredor cubierto con columnas de piedra y piso de losas que guía la mirada hacia el fondo. Sencillo, sobrio y muy funcional.",
        "Eyüp Çağlar"
    ),
    FotoGaleria(
        R.drawable.galeria_10, "Torre de ladrillo con balcones", CAT_FACHADAS,
        "Torre revestida con piezas de ladrillo en tonos claros y pequeños balcones de herrería. Se ve imponente vista desde abajo.",
        "mxkrv"
    )
)

/** Contenido de la sección "Galería" dentro de un LazyColumn. */
fun LazyListScope.galeriaInspiracion(
    categoriaSeleccionada: String?,
    onCategoria: (String?) -> Unit,
    onAbrirFoto: (Int) -> Unit
) {
    val visibles = GALERIA_INSPIRACION.withIndex().filter {
        categoriaSeleccionada == null || it.value.categoria == categoriaSeleccionada
    }

    item {
        Column {
            Text(
                text = "Galería de inspiración (${visibles.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ideas de fachadas, volúmenes y espacios para inspirar tu proyecto. Toca una foto para verla completa.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    item {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = categoriaSeleccionada == null,
                onClick = { onCategoria(null) },
                label = { Text("Todas") }
            )
            CATEGORIAS_GALERIA.forEach { categoria ->
                FilterChip(
                    selected = categoriaSeleccionada == categoria,
                    onClick = { onCategoria(if (categoriaSeleccionada == categoria) null else categoria) },
                    label = { Text(categoria) }
                )
            }
        }
    }

    items(visibles.size, key = { "gal_${visibles[it].index}" }) { posicion ->
        val (indice, foto) = visibles[posicion]
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAbrirFoto(indice) },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                Image(
                    painter = painterResource(id = foto.recurso),
                    contentDescription = foto.titulo,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = foto.categoria.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = foto.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = foto.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "Foto: ${foto.autor} · Pexels",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
