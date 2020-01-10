package com.developer.tymer.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.developer.tymer.TimeFetch;
import com.developer.tymer.roomUtils.Database;
import com.developer.tymer.roomUtils.DateEntry;
import com.developer.tymer.roomUtils.RoomExecuters;
import com.developer.tymer.timeUtils.TimeData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BackgroundService {

    private static TimeFetch mTimeFetch;
    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api.sunrise-sunset.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private static Call<TimeData> mTimeCall = mTimeFetch.getTimeData(31.571968, 74.3235584, "today");
    /*
    Action to Call Server for data
     */
    public static final String SERVER_CALL_ACTION = "call-server";
    public static final String DEUG_TAG = "debug";

    public static void executeTask(final Context context, String action) {

        if (action.equals(SERVER_CALL_ACTION)) {
            //TODO Some Action
            RoomExecuters.getsIntance().getNetworkIo().execute(new Runnable() {
                @Override
                public void run() {
                    mTimeCall.enqueue(new Callback<TimeData>() {
                        @Override
                        public void onResponse(@NonNull Call<TimeData> call, @NonNull final Response<TimeData> response) {
                            final TimeData results = response.body();
                            if (response.isSuccessful() && results != null && results.getStatus().equals(TimeData.STATUS_OK)) {
                                final Date Srise = parseTime(results.getResults().getSunrise());
                                final Date Sset = parseTime(results.getResults().getSunset());

                                RoomExecuters.getsIntance().getDiskIo().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        Database.getInstance(context).datedao().insertDate(new DateEntry(Srise,
                                                Sset, getMidnight(Srise, Sset)));
                                        Log.d(DEUG_TAG, "done in service");
                                        Database.getInstance(context).close();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<TimeData> call, Throwable t) {

                        }
                    });
                }
            });
        }
    }

    private static Date getMidnight(Date sunriseT, Date sunsetT) {
        long midnight = sunriseT.getTime() - sunsetT.getTime();
        midnight = sunsetT.getTime() + (midnight / 2);
        Date date = new Date(midnight);
        date.setTime(date.getTime() + (12 * 60 * 60 * 1000));
        return date;
    }

    private static Date parseTime(String time) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormatUTC = new SimpleDateFormat("hh:mm:ss aa");
        simpleDateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return simpleDateFormatUTC.parse(time);
        } catch (ParseException e) {
//            Log.e(ERROR_TAG, "parseTime Error:" + e.getMessage());
            return null;
        }
    }

}
