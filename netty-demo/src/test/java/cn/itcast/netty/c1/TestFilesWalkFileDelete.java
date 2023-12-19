package cn.itcast.netty.c1;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class TestFilesWalkFileDelete {

    public static void main(String[] args) throws IOException {


        Files.walkFileTree(Paths.get("F:\\hg\\新建文件夹"),new SimpleFileVisitor<Path>(){

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                //删除文件
                Files.delete(file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                //删除空目录
                Files.delete(dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }
}
