# Guía del Tema - PagoDirecto CRM

Esta guía documenta el sistema de diseño actualizado basado en el diseño de Figma "SaaS Real Estate".

## 🎨 Paleta de Colores

### Colores Primarios
El color primario principal es el **naranja** del logo (#FF7628):

```tsx
// Uso en componentes
<button className="bg-primary text-white">Botón Principal</button>
<div className="text-primary">Texto destacado</div>
```

**Variantes disponibles:**
- `primary-50` a `primary-900` - Escalas del naranja
- `primary` (DEFAULT) - `#FF7628`

### Colores de Texto
Los textos utilizan un marrón oscuro cálido en lugar del azul anterior:

```tsx
// Texto principal
<h1 className="text-text-primary">Título</h1>        // #2B1406

// Texto secundario (60% opacidad)
<p className="text-text-secondary">Descripción</p>   // rgba(43, 20, 6, 0.6)

// Texto terciario (50% opacidad)
<span className="text-text-tertiary">Label</span>    // rgba(43, 20, 6, 0.5)

// Texto inverso
<span className="text-text-inverse">Texto blanco</span> // #FFFFFF
```

### Colores de Acento
Nuevos colores de acento basados en el diseño de Figma:

```tsx
// Naranja (primario)
<div className="bg-accent-orange">...</div>           // #FF7628

// Amarillo-naranja
<div className="bg-accent-yellow">...</div>           // #FFA928

// Amarillo ámbar
<div className="bg-accent-amber">...</div>            // #FFD028
```

### Colores Funcionales

```tsx
// Éxito (verde)
<div className="text-success">+24%</div>              // #11A142
<div className="bg-success-light border-success-border">Badge éxito</div>

// Error (rojo)
<div className="text-error">-3%</div>                 // #FF1212
<div className="bg-error-light">Mensaje de error</div>

// Advertencia
<div className="bg-warning">Advertencia</div>         // #FFA928

// Info
<div className="bg-info-light">Información</div>      // #FF7628
```

### Bordes
```tsx
// Borde por defecto
<div className="border border-border">...</div>       // #E5E5E5

// Borde claro
<div className="border border-border-light">...</div> // #F0EEEE

// Borde oscuro (12% opacidad)
<div className="border border-border-dark">...</div>  // rgba(43, 20, 6, 0.12)
```

## 📝 Tipografía

### Fuentes
El diseño utiliza dos fuentes de Google Fonts:

1. **Inter** - Para títulos, números grandes y labels importantes
2. **Plus Jakarta Sans** - Para texto de cuerpo y labels secundarios

```tsx
// Inter para headings y números
<h1 className="font-sans text-lg">Dashboard</h1>
<span className="font-sans text-2xl">$125,839</span>

// Plus Jakarta Sans para body text
<p className="font-body text-sm">Menu</p>
<span className="font-body text-base">Descripción</span>

// O usando las clases de utilidad
<h1 className="font-heading text-lg">Título</h1>
<p className="font-body text-sm">Cuerpo de texto</p>
```

### Tamaños de Fuente

```tsx
// 12px - Labels pequeños, badges
<span className="text-xs">Last 7 days</span>

// 14px - Texto de cuerpo, menú items (MÁS COMÚN)
<p className="text-sm">Dashboard</p>

// 16px - Texto grande, inputs
<input className="text-base" />

// 18px - Títulos de página
<h2 className="text-lg">Dashboard</h2>

// 24px - Números grandes en tarjetas
<span className="text-2xl">746</span>

// 32px - Números muy grandes
<span className="text-3xl">1,783</span>
```

### Letter Spacing
El diseño de Figma usa tracking negativo ligero:

```tsx
// Tracking normal (-0.01em) - Por defecto
<p className="tracking-normal">Texto</p>

// Tracking tight (-0.02em) - Para títulos muy grandes
<h1 className="tracking-tight">Título</h1>
```

## 📐 Espaciado y Bordes

### Border Radius

```tsx
// 4px - Pequeños elementos
<div className="rounded-sm">...</div>

// 6px - Bordes por defecto (botones, inputs)
<button className="rounded">Botón</button>

// 8px - Tarjetas, contenedores
<div className="rounded-md">Card</div>

// 16px - Contenedores grandes
<div className="rounded-lg">Container</div>

// Pills (1000px)
<div className="rounded-full">Badge</div>
```

### Sombras

```tsx
// Sombra suave (cards)
<div className="shadow-sm">Card</div>            // 0 2px 4px rgba(0,0,0,0.04)

// Sombra por defecto
<div className="shadow">Elevated</div>           // 0 2px 8px rgba(0,0,0,0.14)

// Sombra media
<div className="shadow-md">Modal</div>

// Sombra grande
<div className="shadow-lg">Dropdown</div>
```

## 🎯 Componentes Comunes

### Botón Primario
```tsx
<button className="bg-primary hover:bg-primary-600 text-white font-sans text-xs px-3 py-2.5 rounded-md border-0.5 border-white shadow-sm transition-smooth">
  List New Property
</button>
```

### Botón Secundario
```tsx
<button className="bg-white hover:bg-gray-50 text-text-primary font-sans text-xs px-3 py-2.5 rounded-md border border-gray-200 shadow-sm transition-smooth">
  Export
</button>
```

### Card/Contenedor
```tsx
<div className="bg-white rounded-md border border-gray-200 p-4.5">
  {/* Contenido */}
</div>
```

### Menu Item (Activo)
```tsx
<div className="bg-secondary-50 text-text-primary font-body text-sm px-2.5 py-2 rounded-md flex items-center gap-2">
  <Icon className="w-4 h-4" />
  <span>Dashboard</span>
</div>
```

### Menu Item (Inactivo)
```tsx
<div className="text-text-secondary hover:bg-secondary-50 font-body text-sm px-2.5 py-2 rounded-md flex items-center gap-2 transition-smooth">
  <Icon className="w-4 h-4" />
  <span>New Listings</span>
</div>
```

### Stat Card
```tsx
<div className="bg-white rounded-md border border-gray-200 p-4.5">
  <div className="flex items-center justify-between mb-6">
    <p className="font-body text-base text-text-primary">Total Sales</p>
    <button className="w-4 h-4">•••</button>
  </div>

  <h3 className="font-sans text-2xl text-text-primary mb-3">$125,839</h3>

  <div className="flex items-center gap-1">
    <div className="flex items-center gap-0.5 text-success font-body text-sm">
      <Icon className="w-4 h-4" />
      <span>2%</span>
    </div>
    <span className="font-body text-sm text-text-secondary">last month</span>
  </div>
</div>
```

### Badge de Cambio (Positivo)
```tsx
<div className="bg-success-light border border-success-border rounded-full px-1 py-0.5 flex items-center gap-0.5">
  <Icon className="w-4 h-4 text-success" />
  <span className="font-body text-sm text-success">24%</span>
</div>
```

### Badge de Cambio (Negativo)
```tsx
<div className="bg-error-light border border-error rounded-full px-1 py-0.5 flex items-center gap-0.5">
  <Icon className="w-4 h-4 text-error transform scale-y-[-1]" />
  <span className="font-body text-sm text-error">3%</span>
</div>
```

### Input de Búsqueda
```tsx
<div className="bg-white border border-gray-200 rounded-md p-2.5 flex items-center justify-between">
  <div className="flex items-center gap-2">
    <SearchIcon className="w-6 h-6" />
    <input
      type="text"
      placeholder="Search"
      className="font-body text-base text-text-tertiary border-none outline-none"
    />
  </div>
</div>
```

### Selector de Período
```tsx
<div className="bg-white border border-gray-200 rounded-full px-2.5 py-2 flex items-center gap-1">
  <span className="font-sans text-xs text-text-secondary">Sales Overview</span>
  <Icon className="w-3 h-3" />
</div>
```

## 🔄 Migración desde el Tema Anterior

### Cambios de Colores
```tsx
// ANTES
className="bg-primary"        // #FF2463 (magenta)
className="text-secondary"    // #050B26 (azul oscuro)

// AHORA
className="bg-primary"        // #FF7628 (naranja)
className="text-secondary"    // #2B1406 (marrón oscuro)
```

### Cambios de Fuentes
```tsx
// ANTES
className="font-sans"         // 'Outfit'

// AHORA
className="font-sans"         // 'Inter' (para títulos y números)
className="font-body"         // 'Plus Jakarta Sans' (para cuerpo)
```

### Cambios de Border Radius
```tsx
// ANTES
className="rounded-2xl"       // 32px (squircle)

// AHORA
className="rounded-lg"        // 16px (más sutil)
className="rounded-md"        // 8px (default para cards)
```

## 📱 Responsive y Accesibilidad

El tema mantiene las mismas utilidades de accesibilidad:

```tsx
// Focus visible
<button className="focus-visible:ring-2 focus-visible:ring-primary">
  Botón
</button>

// Selección de texto
// Automático con la clase ::selection que usa bg-primary

// Scrollbar personalizado
// Automático en toda la aplicación
```

## 🚀 Próximos Pasos

Para aplicar el nuevo tema en tus componentes:

1. **Reemplaza los colores**: Cambia de `#FF2463` a `#FF7628`
2. **Actualiza las fuentes**: Usa `font-sans` para números/títulos y `font-body` para texto
3. **Ajusta los border-radius**: Usa `rounded-md` en lugar de `rounded-2xl`
4. **Revisa los textos**: Cambia a `text-text-primary` para el color marrón
5. **Actualiza los gradientes**: Usa el nuevo gradiente `gradient-primary`

## 📚 Recursos

- **Figma Design**: SaaS Real Estate Dashboard
- **Fuentes**:
  - [Inter en Google Fonts](https://fonts.google.com/specimen/Inter)
  - [Plus Jakarta Sans en Google Fonts](https://fonts.google.com/specimen/Plus+Jakarta+Sans)
- **Color Primario**: `#FF7628` (del logo actual)
- **Design Tokens**: `/frontend/design-tokens/`

---

**Nota**: Este tema está optimizado para un dashboard de CRM/ERP con un estilo cálido, profesional y moderno basado en el naranja del logo de PagoDirecto.
