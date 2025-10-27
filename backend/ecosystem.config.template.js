/**
 * PM2 Ecosystem Configuration Template
 * PagoDirecto CRM Backend
 *
 * IMPORTANTE: Este es un archivo template.
 * Copia este archivo a 'ecosystem.config.js' y configura las variables de entorno.
 *
 * Comando:
 *   cp ecosystem.config.template.js ecosystem.config.js
 *
 * Luego edita ecosystem.config.js y configura las variables sensibles.
 * O mejor aún, usa variables de entorno del sistema.
 */

module.exports = {
  apps: [{
    name: 'crm-backend',
    script: 'java',
    args: [
      '-jar',
      '-Xms512m',
      '-Xmx1024m',
      '-Dspring.profiles.active=prod',
      './application/target/application-1.0.0-SNAPSHOT.jar'
    ],
    // IMPORTANTE: Ajusta esta ruta según tu entorno
    cwd: process.env.APP_ROOT || '/opt/crm-backend',
    instances: 1,
    autorestart: true,
    watch: false,
    max_memory_restart: '1G',
    env: {
      NODE_ENV: 'production',

      // Puerto del servidor
      SERVER_PORT: process.env.SERVER_PORT || 8082,

      // Base de datos - CONFIGURAR CON VARIABLES DE ENTORNO
      DATABASE_URL: process.env.DATABASE_URL || 'jdbc:postgresql://localhost:5432/crm_db',
      DATABASE_USERNAME: process.env.DATABASE_USERNAME || 'postgres',
      DATABASE_PASSWORD: process.env.DATABASE_PASSWORD || 'CHANGE_ME',

      // JWT - CONFIGURAR CON VARIABLES DE ENTORNO
      JWT_SECRET: process.env.JWT_SECRET || 'CHANGE_ME_TO_SECURE_SECRET',
      JWT_EXPIRATION: process.env.JWT_EXPIRATION || '86400000',        // 24 horas
      JWT_REFRESH_EXPIRATION: process.env.JWT_REFRESH_EXPIRATION || '604800000',  // 7 días

      // Spring Boot
      SPRING_FLYWAY_ENABLED: process.env.SPRING_FLYWAY_ENABLED || 'false',
      SPRING_JPA_HIBERNATE_DDL_AUTO: process.env.SPRING_JPA_HIBERNATE_DDL_AUTO || 'none'
    },
    error_file: './logs/pm2-error.log',
    out_file: './logs/pm2-out.log',
    log_date_format: 'YYYY-MM-DD HH:mm:ss Z',
    merge_logs: true,
    min_uptime: '10s',
    max_restarts: 10,
    restart_delay: 4000
  }]
};
