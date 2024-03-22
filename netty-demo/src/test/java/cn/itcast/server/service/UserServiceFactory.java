package cn.itcast.server.service;

/**
 * 工厂类 -创建具体的实现类
 */

public abstract class UserServiceFactory {

    private static UserService userService = new UserServiceMemoryImpl();

    public static UserService getUserService() {
        return userService;
    }
}
