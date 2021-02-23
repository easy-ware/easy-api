package com.github.easyware.easyapisdk;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.IOUtils;
import org.springframework.http.HttpMethod;
import org.springframework.util.ClassUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.UriComponentsBuilder;


import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Global {

    public static Map<String, String[]> dataTypeMap = new HashMap() {{
        put("byte", new String[]{"integer", "int32"});
        put("short", new String[]{"integer", "int32"});
        put("int", new String[]{"integer", "int32"});
        put("long", new String[]{"integer", "int64"});
        put("float", new String[]{"number", "float"});
        put("double", new String[]{"number", "double"});
        put("string", new String[]{"string", null});
        put("char", new String[]{"string", null});
        put("date", new String[]{"string", "date-time"});
        put("boolean", new String[]{"boolean", null});
        put("object", new String[]{"object", null});//未知的对象

    }};
    private static Class[] ignoreParamTypes = new Class[]{javax.servlet.ServletRequest.class,
            javax.servlet.ServletResponse.class,
            javax.servlet.http.HttpServletRequest.class,
            HttpServletResponse.class,
            javax.servlet.http.HttpSession.class,
            javax.servlet.http.HttpSession.class,
            WebRequest.class,
            NativeWebRequest.class,
            java.security.Principal.class,

            HttpMethod.class,
            Locale.class,
            TimeZone.class,
            java.io.InputStream.class,
            java.time.ZoneId.class,
            java.io.Reader.class,
            java.io.OutputStream.class,
            java.io.Writer.class,
            Map.class,
            org.springframework.ui.Model.class,
            org.springframework.ui.ModelMap.class,
            Errors.class,
            BindingResult.class,
            SessionStatus.class,
            UriComponentsBuilder.class,
            RequestAttribute.class};
    public static Set<Class> ignoreParamTypeSet = new HashSet<>(Arrays.asList(ignoreParamTypes));



    static class Cache<T>{
        T data;
        long expire;
    }
    static Map<String,Cache> cacheMap=new ConcurrentHashMap<>();
    public static <T>T getCache(String key){
        Cache<T> cache=cacheMap.get(key);
        if(cache==null) return null;
        if(cache.expire<System.currentTimeMillis()) return null;
        return cache.data;
    }
    public static void addCache(String key ,Object data,long cacheSeconds){
        Cache cache=new Cache();
        cache.data=data;
        cache.expire=System.currentTimeMillis()+cacheSeconds*1000;
        cacheMap.put(key,cache);
    }

    public static JSONObject httpGetObject(String url,long cacheSeconds){
        JSONObject data=getCache(url);
        if(data!=null) return data;
        JSONObject result=null;
        try {
            result= JSONObject.parseObject(Global.httpGet(url,"UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(result==null) result=new JSONObject();
        addCache(url,result,cacheSeconds);
        return result;
    }
    public static String httpGet(String strUrl,String encoding) throws Exception {
        if(encoding==null) encoding="UTF-8";
        // 创建url资源
        URL url = new URL(strUrl);
        // 建立http连接
        HttpURLConnection conn = null;
        OutputStream out = null;

        try {
            conn = (HttpURLConnection) url.openConnection();
            // 设置允许输出
            //conn.setDoOutput(true);
            conn.setDoInput(true);
            // 设置不用缓存
            conn.setUseCaches(false);
            // 设置传递方式
            conn.setRequestMethod("GET");
            // 设置维持长连接
            conn.setRequestProperty("Connection", "Keep-Alive");
            // 设置文件字符集:
            conn.setRequestProperty("Charset", encoding);

            // 请求返回的状态
            if (conn.getResponseCode() == 200) {
                //System.out.println("连接成功");
                // 请求返回的数据
                InputStream in = conn.getInputStream();
                String text =   convert(in, encoding);
                in.close();
                return text;

            } else {
                throw new Exception("response code : " + conn.getResponseCode());
            }
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
    private static String convert( InputStream input,String encoding) throws Exception {
        StringWriter sw = new StringWriter();
        char[] buffer=new char[4096];
        InputStreamReader in = new InputStreamReader(input, encoding);

        int n = 0;
        while (-1 != (n = in.read(buffer))) {
            sw.write(buffer, 0, n);

        }
        return sw.toString();

    }

    public static boolean isSimpleValueType(Class<?> clazz) {
        return (ClassUtils.isPrimitiveOrWrapper(clazz) ||
                Enum.class.isAssignableFrom(clazz) ||
                CharSequence.class.isAssignableFrom(clazz) ||
                Number.class.isAssignableFrom(clazz) ||
                Date.class.isAssignableFrom(clazz) ||
                URI.class == clazz || URL.class == clazz ||
                Locale.class == clazz || Class.class == clazz);
    }

    public static String getBase(Class clazz) {

        //Class className = object.getClass();

            if (clazz.equals(Integer.class) || clazz.equals(int.class)) return "int";
            if (clazz.equals(Long.class) || clazz.equals(long.class) || clazz.equals(BigInteger.class)) return "long";
            if (clazz.equals(Double.class) || clazz.equals(double.class) || clazz.equals(BigDecimal.class)) return "double";
            if (clazz.equals(Float.class) || clazz.equals(float.class)) return "float";
            if (clazz.equals(Short.class) || clazz.equals(short.class)) return "short";
            if (clazz.equals(Byte.class) || clazz.equals(byte.class)) return "byte";

        if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) return "boolean";
        if (clazz.equals(Character.class) || clazz.equals(char.class)) return "char";
        if(Date.class.isAssignableFrom(clazz)) return "date";
        if (CharSequence.class.isAssignableFrom(clazz) || Enum.class.isAssignableFrom(clazz)) return "string";


        if (clazz.equals(Object.class)) return "object";//object无法决定真正类型，认为是不需要进一步遍历的基本类型
        return null;
    }

    public int i;
    public static void main(String[] args) throws NoSuchFieldException {
        System.out.println(  Global.class.getField("i").getType()==Integer.class);
    }
}
