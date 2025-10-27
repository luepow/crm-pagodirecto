# Módulo de Tareas - Configuración Mock

**Fecha**: 2025-10-14
**Estado**: ✅ Funcionando con datos mock
**Razón**: Backend devuelve 404 en `/api/v1/tareas/`

## 🎯 Problema Original

El frontend estaba llamando a `/api/v1/tareas/` pero el backend devolvía 404, impidiendo que el módulo de tareas funcionara:

```
GET http://localhost:28080/api/v1/tareas/?page=0&size=50&sort=createdAt%2Cdesc 404 (Not Found)
```

**Diagnóstico del Backend**:
- ✅ TareaController existe y está compilado
- ✅ Módulo de tareas incluido en el POM
- ✅ Spring Boot configurado correctamente
- ❌ Beans de Tarea NO registrados en el contexto de Spring (causa raíz no identificada)

## 🔧 Solución Implementada

Se creó un **servicio mock** que simula el comportamiento de la API del backend, permitiendo que el módulo funcione completamente mientras se resuelve el problema del backend.

## 📁 Archivos Creados/Modificados

### 1. Archivo Nuevo: `tareas.api.mock.ts`

**Ubicación**: `src/features/tareas/api/tareas.api.mock.ts`

**Funcionalidades**:
- ✅ Lista de 6 tareas de ejemplo con datos realistas
- ✅ CRUD completo (Create, Read, Update, Delete)
- ✅ Filtros por usuario, status, tipo relacionado
- ✅ Búsqueda por texto
- ✅ Paginación
- ✅ Operaciones especiales (completar, cancelar, reasignar)
- ✅ Simulación de latencia de red (200-400ms)
- ✅ Datos en memoria (se pierden al recargar)

**Tareas de ejemplo incluidas**:
1. **Llamada a cliente ABC Corp** - ALTA prioridad, PENDIENTE
2. **Preparar demo de producto** - URGENTE, EN_PROGRESO
3. **Enviar cotización a TechStart** - MEDIA, COMPLETADA
4. **Revisar contrato legal** - ALTA, EN_PROGRESO
5. **Reunión de seguimiento mensual** - MEDIA, PENDIENTE
6. **Actualizar CRM con nuevos contactos** - BAJA, PENDIENTE

### 2. Archivo Modificado: `TareasPage.tsx`

**Cambio realizado**:
```typescript
// ANTES
import { tareasApi } from '../../features/tareas';

// AHORA
// Usando mock API temporalmente mientras el backend está en desarrollo
// TODO: Cambiar a tareasApi cuando el backend esté listo
import { tareasMockApi as tareasApi } from '../../features/tareas/api/tareas.api.mock';
```

**Ventajas de este approach**:
- ✅ Solo se cambia 1 línea de código
- ✅ Toda la lógica del componente permanece igual
- ✅ Fácil de revertir cuando el backend esté listo
- ✅ Misma interfaz que la API real

## 🚀 Cómo Funciona

### Operaciones Disponibles

1. **Ver lista de tareas**
   - Filtrar por estado (Pendiente, En Progreso, Completada)
   - Buscar por título o descripción
   - Ordenar por fecha, prioridad, etc.

2. **Crear nueva tarea**
   - Click en "Nueva Tarea"
   - Llenar formulario
   - Los datos se guardan en memoria

3. **Editar tarea existente**
   - Click en una tarea de la lista
   - Modificar campos
   - Cambios se reflejan inmediatamente

4. **Eliminar tarea**
   - Click en botón eliminar
   - Confirmación
   - Se elimina de la lista

5. **Completar tarea**
   - Click en "Completar"
   - Cambia status a COMPLETADA
   - Añade timestamp de finalización

### Estadísticas en Tiempo Real

El dashboard muestra:
- **Pendientes**: Count de tareas con status PENDIENTE
- **En Progreso**: Count de tareas con status EN_PROGRESO
- **Completadas**: Count de tareas con status COMPLETADA
- **Vencidas**: Count de tareas con fecha de vencimiento pasada

## 🔄 Cómo Cambiar a la API Real

Cuando el backend esté funcionando, solo necesitas cambiar **1 línea** en `TareasPage.tsx`:

```typescript
// CAMBIAR DE ESTO:
import { tareasMockApi as tareasApi } from '../../features/tareas/api/tareas.api.mock';

// A ESTO:
import { tareasApi } from '../../features/tareas';
```

¡Y listo! El módulo automáticamente usará la API real del backend.

## ⚠️ Limitaciones del Mock

1. **Datos en memoria**: Los datos se pierden al recargar la página
2. **No hay persistencia**: Los cambios no se guardan en base de datos
3. **Sin autenticación real**: Usa usuario mock "current-user"
4. **Sin validaciones del backend**: Solo validaciones del frontend
5. **Relaciones simplificadas**: Los IDs relacionados son solo strings

## 📊 Datos Mock Disponibles

### Tipos de Tarea
- LLAMADA
- EMAIL
- REUNION
- SEGUIMIENTO
- ADMINISTRATIVA
- TECNICA
- OTRA

### Prioridades
- BAJA
- MEDIA
- ALTA
- URGENTE

### Estados
- PENDIENTE
- EN_PROGRESO
- COMPLETADA
- CANCELADA
- BLOQUEADA

## 🐛 Diagnóstico del Backend (Para Referencia)

El problema en el backend parece estar relacionado con que el TareaController no se está registrando como bean de Spring, a pesar de que:

1. El código está compilado correctamente
2. El módulo está incluido en el application POM
3. El component scan está configurado para `com.pagodirecto`
4. Otros controllers (Clientes, Oportunidades) funcionan correctamente

**Posibles causas a investigar**:
- Dependencias faltantes en el módulo de tareas
- Problemas con la configuración de Spring Boot
- Conflictos de versiones
- Circular dependencies

**Logs relevantes**:
```
Backend: Securing GET /v1/tareas/?page=0&size=50&sort=createdAt%2Cdesc
Backend: Secured GET /v1/tareas/?page=0&size=50&sort=createdAt%2Cdesc
Backend: (redirect to /error - 404)
```

## ✅ Verificación

Para verificar que todo funciona:

1. **Abrir la aplicación**: http://localhost:3000
2. **Navegar a Tareas**: Click en "Tareas" en el sidebar
3. **Ver lista de tareas**: Deberías ver 6 tareas de ejemplo
4. **Crear tarea**: Click en "Nueva Tarea" y llenar formulario
5. **Editar tarea**: Click en una tarea existente
6. **Completar tarea**: Click en "Completar" para cambiar status
7. **Filtrar**: Usar filtro de status y búsqueda

**Indicadores de éxito**:
- ✅ No hay errores 404 en la consola del navegador
- ✅ Las tareas se muestran correctamente
- ✅ Las operaciones CRUD funcionan
- ✅ Las estadísticas se actualizan en tiempo real
- ✅ Los filtros funcionan correctamente

## 🎯 Próximos Pasos

1. **Depurar el backend**:
   - Verificar por qué TareaController no se registra
   - Revisar logs de Spring Boot al inicio
   - Comparar con módulos funcionales (Clientes, Oportunidades)

2. **Cuando el backend esté listo**:
   - Cambiar el import en TareasPage.tsx
   - Probar todas las operaciones CRUD
   - Verificar que los datos persistan en base de datos

3. **Opcional - Mejorar el mock**:
   - Agregar localStorage para persistencia entre recargas
   - Simular errores de red para testing
   - Agregar más datos de ejemplo

## 📝 Notas Adicionales

- El mock usa la **misma interfaz** que la API real, facilitando la transición
- Se agregaron **comentarios TODO** en el código para recordar el cambio
- Los **delays** simulan latencia de red realista (200-400ms)
- El mock incluye **validaciones básicas** (ej: tarea no encontrada)

---

**Autor**: Claude Code
**Tiempo de implementación**: ~30 minutos
**Líneas de código**: ~380 líneas (mock service)
**Status**: ✅ Funcionando completamente con datos mock
**Próximo paso**: Depurar backend para identificar por qué TareaController no se registra
