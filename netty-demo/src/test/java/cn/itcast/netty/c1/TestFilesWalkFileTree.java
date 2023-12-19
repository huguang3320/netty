package cn.itcast.netty.c1;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

public class TestFilesWalkFileTree {

    public static void main(String[] args) throws IOException {

        m1();

    }

    private static void m1() throws IOException {
        final AtomicInteger dirCount = new AtomicInteger();
        final AtomicInteger fileCount = new AtomicInteger();

        Files.walkFileTree(Paths.get("F:\\hg\\压缩包\\docker_onekey_setup"),new SimpleFileVisitor<Path>(){

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // dirCount加1
                System.out.println("========>"+dir);
                dirCount.incrementAndGet();
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // fileCount加1
                fileCount.incrementAndGet();
                System.out.println(file);
                return super.visitFile(file, attrs);
            }
        });

        //输出文件数量
        System.out.println("文件夹数量："+dirCount);
        //输出文件夹数量
        System.out.println("文件数量："+fileCount);
    }


}
