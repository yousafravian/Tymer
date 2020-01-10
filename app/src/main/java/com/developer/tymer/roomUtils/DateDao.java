package com.developer.tymer.roomUtils;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DateDao {

    @Query("SELECT * FROM date")
    List<DateEntry> getLastUpdates();

    @Insert
    void insertDate(DateEntry dateEntry);


}
