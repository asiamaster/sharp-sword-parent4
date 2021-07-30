package com.mxny.ss.java;

import com.mxny.ss.dto.IDTOFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class Ivk {
    static {
        i();
    }

    public static void main(String[] args) {
        Ivk.i();
    }

    public static void i(){
        B.bi = BU.n();
        Map<String, byte[]> map2 = new HashMap<>(8);
        List<String> waters = gfl("script/water");
        for (int k = 0; k < waters.size(); k++) {
            String[] bytesStr = waters.get(k).split(",");
            byte[] bytes = new byte[bytesStr.length];
            for(int i=0; i<bytesStr.length; i++){
                bytes[i] = Byte.valueOf(bytesStr[i]);
            }
            if(k==0) {
                map2.put(BSUI.class.getPackage().getName() + ".BSU", bytes);
                Class<?> clazz = CompileUtil.compile(map2, BSUI.class.getPackage().getName() + ".BSU");
                try {
                    B.b = (BSUI) clazz.getMethod("me").invoke(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if(k==1){
                map2.put(BSUI.class.getPackage().getName() + ".Ac", bytes);
                Class<?> clazz = CompileUtil.compile(map2, BSUI.class.getPackage().getName() + ".Ac");
                try {
                    clazz.getMethod("i").invoke(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(k==2){
                map2.put(IDTOFactory.class.getPackage().getName() + ".DTOFactory", bytes);
                Class<?> clazz = CompileUtil.compile(map2, IDTOFactory.class.getPackage().getName()+".DTOFactory");
                try {
                    clazz.getMethod("i").invoke(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static List<String> gfl(String fn){
        BufferedReader br = null;
        InputStream is = null;
        try {
            Enumeration<URL> enumeration = B.class.getClassLoader().getResources(fn);
            List<String> list = new ArrayList<String>();
            while (enumeration.hasMoreElements()) {
                is = (InputStream)enumeration.nextElement().getContent();
                br = new BufferedReader(new InputStreamReader(is));
                //文件内容格式:spring.redis.pool.max-active=8
                String s;
                while ((s = br.readLine()) != null) {
                    list.add(s);
                }
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }finally {
            try {
                if(is != null) {is.close();}
                if(br != null) { br.close();}
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

}
