package com.booway;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * zip解压缩工具
 * @author wuzhenhong
 */
public class ZipUtils
{
    
    private ZipUtils() {}
    
    public static void zipFile(String srcPath, String targetPath)
    {
        OutputStream out = null;
        ZipOutputStream zipOut = null;
        try
        {
            File srcFile = new File(srcPath);
            if(!targetPath.endsWith(".zip")) {
                targetPath += "/" + UUID.randomUUID().toString() + ".zip";
            }
            targetPath = targetPath.replaceAll("\\\\+|//+", "/").replaceAll("//+", "/");
            File targetFile = new File(targetPath);
            if (!targetFile.getParentFile().exists() && !targetFile.getParentFile().mkdirs())
            {
                throw new RuntimeException("目标路径创建失败！");
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

    private static void doZipFile(String srcPath, File srcFile, ZipOutputStream zipOut) throws Exception
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
        } else
        {
            throw new RuntimeException("找不到源文件路径，请传入正确的路径。");
        }
    }

    public static void unZipFile(String srcPath, String targetPath) throws Exception
    {
        InputStream in = null;
        ZipInputStream zipInt = null;
        try
        {
            File file = new File(srcPath);
            if (!file.exists() || file.isDirectory())
            {
                throw new RuntimeException("请传入正确的路径或正确的压缩文件");
            }
            targetPath = targetPath.replaceAll("\\\\", "/");
            if (!targetPath.endsWith("/"))
            {
                targetPath += "/";
            }
            in = new FileInputStream(file);
            zipInt = new ZipInputStream(in, Charset.forName("GBK"));
            ZipEntry zipEntry = null;
            while ((zipEntry = zipInt.getNextEntry()) != null)
            {
                String zipEntryName = zipEntry.getName();
                String finalPath = targetPath + zipEntryName;
                File finalFile = new File(finalPath);
                doUnZipFile(finalFile, zipInt, zipEntry);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("解压失败：" + e.getMessage());
        } finally
        {
            try
            {
                if (zipInt != null)
                {
                    zipInt.close();
                    zipInt = null;
                }
                if (in != null)
                {
                    in.close();
                    in = null;
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private static void doUnZipFile(File finalFile, ZipInputStream zipInt, ZipEntry zipEntry)
    {
        OutputStream out = null;
        BufferedOutputStream bout = null;
        try
        {
            if (zipEntry.isDirectory() && !finalFile.exists() && !finalFile.mkdirs())
            {
                throw new RuntimeException("解压时出现了一些问题：文件路径创建失败！");
            } else if(!zipEntry.isDirectory())
            {
                if(!finalFile.getParentFile().exists() && !finalFile.getParentFile().mkdirs()) {
                    throw new RuntimeException("解压时出现了一些问题：文件路径创建失败！");
                }
                out = new FileOutputStream(finalFile);
                bout = new BufferedOutputStream(out);
                byte[] buff = new byte[1024];
                int len = 0;
                while ((len = zipInt.read(buff)) != -1)
                {
                    bout.write(buff, 0, len);
                }
                out.flush();
                zipInt.closeEntry();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("解压失败!");
        } finally
        {
            try
            {
                if (bout != null)
                {
                    bout.close();
                    bout = null;
                }
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

    private static void putEntry(File srcFile, File srcPath, ZipOutputStream zipOut, String extra) throws Exception
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
