package com.creditease.libaryexercise;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.creditease.libaryexercise.http.HttpClient;
import com.creditease.libaryexercise.imageloader.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageLoader.build().bindBitmap("https://p.ssl.qhimg.com/dmfd/400_300_/t0120b2f23b554b8402.jpg", (ImageView) findViewById(R.id.image_view));
        new Thread(new Runnable() {
            @Override
            public void run() {
                String body = null;
                JSONObject jsonObject = null;
                try {
                    HttpClient client = HttpClient.connect("http://dongcaidi.yixintest.site/api/reward/status?device_guid=96b1934245d049faad635efdc141aca1&platform=android&version_code=27&version_name=1.7.4&bundle_id=com.creditease.dongcaidi.debug&brand=other&screen_resolution=1080x1794&model=Android%20SDK%20built%20for%20x86&channel=dongcaidi&imei=&android_id=22b84db9c63de399&user_id=-1&session_id=&sn=b21322eaf5a49181cecd46df11e039d0");
                        body =     client.setMethod("GET")
                            .setCharset("UTF-8")
                            .execute()
                            .getBody();
                    jsonObject = new JSONObject(body);
                    JSONObject data = (JSONObject) jsonObject.get("data");
                    String notification = (String) data.get("notification");
                    System.out.print(notification);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
