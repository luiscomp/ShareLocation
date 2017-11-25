package com.example.admed.sharelocation.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.example.admed.sharelocation.R;
import com.example.admed.sharelocation.objetos.Usuario;
import com.example.admed.sharelocation.utils.Constantes;
import com.example.admed.sharelocation.utils.ImageUtils;
import com.example.admed.sharelocation.utils.Util;
import com.example.admed.sharelocation.utils.mapsutil.LatLngInterpolator;
import com.example.admed.sharelocation.utils.mapsutil.MarkerAnimation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.muzakki.ahmad.widget.CollapsingToolbarLayout;

public class PerfilActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private Usuario usuario;
    private Marker usuarioMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        usuario = getIntent().getParcelableExtra("usuario");

        ((CollapsingToolbarLayout) findViewById(R.id.toolbar_layout)).setTitle(usuario.getNome());
        ((CollapsingToolbarLayout) findViewById(R.id.toolbar_layout)).setSubtitle(usuario.getOnline() ? getString(R.string.online_text) : getString(R.string.offline_text));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Não deu tempo de fazer o chat... D:", Snackbar.LENGTH_LONG)
                        .setAction("Considerar", null).show();
            }
        });
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
    protected void onStart() {
        super.onStart();
        instanciarListenerAtualizacaoUsuarioFirebase();
    }

    private void instanciarListenerAtualizacaoUsuarioFirebase() {
        FirebaseDatabase.getInstance().getReference("usuarios").orderByChild("id").equalTo(usuario.getId()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                usuario.setImgPerfil(PerfilActivity.this.usuario.getImgPerfil());
                marcarLocalizacaoNoMapa(usuario);

                ((CollapsingToolbarLayout) findViewById(R.id.toolbar_layout)).setSubtitle(usuario.getOnline() ? getString(R.string.online_text) : getString(R.string.offline_text));
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        marcarLocalizacaoNoMapa(usuario);
    }

    private void marcarLocalizacaoNoMapa(Usuario usuario) {
        final LatLng localizacaoAtual;
        localizacaoAtual = new LatLng(usuario.getLatitude(), usuario.getLongitude());

        if(usuarioMarker == null) {
            usuario.setImgPerfil(new ImageUtils(this).setFileName(usuario.getId()).setDirectoryName(Constantes.DIRECTORY_PHOTOS).load());

            MarkerOptions markerOptions;
            markerOptions = new MarkerOptions().position(localizacaoAtual).icon(BitmapDescriptorFactory.fromBitmap(Util.getMarkerBitmapFromView(PerfilActivity.this, R.drawable.ic_launcher_background, usuario.getImgPerfil())));

            usuarioMarker = mMap.addMarker(markerOptions);
            mMap.setIndoorEnabled(true);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(localizacaoAtual, 16f, 0, 0)));
        } else {
            new MarkerAnimation().animateMarkerToGB(usuarioMarker, localizacaoAtual, new LatLngInterpolator.Linear());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(localizacaoAtual, 16.0f));
        }
    }
}
