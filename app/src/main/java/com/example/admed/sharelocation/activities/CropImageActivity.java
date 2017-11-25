package com.example.admed.sharelocation.activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.admed.sharelocation.R;
import com.example.admed.sharelocation.singletons.FotoSingleton;
import com.example.admed.sharelocation.utils.Permissoes;
import com.isseiaoki.simplecropview.CropImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;

public class CropImageActivity extends AppCompatActivity {

    private CropImageView mCropView;
    private ImageView imgConcluir;
    private ImageView imgGaleria;
    private ImageView imgRotacionarEsquerda;
    private ImageView imgRotacionarDireita;

    private String[] permissoesGaleria = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int REQUEST_PERMISSION_GALERY = 1;
    private static final int REQUEST_SELECT_PHOTO = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        instanciarComponentes();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void instanciarComponentes() {
        mCropView = findViewById(R.id.cropImageView);
        mCropView.setCropMode(CropImageView.CropMode.CIRCLE_SQUARE);
        mCropView.setOutputMaxSize(265, 265);
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

        imgGaleria = findViewById(R.id.galeria);
        imgGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selecionarFotoDaGaleria();
            }
        });

        imgRotacionarEsquerda = findViewById(R.id.rodarEsqueda);
        imgRotacionarEsquerda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCropView.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D);
            }
        });

        imgRotacionarDireita = findViewById(R.id.rodarDireita);
        imgRotacionarDireita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCropView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);
            }
        });

        imgConcluir = findViewById(R.id.concluir);
        imgConcluir.setOnClickListener(new View.OnClickListener() {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_PHOTO) {
            if (data != null) {
                Uri contentURI = data.getData();

                mCropView.load(contentURI).useThumbnail(true).execute(new LoadCallback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_GALERY) {
            if(Permissoes.isGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) && Permissoes.isGranted(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                selecionarFotoDaGaleria();
            }
        }
    }

    private void selecionarFotoDaGaleria() {
        if(Permissoes.isGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) && Permissoes.isGranted(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, REQUEST_SELECT_PHOTO);
        } else {
            ActivityCompat.requestPermissions(this, permissoesGaleria, REQUEST_PERMISSION_GALERY);
        }
    }
}
