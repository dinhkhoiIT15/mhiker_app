// app/src/main/java/com/example/mhiker_app/DatabaseHelper.java

package com.example.mhiker_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "M_Hiker.db";
    // CẬP NHẬT: Tăng phiên bản DB lên 10 để thêm cột GPS và Ảnh
    private static final int DATABASE_VERSION = 10;

    // Bảng Hikes
    public static final String TABLE_HIKES = "hikes";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_PARKING = "parking";
    public static final String COLUMN_LENGTH = "length";
    public static final String COLUMN_DIFFICULTY = "difficulty";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_HIKER_COUNT = "hiker_count";
    public static final String COLUMN_EQUIPMENT = "equipment";
    // THÊM MỚI: Cột GPS
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";

    // Bảng Observations
    public static final String TABLE_OBSERVATIONS = "observations";
    public static final String COLUMN_OBS_ID = "_id";
    public static final String COLUMN_OBS_TEXT = "observation_text";
    public static final String COLUMN_OBS_TIME = "observation_time";
    public static final String COLUMN_OBS_COMMENTS = "additional_comments";
    public static final String COLUMN_HIKE_ID_FK = "hike_id";
    // THÊM MỚI: Cột ảnh
    public static final String COLUMN_OBS_IMAGE_PATH = "image_path";

    // Bảng Users
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "_id";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_USERNAME = "username";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_PHONE = "phone_number";


    private static final String CREATE_TABLE_HIKES =
            "CREATE TABLE " + TABLE_HIKES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_LOCATION + " TEXT NOT NULL, " +
                    COLUMN_DATE + " TEXT NOT NULL, " +
                    COLUMN_PARKING + " INTEGER NOT NULL, " +
                    COLUMN_LENGTH + " TEXT NOT NULL, " +
                    COLUMN_DIFFICULTY + " TEXT NOT NULL, " +
                    COLUMN_DESCRIPTION + " TEXT, " +
                    COLUMN_HIKER_COUNT + " TEXT, " +
                    COLUMN_EQUIPMENT + " TEXT, " +
                    // THÊM MỚI
                    COLUMN_LATITUDE + " REAL DEFAULT 0.0, " +
                    COLUMN_LONGITUDE + " REAL DEFAULT 0.0);";

    private static final String CREATE_TABLE_OBSERVATIONS =
            "CREATE TABLE " + TABLE_OBSERVATIONS + " (" +
                    COLUMN_OBS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_OBS_TEXT + " TEXT NOT NULL, " +
                    COLUMN_OBS_TIME + " TEXT NOT NULL, " +
                    COLUMN_OBS_COMMENTS + " TEXT, " +
                    // THÊM MỚI
                    COLUMN_OBS_IMAGE_PATH + " TEXT, " +
                    COLUMN_HIKE_ID_FK + " INTEGER, " +
                    "FOREIGN KEY(" + COLUMN_HIKE_ID_FK + ") REFERENCES " + TABLE_HIKES + "(" + COLUMN_ID + "));";

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_NAME + " TEXT NOT NULL, " +
                    COLUMN_USER_USERNAME + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_USER_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_USER_PHONE + " TEXT);";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_HIKES);
        db.execSQL(CREATE_TABLE_OBSERVATIONS);
        db.execSQL(CREATE_TABLE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // CẬP NHẬT: onUpgrade sẽ chạy vì version đã tăng
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OBSERVATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HIKES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // -----------------------------------------------------------------
    // PHƯƠNG THỨC CHO HIKE
    // -----------------------------------------------------------------

    public long addHike(Hike hike) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, hike.getName());
        values.put(COLUMN_LOCATION, hike.getLocation());
        values.put(COLUMN_DATE, hike.getDateOfHike());
        values.put(COLUMN_PARKING, hike.isParkingAvailable() ? 1 : 0);
        values.put(COLUMN_LENGTH, hike.getLengthOfHike());
        values.put(COLUMN_DIFFICULTY, hike.getDifficultyLevel());
        values.put(COLUMN_DESCRIPTION, hike.getDescription());
        values.put(COLUMN_HIKER_COUNT, hike.getHikerCount());
        values.put(COLUMN_EQUIPMENT, hike.getEquipment());
        // THÊM MỚI
        values.put(COLUMN_LATITUDE, hike.getLatitude());
        values.put(COLUMN_LONGITUDE, hike.getLongitude());

        long id = db.insert(TABLE_HIKES, null, values);
        db.close();
        return id;
    }

    public Hike getHikeById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HIKES, null, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);

        Hike hike = null;
        if (cursor != null && cursor.moveToFirst()) {
            hike = cursorToHike(cursor);
            cursor.close();
        }
        db.close();
        return hike;
    }

    public List<Hike> getAllHikes() {
        List<Hike> hikes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_HIKES, null);

        if (cursor.moveToFirst()) {
            do {
                hikes.add(cursorToHike(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return hikes;
    }

    public int updateHike(Hike hike) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, hike.getName());
        values.put(COLUMN_LOCATION, hike.getLocation());
        values.put(COLUMN_DATE, hike.getDateOfHike());
        values.put(COLUMN_PARKING, hike.isParkingAvailable() ? 1 : 0);
        values.put(COLUMN_LENGTH, hike.getLengthOfHike());
        values.put(COLUMN_DIFFICULTY, hike.getDifficultyLevel());
        values.put(COLUMN_DESCRIPTION, hike.getDescription());
        values.put(COLUMN_HIKER_COUNT, hike.getHikerCount());
        values.put(COLUMN_EQUIPMENT, hike.getEquipment());
        // THÊM MỚI
        values.put(COLUMN_LATITUDE, hike.getLatitude());
        values.put(COLUMN_LONGITUDE, hike.getLongitude());

        return db.update(TABLE_HIKES, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(hike.getId())});
    }

    // ... (Các phương thức searchHikesByName, searchHikesAdvanced giữ nguyên) ...
    public List<Hike> searchHikesByName(String query) {
        return searchHikesAdvanced(query, "", "", "");
    }

    public List<Hike> searchHikesAdvanced(String name, String location, String length, String date) {
        List<Hike> hikes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE_HIKES);
        List<String> selectionArgs = new ArrayList<>();
        List<String> whereClauses = new ArrayList<>();

        if (!TextUtils.isEmpty(name)) {
            whereClauses.add(COLUMN_NAME + " LIKE ?");
            selectionArgs.add("%" + name + "%");
        }

        if (!TextUtils.isEmpty(location)) {
            whereClauses.add(COLUMN_LOCATION + " LIKE ?");
            selectionArgs.add("%" + location + "%");
        }

        if (!TextUtils.isEmpty(date)) {
            whereClauses.add(COLUMN_DATE + " = ?");
            selectionArgs.add(date);
        }

        if (!TextUtils.isEmpty(length)) {
            try {
                double minLength = Double.parseDouble(length);
                whereClauses.add("CAST(" + COLUMN_LENGTH + " AS REAL) >= ?");
                selectionArgs.add(String.valueOf(minLength));
            } catch (NumberFormatException e) {
                // Bỏ qua
            }
        }

        if (!whereClauses.isEmpty()) {
            sql.append(" WHERE ").append(TextUtils.join(" AND ", whereClauses));
        }

        Cursor cursor = db.rawQuery(sql.toString(), selectionArgs.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                hikes.add(cursorToHike(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return hikes;
    }


    public void deleteHike(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_OBSERVATIONS, COLUMN_HIKE_ID_FK + " = ?", new String[]{String.valueOf(id)});
        db.delete(TABLE_HIKES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteAllHikes() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_OBSERVATIONS, null, null);
        db.delete(TABLE_HIKES, null, null);
        db.close();
    }

    // -----------------------------------------------------------------
    // PHƯƠNG THỨC CHO OBSERVATION
    // -----------------------------------------------------------------

    public long addObservation(Observation observation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_OBS_TEXT, observation.getObservationText());
        values.put(COLUMN_OBS_TIME, observation.getTimeOfObservation());
        values.put(COLUMN_OBS_COMMENTS, observation.getAdditionalComments());
        values.put(COLUMN_HIKE_ID_FK, observation.getHikeId());
        // THÊM MỚI
        values.put(COLUMN_OBS_IMAGE_PATH, observation.getImagePath());
        long id = db.insert(TABLE_OBSERVATIONS, null, values);
        db.close();
        return id;
    }

    public List<Observation> getAllObservationsForHike(long hikeId) {
        List<Observation> observations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_OBSERVATIONS, null, COLUMN_HIKE_ID_FK + " = ?",
                new String[]{String.valueOf(hikeId)}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                observations.add(cursorToObservation(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return observations;
    }

    public int updateObservation(Observation observation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_OBS_TEXT, observation.getObservationText());
        values.put(COLUMN_OBS_TIME, observation.getTimeOfObservation());
        values.put(COLUMN_OBS_COMMENTS, observation.getAdditionalComments());
        // THÊM MỚI
        values.put(COLUMN_OBS_IMAGE_PATH, observation.getImagePath());
        int rows = db.update(TABLE_OBSERVATIONS, values, COLUMN_OBS_ID + " = ?",
                new String[]{String.valueOf(observation.getId())});
        db.close();
        return rows;
    }

    public void deleteObservation(long observationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_OBSERVATIONS, COLUMN_OBS_ID + " = ?",
                new String[]{String.valueOf(observationId)});
        db.close();
    }

    // -----------------------------------------------------------------
    // PHƯƠNG THỨC CURSOR
    // -----------------------------------------------------------------

    private Hike cursorToHike(Cursor cursor) {
        Hike hike = new Hike();
        hike.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        hike.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
        hike.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)));
        hike.setDateOfHike(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
        hike.setParkingAvailable(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PARKING)) == 1);
        hike.setLengthOfHike(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LENGTH)));
        hike.setDifficultyLevel(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIFFICULTY)));
        hike.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
        hike.setHikerCount(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HIKER_COUNT)));
        hike.setEquipment(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EQUIPMENT)));
        // THÊM MỚI
        hike.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)));
        hike.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)));
        return hike;
    }

    private Observation cursorToObservation(Cursor cursor) {
        Observation observation = new Observation();
        observation.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_OBS_ID)));
        observation.setObservationText(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OBS_TEXT)));
        observation.setTimeOfObservation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OBS_TIME)));
        observation.setAdditionalComments(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OBS_COMMENTS)));
        observation.setHikeId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_HIKE_ID_FK)));
        // THÊM MỚI
        observation.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OBS_IMAGE_PATH)));
        return observation;
    }

    // -----------------------------------------------------------------
    // CÁC PHƯƠNG THỨC CHO USER (Giữ nguyên)
    // -----------------------------------------------------------------

    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, user.getName());
        values.put(COLUMN_USER_USERNAME, user.getUsername());
        values.put(COLUMN_USER_PASSWORD, user.getPassword());
        values.put(COLUMN_USER_PHONE, user.getPhoneNumber());
        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    public boolean checkUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID},
                COLUMN_USER_USERNAME + " = ?",
                new String[]{username}, null, null, null);

        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return exists;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID},
                COLUMN_USER_USERNAME + " = ? AND " + COLUMN_USER_PASSWORD + " = ?",
                new String[]{username, password}, null, null, null);

        boolean loginSuccess = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return loginSuccess;
    }

    public boolean checkUserForReset(String username, String phoneNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID},
                COLUMN_USER_USERNAME + " = ? AND " + COLUMN_USER_PHONE + " = ?",
                new String[]{username, phoneNumber}, null, null, null);

        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }

    public int updatePassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_PASSWORD, newPassword);

        return db.update(TABLE_USERS, values, COLUMN_USER_USERNAME + " = ?",
                new String[]{username});
    }
}