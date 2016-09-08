package com.azusasoft.facehubcloudsdk.api.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.provider.Settings;

import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by SETA on 2016/3/8.
 */
public class UtilMethods {

    /**
     * 将http请求返回的错误throwable整合为Exception
     *
     * @param statusCode 返回码
     * @param throwable  异常
     * @param addition   其他异常信息
     * @return 整合后的异常
     */
    public static Exception parseHttpError(int statusCode, Throwable throwable, Object addition) {
        String additionStr = "\nOther messages : ";
        try {
            additionStr += addition.toString();
        } catch (Exception e) {
            additionStr += "Null.";
        }
        String msg = "Http Error!Status code : " + statusCode + "\nDetail : " + throwable + additionStr;
        Exception exception = new Exception(msg);
        LogX.fastLog(exception + "");
        return exception;
    }

    // 讲自定义String参数param添加到params中
    public static void addString2Params(RequestParams params, String paramStr) {
        try {
            String[] tmp = paramStr.split("&"); //划分为"key=value"字符串的数组
            for (String tmpKV : tmp) {
//                LogX.fastLog( "#\n" + tmpKV );
                String[] keyValue = tmpKV.split("=");   //每个"key=value"划分成key和value
                params.add(keyValue[0], keyValue[1]);
            }
        } catch (Exception e) {
            LogX.e("Error when adding String to Params : " + e);
        }
    }

    /**
     * Get String from a JsonObject.出错则返回null
     *
     * @param jsonObject JSON对象
     * @param key        key值
     * @return 想要的String或null
     */
    public static String getStringFromJson(JSONObject jsonObject, String key) {
        try {
            if (jsonObject.has(key) && (jsonObject.get(key) instanceof String)) {
                return jsonObject.getString(key);
            }
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isJsonWithKey(JSONObject jsonObject, String key) {
        try {
            return jsonObject.has(key)
                    && jsonObject.get(key) != (null)
                    && !jsonObject.isNull(key);
        } catch (JSONException e) {
            return false;
        }
    }

    /**
     * 复制文件
     **/
    public static void copyFile(String fromPath, String toPath) throws IOException {
        if (fromPath == null || fromPath.equals(toPath)) {
            return;
        }
        File fromFile = new File(fromPath);
        File toFile = new File(toPath);
        copyFile(fromFile, toFile);
    }

    public static void copyFile(File oldLocation, File newLocation) throws IOException {
        if (oldLocation.exists()) {
            BufferedInputStream reader = new BufferedInputStream(new FileInputStream(oldLocation));
            BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(newLocation, false));
            try {
                byte[] buff = new byte[8192];
                int numChars;
                while ((numChars = reader.read(buff, 0, buff.length)) != -1) {
                    writer.write(buff, 0, numChars);
                }
            } catch (IOException ex) {
                throw new IOException("IOException when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
            } finally {
                try {
                    if (reader != null) {
                        writer.close();
                        reader.close();
                    }
                } catch (IOException ex) {
                    //Log.e(TAG, "Error closing files when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() );
                }
            }
        } else {
            throw new IOException("Old location does not exist when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
        }
    }

    public static Object getNewer(Object strOld, Object strNew) {
        if (strNew == null) {
            return strOld;
        }
        return strNew;
    }

    public static int dp2Px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static JSONObject loadJSONFromAssets(Context context, String assetsPath) throws Exception{
        String json = null;
        InputStream is = context.getAssets().open(assetsPath);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        json = new String(buffer, "UTF-8");
        return new JSONObject(json);
    }

    public static String getDateString() {
        Date date = new Date();
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(date);
        return dateStr;
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(timeMillis);
//        String month = calendar.get(Calendar.MONTH)+1 +"";
//        if(month.length()<2){
//            month = "0"+month;
//        }
//        String day = Calendar.DAY_OF_MONTH+"";
//        if(day.length()<2){
//            day = "0"+day;
//        }
//        return calendar.get(Calendar.YEAR) + "-" + month + "-" + day;
    }

    public static String formatString(String string){
        if(string==null){
            return "";
        }
        return string+"";
    }

    /**
     * 获取签名的md5
     * @param context 上下文
     * @return 签名字符串
     */
    public static String getSignatureString(Context context){
        Signature signature = getSignature(context);
        String sign = null;
        if(signature!=null) {
            try {
                sign = doFingerprint(signature.toByteArray(), "MD5");
                LogX.d("Package sign : " + sign);
                return sign;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获取签名
     * @param context 上下文
     * @return 签名对象
     */
    private static Signature getSignature(Context context){
        try {
            Signature[] sigs = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES).signatures;
            return sigs[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param certificateBytes 获取到应用的signature值
     * @param algorithm        在上文指定MD5算法
     * @return md5签名
     */
    private static String doFingerprint(byte[] certificateBytes, String algorithm) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(certificateBytes);
        byte[] digest = md.digest();

        String toRet = "";
        for (int i = 0; i < digest.length; i++) {
//            if (i != 0) {
//                toRet += ":";
//            }
            int b = digest[i] & 0xff;
            String hex = Integer.toHexString(b);
            if (hex.length() == 1) {
                toRet += "0";
            }
            toRet += hex;
        }
        return toRet;
    }

    public static String getDeviceId(Context context){
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static String getStringByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

    public static int getIntegerUnicodeByString(String string){
        char[] chars = string.toCharArray();
        return (int) chars[0];
    }

//    public static String getEmoPath
}
