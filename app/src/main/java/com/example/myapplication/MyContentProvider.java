package com.example.myapplication;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyContentProvider extends ContentProvider {

    // Δηλώνουμε σταθερές για τα πεδία του πίνακα
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String RADIUS = "radius";
    public static final String SESSION = "session";
    public static final String INSIDE = "inside";
    public static final String ID = "id";
    private static final String AUTHORITY = "com.example.myapp.provider"; // Η αρχή του ContentProvider
    private static final String BASE_PATH = "locations"; // Η βάση για το URI των δεδομένων
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH); // Το πλήρες URI για τον ContentProvider

    private static final String DATABASE_NAME = "database.db"; // Όνομα της βάσης δεδομένων
    private static final int DATABASE_VERSION = 2; // Έκδοση της βάσης δεδομένων

    private static final String TABLE_NAME = "locations"; // Όνομα του πίνακα

    // SQL ερώτημα για τη δημιουργία του πίνακα
    private static final String CREATE_TABLE_QUERY =
            "CREATE TABLE " + TABLE_NAME + " (id INTEGER PRIMARY KEY AUTOINCREMENT, latitude REAL, longitude REAL, radius INTEGER, inside INTEGER, session INTEGER);";

    private SQLiteDatabase database; // Αντικείμενο της βάσης δεδομένων
    private DatabaseHelper dbHelper; // Βοηθητική κλάση για τη διαχείριση της βάσης δεδομένων

    public static int ses; // Μεταβλητή για την παρακολούθηση της τρέχουσας συνεδρίας
    public static long counter;


    @Override
    public boolean onCreate() {
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(getContext()); // Δημιουργία του DatabaseHelper αν δεν υπάρχει ήδη
        }
        database = dbHelper.getWritableDatabase(); // Απόκτηση εγγράψιμης βάσης δεδομένων

        if (database == null) {
            Log.e("MyContentProvider", "Failed to create database"); // Καταγραφή σφάλματος αν η δημιουργία απέτυχε
        } else {
            Log.d("MyContentProvider", "Database created successfully"); // Καταγραφή επιτυχίας
        }
        database.execSQL("DROP TABLE locations;"); // Διαγραφή του πίνακα αν υπάρχει (για σκοπούς δοκιμών)
        database.execSQL(CREATE_TABLE_QUERY); // Δημιουργία του πίνακα
        ses = 0; // Αρχικοποίηση της μεταβλητής συνεδρίας
        counter=0;
        Log.d("DatabaseHelper", "Table created successfully."); // Καταγραφή της επιτυχίας δημιουργίας πίνακα

        return true; // Επιτυχής δημιουργία του ContentProvider
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        // Εκτέλεση ερώτησης στη βάση δεδομένων και επιστροφή του αποτελέσματος ως Cursor
        return database.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // Επιστροφή του τύπου MIME για το URI (δεν υλοποιείται εδώ)
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        long id = database.insert(TABLE_NAME, null, values); // Εισαγωγή δεδομένων στη βάση
        if (id > 0) {
            Uri newUri = ContentUris.withAppendedId(CONTENT_URI, id); // Δημιουργία νέου URI με το ID του νέου στοιχείου
            getContext().getContentResolver().notifyChange(newUri, null); // Ενημέρωση του ContentResolver για την αλλαγή
            return newUri; // Επιστροφή του νέου URI
        }
        throw new SQLException("Failed to insert row into " + uri); // Ρίψη εξαίρεσης σε περίπτωση αποτυχίας εισαγωγής
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = database.delete(TABLE_NAME, selection, selectionArgs); // Διαγραφή δεδομένων από τη βάση
        getContext().getContentResolver().notifyChange(uri, null); // Ενημέρωση του ContentResolver για την αλλαγή
        return count; // Επιστροφή του αριθμού των διαγραμμένων γραμμών
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        int count = database.update(TABLE_NAME, values, selection, selectionArgs); // Ενημέρωση δεδομένων στη βάση
        getContext().getContentResolver().notifyChange(uri, null); // Ενημέρωση του ContentResolver για την αλλαγή
        return count; // Επιστροφή του αριθμού των ενημερωμένων γραμμών
    }

    // Βοηθητική κλάση για τη διαχείριση της βάσης δεδομένων
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION); // Κλήση του κατασκευαστή της SQLiteOpenHelper
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_TABLE_QUERY); // Δημιουργία του πίνακα
                Log.d("DatabaseHelper", "Table created successfully."); // Καταγραφή της επιτυχίας δημιουργίας πίνακα
            } catch (SQLException e) {
                Log.e("DatabaseHelper", "Error creating table: " + e.getMessage()); // Καταγραφή σφάλματος δημιουργίας πίνακα
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME); // Διαγραφή του πίνακα σε περίπτωση αναβάθμισης
            onCreate(db); // Αναδημιουργία του πίνακα
        }
    }

   /*
   // Μέθοδος για έλεγχο ύπαρξης του πίνακα
    private boolean tableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[]{"table", tableName});
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int count = cursor.getInt(0);
                    return count > 0; // Επιστροφή true αν ο πίνακας υπάρχει
                }
            } finally {
                cursor.close(); // Κλείσιμο του Cursor
            }
        }
        return false; // Επιστροφή false αν ο πίνακας δεν υπάρχει
    }*/
}
