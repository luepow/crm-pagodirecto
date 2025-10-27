/**
 * Sidebar Component
 *
 * Main navigation sidebar with menu items.
 * Follows BrandBook 2024 design guidelines.
 */

import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  Users,
  Target,
  CheckSquare,
  Package,
  ShoppingCart,
  BarChart3,
  Settings,
  ChevronLeft,
  Shield,
  FileText,
  DollarSign,
} from 'lucide-react';
import { cn } from '@shared-ui/utils';
import { useAuthStore } from '../../lib/stores/authStore';

type UserRole = 'ADMIN' | 'MANAGER' | 'SALES_REP' | 'USER';

interface NavItem {
  label: string;
  icon: React.ReactNode;
  href: string;
  badge?: number;
  comingSoon?: boolean;
  roles?: UserRole[]; // Roles que pueden ver este menú. Si no se especifica, todos lo ven
}

const navItems: NavItem[] = [
  {
    label: 'Dashboard',
    icon: <LayoutDashboard size={20} />,
    href: '/dashboard',
    // Todos los roles pueden ver el dashboard
  },
  {
    label: 'Clientes',
    icon: <Users size={20} />,
    href: '/clientes',
    roles: ['ADMIN', 'MANAGER', 'SALES_REP'], // USER no puede gestionar clientes
  },
  {
    label: 'Oportunidades',
    icon: <Target size={20} />,
    href: '/oportunidades',
    roles: ['ADMIN', 'MANAGER', 'SALES_REP'],
  },
  {
    label: 'Tareas',
    icon: <CheckSquare size={20} />,
    href: '/tareas',
    // Todos pueden ver tareas
  },
  {
    label: 'Productos',
    icon: <Package size={20} />,
    href: '/productos',
    roles: ['ADMIN', 'MANAGER'],
  },
  {
    label: 'Ventas',
    icon: <ShoppingCart size={20} />,
    href: '/ventas',
    roles: ['ADMIN', 'MANAGER', 'SALES_REP'],
    comingSoon: true,
  },
  {
    label: 'Pagos',
    icon: <DollarSign size={20} />,
    href: '/pagos',
    roles: ['ADMIN', 'MANAGER'],
    comingSoon: true,
  },
  {
    label: 'Reportes',
    icon: <BarChart3 size={20} />,
    href: '/reportes',
    roles: ['ADMIN', 'MANAGER'],
    comingSoon: true,
  },
  {
    label: 'Contratos',
    icon: <FileText size={20} />,
    href: '/contratos',
    roles: ['ADMIN', 'MANAGER'],
    comingSoon: true,
  },
  {
    label: 'Seguridad',
    icon: <Shield size={20} />,
    href: '/seguridad',
    roles: ['ADMIN'], // Solo ADMIN puede acceder a gestión de usuarios/roles/permisos
  },
];

interface SidebarProps {
  isOpen: boolean;
  onToggle: () => void;
}

/**
 * Sidebar navigation component
 */
export const Sidebar: React.FC<SidebarProps> = ({ isOpen, onToggle }) => {
  const location = useLocation();
  const { user } = useAuthStore();

  // Filtrar menús según el rol del usuario
  const visibleNavItems = React.useMemo(() => {
    if (!user) return navItems;

    // ADMIN siempre ve todos los menús
    if (user.rol === 'ADMIN') {
      return navItems;
    }

    // Para otros roles, filtrar según permisos
    return navItems.filter(item => {
      // Si no tiene roles definidos, todos pueden verlo
      if (!item.roles || item.roles.length === 0) {
        return true;
      }
      // Verificar si el rol del usuario está en la lista de roles permitidos
      return item.roles.includes(user.rol);
    });
  }, [user]);

  return (
    <>
      {/* Mobile overlay */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-secondary/50 backdrop-blur-sm lg:hidden"
          onClick={onToggle}
        />
      )}

      {/* Sidebar */}
      <aside
        className={cn(
          'fixed left-0 top-0 z-50 h-screen w-64 transform border-r border-gray-200 bg-white transition-transform duration-300 lg:sticky lg:translate-x-0',
          isOpen ? 'translate-x-0' : '-translate-x-full'
        )}
      >
        {/* Logo */}
        <div className="flex h-16 items-center justify-between border-b border-gray-200 px-6">
          <Link to="/dashboard" className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-primary">
              <span className="text-xl font-bold text-white">PD</span>
            </div>
            <div>
              <h1 className="text-lg font-semibold text-secondary">PagoDirecto</h1>
              <p className="text-xs text-text-secondary">CRM/ERP</p>
            </div>
          </Link>

          <button
            onClick={onToggle}
            className="rounded-lg p-1 hover:bg-gray-100 lg:hidden"
            aria-label="Cerrar menú"
          >
            <ChevronLeft size={20} />
          </button>
        </div>

        {/* Navigation */}
        <nav className="flex-1 space-y-1 overflow-y-auto p-4">
          {visibleNavItems.map((item) => {
            const isActive = location.pathname === item.href;

            return (
              <Link
                key={item.href}
                to={item.href}
                className={cn(
                  'flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium transition-smooth',
                  isActive
                    ? 'bg-primary text-white shadow-sm'
                    : 'text-text-secondary hover:bg-gray-100 hover:text-text-primary'
                )}
              >
                {item.icon}
                <span>{item.label}</span>
                {item.comingSoon && (
                  <span
                    className={cn(
                      'ml-auto rounded-full px-2 py-0.5 text-xs font-semibold',
                      isActive
                        ? 'bg-yellow-400/30 text-white'
                        : 'bg-yellow-100 text-yellow-700'
                    )}
                  >
                    Próximamente
                  </span>
                )}
                {item.badge !== undefined && item.badge > 0 && (
                  <span
                    className={cn(
                      'ml-auto rounded-full px-2 py-0.5 text-xs font-semibold',
                      isActive
                        ? 'bg-white/20 text-white'
                        : 'bg-primary/10 text-primary'
                    )}
                  >
                    {item.badge}
                  </span>
                )}
              </Link>
            );
          })}
        </nav>

        {/* Footer */}
        <div className="border-t border-gray-200 p-4">
          <Link
            to="/settings"
            className="flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium text-text-secondary transition-smooth hover:bg-gray-100 hover:text-text-primary"
          >
            <Settings size={20} />
            <span>Configuración</span>
          </Link>
        </div>
      </aside>
    </>
  );
};
