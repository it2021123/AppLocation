package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Εγγραφή για το αίτημα πολλαπλών αδειών πρόσβασης τοποθεσίας
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            // Έλεγχος αν παραχωρήθηκε η άδεια για ακριβή τοποθεσία
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            // Έλεγχος αν παραχωρήθηκε η άδεια για κατά προσέγγιση τοποθεσία
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION, false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Άδεια για ακριβή τοποθεσία παραχωρήθηκε
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Άδεια για κατά προσέγγιση τοποθεσία παραχωρήθηκε
                            } else {
                                // Δεν παραχωρήθηκε καμία άδεια τοποθεσίας
                            }
                        }
                );

        // Εκκίνηση του αιτήματος για άδειες τοποθεσίας
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

        // Δημιουργία αναφοράς στο Switch για την υπηρεσία τοποθεσίας
        Switch locationServiceSwitch = findViewById(R.id.locationServiceSwitch);

        // Καθορισμός αρχικής κατάστασης του Switch
        locationServiceSwitch.setChecked(LocationService.service_on);

        // Διαχείριση αλλαγής κατάστασης του Switch
        locationServiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && LocationService.service_on) {
                    // Αν είναι ενεργοποιημένο το Switch και η υπηρεσία είναι ενεργή
                    startService(new Intent(MainActivity.this, LocationService.class)); // Εκκίνηση της υπηρεσίας τοποθεσίας
                    LocationService.service_on = false; // Ορισμός της κατάστασης της υπηρεσίας σε μη ενεργή
                    Toast.makeText(MainActivity.this, "Location service is now activated", Toast.LENGTH_SHORT).show();
                } else {
                    // Αν το Switch απενεργοποιηθεί
                    stopService(new Intent(MainActivity.this, LocationService.class)); // Τερματισμός της υπηρεσίας τοποθεσίας
                    LocationService.service_on = true; // Ορισμός της κατάστασης της υπηρεσίας σε ενεργή
                    Toast.makeText(MainActivity.this, "Location service is now deactivated", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Κουμπί για τη μετάβαση στο MapActivity
        Button mapButton = findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Έλεγχος αν έχει παραχωρηθεί η άδεια για τοποθεσία
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    // Έχει παραχωρηθεί η άδεια, μετάβαση στο MapActivity
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    startActivity(intent);
                } else {
                    // Αν δεν έχει παραχωρηθεί η άδεια, εμφάνιση μηνύματος στον χρήστη
                    Toast.makeText(MainActivity.this, "Location permission is required", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /*
        // Κουμπί για τερματισμό εντοπισμού και καταγραφής (απενεργοποιημένο προς το παρόν)
        Button stopTrackingButton = findViewById(R.id.stopTrackingButton);
        stopTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LocationService.service_on) {
                    stopService(new Intent(MainActivity.this, LocationService.class));
                    LocationService.service_on = false;
                    Toast.makeText(MainActivity.this, "Location service is now deactivated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Location service is already deactivated.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        */

        // Κουμπί για τη μετάβαση στο ResultsMapActivity
        Button resultsButton = findViewById(R.id.resultsButton);
        resultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Έλεγχος αν έχει παραχωρηθεί η άδεια για τοποθεσία
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    // Έχει παραχωρηθεί η άδεια, μετάβαση στο ResultsMapActivity
                    Intent intent = new Intent(MainActivity.this, ResultsMapActivity.class);
                    startActivity(intent);
                } else {
                    // Αν δεν έχει παραχωρηθεί η άδεια, εμφάνιση μηνύματος στον χρήστη
                    Toast.makeText(MainActivity.this, "Location permission is required", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Κουμπί για τον τερματισμό της εφαρμογής
        Button exitAppButton = findViewById(R.id.exitAppButton);
        exitAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Έλεγχος αν η υπηρεσία τοποθεσίας είναι ενεργή και τερματισμός της αν είναι
                if (LocationService.service_on) {
                    stopService(new Intent(MainActivity.this, LocationService.class));
                    LocationService.service_on = false;
                }
                // Τερματισμός της εφαρμογής
                finishAffinity();
            }
        });
    }
}