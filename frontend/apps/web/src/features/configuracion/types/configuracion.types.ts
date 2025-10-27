/**
 * Types: Configuracion
 *
 * Definiciones de tipos para el módulo de configuración
 */

export interface ConfiguracionGeneral {
  nombreEmpresa: string;
  logoUrl: string;
  zonaHoraria: string;
  moneda: string;
  idioma: string;
  formatoFecha: string;
  formatoHora: string;
  telefonoContacto: string;
  emailContacto: string;
  direccion: string;
}

export interface ConfiguracionNotificaciones {
  emailHabilitado: boolean;
  smtpHost: string;
  smtpPort: number;
  smtpUsername: string;
  smtpTls: boolean;
  emailFrom: string;
  emailFromName: string;
  pushHabilitado: boolean;
  fcmApiKey: string;
  smsHabilitado: boolean;
  smsProveedor: string;
  smsAccountSid: string;
  smsFrom: string;
  notificarNuevosClientes: boolean;
  notificarNuevasOportunidades: boolean;
  notificarTareasVencidas: boolean;
  notificarNuevasVentas: boolean;
}

export interface ConfiguracionIntegraciones {
  googleHabilitado: boolean;
  googleClientId: string;
  googleCalendarHabilitado: boolean;
  pagosPasarelaHabilitada: boolean;
  pagosProveedor: string;
  pagosApiKey: string;
  pagosWebhookUrl: string;
  webhooksHabilitado: boolean;
  webhookClientesUrl: string;
  webhookOportunidadesUrl: string;
  webhookVentasUrl: string;
  webhookSecret: string;
  storageProveedor: string;
  s3BucketName: string;
  s3Region: string;
  apiExternaUrl: string;
  apiExternaKey: string;
  apiTimeout: number;
}

export interface ConfiguracionSeguridad {
  passwordMinLength: number;
  passwordRequiereMaxusculas: boolean;
  passwordRequiereMinusculas: boolean;
  passwordRequiereNumeros: boolean;
  passwordRequiereEspeciales: boolean;
  passwordDiasExpiracion: number;
  passwordHistorial: number;
  sessionDuracion: number;
  sessionTimeoutInactividad: number;
  sessionMaxSimultaneas: number;
  loginMaxIntentosFallidos: number;
  loginDuracionBloqueo: number;
  mfaObligatorio: boolean;
  mfaObligatorioAdmins: boolean;
  ipRestriccionHabilitada: boolean;
  ipListaPermitidas: string;
  auditHabilitado: boolean;
  auditRetencionDias: number;
  auditDatosSensibles: boolean;
  corsOrigenes: string;
  rateLimitHabilitado: boolean;
  rateLimitMaxRequests: number;
}
