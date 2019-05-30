package com.creditease.libaryexercise.http;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * 封装HttpURLConnection开箱即用
 * Create by yster@foxmail.com 2018/9/10/010 19:17
 */
public class HttpClient {
    private HttpURLConnection connection;
    private Charset charset = Charset.forName("UTF-8");
    private int readTimeout = 32000;
    private int connectTimeout = 10000;
    private String method = "GET";
    private boolean doInput = true;
    private Map<String, String> headers = null;
    private String data = null;

    /**
     * 实例化对象
     */
    public static HttpClient connect(String url) throws IOException {
        return new HttpClient((HttpURLConnection) new URL(url).openConnection());
    }

    /**
     * 禁止new实例
     */
    private HttpClient() {
    }

    private HttpClient(HttpURLConnection connection) {
        this.connection = connection;
    }

    /**
     * 设置读去超时时间/ms
     *
     * @param timeout
     */
    public HttpClient setReadTimeout(int timeout) {
        this.readTimeout = timeout;
        return this;
    }

    /**
     * 设置链接超时时间/ms
     *
     * @param timeout
     */
    public HttpClient setConnectTimeout(int timeout) {
        this.connectTimeout = timeout;
        return this;
    }

    /**
     * 设置请求方式
     *
     * @param method
     */
    public HttpClient setMethod(String method) {
        this.method = method;
        return this;
    }

    /**
     * 添加Headers
     *
     * @param map
     */
    public HttpClient setHeaders(Map<String, String> map) {
        String cookie = "Cookie";
        if (map.containsKey(cookie)) {
            headers = new HashMap<>();
            headers.put(cookie, map.get(cookie));
        }
        return this;
    }

    /**
     * 是否接受输入流
     * 默认true
     *
     * @param is
     */
    public HttpClient setDoInput(boolean is) {
        this.doInput = is;
        return this;
    }

    /**
     * 设置请求响应的编码
     */
    public HttpClient setCharset(String charset) {
        this.charset = Charset.forName(charset);
        return this;
    }

    /**
     * 写入数据,接受Map<String,String>或String类型<br>
     * 例如POST时的参数<br>
     * demo=1&name=2
     */
    public HttpClient setData(Object object) {
        if (object == null) {
            return this;
        } else if (object instanceof String) {
            this.data = (String) object;
        } else if (object instanceof Map) {
            Map map = (Map) object;
            StringBuilder builder = new StringBuilder();
            for (Object key : map.keySet()) {
                builder.append(key + "=" + map.get(key) + "&");
            }
            this.data = builder.toString().substring(0, builder.length() > 0 ? builder.length() - 1 : builder.length());
        }
        return this;
    }

    /**
     * 发起请求
     */
    public HttpClient execute() throws IOException {
        //添加请求头
        if (headers != null) {
            for (String key : headers.keySet()) {
                connection.setRequestProperty(key, headers.get(key));
            }
        }
        //设置读去超时时间为10秒
        connection.setReadTimeout(readTimeout);
        //设置链接超时为10秒
        connection.setConnectTimeout(connectTimeout);
        //设置请求方式,GET,POST
        connection.setRequestMethod(method.toUpperCase());
        //接受输入流
        connection.setDoInput(doInput);
        //写入参数
        if (data != null && !method.equalsIgnoreCase("GET")) {
            //启动输出流，当需要传递参数时需要开启
            connection.setDoOutput(true);
            //添加请求参数，注意：如果是GET请求，参数要写在URL中
            OutputStream output = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, charset));
            //写入参数 用&分割。
            writer.write(data);
            writer.flush();
            writer.close();
        }
        //发起请求
        connection.connect();
        return this;
    }

    /**
     * 获取HttpURLConnection
     */
    public HttpURLConnection getConnection() {
        return this.connection;
    }

    /**
     * 获取响应字符串
     */
    public String getBody(String... charsets) {
        //设置编码
        String charset = "UTF-8";
        if (charsets.length > 0) {
            charset = charsets[0];
        }
        //读取输入流
        try {
            InputStream inputStream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset));
            String line = bufferedReader.readLine();
            StringBuilder builder = new StringBuilder();
            while (line != null) {
                builder.append(line);
                line = bufferedReader.readLine();
            }
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //失败返回NULL
        return null;
    }

    public static void main(String[] args) throws IOException {
        String body = HttpClient.connect("http://dongcaidi.yixintest.site/api/reward/status?device_guid=96b1934245d049faad635efdc141aca1&platform=android&version_code=27&version_name=1.7.4&bundle_id=com.creditease.dongcaidi.debug&brand=other&screen_resolution=1080x1794&model=Android%20SDK%20built%20for%20x86&channel=dongcaidi&imei=&android_id=22b84db9c63de399&user_id=-1&session_id=&sn=b21322eaf5a49181cecd46df11e039d0")
                .setMethod("GET")
                .setCharset("UTF-8")
                .execute()
                .getBody();
        try {
            JSONObject object = new JSONObject(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(body);
    }

}
