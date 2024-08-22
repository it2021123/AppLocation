package com.example.myapplication;

import android.content.ContentUris;
import android.graphics.Color;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Iterator;
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

    // Κατασκευαστής της κλάσης MapHelper
    public MapHelper(MapView mapView, Context context) {
        this.mapView = mapView;
        this.context = context;
        this.circles = new ArrayList<>(); // Αρχικοποίηση της λίστας circles
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
    }

    // Αφαίρεση του πλησιέστερου κύκλου από τη δεδομένη θέση
    public GeoPoint removeNearestCircle(GeoPoint point) {
        // Έλεγχος αν υπάρχουν κύκλοι και αν το σημείο είναι έγκυρο
        if (mapView == null || circles == null || circles.isEmpty() || point == null) {
            Log.d("MapHelper", "MapView, circles list, or point is null, or circles list is empty");
            return null;
        }

        GeoPoint nearestCirclePoint = null; // Μεταβλητή για το πλησιέστερο σημείο κύκλου
        Polygon nearestCircle = null; // Μεταβλητή για τον πλησιέστερο κύκλο
        double minDistance = Double.MAX_VALUE; // Αρχικοποίηση της ελάχιστης απόστασης με μέγιστη τιμή

        // Αναζήτηση του πλησιέστερου κύκλου
        for (Polygon circle : circles) {
            if (circle.getPoints() == null || circle.getPoints().isEmpty()) {
                Log.d("MapHelper", "Circle has no points");
                continue;
            }

            GeoPoint circleCenter = (GeoPoint) circle.getPoints().get(0); // Λήψη του κέντρου του κύκλου
            double distance = calculateDistance(
                    point.getLatitude(), point.getLongitude(),
                    circleCenter.getLatitude(), circleCenter.getLongitude()
            );

            // Ενημέρωση του πλησιέστερου κύκλου
            if (distance < minDistance) {
                minDistance = distance;
                nearestCirclePoint = circleCenter;
                nearestCircle = circle;
            }
        }

        // Αφαίρεση του πλησιέστερου κύκλου αν η απόσταση είναι μικρότερη ή ίση των 200 μέτρων
        if (nearestCircle != null && minDistance <= 200) {
            mapView.getOverlayManager().remove(nearestCircle);
            circles.remove(nearestCircle);
            mapView.invalidate(); // Ενημέρωση της προβολής του χάρτη
            Log.d("MapHelper", "Removed nearest circle at distance: " + minDistance + " meters");
        } else {
            Log.d("MapHelper", "No circles found within the specified distance to remove");
        }

        return nearestCirclePoint; // Επιστροφή της θέσης του πλησιέστερου κύκλου
    }

    // Υπολογισμός της απόστασης μεταξύ δύο σημείων χρησιμοποιώντας τη σφαιρική γεωμετρία
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371000; // ακτίνα της Γης σε μέτρα
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c; // Επιστροφή της απόστασης σε μέτρα
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
