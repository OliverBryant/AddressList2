package com.example.addresslist.encrypt;

import com.example.addresslist.encrypt.binary.Base64;
import com.example.addresslist.encrypt.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class AesUtil {

    private static final String ENCODING_CODE = "UTF-8";
    private static final String SECURE_RANDOM = "SHA1PRNG";
    private static final String AES_PADDING = "AES/CBC/PKCS5Padding";

    public static String encrypt(String content,String key) throws EncrypException{
        try {
            Cipher cipher = Cipher.getInstance(AES_PADDING);
            SecretKeySpec secretKeySpec = new SecretKeySpec(Hex.decodeHex(key.toCharArray()), "AES");
            IvParameterSpec iv = getSecretIv(16);
            cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec,iv);
            byte[] entrypted = cipher.doFinal(content.getBytes(ENCODING_CODE));
            return new String(Hex.encodeHex(iv.getIV())).concat(Base64.encodeBase64String(entrypted));
        }catch (Exception e){
            e.printStackTrace();
            throw new EncrypException("-10007",e);
        }
    }

    public static String decrypt(String content,String key) throws EncrypException{
        try {
            String ivStr = content.substring(0,32);
            String message = content.substring(32);

            SecretKeySpec secretKeySpec = new SecretKeySpec(Hex.decodeHex(key.toCharArray()),"AES");
            Cipher cipher = Cipher.getInstance(AES_PADDING);
            IvParameterSpec iv = new IvParameterSpec(Hex.decodeHex(ivStr.toCharArray()));
            cipher.init(Cipher.DECRYPT_MODE,secretKeySpec,iv);

            byte[] original = cipher.doFinal(Base64.decodeBase64(message));
            return new String(original,ENCODING_CODE);
        } catch (Exception e){
            e.printStackTrace();
            throw new EncrypException("-10008",e);
        }
    }

    private static IvParameterSpec getSecretIv(int length) {
        byte[] ivBytes = new byte[length];
        try {
            SecureRandom secureRandom = SecureRandom.getInstance(SECURE_RANDOM);
            secureRandom.nextBytes(ivBytes);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new IvParameterSpec(ivBytes);
    }

    public static String getAESSecureKey() {
        String aesKey = "";
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            SecretKey secretKey = keyGenerator.generateKey();
            aesKey = Hex.encodeHexString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return aesKey;
    }
}
