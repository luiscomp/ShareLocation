package com.example.admed.sharelocation.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.admed.sharelocation.R;
import com.example.admed.sharelocation.dialogs.ProgressDialog;
import com.example.admed.sharelocation.objetos.Usuario;
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
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RegistrarActivity extends AppCompatActivity {

    private static final int SELECT_PHOTO = 3;
    ImageView imageView;
    Bitmap croppedBitmap;
    private EditText etNome;
    private EditText etEmail;
    private EditText etSenha;
    private EditText etConfirmarSenha;
    private Button btnRegistrar;
    private FirebaseAuth mAuth;
    private DatabaseReference usuariosReference = FirebaseDatabase.getInstance().getReference("usuarios");
    private ProgressDialog progressDialog;
    private Uri downloadUrl;
    private Uri contentURI;
    private StorageReference storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance().getReference();

        instanciarComponentes();
        setarValores();
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
                choosePhotoFromGallary();
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
                    if(croppedBitmap!=null){
                        uploadFirebase(croppedBitmap);
                    }else{
                        concluirRegistroNovoUsuario();
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

                            Usuario usuario = new Usuario();
                            usuario.setId(user.getUid());
                            usuario.setNome(etNome.getText().toString());
                            usuario.setEmail(etEmail.getText().toString());
                            usuario.setSenha(etSenha.getText().toString());
                            usuario.setPhoto((downloadUrl!=null) ? downloadUrl.toString() : "");


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

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_PHOTO) {
            if (data != null) {
                contentURI = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    Matrix matrix = new Matrix();
                    matrix.postScale(0.9f, 0.9f);
                    croppedBitmap = Bitmap.createBitmap(bitmap, 300, 300,300, 300, matrix, true);
                    imageView.setImageBitmap(croppedBitmap);
                    //uploadFirebase();
                    //cropImage(contentURI);
                } catch (IOException e) {
                    //Log.d("FIREBASE",e.getMessage());
                    Toast.makeText(RegistrarActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void choosePhotoFromGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, SELECT_PHOTO);
    }

    private void uploadFirebase (Bitmap bitmap){
        String s = contentURI.getPath().toString();
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        m.update(s.getBytes(),0,s.length());
        StorageReference riversRef = storage.child("images/"+new BigInteger(1,m.digest()).toString(16)+".jpg");
        //riversRef.putFile(contentURI);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        UploadTask uploadTask = riversRef.putBytes(byteArray);

        uploadTask .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(RegistrarActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                concluirRegistroNovoUsuario();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Toast.makeText(RegistrarActivity.this, "Upload success!", Toast.LENGTH_SHORT).show();
                downloadUrl = taskSnapshot.getDownloadUrl();
                concluirRegistroNovoUsuario();
            }
        });
    }
}
