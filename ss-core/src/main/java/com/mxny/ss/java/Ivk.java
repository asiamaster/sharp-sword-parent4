package com.mxny.ss.java;

import com.google.common.collect.Lists;
import com.mxny.ss.dto.IDTOFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

@SuppressWarnings("unchecked")
public class Ivk {
    static {
        i();
    }

    @SuppressWarnings("unchecked")
    public static void i(){
        B.bi = BU.n();
        Map<String, byte[]> map2 = new HashMap<>(8);
        List<String> waters = gfl("script/water");
        String pn = BSUI.class.getPackage().getName();
        ArrayList<String> ns = Lists.newArrayList(pn + ".BSU", pn + ".Ac", IDTOFactory.class.getPackage().getName() + ".DTOFactory");
        List<Class> classes = BU.cn(ns, "script/water");
        try {
            B.b = (BSUI) classes.get(0).getMethod("me").invoke(null);
            classes.get(1).getMethod("i").invoke(null);
            classes.get(2).getMethod("i").invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
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
