package com.redrock.wechatchat.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class SendUtil {
    public static String sendPost(String urlStr, Map<String, String> param) {
        String paramStr=null;
        if (param.size() > 1) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : param.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            sb.delete(sb.length() - 1, sb.length());
            paramStr=sb.toString();
        }
        return sendPost(urlStr,paramStr);
    }

    public static String sendPost(String urlStr, String param) {
        String result = null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("POST");
            con.connect();
            StreamUtil.writeStream(con.getOutputStream(),param);
            result=StreamUtil.getText(con.getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String sendGet(String urlStr) {
        String result = null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("GET");
            con.connect();
            result = StreamUtil.getText(con.getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String sendGet(String urlStr, Map<String, String> param) {
        StringBuilder sb = new StringBuilder();
        if (param.size() > 1) {
            sb.append(urlStr).append("?");
            for (Map.Entry<String, String> entry : param.entrySet()) {
                sb.append(entry.getKey()).append("=").append("&");
            }
            sb.delete(sb.length() - 1, sb.length());
        }
        return sendGet(sb.toString());
    }
}
