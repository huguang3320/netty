package cn.itcast.netty.c1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestFilesWalkCopy {

    public static void main(String[] args) throws IOException {

        //原文件地址
        String src = "F:\\新建文件夹";
        //目标文件地址
        String target = "F:\\新建文件夹2";

        //遍历文件树
        Files.walk(Paths.get(src)).forEach(path -> {
            try {
                //将源文件地址替换成新地址
                String targetName = path.toString().replace(src, target);
                if (Files.isDirectory(path)) {     //如果是目录，就创建目录
                    Files.createDirectory(Paths.get(targetName));
                } else if (Files.isRegularFile(path)) { //  如果是文件，就复制文件
                    Files.copy(path, Paths.get(targetName));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
