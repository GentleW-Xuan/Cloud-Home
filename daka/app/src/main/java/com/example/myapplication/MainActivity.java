package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMapOptions;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.Text;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONArray;

import org.json.JSONException;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;
    private MapView mapView;
    private AMap aMap;
    private LinearLayout.LayoutParams mParams;
    private RelativeLayout mContainerLayout;
    private LatLng centerSHpoint = new LatLng(30.428681, 114.417657);

    Button loginbutton = null;
    Marker marker = null;


    static String latitude = null;
    static String longitude = null;

    String sRes = null;

    private static Random random = new Random(System.nanoTime());


    private Handler handler = null;
    private static final int REQUEST_CODE = 1024;
    @SuppressLint({"ResourceType", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
//            }
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //是否有所有问读写权限
            if (Environment.isExternalStorageManager()) {
                //有所有文件读写权限  TODO something
            }else {
                //跳转到打开权限页面
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);

                intent.setData(Uri.parse("package:" + this.getPackageName()));

                startActivityForResult(intent, REQUEST_CODE);


            }

        }else {
            //TODO something
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限

        }
        //创建属于主线程的handler
        handler=new Handler();
        mContainerLayout = (RelativeLayout) findViewById(R.id.activity_main);

        loginbutton = findViewById(R.id.LoginButton);


        AMapOptions aOptions = new AMapOptions();

        aOptions.camera(new CameraPosition(centerSHpoint, 18f, 0, 0));

        mapView = new MapView(this, aOptions);
        mapView.onCreate(savedInstanceState);
        mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        mContainerLayout.addView(mapView, mParams);
        init();

        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //运行打卡
                        sRes = sendPost();
//                        sRes = "{\"success\":true,\"error\":\"\",\"errorCode\":200,\"data\":{\"pointId\":\"600f9995e4b07d7b111863c0_0\",\"pointName\":null,\"pointIndex\":0,\"pointType\":\"START\",\"startTime\":\"2022-09-28 06:00:00\",\"middleTime\":\"2022-09-28 08:10:00\",\"endTime\":\"2022-09-28 12:30:00\",\"time\":1664327259772,\"feature\":\"虹信无线通信产业园\",\"clockInType\":1,\"innerWorkLong\":0.0,\"outerWorkLong\":0.0,\"recordId\":\"b23b6789-a35d-40e1-a04b-4b364639ac2e\",\"photoIds\":null,\"removeRecordId\":null,\"featureDetail\":\"湖北省武汉市江夏区藏龙岛谭湖二路1号虹信产业园2号楼4楼\",\"medal\":null,\"attendanceTips\":null,\"faceRecognition\":null}}";
//                        sRes = "{\"code\":-2,\"success\":false,\"txid\":\"TXid-b830163fa39e4f42a35ff484c5f52f2a\",\"source\":\"attendance-signapi\",\"errorMsg\":\"登录令牌已经失效，请重新登录\"}";
                        try {
                            JSONObject dakares= new JSONObject(sRes);
                            String jieguo = dakares.getString("success");
                            jieguo = jieguo.replace("\"", "");
                            boolean b = jieguo.equals("false");
                            if (b){
                                //开始登陆
                                sRes = sendLoginPost();
                                Log.d("dk","打卡" + sRes);
//                                sRes = "{\"success\":true,\"error\":null,\"errorCode\":100,\"data\":{\"isMobileFirstLogin\":false,\"isHidePhone\":0,\"companyName\":\"武汉虹旭信息技术有限责任公司\",\"jobTitle\":\"软件工程师\",\"wbNetworkId\":\"5ffd655fe4b0ba4b6d64af33\",\"orgInfoId\":\"5ffd655fe4b0ba4b6d64af33\",\"tid\":\"20684867\",\"contact\":[{\"name\":\"生日\",\"type\":\"O\",\"publicid\":\"VIRTUAL\",\"permission\":\"W\",\"value\":\"1998-01-30\",\"inputType\":\"date\"},{\"name\":\"手机\",\"type\":\"P\",\"publicid\":\"VIRTUAL\",\"permission\":\"R\",\"value\":\"13298312669\"}],\"provice\":\"\",\"networkSubType\":\"TEAM\",\"id\":\"6065535ae4b04f0087a2eca2\",\"activeTime\":\"2021-04-01 13:00:10\",\"accountType\":\"\",\"active\":\"1\",\"bindedPhone\":\"13298312669\",\"isAdmin\":0,\"oauth_token_secret\":\"c97b4091baa827db4cfeb2a2171f554f\",\"weights\":568,\"isHide\":false,\"wbUserId\":\"6065535ae4b04f0087a2ec9f\",\"fullPinyin\":\"wu yu xuan\",\"companyNameBack\":\"武汉虹旭信息技术有限责任公司\",\"positiveDate\":\"\",\"phone\":\"13298312669\",\"name\":\"武宇璇\",\"userType\":20,\"status\":1,\"birthday\":\"1998-01-30\",\"eid\":\"20684867\",\"gender\":\"1\",\"city\":\"\",\"openId\":\"6065535ae4b04f0087a2ec9f\",\"orgId\":\"2f8c450b-c475-4d6b-b7de-6d791297f806\",\"photoUrl\":\"https://static.yunzhijia.com/space/c/photo/load?id=6065535a9a39210001d9fce7\",\"uid\":\"134152062\",\"netSubType\":\"TEAM\",\"identityType\":2,\"oauth_token\":\"37cc20739b67b9b3395bd2365c4aa6\",\"oem\":\"\",\"jobNo\":\"0320008775\",\"department\":\"逆向分析组\",\"email\":\"\",\"registerDate\":\"2021-04-01 13:00:10\",\"hireDate\":\"\",\"bindedEmail\":\"\",\"updateTime\":\"2022-09-17 16:00:43\",\"oId\":\"6065535ae4b04f0087a2eca4\",\"userName\":\"武宇璇\",\"isResetPwd\":false,\"token\":\"25669077f7875abe6141c9aea8a5195\",\"isBindWechat\":false,\"hide\":false,\"resetPwd\":false,\"createTime\":\"2021-04-01 13:00:10\",\"identityId\":null,\"isTrustedDevice\":true,\"leaveDate\":\"\"},\"userName\":\"13298312669\",\"isFieldVal\":false,\"isHSTeamIn\":false}";
                                JSONObject denglu= new JSONObject(sRes);
                                JSONObject data = denglu.getJSONObject("data");
                                String token = data.getString("token");

                                //写文件
                                try {
                                    File file = new File(Environment.getExternalStorageDirectory(),"//Download//opentoken.config");
                                    FileOutputStream  token_file = new FileOutputStream (file);
                                    token_file.write(token.getBytes());
                                    token_file.close();
                                    sRes = sendPost();
                                    Log.d("dk","登陆" + sRes);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        handler.post(runnableUi);
                    }
                }).start();
            }
        });
    }

    // 构建Runnable对象，在runnable中更新界面
    Runnable runnableUi = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getApplicationContext(),sRes,Toast.LENGTH_LONG).show();
        }

    };

    protected static String generateNonce() {
        return Long.toString(random.nextLong());
    }

    protected static String generateTimestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }


    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();

            aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {

                }

                @Override
                public void onCameraChangeFinish(CameraPosition cameraPosition) {

                    Log.d("dk", String.valueOf(aMap.getCameraPosition()));


                    if (marker != null){
                        aMap.clear();
                    }


                    String Src = String.valueOf(aMap.getCameraPosition());
                    String idInfo = Src.substring(Src.indexOf("(")+1,Src.indexOf(")"));
                    //latitude
                    //longitude
                    latitude = idInfo.substring(0,idInfo.indexOf(',')); //纬度
                    longitude = idInfo.substring(idInfo.indexOf(',') + 1); //经度

                    double v = Double.parseDouble(latitude);
                    double v1 = Double.parseDouble(longitude);

                    LatLng latLng = new LatLng(v,v1);
                    marker = aMap.addMarker(new MarkerOptions().position(latLng).title("武汉").snippet(latitude + "," + longitude));


                    loginbutton.setText(idInfo);
                }


            });
        }
    }

    public static String sendPost() {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL("https://www.yunzhijia.com/attendance-signapi/signservice/sign/signIn");
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("user-agent", "10201/10.1.0;Android 10;Xiaomi;MI+8;102;1080*2029;deviceId:581f588b-01f7-3f96-a761-7ba6774b3de3;deviceName:Xiaomi MI+8;clientId:10201;os:Android 10;brand:Xiaomi;model:MI+8;lang:zh-CN;");
            conn.setRequestProperty("accept-language", "zh-CN");
            File file = new File(Environment.getExternalStorageDirectory(),"//Download//opentoken.config");
            FileInputStream  opentoken_file = new FileInputStream (file);
            byte[] b = new byte[opentoken_file.available()];
            opentoken_file.read(b);
            String opentoken = new String(b);

            opentoken_file.close();

            conn.setRequestProperty("opentoken", opentoken);

            String oauth_nonce = generateNonce();
            String oauth_timestamp = generateTimestamp();

            //wu
            String oauth_signature_sha1 = "bssid=ec%3A26%3Aca%3A49%3A93%3A40&configId=600f9995e4b07d7b111863c0_10&lat=" + latitude + "&lng=" + longitude + "&networkId=5ffd655fe4b0ba4b6d64af33&oauth_consumer_key=lRudaAEghEJGEHkw&oauth_nonce=" + oauth_nonce +"&oauth_signature_method=HMAC-SHA1&oauth_timestamp="+ oauth_timestamp +"&oauth_token=37cc20739b67b9b3395bd2365c4aa6&oauth_version=1.0&ssid=xxaq-5.0&userId=6065535ae4b04f0087a2ec9f";
            //bao
//            String oauth_signature_sha1 = "bssid=ec%3A26%3Aca%3A49%3A93%3A40&configId=600f9995e4b07d7b111863c0_10&lat=" + latitude + "&lng=" + longitude + "&networkId=5ffd655fe4b0ba4b6d64af33&oauth_consumer_key=lRudaAEghEJGEHkw&oauth_nonce=" + oauth_nonce +"&oauth_signature_method=HMAC-SHA1&oauth_timestamp="+ oauth_timestamp +"&oauth_token=a24ec27def32c52e3fbe8e227d41e9de&oauth_version=1.0";

            String post = "POST&";
            String https = "https://www.yunzhijia.com/attendance-signapi/signservice/sign/signIn";
            https = URLEncoder.encode( https, "UTF-8" );
            //URL编码
            String oauth_signature_url = post + https + "&" + URLEncoder.encode( oauth_signature_sha1, "UTF-8" );
            //做hmac——sha1

            //wu
            SecretKeySpec secretKeySpec = new SecretKeySpec("0QNOWUHsWcI9i8UyqBFKUarayqBDUsVnxJrumYHEUl&c97b4091baa827db4cfeb2a2171f554f".getBytes(), "HmacSHA1");
            //bao
//            SecretKeySpec secretKeySpec = new SecretKeySpec("0QNOWUHsWcI9i8UyqBFKUarayqBDUsVnxJrumYHEUl&427e48b71da1016a3d8962463ed6541".getBytes(), "HmacSHA1");
            Mac instance = Mac.getInstance("HmacSHA1");
            instance.init(secretKeySpec);

            String oauth_signature_base64 = Base64.encodeToString(instance.doFinal(oauth_signature_url.getBytes("UTF-8")), Base64.DEFAULT).trim();
            String oauth_signature = URLEncoder.encode(oauth_signature_base64, "UTF-8");


            //wu
            String authorization = "OAuth oauth_consumer_key=\"lRudaAEghEJGEHkw\", oauth_nonce=\"" + oauth_nonce + "\", oauth_signature=\"" + oauth_signature + "\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"" + oauth_timestamp + "\", oauth_token=\"37cc20739b67b9b3395bd2365c4aa6\", oauth_version=\"1.0\"";
            //bao
//            String authorization = "OAuth oauth_consumer_key=\"lRudaAEghEJGEHkw\", oauth_nonce=\"" + oauth_nonce + "\", oauth_signature=\"" + oauth_signature + "\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"" + oauth_timestamp + "\", oauth_token=\"a24ec27def32c52e3fbe8e227d41e9de\", oauth_version=\"1.0\"";
            conn.setRequestProperty("authorization", authorization);

            conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("accept-encoding", "gzip");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print("lng=" + longitude + "&bssid=ec%3A26%3Aca%3A49%3A93%3A40&configId=600f9995e4b07d7b111863c0_10&networkId=5ffd655fe4b0ba4b6d64af33&userId=6065535ae4b04f0087a2ec9f&ssid=xxaq-5.0&lat=" + latitude);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }



    public static String sendLoginPost() {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL("https://www.yunzhijia.com/openaccess/user/login");
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("user-agent", "10201/10.1.0;Android 10;Xiaomi;MI+8;102;1080*2029;deviceId:581f588b-01f7-3f96-a761-7ba6774b3de3;deviceName:Xiaomi MI+8;clientId:10201;os:Android 10;brand:Xiaomi;model:MI+8;lang:zh-CN;");
            conn.setRequestProperty("accept-language", "zh-CN");

            String oauth_nonce = generateNonce();
            String oauth_timestamp = generateTimestamp();

            String oauth_signature_sha1 = "oauth_consumer_key=lRudaAEghEJGEHkw&oauth_nonce=" + oauth_nonce + "&oauth_signature_method=HMAC-SHA1&oauth_timestamp=" + oauth_timestamp + "&oauth_version=1.0";

            String post = "POST&";
            String https = "https://www.yunzhijia.com/openaccess/user/login";
            https = URLEncoder.encode( https, "UTF-8" );
            //URL编码
            String oauth_signature_url = post + https + "&" + URLEncoder.encode( oauth_signature_sha1, "UTF-8" );
            //做hmac——sha1
            SecretKeySpec secretKeySpec = new SecretKeySpec("0QNOWUHsWcI9i8UyqBFKUarayqBDUsVnxJrumYHEUl&".getBytes(), "HmacSHA1");
            Mac instance = Mac.getInstance("HmacSHA1");
            instance.init(secretKeySpec);

            String oauth_signature_base64 = Base64.encodeToString(instance.doFinal(oauth_signature_url.getBytes("UTF-8")), Base64.DEFAULT).trim();
            String oauth_signature = URLEncoder.encode(oauth_signature_base64, "UTF-8");


            String authorization = "OAuth oauth_consumer_key=\"lRudaAEghEJGEHkw\", oauth_nonce=\"" + oauth_nonce + "\", oauth_signature=\"" + oauth_signature + "\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"" + oauth_timestamp + "\", oauth_version=\"1.0\"";
            conn.setRequestProperty("authorization", authorization);

            conn.setRequestProperty("content-type", "application/json; charset=utf-8");
//            conn.setRequestProperty("accept-encoding", "gzip");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
//保总
//            String password = "Hx123456";
//            Cipher instance1 = Cipher.getInstance("DES/CBC/PKCS5Padding");
//            instance1.init(1, new SecretKeySpec("13349964".getBytes(), "DES"), new IvParameterSpec("13349964".getBytes()));
//            password = Base64.encodeToString(instance1.doFinal(password.getBytes("UTF-8")), Base64.DEFAULT).trim();
            String password = "Ww106471016";
            Cipher instance1 = Cipher.getInstance("DES/CBC/PKCS5Padding");
            instance1.init(1, new SecretKeySpec("13298312".getBytes(), "DES"), new IvParameterSpec("13298312".getBytes()));
            password = Base64.encodeToString(instance1.doFinal(password.getBytes("UTF-8")), Base64.DEFAULT).trim();


//             发送请求参数
            out.print("{\"eid\":\"\",\"userName\":\"13298312669\",\"password\":\"" + password + "\",\"appClientId\":\"10201\",\"deviceId\":\"581f588b-01f7-3f96-a761-7ba6774b3de3\",\"deviceType\":\"MI 8\",\"ua\":\"10201\\/10.1.0;Android 10;Xiaomi;MI+8;102;1080*2029;deviceId:581f588b-01f7-3f96-a761-7ba6774b3de3;deviceName:Xiaomi MI+8;clientId:10201;os:Android 10;brand:Xiaomi;model:MI+8;lang:zh-CN;\"}");
            //保总
//            out.print("{\"eid\":\"\",\"userName\":\"13349964931\",\"password\":\"" + password + "\",\"appClientId\":\"10201\",\"deviceId\":\"581f588b-01f7-3f96-a761-7ba6774b3de3\",\"deviceType\":\"MI 8\",\"ua\":\"10201\\/10.1.0;Android 10;Xiaomi;MI+8;102;1080*2029;deviceId:581f588b-01f7-3f96-a761-7ba6774b3de3;deviceName:Xiaomi MI+8;clientId:10201;os:Android 10;brand:Xiaomi;model:MI+8;lang:zh-CN;\"}");
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                Log.i("clash", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
                if (grantResults[i] == -1){
                    Runtime.getRuntime().exit(0);
                }

            }
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}