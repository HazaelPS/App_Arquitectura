package mx.edu.uttt.appnavegacion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import mx.edu.uttt.appnavegacion.data.preferences.ModoTema

private val EsquemaClaro = lightColorScheme(
    primary = AzulPrimario,
    onPrimary = TextoSobreOscuro,
    secondary = AzulSecundario,
    secondaryContainer = ContenedorSecundarioClaro,
    onSecondaryContainer = SobreContenedorSecundarioClaro,
    background = FondoClaro,
    onBackground = TextoPrincipal,
    surface = TarjetaFondo,
    onSurface = TextoPrincipal,
    surfaceVariant = CabeceraOscura,
    onSurfaceVariant = GrisSecundario
)

private val EsquemaOscuro = darkColorScheme(
    primary = AzulPrimarioOscuro,
    onPrimary = SobreAzulPrimarioOscuro,
    secondary = CianSecundarioOscuro,
    onSecondary = SobreCianSecundarioOscuro,
    secondaryContainer = ContenedorSecundarioOscuro,
    onSecondaryContainer = SobreContenedorSecundarioOscuro,
    background = FondoOscuro,
    onBackground = TextoPrincipalOscuro,
    surface = TarjetaOscura,
    onSurface = TextoPrincipalOscuro,
    surfaceVariant = CabeceraModoOscuro,
    onSurfaceVariant = GrisSecundarioOscuro
)

@Composable
fun usarTemaOscuro(modo: ModoTema): Boolean = when (modo) {
    ModoTema.SISTEMA -> isSystemInDarkTheme()
    ModoTema.CLARO -> false
    ModoTema.OSCURO -> true
}

@Composable
fun AppNavegacionTheme(
    modoTema: ModoTema = ModoTema.SISTEMA,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (usarTemaOscuro(modoTema)) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        content = content
    )
}
