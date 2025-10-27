/**
 * Ventas Page
 *
 * Sales management module - Coming Soon
 */

import React from 'react';
import { ShoppingCart } from 'lucide-react';
import { ComingSoonPage } from '../../components/common/ComingSoonPage';

export const VentasPage: React.FC = () => {
  return (
    <ComingSoonPage
      title="Módulo de Ventas"
      description="Gestión completa del proceso de ventas y facturación"
      icon={ShoppingCart}
      releaseDate="Q1 2025"
      features={[
        'Creación y gestión de órdenes de venta',
        'Generación automática de facturas',
        'Integración con módulo de productos e inventario',
        'Seguimiento de estado de ventas',
        'Historial de transacciones por cliente',
        'Reportes de ventas por período',
        'Gestión de descuentos y promociones',
        'Integración con sistemas de pago',
      ]}
    />
  );
};
