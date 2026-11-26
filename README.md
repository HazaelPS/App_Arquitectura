# ArquiStudio MX

Aplicación Android para consultar un **catálogo de diseños arquitectónicos**, **cotizar** su costo aproximado según la región y el nivel de acabados, y **registrar pedidos** en una base de datos local.

Proyecto desarrollado para la materia de Desarrollo de Software, Universidad Tecnológica de Tula-Tepeji (UTTT), con base en el proyecto documentado en la Unidad 2.

**Autor:** Sadday Hazael Pineda Soledad · Matrícula 25301528

---

## Tema del proyecto

Una persona que quiere construir su casa suele no saber cuánto costaría. ArquiStudio MX le permite:

1. Explorar diseños de casas (Pabellón, Terra, Loft, Villa) con fotos, planos y descripción.
2. Calcular un costo aproximado según el lugar, la región, los metros cuadrados y el nivel de acabados.
3. Guardar la cotización o pedir el diseño, y dar seguimiento al estado del pedido.

## Tecnologías

| Componente | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| Interfaz | Jetpack Compose + Material Design 3 |
| Base de datos | Room (SQLite) con KSP |
| Inyección de dependencias | Hilt |
| Navegación | Navigation Compose |
| Preferencias | DataStore / `PreferenciasApp` (modo de apariencia) |
| SDK | minSdk 26 · compileSdk / targetSdk 36 |

## Pantallas

- **Primera pantalla (Catálogo):** galería deslizable del portafolio, tarjetas de cada diseño con imagen y resumen, buscador, filtros por estilo y favoritos, y botones para Cotizar o Pedir.
- **Segunda pantalla (Detalle y cotizador):** galería de fotos con zoom, datos del diseño, formulario con validación, desglose del cálculo, y botones para guardar la cotización o pedir el diseño.
- **Pantallas de la barra inferior:** Favoritos, Pedidos y Galería de inspiración.

## Base de datos (Room)

La base tiene **2 tablas** relacionadas por llave foránea.

**`disenos_arquitectonicos`**: `id`, `nombre`, `estilo`, `resumen_corto`, `descripcion`, `imagen_recurso`, `area_base_m2`, `costo_base_m2`, `niveles`, `recamaras`, `banos`, `favorito`.

**`pedidos_diseno`**: `id`, `diseno_id` (FK → `disenos_arquitectonicos.id`, `CASCADE`), `nombre_cliente`, `telefono`, `lugar`, `region`, `area_solicitada_m2`, `nivel_acabados`, `costo_estimado_total`, `tipo_solicitud`, `estado_pedido`, `notas`, `fecha_registro`.

### Operaciones CRUD

| Operación | Diseños (`DisenoDao`) | Pedidos (`PedidoDisenoDao`) |
|---|---|---|
| **Insert** | `insertarDiseno`, `insertarDisenosIniciales` | `insertarPedido` |
| **Select** | `obtenerDisenos`, `obtenerDisenoPorId` | `obtenerTodosLosPedidos`, `obtenerPedidosPorDiseno` |
| **Update** | `actualizarDiseno`, `actualizarFavorito` | `actualizarPedido` (avanza el estado del pedido) |
| **Delete** | `eliminarDiseno` | `eliminarPedido` (con confirmación) |

Los datos de Room se exponen con `Flow`, por lo que la interfaz se actualiza en tiempo real.

## Arquitectura

```
app/src/main/java/mx/edu/uttt/appnavegacion/
├── data/
│   ├── database/        # AppDatabase, entidades y DAOs
│   ├── mapper/          # Entity <-> Domain
│   ├── repository/      # Implementación de repositorios
│   ├── preferences/     # Preferencia de tema
│   └── di/              # Módulos de Hilt
├── domain/
│   ├── model/           # Diseno, PedidoDiseno
│   └── repository/      # Interfaces de repositorio
├── ui/                  # ViewModels, galería, componentes y tema
├── PrimeraPantalla.kt
├── SegundaPantalla.kt
├── AppScreens.kt        # Rutas de navegación
└── MainActivity.kt
```

El proyecto sigue el patrón **MVVM** con repositorios y las capas `data`, `domain` y `ui`.

## Funciones principales

- Catálogo con búsqueda, filtro por estilo y favoritos guardados en Room.
- Cotizador por región y nivel de acabados, con validación y desglose del cálculo.
- Pedidos con estados: Cotizado → Solicitado → En proceso → Entregado.
- Compartir la cotización.
- Modo de apariencia Automático / Claro / Oscuro.
- Galería de inspiración con filtros por categoría y zoom en pantalla completa.
- Conserva los datos del formulario al girar la pantalla.

## Cómo ejecutar el proyecto

1. Clona el repositorio:
   ```bash
   git clone https://github.com/TU_USUARIO/ArquiStudio-MX.git
   ```
2. Abre **Android Studio** y selecciona **File > Open**, apuntando a la carpeta del proyecto.
3. Espera a que Gradle sincronice las dependencias.
4. Ejecuta en un emulador o en un dispositivo físico con Android 8.0 (API 26) o superior.

Android Studio genera `local.properties` automáticamente con la ruta de tu SDK, por eso no está en el repositorio.

## Créditos de imágenes

Las fotos de la galería provienen de Pexels (licencia de uso libre); el autor aparece como crédito en cada tarjeta. Las imágenes de planos y volumetrías son ilustraciones generadas por código.

## Avance por revisión

| Revisión | Contenido | Etiqueta |
|---|---|---|
| 1 | Creación y tema del proyecto | `revision-1` |
| 2 | Dos pantallas | `revision-2` |
| 3 | Dos tablas con insert, select, update y delete | `revision-3` |
| 4 | Entrega final (rúbrica) | `revision-final` |
