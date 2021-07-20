package com.mxny.ss.util;


import javax.crypto.Cipher;
import java.io.IOException;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 非对称加密算法RSA算法组件
 * 非对称算法一般是用来传送对称加密算法的密钥来使用的，相对于DH算法，RSA算法只需要一方构造密钥，不需要
 * 大费周章的构造各自本地的密钥对了。DH算法只能算法非对称算法的底层实现。而RSA算法算法实现起来较为简单
 *
 * @author asiamaster
 */
public class RSAUtils {
    //非对称密钥算法
    public static final String KEY_ALGORITHM = "RSA";
    //RSA密钥对
    private static RSAKeyPair rsaKeyPair;
    /**
     * 密钥长度，DH算法的默认密钥长度是1024
     * 密钥长度必须是64的倍数，在512到65536位之间
     * 512的长度为53，1024的长度为117，2048的长度为245
     */
    private static final int KEY_SIZE = 1024;
    //公钥
    private static final String PUBLIC_KEY = "RSAPublicKey";
    //私钥
    private static final String PRIVATE_KEY = "RSAPrivateKey";

    /**
     * 获取并初始化公私钥
     * @return
     * @throws Exception
     */
    public static RSAKeyPair getRSAKeyPair() throws NoSuchAlgorithmException {
        if(rsaKeyPair == null){
            synchronized (RSAKeyPair.class){
                if(rsaKeyPair == null) {
                    rsaKeyPair = new RSAKeyPair();
                    Map<String, Object> map = createKey();
                    rsaKeyPair.setPrivateKey((RSAPrivateKey) map.get(PRIVATE_KEY));
                    rsaKeyPair.setPublicKey((RSAPublicKey) map.get(PUBLIC_KEY));
                }
            }
        }
        return rsaKeyPair;
    }

    /**
     * 根据key获取并初始化公私钥
     * @param publicKey
     * @param privateKey
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static RSAKeyPair getRSAKeyPair(String publicKey, String privateKey) throws Exception {
        if(rsaKeyPair == null){
            synchronized (RSAKeyPair.class){
                if(rsaKeyPair == null) {
                    rsaKeyPair = new RSAKeyPair();
                    rsaKeyPair.setPublicKey(getPublicKey(publicKey));
                    rsaKeyPair.setPrivateKey(getPrivateKey(privateKey));
                }
            }
        }
        return rsaKeyPair;
    }

    /**
     * 根据key创建公私钥
     * @param publicKey
     * @param privateKey
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static synchronized RSAKeyPair createRSAKeyPair(String publicKey, String privateKey) throws Exception {
        RSAKeyPair rsaKeyPair = new RSAKeyPair();
        rsaKeyPair.setPublicKey(getPublicKey(publicKey));
        rsaKeyPair.setPrivateKey(getPrivateKey(privateKey));
        return rsaKeyPair;
    }

    /**
     * 创建密钥对
     *
     * @return Map 甲方密钥的Map
     */
    public static Map<String, Object> createKey() throws NoSuchAlgorithmException {
        //实例化密钥生成器
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        //初始化密钥生成器
        keyPairGenerator.initialize(KEY_SIZE);
        //生成密钥对
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        //甲方公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        //甲方私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        //将密钥存储在map中
        Map<String, Object> keyMap = new HashMap<String, Object>();
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }

    /**
     * 创建密钥对
     * 指定密钥长度
     *
     * @return Map 甲方密钥的Map
     */
    public static Map<String, Object> createKey(int KEY_SIZE) throws Exception {
        //实例化密钥生成器
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        //初始化密钥生成器
        keyPairGenerator.initialize(KEY_SIZE);
        //生成密钥对
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        //甲方公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        //甲方私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        //将密钥存储在map中
        Map<String, Object> keyMap = new HashMap<String, Object>();
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }


    /**
     * 获得公钥byte[]
     * @param keyMap
     * @return
     * @throws Exception
     */
    public static byte[] getPublicKeyBytes(Map<String, Object> keyMap) throws Exception {
        //获得map中的公钥对象 转为key对象
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        //编码返回字符串
        return key.getEncoded();
    }

    /**
     * 获得私钥byte[]
     * @param keyMap
     * @return
     * @throws Exception
     */
    public static byte[] getPrivateKeyBytes(Map<String, Object> keyMap) throws Exception {
        //获得map中的私钥对象 转为key对象
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        //编码返回字符串
        return key.getEncoded();
    }

    /**
     * 获得公钥String
     * @param keyMap
     * @return
     * @throws Exception
     */
    public static String getPublicKey(Map<String, Object> keyMap) throws Exception {
        //获得map中的公钥对象 转为key对象
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        //编码返回字符串
        return encryptBASE64(key.getEncoded());
    }

    /**
     * 获得公钥String
     * @param rsaKeyPair
     * @return
     * @throws Exception
     */
    public static String getPublicKey(RSAKeyPair rsaKeyPair) {
        //获得map中的公钥对象 转为key对象
        Key key = rsaKeyPair.getPublicKey();
        //编码返回字符串
        return encryptBASE64(key.getEncoded());
    }

    /**
     * 获得私钥String
     * @param keyMap
     * @return
     * @throws Exception
     */
    public static String getPrivateKey(Map<String, Object> keyMap) {
        //获得map中的私钥对象 转为key对象
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        //编码返回字符串
        return encryptBASE64(key.getEncoded());
    }

    /**
     * 得到公钥
     * @param key 密钥字符串（经过base64编码）
     * @throws Exception
     */
    public static RSAPublicKey getPublicKey(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        RSAPublicKey publicKey =  (RSAPublicKey)keyFactory.generatePublic(keySpec);
        return publicKey;
    }
    /**
     * 得到私钥
     * @param key 密钥字符串（经过base64编码）
     * @throws Exception
     */
    public static RSAPrivateKey getPrivateKey(String key) throws Exception {
        byte[] keyBytes;
        keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        RSAPrivateKey privateKey = (RSAPrivateKey)keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

    /**
     * 获得私钥String
     * @param rsaKeyPair
     * @return
     * @throws Exception
     */
    public static String getPrivateKey(RSAKeyPair rsaKeyPair) {
        //获得map中的私钥对象 转为key对象
        Key key = rsaKeyPair.getPrivateKey();
        //编码返回字符串
        return encryptBASE64(key.getEncoded());
    }

    //解码返回byte
    public static byte[] decryptBASE64(String key) throws IOException {
        return Base64.getDecoder().decode(key);
    }

    //编码返回字符串
    public static String encryptBASE64(byte[] key) {
        return Base64.getEncoder().encodeToString(key);
    }

    /**
     * 私钥加密
     *
     * @param data 待加密数据
     * @param key       密钥
     * @return byte[] 加密数据
     */
    public static byte[] encryptByPrivateKey(byte[] data, byte[] key) throws Exception {
        //取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(key);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        //生成私钥
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        //数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    /**
     * 公钥加密
     *
     * @param data 待加密数据
     * @param key       密钥
     * @return byte[] 加密数据
     */
    public static byte[] encryptByPublicKey(byte[] data, byte[] key) throws Exception {
        //实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        //初始化公钥
        //密钥材料转换
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(key);
        //产生公钥
        PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
        //数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return cipher.doFinal(data);
    }

    /**
     * 私钥解密
     *
     * @param data 待解密数据
     * @param key  密钥
     * @return byte[] 解密数据
     */
    public static byte[] decryptByPrivateKey(byte[] data, byte[] key) throws Exception {
        //取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(key);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        //生成私钥
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        //数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    /**
     * 公钥解密
     *
     * @param data 待解密数据
     * @param key  密钥
     * @return byte[] 解密数据
     */
    public static byte[] decryptByPublicKey(byte[] data, byte[] key) throws Exception {
        //实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        //初始化公钥
        //密钥材料转换
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(key);
        //产生公钥
        PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
        //数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, pubKey);
        return cipher.doFinal(data);
    }


    /**
     * 获取C#版的私钥
     * @param encodedPrivkey
     * @return
     */
    public static String getRSAPrivateKeyAsNetFormat(byte[] encodedPrivkey) {
        try {
            StringBuffer buff = new StringBuffer(2048);
            PKCS8EncodedKeySpec pvkKeySpec = new PKCS8EncodedKeySpec(encodedPrivkey);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            RSAPrivateCrtKey pvkKey = (RSAPrivateCrtKey) keyFactory.generatePrivate(pvkKeySpec);
            buff.append("<RSAKeyValue>");
            buff.append("<Modulus>" + org.apache.commons.codec.binary.Base64.encodeBase64String(removeMSZero(pvkKey.getModulus().toByteArray())) + "</Modulus>");
            buff.append("<Exponent>" + org.apache.commons.codec.binary.Base64.encodeBase64String(removeMSZero(pvkKey.getPublicExponent().toByteArray())) + "</Exponent>");
            buff.append("<P>" + org.apache.commons.codec.binary.Base64.encodeBase64String(removeMSZero(pvkKey.getPrimeP().toByteArray())) + "</P>");
            buff.append("<Q>" + org.apache.commons.codec.binary.Base64.encodeBase64String(removeMSZero(pvkKey.getPrimeQ().toByteArray())) + "</Q>");
            buff.append("<DP>" + org.apache.commons.codec.binary.Base64.encodeBase64String(removeMSZero(pvkKey.getPrimeExponentP().toByteArray())) + "</DP>");
            buff.append("<DQ>" + org.apache.commons.codec.binary.Base64.encodeBase64String(removeMSZero(pvkKey.getPrimeExponentQ().toByteArray())) + "</DQ>");
            buff.append("<InverseQ>" + org.apache.commons.codec.binary.Base64.encodeBase64String(removeMSZero(pvkKey.getCrtCoefficient().toByteArray())) + "</InverseQ>");
            buff.append("<D>" + org.apache.commons.codec.binary.Base64.encodeBase64String(removeMSZero(pvkKey.getPrivateExponent().toByteArray())) + "</D>");
            buff.append("</RSAKeyValue>");
            return buff.toString().replaceAll("[ \t\n\r]", "");
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }
    }

    /**
     * 获取C#版的公钥
     * @param encodedPrivkey
     * @return
     */
    public static String getRSAPublicKeyAsNetFormat(byte[] encodedPrivkey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(encodedPrivkey));
            return getRSAPublicKeyAsNetFormat(pubKey);
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }
    }

    /**
     * 获取C#版的公钥
     * @param pubKey
     * @return
     */
    public static String getRSAPublicKeyAsNetFormat(RSAPublicKey pubKey) {
        StringBuilder sb = new StringBuilder();
        sb.append("<RSAKeyValue>");
        sb.append("<Modulus>").append(org.apache.commons.codec.binary.Base64.encodeBase64String(removeMSZero(pubKey.getModulus().toByteArray()))).append("</Modulus>");
        sb.append("<Exponent>").append(org.apache.commons.codec.binary.Base64.encodeBase64String(removeMSZero(pubKey.getPublicExponent().toByteArray()))).append("</Exponent>");
        sb.append("</RSAKeyValue>");
        return sb.toString().replaceAll("[ \t\n\r]", "");
    }

    private static byte[] removeMSZero(byte[] data) {
        byte[] data1;
        int len = data.length;
        if (data[0] == 0) {
            data1 = new byte[data.length - 1];
            System.arraycopy(data, 1, data1, 0, len - 1);
        } else {
            data1 = data;
        }
        return data1;
    }

    // ====================================================================================


    /**
     * 获取密钥
     * @param args
     */
    public static void main0(String[] args) {
        String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCz4D01cJbbLdzUprznyrz4bueMWkLZNSBHuxXjynn4WnaELTidvA6280h7WHP+87iNmZAtvrmcEWGPCBvGrNRFzpqtN7c8h6E12SESVWjuF4VkH/tUN/F4UJLtNPEnsmmVAdarwn/c5RJqFVA2sFVlm6Zc2FV3QyPdrdMfa9AizwIDAQAB";
        String PRIVATE_KEY = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALPgPTVwltst3NSmvOfKvPhu54xaQtk1IEe7FePKefhadoQtOJ28DrbzSHtYc/7zuI2ZkC2+uZwRYY8IG8as1EXOmq03tzyHoTXZIRJVaO4XhWQf+1Q38XhQku008SeyaZUB1qvCf9zlEmoVUDawVWWbplzYVXdDI92t0x9r0CLPAgMBAAECgYEAqCPLc4G8MkOLsmfuG0njHOMmpIbXCAzmEMcr7hOdse517JYM3z0kEBYXwdzsCP0vnYVXRbuL6vxAUqBEvpFdlhMYDNeDbKlqfWbvAa2RP6stib4OWR85gYbssRn3kh4IY1VWn+GeSbc5ztjSVXKnRbS+ezd0OmXJqiKzPpQtNMECQQDylOWkFeKgegAEzMXM/9VjjgXFoNb8AJVT8QXj2/m4ndL17/n4YHOwbMo0PDy69NKKMDAG3EnTNKBL0xIq2NMhAkEAvdNkMoI7Cedd35xG5bqB+GxWvrZPZN/QHhmQiUGO/CvslHL7QKeit4auDi30g3aUKbo07w/WfxL/me6yJRkn7wJAcXAtv0C4vOCwV45GxWmxqR+GFXf0cN349ssUPQzmR24OdBHnrD22e/8zw5+Tqr3IIvUL0Hl9UHYgq7Sln0HL4QJBAKn0u3Axg5SRb04GyL9kpnt63IuyBRGnBdn9P5h0dwW2egJLlENGE/zHe808PgD6SRu3GS+1eXGa2/jBawSmKkcCQGxLhtbCa08GrcQOHNYrtSfKRn+hJRKvwAWK4K64OGC94spgtPX5H3Ks3QxUGBWAtdlP+OVugfIfZ3Esim+2xSA=";
        Map<String, Object> keyMap;
        try {
            keyMap = createKey();  // 使用 java.security.KeyPairGenerator 生成 公/私钥
            String publicKey = getPublicKey(keyMap);
            System.out.println("公钥：\n"+publicKey);
            String privateKey = getPrivateKey(keyMap);
            System.out.println("私钥：\n"+privateKey);
            RSAPublicKey publicKey1 = getPublicKey(PUBLIC_KEY);
            RSAPrivateKey privateKey1 = getPrivateKey(PRIVATE_KEY);
            RSAKeyPair rsaKeyPair = new RSAKeyPair();
            rsaKeyPair.setPublicKey(publicKey1);
            rsaKeyPair.setPrivateKey(privateKey1);
            System.out.println(rsaKeyPair);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main1(String[] args) throws Exception {
        //初始化密钥
        //生成密钥对
        //默认512的长度为53，1024的长度为117，2048的长度为245
        Map<String, Object> keyMap = RSAUtils.createKey();
        //公钥
        byte[] publicKey = RSAUtils.getPublicKeyBytes(keyMap);
        //私钥
        byte[] privateKey = RSAUtils.getPrivateKeyBytes(keyMap);
        System.out.println("公钥：" + Base64.getEncoder().encodeToString(publicKey));
        System.out.println("C#公钥：" + getRSAPublicKeyAsNetFormat(publicKey));
        System.out.println("私钥：" + Base64.getEncoder().encodeToString(privateKey));
        System.out.println("C#私钥：" + getRSAPrivateKeyAsNetFormat(privateKey));
        System.out.println("================密钥对构造完毕,甲方将公钥公布给乙方，开始进行加密数据的传输=============");
        String str = "RSA密码交换算法";
        System.out.println("===========甲方向乙方发送加密数据==============");
        System.out.println("原文:" + str);
        //甲方进行数据的加密
        byte[] code1 = RSAUtils.encryptByPrivateKey(str.getBytes(), privateKey);
        System.out.println("加密后的数据：" + Base64.getEncoder().encodeToString(code1));
        System.out.println("===========乙方使用甲方提供的公钥对数据进行解密==============");
        //乙方进行数据的解密
        byte[] decode1 = RSAUtils.decryptByPublicKey(code1, publicKey);
        System.out.println("乙方解密后的数据：" + new String(decode1));

        System.out.println("===========反向进行操作，乙方向甲方发送数据==============");

        str = "乙方向甲方发送数据RSA算法";

        System.out.println("原文:" + str);

        //乙方使用公钥对数据进行加密
        byte[] code2 = RSAUtils.encryptByPublicKey(str.getBytes(), publicKey);
        System.out.println("===========乙方使用公钥对数据进行加密==============");
        System.out.println("加密后的数据：" + Base64.getEncoder().encodeToString(code2));

        System.out.println("=============乙方将数据传送给甲方======================");
        System.out.println("===========甲方使用私钥对数据进行解密==============");

        //甲方使用私钥对数据进行解密
        byte[] decode2 = RSAUtils.decryptByPrivateKey(code2, privateKey);
        System.out.println("甲方解密后的数据：" + new String(decode2));
    }


    public static void main2(String[] args) throws Exception {
        String data= "N1C/KA/kia3/9pBPbEeE2eHEy8R8wd04E9TDvmWqPZOqEXfq27VK/OoMvvUPnB4wJJnu0S8UjP83ajY3dKZluA==";
        String k = "MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAlKbvV+TbA6tAc+q+RTFF5CqNjgqpayrWFCTK1TYtpZ69dFGhIkKnKpT4nTHs9wFyTY8CfnyziWWhneILpoxjpwIDAQABAkAxEzuJBODZQTwyCJlwNmggf3vkHNj5rFaop8zevtgrCM8lTpbtkbDlz/Y90ifhn00eHbaWx4cOXwBVG3g7p6eBAiEA3VsVh6TgE8MMhH2ailRPI5BTKAPKXxB1Fz6qwechg/cCIQCr6uEtvpiwwQVmeMDraufl/AFY1zLmXSIn+YJh8WXR0QIgRws2y8RFDtKpL9TIRuFsTPPDXLJqvzwe+IjqcTVncl0CIQCJ0NPM8QLEhyfGGr1Eu8HFCz0lM/Z412Y/N3S/AV5HUQIhAMTCITa1fmz3nRxH4L7EofGMniZ+xCC0Pk3G1BbxXlSo";
        byte[] decode2 = RSAUtils.decryptByPrivateKey(Base64.getDecoder().decode(data), Base64.getDecoder().decode(k));
        System.out.println("甲方解密后的数据：" + new String(decode2));
    }

    public static void main3(String[] args) throws Exception {
        //私钥
        String privateStr = "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAtmEBC5xciJySRAqchSYQR5tnEzsKO/dK0Fg1dVBKKPPwETD5HrQqcDPegRwoiZm8ASpVA2MKZd0iBHFU/M7wNQIDAQABAkEAtK25OWV4jqZ+iQXyNj6VVjtwjC6rXukIpwscOtKGBbalCLgRAs8Q0ZePqe9Duj3/vE8/ZZuTXjSlsJlVSCp/aQIhAPdo8I2aLJrkm/om/CtUHvlW1TCw14eP28zvChQzIx4zAiEAvLYMMVcHD7pe+Xj0hfnc+rmai/64zcjP4VpknqHI//cCIF8bRwWYE7eDU/ZokB1z2+hLme56vI+PHJZ9+Wjkc4aDAiBdJ0Rnir06n1ZIsdOK2yehQMOwfaH+OzWa2YM350cQSwIgOscoD26vCWCF3Q35Tn16RgRYSSyk28s+uqZs1Ld4PvU=";
        System.out.println("java私钥:"+privateStr);
        byte[] privateBytes = Base64.getDecoder().decode(privateStr);
        //转换C#私钥
        String dotnetPrivateStr = getRSAPrivateKeyAsNetFormat(privateBytes);
        System.out.println("C#私钥:" + dotnetPrivateStr);
        //公钥
        String publicStr = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBALZhAQucXIickkQKnIUmEEebZxM7Cjv3StBYNXVQSijz8BEw+R60KnAz3oEcKImZvAEqVQNjCmXdIgRxVPzO8DUCAwEAAQ==";
        System.out.println("java公钥:"+publicStr);
        byte[] publicBytes = Base64.getDecoder().decode(publicStr);
        //转换C#公钥
        String dotnetPublic = getRSAPublicKeyAsNetFormat(publicBytes);
        System.out.println("C#公钥:" + dotnetPublic);

        String content = "{userName:\"admin\", password:\"asdf1234\"}";

        byte[] encryptByPublic = RSAUtils.encryptByPublicKey(content.getBytes(), publicBytes);
        System.out.println("===========甲方使用公钥对数据进行加密==============");
        System.out.println("加密后的数据：" + Base64.getEncoder().encodeToString(encryptByPublic));
        //加密后的数据：pDm5Ge+2N16d7PbyeucjK7QYq7bWWqbZ7WiIv6706gLwuwyG088/AMTlloeDihSkQkP4sRyxS0ivY9UACNVVdg==
        System.out.println("===========甲方使用私钥对数据进行解密==============");
        //甲方使用私钥对数据进行解密
        byte[] decryptByPrivate = RSAUtils.decryptByPrivateKey(encryptByPublic, privateBytes);
        System.out.println("乙方解密后的数据：" + new String(decryptByPrivate));
    }
}
