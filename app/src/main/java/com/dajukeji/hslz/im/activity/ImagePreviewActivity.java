package com.dajukeji.hslz.im.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dajukeji.hslz.R;

import java.io.File;
import java.io.IOException;

public class ImagePreviewActivity extends BaseActivity {

    private String path;
    private CheckBox isOri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        path = getIntent().getStringExtra("path");
        isOri = (CheckBox) findViewById(R.id.isOri);

        initToolBar();

        showImage();
    }

    private void initToolBar(){
        Toolbar mToolBar = (Toolbar) findViewById(R.id.image_preview_toolBar);
        TextView mTvSend = (TextView) findViewById(R.id.image_preview_send);

        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//取消默认的app title
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTvSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("path", path);
                intent.putExtra("isOri", isOri.isChecked());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void showImage(){
        if (path.equals("")) return;
        File file = new File(path);
        if (file.exists()){
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            if (file.length() == 0 && options.outWidth == 0) {
                finish();
                return;
            }
            long fileLength = file.length();
            if (fileLength == 0) {
                fileLength = options.outWidth*options.outHeight/3;
            }
            int reqWidth, reqHeight, width=options.outWidth, height=options.outHeight;
            if (width > height){
                reqWidth = getWindowManager().getDefaultDisplay().getWidth();
                reqHeight = (reqWidth * height)/width;
            }else{
                reqHeight = getWindowManager().getDefaultDisplay().getHeight();
                reqWidth = (width * reqHeight)/height;
            }
            int inSampleSize = 1;
            if (height > reqHeight || width > reqWidth) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }
            isOri.setText(getString(R.string.chat_image_preview_ori) + "(" + getFileSize(fileLength) + ")");
            try{
                options.inSampleSize = inSampleSize;
                options.inJustDecodeBounds = false;
                float scaleX = (float) reqWidth / (float) (width/inSampleSize);
                float scaleY = (float) reqHeight / (float) (height/inSampleSize);
                Matrix mat = new Matrix();
                mat.postScale(scaleX, scaleY);
                Bitmap bitmap = BitmapFactory.decodeFile(path, options);
                ExifInterface ei =  new ExifInterface(path);
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                switch(orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        mat.postRotate(90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        mat.postRotate(180);
                        break;
                }
                ImageView imageView = (ImageView) findViewById(R.id.image);
                imageView.setImageBitmap(Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true));
            }catch (IOException e){
                Toast.makeText(this, getString(R.string.chat_image_preview_load_err), Toast.LENGTH_SHORT).show();
            }
        }else{
            finish();
        }
    }

    private String getFileSize(long size){
        StringBuilder strSize = new StringBuilder();
        if (size < 1024){
            strSize.append(size).append("B");
        }else if (size < 1024*1024){
            strSize.append(size/1024).append("K");
        }else{
            strSize.append(size/1024/1024).append("M");
        }
        return strSize.toString();
    }


}
