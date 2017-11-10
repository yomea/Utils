package com.booway;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils
{

    public static void main(String[] args) throws Exception
    {

        String targetPath = "D:\\haha.zip";

        String srcPath = "D:\\毕业设计\\";

        zipFile(srcPath, targetPath);

    }

    public static void zipFile(String srcPath, String targetPath)
    {
        OutputStream out = null;
        ZipOutputStream zipOut = null;
        try
        {
            File srcFile = new File(srcPath);
            File targetFile = new File(targetPath);
            if(!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }
            out = new FileOutputStream(targetFile);
            //有中文文件夹的必须使用GBK编码，否则被认为是文件
            zipOut = new ZipOutputStream(out, Charset.forName("GBK"));
            doZipFile(srcPath, srcFile, zipOut);

        } catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("压缩文件失败！");
        } finally
        {
            try
            {
                if (zipOut != null)
                {
                    zipOut.close();
                    zipOut = null;
                }
                //注意关闭顺序，先关外层的，再内层。
                if (out != null)
                {
                    out.close();
                    out = null;
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void doZipFile(String srcPath, File srcFile, ZipOutputStream zipOut) throws Exception
    {
        if (srcFile.exists())
        {
            File srcPathFile = new File(srcPath);
            if (srcFile.isFile())
            {
                InputStream in = new FileInputStream(srcFile);
                BufferedInputStream bin = new BufferedInputStream(in);
                putEntry(srcFile, srcPathFile, zipOut, "");
                byte[] buff = new byte[1024];
                int len = 0;
                while ((len = bin.read(buff)) != -1)
                {
                    zipOut.write(buff, 0, len);
                }
                zipOut.flush();
                zipOut.closeEntry();
                bin.close();
                in.close();

            } else if (srcFile.isDirectory())
            {
                File[] files = srcFile.listFiles();
                if (files.length == 0)
                {
                    putEntry(srcFile, srcPathFile, zipOut, "/");
                    zipOut.closeEntry();
                } else
                {
                    for (File file : files)
                    {
                        doZipFile(srcPath, file, zipOut);
                    }
                }
            }
        } else {
            throw new RuntimeException("找不到源文件路径，请传入正确的路径。");
        }
    }

    public static void putEntry(File srcFile, File srcPath, ZipOutputStream zipOut, String extra) throws Exception
    {
        String entryName = srcPath.getName() + srcFile.getAbsolutePath().replace(srcPath.getAbsolutePath(), "");
        if (srcFile.getAbsolutePath().equals(srcPath.getAbsolutePath()))
        {
            entryName = srcFile.getName();
        }
        entryName = (entryName + extra).replaceAll("\\\\", "/");
        System.out.println(entryName);
        ZipEntry zipEntry = new ZipEntry(entryName);
        zipOut.putNextEntry(zipEntry);
    }

}
