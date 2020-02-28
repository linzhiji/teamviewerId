package com.ishehui.teamviewerid;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Create by WangTengFei on 2019/12/6 10:37
 * 获取TeamViewer log文件中的ID
 */
public class TeamViewerHelper {

    private static final String TAG = "TeamViewerHelper";

    /**
     * 定义script的正则表达式
     */
    private static final String REGEX_SCRIPT = "<script[^>]*?>[\\s\\S]*?<\\/script>";
    /**
     * 定义style的正则表达式
     */
    private static final String REGEX_STYLE = "<style[^>]*?>[\\s\\S]*?<\\/style>";
    /**
     * 定义HTML标签的正则表达式
     */
    private static final String REGEX_HTML = "<[^>]+>";
    /**
     * 定义空格回车换行符正则表达式
     */
    private static final String REGEX_SPACE = "\\s*|\t|\r|\n";

    /**
     * 截取字符串中的数字
     **/
    private static final String REGEX_ONLY_NUMBER = "\\d+";

    public TeamViewerHelper() {

    }

    public String getTeamviewerIDFromContent(Context content){
        String teamViewerLogFilePath = CardUtils.getTeamViewerLogFilePath(content);
        return getTeamViewerIdFromPath(teamViewerLogFilePath);
    }


    /**
     * 从teamViewer日志文件中获取ID
     **/
    public String getTeamViewerIdFromPath(String logFileDir) {
        String teamViewerId = null;
        if (!StringUtil.IsEmptyOrNullString(logFileDir)) {
            try {
                File logDirFile = new File(logFileDir);//获取log目录文件的file
                if (!logDirFile.exists() || logDirFile.isFile()) {
                    Log.e(TAG, "log文件所在文件夹不存在");
                }
                File[] logFileList = logDirFile.listFiles();
                if (logFileList != null && logFileList.length > 0) {
                    for (File logFile : logFileList) {
                        if (logFile.exists() && !StringUtil.IsEmptyOrNullString(logFile.getName()) && logFile.getName().toLowerCase().contains("log")) {
                            teamViewerId = getIdFromLogFile(logFile);
                            break;
                        }
                    }
                } else {
                    Log.e(TAG, "log文件所在文件夹为空");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return teamViewerId;
    }

    /**
     * 从对应的文件中获取id
     **/
    private String getIdFromLogFile(File logFile) {
        String teamViewerId = null;
        if (!logFile.exists() || logFile.isDirectory()) {
            Log.e(TAG, TAG + "---getIdFromLogFile 提供的日志文件不存在");
        } else {
            //设置文件权限
            logFile.setExecutable(true);
            logFile.setReadable(true);
            logFile.setWritable(true);
            FileInputStream fis = null;
            InputStreamReader is = null;
            BufferedReader br = null;
            try {
                fis = new FileInputStream(logFile);
                is = new InputStreamReader(fis);
                br = new BufferedReader(is);
                String temp;
                while ((temp = br.readLine()) != null) {
                    // TODO: 2019/12/6 获取teamViewer中的ID
                    if ((!StringUtil.IsEmptyOrNullString(temp)) && temp.contains("VersionInfo")) {
                        temp = temp.trim();//压缩空格
                        temp = replaceHTMLTag(temp);//替换字符串中的html标签
                        temp = temp.substring(temp.indexOf("ID:"));//截取ID后面的所有字符串
                        if (!StringUtil.IsEmptyOrNullString(temp)) {
                            temp = getNumStr(temp);
                            Log.e(TAG, TAG + "-----运行中的teamViewer ID 是：" + temp);
                            teamViewerId = temp;
                        } else {
                            Log.e(TAG, TAG + "-----运行中的teamViewer ID 是：不存在");
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return teamViewerId;
    }

    /**
     * 采用正则 替换字符串中的HTML标签
     **/
    private String replaceHTMLTag(String htmlStr) {
        // 过滤script标签
        Pattern scriptPattern = Pattern.compile(REGEX_SCRIPT, Pattern.CASE_INSENSITIVE);
        Matcher scriptMatcher = scriptPattern.matcher(htmlStr);
        htmlStr = scriptMatcher.replaceAll("");
        // 过滤style标签
        Pattern stylePattern = Pattern.compile(REGEX_STYLE, Pattern.CASE_INSENSITIVE);
        Matcher styleMatcher = stylePattern.matcher(htmlStr);
        htmlStr = styleMatcher.replaceAll("");
        // 过滤html标签
        Pattern htmlPattern = Pattern.compile(REGEX_HTML, Pattern.CASE_INSENSITIVE);
        Matcher htmlMatcher = htmlPattern.matcher(htmlStr);
        htmlStr = htmlMatcher.replaceAll("");
        // 过滤空格回车标签
        Pattern spacePattern = Pattern.compile(REGEX_SPACE, Pattern.CASE_INSENSITIVE);
        Matcher spaceMatcher = spacePattern.matcher(htmlStr);
        htmlStr = spaceMatcher.replaceAll("");
        return htmlStr.trim(); // 返回文本字符串
    }

    /**
     * 截取ID后面的数字
     * 例如:ID:1448002198 License: 10000 OS:And5.1.1 最终结果：1448002198
     **/
    private String getNumStr(String originalStr) {
        Pattern numberPattern = Pattern.compile(REGEX_ONLY_NUMBER, Pattern.CASE_INSENSITIVE);
        Matcher numberMatcher = numberPattern.matcher(originalStr);
        //截取开始的数字 遇到字母后直接结束
        if (numberMatcher.find()) {
            return numberMatcher.group();//标识提取第一个截取的结果 正好处于ID处
        }
        return null;
    }
}
