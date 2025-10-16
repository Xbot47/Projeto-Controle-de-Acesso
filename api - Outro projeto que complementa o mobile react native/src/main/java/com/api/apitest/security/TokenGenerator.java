package com.api.apitest.security;

import java.util.Calendar;
import java.util.Base64;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class TokenGenerator {

    // CHAVE MESTRA SECRETA — personalize para seu projeto
    private static final String MASTER_SECRET = "SUPER_CHAVE_INVIOLAVEL_DO_PROJETO_2025";

    /**
     * Gera o token válido para a semana atual.
     * @return Token de 16 caracteres.
     */
    public static String generateWeeklyToken() {
        try {
            // Pega a semana atual do ano (1–52)
            int weekOfYear = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);

            // Junta segredo + semana
            String dataToHash = MASTER_SECRET + weekOfYear;

            // Cria hash SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));

            // Codifica em Base64 e retorna 16 primeiros caracteres
            return Base64.getEncoder().encodeToString(hash).substring(0, 16);
        } catch (Exception e) {
            return "ERROR_TOKEN_GENERATION";
        }
    }
}
