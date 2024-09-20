package com.example.myapplication;

import static com.example.myapplication.MyContentProvider.CONTENT_URI; // Στατική εισαγωγή του CONTENT_URI από τον πάροχο περιεχομένου
import static com.example.myapplication.MyContentProvider.INSIDE; // Στατική εισαγωγή του INSIDE από τον πάροχο περιεχομένου

import android.Manifest; // Εισαγωγή των απαιτούμενων αδειών
import android.app.Service; // Εισαγωγή της κλάσης Service
import android.content.ContentResolver; // Εισαγωγή της κλάσης ContentResolver
import android.content.ContentValues; // Εισαγωγή της κλάσης ContentValues για την ενημέρωση της βάσης δεδομένων
import android.content.Context; // Εισαγωγή της κλάσης Context
import android.content.Intent; // Εισαγωγή της κλάσης Intent
import android.content.pm.PackageManager; // Εισαγωγή της κλάσης PackageManager
import android.database.Cursor; // Εισαγωγή της κλάσης Cursor για τη διαχείριση των δεδομένων της βάσης δεδομένων
import android.location.Location; // Εισαγωγή της κλάσης Location
import android.location.LocationListener; // Εισαγωγή της κλάσης LocationListener
import android.location.LocationManager; // Εισαγωγή της κλάσης LocationManager
import android.os.Bundle;
import android.os.Handler; // Εισαγωγή της κλάσης Handler για τη διαχείριση των επαναλαμβανόμενων εργασιών
import android.os.IBinder; // Εισαγωγή της κλάσης IBinder
import android.widget.Toast; // Εισαγωγή της κλάσης Toast για εμφάνιση μηνυμάτων

import androidx.annotation.NonNull; // Εισαγωγή των annotation NonNull
import androidx.annotation.Nullable; // Εισαγωγή των annotation Nullable
import androidx.core.content.ContextCompat; // Εισαγωγή της κλάσης ContextCompat για έλεγχο αδειών
public class LocationService extends Service {
    private ContentResolver resolver; // Δήλωση του ContentResolver για διαχείριση δεδομένων
    private LocationManager lm; // Δήλωση του LocationManager για διαχείριση τοποθεσίας
    private static final int LOCATION_UPDATE_INTERVAL = 5000; // 5 δευτερόλεπτα (διάστημα ενημέρωσης τοποθεσίας)
    private Handler handler = new Handler(); // Δήλωση του Handler για εκτέλεση επαναλαμβανόμενων εργασιών

    private ProviderService provService; // Δήλωση του ProviderService
    public static volatile boolean service_on = false;

    // Runnable για ενημέρωση της τοποθεσίας
    private Runnable locationUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (lm != null && hasLocationPermission()) {
                try {
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener); // Αίτηση ενημέρωσης τοποθεσίας
                    service_on = true;
                    //showToast("Location update request");
                } catch (SecurityException e) {
                    e.printStackTrace();
                    showToast("Error requesting location update");
                }
            }
            handler.postDelayed(this, LOCATION_UPDATE_INTERVAL); // Προγραμματισμός της επόμενης ενημέρωσης
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        resolver = getContentResolver(); // Απόκτηση του ContentResolver
        provService = new ProviderService(resolver, this); // Αρχικοποίηση του ProviderService με το context
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE); // Απόκτηση του LocationManager
        initializeLocationUpdates(); // Αρχικοποίηση ενημερώσεων τοποθεσίας
    }

    private void initializeLocationUpdates() {
        if (lm != null && hasLocationPermission()) {
            try {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener); // Αίτηση ενημέρωσης τοποθεσίας
               // showToast("Update Location");
            } catch (SecurityException e) {
                e.printStackTrace();
                showToast("Error:Location was not update");
            }
        } else {
            service_on = false;
            showToast("Location permit has not been granted");
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            if (service_on) {
                double currentLatitude = location.getLatitude(); // Απόκτηση γεωγραφικού πλάτους
                double currentLongitude = location.getLongitude(); // Απόκτηση γεωγραφικού μήκους
                if(MyContentProvider.ses>0) {
                    provService.isLocationWithinRadius(currentLatitude, currentLongitude, MyContentProvider.ses-1);
                }// Έλεγχος αν η τοποθεσία είναι εντός της ακτίνας
               // showToast("Location update: " + currentLatitude + ", " + currentLongitude);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Διαχείριση αλλαγής κατάστασης
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            showToast("The provider is enabled: " + provider);
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            showToast("The provider was disabled: " + provider);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        service_on = true;
        if (hasLocationPermission()) {
            handler.postDelayed(locationUpdateRunnable, LOCATION_UPDATE_INTERVAL); // Εκκίνηση του ενημερωτικού runnable
           // showToast("Η υπηρεσία τοποθεσίας ξεκίνησε");
            return START_STICKY; // Η υπηρεσία θα επανεκκινήσει αν τερματιστεί
        } else {
           // showToast("Απαιτείται άδεια τοποθεσίας");
            return START_NOT_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        service_on = false;
        if (lm != null) {
            lm.removeUpdates(locationListener); // Διακοπή ενημερώσεων τοποθεσίας
           // showToast("Ενημερώσεις τοποθεσίας σταμάτησαν");
        }
        if (handler != null) {
            handler.removeCallbacks(locationUpdateRunnable); // Αφαίρεση των επαναλαμβανόμενων εργασιών
        }
        showToast("Location service stoped");
        super.onDestroy(); // Κλήση της onDestroy της υπερκλάσης
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Η υπηρεσία δεν υποστηρίζει δέσμευση
    }

    // Έλεγχος αν έχεις την άδεια τοποθεσίας
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // Μέθοδος για εμφάνιση toast μηνύματος
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}

