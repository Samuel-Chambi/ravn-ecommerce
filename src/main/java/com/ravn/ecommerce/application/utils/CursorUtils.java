package com.ravn.ecommerce.application.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.ScrollPosition;

import java.util.Base64;
import java.util.Map;

/**
 * Shared utility for encoding/decoding cursor-based pagination positions.
 * Serializes keyset scroll positions as Base64-encoded JSON to ensure
 * proper round-trip encoding.
 */
@Slf4j
public final class CursorUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private CursorUtils() {
    }

    /**
     * Encode a ScrollPosition to a Base64 cursor string.
     * Returns null if position is not a KeysetScrollPosition.
     */
    public static String encode(ScrollPosition position) {
        if (position instanceof KeysetScrollPosition keyset) {
            try {
                Map<String, Object> keys = keyset.getKeys();
                String json = OBJECT_MAPPER.writeValueAsString(keys);
                return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes());
            } catch (JsonProcessingException e) {
                log.error("Failed to encode scroll position: {}", e.getMessage());
                return null;
            }
        }
        return null;
    }

    /**
     * Decode a Base64 cursor string back to a ScrollPosition.
     * Returns ScrollPosition.offset() if the cursor is invalid.
     */
    public static ScrollPosition decode(String cursor) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cursor);
            String json = new String(decoded);
            Map<String, Object> keys = OBJECT_MAPPER.readValue(json, new TypeReference<>() {
            });
            return ScrollPosition.forward(keys);
        } catch (Exception e) {
            log.warn("Invalid cursor '{}', starting from beginning: {}", cursor, e.getMessage());
            return ScrollPosition.offset();
        }
    }
}
