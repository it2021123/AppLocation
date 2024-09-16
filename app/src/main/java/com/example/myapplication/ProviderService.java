package com.example.myapplication;

import static com.example.myapplication.MyContentProvider.CONTENT_URI;
import static com.example.myapplication.MyContentProvider.INSIDE;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import org.osmdroid.util.GeoPoint;

import java.util.Locale;

public class ProviderService {

    private ContentResolver resolver;
    private Context context; // Το context αποθηκεύεται για τη χρήση του σε μηνύματα Toast

    // Κατασκευαστής της κλάσης που δέχεται έναν ContentResolver και το Context
    public ProviderService(ContentResolver resolver, Context context) {
        this.resolver = resolver;
        this.context = context;
    }

    // Μέθοδος για να ανακτήσει τις τοποθεσίες που είναι εκτός της καθορισμένης ακτίνας
    private Cursor queryLocationsOutsideRadius(String[] projection, String selection, String[] selectionArgs) {
        return resolver.query(MyContentProvider.CONTENT_URI, projection, selection, selectionArgs, null);
    }

    // Μέθοδος για να ενημερώσει την κατάσταση μιας τοποθεσίας (π.χ., αν βρίσκεται εντός της ακτίνας)
    private void updateLocationStatus(Cursor cursor, String selection, String[] selectionArgs) {
        ContentValues updateValues = new ContentValues();
        updateValues.put(MyContentProvider.INSIDE, 1);
        resolver.update(MyContentProvider.CONTENT_URI, updateValues, selection, selectionArgs);
    }

    // Μέθοδος για διαγραφή τοποθεσίας με βάση τη γεωγραφική τοποθεσία (GeoPoint)
    /*public void deleteLocationByGeoPoint(GeoPoint point, Context c) {
        if (point == null) {
            Toast.makeText(c, "Error: GeoPoint is null", Toast.LENGTH_SHORT).show();
            return;
        }

        double latitude = point.getLatitude();
        double longitude = point.getLongitude();

        // Ορισμός του WHERE clause με μεγαλύτερη ακρίβεια στις συντεταγμένες
        String whereClause = MyContentProvider.LATITUDE + " = ? AND " + MyContentProvider.LONGITUDE + " = ?";

        // Στρογγυλοποίηση των συντεταγμένων στο 6ο δεκαδικό για ακρίβεια
        String[] whereArgs = {
                String.format(Locale.US, "%.6f", latitude),
                String.format(Locale.US, "%.6f", longitude)
        };

        // Εκτέλεση της διαγραφής χρησιμοποιώντας το ContentResolver
        Uri uri = MyContentProvider.CONTENT_URI;
        int deletedRows = resolver.delete(uri, whereClause, whereArgs);

        // Έλεγχος αποτελέσματος διαγραφής και εμφάνιση ανάλογου μηνύματος
        if (deletedRows > 0) {
            Toast.makeText(c, "Location at " + latitude + ", " + longitude + " deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(c, "No location found at " + latitude + ", " + longitude, Toast.LENGTH_SHORT).show();
        }
    }*/





    // Μέθοδος για διαγραφή τοποθεσιών με βάση το session ID
    public void deleteLocationBySession(int session, Context c) {
        // Ορισμός του WHERE clause για το session
        String whereClause = MyContentProvider.SESSION + " = ?";
        String[] whereArgs = {String.valueOf(session)};

        // Διαγραφή τοποθεσιών με βάση το session
        Uri uri = MyContentProvider.CONTENT_URI;
        int deletedRows = resolver.delete(uri, whereClause, whereArgs);

        if (deletedRows > 0) {
            Toast.makeText(context, "Locations for session " + session + " deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "No locations found for session " + session, Toast.LENGTH_SHORT).show();
        }
    }

    // Μέθοδος για διαγραφή τοποθεσίας με βάση το ID της
    public void deleteLocation(long locationId, Context c) {
        String selection = MyContentProvider.ID + " = ?";
        String[] selectionArgs = {String.valueOf(locationId)};
        int rowsDeleted = resolver.delete(MyContentProvider.CONTENT_URI, selection, selectionArgs);

        if (rowsDeleted > 0) {
            Toast.makeText(c, "Deleted location with ID: " + locationId, Toast.LENGTH_SHORT).show();
        }
    }

    // Μέθοδος για να ελέγξει αν μια τοποθεσία βρίσκεται εντός της καθορισμένης ακτίνας
    public boolean isLocationWithinRadius(double currentLatitude, double currentLongitude) {
        boolean ret = false;
        String[] projection = {
                MyContentProvider.ID,
                MyContentProvider.LATITUDE,
                MyContentProvider.LONGITUDE,
                MyContentProvider.RADIUS,
                MyContentProvider.INSIDE
        };
        String selection = MyContentProvider.INSIDE + " = ?";
        String[] selectionArgs = {"0"};
        Cursor cursor = queryLocationsOutsideRadius(projection, selection, selectionArgs);

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    if (isPointWithinRadius(cursor, currentLatitude, currentLongitude,100)) {
                        ret = true;
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return ret;
    }

    // Μέθοδος για να ελέγξει αν μια τοποθεσία βρίσκεται εντός της καθορισμένης ακτίνας από ένα συγκεκριμένο σημείο
    private boolean isPointWithinRadius(Cursor cursor, double currentLatitude, double currentLongitude ,int rad) {
        double pointLatitude = cursor.getDouble(cursor.getColumnIndexOrThrow(MyContentProvider.LATITUDE));
        double pointLongitude = cursor.getDouble(cursor.getColumnIndexOrThrow(MyContentProvider.LONGITUDE));
        int radius = rad; // Ακτίνα σε μέτρα

        double distance = calculateDistance(currentLatitude, currentLongitude, pointLatitude, pointLongitude);

        if (distance <= radius) {
            notifyLocationWithinRadius(pointLatitude, pointLongitude);
            String selection1 = MyContentProvider.ID + " = ?";
            String[] selectionArgs1 = {String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(MyContentProvider.ID)))};
            updateLocationStatus(cursor, selection1, selectionArgs1);
            return true;
        }
        return false;
    }

    // Μέθοδος για να εμφανίσει ένα μήνυμα όταν η τοποθεσία είναι εντός της ακτίνας
    private void notifyLocationWithinRadius(double pointLatitude, double pointLongitude) {
        Toast.makeText(context, "Location is in radius: " + pointLatitude + "," + pointLongitude, Toast.LENGTH_SHORT).show();
    }

    // Μέθοδος για την προσθήκη τοποθεσίας στον ContentProvider
    public void addLocationToProvider(GeoPoint point, Context c) {
        ContentValues values = new ContentValues();
        values.put(MyContentProvider.LATITUDE, point.getLatitude());
        values.put(MyContentProvider.LONGITUDE, point.getLongitude());
        values.put(MyContentProvider.RADIUS, 100); // Ακτίνα 100 μέτρα
        values.put(MyContentProvider.INSIDE, 0);
        values.put(MyContentProvider.SESSION, MyContentProvider.ses);
        Uri uri = resolver.insert(MyContentProvider.CONTENT_URI, values);

        if (uri != null) {
            Toast.makeText(c, "Location added at: " + point.getLatitude() + ", " + point.getLongitude(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(c, "Failed to add location", Toast.LENGTH_SHORT).show();
        }
    }

    // Μέθοδος για αποθήκευση περιοχών στη βάση δεδομένων μέσω του ContentProvider
    public void saveRegionsToDatabase(GeoPoint center) {
        Uri uri = MyContentProvider.CONTENT_URI;

        double latitude = center.getLatitude();
        double longitude = center.getLongitude();

        ContentValues values = new ContentValues();
        values.put(MyContentProvider.LATITUDE, latitude);
        values.put(MyContentProvider.LONGITUDE, longitude);
        values.put(MyContentProvider.RADIUS, 100); // Ακτίνα 100 μέτρα
        values.put(MyContentProvider.INSIDE, 0);
        resolver.insert(uri, values);

        // Ειδοποιήστε τις αλλαγές στους παρατηρητές
        resolver.notifyChange(uri, null);
    }

    // Μέθοδος για υπολογισμό της απόστασης μεταξύ δύο σημείων σε μέτρα
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000; // Ακτίνα της Γης σε μέτρα
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Επιστροφή της απόστασης σε μέτρα
    }
}
