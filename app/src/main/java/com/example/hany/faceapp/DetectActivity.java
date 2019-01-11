package com.example.hany.faceapp;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.megvii.facepp.api.FacePPApi;
import com.megvii.facepp.api.IFacePPCallBack;
import com.megvii.facepp.api.bean.DetectResponse;
import com.megvii.facepp.api.bean.Face;
import com.megvii.facepp.api.bean.HumanBodyDetectResponse;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DetectActivity extends AppCompatActivity implements View.OnClickListener{


    private static final int PHOTO_REQUEST_CODE = 1;
    private final String API_KEY = "UG9PXl689pOhywdM0uGLqIkfSKO2P5Nl";
    private final String API_SECRET = "w5QPWOEW-ihgHmbJNKf9GcOI54drjQ56";
    private ImageView displayPicImg;
    private Button cameraBtn;
    private Button detectBtn;
    private Button galleryBtn;
    private Button saveBtn;
    private String imgPath;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);
        displayPicImg = findViewById(R.id.img_display_pic);
        cameraBtn = findViewById(R.id.btn_camera);
        cameraBtn.setOnClickListener(this);
        detectBtn = findViewById(R.id.btn_detect);
        detectBtn.setOnClickListener(this);
        galleryBtn = findViewById(R.id.btn_gallery);
        galleryBtn.setOnClickListener(this);
        saveBtn = findViewById(R.id.btn_save);
        saveBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_camera:
                break;
            case R.id.btn_detect:
                Map<String, String> params = new HashMap<>();
                params.put("return_attributes", "gender,age");
                params.put("return_landmark", "0");

                FacePPApi facePPApi = new FacePPApi(API_KEY, API_SECRET);
                facePPApi.detect(params, toByteArray(bitmap), new IFacePPCallBack<DetectResponse>() {
                    @Override
                    public void onSuccess(DetectResponse detectResponse) {
                        Canvas canvas = new Canvas(bitmap);
                        Paint paint = new Paint();

                        // 绘制黄色方框
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setColor(Color.YELLOW);
                        paint.setStrokeWidth(8);
                        for (Face face : detectResponse.getFaces()) {
                            int height = face.getFace_rectangle().getHeight();
                            int width = face.getFace_rectangle().getWidth();
                            int left = face.getFace_rectangle().getLeft();
                            int top = face.getFace_rectangle().getTop();
                            int right = width + left;
                            int bottom = height + top;
                            canvas.drawRect(left, top, right, bottom, paint);
                        }

                        // 绘制红框白字
//                        int distance = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
//                        distance = distance * bitmap.getHeight() / getResources().getDisplayMetrics().heightPixels;
                        for (Face face : detectResponse.getFaces()) {
//                            int rectHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, face.getFace_rectangle().getHeight(), getResources().getDisplayMetrics());
//                            int rectWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, face.getFace_rectangle().getWidth(), getResources().getDisplayMetrics());
                            // 设置红框尺寸
                            int rectHeight = face.getFace_rectangle().getHeight() / 3;
                            int rectWidth = face.getFace_rectangle().getWidth();
                            int distance = face.getFace_rectangle().getHeight() / 4;
                            int faceCenterX = face.getFace_rectangle().getLeft()
                                    + face.getFace_rectangle().getWidth() / 2;
                            int faceCenterY = face.getFace_rectangle().getTop()
                                    + face.getFace_rectangle().getHeight() / 2;
                            int rectCenterX = faceCenterX;
                            int rectCenterY = faceCenterY - face.getFace_rectangle().getHeight() / 2 - distance - rectHeight / 2;
                            int rectLeft = rectCenterX - rectWidth / 2;
                            int rectTop = rectCenterY - rectHeight / 2;
                            int rectRight = rectCenterX + rectWidth / 2;
                            int rectBottom = rectCenterY + rectHeight/ 2;
                            // 设置画笔颜色及风格，然后绘制红框
                            paint.setColor(Color.RED);
                            paint.setStyle(Paint.Style.FILL);
                            canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, paint);

                            // 设置字体参数
                            String str = face.getAttributes().getGender().getValue() + "," + face.getAttributes().getAge().getValue();
                            // 设置字体的框
                            Rect rect = new Rect(rectLeft, rectTop, rectRight, rectBottom);
                            paint.getTextBounds(str, 0, str.length(), rect);
                            paint.setTextSize(face.getFace_rectangle().getHeight() / 4);

                            paint.setTextAlign(Paint.Align.CENTER);
                            paint.setColor(Color.WHITE);
                            canvas.drawText(str, rectCenterX, rectCenterY + rect.height() / 3 * 2, paint);

                        }

                        displayPicImg.post(new Runnable() {
                            @Override
                            public void run() {
                                displayPicImg.postInvalidate();
                            }
                        });
                    }

                    @Override
                    public void onFailed(String s) {
                        Toast.makeText(DetectActivity.this, "检测失败！", Toast.LENGTH_SHORT).show();
                    }
                });

                break;
            case R.id.btn_gallery:
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PHOTO_REQUEST_CODE);
                break;
            case R.id.btn_save:
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    Toast.makeText(this, "没有SD卡，无法保存", Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    Toast.makeText(this, "查询到SD卡", Toast.LENGTH_SHORT).show();
                }
                File picFileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                        "faceApp");
                if (!picFileDir.exists()) {
                    picFileDir.mkdir();
                    Toast.makeText(this, "文件未建立，进行建立", Toast.LENGTH_SHORT).show();
                }
                File picFile = new File(picFileDir, newPicName());
                BufferedOutputStream bos = null;
                try {
                    bos = new BufferedOutputStream(new FileOutputStream(picFile));
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    bos.flush();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (bos != null) {
                            bos.close();
                            // 通知系统扫描该文件，之后才能在相册中看到该文件
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(picFile)));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                break;
        }
    }

    private String newPicName() {
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyyMMddHHmmsssss");
        String newName = simpleDateFormat.format(new Date())  + ".jpeg";
        return newName;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                Cursor cursor = getContentResolver().
                        query(uri, null, null, null, null);
                cursor.moveToNext();
                imgPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true; // 只读取图片，不加载到内存中
                BitmapFactory.decodeFile(imgPath, options);
                // 获取原图大小
                int outHeight = options.outHeight;
                int outWidth = options.outWidth;
                // 获取ImageView控件大小
                int picImgHeight = displayPicImg.getHeight();
                int picImgWidth = displayPicImg.getWidth();
                // 计算比例
                int ratioHeight = outHeight / picImgHeight;
                int ratioWidth = outWidth / picImgWidth;
                int ratio = Math.max(ratioHeight, ratioWidth);
                // 根据比例压缩图片
                options.inJustDecodeBounds = false; // 加载到内存中
                options.inSampleSize = ratio;
                options.inMutable = true; // 设置bitmap是可修改、可变的
                // 加载压缩后的图片
                bitmap = BitmapFactory.decodeFile(imgPath, options);
                // 显示图片
                displayPicImg.setImageBitmap(bitmap);
            }
        }
    }
    public static byte[] toByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        return baos.toByteArray();
    }

}
