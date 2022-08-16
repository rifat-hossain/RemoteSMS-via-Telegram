package com.rhossain.remotesms;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Queue {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "sender")
    public String sender;

    @ColumnInfo(name = "body")
    public String body;

    @ColumnInfo(name = "datetime")
    public long dt;
}
