package mx.edu.uttt.appnavegacion

sealed class AppScreens(val route: String) {
    data object PrimeraPantalla : AppScreens("primera_pantalla")
    data object SegundaPantalla : AppScreens("segunda_pantalla/{disenoId}") {
        fun crearRuta(disenoId: Int) = "segunda_pantalla/$disenoId"
    }
}
