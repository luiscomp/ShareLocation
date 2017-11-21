package com.example.admed.sharelocation.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.admed.sharelocation.R;
import com.example.admed.sharelocation.objetos.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrarActivity extends AppCompatActivity {

    private EditText etNome;
    private EditText etEmail;
    private EditText etSenha;
    private EditText etConfirmarSenha;
    private Button btnRegistrar;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dataBaseReference = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        mAuth = FirebaseAuth.getInstance();

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
    }


    private void registrarNovoUsuario() {
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

                            dataBaseReference.child("usuarios").child(usuario.getId()).setValue(usuario);

                            chamarTelaMapa();
                        } else {
                            Toast.makeText(RegistrarActivity.this, "Não foi possível registrar o usuário, tente novamente", Toast.LENGTH_SHORT).show();
                        }
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
}
