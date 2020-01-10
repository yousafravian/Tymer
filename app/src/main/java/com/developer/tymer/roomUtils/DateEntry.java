package com.developer.tymer.roomUtils;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "date")
public class DateEntry {

    @PrimaryKey(autoGenerate = true)
    int id;

    @ColumnInfo(name = "sun_rise")
    Date sunRise;
    @ColumnInfo(name = "sun_set")
    Date sunSet;
    @ColumnInfo(name = "midnight")
    Date midnight;

    @Ignore
    public DateEntry(Date sunRise, Date sunSet, Date midnight) {
        this.sunRise = sunRise;
        this.sunSet = sunSet;
        this.midnight = midnight;
    }

    public DateEntry(int id, Date sunRise, Date sunSet, Date midnight) {
        this.id = id;
        this.sunRise = sunRise;
        this.sunSet = sunSet;
        this.midnight = midnight;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getSunRise() {
        return sunRise;
    }

    public void setSunRise(Date sunRise) {
        this.sunRise = sunRise;
    }

    public Date getSunSet() {
        return sunSet;
    }

    public void setSunSet(Date sunSet) {
        this.sunSet = sunSet;
    }

    public Date getMidnight() {
        return this.midnight;
    }

    public void setMidnight(Date midnight) {
        this.midnight = midnight;
    }


}
