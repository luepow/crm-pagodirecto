# PagoDirecto CRM/ERP - Visual Entity Relationship Diagram

This document provides ASCII-based visual representations of the database schema organized by domain.

---

## Complete System Overview

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                    SEGURIDAD DOMAIN                                 │
│                            (Security, IAM, Audit Logging)                          │
│                                                                                     │
│  ┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐        │
│  │ seguridad_       │      │ seguridad_       │      │ seguridad_       │        │
│  │ usuarios         │◀────▶│ usuarios_roles   │─────▶│ roles            │        │
│  │                  │      │                  │      │                  │        │
│  │ • id (PK)        │      │ • id (PK)        │      │ • id (PK)        │        │
│  │ • username       │      │ • usuario_id (FK)│      │ • nombre         │        │
│  │ • email          │      │ • rol_id (FK)    │      │ • departamento   │        │
│  │ • password_hash  │      │ • fecha_expir    │      │ • nivel_jerarq   │        │
│  │ • mfa_enabled    │      └──────────────────┘      └────────┬─────────┘        │
│  │ • status         │                                          │                  │
│  └────────┬─────────┘                                          │                  │
│           │                                                    │                  │
│           │                                      ┌─────────────▼──────────────┐   │
│           │                                      │ seguridad_                 │   │
│           │                                      │ roles_permisos             │   │
│           │                                      │                            │   │
│           │                                      │ • id (PK)                  │   │
│           │                                      │ • rol_id (FK)              │   │
│           │                                      │ • permiso_id (FK)          │   │
│           │                                      └────────────┬───────────────┘   │
│           │                                                   │                   │
│           │                                      ┌────────────▼───────────────┐   │
│           │                                      │ seguridad_permisos         │   │
│           │                                      │                            │   │
│           │                                      │ • id (PK)                  │   │
│           │                                      │ • recurso                  │   │
│           │                                      │ • accion                   │   │
│           │                                      │ • scope                    │   │
│           │                                      └────────────────────────────┘   │
│           │                                                                        │
│           ├──────────────────────────────┐                                        │
│           │                              │                                        │
│  ┌────────▼─────────┐          ┌────────▼────────────┐                           │
│  │ seguridad_       │          │ seguridad_          │                           │
│  │ refresh_tokens   │          │ audit_log           │                           │
│  │                  │          │                     │                           │
│  │ • id (PK)        │          │ • id (PK)           │                           │
│  │ • usuario_id (FK)│          │ • usuario_id (FK)   │                           │
│  │ • token_hash     │          │ • accion            │                           │
│  │ • expires_at     │          │ • recurso           │                           │
│  │ • revocado       │          │ • metadata (JSONB)  │                           │
│  └──────────────────┘          │ • resultado         │                           │
│                                 │ • IMMUTABLE         │                           │
│                                 └─────────────────────┘                           │
└─────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                   CLIENTES DOMAIN                                   │
│                              (Clients, Contacts, CRM)                              │
│                                                                                     │
│  ┌──────────────────────┐                                                          │
│  │ clientes_clientes    │                                                          │
│  │                      │                                                          │
│  │ • id (PK)            │                                                          │
│  │ • unidad_negocio_id  │                                                          │
│  │ • codigo (UNIQUE)    │                                                          │
│  │ • nombre             │                                                          │
│  │ • email              │                                                          │
│  │ • telefono           │                                                          │
│  │ • tipo (PERSONA/     │                                                          │
│  │   EMPRESA)           │                                                          │
│  │ • rfc                │                                                          │
│  │ • status             │                                                          │
│  │ • segmento           │                                                          │
│  │ • propietario_id (FK)│──────────────▶ seguridad_usuarios                       │
│  └──────┬───────┬───────┘                                                          │
│         │       │                                                                  │
│         │       │                                                                  │
│    ┌────▼─────┐ └──────────────┐                                                  │
│    │          │                │                                                  │
│  ┌─▼──────────▼─────┐   ┌──────▼──────────────┐                                  │
│  │ clientes_        │   │ clientes_           │                                  │
│  │ contactos        │   │ direcciones         │                                  │
│  │                  │   │                     │                                  │
│  │ • id (PK)        │   │ • id (PK)           │                                  │
│  │ • cliente_id (FK)│   │ • cliente_id (FK)   │                                  │
│  │ • nombre         │   │ • tipo (FISCAL/     │                                  │
│  │ • email          │   │   ENVIO/OTRO)       │                                  │
│  │ • telefono       │   │ • calle             │                                  │
│  │ • cargo          │   │ • ciudad            │                                  │
│  │ • is_primary     │   │ • estado            │                                  │
│  └──────────────────┘   │ • codigo_postal     │                                  │
│                         │ • pais              │                                  │
│                         │ • is_default        │                                  │
│                         └─────────────────────┘                                  │
└─────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                OPORTUNIDADES DOMAIN                                 │
│                            (Sales Pipeline, Activities)                            │
│                                                                                     │
│  ┌──────────────────────┐                                                          │
│  │ oportunidades_       │                                                          │
│  │ etapas_pipeline      │                                                          │
│  │                      │                                                          │
│  │ • id (PK)            │                                                          │
│  │ • unidad_negocio_id  │                                                          │
│  │ • nombre             │                                                          │
│  │ • tipo (LEAD/        │                                                          │
│  │   QUALIFIED/...)     │                                                          │
│  │ • orden              │                                                          │
│  │ • probabilidad_      │                                                          │
│  │   default            │                                                          │
│  └────────┬─────────────┘                                                          │
│           │                                                                        │
│           │                                                                        │
│  ┌────────▼─────────────┐                                                          │
│  │ oportunidades_       │                                                          │
│  │ oportunidades        │                                                          │
│  │                      │                                                          │
│  │ • id (PK)            │                                                          │
│  │ • unidad_negocio_id  │                                                          │
│  │ • cliente_id (FK)    │──────────────▶ clientes_clientes                        │
│  │ • titulo             │                                                          │
│  │ • valor_estimado     │                                                          │
│  │ • probabilidad       │                                                          │
│  │ • etapa_id (FK)      │──────────────▶ oportunidades_etapas_pipeline            │
│  │ • fecha_cierre_est   │                                                          │
│  │ • propietario_id (FK)│──────────────▶ seguridad_usuarios                       │
│  └──────┬───────────────┘                                                          │
│         │                                                                          │
│         │                                                                          │
│  ┌──────▼───────────────┐                                                          │
│  │ oportunidades_       │                                                          │
│  │ actividades          │                                                          │
│  │                      │                                                          │
│  │ • id (PK)            │                                                          │
│  │ • oportunidad_id (FK)│                                                          │
│  │ • tipo (LLAMADA/     │                                                          │
│  │   REUNION/EMAIL)     │                                                          │
│  │ • titulo             │                                                          │
│  │ • fecha_actividad    │                                                          │
│  │ • completada         │                                                          │
│  │ • resultado          │                                                          │
│  └──────────────────────┘                                                          │
└─────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                   TAREAS DOMAIN                                     │
│                          (Tasks, Activities, Comments)                             │
│                                                                                     │
│  ┌──────────────────────┐                                                          │
│  │ tareas_tareas        │                                                          │
│  │                      │                                                          │
│  │ • id (PK)            │                                                          │
│  │ • unidad_negocio_id  │                                                          │
│  │ • titulo             │                                                          │
│  │ • tipo               │                                                          │
│  │ • prioridad (BAJA/   │                                                          │
│  │   MEDIA/ALTA/URG)    │                                                          │
│  │ • status (PENDIENTE/ │                                                          │
│  │   EN_PROGRESO/...)   │                                                          │
│  │ • fecha_vencimiento  │                                                          │
│  │ • asignado_a (FK)    │──────────────▶ seguridad_usuarios                       │
│  │ • relacionado_tipo   │─┐ POLYMORPHIC                                           │
│  │ • relacionado_id     │─┴▶ (CLIENTE, OPORTUNIDAD, PEDIDO, etc.)                 │
│  └──────┬───────────────┘                                                          │
│         │                                                                          │
│         │                                                                          │
│  ┌──────▼───────────────┐                                                          │
│  │ tareas_comentarios   │                                                          │
│  │                      │                                                          │
│  │ • id (PK)            │                                                          │
│  │ • tarea_id (FK)      │                                                          │
│  │ • usuario_id (FK)    │──────────────▶ seguridad_usuarios                       │
│  │ • comentario (TEXT)  │                                                          │
│  └──────────────────────┘                                                          │
└─────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                 PRODUCTOS DOMAIN                                    │
│                         (Catalog, Categories, Pricing)                             │
│                                                                                     │
│  ┌──────────────────────┐                                                          │
│  │ productos_categorias │                                                          │
│  │                      │                                                          │
│  │ • id (PK)            │◀─────┐ SELF-REFERENCE (tree)                             │
│  │ • unidad_negocio_id  │      │                                                   │
│  │ • nombre             │      │                                                   │
│  │ • parent_id (FK)     │──────┘                                                   │
│  │ • nivel (0-5)        │                                                          │
│  │ • path               │                                                          │
│  │ • orden              │                                                          │
│  └────────┬─────────────┘                                                          │
│           │                                                                        │
│           │                                                                        │
│  ┌────────▼─────────────┐                                                          │
│  │ productos_productos  │                                                          │
│  │                      │                                                          │
│  │ • id (PK)            │                                                          │
│  │ • unidad_negocio_id  │                                                          │
│  │ • codigo (UNIQUE)    │                                                          │
│  │ • nombre             │                                                          │
│  │ • categoria_id (FK)  │                                                          │
│  │ • tipo (PRODUCTO/    │                                                          │
│  │   SERVICIO/COMBO)    │                                                          │
│  │ • precio_base        │                                                          │
│  │ • costo_unitario     │                                                          │
│  │ • stock_actual       │                                                          │
│  │ • stock_minimo       │                                                          │
│  │ • status             │                                                          │
│  └──────┬───────────────┘                                                          │
│         │                                                                          │
│         │                                                                          │
│  ┌──────▼───────────────┐                                                          │
│  │ productos_precios    │                                                          │
│  │                      │                                                          │
│  │ • id (PK)            │                                                          │
│  │ • producto_id (FK)   │                                                          │
│  │ • tipo_precio (LISTA/│                                                          │
│  │   MAYOREO/PROMO)     │                                                          │
│  │ • precio             │                                                          │
│  │ • fecha_inicio       │                                                          │
│  │ • fecha_fin          │                                                          │
│  │ • segmento_cliente   │                                                          │
│  │ • cantidad_minima    │                                                          │
│  └──────────────────────┘                                                          │
└─────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                   VENTAS DOMAIN                                     │
│                         (Quotes, Orders, Line Items)                               │
│                                                                                     │
│  ┌──────────────────────┐                                                          │
│  │ ventas_cotizaciones  │                                                          │
│  │                      │                                                          │
│  │ • id (PK)            │                                                          │
│  │ • unidad_negocio_id  │                                                          │
│  │ • cliente_id (FK)    │──────────────▶ clientes_clientes                        │
│  │ • oportunidad_id (FK)│──────────────▶ oportunidades_oportunidades              │
│  │ • numero (UNIQUE)    │                                                          │
│  │ • fecha              │                                                          │
│  │ • fecha_validez      │                                                          │
│  │ • status (BORRADOR/  │                                                          │
│  │   ENVIADA/ACEPTADA)  │                                                          │
│  │ • subtotal           │                                                          │
│  │ • impuestos          │                                                          │
│  │ • total              │                                                          │
│  │ • propietario_id (FK)│──────────────▶ seguridad_usuarios                       │
│  └──────┬───────────────┘                                                          │
│         │                                                                          │
│         │                                                                          │
│  ┌──────▼───────────────┐                                                          │
│  │ ventas_items_        │                                                          │
│  │ cotizacion           │                                                          │
│  │                      │                                                          │
│  │ • id (PK)            │                                                          │
│  │ • cotizacion_id (FK) │                                                          │
│  │ • producto_id (FK)   │──────────────▶ productos_productos                      │
│  │ • cantidad           │                                                          │
│  │ • precio_unitario    │                                                          │
│  │ • descuento_%        │                                                          │
│  │ • subtotal           │                                                          │
│  │ • impuesto_%         │                                                          │
│  │ • total              │                                                          │
│  └──────────────────────┘                                                          │
│                                                                                     │
│  ┌──────────────────────┐                                                          │
│  │ ventas_pedidos       │                                                          │
│  │                      │                                                          │
│  │ • id (PK)            │                                                          │
│  │ • unidad_negocio_id  │                                                          │
│  │ • cotizacion_id (FK) │──────────────▶ ventas_cotizaciones (optional)           │
│  │ • cliente_id (FK)    │──────────────▶ clientes_clientes                        │
│  │ • numero (UNIQUE)    │                                                          │
│  │ • fecha              │                                                          │
│  │ • fecha_entrega_est  │                                                          │
│  │ • status (PENDIENTE/ │                                                          │
│  │   CONFIRMADO/...)    │                                                          │
│  │ • subtotal           │                                                          │
│  │ • impuestos          │                                                          │
│  │ • total              │                                                          │
│  │ • metodo_pago        │                                                          │
│  │ • propietario_id (FK)│──────────────▶ seguridad_usuarios                       │
│  └──────┬───────────────┘                                                          │
│         │                                                                          │
│         │                                                                          │
│  ┌──────▼───────────────┐                                                          │
│  │ ventas_items_pedido  │                                                          │
│  │                      │                                                          │
│  │ • id (PK)            │                                                          │
│  │ • pedido_id (FK)     │                                                          │
│  │ • producto_id (FK)   │──────────────▶ productos_productos                      │
│  │ • cantidad           │                                                          │
│  │ • cantidad_entregada │                                                          │
│  │ • precio_unitario    │                                                          │
│  │ • descuento_%        │                                                          │
│  │ • subtotal           │                                                          │
│  │ • impuesto_%         │                                                          │
│  │ • total              │                                                          │
│  └──────────────────────┘                                                          │
└─────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                 REPORTES DOMAIN                                     │
│                          (Dashboards, Widgets, Analytics)                          │
│                                                                                     │
│  ┌──────────────────────┐                                                          │
│  │ reportes_dashboards  │                                                          │
│  │                      │                                                          │
│  │ • id (PK)            │                                                          │
│  │ • unidad_negocio_id  │                                                          │
│  │ • nombre             │                                                          │
│  │ • configuracion      │ (JSONB: layout, filters, etc.)                          │
│  │   (JSONB)            │                                                          │
│  │ • propietario_id (FK)│──────────────▶ seguridad_usuarios                       │
│  │ • es_publico         │                                                          │
│  │ • orden              │                                                          │
│  └──────┬───────────────┘                                                          │
│         │                                                                          │
│         │                                                                          │
│  ┌──────▼───────────────┐                                                          │
│  │ reportes_widgets     │                                                          │
│  │                      │                                                          │
│  │ • id (PK)            │                                                          │
│  │ • dashboard_id (FK)  │                                                          │
│  │ • tipo (CHART/TABLE/ │                                                          │
│  │   KPI/MAP/LIST)      │                                                          │
│  │ • titulo             │                                                          │
│  │ • configuracion      │ (JSONB: query, fields, colors, etc.)                    │
│  │   (JSONB)            │                                                          │
│  │ • posicion (JSONB)   │ {x, y, width, height}                                   │
│  └──────────────────────┘                                                          │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## Relationship Summary

### Cross-Domain Relationships

```
seguridad_usuarios (created_by/updated_by)
    │
    ├──▶ ALL TABLES (audit trail)
    │
    ├──▶ clientes_clientes (propietario_id)
    ├──▶ oportunidades_oportunidades (propietario_id)
    ├──▶ tareas_tareas (asignado_a)
    ├──▶ ventas_cotizaciones (propietario_id)
    ├──▶ ventas_pedidos (propietario_id)
    └──▶ reportes_dashboards (propietario_id)

clientes_clientes
    │
    ├──▶ oportunidades_oportunidades
    ├──▶ ventas_cotizaciones
    └──▶ ventas_pedidos

oportunidades_oportunidades
    └──▶ ventas_cotizaciones (optional link)

productos_productos
    │
    ├──▶ ventas_items_cotizacion
    └──▶ ventas_items_pedido

ventas_cotizaciones
    └──▶ ventas_pedidos (quote to order conversion)
```

---

## Cardinality Notation

- **1:1** (One to One): `────`
- **1:N** (One to Many): `────▶`
- **N:M** (Many to Many): `◀────▶` (via junction table)
- **0..1** (Optional): `─ ─ ─▶`
- **0..N** (Optional Many): `- - -▶`

---

## Key Patterns Illustrated

### 1. Hierarchical Tree (Self-Reference)
```
productos_categorias
    ├── Electronics
    │   ├── Computers
    │   │   ├── Laptops
    │   │   └── Desktops
    │   └── Phones
    └── Furniture
```

Implementation: `parent_id` references same table + `path` field for fast queries

### 2. Polymorphic Relationships
```
tareas_tareas.relacionado_tipo + relacionado_id
    │
    ├──▶ clientes_clientes (when relacionado_tipo = 'CLIENTE')
    ├──▶ oportunidades_oportunidades (when relacionado_tipo = 'OPORTUNIDAD')
    └──▶ ventas_pedidos (when relacionado_tipo = 'PEDIDO')
```

### 3. Soft Delete Pattern
```
SELECT * FROM clientes_clientes WHERE deleted_at IS NULL;  -- Active records
SELECT * FROM clientes_clientes WHERE deleted_at IS NOT NULL;  -- Deleted records
```

### 4. Multi-Tenant Isolation
```sql
-- RLS Policy ensures:
SELECT * FROM clientes_clientes;
-- Automatically becomes:
SELECT * FROM clientes_clientes WHERE unidad_negocio_id = app_current_tenant();
```

### 5. Audit Trail Chain
```
Record created:
    created_at = NOW()
    created_by = current_user_id
    updated_at = NOW()
    updated_by = current_user_id

Record updated:
    updated_at = NOW()
    updated_by = current_user_id
    (created_* unchanged)
```

---

## Data Flow Examples

### Quote to Order Conversion

```
1. Cliente creates opportunity
   clientes_clientes → oportunidades_oportunidades

2. Sales rep creates quote
   oportunidades_oportunidades → ventas_cotizaciones
                               → ventas_items_cotizacion (← productos_productos)

3. Quote accepted, create order
   ventas_cotizaciones → ventas_pedidos
   ventas_items_cotizacion → ventas_items_pedido (copy lines)

4. Order fulfillment
   ventas_items_pedido.cantidad_entregada updated
   productos_productos.stock_actual decremented
```

### User Permissions Check

```
1. User logs in
   seguridad_usuarios authenticated

2. Load user roles
   seguridad_usuarios → seguridad_usuarios_roles → seguridad_roles

3. Load role permissions
   seguridad_roles → seguridad_roles_permisos → seguridad_permisos

4. Check action allowed
   IF permiso.recurso = 'clientes' AND permiso.accion = 'READ' THEN
       Allow access to clientes_clientes
   ELSE
       Deny access
```

### Sales Pipeline Report

```
SELECT
    e.nombre AS etapa,
    COUNT(o.id) AS cantidad,
    SUM(o.valor_estimado) AS valor_total,
    AVG(o.probabilidad) AS prob_promedio
FROM oportunidades_oportunidades o
JOIN oportunidades_etapas_pipeline e ON o.etapa_id = e.id
WHERE o.deleted_at IS NULL
  AND o.unidad_negocio_id = app_current_tenant()
GROUP BY e.nombre, e.orden
ORDER BY e.orden;
```

---

## Legend

### Symbols Used

- **PK**: Primary Key
- **FK**: Foreign Key
- **UNIQUE**: Unique constraint/index
- **JSONB**: JSON data type (binary storage)
- **▶**: One-to-many relationship direction
- **◀▶**: Many-to-many relationship (bidirectional)
- **─ ─ ─▶**: Optional relationship (nullable FK)

### Status Values

Common status fields across domains:

**User Status:**
- ACTIVE, INACTIVE, LOCKED, SUSPENDED

**Client Status:**
- ACTIVE, INACTIVE, PROSPECT, LEAD, BLACKLIST

**Opportunity Stage:**
- LEAD, QUALIFIED, PROPOSAL, NEGOTIATION, CLOSED_WON, CLOSED_LOST

**Task Status:**
- PENDIENTE, EN_PROGRESO, COMPLETADA, CANCELADA, BLOQUEADA

**Quote Status:**
- BORRADOR, ENVIADA, ACEPTADA, RECHAZADA, EXPIRADA

**Order Status:**
- PENDIENTE, CONFIRMADO, EN_PROCESO, ENVIADO, ENTREGADO, CANCELADO, DEVUELTO

---

**END OF VISUAL ERD**
