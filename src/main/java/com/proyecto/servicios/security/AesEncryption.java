package com.proyecto.servicios.security;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public final class AesEncryption {

    private AesEncryption() {
    }

    public static String cipher(String mode, String input, String ivBase64, String keyBase64) {
        if (input == null || ivBase64 == null || keyBase64 == null) {
            return null;
        }
        try {
            byte[] iv = Base64.getDecoder().decode(ivBase64);
            byte[] key = Base64.getDecoder().decode(keyBase64);
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            SecretKey secretKey = new SecretKeySpec(key, 0, key.length, "AES");

            if ("ENCRYPT".equalsIgnoreCase(mode)) {
                return encrypt(input, secretKey, ivspec);
            } else if ("DECRYPT".equalsIgnoreCase(mode)) {
                return decrypt(input, secretKey, ivspec);
            }
        } catch (Exception e) {
            log.error("Error al procesar cipher AES ({})", e.getClass().getSimpleName());
        }
        return null;
    }

    public static String encrypt(String input, SecretKey secretKey, IvParameterSpec ivspec) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.error("Error criptografico al cifrar payload ({})", e.getClass().getSimpleName());
            return null;
        }
    }

    public static String decrypt(String input, SecretKey secretKey, IvParameterSpec ivspec) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(input)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.debug("No fue posible descifrar la cadena recibida ({})", e.getClass().getSimpleName());
            return null;
        }
    }
}
