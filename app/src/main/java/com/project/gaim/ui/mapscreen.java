package com.project.gaim.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.project.gaim.R;

public class mapscreen extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    double[] DD_pos;
    double[] TruePos = {37.4505635346794, 126.655491420141};
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        DD_pos = intent.getExtras().getDoubleArray("DD_pos");

    }


    @Override
    public void onMapReady(GoogleMap googleMap){
        mMap = googleMap;

        // double inha_lon = 126.6530444;
        // double inha_lat = 37.44865000;
        LatLng truepos_gd = new LatLng(TruePos[0], TruePos[1]);
        // LatLng inhauniv = new LatLng(inha_lat, inha_lon);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(truepos_gd, 17));
        mMap.addMarker(new MarkerOptions().position(truepos_gd));
        LatLng estm = new LatLng(DD_pos[0], DD_pos[1]);


        BitmapDrawable bitmapdraw; Bitmap b, smallMarker;
        bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.mapingicon);
        b=bitmapdraw.getBitmap();
        smallMarker = Bitmap.createScaledBitmap(b, 115, 150, false);
        mMap.addMarker(new MarkerOptions().position(estm).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
    }
}
