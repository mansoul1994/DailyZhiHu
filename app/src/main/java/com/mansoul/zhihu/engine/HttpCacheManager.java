package com.mansoul.zhihu.engine;

import android.content.Context;

import com.mansoul.zhihu.utils.IOUtils;
import com.mansoul.zhihu.utils.MD5Encoder;
import com.mansoul.zhihu.utils.UIUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Created by Mansoul on 16/6/13.
 */
public class HttpCacheManager {
    //写缓存
    public static void setCache(Context context, String url, String json) {

        try {
            url = MD5Encoder.encode(url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //url为文件名, json为内容,保存在本地
        File cacheDir = context.getCacheDir(); //缓存文件

        //生成缓存文件
        File cacheFile = new File(cacheDir, url);

        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(cacheFile));
            System.out.println("写入缓存了！");

//            long deadLine = System.currentTimeMillis() + 30 * 60 * 1000; //缓存有效期半个小时
//            writer.write(deadLine + "\n"); //第一行写入时间,换行
            writer.write(json); //写入json

            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(writer);
        }
    }

    //读缓存
    public static String getCache(String url) {
        try {
            url = MD5Encoder.encode(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        File cacheDir = UIUtils.getContext().getCacheDir(); //缓存文件

        //生成缓存文件
        File cacheFile = new File(cacheDir, url);

        //判断缓存是否存在
        if (cacheFile.exists()) {


            //判断缓存是否有效
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(cacheFile));

//                long deadTime = Long.parseLong(reader.readLine());
//
//                if (System.currentTimeMillis() < deadTime) {
//                    //缓存有效
////                    StringBuffer sb = new StringBuffer();
//                    StringBuilder sb = new StringBuilder();
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                        sb.append(line);
//                    }
//                    return sb.toString();
//                }
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                return sb.toString();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                IOUtils.close(reader);
            }
        }
        return null;
    }

    public static File getCacheFile(String url) {
        try {
            url = MD5Encoder.encode(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        File cacheDir = UIUtils.getContext().getCacheDir(); //缓存文件

        //生成缓存文件
        File cacheFile = new File(cacheDir, url);

        //判断缓存是否存在
        if (cacheFile.exists()) {
            return cacheFile;
        }
        return null;
    }
}
