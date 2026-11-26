package mx.edu.uttt.appnavegacion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import mx.edu.uttt.appnavegacion.ui.TemaViewModel
import mx.edu.uttt.appnavegacion.ui.theme.AppNavegacionTheme
import mx.edu.uttt.appnavegacion.ui.theme.usarTemaOscuro

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val temaViewModel: TemaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val modoTema by temaViewModel.modoTema.collectAsStateWithLifecycle()
            val oscuro = usarTemaOscuro(modoTema)

            DisposableEffect(oscuro) {
                val transparente = android.graphics.Color.TRANSPARENT
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(transparente),
                    navigationBarStyle = if (oscuro) {
                        SystemBarStyle.dark(transparente)
                    } else {
                        SystemBarStyle.light(transparente, transparente)
                    }
                )
                onDispose { }
            }

            AppNavegacionTheme(modoTema = modoTema) {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = AppScreens.PrimeraPantalla.route
                ) {
                    composable(route = AppScreens.PrimeraPantalla.route) {
                        PrimeraPantalla(
                            navController = navController,
                            modoTema = modoTema,
                            onCambiarModo = temaViewModel::cambiarModo
                        )
                    }
                    composable(
                        route = AppScreens.SegundaPantalla.route,
                        arguments = listOf(navArgument("disenoId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val disenoId = backStackEntry.arguments?.getInt("disenoId") ?: 1
                        SegundaPantalla(
                            navController = navController,
                            disenoId = disenoId
                        )
                    }
                }
            }
        }
    }
}
