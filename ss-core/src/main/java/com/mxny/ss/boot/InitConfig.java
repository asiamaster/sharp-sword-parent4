package com.mxny.ss.boot;

import com.mxny.ss.java.B;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class InitConfig {
    public static boolean isInit = false;

    @Autowired
    public Environment env;

    @PostConstruct
    public void init(){
        InitConfig.done();
    }

    /**
     * 执行
     */
    public synchronized static void done(){
        if(isInit){
            return;
        }
        B.i();
        isInit = true;
    }
}
