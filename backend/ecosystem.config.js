/**
 * PM2 Ecosystem Configuration
 * PagoDirecto CRM Backend
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
      './application.jar'
    ],
    cwd: '/opt/crm-backend',
    instances: 1,
    autorestart: true,
    watch: false,
    max_memory_restart: '1G',
    env_file: '/opt/crm-backend/.env',
    error_file: './logs/pm2-error.log',
    out_file: './logs/pm2-out.log',
    log_date_format: 'YYYY-MM-DD HH:mm:ss Z',
    merge_logs: true,
    min_uptime: '10s',
    max_restarts: 10,
    restart_delay: 4000
  }]
};
