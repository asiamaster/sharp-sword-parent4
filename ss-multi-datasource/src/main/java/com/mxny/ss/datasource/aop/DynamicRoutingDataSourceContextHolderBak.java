//package com.mxny.ss.datasource.aop;
//
///**
// * 动态数据源切换上下文持有者
// * Created by asiamaster on 2022/4/13.
// */
//public class DynamicRoutingDataSourceContextHolderBak {
//
//    private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();
//
//    public static void setDataSourceType(String dataSourceType) {
//        contextHolder.set(dataSourceType);
//    }
//
//    public static String getDataSourceType() {
//        return contextHolder.get();
//    }
//
//    /**
//     * 获取当前数据源，数据不变
//     * @return
//     */
//    public static String peek() {
//
//        return contextHolder.get();
//    }
//
//    /**
//     * 栈顶推进一个数据源
//     * @return
//     */
//    public static void push(String value) {
//        contextHolder.set(value);
//    }
//
//    /**
//     * 栈顶弹出一个数据源, 没数据返回null，不会抛异常
//     * @return
//     */
//    public static String pop() {
//        return contextHolder.get();
//    }
//
//    public static void clear() {
//        contextHolder.remove();
//    }
//
//    /**
//     * 判断指定DataSrouce当前是否存在
//     *
//     * @param dataSourceId
//     * @return
//     */
//    public static boolean containsDataSource(String dataSourceId){
//        return DynamicRoutingDataSourceRegister.customDataSources.containsKey(dataSourceId);
//    }
//
//}
