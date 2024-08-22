package com.example.myapplication;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class ResultsMapActivity extends AppCompatActivity {
    private Marker marker;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 123;

    private MyContentProvider contentProvider;
    private MapView mapView;
    private double latitude;
    private double longitude;

    private MapHelper mph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results_map);

        // Αρχικοποίηση της βιβλιοθήκης osmdroid
        Configuration.getInstance().load(getApplicationContext(), getPreferences(MODE_PRIVATE));
        int session = 0;  // Μεταβλητή για την παρακολούθηση της τρέχουσας συνεδρίας
        contentProvider = new MyContentProvider();

        // Εύρεση και αρχικοποίηση του MapView
        mapView = findViewById(R.id.map2);
        mapView.setTileSource(TileSourceFactory.MAPNIK); // Ορισμός του χάρτη στη μορφή Mapnik
        marker = new Marker(mapView);  // Δημιουργία ενός marker για τον χάρτη
        mph = new MapHelper(mapView, this);  // Δημιουργία του βοηθού χάρτη

        // Ρύθμιση του ελεγκτή του χάρτη
        IMapController mapController = mapView.getController();
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        mapController.setZoom(16.0);  // Ορισμός του επιπέδου ζουμ στον χάρτη
        mapController.setCenter(new GeoPoint(37.9838, 23.7275));  // Ορισμός του κέντρου του χάρτη (Αθήνα)

        // Προσθήκη επικάλυψης για την εμφάνιση της τρέχουσας τοποθεσίας
        MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(mapView);
        mapView.getOverlays().add(myLocationOverlay);

        // Έλεγχος αν η άδεια για πρόσβαση στη θέση έχει παραχωρηθεί
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Αν η άδεια έχει παραχωρηθεί, ζήτηση ενημερώσεων τοποθεσίας
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    mph.clearMap();  // Καθαρισμός του χάρτη από προηγούμενες επικαλύψεις

                    // Ενημέρωση της τοποθεσίας
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    GeoPoint updatedLocation = new GeoPoint(latitude, longitude);

                    // Ρύθμιση του κέντρου του χάρτη στη νέα τοποθεσία
                    mapController.setCenter(updatedLocation);

                    // Προσθήκη marker στην νέα τοποθεσία
                    mph.addMarker(updatedLocation);

                    // Εμφάνιση των επισημειωμένων περιοχών από το Content Provider
                    mph.showMarkedRegions(session);
                }
            });
        } else {
            // Αν δεν έχει παραχωρηθεί η άδεια, ζήτηση της άδειας από τον χρήστη
            ActivityCompat.requestPermissions(ResultsMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        }

        // Κουμπί για την παύση ή επανεκκίνηση της υπηρεσίας τοποθεσίας
        Button pauseResumeButton = findViewById(R.id.pauseResumeButton);
        pauseResumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LocationService.service_on) {
                    // Αν η υπηρεσία είναι ενεργή, τερματισμός της
                    stopService(new Intent(ResultsMapActivity.this, LocationService.class));
                    LocationService.service_on = false;
                    Toast.makeText(getApplicationContext(), "Location service is now deactivated.", Toast.LENGTH_SHORT).show();
                } else {
                    // Αν η υπηρεσία είναι ανενεργή, εκκίνηση της
                    startService(new Intent(ResultsMapActivity.this, LocationService.class));
                    LocationService.service_on = true;
                    Toast.makeText(getApplicationContext(), "Location service is now activated.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Κουμπί για την επιστροφή στο MainActivity
        Button returnButton = findViewById(R.id.returnButton);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Εκκίνηση του MainActivity
                Intent intent = new Intent(ResultsMapActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}

