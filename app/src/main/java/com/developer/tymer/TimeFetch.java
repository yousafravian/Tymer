package com.developer.tymer;

import com.developer.tymer.timeUtils.TimeData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TimeFetch {
    @GET("json")
    Call<TimeData> getTimeData(@Query("lat") double latitude, @Query("lng") double longitude, @Query("date") String date);
}
