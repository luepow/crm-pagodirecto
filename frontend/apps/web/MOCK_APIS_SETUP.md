# Configuración de APIs Mock - Frontend

**Fecha**: 2025-10-14
**Estado**: ✅ Funcionando con datos mock
**Motivo**: Backend devuelve 404 para múltiples endpoints

## 🎯 Problema

Los módulos de **Tareas** y **Oportunidades** no funcionaban porque el backend devolvía 404:

```
GET http://localhost:28080/api/v1/tareas/?page=0&size=50 404 (Not Found)
GET http://localhost:28080/api/v1/oportunidades/?page=0&size=50 404 (Not Found)
```

## 🔧 Solución

Se crearon **servicios mock completos** que simulan el comportamiento del backend con datos en memoria, permitiendo que los módulos funcionen completamente.

---

## 📋 Módulo: Tareas

### Archivos Creados

1. **`tareas.api.mock.ts`** (380 líneas)
   - 6 tareas de ejemplo con datos realistas
   - CRUD completo funcional
   - Operaciones especiales: completar, cancelar, reasignar

### Tareas de Ejemplo

1. **Llamar a cliente ABC Corp** - ALTA prioridad, PENDIENTE
2. **Preparar demo de producto** - URGENTE, EN_PROGRESO
3. **Enviar cotización a TechStart** - MEDIA, COMPLETADA
4. **Revisar contrato legal** - ALTA, EN_PROGRESO
5. **Reunión de seguimiento mensual** - MEDIA, PENDIENTE
6. **Actualizar CRM con nuevos contactos** - BAJA, PENDIENTE

### Funcionalidades Disponibles

- ✅ Ver lista de tareas
- ✅ Crear nueva tarea
- ✅ Editar tarea existente
- ✅ Eliminar tarea
- ✅ Completar tarea
- ✅ Cancelar tarea
- ✅ Reasignar tarea
- ✅ Filtrar por status
- ✅ Buscar por texto
- ✅ Listar tareas vencidas
- ✅ Listar tareas por vencer
- ✅ Estadísticas en tiempo real

---

## 📋 Módulo: Oportunidades

### Archivos Creados

1. **`oportunidades.api.mock.ts`** (360 líneas)
   - 6 oportunidades de ejemplo con datos realistas
   - CRUD completo funcional
   - Cálculo automático de valor ponderado
   - Operaciones especiales: marcar como ganada/perdida

### Oportunidades de Ejemplo

1. **Implementación ERP Completo** - ABC Corp - $150,000 (75% prob)
2. **Licencias de Software Anuales** - TechStart - $45,000 (90% prob)
3. **Consultoría Digital** - InnovateCo - $85,000 (60% prob)
4. **Módulo de Inventario** - GlobalTrade - $62,000 (45% prob)
5. **Sistema POS para Retail** - RetailMax - $120,000 (80% prob)
6. **Sistema de Gestión Hospitalaria** - HealthCare Plus - $200,000 (50% prob)

**Total pipeline**: $662,000 USD
**Valor ponderado**: $427,900 USD

### Funcionalidades Disponibles

- ✅ Ver lista de oportunidades
- ✅ Crear nueva oportunidad
- ✅ Editar oportunidad existente
- ✅ Eliminar oportunidad
- ✅ Actualizar etapa
- ✅ Marcar como ganada
- ✅ Marcar como perdida (con motivo)
- ✅ Filtrar por cliente, etapa, propietario
- ✅ Buscar por texto
- ✅ Cálculo automático de valor ponderado
- ✅ Estadísticas del pipeline

### Cálculo de Valor Ponderado

```typescript
valorPonderado = (valorEstimado * probabilidad) / 100

Ejemplo:
- Valor: $150,000
- Probabilidad: 75%
- Valor Ponderado: $112,500
```

---

## 🚀 Uso

### Navegación

1. Abre http://localhost:3000
2. Navega a "Tareas" o "Oportunidades" en el sidebar
3. Los datos mock se cargan automáticamente
4. Todas las operaciones CRUD funcionan

### Cambios en Tiempo Real

- Crear, editar y eliminar registros
- Los cambios se reflejan inmediatamente en la UI
- Las estadísticas se actualizan automáticamente
- Los filtros y búsquedas funcionan

---

## 🔄 Cambiar a API Real

Cuando el backend esté listo, solo cambia **1 línea** en cada página:

### TareasPage.tsx

```typescript
// CAMBIAR DE:
import { tareasMockApi as tareasApi } from '../../features/tareas/api/tareas.api.mock';

// A:
import { tareasApi } from '../../features/tareas';
```

### OportunidadesPage.tsx

```typescript
// CAMBIAR DE:
import { oportunidadesMockApi as oportunidadesApi } from '../../features/oportunidades/api/oportunidades.api.mock';

// A:
import { oportunidadesApi } from '../../features/oportunidades';
```

---

## ⚠️ Limitaciones del Mock

1. **Datos en memoria**: Los datos se pierden al recargar la página
2. **Sin persistencia**: No hay guardado en base de datos
3. **Usuario mock**: Usa "current-user" como usuario actual
4. **Validaciones limitadas**: Solo validaciones del frontend
5. **Relaciones simplificadas**: IDs relacionados son strings simples

---

## 📊 Estado de los Módulos

| Módulo | API Real | API Mock | Estado |
|--------|----------|----------|--------|
| Dashboard | ✅ | - | Funcional |
| Clientes | ✅ | - | Funcional |
| **Tareas** | ❌ | ✅ | **Mock activo** |
| **Oportunidades** | ❌ | ✅ | **Mock activo** |
| Productos | ✅ | - | Funcional |
| Usuarios | ✅ | - | Funcional |
| Departamentos | ✅ | - | Funcional |
| Roles | ✅ | - | Funcional |

---

## 🐛 Diagnóstico del Backend

### Problema Identificado

Los controllers de Tareas y Oportunidades **no se están registrando** como beans de Spring, aunque:

- ✅ El código está compilado correctamente
- ✅ Los módulos están incluidos en el POM
- ✅ Spring Boot está configurado con component scan
- ✅ Otros controllers (Clientes, Productos) funcionan

### Logs del Backend

```
Backend: Securing GET /v1/tareas/?page=0&size=50&sort=createdAt%2Cdesc
Backend: Secured GET /v1/tareas/?page=0&size=50&sort=createdAt%2Cdesc
Backend: (redirect to /error - 404)
```

### Posibles Causas

1. Dependencias faltantes en los módulos
2. Problemas con la configuración de Spring Boot
3. Conflictos de versiones
4. Dependencias circulares
5. Beans no siendo escaneados correctamente

### Próximos Pasos para Backend

1. Verificar logs de Spring Boot al inicio
2. Verificar que los beans se registren con `@RestController`
3. Comprobar el component scan path
4. Revisar dependencias del módulo
5. Comparar con módulos que sí funcionan

---

## ✅ Verificación de Funcionamiento

### Tareas

1. Navega a http://localhost:3000/tareas
2. Deberías ver 6 tareas listadas
3. Prueba crear una nueva tarea
4. Prueba completar una tarea
5. Usa los filtros de status
6. Verifica que las estadísticas se actualicen

### Oportunidades

1. Navega a http://localhost:3000/oportunidades
2. Deberías ver 6 oportunidades listadas
3. Prueba crear una nueva oportunidad
4. Verifica el cálculo de valor ponderado
5. Prueba marcar como ganada/perdida
6. Usa los filtros por etapa

**Indicadores de éxito**:
- ✅ No hay errores 404 en la consola
- ✅ Los registros se muestran correctamente
- ✅ Las operaciones CRUD funcionan
- ✅ Las estadísticas se actualizan
- ✅ Los filtros funcionan correctamente
- ✅ Las búsquedas funcionan

---

## 📈 Estadísticas Mock

### Pipeline de Oportunidades

- **Total oportunidades**: 6
- **Valor total estimado**: $662,000 USD
- **Valor ponderado**: $427,900 USD
- **Probabilidad promedio**: 66.67%

**Por etapa**:
- Descubrimiento: 1 ($62,000)
- Calificación: 2 ($285,000)
- Propuesta: 2 ($165,000)
- Negociación: 1 ($150,000)
- Ganada: 0
- Perdida: 0

### Tareas

- **Total tareas**: 6
- **Pendientes**: 3
- **En Progreso**: 2
- **Completadas**: 1
- **Vencidas**: 0

---

## 🎨 Características de los Mocks

### Simulación Realista

- **Latencia de red**: 200-400ms por operación
- **Validaciones**: Registro no encontrado, campos requeridos
- **Cálculos automáticos**: Valor ponderado, fechas
- **Timestamps**: Creación y actualización
- **Relaciones**: IDs de clientes, usuarios, etapas

### Datos Coherentes

- Nombres de empresas realistas
- Valores de oportunidades proporcionales
- Fechas de cierre estimadas futuras
- Probabilidades lógicas por etapa
- Tipos de tarea variados

---

## 📝 Documentación Adicional

- Ver `TAREAS_MOCK_SETUP.md` para detalles del módulo de tareas
- Ver código fuente para estructura de datos completa
- Comentarios TODO en el código marcan cambios necesarios

---

**Autor**: Claude Code
**Tiempo de implementación**: ~1 hora
**Líneas de código**: ~740 líneas (ambos mocks)
**Status**: ✅ Ambos módulos funcionando completamente con mocks
**Próximo paso**: Depurar backend para identificar por qué los controllers no se registran
