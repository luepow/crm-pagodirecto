-- =====================================================================================================================
-- V11__seed_spidi_room_types.sql
-- Migración para seed data del módulo Spidi - Tipos de Salas
-- Fecha: 2025-10-13
-- Autor: Database Architecture Team
-- Descripción: Datos iniciales de catálogo de tipos de salas Spidi
-- =====================================================================================================================

-- Insertar tipos de salas predefinidos
INSERT INTO tba_spd_room_type (code, name, description, icon, color, default_capacity, default_ttl_seconds, created_at)
VALUES
    (
        'WEBRTC',
        'WebRTC Video Conference',
        'Sala de videoconferencia con soporte WebRTC para comunicación peer-to-peer en tiempo real',
        'video',
        '#4CAF50',
        50,
        7200,
        NOW()
    ),
    (
        'CHAT',
        'Chat Room',
        'Sala de chat textual con mensajería instantánea y notificaciones en tiempo real',
        'message',
        '#2196F3',
        500,
        3600,
        NOW()
    ),
    (
        'NOTIFICATION',
        'Notification Channel',
        'Canal de notificaciones push para alertas y actualizaciones del sistema',
        'bell',
        '#FF9800',
        10000,
        1800,
        NOW()
    ),
    (
        'BROADCAST',
        'Live Broadcast',
        'Transmisión en vivo de eventos con un emisor y múltiples receptores',
        'broadcast',
        '#F44336',
        1000,
        14400,
        NOW()
    ),
    (
        'COLLABORATION',
        'Collaborative Workspace',
        'Espacio colaborativo con pizarra compartida, documentos y co-edición',
        'users',
        '#9C27B0',
        25,
        3600,
        NOW()
    ),
    (
        'SUPPORT',
        'Customer Support',
        'Canal de soporte al cliente con sistema de colas y asignación de agentes',
        'headset',
        '#00BCD4',
        100,
        1800,
        NOW()
    ),
    (
        'GAMING',
        'Gaming Lobby',
        'Lobby de juegos multijugador con sincronización de estado en tiempo real',
        'gamepad',
        '#E91E63',
        16,
        7200,
        NOW()
    ),
    (
        'MONITORING',
        'System Monitoring',
        'Dashboard de monitoreo de sistemas con métricas en tiempo real',
        'activity',
        '#607D8B',
        10,
        86400,
        NOW()
    ),
    (
        'EVENT',
        'Event Room',
        'Sala de eventos temporales con integración de calendario y recordatorios',
        'calendar',
        '#FFEB3B',
        200,
        10800,
        NOW()
    ),
    (
        'GENERIC',
        'Generic Room',
        'Sala genérica multipropósito para casos de uso personalizados',
        'grid',
        '#9E9E9E',
        100,
        3600,
        NOW()
    );

-- Insertar permisos del módulo Spidi en la tabla de permisos
INSERT INTO seguridad_permisos (recurso, accion, scope, descripcion, created_at)
VALUES
    ('spidi', 'READ', 'spidi:monitor', 'Ver dashboards y métricas de salas Spidi', NOW()),
    ('spidi', 'CREATE', 'spidi:admin', 'Crear y configurar nuevas salas Spidi', NOW()),
    ('spidi', 'UPDATE', 'spidi:admin', 'Modificar configuración de salas Spidi', NOW()),
    ('spidi', 'DELETE', 'spidi:admin', 'Eliminar salas Spidi', NOW()),
    ('spidi', 'EXECUTE', 'spidi:user', 'Conectarse a salas Spidi y enviar heartbeats', NOW()),
    ('spidi', 'ADMIN', 'spidi:admin', 'Administración completa del módulo Spidi', NOW());

-- Insertar alertas predefinidas genéricas (se pueden personalizar por sala)
-- Estas son plantillas que se pueden usar al crear salas

COMMENT ON TABLE tba_spd_room_type IS 'Catálogo de tipos de salas Spidi (10 tipos predefinidos)';

-- =====================================================================================================================
-- NOTAS DE CONFIGURACIÓN
-- =====================================================================================================================

-- 1. CAPACIDADES RECOMENDADAS POR TIPO:
--    - WEBRTC: 50 (limitación de ancho de banda para video)
--    - CHAT: 500 (óptimo para rendimiento de mensajería)
--    - NOTIFICATION: 10,000 (solo lectura, alta concurrencia)
--    - BROADCAST: 1,000 (un emisor, muchos receptores)
--    - COLLABORATION: 25 (interacción intensa)
--    - SUPPORT: 100 (sistema de colas)
--    - GAMING: 16 (típico para juegos multijugador)
--    - MONITORING: 10 (dashboards administrativos)
--    - EVENT: 200 (eventos medianos)
--    - GENERIC: 100 (valor estándar)
--
-- 2. TTL RECOMENDADOS:
--    - Sesiones cortas (SUPPORT, NOTIFICATION): 1800s (30 min)
--    - Sesiones estándar (CHAT, COLLABORATION, GENERIC, EVENT): 3600s (1 hora)
--    - Sesiones largas (WEBRTC, GAMING): 7200s (2 horas)
--    - Sesiones extendidas (BROADCAST): 14400s (4 horas)
--    - Sesiones persistentes (MONITORING): 86400s (24 horas)
--
-- 3. COLORES:
--    - Verde (#4CAF50): Video/comunicación activa
--    - Azul (#2196F3): Chat/mensajería
--    - Naranja (#FF9800): Notificaciones/alertas
--    - Rojo (#F44336): Broadcast/en vivo
--    - Púrpura (#9C27B0): Colaboración
--    - Cian (#00BCD4): Soporte
--    - Rosa (#E91E63): Gaming/entretenimiento
--    - Gris (#607D8B): Monitoreo/admin
--    - Amarillo (#FFEB3B): Eventos
--    - Gris claro (#9E9E9E): Genérico

-- =====================================================================================================================
-- FIN DE MIGRACIÓN V11__seed_spidi_room_types.sql
-- =====================================================================================================================
