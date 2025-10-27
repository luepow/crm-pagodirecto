package com.pagodirecto.core.exception;

import java.util.UUID;

/**
 * Exception thrown when a requested entity is not found.
 */
public class EntityNotFoundException extends BusinessException {

    public EntityNotFoundException(String entityName, UUID id) {
        super("ENTITY_NOT_FOUND",
              String.format("%s with id %s not found", entityName, id));
    }

    public EntityNotFoundException(String entityName, String field, Object value) {
        super("ENTITY_NOT_FOUND",
              String.format("%s with %s %s not found", entityName, field, value));
    }

    public EntityNotFoundException(String message) {
        super("ENTITY_NOT_FOUND", message);
    }
}
