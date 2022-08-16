package com.rhossain.remotesms;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class db_receiver {
    @PrimaryKey(autoGenerate = true)
    int _id;

    @ColumnInfo(name = "chat_id")
    long chatId;
}
