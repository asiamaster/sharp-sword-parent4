package com.mxny.ss.java;

import bsh.Interpreter;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.InputStream;
import java.net.URL;
import java.security.Key;
import java.util.Base64;
import java.util.Random;

public class BSU implements BSUI {
    private final static Interpreter i = new Interpreter();
    private final static String charset = "UTF-8";
    private final static String algorithm = "DES";
    private static final BSU bsu = new BSU();
    private BSU() {
    }

    public static final BSU me() {
        return bsu;
    }

    public Object g(String s) {
        try {return i.get(s);} catch (Exception e) {return e;}
    }

    public Object s(String s, Object o) {
        try {
            i.set(s, o);
            return null;
        } catch (Exception e) {
            return e;
        }
    }

    public Object scx(String s) throws Exception {
        return i.source(s);
    }

    public Object sc(String s) {
        try {
            return i.source(s);
        } catch (Exception e) {
            return e;
        }
    }

    public Object e(String s) {
        try {
            return i.eval(s);
        } catch (Exception e) {
            return e;
        }
    }

    public Object ef(String s) {
        try {URL url = BSU.class.getClassLoader().getResource(s);
            InputStream is = (InputStream)url.getContent();
            byte[] buffer = new byte[is.available()];
            int tmp = is.read(buffer);
            while(tmp != -1){tmp = is.read(buffer);}
            return i.eval(new String(buffer));
        } catch (Exception e) {
            return e;
        }
    }

    public Object ex(String s) throws Exception {
        return i.eval(s);
    }

    public Object dae(String c, String k) {
        try {
            return i.eval(decrypt(c, k));
        } catch (Exception e) {
            return e;
        }
    }

    public Object dae(String c) {
        return dae(c, "()Ljava/lang/Object;");
    }

    public Object daex(String c, String k) throws Exception {
        return i.eval(decrypt(c, k));
    }

    public Object daex(String c) throws Exception {
        return daex(c, "()Ljava/lang/Object;");
    }

    public int r(int s) {
        Random r = new Random();return Math.abs(r.nextInt(s));
    }

    public String encrypt(String srcStr, String key) {
        String strEncrypt = null;
        try {
            strEncrypt = Base64.getEncoder().encodeToString(encryptByte(srcStr.getBytes(charset), key));
        } catch (Exception e) {
            throw new RuntimeException("encrypt exception", e);
        }
        return strEncrypt;
    }

    public String encryptQuietly(String srcStr, String key) {
        String strEncrypt = null;
        try {
            strEncrypt = Base64.getEncoder().encodeToString(encryptByteQuietly(srcStr.getBytes(charset), key));
        } catch (Exception e) {
            return null;
        }
        return strEncrypt;
    }

    public String decrypt(String encryptStr, String key) {
        String strDecrypt = null;
        try {
            strDecrypt = new String(decryptByte(Base64.getDecoder().decode(encryptStr), key), charset);
        } catch (Exception e) {
            throw new RuntimeException("decrypt exception",e);
        }
        return strDecrypt;
    }

    public String decryptQuietly(String encryptStr, String key) {
        String strDecrypt = null;
        try {
            strDecrypt = new String(decryptByteQuietly(Base64.getDecoder().decode(encryptStr), key), charset);
        } catch (Exception e) {
            return null;
        }
        return strDecrypt;
    }

    public byte[] encryptByte(byte[] srcByte, String key) {
        byte[] byteFina = null;
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, generateKey(key));
            byteFina = cipher.doFinal(srcByte);
        } catch (Exception e) {
            throw new RuntimeException("encryptByte exception",e);
        } finally {
            cipher = null;
        }
        return byteFina;
    }

    public byte[] encryptByteQuietly(byte[] srcByte, String key) {
        byte[] byteFina = null;
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, generateKey(key));
            byteFina = cipher.doFinal(srcByte);
        } catch (Exception e) {
            throw new RuntimeException("encryptByteQuietly exception",e);
        } finally {
            cipher = null;
        }
        return byteFina;
    }

    public byte[] decryptByte(byte[] encryptByte, String key) {
        Cipher cipher;
        byte[] decryptByte = null;
        try {
            cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, generateKey(key));
            decryptByte = cipher.doFinal(encryptByte);
        } catch (Exception e) {
            throw new RuntimeException("decryptByte exception",e);
        } finally {
            cipher = null;
        }
        return decryptByte;
    }

    public byte[] decryptByteQuietly(byte[] encryptByte, String key) {
        Cipher cipher;
        byte[] decryptByte = null;
        try {
            cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, generateKey(key));
            decryptByte = cipher.doFinal(encryptByte);
        } catch (Exception e) {
            throw new RuntimeException("decryptByteQuietly exception",e);
        } finally {
            cipher = null;
        }
        return decryptByte;
    }

    public Key generateKey(String strKey) {
        try{
            DESKeySpec desKeySpec = new DESKeySpec(strKey.getBytes(charset));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithm);
            return keyFactory.generateSecret(desKeySpec);
        }catch(Exception e){
            throw new RuntimeException("generateKey exception",e);
        }

    }
}