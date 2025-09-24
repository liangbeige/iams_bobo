package com.iams.minio.service;

import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.util.Base64;

import static com.iams.framework.datasource.DynamicDataSourceContextHolder.log;

// EncryptionService.java
@Service
public class EncryptionService {

    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    public InputStream decryptStream(InputStream encryptedStream,
                                     String base64Key,
                                     String base64Iv) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    Base64.getDecoder().decode(base64Key), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(
                    Base64.getDecoder().decode(base64Iv));

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            return new CipherInputStream(encryptedStream, cipher);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    // EncryptionService.java
    public SecretKeySpec deriveKey(String fileHash, String hexSalt) throws Exception {
        char[] password = ("secure-passphrase-" + fileHash).toCharArray();
        byte[] salt = Hex.decodeHex(hexSalt);
        int iterations = 100000;
        int keyLength = 256;

        PBEKeySpec spec = new PBEKeySpec(
                password,
                salt,
                iterations,
                keyLength
        );
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();

        return new SecretKeySpec(keyBytes, "AES");
    }
}
