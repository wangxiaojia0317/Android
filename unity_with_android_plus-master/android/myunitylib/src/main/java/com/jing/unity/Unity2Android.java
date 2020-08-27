package com.jing.unity;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Jing on 2018-1-18.
 */
public class Unity2Android {



   /**
     * unity项目启动时的的上下文
     */
    private Activity _unityActivity;
    /**
     * 获取unity项目的上下文
     * @return
     */
    Activity getActivity(){
        if(null == _unityActivity) {
            try {
                Class<?> classtype = Class.forName("com.unity3d.player.UnityPlayer");
                Activity activity = (Activity) classtype.getDeclaredField("currentActivity").get(classtype);
                _unityActivity = activity;
            } catch (ClassNotFoundException e) {

            } catch (IllegalAccessException e) {

            } catch (NoSuchFieldException e) {

            }
        }
        return _unityActivity;
    }

    /**
     * 调用Unity的方法
     * @param gameObjectName    调用的GameObject的名称
     * @param functionName      方法名
     * @param args              参数
     * @return                  调用是否成功
     */
    boolean callUnity(String gameObjectName, String functionName, String args){
        try {
            Class<?> classtype = Class.forName("com.unity3d.player.UnityPlayer");
            Method method =classtype.getMethod("UnitySendMessage", String.class,String.class,String.class);
            method.invoke(classtype,gameObjectName,functionName,args);
            return true;
        } catch (ClassNotFoundException e) {

        } catch (NoSuchMethodException e) {

        } catch (IllegalAccessException e) {

        } catch (InvocationTargetException e) {

        }
        return false;
    }

    /**
     * Toast显示unity发送过来的内容
     * @param content           消息的内容
     * @return                  调用是否成功
     */
    public boolean showToast(String content){
        Toast.makeText(getActivity(),content,Toast.LENGTH_SHORT).show();
        //这里是主动调用Unity中的方法，该方法之后unity部分会讲到
        callUnity("Main Camera","FromAndroid", "hello unity i'm android");
        CreateDir();

        return true;
    }



    public String GetIntent(){
        Intent intent = getActivity().getIntent();
        String value = intent.getStringExtra("arg");
        return value;
    }


    public  String  CreateDir()
    {
        String str="";
        try
        {

// 保存在video文件夹下
            String video_savePath = Environment.getExternalStorageDirectory()
                    .toString() + "/video";
            File file = new File(video_savePath);
            // video文件夹不存在
            if (!file.exists()) {
                // 创建文件夹
                file.mkdirs();
            }
            Log.d("path", video_savePath);
            // 构造文件名 .mp4格式
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                    "yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
            Date date = new Date(System.currentTimeMillis());
            String FileName = "/" + simpleDateFormat.format(date) + ".mp4";
            // 创建文件
            video_savePath = video_savePath + FileName;
            file = new File(video_savePath);
            if (!file.exists()) {
                // 创建文件
                try {
                    file.createNewFile();
                    Log.d("path", "create sucessful");
                } catch (IOException e) {
                    Log.d("path","create failed");
                    e.printStackTrace();
                    str=e.toString();
                }
            }
            str="创建成功";
        }
        catch (Exception e)
        {
            str=e.toString();
        }

        return  str;
    }



    public String ReadFile(String targetPath)
    {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/io.dcloud.HBuilder/apps/HBuilder/downloads/2727.pdf";
        copyFile(path,targetPath);
        return "读取";


    }
    public String ReadFile1(String targetPath)
    {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/io.dcloud.HBuilder/downloads/238.json";
        copyFile(path,targetPath);
        return ReadTxtFile(path)+"app下面的downloads";


    }

    public void FileCopy(String fileName,String newPath)
    {
        copyFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/io.dcloud.HBuilder/apps/HBuilder/downloads/"+fileName,newPath);

    }
    public void JsonCopy(String fileName,String newPath)
    {
        copyFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/io.dcloud.HBuilder/downloads/"+fileName,newPath);
    }

    public boolean copyFile(String oldPath$Name, String newPath$Name) {
        try {
            File oldFile = new File(oldPath$Name);
            if (!oldFile.exists()) {
                Log.e("--Method--", "copyFile:  oldFile not exist.");
                return false;
            } else if (!oldFile.isFile()) {
                Log.e("--Method--", "copyFile:  oldFile not file.");
                return false;
            } else if (!oldFile.canRead()) {
                Log.e("--Method--", "copyFile:  oldFile cannot read.");
                return false;
            }


            FileInputStream fileInputStream = new FileInputStream(oldPath$Name);    //读入原文件
            FileOutputStream fileOutputStream = new FileOutputStream(newPath$Name);
            byte[] buffer = new byte[1024];
            int byteRead;
            while ((byteRead = fileInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }




    public  String ReadTxtFile(String strFilePath)
    {
        String path = strFilePath;
        String content = ""; //文件内容字符串
        //打开文件
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory())
        {
            Log.d("TestFile", "The File doesn't not exist.");
        }
        else
        {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null)
                {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    //分行读取
                    while (( line = buffreader.readLine()) != null) {
                        content += line + "\n";
                    }
                    instream.close();
                }
            }
            catch (java.io.FileNotFoundException e)
            {
                Log.d("TestFile", "The File doesn't not exist.");
            }
            catch (IOException e)
            {
                Log.d("TestFile", e.getMessage());
            }
        }
        return content;
    }






}
