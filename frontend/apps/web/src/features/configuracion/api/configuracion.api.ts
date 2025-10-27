/**
 * API: Configuracion
 *
 * Servicios de API para gestión de configuraciones del sistema
 */

import axios from 'axios';
import type {
  ConfiguracionGeneral,
  ConfiguracionNotificaciones,
  ConfiguracionIntegraciones,
  ConfiguracionSeguridad,
} from '../types/configuracion.types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:28080/api';

const http = axios.create({
  baseURL: `${API_BASE_URL}/v1/configuracion`,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
});

export const configuracionApi = {
  /**
   * Obtiene la configuración general
   */
  getGeneral: async (): Promise<ConfiguracionGeneral> => {
    const response = await http.get<ConfiguracionGeneral>('/general');
    return response.data;
  },

  /**
   * Actualiza la configuración general
   */
  updateGeneral: async (data: ConfiguracionGeneral): Promise<ConfiguracionGeneral> => {
    const response = await http.put<ConfiguracionGeneral>('/general', data);
    return response.data;
  },

  /**
   * Obtiene la configuración de notificaciones
   */
  getNotificaciones: async (): Promise<ConfiguracionNotificaciones> => {
    const response = await http.get<ConfiguracionNotificaciones>('/notificaciones');
    return response.data;
  },

  /**
   * Actualiza la configuración de notificaciones
   */
  updateNotificaciones: async (
    data: ConfiguracionNotificaciones
  ): Promise<ConfiguracionNotificaciones> => {
    const response = await http.put<ConfiguracionNotificaciones>('/notificaciones', data);
    return response.data;
  },

  /**
   * Obtiene la configuración de integraciones
   */
  getIntegraciones: async (): Promise<ConfiguracionIntegraciones> => {
    const response = await http.get<ConfiguracionIntegraciones>('/integraciones');
    return response.data;
  },

  /**
   * Actualiza la configuración de integraciones
   */
  updateIntegraciones: async (
    data: ConfiguracionIntegraciones
  ): Promise<ConfiguracionIntegraciones> => {
    const response = await http.put<ConfiguracionIntegraciones>('/integraciones', data);
    return response.data;
  },

  /**
   * Obtiene la configuración de seguridad
   */
  getSeguridad: async (): Promise<ConfiguracionSeguridad> => {
    const response = await http.get<ConfiguracionSeguridad>('/seguridad');
    return response.data;
  },

  /**
   * Actualiza la configuración de seguridad
   */
  updateSeguridad: async (data: ConfiguracionSeguridad): Promise<ConfiguracionSeguridad> => {
    const response = await http.put<ConfiguracionSeguridad>('/seguridad', data);
    return response.data;
  },
};
