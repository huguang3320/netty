package cn.itcast.test;

import com.google.gson.Gson;

public class testGson {
    public static void main(String[] args) {
        System.out.println(new Gson().toJson(String.class));
    }
}
