package com.hatokuse.protocol;

/**
 * HaToKuSe (Hata-Tolere Kuyruk Servisi) Protokolü
 * Text tabanlı istemci-lider haberleşmesi için kullanılır.
 * 
 * Komutlar:
 * - SET <message_id> <message>
 * - GET <message_id>
 * - DEL <message_id>
 * 
 * Yanıtlar:
 * - OK
 * - OK <message>
 * - ERROR <error_message>
 */
public class HaToKuSeProtocol {

    // Komut tipleri
    public static final String CMD_SET = "SET";
    public static final String CMD_GET = "GET";
    public static final String CMD_DEL = "DEL";

    // Yanıt tipleri
    public static final String RESP_OK = "OK";
    public static final String RESP_ERROR = "ERROR";

    /**
     * SET komutu için istek oluşturur.
     */
    public static String createSetCommand(String messageId, String message) {
        return CMD_SET + " " + messageId + " " + message;
    }

    /**
     * GET komutu için istek oluşturur.
     */
    public static String createGetCommand(String messageId) {
        return CMD_GET + " " + messageId;
    }

    /**
     * DEL komutu için istek oluşturur.
     */
    public static String createDelCommand(String messageId) {
        return CMD_DEL + " " + messageId;
    }

    /**
     * Başarılı yanıt oluşturur.
     */
    public static String createOkResponse() {
        return RESP_OK;
    }

    /**
     * Başarılı yanıt (mesaj ile) oluşturur.
     */
    public static String createOkResponse(String message) {
        return RESP_OK + " " + message;
    }

    /**
     * Hata yanıtı oluşturur.
     */
    public static String createErrorResponse(String errorMessage) {
        return RESP_ERROR + " " + errorMessage;
    }

    /**
     * Gelen komutu parse eder.
     */
    public static ParsedCommand parseCommand(String rawCommand) {
        if (rawCommand == null || rawCommand.trim().isEmpty()) {
            return new ParsedCommand(null, null, null, "Boş komut");
        }

        String command = rawCommand.trim();
        String[] parts = command.split(" ", 3);

        if (parts.length == 0) {
            return new ParsedCommand(null, null, null, "Geçersiz komut formatı");
        }

        String cmdType = parts[0].toUpperCase();

        switch (cmdType) {
            case CMD_SET:
                if (parts.length < 3) {
                    return new ParsedCommand(CMD_SET, null, null, "SET komutu için format: SET <message_id> <message>");
                }
                return new ParsedCommand(CMD_SET, parts[1], parts[2], null);

            case CMD_GET:
                if (parts.length < 2) {
                    return new ParsedCommand(CMD_GET, null, null, "GET komutu için format: GET <message_id>");
                }
                return new ParsedCommand(CMD_GET, parts[1], null, null);

            case CMD_DEL:
                if (parts.length < 2) {
                    return new ParsedCommand(CMD_DEL, null, null, "DEL komutu için format: DEL <message_id>");
                }
                return new ParsedCommand(CMD_DEL, parts[1], null, null);

            default:
                return new ParsedCommand(null, null, null, "Bilinmeyen komut: " + cmdType);
        }
    }

    /**
     * Gelen yanıtı parse eder.
     */
    public static ParsedResponse parseResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return new ParsedResponse(false, null, "Boş yanıt");
        }

        String response = rawResponse.trim();

        if (response.startsWith(RESP_OK)) {
            String message = response.length() > 3 ? response.substring(3).trim() : null;
            return new ParsedResponse(true, message, null);
        } else if (response.startsWith(RESP_ERROR)) {
            String error = response.length() > 6 ? response.substring(6).trim() : "Bilinmeyen hata";
            return new ParsedResponse(false, null, error);
        } else {
            return new ParsedResponse(false, null, "Geçersiz yanıt formatı");
        }
    }

    /**
     * Parse edilmiş komut.
     */
    public static class ParsedCommand {
        private final String commandType;
        private final String messageId;
        private final String messageContent;
        private final String error;

        public ParsedCommand(String commandType, String messageId, String messageContent, String error) {
            this.commandType = commandType;
            this.messageId = messageId;
            this.messageContent = messageContent;
            this.error = error;
        }

        public boolean isValid() {
            return error == null;
        }

        public String getCommandType() {
            return commandType;
        }

        public String getMessageId() {
            return messageId;
        }

        public String getMessageContent() {
            return messageContent;
        }

        public String getError() {
            return error;
        }

        public boolean isSet() {
            return CMD_SET.equals(commandType);
        }

        public boolean isGet() {
            return CMD_GET.equals(commandType);
        }

        public boolean isDel() {
            return CMD_DEL.equals(commandType);
        }
    }

    /**
     * Parse edilmiş yanıt.
     */
    public static class ParsedResponse {
        private final boolean success;
        private final String message;
        private final String error;

        public ParsedResponse(boolean success, String message, String error) {
            this.success = success;
            this.message = message;
            this.error = error;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getError() {
            return error;
        }
    }
}
