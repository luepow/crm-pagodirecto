/**
 * Page: ConfiguracionPage
 *
 * Página de configuración del sistema con múltiples categorías
 */

import React, { useState, useEffect } from 'react';
import {
  Settings,
  Bell,
  Plug,
  Shield,
  Save,
  Globe,
  Building,
  Mail,
  Phone,
  Clock,
} from 'lucide-react';
import { Card, CardHeader, CardTitle, CardContent } from '@shared-ui/components/Card';
import { Button } from '@shared-ui/components/Button';
import { toast } from 'sonner';
import { configuracionApi } from '../../features/configuracion/api/configuracion.api';
import type {
  ConfiguracionGeneral,
  ConfiguracionNotificaciones,
  ConfiguracionIntegraciones,
  ConfiguracionSeguridad,
} from '../../features/configuracion/types/configuracion.types';

type TabType = 'general' | 'notificaciones' | 'integraciones' | 'seguridad';

/**
 * ConfiguracionPage Component
 */
export const ConfiguracionPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<TabType>('general');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);

  // Form states
  const [generalData, setGeneralData] = useState<ConfiguracionGeneral>({
    nombreEmpresa: '',
    logoUrl: '',
    zonaHoraria: 'America/Mexico_City',
    moneda: 'MXN',
    idioma: 'es-MX',
    formatoFecha: 'dd/MM/yyyy',
    formatoHora: 'HH:mm:ss',
    telefonoContacto: '',
    emailContacto: '',
    direccion: '',
  });

  const [notificacionesData, setNotificacionesData] = useState<ConfiguracionNotificaciones>({
    emailHabilitado: true,
    smtpHost: 'smtp.gmail.com',
    smtpPort: 587,
    smtpUsername: '',
    smtpTls: true,
    emailFrom: '',
    emailFromName: '',
    pushHabilitado: false,
    fcmApiKey: '',
    smsHabilitado: false,
    smsProveedor: 'twilio',
    smsAccountSid: '',
    smsFrom: '',
    notificarNuevosClientes: true,
    notificarNuevasOportunidades: true,
    notificarTareasVencidas: true,
    notificarNuevasVentas: true,
  });

  const [integracionesData, setIntegracionesData] = useState<ConfiguracionIntegraciones>({
    googleHabilitado: false,
    googleClientId: '',
    googleCalendarHabilitado: false,
    pagosPasarelaHabilitada: false,
    pagosProveedor: 'stripe',
    pagosApiKey: '',
    pagosWebhookUrl: '',
    webhooksHabilitado: false,
    webhookClientesUrl: '',
    webhookOportunidadesUrl: '',
    webhookVentasUrl: '',
    webhookSecret: '',
    storageProveedor: 'local',
    s3BucketName: '',
    s3Region: 'us-east-1',
    apiExternaUrl: '',
    apiExternaKey: '',
    apiTimeout: 30,
  });

  const [seguridadData, setSeguridadData] = useState<ConfiguracionSeguridad>({
    passwordMinLength: 8,
    passwordRequiereMaxusculas: true,
    passwordRequiereMinusculas: true,
    passwordRequiereNumeros: true,
    passwordRequiereEspeciales: true,
    passwordDiasExpiracion: 90,
    passwordHistorial: 5,
    sessionDuracion: 60,
    sessionTimeoutInactividad: 30,
    sessionMaxSimultaneas: 3,
    loginMaxIntentosFallidos: 5,
    loginDuracionBloqueo: 30,
    mfaObligatorio: false,
    mfaObligatorioAdmins: true,
    ipRestriccionHabilitada: false,
    ipListaPermitidas: '',
    auditHabilitado: true,
    auditRetencionDias: 365,
    auditDatosSensibles: true,
    corsOrigenes: '*',
    rateLimitHabilitado: true,
    rateLimitMaxRequests: 100,
  });

  useEffect(() => {
    loadConfiguracion(activeTab);
  }, [activeTab]);

  const loadConfiguracion = async (tab: TabType) => {
    try {
      setIsLoading(true);
      switch (tab) {
        case 'general':
          const general = await configuracionApi.getGeneral();
          setGeneralData(general);
          break;
        case 'notificaciones':
          const notif = await configuracionApi.getNotificaciones();
          setNotificacionesData(notif);
          break;
        case 'integraciones':
          const integ = await configuracionApi.getIntegraciones();
          setIntegracionesData(integ);
          break;
        case 'seguridad':
          const seg = await configuracionApi.getSeguridad();
          setSeguridadData(seg);
          break;
      }
    } catch (error) {
      console.error('Error cargando configuración:', error);
      toast.error('Error al cargar la configuración');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSave = async () => {
    try {
      setIsSaving(true);
      switch (activeTab) {
        case 'general':
          await configuracionApi.updateGeneral(generalData);
          break;
        case 'notificaciones':
          await configuracionApi.updateNotificaciones(notificacionesData);
          break;
        case 'integraciones':
          await configuracionApi.updateIntegraciones(integracionesData);
          break;
        case 'seguridad':
          await configuracionApi.updateSeguridad(seguridadData);
          break;
      }
      toast.success('Configuración guardada exitosamente');
    } catch (error) {
      console.error('Error guardando configuración:', error);
      toast.error('Error al guardar la configuración');
    } finally {
      setIsSaving(false);
    }
  };

  const renderGeneralTab = () => (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>
            <Building size={20} className="inline-block mr-2" />
            Información de la Empresa
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">
                Nombre de la Empresa
              </label>
              <input
                type="text"
                value={generalData.nombreEmpresa}
                onChange={(e) => setGeneralData({ ...generalData, nombreEmpresa: e.target.value })}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">
                URL del Logo
              </label>
              <input
                type="url"
                value={generalData.logoUrl}
                onChange={(e) => setGeneralData({ ...generalData, logoUrl: e.target.value })}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">
                <Mail size={16} className="inline-block mr-1" />
                Email de Contacto
              </label>
              <input
                type="email"
                value={generalData.emailContacto}
                onChange={(e) => setGeneralData({ ...generalData, emailContacto: e.target.value })}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">
                <Phone size={16} className="inline-block mr-1" />
                Teléfono de Contacto
              </label>
              <input
                type="tel"
                value={generalData.telefonoContacto}
                onChange={(e) => setGeneralData({ ...generalData, telefonoContacto: e.target.value })}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-text-primary mb-1">Dirección</label>
              <textarea
                value={generalData.direccion}
                onChange={(e) => setGeneralData({ ...generalData, direccion: e.target.value })}
                rows={2}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>
            <Globe size={20} className="inline-block mr-2" />
            Configuración Regional
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">
                Zona Horaria
              </label>
              <select
                value={generalData.zonaHoraria}
                onChange={(e) => setGeneralData({ ...generalData, zonaHoraria: e.target.value })}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              >
                <option value="America/Mexico_City">América/Ciudad de México</option>
                <option value="America/New_York">América/Nueva York</option>
                <option value="America/Los_Angeles">América/Los Ángeles</option>
                <option value="Europe/Madrid">Europa/Madrid</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">Moneda</label>
              <select
                value={generalData.moneda}
                onChange={(e) => setGeneralData({ ...generalData, moneda: e.target.value })}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              >
                <option value="MXN">MXN - Peso Mexicano</option>
                <option value="USD">USD - Dólar Estadounidense</option>
                <option value="EUR">EUR - Euro</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">Idioma</label>
              <select
                value={generalData.idioma}
                onChange={(e) => setGeneralData({ ...generalData, idioma: e.target.value })}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              >
                <option value="es-MX">Español (México)</option>
                <option value="en-US">English (US)</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-text-primary mb-1">
                <Clock size={16} className="inline-block mr-1" />
                Formato de Fecha
              </label>
              <select
                value={generalData.formatoFecha}
                onChange={(e) => setGeneralData({ ...generalData, formatoFecha: e.target.value })}
                className="w-full rounded-xl border border-gray-300 px-4 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
              >
                <option value="dd/MM/yyyy">DD/MM/AAAA</option>
                <option value="MM/dd/yyyy">MM/DD/AAAA</option>
                <option value="yyyy-MM-dd">AAAA-MM-DD</option>
              </select>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );

  if (isLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <div className="mx-auto h-12 w-12 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
          <p className="mt-4 text-text-secondary">Cargando configuración...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold text-text-primary">Configuración</h1>
          <p className="mt-1 text-text-secondary">Gestiona la configuración del sistema</p>
        </div>

        <Button variant="primary" leftIcon={<Save size={18} />} onClick={handleSave} disabled={isSaving}>
          {isSaving ? 'Guardando...' : 'Guardar Cambios'}
        </Button>
      </div>

      {/* Tabs */}
      <div className="flex flex-wrap gap-2 border-b border-gray-200">
        <button
          onClick={() => setActiveTab('general')}
          className={`flex items-center gap-2 px-4 py-2 font-medium transition-colors ${
            activeTab === 'general'
              ? 'border-b-2 border-primary text-primary'
              : 'text-text-secondary hover:text-text-primary'
          }`}
        >
          <Settings size={18} />
          General
        </button>
        <button
          onClick={() => setActiveTab('notificaciones')}
          className={`flex items-center gap-2 px-4 py-2 font-medium transition-colors ${
            activeTab === 'notificaciones'
              ? 'border-b-2 border-primary text-primary'
              : 'text-text-secondary hover:text-text-primary'
          }`}
        >
          <Bell size={18} />
          Notificaciones
        </button>
        <button
          onClick={() => setActiveTab('integraciones')}
          className={`flex items-center gap-2 px-4 py-2 font-medium transition-colors ${
            activeTab === 'integraciones'
              ? 'border-b-2 border-primary text-primary'
              : 'text-text-secondary hover:text-text-primary'
          }`}
        >
          <Plug size={18} />
          Integraciones
        </button>
        <button
          onClick={() => setActiveTab('seguridad')}
          className={`flex items-center gap-2 px-4 py-2 font-medium transition-colors ${
            activeTab === 'seguridad'
              ? 'border-b-2 border-primary text-primary'
              : 'text-text-secondary hover:text-text-primary'
          }`}
        >
          <Shield size={18} />
          Seguridad
        </button>
      </div>

      {/* Tab content */}
      <div>
        {activeTab === 'general' && renderGeneralTab()}
        {activeTab === 'notificaciones' && <p className="text-center text-text-secondary py-8">Sección de notificaciones en desarrollo</p>}
        {activeTab === 'integraciones' && <p className="text-center text-text-secondary py-8">Sección de integraciones en desarrollo</p>}
        {activeTab === 'seguridad' && <p className="text-center text-text-secondary py-8">Sección de seguridad en desarrollo</p>}
      </div>
    </div>
  );
};
