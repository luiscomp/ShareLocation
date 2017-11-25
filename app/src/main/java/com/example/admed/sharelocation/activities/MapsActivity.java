package com.example.admed.sharelocation.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.example.admed.sharelocation.R;
import com.example.admed.sharelocation.dialogs.ProgressDialog;
import com.example.admed.sharelocation.objetos.Usuario;
import com.example.admed.sharelocation.utils.Constantes;
import com.example.admed.sharelocation.utils.ImageUtils;
import com.example.admed.sharelocation.utils.Permissoes;
import com.example.admed.sharelocation.utils.PhotoMarker;
import com.example.admed.sharelocation.utils.Util;
import com.example.admed.sharelocation.utils.mapsutil.LatLngInterpolator;
import com.example.admed.sharelocation.utils.mapsutil.MapUtils;
import com.example.admed.sharelocation.utils.mapsutil.MarkerAnimation;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener {

    private FloatingActionButton menuPerfil;
    private FloatingActionButton menuSair;


    private static final int RESULT_LOCALIZACAO_PERMISSION = 1;
    private static String[] permissoesLocalizacao = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    private Marker usuarioMarker;
    private FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
    private Usuario usuario;
    private boolean singOut = false;

    private DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();

    private Map<String, Usuario> usuariosLogados = new HashMap<>();
    private Map<String, Marker> usuariosLogadosMarker = new HashMap<>();

    private ProgressDialog progressDialog;


    private LocationCallback locationCallBack = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                marcarPosicaoNoMapa(location);
            }
        }
    };

    private GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        }
    };

    private GoogleApiClient.ConnectionCallbacks connectionCallBacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            verificarPermissao();
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        instanciarComponentes();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setUpGClient();

        recuperarUsuario();
    }

    private void instanciarComponentes() {
        menuPerfil = findViewById(R.id.menuPerfil);
        menuPerfil.setEnabled(false);
        menuPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chamarActivityPerfilUsuario(usuario);
            }
        });


        menuSair = findViewById(R.id.menuSair);
        menuSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MapsActivity.this)
                        .setTitle(getString(R.string.sair_text))
                        .setMessage(getString(R.string.fazer_logout_text))
                        .setPositiveButton(getString(R.string.sim_text), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressDialog = new ProgressDialog();
                                progressDialog.show(getSupportFragmentManager(), "progressDialog");

                                singOut = true;
                                dialog.dismiss();
                                atualizarUsuarioParaOffline();
                            }
                        })
                        .setNegativeButton(getString(R.string.nao_text), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
            }
        });
    }

    private void chamarActivityPerfilUsuario(Usuario usuario) {
        Intent intent = new Intent(MapsActivity.this, PerfilActivity.class);
        intent.putExtra("usuario", usuario);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adicionarListenerDeAtualizacao();
    }

    private void adicionarListenerDeAtualizacao() {
        dataBaseReference.getRoot().child("usuarios").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                atualizarPosicaoUsuarioMapa(dataSnapshot.getValue(Usuario.class));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                atualizarPosicaoUsuarioMapa(dataSnapshot.getValue(Usuario.class));
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

    private void atualizarPosicaoUsuarioMapa(final Usuario usuario) {
        if (!usuario.getId().equals(fbUser.getUid())) {
            if (usuario.getLatitude() != null && usuario.getLongitude() != null) {
                if (usuariosLogados.get(usuario.getId()) != null) {
                    if(usuario.getOnline()) {
                        if (usuariosLogadosMarker.get(usuario.getId()) == null) {
                            Marker markerUsuario;
                            final LatLng localizacaoUsuario = new LatLng(usuario.getLatitude(), usuario.getLongitude());

                            MarkerOptions markerOptions = new MarkerOptions().position(localizacaoUsuario);
                            markerUsuario = mMap.addMarker(markerOptions);
                            mMap.setIndoorEnabled(true);

                            PhotoMarker marker = new PhotoMarker();
                            marker.setUsuario(usuario);
                            marker.setMarker(markerUsuario);
                            marker.setUri(Uri.parse(usuario.getPhoto()));

                            usuario.setImgPerfil(new ImageUtils(this).setFileName(usuario.getId()).setDirectoryName(Constantes.DIRECTORY_PHOTOS).load());
                            if(usuariosLogados.get(usuario.getId()).getImgPerfil() == null) {
                                markerUsuario.setIcon(BitmapDescriptorFactory.fromBitmap(Util.getMarkerBitmapFromView(MapsActivity.this, R.drawable.ic_launcher_background, null)));
                                new SetarFotoMarkerTask().execute(marker);
                            } else {
                                markerUsuario.setIcon(BitmapDescriptorFactory.fromBitmap(Util.getMarkerBitmapFromView(MapsActivity.this, R.drawable.ic_launcher_background, usuariosLogados.get(usuario.getId()).getImgPerfil())));
                            }

                            usuariosLogadosMarker.put(usuario.getId(), markerUsuario);

                            mostrarUsuariosLogados();
                        } else {
                            Marker markerUsuario;
                            final LatLng localizacaoUsuario = new LatLng(usuario.getLatitude(), usuario.getLongitude());

                            markerUsuario = usuariosLogadosMarker.get(usuario.getId());

                            PhotoMarker marker = new PhotoMarker();
                            marker.setUsuario(usuario);
                            marker.setMarker(markerUsuario);
                            marker.setUri(Uri.parse(usuario.getPhoto()));

                            usuario.setImgPerfil(new ImageUtils(this).setFileName(usuario.getId()).setDirectoryName(Constantes.DIRECTORY_PHOTOS).load());
                            if(usuariosLogados.get(usuario.getId()).getImgPerfil() == null) {
                                markerUsuario.setIcon(BitmapDescriptorFactory.fromBitmap(Util.getMarkerBitmapFromView(MapsActivity.this, R.drawable.ic_launcher_background, null)));
                                new SetarFotoMarkerTask().execute(marker);
                            } else {
                                markerUsuario.setIcon(BitmapDescriptorFactory.fromBitmap(Util.getMarkerBitmapFromView(MapsActivity.this, R.drawable.ic_launcher_background, usuariosLogados.get(usuario.getId()).getImgPerfil())));
                            }

                            new MarkerAnimation().animateMarkerToGB(markerUsuario, localizacaoUsuario, new LatLngInterpolator.Linear());
                        }
                    } else {
                        usuariosLogados.remove(usuario.getId());
                        usuariosLogadosMarker.get(usuario.getId()).remove();
                    }
                } else {
                    if(usuario.getOnline()) {
                        Marker markerUsuario;
                        LatLng localizacaoUsuario = new LatLng(usuario.getLatitude(), usuario.getLongitude());

                        MarkerOptions markerOptions = new MarkerOptions().position(localizacaoUsuario);
                        markerUsuario = mMap.addMarker(markerOptions);
                        mMap.setIndoorEnabled(true);

                        PhotoMarker marker = new PhotoMarker();
                        marker.setUsuario(usuario);
                        marker.setMarker(markerUsuario);
                        marker.setUri(Uri.parse(usuario.getPhoto()));

                        usuario.setImgPerfil(new ImageUtils(this).setFileName(usuario.getId()).setDirectoryName(Constantes.DIRECTORY_PHOTOS).load());
                        markerUsuario.setIcon(BitmapDescriptorFactory.fromBitmap(Util.getMarkerBitmapFromView(MapsActivity.this, R.drawable.ic_launcher_background, usuario.getImgPerfil())));
                        new SetarFotoMarkerTask().execute(marker);

                        usuariosLogados.put(usuario.getId(), usuario);
                        usuariosLogadosMarker.put(usuario.getId(), markerUsuario);

                        mostrarUsuariosLogados();
                    }
                }
            }
        }
    }

    private void mostrarUsuariosLogados() {
        Query query = dataBaseReference.getRoot().child("usuarios").orderByChild("online").equalTo(Boolean.TRUE);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LatLngBounds.Builder pointsBuilder = null;

                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    Usuario usuario = data.getValue(Usuario.class);

                    LatLng localizacaoUsuario  = new LatLng(usuario.getLatitude(), usuario.getLongitude());

                    if(pointsBuilder == null) {
                        pointsBuilder = new LatLngBounds.Builder();
                    }
                    pointsBuilder.include(localizacaoUsuario);
                }

                if(pointsBuilder != null) {
                    MapUtils.aplicarZoomEntreVariasLocalizacoes(mMap, pointsBuilder, MapsActivity.this);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void recuperarUsuario() {
        Query query = dataBaseReference.getRoot().child("usuarios").orderByChild("id").equalTo(fbUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    usuario = data.getValue(Usuario.class);
                    if (usuarioMarker != null) {
                        PhotoMarker marker = new PhotoMarker();
                        marker.setUsuario(usuario);
                        marker.setMarker(usuarioMarker);
                        marker.setUri(Uri.parse(usuario.getPhoto()));

                        new SetarFotoMarkerTask().execute(marker);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void verificarPermissao() {
        if (Permissoes.isGranted(this, Manifest.permission.ACCESS_FINE_LOCATION) && Permissoes.isGranted(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            capturarPosicaoCelular();
        } else {
            ActivityCompat.requestPermissions(this, permissoesLocalizacao, RESULT_LOCALIZACAO_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RESULT_LOCALIZACAO_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (Permissoes.isGranted(this, Manifest.permission.ACCESS_FINE_LOCATION) && Permissoes.isGranted(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        capturarPosicaoCelular();
                    }
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        atualizarUsuarioParaOffline();
        mFusedLocationProviderClient.removeLocationUpdates(locationCallBack);
        super.onDestroy();
    }

    private void atualizarUsuarioParaOffline() {
        final Map<String, Object> online = new HashMap<>();
        online.put("online", Boolean.FALSE);

        Query query = dataBaseReference.getRoot().child("usuarios").orderByChild("id").equalTo(fbUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().child(fbUser.getUid()).updateChildren(online).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            if(singOut) {
                                FirebaseAuth.getInstance().signOut();

                                progressDialog.dismiss();

                                Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private synchronized void setUpGClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, connectionFailedListener)
                .addConnectionCallbacks(connectionCallBacks)
                .addOnConnectionFailedListener(connectionFailedListener)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        marcarPosicaoNoMapa(location);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        for(String chave: usuariosLogadosMarker.keySet()) {
            if(marker.equals(usuariosLogadosMarker.get(chave))) {
                chamarActivityPerfilUsuario(usuariosLogados.get(chave));
                return true;
            }
        }
        return false;
    }

    private void marcarPosicaoNoMapa(Location location) {
        final LatLng localizacaoAtual;
        localizacaoAtual = new LatLng(location.getLatitude(), location.getLongitude());

        if(usuarioMarker == null) {
            Bitmap imgPerfil = new ImageUtils(this).setFileName(fbUser.getUid()).setDirectoryName(Constantes.DIRECTORY_PHOTOS).load();

            MarkerOptions markerOptions;
            markerOptions = new MarkerOptions()
                    .position(localizacaoAtual)
                    .icon(BitmapDescriptorFactory.fromBitmap(Util.getMarkerBitmapFromView(MapsActivity.this, R.drawable.ic_launcher_background, imgPerfil)));

            usuarioMarker = mMap.addMarker(markerOptions);
            mMap.setIndoorEnabled(true);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(localizacaoAtual, 14.0f));
        } else {
            if(usuario != null && usuario.getImgPerfil() == null) {
                PhotoMarker marker = new PhotoMarker();
                marker.setMarker(usuarioMarker);
                marker.setUri(Uri.parse(usuario.getPhoto()));

                new SetarFotoMarkerTask().execute(marker);
            }

            new MarkerAnimation().animateMarkerToGB(usuarioMarker, localizacaoAtual, new LatLngInterpolator.Linear());
        }

        atualizarPosicaoFireBase(localizacaoAtual);
    }

    private void atualizarPosicaoFireBase(final LatLng localizacaoAtual) {
        final Map<String, Object> localizacao = new HashMap<>();
        localizacao.put("latitude", localizacaoAtual.latitude);
        localizacao.put("longitude", localizacaoAtual.longitude);
        localizacao.put("online", Boolean.TRUE);

        Query query = dataBaseReference.getRoot().child("usuarios").orderByChild("id").equalTo(fbUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().child(fbUser.getUid()).updateChildren(localizacao);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case LocationSettingsRequest.CONTENTS_FILE_DESCRIPTOR:
                if(resultCode != LocationSettingsResult.CONTENTS_FILE_DESCRIPTOR) {
                    capturarPosicaoCelular();
                }
                break;
        }
    }

    private void capturarPosicaoCelular() {
        if(googleApiClient != null && googleApiClient.isConnected()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationRequest = new LocationRequest();
                locationRequest.setInterval(3000);
                locationRequest.setFastestInterval(3000);
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
                builder.setAlwaysShow(true);

                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);

                PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
                result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(LocationSettingsResult result) {
                        final Status status = result.getStatus();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.SUCCESS:
                                if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
                                        @Override
                                        public void onSuccess(Location location) {
                                            marcarPosicaoNoMapa(location);
                                        }
                                    });
                                }
                                break;
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    status.startResolutionForResult(MapsActivity.this, RESULT_LOCALIZACAO_PERMISSION);
                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                                break;
                        }
                    }
                });
            }
        }
    }

    private class SetarFotoMarkerTask extends AsyncTask<PhotoMarker, Void, Void> {
        @Override
        protected Void doInBackground(final PhotoMarker... photoMarkers) {
            final Bitmap bmp;
            bmp = getBitmapFromURL(photoMarkers[0].getUri().toString());

            if(photoMarkers[0].getUsuario() != null) {
                photoMarkers[0].getUsuario().setImgPerfil(bmp);
                salvarImagemNaMemoriaInterna(photoMarkers[0].getUsuario(), bmp);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        photoMarkers[0].getMarker().setIcon(BitmapDescriptorFactory.fromBitmap(Util.getMarkerBitmapFromView(MapsActivity.this, R.drawable.ic_launcher_background, bmp)));
                        menuPerfil.setEnabled(true);
                    } catch (IllegalArgumentException e) {
                        // Ignorar erro, pois ele só ocorre quando algum marcador saiu do mapa e havia uma task para atualizar sua imagem. Ao tentar setar a nova imagem ele já não existe.
                    }
                }
            });
            return null;
        }

        private void salvarImagemNaMemoriaInterna(Usuario usuario, Bitmap bitmap) {
            new ImageUtils(MapsActivity.this).setFileName(usuario.getId()).setDirectoryName(Constantes.DIRECTORY_PHOTOS).save(bitmap);
        }

        private Bitmap getBitmapFromURL(String src) {
            if(src.trim() != ""){
                try {
                    URL url = new URL(src);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
                    return myBitmap;
                } catch (IOException e) {
                    return null;
                }
            }
            return null;
        }
    }
}
