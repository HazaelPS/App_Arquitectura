package mx.edu.uttt.appnavegacion.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.edu.uttt.appnavegacion.data.repository.WebRepository
import mx.edu.uttt.appnavegacion.domain.model.Diseno
import mx.edu.uttt.appnavegacion.domain.model.PedidoDiseno
import mx.edu.uttt.appnavegacion.domain.model.PostModel
import mx.edu.uttt.appnavegacion.domain.repository.DisenoRepository
import mx.edu.uttt.appnavegacion.domain.repository.PedidoDisenoRepository
import javax.inject.Inject

val FLUJO_ESTADOS = listOf("COTIZADO", "SOLICITADO", "EN_PROCESO", "ENTREGADO")

data class RegionTarifa(
    val region: String,
    val lugaresEjemplo: String,
    val factorCosto: Double
)

@HiltViewModel
class ArquitecturaViewModel @Inject constructor(
    private val disenoRepository: DisenoRepository,
    private val pedidoRepository: PedidoDisenoRepository,
    private val webRepository: WebRepository
) : ViewModel() {

    val regionesDisponibles: List<RegionTarifa> = listOf(
        RegionTarifa("Centro (Hidalgo / EdoMéx)", "Tula de Allende, Pachuca, Tepeji", 1.00),
        RegionTarifa("Zona Metropolitana CDMX", "CDMX, Naucalpan, Huixquilucan", 1.28),
        RegionTarifa("Bajío Industrial", "Querétaro, León, San Luis Potosí", 1.12),
        RegionTarifa("Norte", "Monterrey, Saltillo, Chihuahua", 1.22),
        RegionTarifa("Sur - Sureste / Costa", "Mérida, Cancún, Oaxaca", 1.18)
    )

    val factoresAcabado: Map<String, Double> = mapOf(
        "INTERES_SOCIAL" to 0.85,
        "ESTANDAR" to 1.00,
        "RESIDENCIAL_PLUS" to 1.25,
        "LUJO" to 1.55
    )

    val disenos: StateFlow<List<Diseno>> = disenoRepository.obtenerDisenos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val pedidos: StateFlow<List<PedidoDiseno>> = pedidoRepository.obtenerTodosLosPedidos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _remotePosts = MutableStateFlow<List<PostModel>>(emptyList())
    val remotePosts: StateFlow<List<PostModel>> = _remotePosts.asStateFlow()

    private val _isLoadingPosts = MutableStateFlow(false)
    val isLoadingPosts: StateFlow<Boolean> = _isLoadingPosts.asStateFlow()

    private val _mensajeEstado = MutableStateFlow<String?>(null)
    val mensajeEstado: StateFlow<String?> = _mensajeEstado.asStateFlow()

    init {
        viewModelScope.launch {
            disenoRepository.precargarCatalogoSiVacio()
            cargarPostsRemotos()
        }
    }

    fun cargarPostsRemotos() {
        viewModelScope.launch {
            _isLoadingPosts.value = true
            try {
                val posts = webRepository.getPosts()
                _remotePosts.value = posts
                _mensajeEstado.value = "¡${posts.size} artículos cargados desde JSONPlaceholder!"
            } catch (e: Exception) {
                _mensajeEstado.value = "Error al consumir API: ${e.message}"
            } finally {
                _isLoadingPosts.value = false
            }
        }
    }

    fun calcularCostoAproximado(
        costoBaseM2: Double,
        areaM2: Double,
        region: String,
        nivelAcabados: String
    ): Double {
        val factorReg = regionesDisponibles.find { it.region == region }?.factorCosto ?: 1.0
        val factorAcab = factoresAcabado[nivelAcabados] ?: 1.0
        return costoBaseM2 * areaM2 * factorReg * factorAcab
    }

    fun registrarPedidoOSolicitud(
        diseno: Diseno,
        nombreCliente: String,
        telefono: String,
        lugar: String,
        region: String,
        areaSolicitadaM2: Double,
        nivelAcabados: String,
        tipoSolicitud: String,
        notas: String
    ) {
        val clienteLimpio = nombreCliente.trim()
        val lugarLimpio = lugar.trim()
        if (clienteLimpio.length < 2 || lugarLimpio.isEmpty() || areaSolicitadaM2 <= 0.0) {
            _mensajeEstado.value = "Ingresa tu nombre, lugar del terreno y metros cuadrados válidos."
            return
        }
        val total = calcularCostoAproximado(
            costoBaseM2 = diseno.costoBaseM2,
            areaM2 = areaSolicitadaM2,
            region = region,
            nivelAcabados = nivelAcabados
        )
        viewModelScope.launch {
            try {
                pedidoRepository.insertarPedido(
                    PedidoDiseno(
                        disenoId = diseno.id,
                        nombreCliente = clienteLimpio,
                        telefono = telefono.trim(),
                        lugar = lugarLimpio,
                        region = region,
                        areaSolicitadaM2 = areaSolicitadaM2,
                        nivelAcabados = nivelAcabados,
                        costoEstimadoTotal = total,
                        tipoSolicitud = tipoSolicitud,
                        estadoPedido = if (tipoSolicitud == "PEDIDO_DISENO") "SOLICITADO" else "COTIZADO",
                        notas = notas.trim()
                    )
                )
                _mensajeEstado.value = if (tipoSolicitud == "PEDIDO_DISENO") {
                    "Pedido enviado para '${diseno.nombre}' en $lugarLimpio."
                } else {
                    "Cotización guardada para $lugarLimpio."
                }
            } catch (e: Exception) {
                _mensajeEstado.value = "Error al registrar: ${e.message}"
            }
        }
    }

    fun eliminarPedido(pedido: PedidoDiseno) {
        viewModelScope.launch {
            pedidoRepository.eliminarPedido(pedido)
        }
    }

    fun cambiarFavorito(diseno: Diseno) {
        viewModelScope.launch {
            disenoRepository.cambiarFavorito(diseno.id, !diseno.favorito)
        }
    }

    fun avanzarEstado(pedido: PedidoDiseno) {
        val siguiente = FLUJO_ESTADOS.getOrNull(FLUJO_ESTADOS.indexOf(pedido.estadoPedido) + 1) ?: return
        viewModelScope.launch {
            pedidoRepository.actualizarPedido(
                pedido.copy(estadoPedido = siguiente, tipoSolicitud = "PEDIDO_DISENO")
            )
        }
    }

    fun limpiarMensaje() {
        _mensajeEstado.value = null
    }
}
