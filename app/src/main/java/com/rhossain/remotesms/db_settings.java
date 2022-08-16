package com.rhossain.remotesms;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class db_settings {
    @PrimaryKey
    int slot;

    @ColumnInfo(name = "backups")
    String backups;

    @ColumnInfo(name = "sendsim")
    int sendsim;
}
