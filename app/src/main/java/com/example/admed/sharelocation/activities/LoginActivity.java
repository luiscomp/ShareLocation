package com.example.admed.sharelocation.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admed.sharelocation.R;
import com.example.admed.sharelocation.dialogs.ProgressDialog;
import com.example.admed.sharelocation.objetos.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etSenha;
    private Button btnLogin;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        instanciarComponentes();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            chamarTelaMapa();
        }
    }

    private void chamarTelaMapa() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
        finish();
    }

    private void instanciarComponentes() {
        etEmail = findViewById(R.id.email);
        etSenha = findViewById(R.id.senha);
        etSenha.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    if(dadosValidos()) {
                        logarUsuario();
                        return true;
                    }
                }
                return false;
            }
        });

        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dadosValidos()) {
                    logarUsuario();
                }
            }
        });
    }

    private void logarUsuario() {
        final ProgressDialog dialog = ProgressDialog.newInstance(false);
        dialog.show(getSupportFragmentManager(), "dialogLogin");

        mAuth.signInWithEmailAndPassword(etEmail.getText().toString(), etSenha.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            chamarTelaMapa();
                            dialog.dismiss();
                        } else {
                            AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
                            dialog.setTitle(getString(R.string.registrar_text))
                                    .setMessage(getString(R.string.usuario_nao_encontrado_text))
                                    .setPositiveButton(getString(R.string.sim_text), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            registrarUsuario();
                                        }
                                    })
                                    .create()
                                    .show();
                        }
                    }
                });
    }

    private void registrarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setEmail(etEmail.getText().toString());
        usuario.setSenha(etSenha.getText().toString());
        usuario.setLatitude(0.0);
        usuario.setLongitude(0.0);

        Intent intent = new Intent(this, RegistrarActivity.class);
        intent.putExtra("usuario", usuario);
        startActivity(intent);
        finish();
    }


    private boolean dadosValidos() {
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

        return true;
    }
}

