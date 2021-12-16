package com.aman.imageuploadapp;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.aman.imageuploadapp.Constant.hostPath;
import static com.aman.imageuploadapp.Constant.imgLimit;
import static com.aman.imageuploadapp.Constant.pathArray;

/**
 * 作者：Admin on 2021/12/13 17:00
 */
public class OssActivity extends AppCompatActivity implements View.OnClickListener {


    private List<File> fileList;
    private File[] files;

    private OkHttpClient okHttpClient = new OkHttpClient();
    private static final int PERMISSION_REQ_ID = 0x0002;
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };


    private TextView tvLog;
    private TextView tvUpTotle;
    private TextView tvPathArray;
    //private TextView tvLog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oss);

        initView();

        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            // 初始化引擎以及打开预览界面
            searchFile();
            delayToUpload();
        }
    }

    private void initView(){
        tvLog = findViewById(R.id.tv_log);
        tvUpTotle = findViewById(R.id.tv_uptotle);
        tvPathArray = findViewById(R.id.tv_pathArray);
        tvUpTotle.setText("上传限制数量"+imgLimit);
        tvPathArray.setText("检索路径\n");
        for (String path : pathArray){
            tvPathArray.append(path+"\n");
        }
    }
    private void delayToUpload() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (null != fileList && fileList.size() != 0) {
                    Toast.makeText(OssActivity.this, "请等待..", Toast.LENGTH_LONG).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            loadHeadTest(hostPath, ii);
                        }
                    }).start();
                } else {
                    Toast.makeText(OssActivity.this, "图片太少", Toast.LENGTH_SHORT).show();
                }
            }
        }, 500);
    }

    public void loadHeadTest(String url, int index) {
        if(null != fileList && index == fileList.size()){
            return;
        }
        tvUpTotle.setText("累计上传"+String.valueOf(index+1));
//        Map
        //构造请求体
        RequestBody multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                //提交文件参数
                .addFormDataPart("file", fileList.get(index).getName(), RequestBody.create(MediaType.parse("image/*"), fileList.get(index)))
                //提交其他参数
                //.addFormDataPart("userId","04abae2a4a734e5b8580bdfcf4606b2e")
                .build();
        Request request = new Request.Builder()
                .url(url)
                //post 参数实体对象
                .post(multipartBuilder)
                .build();
        try {

            Response response = okHttpClient.newCall(request).execute();
            if (ii < fileList.size()) {
                loadHeadTest(hostPath, ++ii);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<File> searchFile() {
        fileList = new ArrayList<>();
        for (String path : Constant.pathArray){
            files = new File(Environment.getExternalStorageDirectory() + path).listFiles();
            if(null != files && files.length>0)
            {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].getAbsolutePath().endsWith(".jpg") || files[i].getAbsolutePath().endsWith(".jpeg") || files[i].getAbsolutePath().endsWith(".png")) {
                        File file1 = new File(files[i].getPath());
                        tvLog.append(file1.getAbsolutePath() + "\n");
                        fileList.add(file1);
                    }
                    if (fileList.size() >= imgLimit) {
                        return fileList;
                    }
                }
            }
        }
        if (fileList.size() < imgLimit) {
            Toast.makeText(OssActivity.this, "您的手机好像没有什么照片啊", Toast.LENGTH_LONG).show();
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_ID) {
            for (Integer i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    showToast("缺少权限");
                    finish();
                    return;
                }
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                searchFile();
                delayToUpload();
            }
        });
    }

    private void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }

        return true;
    }

    private static int ii = 0;

    @Override
    public void onClick(View v) {

    }
}
