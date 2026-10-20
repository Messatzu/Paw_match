CREATE DATABASE IF NOT EXISTS paw_match
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE paw_match;


-- =====================================================
-- USUARIOS
-- =====================================================

CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    rol ENUM('ADOPTANTE', 'PUBLICADOR') NOT NULL,
    tipo_publicador ENUM('PARTICULAR', 'REFUGIO') NULL,

    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NULL,
    correo VARCHAR(254) NULL,
    password_hash VARCHAR(255) NULL,

    foto_url VARCHAR(2048) NULL,
    ciudad VARCHAR(100) NULL,
    provincia VARCHAR(100) NULL,
    pais VARCHAR(100) NULL,
    direccion VARCHAR(255) NULL,
    descripcion VARCHAR(2000) NULL,

    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    UNIQUE KEY uq_usuarios_correo (correo),

    CONSTRAINT chk_usuarios_nombre
        CHECK (CHAR_LENGTH(TRIM(nombre)) > 0),

    CONSTRAINT chk_usuarios_activo
        CHECK (activo IN (0, 1)),

    CONSTRAINT chk_usuarios_tipo
        CHECK (
            (rol = 'ADOPTANTE' AND tipo_publicador IS NULL)
            OR
            (
                rol = 'PUBLICADOR'
                AND tipo_publicador IS NOT NULL
                AND tipo_publicador IN ('PARTICULAR', 'REFUGIO')
            )
        ),

    -- Las cuentas activas requieren sus datos de registro.
    -- Al dar de baja una cuenta, se podrán retirar sus datos
    -- personales y conservar el identificador.
    CONSTRAINT chk_usuarios_perfil_activo
        CHECK (
            activo = FALSE
            OR (
                correo IS NOT NULL
                AND CHAR_LENGTH(TRIM(correo)) > 0

                AND password_hash IS NOT NULL
                AND CHAR_LENGTH(TRIM(password_hash)) > 0

                AND ciudad IS NOT NULL
                AND CHAR_LENGTH(TRIM(ciudad)) > 0

                AND provincia IS NOT NULL
                AND CHAR_LENGTH(TRIM(provincia)) > 0

                AND pais IS NOT NULL
                AND CHAR_LENGTH(TRIM(pais)) > 0

                AND (
                    (
                        rol = 'PUBLICADOR'
                        AND tipo_publicador = 'REFUGIO'
                    )
                    OR (
                        apellido IS NOT NULL
                        AND CHAR_LENGTH(TRIM(apellido)) > 0
                    )
                )
            )
        )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- =====================================================
-- MASCOTAS
-- =====================================================

CREATE TABLE IF NOT EXISTS mascotas (
    id_mascota INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_publicador INT UNSIGNED NOT NULL,

    nombre VARCHAR(100) NOT NULL,
    especie ENUM('PERRO', 'GATO') NOT NULL,
    raza VARCHAR(100) NULL,
    edad_meses INT NULL,
    sexo ENUM('MACHO', 'HEMBRA') NULL,
    tamano ENUM('PEQUENO', 'MEDIANO', 'GRANDE') NOT NULL,

    descripcion VARCHAR(2000) NOT NULL,
    foto_url VARCHAR(2048) NULL,

    estado ENUM('DISPONIBLE', 'ADOPTADA', 'RETIRADA')
        NOT NULL DEFAULT 'DISPONIBLE',

    fecha_publicacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    KEY idx_mascotas_publicador_estado (id_publicador, estado),
    KEY idx_mascotas_estado_fecha (estado, fecha_publicacion),

    CONSTRAINT fk_mascotas_publicador
        FOREIGN KEY (id_publicador)
        REFERENCES usuarios (id_usuario)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT chk_mascotas_edad
        CHECK (edad_meses IS NULL OR edad_meses >= 0),

    CONSTRAINT chk_mascotas_textos
        CHECK (
            CHAR_LENGTH(TRIM(nombre)) > 0
            AND CHAR_LENGTH(TRIM(descripcion)) > 0
        ),

    CONSTRAINT chk_mascotas_valores
        CHECK (
            especie IN ('PERRO', 'GATO')
            AND tamano IN ('PEQUENO', 'MEDIANO', 'GRANDE')
            AND (sexo IS NULL OR sexo IN ('MACHO', 'HEMBRA'))
            AND estado IN ('DISPONIBLE', 'ADOPTADA', 'RETIRADA')
        )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- =====================================================
-- SOLICITUDES
-- =====================================================

CREATE TABLE IF NOT EXISTS solicitudes (
    id_solicitud INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_adoptante INT UNSIGNED NOT NULL,
    id_mascota INT UNSIGNED NOT NULL,

    estado ENUM(
        'PENDIENTE',
        'ACEPTADA',
        'RECHAZADA',
        'CANCELADA'
    ) NOT NULL DEFAULT 'PENDIENTE',

    motivo_cierre VARCHAR(500) NULL,
    fecha_solicitud DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_resolucion DATETIME NULL,

    -- Columna calculada automáticamente.
    -- Las solicitudes pendientes o aceptadas tienen marca 1.
    -- Las rechazadas o canceladas tienen NULL.
    marca_activa TINYINT
        GENERATED ALWAYS AS (
            CASE
                WHEN estado IN ('PENDIENTE', 'ACEPTADA') THEN 1
                ELSE NULL
            END
        ) STORED,

    -- Impide dos solicitudes activas para la misma pareja.
    -- Permite conservar varias solicitudes finalizadas.
    UNIQUE KEY uq_solicitudes_activa (
        marca_activa,
        id_adoptante,
        id_mascota
    ),

    KEY idx_solicitudes_adoptante_fecha (
        id_adoptante,
        fecha_solicitud
    ),

    KEY idx_solicitudes_mascota_estado (
        id_mascota,
        estado
    ),

    CONSTRAINT fk_solicitudes_adoptante
        FOREIGN KEY (id_adoptante)
        REFERENCES usuarios (id_usuario)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_solicitudes_mascota
        FOREIGN KEY (id_mascota)
        REFERENCES mascotas (id_mascota)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT chk_solicitudes_resolucion
        CHECK (
            (
                estado = 'PENDIENTE'
                AND fecha_resolucion IS NULL
            )
            OR
            (
                estado IN ('ACEPTADA', 'RECHAZADA', 'CANCELADA')
                AND fecha_resolucion IS NOT NULL
            )
        ),

    CONSTRAINT chk_solicitudes_fechas
        CHECK (
            fecha_resolucion IS NULL
            OR fecha_resolucion >= fecha_solicitud
        )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- =====================================================
-- MENSAJES
-- =====================================================

CREATE TABLE IF NOT EXISTS mensajes (
    id_mensaje INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_solicitud INT UNSIGNED NOT NULL,
    id_emisor INT UNSIGNED NOT NULL,

    contenido VARCHAR(1000) NOT NULL,
    fecha_envio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    KEY idx_mensajes_conversacion (
        id_solicitud,
        fecha_envio,
        id_mensaje
    ),

    KEY idx_mensajes_emisor (id_emisor),

    CONSTRAINT fk_mensajes_solicitud
        FOREIGN KEY (id_solicitud)
        REFERENCES solicitudes (id_solicitud)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT fk_mensajes_emisor
        FOREIGN KEY (id_emisor)
        REFERENCES usuarios (id_usuario)
        ON DELETE RESTRICT
        ON UPDATE RESTRICT,

    CONSTRAINT chk_mensajes_contenido
        CHECK (CHAR_LENGTH(TRIM(contenido)) > 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;