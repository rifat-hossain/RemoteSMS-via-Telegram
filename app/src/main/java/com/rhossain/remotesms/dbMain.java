package com.rhossain.remotesms;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Queue.class, db_settings.class, db_receiver.class}, version = 1)
public abstract class dbMain extends RoomDatabase {
    public abstract dbDao dao();

    public static volatile dbMain INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    static dbMain getDB(final Context context){
        if (INSTANCE == null) {
            synchronized (dbMain.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            dbMain.class, "core_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
