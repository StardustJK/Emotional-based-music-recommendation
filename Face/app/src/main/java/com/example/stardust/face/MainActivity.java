package com.example.stardust.face;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.SSLException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermission();
        btn=findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setAction(intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                startActivityForResult(Intent.createChooser(
                        intent, "Select Picture"), PICK_IMAGE);
            }
        });
    }
    private final static int CONNECT_TIME_OUT = 30000;
    private final static int READ_OUT_TIME = 50000;
    private static String boundaryString = getBoundary();
    private Button btn;
    private final int PICK_IMAGE = 1;
    private MediaPlayer mPlayer=null;
    private boolean isRelease=true;//判断MediaPlayer是否释放的标识


    //选择相册，获得图片uri并进行检测
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {

            //小米手机的缘故，给强制转换了
            Uri uri = data.getData();
            String str=Uri.decode(String.valueOf(uri));
            str=str.replace("content://com.miui.gallery.open/raw//","/");
            Log.d("hcccc","uri:"+str);

            detectEmotion(Uri.parse(str));

        }
    }

    //post上去，获得json结果，并调用getEmotion解析
    public void detectEmotion(Uri uri){
        File file = new File(String.valueOf(uri));
        byte[] buff = getBytesFromFile(file);
        final String url = "https://api-cn.faceplusplus.com/facepp/v3/detect";
        final HashMap<String, String> map = new HashMap<>();
        final HashMap<String, byte[]> byteMap = new HashMap<>();
        map.put("api_key", "xR6jiba-VXyP7QXQAfottIWPbq0ATYx5");
        map.put("api_secret", "zMLjH919nZxU6EHSa6lCFIDr7HtXyCOW");
        map.put("return_attributes", "emotion");
        byteMap.put("image_file", buff);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    byte[] bacd = post(url, map, byteMap);
                    String str = new String(bacd);
                    Log.d("zxc",str);
                    String femotion=getEmotion(str);
                    getSongList(femotion);


                }catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();



    }



    //手动获取权限，安卓6.0以后
    public void getPermission(){
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

    }

    //解析返回的json 打印表情信息
    public String getEmotion(String str) throws JSONException {
        JSONObject jsonObject=new JSONObject(str);
        JSONArray faces_json=jsonObject.getJSONArray("faces");
        //目前仅支持单人
//        for(int i=0;i<faces_json.length();i++){
//            JSONObject face_json=faces_json.getJSONObject(i);
        JSONObject face_json=faces_json.getJSONObject(0);
        JSONObject attributes_json=face_json.getJSONObject("attributes");
            JSONObject emotion_json=attributes_json.getJSONObject("emotion");
            //按照sadness，neutral，disgust，anger，surprise，fear，happiness的顺序
            Iterator<String> iterator=emotion_json.keys();
            Double []emotion=new Double[7];

            int femotion=0;
            for(int j=0;iterator.hasNext();j++){
                emotion[j]=emotion_json.getDouble(iterator.next());
                if(j>0&&emotion[j]>=emotion[j-1]){
                    femotion=j;
                }
                Log.d("zxc",emotion[j]+"");
            }

            Log.d("zxc",femotion+"");
            switch (femotion){
                case 0://sadneess
                    return "伤感";
                case 1://neutral
                    return "安静";
                case 2://disgust
                    return "金属";//？？？
                case 3://anger
                    return "朋克";//？？？
                case 4://surprise
                    return "兴奋";
                case 5://fear
                    return "孤独";
                case 6://happiness
                    return "快乐";
                    default:return null;

            }

    }

    //post的具体操作
    protected static byte[] post(String url, HashMap<String, String> map, HashMap<String, byte[]> fileMap) throws Exception {
        HttpURLConnection conne;
        URL url1 = new URL(url);
        conne = (HttpURLConnection) url1.openConnection();
        conne.setDoOutput(true);
        conne.setUseCaches(false);
        conne.setRequestMethod("POST");
        conne.setConnectTimeout(CONNECT_TIME_OUT);
        conne.setReadTimeout(READ_OUT_TIME);
        conne.setRequestProperty("accept", "*/*");
        conne.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);
        conne.setRequestProperty("connection", "Keep-Alive");
        conne.setRequestProperty("user-agent", "Mozilla/4.0 (compatible;MSIE 6.0;Windows NT 5.1;SV1)");
        DataOutputStream obos = new DataOutputStream(conne.getOutputStream());
        Iterator iter = map.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<String, String> entry = (Map.Entry) iter.next();
            String key = entry.getKey();
            String value = entry.getValue();
            obos.writeBytes("--" + boundaryString + "\r\n");
            obos.writeBytes("Content-Disposition: form-data; name=\"" + key
                    + "\"\r\n");
            obos.writeBytes("\r\n");
            obos.writeBytes(value + "\r\n");
        }
        if(fileMap != null && fileMap.size() > 0){
            Iterator fileIter = fileMap.entrySet().iterator();
            while(fileIter.hasNext()){
                Map.Entry<String, byte[]> fileEntry = (Map.Entry<String, byte[]>) fileIter.next();
                obos.writeBytes("--" + boundaryString + "\r\n");
                obos.writeBytes("Content-Disposition: form-data; name=\"" + fileEntry.getKey()
                        + "\"; filename=\"" + encode(" ") + "\"\r\n");
                obos.writeBytes("\r\n");
                obos.write(fileEntry.getValue());
                obos.writeBytes("\r\n");
            }
        }
        obos.writeBytes("--" + boundaryString + "--" + "\r\n");
        obos.writeBytes("\r\n");
        obos.flush();
        obos.close();
        InputStream ins = null;
        int code = conne.getResponseCode();
        try{
            if(code == 200){
                ins = conne.getInputStream();
            }else{
                ins = conne.getErrorStream();
            }
        }catch (SSLException e){
            e.printStackTrace();
            return new byte[0];
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[4096];
        int len;
        while((len = ins.read(buff)) != -1){
            baos.write(buff, 0, len);
        }
        byte[] bytes = baos.toByteArray();
        ins.close();
        return bytes;
    }
    //将文件转成bytes文件准备上传
    public static byte[] getBytesFromFile(File f) {
        if (f == null) {
            return null;
        }
        try {
            FileInputStream stream = new FileInputStream(f);
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = stream.read(b)) != -1)
                out.write(b, 0, n);
            stream.close();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
        }
        return null;
    }
    //转换成bytes文件时用的
    private static String encode(String value) throws Exception{
        return URLEncoder.encode(value, "UTF-8");
    }
    //转换成bytes文件时用的
    private static String getBoundary() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for(int i = 0; i < 32; ++i) {
            sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-".charAt(random.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_".length())));
        }
        return sb.toString();
    }

    //放音乐相关
    private void getSongList(final String str) {
        HttpURLConnection connection = null;

        int offset = (int) (Math.random() * 10);
        StringBuilder host = new StringBuilder();
        try {
            host.append("https://api.bzqll.com/music/netease/hotSongList?key=579621905&cat=")
                    .append(URLEncoder.encode(str, "utf-8")).append("&limit=1&offset=").append(offset);
            Log.d("hccc", String.valueOf(host));
            if (Thread.interrupted())
                throw new InterruptedException();

            URL url = new URL(String.valueOf(host));
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            connection.connect();
            if (Thread.interrupted())
                throw new InterruptedException();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String jonString = reader.readLine();
            Log.d("hcc", jonString);

            reader.close();
            JSONObject jsonObject = new JSONObject(jonString);
            JSONArray jsdata = jsonObject.getJSONArray("data");
            Log.d("hccc", String.valueOf(jsdata));
            JSONObject jslist = jsdata.getJSONObject(0);
            Log.d("hccc", String.valueOf(jslist));
            String id = jslist.getString("id");
            Log.d("hccc", id);
            getSongId(id);


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null)
                connection.disconnect();
        }


    }

    public void getSongId(final String listId) {
        HttpURLConnection connection = null;

        StringBuilder host = new StringBuilder();
        try {
            host.append("https://api.bzqll.com/music/netease/songList?key=579621905&id=")
                    .append(URLEncoder.encode(listId, "utf-8")).append("&limit=1&offset=0");
            Log.d("hcccx", String.valueOf(host));
            if (Thread.interrupted())
                throw new InterruptedException();

            URL url = new URL(String.valueOf(host));
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            connection.connect();
            if (Thread.interrupted())
                throw new InterruptedException();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String jonString = reader.readLine();
            Log.d("hcccx", jonString);

            reader.close();


            JSONObject jsonObject=new JSONObject(jonString);
            JSONObject jsdata=jsonObject.getJSONObject("data");
            int seed=jsdata.getInt("songListCount");
            int offset = (int) (Math.random() * seed);
            JSONArray jsSongs=jsdata.getJSONArray("songs");
            JSONObject jssong=jsSongs.getJSONObject(offset);
            int songid=jssong.getInt("id");
            String songurl=jssong.getString("url");
            Log.d("hccc", songurl);
            playSong(songurl);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void playSong(String url){
        mPlayer=new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mPlayer.setDataSource(url);
        } catch (IOException e) {
            Log.d("hcc","setdata");
            e.printStackTrace();
        }
        try {
            mPlayer.prepare();
            Log.d("hcc","pd");
        } catch (IOException e) {
            Log.d("hcc","prepare");
            e.printStackTrace();
        }
        mPlayer.start();
    }


}

