package com.example.addresslist.encrypt;

import com.example.addresslist.encrypt.binary.Base64;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RsaUtil {

    private static final String ALGORITHM = "RSA";
    private static final String PADDING = "RSA/ECB/PKCS1Padding";

    public static String encrypt(String data, String pubKey) throws EncrypException{
        try {
            byte[] decoded = Base64.decodeBase64(pubKey);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(decoded));

            Cipher cipher = Cipher.getInstance(PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            int splitLength = publicKey.getModulus().bitLength() / 8 - 11;
            byte[][] arrays = RsaUtil.splitBytes(data.getBytes(),splitLength);
            StringBuilder stringBuffer = new StringBuilder();
            for (byte[] array: arrays
                 ) {
                stringBuffer.append(RsaUtil.bytesToHexString(cipher.doFinal(array)));
            }
            return stringBuffer.toString();
        } catch (Exception e){
            System.out.println("RSA 加密失败");
            e.printStackTrace();
            throw new EncrypException("-10004",e);
        }
    }

    public static String decrypt(String data,String priKey) throws EncrypException{
        try {
            KeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(priKey));
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);

            Cipher cipher = Cipher.getInstance(PADDING);
            cipher.init(Cipher.DECRYPT_MODE,privateKey);

            int splitLength = privateKey.getModulus().bitLength()/8;
            byte[] contentBytes = hexStringToBytes(data);
            byte[][] arrays = RsaUtil.splitBytes(contentBytes,splitLength);
            StringBuilder stringBuffer = new StringBuilder();
            for (byte[] array :
                    arrays) {
                stringBuffer.append(new String(cipher.doFinal(array)));
            }
            return stringBuffer.toString();
        }catch (Exception e){
            System.out.println("RSA 解密失败");
            e.printStackTrace();
            throw new EncrypException("-10009", e);
        }
    }

    private static byte[] hexStringToBytes(String hex) {
        int len = (hex.length() / 2);
        hex = hex.toUpperCase();
        byte[] result = new byte[len];
        char[] chars = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i*2;
            result[i] = (byte) (toByte(chars[pos]) << 4 | toByte(chars[pos+1]));
        }
        return result;
    }

    private static byte toByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder(bytes.length);
        String temp;
        for (byte aByte :
                bytes) {
            temp = Integer.toHexString(0xFF & aByte);
            if (temp.length() < 2){
                stringBuilder.append(0);
            }
            stringBuilder.append(temp);
        }
        return stringBuilder.toString();
    }

    private static byte[][] splitBytes(byte[] bytes, int splitLength) {
        
        int remainder = bytes.length % splitLength;
        
        int quotient = remainder != 0 ? bytes.length / splitLength + 1 : bytes.length / splitLength;
        byte[][] arrays = new byte[quotient][];
        byte[] array = null;
        for (int i = 0; i < quotient; i++) {
            if (i == quotient - 1 && remainder != 0 ){
                array = new byte[remainder];
                System.arraycopy(bytes,i*splitLength,array,0,remainder);
            }else {
                array = new byte[splitLength];
                System.arraycopy(bytes,i*splitLength,array,0,splitLength);
            }
            arrays[i] = array;
        }
        return arrays;
    }
    
    
}
