package com.example.mhiker_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "M_Hiker.db";
    // THAY ĐỔI: Tăng phiên bản CSDL lên 6 để buộc onUpgrade
    private static final int DATABASE_VERSION = 6;

    public static final String TABLE_HIKES = "hikes";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_PARKING = "parking";
    public static final String COLUMN_LENGTH = "length";
    public static final String COLUMN_DIFFICULTY = "difficulty";
    public static final String COLUMN_HIKER_COUNT = "hiker_count";
    public static final String COLUMN_EQUIPMENT = "equipment";

    public static final String TABLE_OBSERVATIONS = "observations";
    public static final String COLUMN_OBS_ID = "_id";
    public static final String COLUMN_OBS_TEXT = "observation_text";
    public static final String COLUMN_OBS_TIME = "observation_time";
    public static final String COLUMN_OBS_COMMENTS = "additional_comments";
    public static final String COLUMN_HIKE_ID_FK = "hike_id";


    private static final String CREATE_TABLE_HIKES =
            "CREATE TABLE " + TABLE_HIKES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_LOCATION + " TEXT NOT NULL, " +
                    COLUMN_DATE + " TEXT NOT NULL, " +
                    COLUMN_PARKING + " INTEGER NOT NULL, " +
                    COLUMN_LENGTH + " TEXT NOT NULL, " +
                    COLUMN_DIFFICULTY + " TEXT NOT NULL, " +
                    COLUMN_HIKER_COUNT + " TEXT, " +
                    COLUMN_EQUIPMENT + " TEXT);"; // (Đã xóa cột Description)

    private static final String CREATE_TABLE_OBSERVATIONS =
            "CREATE TABLE " + TABLE_OBSERVATIONS + " (" +
                    COLUMN_OBS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_OBS_TEXT + " TEXT NOT NULL, " +
                    COLUMN_OBS_TIME + " TEXT NOT NULL, " +
                    COLUMN_OBS_COMMENTS + " TEXT, " +
                    COLUMN_HIKE_ID_FK + " INTEGER, " +
                    "FOREIGN KEY(" + COLUMN_HIKE_ID_FK + ") REFERENCES " + TABLE_HIKES + "(" + COLUMN_ID + "));";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_HIKES);
        db.execSQL(CREATE_TABLE_OBSERVATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa bảng cũ và tạo lại
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OBSERVATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HIKES);
        onCreate(db);
    }

    public long addHike(Hike hike) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, hike.getName());
        values.put(COLUMN_LOCATION, hike.getLocation());
        values.put(COLUMN_DATE, hike.getDateOfHike());
        values.put(COLUMN_PARKING, hike.isParkingAvailable() ? 1 : 0);
        values.put(COLUMN_LENGTH, hike.getLengthOfHike());
        values.put(COLUMN_DIFFICULTY, hike.getDifficultyLevel());
        values.put(COLUMN_HIKER_COUNT, hike.getHikerCount());
        values.put(COLUMN_EQUIPMENT, hike.getEquipment());

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
        values.put(COLUMN_HIKER_COUNT, hike.getHikerCount());
        values.put(COLUMN_EQUIPMENT, hike.getEquipment());

        return db.update(TABLE_HIKES, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(hike.getId())});
    }

    public List<Hike> searchHikesByName(String query) {
        List<Hike> hikes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT * FROM " + TABLE_HIKES + " WHERE " + COLUMN_NAME + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%"};

        Cursor cursor = db.rawQuery(sql, selectionArgs);

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

    public long addObservation(Observation observation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_OBS_TEXT, observation.getObservationText());
        values.put(COLUMN_OBS_TIME, observation.getTimeOfObservation());
        values.put(COLUMN_OBS_COMMENTS, observation.getAdditionalComments());
        values.put(COLUMN_HIKE_ID_FK, observation.getHikeId());
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

    private Hike cursorToHike(Cursor cursor) {
        Hike hike = new Hike();
        hike.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        hike.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
        hike.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)));
        hike.setDateOfHike(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
        hike.setParkingAvailable(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PARKING)) == 1);
        hike.setLengthOfHike(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LENGTH)));
        hike.setDifficultyLevel(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIFFICULTY)));
        hike.setHikerCount(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HIKER_COUNT)));
        hike.setEquipment(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EQUIPMENT)));
        return hike;
    }

    private Observation cursorToObservation(Cursor cursor) {
        Observation observation = new Observation();
        observation.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_OBS_ID)));
        observation.setObservationText(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OBS_TEXT)));
        observation.setTimeOfObservation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OBS_TIME)));
        observation.setAdditionalComments(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OBS_COMMENTS)));
        observation.setHikeId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_HIKE_ID_FK)));
        return observation;
    }
}