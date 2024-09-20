package com.example.myapplication;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.graphics.Color;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MapHelper {


    private MapView mapView;  // Αντικείμενο του χάρτη
    private Context context;  // Context της εφαρμογής
    private List<Polygon> circles; // Λίστα με τους κύκλους που έχουν προστεθεί στο χάρτη
    private Marker lastMarker; // Δήλωση μεταβλητής για τον τελευταίο marker που προστέθηκε

    private LinkedList<Long> idCircles;
   // private long counter=1;
    // Κατασκευαστής της κλάσης MapHelper
    public MapHelper(MapView mapView, Context context) {
        this.mapView = mapView;
        this.context = context;
        this.circles = new ArrayList<>(); // Αρχικοποίηση της λίστας circles
        this.idCircles = new LinkedList<>();

    }

    // Προσθήκη marker στο χάρτη
    public void addMarker(GeoPoint position) {
        if (mapView == null) return; // Έλεγχος αν το mapView είναι null

        // Αφαίρεση του προηγούμενου marker αν υπάρχει
        if (lastMarker != null) {
            mapView.getOverlays().remove(lastMarker);
        }

        // Δημιουργία και προσθήκη νέου marker στη νέα θέση
        lastMarker = new Marker(mapView);
        lastMarker.setPosition(position);
        mapView.getOverlays().add(lastMarker);
        mapView.invalidate(); // Ενημέρωση της προβολής του χάρτη
    }

    // Προσθήκη κύκλου στο χάρτη
    public void addCircle(GeoPoint center, double radiusMeters, int color) {
        if (mapView == null) return; // Έλεγχος αν το mapView είναι null
        Polygon circle = createCircle(center, radiusMeters, 360, color); // Δημιουργία του κύκλου
        mapView.getOverlayManager().add(circle); // Προσθήκη του κύκλου στο overlay του χάρτη
        circles.add(circle); // Προσθήκη του κύκλου στη λίστα
        mapView.invalidate(); // Ενημέρωση της προβολής του χάρτη
        idCircles.add(MyContentProvider.counter);
        MyContentProvider.counter++;
    }

    // Αφαίρεση του πλησιέστερου κύκλου από τη δεδομένη θέση
    // Αφαίρεση του πλησιέστερου κύκλου από τη δεδομένη θέση
    public GeoPoint removeNearestCircle(GeoPoint point, ProviderService providerse) {
        if (mapView == null || circles == null || circles.isEmpty() || point == null) {
            Log.e("MapHelper", "MapView, circles list, or point is null, or circles list is empty");
            return null;
        }

        GeoPoint nearestCirclePoint = null;  // Μεταβλητή για το πλησιέστερο σημείο κύκλου
        Polygon nearestCircle = null;  // Μεταβλητή για τον πλησιέστερο κύκλο
        double minDistance = 500;  // Αρχικοποίηση της ελάχιστης απόστασης
        int i = 0;
        int j = -1;  // Χρήση -1 για αρχικοποίηση ώστε να δείχνει όταν δεν έχει βρεθεί ακόμα πλησιέστερος κύκλος

        // Βρίσκουμε τον πλησιέστερο κύκλο
        for (Polygon circle : circles) {
            if (circle.getPoints() != null && !circle.getPoints().isEmpty()) {
                GeoPoint circleCenter = (GeoPoint) circle.getPoints().get(0);  // Λήψη του κέντρου του κύκλου
                double distance = providerse.calculateDistance(point.getLatitude(), point.getLongitude(), circleCenter.getLatitude(), circleCenter.getLongitude());

                // Αν η απόσταση είναι μικρότερη από την προηγούμενη ελάχιστη, ενημερώνουμε τον πλησιέστερο κύκλο
                if (distance < minDistance) {
                    j = i;
                    minDistance = distance;
                    nearestCirclePoint = circleCenter;
                    nearestCircle = circle;
                }
            }
            i++;
        }

        // Αν βρέθηκε κύκλος και η απόσταση είναι μικρότερη από 500 μέτρα, αφαιρούμε τον κύκλο
        if (nearestCircle != null && j >= 0) {
            mapView.getOverlayManager().remove(nearestCircle);
            circles.remove(nearestCircle);
            long idc = idCircles.get(j);  // Χρήση της LinkedList για πρόσβαση στο ID του κύκλου
            idCircles.remove(j);  // Αφαίρεση του ID από τη LinkedList
            mapView.invalidate();  // Ενημέρωση της προβολής του χάρτη
            Log.d("MapHelper", "Removed nearest circle at distance: " + minDistance + " meters");

            // Αν υπάρχει υπηρεσία ProviderService, διαγράφουμε τη θέση από τη βάση δεδομένων
            if (providerse != null && nearestCirclePoint != null) {
                providerse.deleteLocation(idc, context);
            } else {
                Log.e("MapHelper", "ProviderService or nearestCirclePoint is null");
            }

            return nearestCirclePoint;  // Επιστρέφουμε το σημείο του πλησιέστερου κύκλου που διαγράφηκε
        } else {
            Log.d("MapHelper", "No circle found within 500 meters to remove");
            return null;  // Αν δεν βρέθηκε κύκλος εντός της απόστασης, επιστρέφουμε null
        }
    }
    // Δημιουργία κύκλου (Polygon) με κέντρο, ακτίνα και χρώμα
    private Polygon createCircle(GeoPoint centerPoint, double radiusMeters, int numberOfPoints, int color) {
        Polygon circlePolygon = new Polygon();
        List<GeoPoint> points = new ArrayList<>();

        // Δημιουργία σημείων για τον κύκλο
        for (int i = 0; i < numberOfPoints; i++) {
            double angle = Math.toRadians((360.0 / numberOfPoints) * i);
            double lat = centerPoint.getLatitude() + (radiusMeters / 111111.0) * Math.cos(angle);
            double lon = centerPoint.getLongitude() + (radiusMeters / (111111.0 * Math.cos(Math.toRadians(centerPoint.getLatitude())))) * Math.sin(angle);
            GeoPoint point = new GeoPoint(lat, lon);
            points.add(point);
        }

        // Ρύθμιση των σημείων και χρωμάτων για τον κύκλο
        circlePolygon.setPoints(points);
        circlePolygon.setFillColor(Color.argb(75, Color.red(color), Color.green(color), Color.blue(color)));
        circlePolygon.setStrokeColor(color);
        return circlePolygon; // Επιστροφή του κύκλου ως Polygon
    }

    // Προβολή των σημειωμένων περιοχών στον χάρτη
    public void showMarkedRegions(int session) {
        addPolylinesForInside(1, Color.GREEN, session); // Προσθήκη πράσινων περιοχών
        addPolylinesForInside(0, Color.RED, session);   // Προσθήκη κόκκινων περιοχών
        addPolylinesForInside(2,Color.YELLOW,session);//
        mapView.invalidate(); // Ενημέρωση της προβολής του χάρτη
    }

    // Προσθήκη πολυγώνων για περιοχές εντός/εκτός ενός καθορισμένου σημείου
    private void addPolylinesForInside(int insideValue, int color, int session) {
        Cursor cursor = context.getContentResolver().query(
                MyContentProvider.CONTENT_URI,
                new String[]{MyContentProvider.LATITUDE, MyContentProvider.LONGITUDE, MyContentProvider.INSIDE},
                MyContentProvider.INSIDE + " = ? AND " + MyContentProvider.SESSION + " = ?",
                new String[]{String.valueOf(insideValue), String.valueOf(session)},
                null
        );

        if (cursor != null) {
            List<Polygon> polygons = new ArrayList<>();

            // Ανάγνωση δεδομένων από τη βάση και δημιουργία πολυγώνων
            while (cursor.moveToNext()) {
                int latitudeIndex = cursor.getColumnIndexOrThrow(MyContentProvider.LATITUDE);
                int longitudeIndex = cursor.getColumnIndexOrThrow(MyContentProvider.LONGITUDE);

                double latitude = cursor.getDouble(latitudeIndex);
                double longitude = cursor.getDouble(longitudeIndex);

                GeoPoint point = new GeoPoint(latitude, longitude);
                Polygon polygon = createCircle(point, 100, 360, color);
                polygons.add(polygon);
            }

            cursor.close();

            // Προσθήκη των πολυγώνων στο χάρτη
            for (Polygon polygon : polygons) {
                mapView.getOverlayManager().add(polygon);
            }
        }
    }

    // Εκκαθάριση όλων των overlay στοιχείων από τον χάρτη
    public void clearMap() {
        mapView.getOverlayManager().clear();
        circles.clear(); // Επαναφορά της λίστας των κύκλων
        mapView.invalidate(); // Ενημέρωση της προβολής του χάρτη
    }
}
