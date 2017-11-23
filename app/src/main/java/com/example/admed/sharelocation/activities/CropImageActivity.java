package com.example.admed.sharelocation.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.admed.sharelocation.R;
import com.example.admed.sharelocation.singletons.FotoSingleton;
import com.github.clans.fab.FloatingActionButton;
import com.isseiaoki.simplecropview.CropImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;

public class CropImageActivity extends AppCompatActivity {

    private CropImageView mCropView;
    private Button btnCortar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);


        instanciarComponentes();
    }

    private void instanciarComponentes() {
        mCropView = findViewById(R.id.cropImageView);
        mCropView.setCropMode(CropImageView.CropMode.CIRCLE_SQUARE);
        mCropView.setCompressFormat(Bitmap.CompressFormat.JPEG);
        mCropView.setCompressQuality(100);

        final Uri imagem = Uri.parse(getIntent().getExtras().getString("urlImagem"));

        mCropView.load(imagem).useThumbnail(true).execute(new LoadCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(Throwable e) {

            }
        });

        btnCortar = findViewById(R.id.btnCortar);
        btnCortar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCropView.crop(imagem).execute(new CropCallback() {
                    @Override
                    public void onSuccess(Bitmap cropped) {

                        FotoSingleton.getInstance().foto = cropped;
                        setResult(1);
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
            }
        });
    }
}
