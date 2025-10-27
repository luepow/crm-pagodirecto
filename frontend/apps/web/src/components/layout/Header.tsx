/**
 * Header Component
 *
 * Top navigation bar with search, notifications, and user menu.
 * Follows BrandBook 2024 design guidelines.
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Menu, Search, Bell, LogOut, User as UserIcon } from 'lucide-react';
import { Avatar } from '@shared-ui/components/Avatar';
import { Badge } from '@shared-ui/components/Badge';
import { useAuthStore } from '../../lib/stores/authStore';

interface HeaderProps {
  onMenuToggle: () => void;
}

/**
 * Header component
 */
export const Header: React.FC<HeaderProps> = ({ onMenuToggle }) => {
  const navigate = useNavigate();
  const { user, clearAuth } = useAuthStore();
  const [showUserMenu, setShowUserMenu] = React.useState(false);

  const handleLogout = () => {
    clearAuth();
    navigate('/login');
  };

  const getInitials = (nombre?: string, apellido?: string) => {
    if (!nombre || !apellido) return 'U';
    return `${nombre.charAt(0)}${apellido.charAt(0)}`.toUpperCase();
  };

  return (
    <header className="sticky top-0 z-30 border-b border-gray-200 bg-white/80 backdrop-blur-lg">
      <div className="flex h-16 items-center justify-between px-4 lg:px-6">
        {/* Left side */}
        <div className="flex items-center gap-4">
          <button
            onClick={onMenuToggle}
            className="rounded-lg p-2 hover:bg-gray-100 lg:hidden"
            aria-label="Abrir menú"
          >
            <Menu size={20} />
          </button>

          {/* Search bar */}
          <div className="relative hidden md:block">
            <Search
              size={20}
              className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
            />
            <input
              type="search"
              placeholder="Buscar clientes, oportunidades..."
              className="h-10 w-80 rounded-xl border border-gray-200 bg-gray-50 pl-10 pr-4 text-sm transition-smooth placeholder:text-gray-400 focus:border-primary focus:bg-white focus:outline-none focus:ring-2 focus:ring-primary/20"
            />
          </div>
        </div>

        {/* Right side */}
        <div className="flex items-center gap-3">
          {/* Notifications */}
          <button
            className="relative rounded-xl p-2 hover:bg-gray-100"
            aria-label="Notificaciones"
          >
            <Bell size={20} />
            <span className="absolute right-1 top-1 flex h-2 w-2">
              <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-primary opacity-75"></span>
              <span className="relative inline-flex h-2 w-2 rounded-full bg-primary"></span>
            </span>
          </button>

          {/* User menu */}
          <div className="relative">
            <button
              onClick={() => setShowUserMenu(!showUserMenu)}
              className="flex items-center gap-3 rounded-xl p-2 hover:bg-gray-100"
            >
              <Avatar
                initials={user ? getInitials(user.nombre, user.apellido) : 'U'}
                size="sm"
              />
              <div className="hidden text-left lg:block">
                <p className="text-sm font-medium text-text-primary">
                  {user?.nombre} {user?.apellido}
                </p>
                <Badge variant="primary" size="sm" className="mt-0.5">
                  {user?.rol}
                </Badge>
              </div>
            </button>

            {/* User dropdown menu */}
            {showUserMenu && (
              <>
                <div
                  className="fixed inset-0 z-40"
                  onClick={() => setShowUserMenu(false)}
                />
                <div className="absolute right-0 top-full z-50 mt-2 w-64 rounded-2xl border border-gray-200 bg-white p-2 shadow-xl animate-scale-in">
                  {/* User info */}
                  <div className="border-b border-gray-100 px-4 py-3">
                    <p className="font-medium text-text-primary">
                      {user?.nombre} {user?.apellido}
                    </p>
                    <p className="mt-0.5 text-sm text-text-secondary">{user?.email}</p>
                  </div>

                  {/* Menu items */}
                  <div className="py-2">
                    <button
                      onClick={() => {
                        setShowUserMenu(false);
                        navigate('/profile');
                      }}
                      className="flex w-full items-center gap-3 rounded-xl px-4 py-2.5 text-sm font-medium text-text-secondary transition-smooth hover:bg-gray-100 hover:text-text-primary"
                    >
                      <UserIcon size={18} />
                      <span>Mi Perfil</span>
                    </button>

                    <button
                      onClick={handleLogout}
                      className="flex w-full items-center gap-3 rounded-xl px-4 py-2.5 text-sm font-medium text-error transition-smooth hover:bg-error/5"
                    >
                      <LogOut size={18} />
                      <span>Cerrar Sesión</span>
                    </button>
                  </div>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};
