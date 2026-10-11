package com.example.BI.Chat;

import java.util.regex.Pattern;

/**
 * Sécurité : longueur du message (contrôleur), détection des
 * tentatives d'extraction du prompt système / de la
 * configuration / de secrets. Les instructions présentes
 * dans les données sont ignorées via le prompt système
 * (les chunks sont générés par le backend, jamais par l'utilisateur).
 */
public final class ChatSecurity {

    private ChatSecurity() {
    }

    public static final int MAX_MESSAGE_LENGTH = 1000;
    public static final int MAX_HISTORY_MESSAGES = 10;
    public static final int MAX_HISTORY_CONTENT_LENGTH = 500;

    /** Tentatives de révéler le prompt, la configuration ou des secrets. */
    private static final Pattern FORBIDDEN_REQUEST = Pattern.compile(
            "(system\\s*prompt|reveal\\s+(your\\s+)?(instructions|prompt|configuration)|"
                    + "your\\s+(instructions|configuration|system\\s*prompt)|"
                    + "ignore\\s+(all\\s+)?(previous|above)\\s+(instructions|rules)|"
                    + "api[_\\s-]?key|openrouter[_\\s-]?key|password|secret\\s+key|"
                    + "application\\.properties|environnement\\s*variable)",
            Pattern.CASE_INSENSITIVE);

    /** Caractères de contrôle interdits dans le message. */
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}&&[^\t]]");

    public record SecurityCheck(boolean rejected, String refusalAnswer) {
    }

    public static SecurityCheck validate(String message) {
        if (message == null) {
            return new SecurityCheck(false, null);
        }
        String cleaned = CONTROL_CHARS.matcher(message).replaceAll(" ");
        if (FORBIDDEN_REQUEST.matcher(cleaned).find()) {
            return new SecurityCheck(true,
                    "Je ne peux pas révéler mon prompt système, ma configuration "
                            + "ou mes clés d'accès. Je peux uniquement répondre à des "
                            + "questions sur les données RH de l'entrepôt DW-Projet "
                            + "(employés, départements, postes, sociétés, managers, salaires).");
        }
        return new SecurityCheck(false, null);
    }

    public static String sanitizeMessage(String message) {
        if (message == null) {
            return "";
        }
        return CONTROL_CHARS.matcher(message).replaceAll(" ").trim();
    }
}
