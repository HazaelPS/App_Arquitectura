package mx.edu.uttt.appnavegacion.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import mx.edu.uttt.appnavegacion.data.preferences.ModoTema
import mx.edu.uttt.appnavegacion.data.preferences.PreferenciasApp
import javax.inject.Inject

@HiltViewModel
class TemaViewModel @Inject constructor(
    private val preferencias: PreferenciasApp
) : ViewModel() {
    val modoTema: StateFlow<ModoTema> = preferencias.modoTema

    fun cambiarModo(modo: ModoTema) = preferencias.guardarModo(modo)
}
