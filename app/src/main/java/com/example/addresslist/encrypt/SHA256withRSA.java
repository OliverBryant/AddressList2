package com.example.addresslist.encrypt;

import com.example.addresslist.encrypt.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

public class SHA256withRSA {
    public static final String KEY_ALGORITHM="RSA";
    public static final String SIGNATURE_ALGORITHM="SHA256withRSA";


    public static Map<String,String> generateKeyBytes() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            keyPairGenerator.initialize(2048);                                                  //
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            Map<String ,String> keyMap = new HashMap<>();
            keyMap.put("PUBLIC_KEY", Base64.encodeBase64String(publicKey.getEncoded()));
            keyMap.put("PRIVATE_KEY", Base64.encodeBase64String(privateKey.getEncoded()));
            return keyMap;
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            System.out.println("Fail to verify signature!");
        }
        return null;
    }

    public static PublicKey restorePublicKey(String publicKey) throws EncrypException{
        byte[] prikeyByte;
        PublicKey Publickey = null;
        try {
            prikeyByte = Base64.decodeBase64(publicKey.getBytes(StandardCharsets.UTF_8));
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(prikeyByte);
            KeyFactory factory = KeyFactory.getInstance((KEY_ALGORITHM));

            Publickey = factory.generatePublic(x509EncodedKeySpec);
            return Publickey;
        } catch (Exception e){
            e.printStackTrace();
            throw new EncrypException("-10000",e);
        }
    }

    public static PrivateKey restorePrivateKey(String privateKey) throws EncrypException{
        byte[] prikeyByte;
        PrivateKey PrivateKey;
        try {
            prikeyByte = Base64.decodeBase64(privateKey.getBytes(StandardCharsets.UTF_8));
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(prikeyByte);
            KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
            PrivateKey = factory.generatePrivate(pkcs8EncodedKeySpec);
            return PrivateKey;
        } catch (Exception e){
            e.printStackTrace();
            throw new EncrypException("-10001",e);
        }
    }

    public static String sign(String privateKey, String plainText) throws EncrypException{
        try {
            PrivateKey restorePrivateKey = restorePrivateKey(privateKey);
            Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
            sign.initSign(restorePrivateKey);
            sign.update(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] signByte = sign.sign();
            return Base64.encodeBase64String(signByte);
        } catch (Exception e){
            e.printStackTrace();
            throw new EncrypException("-10003",e);
        }
    }

    public static Boolean verifySign(String publicKey,String plainText,String signedText) throws EncrypException{
        try {
            PublicKey restorePublicKey = restorePublicKey(publicKey);
            Signature verifySign = Signature.getInstance(SIGNATURE_ALGORITHM);
            verifySign.initVerify(restorePublicKey);
            verifySign.update(plainText.getBytes(StandardCharsets.UTF_8));
            return verifySign.verify(Base64.decodeBase64(signedText.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e){
            e.printStackTrace();
            throw new EncrypException("-10004",e);
        }
    }

   /* public static String writeToString (Object obj) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        String setStr = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(obj);
            setStr = byteArrayOutputStream.toString(StandardCharsets.ISO_8859_1);
            setStr = java.net.URLEncoder.encode(setStr,StandardCharsets.UTF_8);
        }catch (IOException e){
            e.printStackTrace();
        }finallfiles('libs\\gson-2.6.2.jar')y {
            objectOutputStream.close();
        }
        return setStr;
    }

    public static Object deserializeFromString (String str) throws IOException{
        ByteArrayInputStream byteArrayInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            String deserStr = java.net.URLDecoder.decode(str,StandardCharsets.UTF_8);
            byteArrayInputStream = new ByteArrayInputStream(deserStr.getBytes(StandardCharsets.ISO_8859_1));
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return objectInputStream.readObject();
        }catch (IOException e){
            e.printStackTrace();
        }catch (ClassNotFoundException c){
            c.printStackTrace();
        }finally {
            objectInputStream.close();
            byteArrayInputStream.close();
        }
        return null;
    }*/

   /* public static void main(String[] args) throws EncrypException, IOException {
        Map<String,String> map = generateKeyBytes();
        String public_key = map.get("PUBLIC_KEY");
        String private_key = map.get("PRIVATE_KEY");
        System.out.println(public_key);
        System.out.println(private_key);
        String plainText = "oliver";

//        JsonObject busDataJson = new JsonObject();
//        busDataJson.addProperty("plainText",plainText);
//        String signInfo = SHA256withRSA.sign(KeyBytes.PRIVATE_KEY,plainText);
//        busDataJson.addProperty("signInfo",signInfo);
//        System.out.println("签名信息：" + signInfo);
//
//        String aesKey = AesUtil.getAESSecureKey();
//        System.out.println("密钥明文：" + aesKey);
//
//        String encryptedAesKey = RsaUtil.encrypt(aesKey,KeyBytes.PUBLIC_KEY);
//        System.out.println("密钥密文：" + encryptedAesKey);
//
//        String encrypt = AesUtil.encrypt(busDataJson.toString(),aesKey);
//        System.out.println("加密数据：" + encrypt);
//
//        JsonObject root = new JsonObject();
//        root.addProperty("encryptKey",encryptedAesKey);
//        root.addProperty("busData",encrypt);
//        System.out.println("发送数据："+root.toString());
//
//        System.out.println("======================以下是解密=======================");
//
//        String encryptKey = root.get("encryptKey").getAsString();
//        String decryptKey = RsaUtil.decrypt(encryptKey,KeyBytes.PRIVATE_KEY);
//        System.out.println("解密后的密钥：" + decryptKey);
//
//        String busData = root.get("busData").getAsString();
//        String decryptBusData = AesUtil.decrypt(busData,decryptKey);
//
//        JsonObject respDataJsonObject = JsonParser.parseString(decryptBusData).getAsJsonObject();
//        System.out.println("解密后的数据：" + respDataJsonObject.toString());
//
//        String decryptPlainText = respDataJsonObject.get("plainText").getAsString();
//        String decryptSingInfo = respDataJsonObject.get("signInfo").getAsString();
//        boolean verifySign = SHA256withRSA.verifySign(KeyBytes.PUBLIC_KEY,decryptPlainText,decryptSingInfo);
//        System.out.println("是否验签通过：" + verifySign);

        *//*SimContactor simContactor = new SimContactor();
        simContactor.setImageId("213");
        simContactor.setName("oliver");

        JSONObject jsonObject = JSONObject.fromObject(simContactor);
        System.out.println(jsonObject);

        JSONObject jsonObject1=JSONObject.fromObject(jsonObject);
        SimContactor simContactor1 = (SimContactor) JSONObject.toBean(jsonObject,SimContactor.class);
        System.out.println(simContactor1.toString());*//*
    }*/

}
