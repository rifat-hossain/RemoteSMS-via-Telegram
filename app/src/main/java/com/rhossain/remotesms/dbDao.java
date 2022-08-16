package com.rhossain.remotesms;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface dbDao {
    @Query("SELECT * FROM Queue")
    List<Queue> getQueues();

    @Query("SELECT * FROM db_settings WHERE slot = 1")
    db_settings load_settings();

    @Query("SELECT * FROM db_receiver")
    List<db_receiver> getReceivers();

    @Insert
    void insert(Queue sms);

    @Delete
    void delete(Queue sms);

    @Delete
    void deleteReceiver(db_receiver receiver);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert_set(db_settings settings);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert_receiver(db_receiver receiver);
}
