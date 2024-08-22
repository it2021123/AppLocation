package com.example.myapplication;




import android.Manifest;
import android.content.ContentResolver;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {
    private ContentResolver resolver; // Διαχειριστής περιεχομένου για πρόσβαση στη βάση δεδομένων
    private Marker marker; // Ο τρέχων marker που προστίθεται στον χάρτη
    private MapHelper hmap; // Βοηθητική κλάση για τη διαχείριση του χάρτη
    private MapView mapView; // Το στοιχείο του χάρτη
    private GeoPoint circleCenter; // Μεταβλητή για το κέντρο του κύκλου
    private int circleRadius = 100; // Ακτίνα του κύκλου σε μέτρα
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 123; // Κωδικός αιτήματος για άδεια πρόσβασης τοποθεσίας
    private ProviderService providerse; // Υπηρεσία για την επικοινωνία με τη βάση δεδομένων
    public double latitude; // Τρέχουσα γεωγραφική θέση (γεωγραφικό πλάτος)
    public double longitude; // Τρέχουσα γεωγραφική θέση (γεωγραφικό μήκος)

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Φόρτωση των ρυθμίσεων του osmdroid
        Configuration.getInstance().load(getApplicationContext(), getPreferences(MODE_PRIVATE));

        // Ορισμός του περιεχομένου της δραστηριότητας
        setContentView(R.layout.activity_map);

        // Αρχικοποίηση του resolver
        resolver = getContentResolver();

        // Αρχικοποίηση του ProviderService με τον ContentResolver και το Context
        providerse = new ProviderService(resolver, this);

        // Ρύθμιση του mapView
        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        marker = new Marker(mapView); // Δημιουργία ενός marker

        hmap = new MapHelper(mapView, this); // Αρχικοποίηση της βοηθητικής κλάσης MapHelper
        IMapController mapController = mapView.getController(); // Λήψη του controller του χάρτη
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE); // Λήψη του LocationManager για πρόσβαση στην τοποθεσία
        mapController.setZoom(16.0); // Ορισμός του επιπέδου zoom στον χάρτη
        mapController.setCenter(new GeoPoint(37.9838, 23.7275)); // Κέντρο του χάρτη στην Αθήνα
        MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(mapView);
        mapView.getOverlays().add(myLocationOverlay); // Προσθήκη overlay για την τοποθεσία

        // Έλεγχος για άδεια πρόσβασης τοποθεσίας
        if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Αν η άδεια έχει δοθεί
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    // Ενημέρωση τοποθεσίας
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    GeoPoint updatedLocation = new GeoPoint(latitude, longitude);

                    // Ρύθμιση του κέντρου του χάρτη στην νέα τοποθεσία
                    mapController.setCenter(new GeoPoint(latitude, longitude));
                    hmap.addMarker(new GeoPoint(latitude, longitude)); // Προσθήκη marker στη νέα τοποθεσία
                }
            });
        } else {
            // Αν δεν έχει δοθεί η άδεια, ζητήστε την από τον χρήστη
            ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        }

        // Ακρόαση γεγονότων του χάρτη
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                hmap.addCircle(p, 100, Color.RED); // Προσθήκη κόκκινου κύκλου με κέντρο στο σημείο p
                providerse.addLocationToProvider(p, MapActivity.this); // Αποθήκευση της τοποθεσίας στη βάση δεδομένων
                circleCenter = p; // Ορισμός της τιμής του κέντρου του κύκλου
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                deleteLocation(p); // Διαγραφή της πλησιέστερης τοποθεσίας στο σημείο p
                return false;
            }
        };

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mReceive);
        mapView.getOverlays().add(0, mapEventsOverlay); // Προσθήκη του overlay των γεγονότων στον χάρτη

        // Ρύθμιση του κουμπιού ακύρωσης
        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                providerse.deleteLocationBySession(MyContentProvider.ses, MapActivity.this); // Διαγραφή τοποθεσιών για την τρέχουσα συνεδρία
                hmap.clearMap(); // Εκκαθάριση του χάρτη
                Intent intent = new Intent(MapActivity.this, MainActivity.class); // Μετάβαση στην κύρια δραστηριότητα
                startActivity(intent);
                Toast.makeText(MapActivity.this, "Finish and Close this session : " + MyContentProvider.ses + "", Toast.LENGTH_SHORT).show(); // Εμφάνιση μηνύματος
            }
        });

        // Ρύθμιση του κουμπιού έναρξης
        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (circleCenter != null) {
                    providerse.saveRegionsToDatabase(circleCenter); // Αποθήκευση της τρέχουσας περιοχής στη βάση δεδομένων
                    Intent intent = new Intent(MapActivity.this, MainActivity.class); // Μετάβαση στην κύρια δραστηριότητα
                    startService(new Intent(MapActivity.this, LocationService.class)); // Εκκίνηση της υπηρεσίας τοποθεσίας
                    startActivity(intent);
                    MyContentProvider.ses += 1; // Αύξηση της τρέχουσας συνεδρίας
                } else {
                    Toast.makeText(MapActivity.this, "Failed to find pointed location", Toast.LENGTH_SHORT).show(); // Εμφάνιση μηνύματος αν δεν υπάρχει τοποθεσία
                }
            }
        });
    }

    // Μέθοδος για τη διαγραφή της πλησιέστερης τοποθεσίας
    private void deleteLocation(GeoPoint p) {
        if (p != null) {
            // Εύρεση και διαγραφή του πλησιέστερου κύκλου από τον χάρτη
            GeoPoint po = hmap.removeNearestCircle(p);
            if (po != null) {
                providerse.deleteLocationByGeoPoint(po, MapActivity.this); // Διαγραφή της τοποθεσίας από τη βάση δεδομένων
            } else {
                Toast.makeText(MapActivity.this, "No nearby location found to delete", Toast.LENGTH_SHORT).show(); // Εμφάνιση μηνύματος αν δεν βρέθηκε τοποθεσία
            }
        } else {
            Toast.makeText(MapActivity.this, "No location point provided", Toast.LENGTH_SHORT).show(); // Εμφάνιση μηνύματος αν δεν δόθηκε τοποθεσία
        }
    }
}
