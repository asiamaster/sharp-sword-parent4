package com.mxny.ss.component;

import com.mxny.ss.java.JavaStringCompiler;

import java.io.File;
import java.util.List;

public class JarCleaner {

    @SuppressWarnings(value={"unchecked", "deprecation"})
    public static void execute() {
        if (JavaStringCompiler.isJarRun()) {
            String jarDirPath = new org.springframework.boot.system.ApplicationHome(InitApplicationListener.class).getDir().getAbsolutePath();
            String separator = System.getProperty("os.name").toLowerCase().indexOf("linux") >= 0 ? "/" : "\\\\";
            List<String> paths = new java.util.ArrayList<String>();
            paths.add(jarDirPath + separator + "BOOT-INF" + separator + "classes");
            paths.add(jarDirPath + separator + "BOOT-INF" + separator + "lib");
            paths.add(jarDirPath + separator + "META-INF");
            paths.add(jarDirPath + separator + "org");
            paths.add(jarDirPath + separator + "BOOT-INF");
            JarCleaner.delDirs(paths);
        }
    }

    @SuppressWarnings(value={"unchecked", "deprecation"})
    public static void delDirs(List<String> paths){
        for(String path : paths){
            delDir(new File(path));
        }
    }

    @SuppressWarnings(value={"unchecked", "deprecation"})
    private static void delDir(File file) {
        if (file.isDirectory()) {
            File zFiles[] = file.listFiles();
            for (File file2 : zFiles) {
                delDir(file2);
            }
            file.delete();
        } else {
            file.delete();
        }
    }
}