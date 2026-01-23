package com.example.chat.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class AdminDetectionService {
    private static final Logger logger = LoggerFactory.getLogger(AdminDetectionService.class);

    private static final Pattern ADMIN_REQUEST_PATTERN = Pattern.compile(
        "\\b(admin|administrator|operator|agent|human|persoana|persoane)\\b|" +
        "(vr[eă]u\\s+s[ăa]\\s+vorbesc\\s+cu|vr[eă]u\\s+s[ăa]\\s+discut\\s+cu)|" +
        "(conectaz|escalez|transport|transfer)\\s+(la\\s+)?(admin|operator|agent)|" +
        "(contact\\s+(admin|operator|agent|persoana))",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
    );

    public boolean isAdminRequestExplicit(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }

        String lower = message.toLowerCase(Locale.ROOT);
        logger.debug("[ADMIN-DETECTION] Checking message for admin request: {}", message);

        if (ADMIN_REQUEST_PATTERN.matcher(lower).find()) {
            logger.info("[ADMIN-DETECTION] Admin request explicitly detected");
            return true;
        }

        logger.debug("[ADMIN-DETECTION] No admin request detected");
        return false;
    }
}

