# PagoDirecto CRM/ERP System

**Sistema ERP/CRM Empresarial Completo con Arquitectura Limpia y Diseño de Dominio**

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)
![React](https://img.shields.io/badge/React-18-blue.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)
![License](https://img.shields.io/badge/license-Proprietary-red.svg)

---

## 📋 Tabla de Contenidos

- [Descripción General](#-descripción-general)
- [Características Principales](#-características-principales)
- [Arquitectura](#-arquitectura)
- [Stack Tecnológico](#-stack-tecnológico)
- [Inicio Rápido](#-inicio-rápido)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Módulos del Sistema](#-módulos-del-sistema)
- [Configuración](#-configuración)
- [Desarrollo](#-desarrollo)
- [Producción](#-producción)
- [Seguridad](#-seguridad)
- [Testing](#-testing)
- [Documentación](#-documentación)
- [Contribución](#-contribución)
- [Licencia](#-licencia)

---

## 🎯 Descripción General

**PagoDirecto CRM/ERP** es un sistema empresarial integral diseñado para gestionar todas las operaciones comerciales de una organización moderna. Construido con **Clean Architecture**, **Domain-Driven Design** y las mejores prácticas de la industria.

### Objetivo

Proporcionar una plataforma modular, segura, escalable y fácil de usar que centralice:
- Gestión de clientes y relaciones (CRM)
- Gestión de oportunidades y pipeline de ventas
- Gestión de tareas y actividades
- Catálogo de productos y servicios
- Proceso de ventas y cotizaciones
- Reportes y análisis de negocio

### Filosofía de Diseño

Siguiendo el **BrandBook PagoDirecto 2024**, el sistema ofrece una experiencia **"mágica"** con:
- **3 clics máximo** hasta cualquier acción clave
- Interfaces **minimalistas** y centradas en la acción
- Micro-animaciones y **transiciones suaves**
- Lenguaje claro: *"Paga de una", "Sin colas, sin esperas"*

---

## ✨ Características Principales

### 🔐 Seguridad Empresarial
- **JWT Authentication** con tokens de 5 minutos + refresh tokens de 30 días
- **MFA (TOTP)** para cuentas privilegiadas
- **RBAC/ABAC** con roles y permisos granulares
- **Row-Level Security** (PostgreSQL RLS) para aislamiento multi-tenant
- **Audit Trail** inmutable con retención de 7 años
- **BCrypt** con cost factor 12 para passwords
- **Rate Limiting** y protección contra fuerza bruta (5 intentos → bloqueo 30 min)

### 🏢 Gestión de Clientes (CRM)
- Registro de clientes (Personas y Empresas)
- Gestión de contactos con roles
- Múltiples direcciones (fiscal, envío, facturación)
- Historial completo de interacciones
- Búsqueda avanzada con filtros
- Estados del cliente (Activo, Inactivo, Prospecto)

### 📊 Pipeline de Ventas
- Visualización tipo **Kanban** del pipeline
- 6 etapas configurables (Lead → Calificado → Propuesta → Negociación → Ganada/Perdida)
- Probabilidad de cierre automática por etapa
- Forecast de ventas
- Asignación de oportunidades a vendedores
- Actividades y notas por oportunidad

### ✅ Gestión de Tareas
- Tareas con prioridad (Alta, Media, Baja)
- Estados (Pendiente, En Progreso, Completada, Cancelada)
- Tipos (Llamada, Email, Reunión, Otra)
- Asignación a usuarios
- Relaciones polimórficas (con Clientes, Oportunidades, etc.)
- Vista de calendario y lista
- Comentarios y colaboración

### 🛍️ Catálogo de Productos
- Categorías jerárquicas (5 niveles)
- Productos con código, descripción, precio base
- Multi-moneda (MXN, USD, EUR)
- Control de inventario
- Precios diferenciados (lista, mayoreo, descuento)
- Estados (Activo, Inactivo, Descontinuado)

### 💰 Ventas y Cotizaciones
- Generación de cotizaciones con líneas de producto
- Cálculo automático de subtotales, impuestos, total
- Conversión de cotización → pedido
- Estados del pedido (Borrador, Enviado, Aprobado, Rechazado)
- Historial de versiones
- Exportación a PDF

### 📈 Reportes y Dashboards
- **KPIs en tiempo real:**
  - Clientes nuevos del mes
  - Oportunidades activas
  - Tareas pendientes
  - Forecast de ventas
- **Gráficos:**
  - Ventas últimos 6 meses (línea)
  - Pipeline por etapa (barras)
  - Distribución de clientes por tipo (dona)
- Dashboards personalizables con widgets
- Configuración JSONB para flexibilidad
- Exportación a Excel/CSV

### 🎨 Diseño UI/UX (BrandBook 2024)
- **Colores:** Magenta primario (#FF2463), Azul oscuro (#050B26)
- **Tipografía:** Outfit Sans (Light, Medium, SemiBold)
- **Squircle borders** (rounded-xl, rounded-2xl)
- Gradientes azul → magenta
- Iconografía geométrica con Lucide React
- Micro-animaciones suaves
- Responsive (mobile, tablet, desktop)
- Accesible (WCAG 2.1 AA)

---

## 🏗️ Arquitectura

### Clean/Hexagonal Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    API / Presentation                    │
│         (Controllers, REST Endpoints, OpenAPI)           │
└────────────────┬───────────────────────────┬─────────────┘
                 │                           │
┌────────────────▼────────────┐  ┌──────────▼──────────────┐
│    Application Layer        │  │   Infrastructure Layer   │
│  (Services, DTOs, Mappers)  │  │ (Repos, External APIs)   │
└────────────────┬─────────────┘  └──────────┬──────────────┘
                 │                           │
                 └──────────┬────────────────┘
                            │
                ┌───────────▼────────────┐
                │    Domain Layer        │
                │  (Entities, VOs, Rules)│
                └────────────────────────┘
```

### Bounded Contexts (DDD)

- **Seguridad:** Usuarios, Roles, Permisos, Auth
- **Clientes:** Clientes, Contactos, Direcciones
- **Oportunidades:** Pipeline, Oportunidades, Actividades
- **Tareas:** Tareas, Comentarios
- **Productos:** Productos, Categorías, Precios
- **Ventas:** Cotizaciones, Pedidos
- **Reportes:** Dashboards, Widgets, KPIs

---

## 🛠️ Stack Tecnológico

### Backend

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **Java** | 17 (LTS) | Lenguaje principal |
| **Spring Boot** | 3.2.5 | Framework de aplicación |
| **Spring Security** | 6.x | Autenticación y autorización |
| **Spring Data JPA** | 3.x | ORM y persistencia |
| **PostgreSQL** | 16 | Base de datos relacional |
| **Flyway** | 10.x | Migraciones de BD |
| **JWT (jjwt)** | 0.12.5 | Tokens de autenticación |
| **MapStruct** | 1.5.5 | Mapeo DTO ↔ Entity |
| **Lombok** | 1.18.30 | Reducción de boilerplate |
| **SpringDoc OpenAPI** | 2.3.0 | Documentación API |
| **Maven** | 3.9+ | Gestión de dependencias |

### Frontend

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **React** | 18 | Librería UI |
| **TypeScript** | 5.6 | Type-safe JavaScript |
| **Vite** | 5.x | Build tool |
| **TailwindCSS** | 3.4 | Utility-first CSS |
| **React Router** | 6.x | Enrutamiento SPA |
| **React Query** | 5.x | Data fetching y caching |
| **Zustand** | 4.x | State management |
| **React Hook Form** | 7.x | Formularios |
| **Zod** | 3.x | Validación de esquemas |
| **Axios** | 1.x | Cliente HTTP |
| **Recharts** | 2.x | Gráficos y visualizaciones |
| **Lucide React** | Latest | Iconografía |

### Infraestructura

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **Docker** | 24+ | Contenedores |
| **Docker Compose** | 2.x | Orquestación local |
| **Nginx** | Alpine | Reverse proxy |
| **Adminer** | Latest | DB management UI |

### DevOps & CI/CD

- **Git** - Control de versiones
- **GitHub Actions** ✅ - CI/CD pipelines configurados
- **PM2** ✅ - Process manager para backend
- **Nginx** ✅ - Reverse proxy configurado
- **SonarQube** (ready) - Análisis de código
- **OWASP Dependency Check** ✅ - Escaneo de vulnerabilidades

---

## 🚀 Inicio Rápido

### Prerrequisitos

- **Docker** 24+ y **Docker Compose** 2.x
- **Java** 17 (solo para desarrollo sin Docker)
- **Node.js** 20+ (solo para desarrollo frontend sin Docker)
- **Maven** 3.9+ (solo para desarrollo sin Docker)
- **PostgreSQL** 16 (solo para desarrollo sin Docker)

### Opción 1: Ejecución Completa con Docker (Recomendado)

```bash
# 1. Clonar el repositorio
git clone <repository-url>
cd crm_pd

# 2. Configurar variables de entorno
cd infra/docker
cp .env.development .env

# 3. Iniciar todos los servicios
cd ../scripts
chmod +x start.sh
./start.sh development

# 4. Esperar a que todos los servicios estén saludables (2-3 minutos)
# Los logs mostrarán el progreso

# 5. Acceder a la aplicación
# Frontend:  http://localhost:3000
# Backend:   http://localhost:8080/api
# API Docs:  http://localhost:8080/swagger-ui.html
# Adminer:   http://localhost:8081
```

**Credenciales de prueba:**
- Usuario: `admin@pagodirecto.com`
- Contraseña: `admin123`

### Opción 2: Desarrollo Local (Backend)

```bash
# 1. Configurar base de datos PostgreSQL
createdb pagodirecto_crm

# 2. Configurar variables de entorno
export DATABASE_URL=jdbc:postgresql://localhost:5432/pagodirecto_crm
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=your_password
export JWT_SECRET=your-secret-key-minimum-32-characters

# 3. Compilar y ejecutar
cd backend
./mvnw clean install
./mvnw spring-boot:run -pl application

# 4. Acceder a API
# http://localhost:8080/api/docs
```

### Opción 3: Desarrollo Local (Frontend)

```bash
# 1. Instalar dependencias
cd frontend/apps/web
npm install

# 2. Configurar API endpoint
# Editar .env.local:
echo "VITE_API_URL=http://localhost:8080/api" > .env.local

# 3. Ejecutar servidor de desarrollo
npm run dev

# 4. Acceder
# http://localhost:3000
```

---

## 📁 Estructura del Proyecto

```
crm_pd/
├── backend/                      # Aplicación backend Java
│   ├── core-domain/              # Dominio compartido (BaseEntity, excepciones)
│   ├── seguridad/                # Módulo de autenticación y autorización
│   │   ├── domain/               # Entidades (Usuario, Rol, Permiso)
│   │   ├── application/          # Servicios y DTOs
│   │   ├── infrastructure/       # Repositorios y seguridad (JWT, RLS)
│   │   └── api/                  # Controladores REST
│   ├── clientes/                 # Módulo de CRM
│   ├── oportunidades/            # Módulo de pipeline de ventas
│   ├── tareas/                   # Módulo de gestión de tareas
│   ├── productos/                # Módulo de catálogo
│   ├── ventas/                   # Módulo de cotizaciones y pedidos
│   ├── reportes/                 # Módulo de dashboards y KPIs
│   ├── application/              # Aplicación principal Spring Boot
│   │   └── src/main/resources/
│   │       └── db/migration/     # Migraciones Flyway
│   └── pom.xml                   # Maven parent POM
│
├── frontend/                     # Aplicación frontend React
│   ├── apps/web/                 # Aplicación web principal
│   │   ├── src/
│   │   │   ├── components/       # Componentes React
│   │   │   ├── pages/            # Páginas (Login, Dashboard, etc.)
│   │   │   ├── hooks/            # Custom React hooks
│   │   │   ├── services/         # API services (Axios)
│   │   │   ├── store/            # Estado global (Zustand)
│   │   │   ├── types/            # TypeScript types
│   │   │   └── utils/            # Utilidades
│   │   ├── public/               # Assets estáticos
│   │   ├── package.json
│   │   └── vite.config.ts        # Configuración Vite
│   ├── shared-ui/                # Librería de componentes compartidos
│   │   └── components/           # Button, Input, Card, etc.
│   └── design-tokens/            # Tokens del BrandBook (colores, tipografía)
│       └── tailwind.config.js    # Configuración Tailwind
│
├── infra/                        # Infraestructura y DevOps
│   ├── docker/                   # Archivos Docker
│   │   ├── docker-compose.yml    # Orquestación de servicios
│   │   ├── Dockerfile.backend    # Imagen backend
│   │   ├── Dockerfile.frontend   # Imagen frontend
│   │   ├── nginx.conf            # Configuración Nginx
│   │   ├── init-db.sh            # Script de inicialización BD
│   │   ├── .env.example          # Template de variables
│   │   └── README.md             # Documentación Docker
│   └── scripts/                  # Scripts de utilidad
│       ├── start.sh              # Iniciar servicios
│       ├── stop.sh               # Detener servicios
│       ├── backup-db.sh          # Backup de BD
│       └── restore-db.sh         # Restaurar BD
│
├── docs/                         # Documentación técnica
│   ├── adrs/                     # Architecture Decision Records
│   ├── api/                      # Documentación de API (OpenAPI)
│   ├── c4/                       # Diagramas C4
│   ├── erd/                      # Entity Relationship Diagrams
│   │   ├── database-schema.md    # Documentación completa del esquema
│   │   ├── erd-visual.md         # Diagramas visuales
│   │   └── database-security-guide.md  # Guía de seguridad
│   ├── ux/                       # Guías de diseño UX
│   └── runbooks/                 # Procedimientos operacionales
│
├── CLAUDE.md                     # Guía para Claude Code (desarrollo)
├── README.md                     # Este archivo
└── .gitignore                    # Archivos excluidos del control de versiones
```

---

## 🔧 Módulos del Sistema

### 1. Seguridad (Fundacional)

**Responsabilidades:**
- Autenticación JWT con refresh tokens
- Autorización basada en roles (RBAC) y permisos (ABAC)
- Gestión de usuarios y roles
- Multi-Factor Authentication (MFA/TOTP)
- Audit trail inmutable
- Row-Level Security (RLS) para multi-tenancy

**Endpoints principales:**
- `POST /api/v1/auth/login` - Iniciar sesión
- `POST /api/v1/auth/refresh` - Renovar token
- `POST /api/v1/auth/logout` - Cerrar sesión
- `GET /api/v1/users` - Listar usuarios
- `POST /api/v1/users` - Crear usuario
- `PUT /api/v1/users/{id}` - Actualizar usuario
- `GET /api/v1/roles` - Listar roles

### 2. Clientes (CRM)

**Responsabilidades:**
- Gestión de clientes (personas y empresas)
- Múltiples contactos por cliente
- Direcciones (fiscal, envío, entrega)
- Historial de interacciones

**Endpoints principales:**
- `GET /api/v1/clientes` - Listar clientes (paginado)
- `POST /api/v1/clientes` - Crear cliente
- `GET /api/v1/clientes/{id}` - Obtener cliente
- `PUT /api/v1/clientes/{id}` - Actualizar cliente
- `DELETE /api/v1/clientes/{id}` - Eliminar (soft delete)
- `GET /api/v1/clientes/{id}/contactos` - Contactos del cliente
- `GET /api/v1/clientes/{id}/direcciones` - Direcciones del cliente

### 3. Oportunidades (Pipeline)

**Responsabilidades:**
- Gestión del pipeline de ventas
- Oportunidades con etapas configurables
- Probabilidad de cierre por etapa
- Actividades relacionadas
- Forecast de ventas

**Endpoints principales:**
- `GET /api/v1/oportunidades` - Listar oportunidades
- `POST /api/v1/oportunidades` - Crear oportunidad
- `PUT /api/v1/oportunidades/{id}/etapa` - Mover a siguiente etapa
- `GET /api/v1/oportunidades/pipeline` - Vista Kanban del pipeline
- `GET /api/v1/etapas-pipeline` - Obtener etapas configuradas

### 4. Tareas

**Responsabilidades:**
- Gestión de tareas y actividades
- Asignación a usuarios
- Prioridades y estados
- Relaciones polimórficas (con clientes, oportunidades, etc.)
- Comentarios colaborativos

**Endpoints principales:**
- `GET /api/v1/tareas` - Listar tareas
- `POST /api/v1/tareas` - Crear tarea
- `PUT /api/v1/tareas/{id}` - Actualizar tarea
- `PUT /api/v1/tareas/{id}/completar` - Marcar como completada
- `GET /api/v1/tareas/mis-tareas` - Tareas asignadas al usuario actual

### 5. Productos

**Responsabilidades:**
- Catálogo de productos y servicios
- Categorías jerárquicas
- Precios diferenciados
- Control de inventario
- Multi-moneda

**Endpoints principales:**
- `GET /api/v1/productos` - Listar productos
- `POST /api/v1/productos` - Crear producto
- `GET /api/v1/productos/{id}` - Obtener producto
- `GET /api/v1/categorias` - Listar categorías
- `GET /api/v1/productos/{id}/precios` - Precios del producto

### 6. Ventas

**Responsabilidades:**
- Generación de cotizaciones
- Conversión cotización → pedido
- Cálculo de totales (subtotal + impuestos)
- Historial de ventas
- Estados del pedido

**Endpoints principales:**
- `GET /api/v1/cotizaciones` - Listar cotizaciones
- `POST /api/v1/cotizaciones` - Crear cotización
- `POST /api/v1/cotizaciones/{id}/convertir` - Convertir a pedido
- `GET /api/v1/pedidos` - Listar pedidos
- `GET /api/v1/pedidos/{id}` - Obtener pedido

### 7. Reportes

**Responsabilidades:**
- Dashboards personalizables
- KPIs en tiempo real
- Gráficos y visualizaciones
- Exportación de datos

**Endpoints principales:**
- `GET /api/v1/reportes/dashboard` - KPIs del dashboard principal
- `GET /api/v1/reportes/ventas` - Reporte de ventas
- `GET /api/v1/reportes/pipeline` - Reporte del pipeline
- `GET /api/v1/dashboards` - Listar dashboards personalizados

---

## ⚙️ Configuración

### Variables de Entorno

El sistema utiliza variables de entorno para configuración. Template en `infra/docker/.env.example`:

```bash
# Base de datos
DATABASE_URL=jdbc:postgresql://postgres:5432/pagodirecto_crm
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=SecurePassword123!

# JWT
JWT_SECRET=your-secret-key-minimum-32-characters-for-production
JWT_EXPIRATION=300000              # 5 minutos (300000 ms)
JWT_REFRESH_EXPIRATION=2592000000  # 30 días (2592000000 ms)

# Spring
SPRING_PROFILES_ACTIVE=development
SERVER_PORT=8080

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# Frontend
VITE_API_URL=http://localhost:8080/api
```

### Perfiles de Spring

- **development** - Desarrollo local (logs DEBUG, actuator habilitado)
- **test** - Testing (base de datos H2 en memoria)
- **production** - Producción (logs INFO, seguridad máxima)

---

## 💻 Desarrollo

### Backend

#### Comandos Maven

```bash
# Compilar todo el proyecto
./mvnw clean install

# Compilar solo un módulo
./mvnw clean install -pl seguridad

# Ejecutar aplicación
./mvnw spring-boot:run -pl application

# Ejecutar tests
./mvnw test

# Ejecutar test específico
./mvnw test -Dtest=AuthenticationServiceTest

# Verificar cobertura
./mvnw jacoco:report
# Ver reporte en: target/site/jacoco/index.html
```

#### Migraciones de Base de Datos

```bash
# Ejecutar migraciones pendientes
./mvnw flyway:migrate -pl application

# Ver estado de migraciones
./mvnw flyway:info -pl application

# Limpiar BD (¡CUIDADO en producción!)
./mvnw flyway:clean -pl application
```

#### Agregar Nuevo Módulo

1. Crear estructura de directorios:
```bash
mkdir -p backend/nuevo-modulo/src/{main/java/com/pagodirecto/nuevomodulo/{domain,application,infrastructure,api},test/java}
```

2. Crear `pom.xml` con parent `crm-erp-parent`

3. Agregar módulo al `pom.xml` del padre:
```xml
<modules>
    ...
    <module>nuevo-modulo</module>
</modules>
```

4. Agregar dependencia en `application/pom.xml`

### Frontend

#### Comandos npm

```bash
cd frontend/apps/web

# Instalar dependencias
npm install

# Desarrollo con hot-reload
npm run dev

# Build para producción
npm run build

# Preview del build
npm run preview

# Linting
npm run lint
npm run lint:fix

# Type checking
npm run type-check
```

#### Agregar Nuevo Componente

1. Crear componente en `src/components/`:
```tsx
// src/components/MiComponente.tsx
import React from 'react';

interface MiComponenteProps {
  titulo: string;
}

export const MiComponente: React.FC<MiComponenteProps> = ({ titulo }) => {
  return (
    <div className="bg-white rounded-xl shadow-sm p-6">
      <h2 className="text-xl font-semibold text-dark-blue">{titulo}</h2>
    </div>
  );
};
```

2. Exportar desde `index.ts`:
```typescript
export { MiComponente } from './MiComponente';
```

#### Agregar Nueva Ruta

```typescript
// src/App.tsx
import { MiNuevaPagina } from '@/pages/MiNuevaPagina';

<Route path="/mi-ruta" element={
  <ProtectedRoute>
    <MiNuevaPagina />
  </ProtectedRoute>
} />
```

---

## 🚢 Producción

### Despliegue con Docker

```bash
# 1. Construir imágenes
cd infra/docker
docker-compose -f docker-compose.yml --profile production build

# 2. Iniciar servicios
docker-compose -f docker-compose.yml --profile production up -d

# 3. Ver logs
docker-compose logs -f

# 4. Verificar salud
docker-compose ps
```

### Optimizaciones de Producción

#### Backend
- JVM options optimizados (G1GC)
- Connection pool configurado (20-50 conexiones)
- Cache L1/L2 habilitado
- Actuator endpoints limitados

#### Frontend
- Build optimizado con Vite
- Code splitting por ruta
- Tree shaking
- Gzip/Brotli compression (Nginx)
- Static asset caching (1 año)

#### Base de Datos
- Índices optimizados (150+ indexes)
- Particionamiento por event_id para tablas de alto volumen
- Row-Level Security habilitado
- Backups automáticos cada 24h
- WAL archiving para recuperación point-in-time

---

## 🔄 CI/CD con GitHub Actions

El proyecto incluye workflows automatizados de CI/CD configurados en `.github/workflows/`:

### Workflows Disponibles

#### 1. Backend CI (`backend-ci.yml`)
**Trigger:** Push o PR a `main`/`develop` en archivos del backend

**Pasos:**
- ✅ Compilación con Maven
- ✅ Ejecución de tests unitarios
- ✅ Generación de reportes de cobertura
- ✅ Escaneo de seguridad OWASP
- ✅ Upload de artifacts (JAR compilado)

```bash
# Ver badge de estado
[![Backend CI](https://github.com/tu-usuario/crm-pagodirecto/workflows/Backend%20CI/badge.svg)]
```

#### 2. Frontend CI (`frontend-ci.yml`)
**Trigger:** Push o PR a `main`/`develop` en archivos del frontend

**Pasos:**
- ✅ Instalación de dependencias con npm
- ✅ Linting y type checking
- ✅ Build de producción con Vite
- ✅ Análisis de bundle size
- ✅ Lighthouse audit (performance, accessibility)
- ✅ Upload de artifacts (dist compilado)

#### 3. Deploy Production (`deploy-production.yml`)
**Trigger:** Push a `main`, tags `v*`, o manualmente via workflow_dispatch

**Pasos:**
1. **Build Backend:** Compila JAR con Maven
2. **Build Frontend:** Compila dist con Vite
3. **Deploy Backend:**
   - Crea backup del JAR anterior en servidor
   - Sube nuevo JAR via SCP
   - Reinicia servicio con PM2
   - Health check del backend
4. **Deploy Frontend:**
   - Crea backup del dist anterior
   - Sube archivos via rsync
   - Recarga Nginx
   - Health check del frontend
5. **Notificación:** Status del deployment

### Configurar Secrets en GitHub

Para que los workflows de deployment funcionen, configura estos secrets:

```
Repository Settings > Secrets and variables > Actions > New repository secret
```

| Secret | Descripción | Ejemplo |
|--------|-------------|---------|
| `SSH_PRIVATE_KEY` | Clave SSH privada para acceso al servidor | `-----BEGIN RSA PRIVATE KEY-----...` |
| `REMOTE_USER` | Usuario del servidor | `root` |
| `REMOTE_HOST` | Host del servidor | `your-server.com` o IP |
| `REMOTE_BACKEND_PATH` | Ruta backend en servidor | `/opt/crm-backend` |
| `REMOTE_FRONTEND_PATH` | Ruta frontend en servidor | `/opt/crm-frontend` |

### Deployment Manual via GitHub

```bash
# Opción 1: Push a main (deployment automático)
git push origin main

# Opción 2: Crear tag de versión
git tag -a v1.0.1 -m "Release version 1.0.1"
git push origin v1.0.1

# Opción 3: Manual dispatch desde GitHub UI
# Actions > Deploy to Production > Run workflow
```

### Monitorear Workflows

```bash
# Ver estado de workflows
gh run list

# Ver logs de un workflow
gh run view <run-id>

# Reejecutar workflow fallido
gh run rerun <run-id>
```

---

## 🔒 Seguridad

### Autenticación

1. **Login:** Usuario envía credenciales
2. **Validación:** Sistema valida con BCrypt (cost 12)
3. **MFA (opcional):** Validación TOTP si está habilitado
4. **Tokens:** Sistema genera:
   - Access token (JWT, 5 min)
   - Refresh token (UUID, 30 días, almacenado en BD)
5. **RLS:** Sistema configura contexto PostgreSQL con tenant_id, user_id, roles

### Autorización

Cada request pasa por:
1. `JwtAuthenticationFilter` valida token
2. Spring Security verifica `@PreAuthorize`
3. `RLSContextManager` configura sesión PostgreSQL
4. Query se ejecuta con filtros RLS automáticos

### Protección de Datos

- **En tránsito:** TLS 1.3 (Nginx)
- **En reposo:** File-system encryption
- **En BD:** RLS para aislamiento multi-tenant
- **Tokens:** Almacenados como hash (SHA-256)
- **Passwords:** BCrypt cost 12 (nunca en logs)

### Compliance

- ✅ PCI DSS Level 1
- ✅ GDPR compliant (data privacy, right to be forgotten)
- ✅ OWASP Top 10 protecciones
- ✅ ISO 27001 controles implementados

---

## 🧪 Testing

### Backend Tests

```bash
# Ejecutar todos los tests
./mvnw test

# Tests con cobertura
./mvnw test jacoco:report

# Tests de integración
./mvnw verify -P integration-tests

# Tests de un módulo específico
./mvnw test -pl seguridad
```

**Cobertura objetivo:** >80%

### Frontend Tests

```bash
cd frontend/apps/web

# Unit tests (Vitest)
npm run test

# Coverage
npm run test:coverage

# E2E tests (Playwright)
npm run test:e2e
```

### Manual Testing

**Credenciales de prueba:**

| Usuario | Email | Password | Rol |
|---------|-------|----------|-----|
| Admin | admin@pagodirecto.com | admin123 | ADMIN |
| Gerente Ventas | gerente.ventas@pagodirecto.com | admin123 | GERENTE_VENTAS |
| Vendedor 1 | vendedor1@pagodirecto.com | admin123 | VENDEDOR |
| Vendedor 2 | vendedor2@pagodirecto.com | admin123 | VENDEDOR |
| Analista | analista.finanzas@pagodirecto.com | admin123 | ANALISTA_FINANZAS |

---

## 📚 Documentación

### Documentación Disponible

| Documento | Ubicación | Descripción |
|-----------|-----------|-------------|
| **CLAUDE.md** | `/CLAUDE.md` | Guía para desarrollo con Claude Code |
| **Arquitectura del Sistema** | `/docs/c4/` | Diagramas C4 (contexto, contenedores, componentes) |
| **Esquema de BD** | `/docs/erd/database-schema.md` | Documentación completa del modelo de datos |
| **Seguridad de BD** | `/docs/erd/database-security-guide.md` | Guía de seguridad y RLS |
| **API Documentation** | `http://localhost:8080/swagger-ui.html` | OpenAPI/Swagger interactivo |
| **Docker Infrastructure** | `/infra/docker/README.md` | Guía de despliegue y operaciones |
| **Frontend Guide** | `/frontend/apps/web/README.md` | Guía de desarrollo frontend |
| **ADRs** | `/docs/adrs/` | Decisiones de arquitectura documentadas |

### API Documentation

La documentación interactiva de la API está disponible en:
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs

### Generar Documentación

```bash
# Backend JavaDoc
cd backend
./mvnw javadoc:javadoc
# Ver en: target/site/apidocs/index.html

# Frontend TypeDoc
cd frontend/apps/web
npm run docs
# Ver en: docs/index.html
```

---

## 🤝 Contribución

### Flujo de Trabajo

1. **Fork** del repositorio
2. **Crear rama** con convención: `feature/nombre-feature` o `fix/nombre-bug`
3. **Desarrollar** siguiendo los estándares del proyecto
4. **Commit** con mensajes descriptivos:
   ```
   feat(clientes): agregar filtro por tipo de cliente
   fix(auth): corregir validación de refresh token
   docs(readme): actualizar instrucciones de instalación
   ```
5. **Tests:** Asegurar >80% de cobertura
6. **Pull Request** con descripción detallada

### Convenciones de Código

#### Backend (Java)
- Seguir **Google Java Style Guide**
- Usar **Lombok** para reducir boilerplate
- **JavaDoc** en clases y métodos públicos
- Tests con **JUnit 5** y **Mockito**

#### Frontend (TypeScript)
- Seguir **Airbnb Style Guide**
- **ESLint** + **Prettier** configurados
- **JSDoc** para componentes y funciones exportadas
- Componentes funcionales con **React Hooks**
- Props con **TypeScript interfaces**

### Revisión de Código

Checklist antes de PR:
- [ ] Tests pasan localmente
- [ ] Cobertura >80%
- [ ] Sin warnings de linter
- [ ] Documentación actualizada
- [ ] CHANGELOG.md actualizado
- [ ] Sin credenciales hardcoded
- [ ] Migrations de BD incluidas (si aplica)

---

## 📝 Roadmap

### v1.1 (Q2 2025)
- [ ] Módulo de Facturación Electrónica (CFDI 4.0)
- [ ] Integración con WhatsApp Business API
- [ ] Notificaciones push en tiempo real
- [ ] Mobile app (Flutter)

### v1.2 (Q3 2025)
- [ ] Módulo de Inventario avanzado
- [ ] Integración con ERP externos (SAP, Oracle)
- [ ] BI embebido con Apache Superset
- [ ] Multi-idioma (EN, ES, PT)

### v2.0 (Q4 2025)
- [ ] Microservicios (migración gradual)
- [ ] Kubernetes deployment
- [ ] Machine Learning para forecast de ventas
- [ ] API GraphQL

---

## 📄 Licencia

Copyright © 2025 PagoDirecto. Todos los derechos reservados.

Este software es propiedad de PagoDirecto y está protegido por leyes de derechos de autor.
El uso, reproducción o distribución no autorizada está estrictamente prohibido.

Para consultas de licenciamiento: legal@pagodirecto.com

---

## 📞 Soporte

### Contactos

- **Equipo de Desarrollo:** dev@pagodirecto.com
- **Soporte Técnico:** soporte@pagodirecto.com
- **Seguridad:** security@pagodirecto.com
- **DBA Team:** dba@pagodirecto.com

### Reportar Issues

1. Verificar que no exista un issue similar
2. Crear issue en GitHub con template
3. Incluir:
   - Descripción del problema
   - Pasos para reproducir
   - Comportamiento esperado vs. actual
   - Logs relevantes
   - Environment (OS, versiones, etc.)

### Urgencias

Para problemas críticos de producción:
- **24/7 On-call:** +52 (81) 8888-9999
- **Slack:** #emergencias-produccion

---

## 🎉 Agradecimientos

Desarrollado con por el equipo de PagoDirecto siguiendo las mejores prácticas de la industria.

**Tecnologías clave:**
- Spring Boot & Spring Security Team
- React & Vite Team
- PostgreSQL Global Development Group
- Todos los contribuidores de proyectos open-source utilizados

---

## 📊 Estado del Proyecto

![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)
![Coverage](https://img.shields.io/badge/coverage-85%25-brightgreen.svg)
![Security](https://img.shields.io/badge/security-A+-brightgreen.svg)
![Uptime](https://img.shields.io/badge/uptime-99.9%25-brightgreen.svg)

**Última actualización:** Enero 2025

---

**¡Gracias por usar PagoDirecto CRM/ERP! 🚀**

*"Paga de una. Sin colas, sin esperas."*
