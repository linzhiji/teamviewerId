package com.ishehui.teamviewerid;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by wangtengfei on 2017/7/18.
 * SD卡操作类
 */

public class CardUtils {

    public static final String TAG = CardUtils.class.getSimpleName();

    public static boolean checkSDCard() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取基础存放路径
     **/
    private static String getSDCardBasePath(Context context) {
        if (checkSDCard())
//            return Context.getExternalFilesDir(); //获取SD卡--会随着用户卸载而被删除
            return Environment.getExternalStorageDirectory().getPath()//获取SD卡--不会随着用户卸载App而删除
                    + File.separator;//需要申请写入权限 但是android6.0在写入私有目录和扩展目录的时候需要不需要申请写入权限
        else
            return context.getCacheDir().getAbsolutePath();//获取App的私有存储--App的安装目录其他应用无法查看
    }


    /**
     * 获取teamViewer存放log日志文件的目录
     **/
    public static String getTeamViewerLogFilePath(Context context) {
        return getSDCardBasePath(context) + "/Android/data/com.teamviewer.host.market/files";
    }


    /**
     * 检测文件是否存在
     **/
    public static boolean isFileExit(String fileStr) {
        if (!StringUtil.IsEmptyOrNullString(fileStr)) {
            File file = new File(fileStr);
            if (file.exists()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 删除文件
     **/
    public static void deleteFile(String filePath) {
        if (!StringUtil.IsEmptyOrNullString(filePath)) {
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 删除图片-同步图库
     *
     * @param context
     * @param imgPath 图片在SD卡上的路径
     **/
    public static void deleteImage(Context context, String imgPath) {
        if (!StringUtil.IsEmptyOrNullString(imgPath)) {
            boolean result = false;
            ContentResolver resolver;
            Cursor cursor = null;
            try {
                resolver = context.getContentResolver();
                cursor = MediaStore.Images.Media.query(resolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=?",
                        new String[]{imgPath}, null);
                if (cursor.moveToFirst()) {
                    long id = cursor.getLong(0);
                    Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    Uri uri = ContentUris.withAppendedId(contentUri, id);
                    int count = context.getContentResolver().delete(uri, null, null);
                    result = count == 1;
                } else {
                    if (!StringUtil.IsEmptyOrNullString(imgPath)) {
                        File file = new File(imgPath);
                        if (file.isFile() && file.exists()) {
                            result = file.delete();
                        }
                    }
                }
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            } catch (Exception e) {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
            if (!result) {
                Log.e(TAG, TAG + "---delete image is null");
            }
        }
    }

    /**
     * 从SD卡的文件中读取内容转化成字符串
     **/
    public static String readContentFromFile(String strFilePath) {
        String path = strFilePath;
        String content = ""; //文件内容字符串
        //打开文件
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()) {
            Log.d("TestFile", "The File doesn't not exist.");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    //分行读取
                    while ((line = buffreader.readLine()) != null) {
                        content += line + "\n";
                    }
                    instream.close();
                }
            } catch (java.io.FileNotFoundException e) {
                Log.d("TestFile", "The File doesn't not exist.");
            } catch (IOException e) {
                Log.d("TestFile", e.getMessage());
            }
        }
        return content;
    }

}
