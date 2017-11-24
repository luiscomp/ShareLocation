package com.example.admed.sharelocation.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.admed.sharelocation.R;
import com.example.admed.sharelocation.dialogs.ProgressDialog;
import com.example.admed.sharelocation.objetos.Usuario;
import com.example.admed.sharelocation.singletons.FotoSingleton;
import com.example.admed.sharelocation.utils.Criptografia;
import com.example.admed.sharelocation.utils.Permissoes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.TimeZone;

public class RegistrarActivity extends AppCompatActivity {

    private String[] permissoesGaleria = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int REQUEST_PERMISSION_GALERY = 1;
    private static final int REQUEST_CROPED_IMAGE = 2;
    private static final int REQUEST_SELECT_PHOTO = 3;

    private ImageView imageView;
    private Bitmap croppedBitmap;

    private Usuario usuario = new Usuario();

    private EditText etNome;
    private EditText etEmail;
    private EditText etSenha;
    private EditText etConfirmarSenha;
    private Button btnRegistrar;

    private StorageReference storage;
    private FirebaseAuth mAuth;
    private DatabaseReference usuariosReference = FirebaseDatabase.getInstance().getReference("usuarios");
    private ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance().getReference();

        instanciarComponentes();
        setarValores();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(RegistrarActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Intent intent = new Intent(RegistrarActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void setarValores() {
        Usuario usuario = getIntent().getExtras().getParcelable("usuario");

        etEmail.setText(usuario.getEmail());
        etSenha.setText(usuario.getSenha());
        etNome.requestFocus();
    }

    private void instanciarComponentes() {
        etNome = findViewById(R.id.nome);
        etEmail = findViewById(R.id.email);
        etSenha = findViewById(R.id.senha);
        etConfirmarSenha = findViewById(R.id.confirmarSenha);

        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dadosValidos()) {
                    registrarNovoUsuario();
                }
            }
        });

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selecionarFotoDaGaleria();
            }
        });
    }


    private void registrarNovoUsuario() {
        progressDialog = ProgressDialog.newInstance(false);
        progressDialog.show(getSupportFragmentManager(), "dialogLogin");

        mAuth.fetchProvidersForEmail(etEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                if(task.isSuccessful() && task.getResult().getProviders().size() == 0 ) {
                    if(croppedBitmap != null){
                        uploadFirebase(croppedBitmap);
                    }
                } else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(RegistrarActivity.this);
                    dialog.setTitle(getString(R.string.ops_text))
                            .setMessage(getString(R.string.email_ja_cadastrado_text))
                            .setPositiveButton(getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .create()
                            .show();

                    progressDialog.dismiss();
                }
            }
        });
    }

    private void concluirRegistroNovoUsuario() {
        mAuth.createUserWithEmailAndPassword(etEmail.getText().toString(), etSenha.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            usuario.setId(user.getUid());
                            usuario.setNome(etNome.getText().toString());
                            usuario.setEmail(etEmail.getText().toString());
                            usuario.setSenha(etSenha.getText().toString());

                            usuariosReference.child(usuario.getId()).setValue(usuario);

                            chamarTelaMapa();
                        } else {
                            Toast.makeText(RegistrarActivity.this, getString(R.string.nao_e_possivel_registrar_usuario), Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }

    private void chamarTelaMapa() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean dadosValidos() {
        if(etNome.getText().length() == 0) {
            etNome.setError(getString(R.string.nome_invalido_text));
            etNome.requestFocus();
            return false;
        }

        if(!etEmail.getText().toString().contains("@")) {
            etEmail.setError(getString(R.string.email_invalido_text));
            etEmail.requestFocus();
            return false;
        }

        if(etSenha.getText().length() < 6) {
            etSenha.setError(getString(R.string.senha_invalida_text));
            etSenha.requestFocus();
            return false;
        }

        if(etConfirmarSenha.getText().length() < 6) {
            etConfirmarSenha.setError(getString(R.string.senha_invalida_text));
            etConfirmarSenha.requestFocus();
            return false;
        }

        if(!etSenha.getText().toString().equals(etConfirmarSenha.getText().toString())) {
            etConfirmarSenha.setError(getString(R.string.confirmar_senha_invalida_text));
            etConfirmarSenha.requestFocus();
            return false;
        }

        if(croppedBitmap == null) {
            Toast.makeText(this, "Favor, escolha uma foto de perfil.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_PHOTO) {
            if (data != null) {
                Uri contentURI = data.getData();

                Intent intent = new Intent(this, CropImageActivity.class);
                intent.putExtra("urlImagem", contentURI.toString());
                startActivityForResult(intent, REQUEST_CROPED_IMAGE);
            }
        } else if(requestCode == REQUEST_CROPED_IMAGE) {
            if(resultCode == 1) {
                croppedBitmap = FotoSingleton.getInstance().foto;
                imageView.setImageBitmap(FotoSingleton.getInstance().foto);
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

    public void selecionarFotoDaGaleria() {
        if(Permissoes.isGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) && Permissoes.isGranted(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, REQUEST_SELECT_PHOTO);
        } else {
            ActivityCompat.requestPermissions(this, permissoesGaleria, REQUEST_PERMISSION_GALERY);
        }
    }

    private void uploadFirebase (Bitmap bitmap){
        bitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storage.child("imagens/"+ Criptografia.criptografarMD5(String.valueOf(Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis()))+".jpg").putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(RegistrarActivity.this, "Falha ao Cadastrar, tente novamente!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                usuario.setPhoto(taskSnapshot.getDownloadUrl().toString());
                concluirRegistroNovoUsuario();
            }
        });
    }
}
