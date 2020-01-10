package com.developer.tymer.roomUtils;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@androidx.room.Database(entities = {DateEntry.class}, version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class Database extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "updatesHistory";
    private static Database sInstance;


    public static Database getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        Database.class, Database.DATABASE_NAME)
                        //Only Allow for testing Queries on Main Thread (Executers used for this purposes)
                        .allowMainThreadQueries()
                        .build();
            }
        }
        return sInstance;
    }

    public abstract DateDao datedao();
}
