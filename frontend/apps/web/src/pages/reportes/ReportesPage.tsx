/**
 * Reportes Page
 *
 * Reports and analytics module - Coming Soon
 */

import React from 'react';
import { BarChart3 } from 'lucide-react';
import { ComingSoonPage } from '../../components/common/ComingSoonPage';

export const ReportesPage: React.FC = () => {
  return (
    <ComingSoonPage
      title="Módulo de Reportes"
      description="Análisis y reportes avanzados de tu negocio"
      icon={BarChart3}
      releaseDate="Q1 2025"
      features={[
        'Dashboard ejecutivo con KPIs principales',
        'Reportes de ventas por período y vendedor',
        'Análisis de rentabilidad por producto',
        'Reportes de clientes y segmentación',
        'Análisis de pipeline de oportunidades',
        'Reportes de productividad del equipo',
        'Exportación a Excel y PDF',
        'Reportes personalizables',
        'Gráficos interactivos y drill-down',
      ]}
    />
  );
};
