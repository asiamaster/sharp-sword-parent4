//package com.mxny;
//
//import com.mxny.ss.netty.server.acceptor.DefaultCommonSrvAcceptor;
//import com.mxny.ss.retrofitful.annotation.RestfulScan;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
//import org.springframework.context.annotation.ComponentScan;
//
///**
// * 由MyBatis Generator工具自动生成
// */
//@SpringBootApplication
//@ComponentScan(basePackages={"com.mxny.ss","com.mxny.netty.server"})
//@RestfulScan({"com.mxny.netty.server.rpc"})
///**
// * 除了内嵌容器的部署模式，Spring Boot也支持将应用部署至已有的Tomcat容器, 或JBoss, WebLogic等传统Java EE应用服务器。
// * 以Maven为例，首先需要将<packaging>从jar改成war，然后取消spring-boot-maven-plugin，然后修改Application.java
// * 继承SpringBootServletInitializer
// */
//public class ServerApplication extends SpringBootServletInitializer implements CommandLineRunner {
//
//    public static void main(String[] args) {
//        SpringApplication.run(ServerApplication.class, args);
//    }
//
//    @Override
//    public void run(String... strings) throws InterruptedException {
//        DefaultCommonSrvAcceptor defaultCommonSrvAcceptor = new DefaultCommonSrvAcceptor(20011,null);
//        defaultCommonSrvAcceptor.start();
//    }
//}
