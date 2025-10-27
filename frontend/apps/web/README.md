# PagoDirecto CRM/ERP - Frontend

Modern, enterprise-grade CRM/ERP frontend application built with React, TypeScript, and TailwindCSS following the BrandBook 2024 design system.

## Technology Stack

### Core
- **React 18.3** - Modern UI library with concurrent features
- **TypeScript 5.6** - Type-safe development
- **Vite 5.4** - Fast build tool and dev server

### Styling & Design
- **TailwindCSS 3.4** - Utility-first CSS framework
- **BrandBook 2024 Design Tokens** - Custom color palette, typography, and spacing
- **Outfit Font** - Primary typeface (Light 300, Medium 500, SemiBold 600)
- **Lucide React** - Modern icon library

### State Management & Data
- **Zustand 4.5** - Lightweight state management
- **React Query 5.56** - Server state and data fetching
- **Axios 1.7** - HTTP client with interceptors

### Forms & Validation
- **React Hook Form 7.53** - Performant form library
- **Zod 3.23** - TypeScript-first schema validation

### Charts & Visualization
- **Recharts 2.12** - Composable charting library

### Routing
- **React Router 6.26** - Client-side routing with protected routes

### Developer Experience
- **ESLint** - Code linting
- **Prettier** - Code formatting
- **TypeScript Strict Mode** - Maximum type safety

## Project Structure

```
frontend/apps/web/
├── src/
│   ├── components/         # Reusable components
│   │   ├── auth/          # Authentication components
│   │   └── layout/        # Layout components (Sidebar, Header, MainLayout)
│   ├── lib/               # Utilities and libraries
│   │   ├── api/          # API client and endpoints
│   │   └── stores/       # Zustand stores
│   ├── pages/            # Page components
│   │   ├── auth/         # Login, Register
│   │   ├── dashboard/    # Dashboard with KPIs and charts
│   │   ├── clientes/     # Customer management
│   │   ├── oportunidades/# Opportunities pipeline
│   │   └── tareas/       # Task management
│   ├── App.tsx           # Main app component with routing
│   ├── main.tsx          # Application entry point
│   └── index.css         # Global styles and Tailwind imports
├── design-tokens/        # Design system tokens
│   ├── colors.ts         # Color palette
│   ├── typography.ts     # Font families and sizes
│   ├── spacing.ts        # Spacing and border radius
│   └── animations.ts     # Animation keyframes
├── shared-ui/            # Shared component library
│   ├── components/       # Reusable UI components
│   └── utils/           # Utility functions
├── package.json
├── tsconfig.json
├── vite.config.ts
├── tailwind.config.js
└── README.md
```

## Getting Started

### Prerequisites

- **Node.js** >= 18.0.0
- **npm** >= 9.0.0 (or pnpm/yarn)

### Installation

1. Navigate to the web app directory:
```bash
cd frontend/apps/web
```

2. Install dependencies:
```bash
npm install
```

3. Create environment file:
```bash
cp .env.example .env
```

4. Update environment variables in `.env`:
```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_ENV=development
```

### Development

Start the development server:
```bash
npm run dev
```

The application will be available at `http://localhost:3000`

### Building for Production

Build the application:
```bash
npm run build
```

Preview production build:
```bash
npm run preview
```

### Code Quality

Run linter:
```bash
npm run lint
```

Fix linting issues:
```bash
npm run lint:fix
```

Format code:
```bash
npm run format
```

Type check:
```bash
npm run type-check
```

## Design System

### BrandBook 2024 Guidelines

#### Colors

**Primary Brand Colors:**
- Primary (Magenta): `#FF2463`
- Secondary (Dark Blue): `#050B26`
- Background: `#FFFFFF`

**Vertical-Specific Accents:**
- Seguros (Insurance): `#0066FF`
- Viajes (Travel): `#8B5CF6`
- Servicios (Services): `#10B981`

**Functional Colors:**
- Success: `#10B981`
- Warning: `#F59E0B`
- Error: `#EF4444`
- Info: `#0066FF`

#### Typography

**Font Family:** Outfit (Google Fonts)
- Light: 300
- Medium: 500
- SemiBold: 600

**Font Sizes:**
- xs: 12px
- sm: 14px
- base: 16px
- lg: 18px
- xl: 20px
- 2xl: 24px

#### Visual Style

- **Squircle Borders:** Use `rounded-xl` (16px) or `rounded-2xl` (20px)
- **Gradients:** Blue → Magenta for primary actions
- **Micro-animations:** Smooth transitions with 200ms duration
- **Glass Morphism:** Subtle backdrop blur for overlays

### Component Library

All components follow BrandBook 2024 design principles:

**Form Components:**
- `Button` - Primary, secondary, ghost, outline, danger variants
- `Input` - Text input with label, error states, and icons
- `Badge` - Status indicators and labels
- `Avatar` - User avatars with initials fallback

**Layout Components:**
- `Card` - Container with squircle borders and variants
- `Skeleton` - Loading placeholders

**Utility Functions:**
- `cn()` - Class name merger with Tailwind conflict resolution

## Authentication

The application uses JWT-based authentication:

1. **Login:** User provides email and password
2. **Token Storage:** Access and refresh tokens stored in localStorage
3. **Auto-Refresh:** Axios interceptor refreshes expired tokens
4. **Protected Routes:** Authentication check before rendering

### Test Credentials

```
Email: admin@pagodirecto.com
Password: admin123
```

## API Integration

### Configuration

API client is configured in `src/lib/api/client.ts`:
- Base URL: `http://localhost:8080/api`
- Timeout: 30 seconds
- Automatic JWT token injection
- Token refresh on 401 responses

### Making API Calls

Use React Query for data fetching:

```typescript
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/lib/api/client';

const { data, isLoading, error } = useQuery({
  queryKey: ['clientes'],
  queryFn: async () => {
    const response = await apiClient.get('/clientes');
    return response.data;
  },
});
```

## Features

### Dashboard
- KPI cards with trend indicators
- Line chart for sales over 6 months
- Bar chart for pipeline stages
- Recent activities feed
- Quick action buttons

### Clientes (Customers)
- List view with search and filters
- Customer status badges
- Company and contact information

### Oportunidades (Opportunities)
- Kanban board view for pipeline
- Drag-and-drop functionality (to be implemented)
- Stage-based organization

### Tareas (Tasks)
- Calendar view (to be implemented)
- List view with filters
- Priority and status indicators

## Performance Optimizations

### Code Splitting
- Automatic route-based code splitting
- Manual chunks for vendor libraries
- Lazy loading for heavy components

### Bundle Size
- Tree-shaking enabled
- Production builds optimized
- Source maps for debugging

### Caching Strategy
- React Query for server state caching (5 minutes)
- Zustand persist for auth state
- Service worker (to be implemented)

## Accessibility (WCAG 2.1 AA)

- Semantic HTML5 elements
- ARIA labels and roles
- Keyboard navigation support
- Focus management
- Color contrast ratio ≥ 4.5:1
- Screen reader optimization

## Browser Support

- Chrome/Edge (last 2 versions)
- Firefox (last 2 versions)
- Safari (last 2 versions)
- Mobile browsers (iOS Safari, Chrome Mobile)

## Deployment

### Environment Variables

Production environment variables:
```env
VITE_API_BASE_URL=https://api.pagodirecto.com/api
VITE_ENV=production
```

### Build Command
```bash
npm run build
```

Output directory: `dist/`

### Hosting Recommendations
- Vercel (recommended for Vite apps)
- Netlify
- AWS S3 + CloudFront
- Azure Static Web Apps

## Troubleshooting

### Common Issues

**Port 3000 already in use:**
```bash
# Change port in vite.config.ts or kill process:
lsof -ti:3000 | xargs kill -9
```

**TypeScript errors:**
```bash
# Clear TypeScript cache:
rm -rf node_modules/.vite
npm run type-check
```

**Build errors:**
```bash
# Clear cache and rebuild:
rm -rf dist node_modules/.vite
npm install
npm run build
```

## Contributing

1. Follow TypeScript strict mode rules
2. Use ESLint and Prettier configurations
3. Write JSDoc comments for components
4. Test accessibility with keyboard navigation
5. Ensure responsive design (mobile, tablet, desktop)

## License

© 2024 PagoDirecto. All rights reserved.

## Support

For technical questions or issues, contact the development team or refer to the project documentation in `/docs`.
