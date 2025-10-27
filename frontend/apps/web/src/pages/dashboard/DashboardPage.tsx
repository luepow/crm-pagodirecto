/**
 * Dashboard Page
 *
 * Main dashboard with KPIs, charts, and quick actions.
 * Follows BrandBook 2024 design guidelines.
 */

import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Users,
  Target,
  CheckSquare,
  TrendingUp,
  ArrowUp,
  ArrowDown,
  Plus,
  Calendar,
  DollarSign,
} from 'lucide-react';
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import { Card, CardHeader, CardTitle, CardContent } from '@shared-ui/components/Card';
import { Button } from '@shared-ui/components/Button';
import { Badge } from '@shared-ui/components/Badge';
import { Avatar } from '@shared-ui/components/Avatar';
import { cn } from '@shared-ui/utils';
import { dashboardApi, DashboardStats } from '../../features/dashboard/api/dashboard.api';
import { toast } from 'sonner';

const ventasData = [
  { mes: 'Ene', ventas: 45000 },
  { mes: 'Feb', ventas: 52000 },
  { mes: 'Mar', ventas: 48000 },
  { mes: 'Abr', ventas: 61000 },
  { mes: 'May', ventas: 55000 },
  { mes: 'Jun', ventas: 67000 },
];

const pipelineData = [
  { etapa: 'Prospecto', cantidad: 12, valor: 45000 },
  { etapa: 'Calificación', cantidad: 8, valor: 32000 },
  { etapa: 'Propuesta', cantidad: 5, valor: 28000 },
  { etapa: 'Negociación', cantidad: 3, valor: 15000 },
  { etapa: 'Ganada', cantidad: 2, valor: 12000 },
];

const actividadesRecientes = [
  {
    id: '1',
    tipo: 'cliente' as const,
    titulo: 'Nuevo cliente registrado',
    descripcion: 'María González se registró como cliente',
    tiempo: 'Hace 10 min',
    usuario: 'MG',
  },
  {
    id: '2',
    tipo: 'oportunidad' as const,
    titulo: 'Oportunidad actualizada',
    descripcion: 'Propuesta enviada a Tech Solutions Inc.',
    tiempo: 'Hace 1 hora',
    usuario: 'JS',
  },
  {
    id: '3',
    tipo: 'tarea' as const,
    titulo: 'Tarea completada',
    descripcion: 'Llamada de seguimiento con cliente premium',
    tiempo: 'Hace 2 horas',
    usuario: 'AP',
  },
];

/**
 * KPI Card Component
 */
interface KPICardProps {
  title: string;
  value: number | string;
  change: number;
  trend: 'up' | 'down';
  icon: React.ReactNode;
  iconColor: string;
  formatter?: (value: number | string) => string;
}

const KPICard: React.FC<KPICardProps> = ({
  title,
  value,
  change,
  trend,
  icon,
  iconColor,
  formatter = (v) => String(v),
}) => {
  return (
    <Card hoverable>
      <CardContent padding="md">
        <div className="flex items-start justify-between">
          <div className="flex-1">
            <p className="text-sm font-medium text-text-secondary">{title}</p>
            <p className="mt-2 text-3xl font-bold text-text-primary">
              {formatter(value)}
            </p>
            <div
              className={cn(
                'mt-2 flex items-center gap-1 text-sm font-medium',
                trend === 'up' ? 'text-success' : 'text-error'
              )}
            >
              {trend === 'up' ? <ArrowUp size={16} /> : <ArrowDown size={16} />}
              <span>{Math.abs(change)}%</span>
              <span className="text-text-secondary">vs mes anterior</span>
            </div>
          </div>
          <div className={cn('rounded-2xl p-3', iconColor)}>{icon}</div>
        </div>
      </CardContent>
    </Card>
  );
};

/**
 * Dashboard Page Component
 */
export const DashboardPage: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const formatCurrency = (value: number | string) => {
    const num = typeof value === 'string' ? parseFloat(value) : value;
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN',
    }).format(num);
  };

  useEffect(() => {
    const loadStats = async () => {
      try {
        setIsLoading(true);
        const data = await dashboardApi.getStats();
        setStats(data);
      } catch (error) {
        console.error('Error cargando estadísticas:', error);
        toast.error('Error al cargar las estadísticas del dashboard');
      } finally {
        setIsLoading(false);
      }
    };

    loadStats();
  }, []);

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <div className="mx-auto h-12 w-12 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
          <p className="mt-4 text-text-secondary">Cargando dashboard...</p>
        </div>
      </div>
    );
  }

  if (!stats) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <p className="text-text-secondary">No se pudieron cargar las estadísticas</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold text-text-primary">Dashboard</h1>
          <p className="mt-1 text-text-secondary">
            Vista general de tu negocio en tiempo real
          </p>
        </div>

        <div className="flex gap-2">
          <Button variant="outline" leftIcon={<Calendar size={18} />}>
            Últimos 30 días
          </Button>
          <Button variant="primary" leftIcon={<Plus size={18} />}>
            Acción Rápida
          </Button>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <KPICard
          title="Clientes Nuevos"
          value={stats.clientesNuevosEsteMes}
          change={stats.clientesCambioMensual}
          trend={stats.clientesCambioMensual >= 0 ? 'up' : 'down'}
          icon={<Users size={24} className="text-white" />}
          iconColor="bg-gradient-to-br from-verticals-seguros to-verticals-seguros-600"
        />
        <KPICard
          title="Oportunidades Activas"
          value={stats.oportunidadesActivas}
          change={stats.oportunidadesCambioMensual}
          trend={stats.oportunidadesCambioMensual >= 0 ? 'up' : 'down'}
          icon={<Target size={24} className="text-white" />}
          iconColor="bg-gradient-to-br from-primary to-primary-600"
        />
        <KPICard
          title="Tareas Pendientes"
          value={stats.tareasPendientes}
          change={stats.tareasCambioMensual}
          trend={stats.tareasCambioMensual >= 0 ? 'up' : 'down'}
          icon={<CheckSquare size={24} className="text-white" />}
          iconColor="bg-gradient-to-br from-verticals-viajes to-verticals-viajes-600"
        />
        <KPICard
          title="Ventas del Mes"
          value={stats.ventasTotalesEsteMes}
          change={stats.ventasCambioMensual}
          trend={stats.ventasCambioMensual >= 0 ? 'up' : 'down'}
          icon={<TrendingUp size={24} className="text-white" />}
          iconColor="bg-gradient-to-br from-verticals-servicios to-verticals-servicios-600"
          formatter={formatCurrency}
        />
      </div>

      {/* Charts Row */}
      <div className="grid gap-4 lg:grid-cols-2">
        {/* Ventas Chart */}
        <Card>
          <CardHeader>
            <CardTitle>Ventas Últimos 6 Meses</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={ventasData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#E5E7EB" />
                <XAxis
                  dataKey="mes"
                  stroke="#6B7280"
                  style={{ fontSize: '12px' }}
                />
                <YAxis stroke="#6B7280" style={{ fontSize: '12px' }} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#fff',
                    border: '1px solid #E5E7EB',
                    borderRadius: '12px',
                  }}
                  formatter={(value: number) => formatCurrency(value)}
                />
                <Line
                  type="monotone"
                  dataKey="ventas"
                  stroke="#FF2463"
                  strokeWidth={3}
                  dot={{ fill: '#FF2463', r: 4 }}
                  activeDot={{ r: 6 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Pipeline Chart */}
        <Card>
          <CardHeader>
            <CardTitle>Pipeline por Etapa</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={pipelineData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#E5E7EB" />
                <XAxis
                  dataKey="etapa"
                  stroke="#6B7280"
                  style={{ fontSize: '12px' }}
                />
                <YAxis stroke="#6B7280" style={{ fontSize: '12px' }} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#fff',
                    border: '1px solid #E5E7EB',
                    borderRadius: '12px',
                  }}
                />
                <Bar dataKey="cantidad" fill="#0066FF" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      {/* Bottom Row */}
      <div className="grid gap-4 lg:grid-cols-3">
        {/* Recent Activities */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle>Actividades Recientes</CardTitle>
              <Link
                to="/actividades"
                className="text-sm font-medium text-primary hover:underline"
              >
                Ver todas
              </Link>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {actividadesRecientes.map((actividad) => (
                <div
                  key={actividad.id}
                  className="flex items-start gap-4 rounded-xl border border-gray-100 p-4 transition-smooth hover:bg-gray-50"
                >
                  <Avatar initials={actividad.usuario} size="sm" />
                  <div className="flex-1">
                    <p className="font-medium text-text-primary">{actividad.titulo}</p>
                    <p className="mt-0.5 text-sm text-text-secondary">
                      {actividad.descripcion}
                    </p>
                    <p className="mt-1 text-xs text-text-tertiary">{actividad.tiempo}</p>
                  </div>
                  <Badge
                    variant={
                      actividad.tipo === 'cliente'
                        ? 'info'
                        : actividad.tipo === 'oportunidad'
                        ? 'primary'
                        : 'success'
                    }
                    size="sm"
                  >
                    {actividad.tipo}
                  </Badge>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Quick Actions */}
        <Card>
          <CardHeader>
            <CardTitle>Acciones Rápidas</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              <Button
                variant="outline"
                fullWidth
                leftIcon={<Users size={18} />}
                className="justify-start"
              >
                Crear Cliente
              </Button>
              <Button
                variant="outline"
                fullWidth
                leftIcon={<Target size={18} />}
                className="justify-start"
              >
                Nueva Oportunidad
              </Button>
              <Button
                variant="outline"
                fullWidth
                leftIcon={<CheckSquare size={18} />}
                className="justify-start"
              >
                Agregar Tarea
              </Button>
              <Button
                variant="outline"
                fullWidth
                leftIcon={<DollarSign size={18} />}
                className="justify-start"
              >
                Registrar Venta
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
