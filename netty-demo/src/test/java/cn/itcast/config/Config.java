package cn.itcast.config;

import cn.itcast.protocol.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置项，可根据配置项中读取的参数，来获取实际值
 */
public abstract class Config {
    static Properties properties;
    static {
        try (InputStream in = Config.class.getResourceAsStream("/application.properties")) {
            properties = new Properties();
            properties.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    public static int getServerPort() {
        String value = properties.getProperty("server.port");
        if(value == null) {
            return 8080;
        } else {
            return Integer.parseInt(value);
        }
    }
    public static Serializer.Algorithm getSerializerAlgorithm() {
        String value = properties.getProperty("serializer.algorithm");
        if(value == null) {
            // 如果没在配置文件中配置serializer.algorithm属性，则默认用jdk的方式进行编译、反编译
            return Serializer.Algorithm.Java;
        } else {
            // 枚举类的valueOf方法，是根据传入的枚举常量 返回一个枚举类的实例
            return Serializer.Algorithm.valueOf(value);
        }
    }
}