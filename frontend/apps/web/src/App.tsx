/**
 * App Component
 *
 * Main application component with routing and providers.
 */

import React, { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { Toaster } from 'sonner';
import { Bot } from 'lucide-react';

// Layouts
import { MainLayout } from './components/layout/MainLayout';

// Auth
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { LoginPage } from './pages/auth/LoginPage';

// Pages
import { DashboardPage } from './pages/dashboard/DashboardPage';
import { ClientesPage } from './pages/clientes/ClientesPage';
import { OportunidadesPage } from './pages/oportunidades/OportunidadesPage';
import { TareasPage } from './pages/tareas/TareasPage';
import { ProductosPage } from './pages/productos/ProductosPage';
import { VentasPage } from './pages/ventas/VentasPage';
import { ReportesPage } from './pages/reportes/ReportesPage';
import { ProfilePage } from './pages/profile/ProfilePage';
import { ConfiguracionPage } from './pages/configuracion/ConfiguracionPage';
import { UsuariosPage } from './pages/usuarios/UsuariosPage';
import { DepartamentosPage } from './pages/departamentos/DepartamentosPage';
import { RolesPage } from './pages/seguridad/RolesPage';
import { PermisosPage } from './pages/seguridad/PermisosPage';

// Features
import { AIChat } from './features/ai/components/AIChat';

/**
 * React Query client configuration
 */
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
      staleTime: 5 * 60 * 1000, // 5 minutes
    },
  },
});

/**
 * Main App component
 */
export const App: React.FC = () => {
  const [isChatOpen, setIsChatOpen] = useState(false);

  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          {/* Public routes */}
          <Route path="/login" element={<LoginPage />} />

          {/* Protected routes */}
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <MainLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="/dashboard" replace />} />
            <Route path="dashboard" element={<DashboardPage />} />
            <Route path="clientes" element={<ClientesPage />} />
            <Route path="oportunidades" element={<OportunidadesPage />} />
            <Route path="tareas" element={<TareasPage />} />

            {/* Profile route */}
            <Route path="profile" element={<ProfilePage />} />

            {/* Settings route */}
            <Route path="settings" element={<ConfiguracionPage />} />

            {/* Usuarios route */}
            <Route path="usuarios" element={<UsuariosPage />} />

            {/* Departamentos route */}
            <Route path="departamentos" element={<DepartamentosPage />} />

            {/* Seguridad routes */}
            <Route path="seguridad" element={<UsuariosPage />} />
            <Route path="roles" element={<RolesPage />} />
            <Route path="permisos" element={<PermisosPage />} />

            {/* Module routes */}
            <Route path="productos" element={<ProductosPage />} />
            <Route path="ventas" element={<VentasPage />} />
            <Route path="reportes" element={<ReportesPage />} />
          </Route>

          {/* 404 */}
          <Route
            path="*"
            element={
              <div className="flex min-h-screen items-center justify-center">
                <div className="text-center">
                  <h1 className="text-6xl font-bold text-primary">404</h1>
                  <p className="mt-4 text-xl text-text-secondary">Página no encontrada</p>
                </div>
              </div>
            }
          />
        </Routes>
      </BrowserRouter>

      {/* Toast notifications */}
      <Toaster
        position="top-right"
        expand={true}
        richColors
        closeButton
        duration={4000}
      />

      {/* AI Assistant Chat */}
      <AIChat isOpen={isChatOpen} onClose={() => setIsChatOpen(false)} />

      {/* Floating AI Chat Button */}
      {!isChatOpen && (
        <button
          onClick={() => setIsChatOpen(true)}
          className="fixed bottom-4 right-4 z-40 flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-r from-primary to-primary-600 text-white shadow-2xl transition-all hover:scale-110 hover:shadow-3xl focus:outline-none focus:ring-4 focus:ring-primary focus:ring-opacity-50"
          aria-label="Abrir asistente de IA"
        >
          <Bot size={28} />
        </button>
      )}

      {/* React Query devtools (development only) */}
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  );
};
