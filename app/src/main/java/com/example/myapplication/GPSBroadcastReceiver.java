package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.widget.Toast;



// Δημιουργία κλάσης που επεκτείνει την BroadcastReceiver για την παρακολούθηση ενημερώσεων τοποθεσίας
public class GPSBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Έλεγχος αν το intent έχει την κατάλληλη δράση "com.example.myapplication.LOCATION_UPDATE"
        if (intent.getAction() != null && intent.getAction().equals("com.example.myapplication.LOCATION_UPDATE")) {
            // Απόκτηση των δεδομένων τοποθεσίας από το intent
            double latitude = intent.getDoubleExtra("latitude", 0.0);
            double longitude = intent.getDoubleExtra("longitude", 0.0);

            // Έλεγχος της κατάστασης του GPS
            if (isGpsEnabled(context)) {
                // Εμφάνιση μηνύματος Toast αν το GPS είναι ενεργό
                Toast.makeText(context, "GPS IS ACTIVE", Toast.LENGTH_SHORT).show();
                // Εκκίνηση της υπηρεσίας LocationService
                context.startService(new Intent(context, LocationService.class));
                LocationService.service_on= true;
            } else {//αν το GPS δεν είναι ενεργό
                LocationService.service_on= false;
                Toast.makeText(context, "GPS IS NO ACTIVE", Toast.LENGTH_SHORT).show();
                // Διακοπή της υπηρεσίας LocationService
                context.stopService(new Intent(context, LocationService.class));
            }
        }
    }

    // Μέθοδος που ελέγχει αν το GPS είναι ενεργοποιημένο
    private boolean isGpsEnabled(Context context) {
        // Παίρνει το LocationManager από το σύστημα μέσω του context.
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Ελέγχει αν το locationManager δεν είναι null και αν ο πάροχος GPS είναι ενεργοποιημένος.
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}

