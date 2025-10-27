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
} from 'lucide-react';
import { cn } from '@shared-ui/utils';

interface NavItem {
  label: string;
  icon: React.ReactNode;
  href: string;
  badge?: number;
  comingSoon?: boolean;
}

const navItems: NavItem[] = [
  {
    label: 'Dashboard',
    icon: <LayoutDashboard size={20} />,
    href: '/dashboard',
  },
  {
    label: 'Clientes',
    icon: <Users size={20} />,
    href: '/clientes',
  },
  {
    label: 'Oportunidades',
    icon: <Target size={20} />,
    href: '/oportunidades',
  },
  {
    label: 'Tareas',
    icon: <CheckSquare size={20} />,
    href: '/tareas',
  },
  {
    label: 'Productos',
    icon: <Package size={20} />,
    href: '/productos',
  },
  {
    label: 'Ventas',
    icon: <ShoppingCart size={20} />,
    href: '/ventas',
    comingSoon: true,
  },
  {
    label: 'Reportes',
    icon: <BarChart3 size={20} />,
    href: '/reportes',
    comingSoon: true,
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
          {navItems.map((item) => {
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
