package com.itheima.musicproject.activity;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import com.davemorrissey.labs.subscaleview.ImageSource;
import com.itheima.musicproject.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.SSLException;

public class Facedetect extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facedetect);
        overridePendingTransition(0, 0);
        getPermission();
        bindViews();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

    }
//    private com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView iv_face;
    private ImageView iv_face;
    private Button btn_album,btn_takepic,btn_recommend;
    private TextView tv_result;
    private Handler uihandler;//更新UI
    private Handler songurlhandler;
    private String emotion="default";//记录的emotion检测结果
    private String songurl;



    private final int TAKE_PHOTO = 1;//拍照操作
    private final int PICK_IMAGE=2;
    private Uri imageUri;//拍照所得到的图像的保存路径
    private String fileName;//当前用户拍照或者从相册选择的照片的文件名

    private static String boundaryString=getBoundary();
    private final static int CONNECT_TIME_OUT=30000;
    private final static int READ_OUT_TIME=50000;
    //获得权限
    public void getPermission(){
        //内存读取权限
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        int permission = ActivityCompat.checkSelfPermission(Facedetect.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    Facedetect.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        //相机权限
        if(ContextCompat.checkSelfPermission(
                this,Manifest.permission.CAMERA)!=                                                            PackageManager.PERMISSION_GRANTED)
        {ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                1);
        }


    }

    private void bindViews() {
        iv_face = findViewById(R.id.iv_face);
        btn_album=findViewById(R.id.btn_album);
        btn_album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPhoto();
            }
        });
        btn_takepic=findViewById(R.id.btn_takepic);
        btn_takepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        btn_recommend=findViewById(R.id.btn_recommend);
        btn_recommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(emotion.equals("default")){
                    Toast.makeText(Facedetect.this,"请选择照片",Toast.LENGTH_LONG).show();
                }
                else {
                    Intent intent = new Intent(Facedetect.this, MusicPlayerActivity.class);
                    intent.putExtra("path", songurl);
                    startActivity(intent);
                }


            }
        });
        tv_result=findViewById(R.id.tv_result);
        uihandler=new Handler(){
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case 0://sadneess
                        tv_result.setText("sadness");break;
                    case 1://neutral
                        tv_result.setText("neutral");break;
                    case 2://disgust
                        tv_result.setText("disgust");break;
                    case 3://anger
                        tv_result.setText("anger");break;
                    case 4://surprise
                        tv_result.setText("surprise");break;
                    case 5://fear
                        tv_result.setText("fear");break;
                    case 6://happiness
                        tv_result.setText("happiness");break;
                    case 7:
                        Toast.makeText(Facedetect.this,"检测不到，请再试一次",Toast.LENGTH_LONG).show();
                    case 8:
                        btn_recommend.setEnabled(true);break;
                    case 9:
                        btn_recommend.setEnabled(false);break;
                    default:break;
                }
            }
        };

    }

    //从图库获得图片
    public void getPhoto(){
        Intent galleryIntent=new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        if(galleryIntent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(Intent.createChooser(galleryIntent,"Select Picture"),PICK_IMAGE);
        }
    }

    //打开摄像头拍照
    public void takePhoto(){
        //文件命名
        SimpleDateFormat format=new SimpleDateFormat("yyyyMMddHHmmss");
        Date date=new Date(System.currentTimeMillis());
        this.fileName="easyasset"+format.format(date);

        //用于存放拍照得到照片
        File path=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File outputImage=new File(path,this.fileName+".jpg");

        //以防万一检测文件是否存在，存在则删除
        if(outputImage.exists()){
            outputImage.delete();
        }
        try {
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //将file对象转化为uri对象
        this.imageUri=Uri.fromFile(outputImage);

        //启动系统拍照intent
        Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,this.imageUri);
        startActivityForResult(intent,this.TAKE_PHOTO);

    }


    //intent 回调方法
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if(resultCode!=RESULT_OK){
            Log.d("hccc","result"+resultCode);
            Toast.makeText(this,"图片资源出错",Toast.LENGTH_LONG).show();

        }
        //处理相机拍照
        else if(requestCode==TAKE_PHOTO){
            //不能用
//            iv_face.setImage(ImageSource.uri(this.imageUri));
            imageUri= Uri.parse(getPath(this,data.getData()));
            iv_face.setImageURI(imageUri);
            String uri=imageUri.toString();
            uri=uri.replace("file:///","/");

            Log.d("hccc","拍照返回的uri"+uri);
            //detectEmotion(Uri.parse(uri));
        }
        //处理从图库选择
        else if(requestCode==PICK_IMAGE&&data!=null&&data.getData()!=null){
            Uri uri=data.getData();
            try{
                Bitmap bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                Log.d("hccc","相册返回的uri"+uri);
                String imageuri=getPath(this,uri);
                Log.d("hccc",imageuri);
//                iv_face.setImage(ImageSource.bitmap(bitmap));
                iv_face.setImageBitmap(bitmap);
                detectEmotion(imageuri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
    private static String encode(String value) throws Exception{
        return URLEncoder.encode(value, "UTF-8");
    }
    private static String getBoundary() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for(int i = 0; i < 32; ++i) {
            sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-".charAt(random.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_".length())));
        }
        return sb.toString();
    }

    //post上传进行检测并获得返回值
    private void detectEmotion(String uri){
        File file = new File(uri);
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
                    Log.d("hccc","返回的json string"+str);
                    emotion=getEmotion(str);
                    Message msg1=new Message();
                    msg1.what=9;
                    uihandler.sendMessage(msg1);
                    songurl=getSongUrl(emotion);
                    Log.d("hccc","检测的表情"+emotion);
                    if(emotion.equals("error")){
                        Message msg = new Message();
                        msg.what = 7;
                        uihandler.sendMessage(msg);
                    }
                    else {
                        Message msg=new Message();
                        msg.what=8;
                        uihandler.sendMessage(msg);

                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
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


    //解析返回值，生成表情参数
    private String getEmotion(String str) throws JSONException {
        JSONObject jsonObject = new JSONObject(str);
    //检测不到人脸的处理
        Iterator<String> key = jsonObject.keys();
        key.next();
        String msg_error = key.next();
        Log.d("hccc", msg_error);
        if (msg_error.equals("error_message")) {
            Log.d("hccc", "没检测到人脸");
            return "error";
        }

        else {
            JSONArray faces_json = jsonObject.getJSONArray("faces");
            Log.d("hccc", "faces:" + faces_json.toString());
            //目前仅支持单人
//        for(int i=0;i<faces_json.length();i++){
//            JSONObject face_json=faces_json.getJSONObject(i);
            JSONObject face_json = faces_json.getJSONObject(0);
            JSONObject attributes_json = face_json.getJSONObject("attributes");
            JSONObject emotion_json = attributes_json.getJSONObject("emotion");

            //按照sadness，neutral，disgust，anger，surprise，fear，happiness的顺序
            Iterator<String> iterator = emotion_json.keys();
            Double[] emotion = new Double[7];

            //获得比重最大的femotion

            int femotion = 0;
            for (int j = 0; iterator.hasNext(); j++) {
                emotion[j] = emotion_json.getDouble(iterator.next());
                if (j > 0 && emotion[j] >= emotion[j - 1]) {
                    femotion = j;
                }
                //遍历每个表情
                Log.d("hccc", emotion[j] + "");
            }

            Log.d("hccc", "最终表情"+femotion);
            Message msg = new Message();
            msg.what = femotion;
            uihandler.sendMessage(msg);
            switch (femotion) {
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
                default:
                    return null;

            }

        }
    }

    private String getSongUrl(final String emotion){

        String url=getSongList(emotion);
        return url;
    }


    public String getSongList(final String str){
        HttpURLConnection connection = null;

        int offset = (int) (Math.random() * 10);
        StringBuilder host = new StringBuilder();
        try {
            host.append(" https://api.itooi.cn/music/netease/hotSongList?key=579621905&cat=")
                    .append(URLEncoder.encode(str, "utf-8")).append("&limit=1&offset=").append(offset);
            Log.d("hccc", "获取分类的歌单"+String.valueOf(host));
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
            Log.d("hccc", "获取分类的歌单返回"+jonString);

            reader.close();
            JSONObject jsonObject = new JSONObject(jonString);
            JSONArray jsdata = jsonObject.getJSONArray("data");
            JSONObject jslist = jsdata.getJSONObject(0);
            String id = jslist.getString("id");
            Log.d("hccc", "歌单id"+id);
            return getSongId(id);


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
        return null;


    }

    public String getSongId(final String listId){
        HttpURLConnection connection = null;

        StringBuilder host = new StringBuilder();
        try {
            host.append("https://api.itooi.cn/music/netease/songList?key=579621905&id=")
                    .append(URLEncoder.encode(listId, "utf-8")).append("&limit=1&offset=0");
            Log.d("hccc", "获取歌单"+String.valueOf(host));
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
            Log.d("hccc", "返回的歌单"+jonString);

            reader.close();


            JSONObject jsonObject=new JSONObject(jonString);
            JSONObject jsdata=jsonObject.getJSONObject("data");
            int seed=jsdata.getInt("songListCount");
            int offset = (int) (Math.random() * seed);
            JSONArray jsSongs=jsdata.getJSONArray("songs");
            JSONObject jssong=jsSongs.getJSONObject(offset);
            int songid=jssong.getInt("id");
            String songurl=jssong.getString("url");
            Log.d("hccc", "歌曲播放地址"+songurl);
            return songurl;

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

        return null;
    }
    //路径转换
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }
    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

}
