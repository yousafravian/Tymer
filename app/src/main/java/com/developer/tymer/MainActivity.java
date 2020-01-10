package com.developer.tymer;


import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.developer.tymer.roomUtils.Database;
import com.developer.tymer.roomUtils.DateEntry;
import com.developer.tymer.roomUtils.RoomExecuters;
import com.developer.tymer.timeUtils.TimeData;
import com.hadadroid.splash.SplashBuilder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ImageView mSettings, mNetworkError, mSunBackground;
    private TextView mSunRise, mSunSet, mMidnight, mDate, mSunriseClock, mSunSetClock, mMidnightClock;
    private TextClock mMainClock;
    private Call<TimeData> mTimeCall;
    private TimeFetch mTimeFetch;
    private LinearLayout mLinearLayoutMain;
    private final String BASE_URL = getResources().getString(R.string.server_base_url);
    private Animation sunLoadingAnimation, layoutAppearAnimation, splashAnimation;
    private SplashBuilder.SplashTask splashTask;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Intent intentSettings;
    private final String DEBUG_TAG = "debug";
    private final String ERROR_TAG = "error";
    private Date timeData[] = new Date[3];
    private Database mDatabase;
    private RelativeLayout mRelativeLayout;

    //.......SharedPrefFields......>
    private String PREF_TIME_FORMAT = null;

    //.............................>

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setTheme();
        defaultSetup();
        layoutAppearAnimation = AnimationUtils.loadAnimation(this, R.anim.layout_load);
        sunLoadingAnimation = AnimationUtils.loadAnimation(this, R.anim.loading_sun_animation);
        splashAnimation = AnimationUtils.loadAnimation(this, R.anim.app_start_animation);
        @SuppressLint("SimpleDateFormat") DateFormat mDateFormat = new SimpleDateFormat("EEE MMMM, dd");
        mDate.setText(mDateFormat.format(Calendar.getInstance().getTime()));
        splashAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mLinearLayoutMain.startAnimation(layoutAppearAnimation);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        layoutAppearAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mLinearLayoutMain.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                swipeRefreshLayout.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        AnimatorSet set = new AnimatorSet();
        mSunBackground.startAnimation(splashAnimation);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mTimeFetch = retrofit.create(TimeFetch.class);
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (intentSettings != null) {
                    startActivity(intentSettings);
                } else {
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ApiRequest();
            }
        });
        swipeRefreshLayout.setEnabled(false);
    }

    private void setTheme() {
        Date currentTime = Calendar.getInstance().getTime();
        if (currentTime.getHours() > 18 || currentTime.getHours() < 5) {
//            Toast.makeText(this, Integer.toString(currentTime.getHours()), Toast.LENGTH_SHORT).show();
            mSunBackground.setImageDrawable(getResources().getDrawable(R.drawable.moon));
            mRelativeLayout.setBackground(getResources().getDrawable(R.drawable.darkbackground));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(Color.BLACK);
            }
        } else {
//            Toast.makeText(this, Integer.toString(currentTime.getHours()), Toast.LENGTH_SHORT).show();
            mSunBackground.setImageDrawable(getResources().getDrawable(R.drawable.sun));
            mRelativeLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
            }
        }
    }

    public void defaultSetup() {
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean is12Hours = mSharedPreferences.getBoolean(
                getResources().getString(R.string.pref_time_format_key)
                , false);

        setTimeFormat(is12Hours);


    }

    private void setTimeFormat(boolean is12Hours) {
        if (is12Hours) {
            PREF_TIME_FORMAT = "hh:mm aa";
        } else {
            PREF_TIME_FORMAT = "HH:mm";
        }
    }

    private void ApiRequest() {
        mTimeCall = mTimeFetch.getTimeData(31.571968, 74.3235584, "today");
        mSunBackground.startAnimation(sunLoadingAnimation);
        mSettings.setClickable(false);
        RoomExecuters.getsIntance().getNetworkIo().execute(new Runnable() {
            @Override
            public void run() {
                mTimeCall.enqueue(new Callback<TimeData>() {
                    @Override
                    public void onResponse(@NonNull Call<TimeData> call, @NonNull Response<TimeData> response) {
                        swipeRefreshLayout.setRefreshing(false);
                        mSunBackground.clearAnimation();
                        mSettings.setClickable(true);
                        TimeData results = response.body();
                        if (response.isSuccessful() && results != null && results.getStatus().equals(TimeData.STATUS_OK)) {
                            timeData[0] = parseTime(results.getResults().getSunrise());
                            timeData[1] = parseTime(results.getResults().getSunset());
                            if (timeData[0] != null && timeData[1] != null)
                                timeData[2] = getMidnight(timeData[0], timeData[1]);
                            if (mDatabase != null && mDatabase.isOpen()) {
                                try {
                                    RoomExecuters.getsIntance().getDiskIo().execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            mDatabase.datedao().insertDate(new DateEntry(timeData[0], timeData[1], timeData[2]));
                                        }
                                    });
                                    Log.d(DEBUG_TAG, "Data Written to DAO");
                                } catch (Exception e) {
                                    Log.d(DEBUG_TAG, "DATABASE WRITING ERROR---------->" + e.getMessage());
                                }

                            } else {
                                Log.d(DEBUG_TAG, "DATABASE NOT OPEN");
                            }
                            if (timeData != null)
                                updateUI(timeData);
                        } else {
                            Toast.makeText(MainActivity.this, response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TimeData> call, @NonNull Throwable t) {
                        mSunBackground.clearAnimation();
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                        mNetworkError.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    private Date parseTime(String time) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormatUTC = new SimpleDateFormat("hh:mm:ss aa");
        simpleDateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return simpleDateFormatUTC.parse(time);
        } catch (ParseException e) {
            Log.e(ERROR_TAG, "parseTime Error:" + e.getMessage());
            return null;
        }
    }

    private void updateUI(@NonNull Date[] timedata) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(PREF_TIME_FORMAT);
        mSunriseClock.setText(mSimpleDateFormat.format(timedata[0]));
        mSunSetClock.setText(mSimpleDateFormat.format(timedata[1]));
        mMidnightClock.setText(mSimpleDateFormat.format(timedata[2]));
    }

    private Date getMidnight(Date sunriseT, Date sunsetT) {
        long midnight = sunriseT.getTime() - sunsetT.getTime();
        midnight = sunsetT.getTime() + (midnight / 2);
        Date date = new Date(midnight);
        date.setTime(date.getTime() + (12 * 60 * 60 * 1000));
        return date;
    }

    @SuppressLint("SimpleDateFormat")
    private String convertTime(String time) {
        SimpleDateFormat simpleDateFormatUTC = new SimpleDateFormat(PREF_TIME_FORMAT);
        try {
            Date timeUTC = simpleDateFormatUTC.parse(time);
            if (timeUTC != null) {
//                Toast.makeText(this, new SimpleDateFormat(PREF_TIME_FORMAT).format(timeUTC), Toast.LENGTH_SHORT).show();
                return new SimpleDateFormat(PREF_TIME_FORMAT).format(timeUTC);
            } else {
                return time;
            }
        } catch (ParseException e) {
            return time;
        }
    }

    private void initializeViews() {
        mRelativeLayout = findViewById(R.id.layout_background);
        mDatabase = Database.getInstance(this);
        intentSettings = new Intent(this, SettingsActivity.class);
        swipeRefreshLayout = findViewById(R.id.SwipeRefreshLayout);
        mSunRise = findViewById(R.id.mSunRise);
        mSunSet = findViewById(R.id.mSunSet);
        mMidnight = findViewById(R.id.mMidnight);
        mDate = findViewById(R.id.mDate);
        mSunriseClock = findViewById(R.id.mSunRiseClock);
        mSunSetClock = findViewById(R.id.mSunSetClock);
        mMidnightClock = findViewById(R.id.mMidnightClock);
        mMainClock = findViewById(R.id.mMainClock);
        mLinearLayoutMain = findViewById(R.id.linearLayoutMain);
        mSettings = findViewById(R.id.mSettings);
        mNetworkError = findViewById(R.id.networkErrorIcon);
        mSunBackground = findViewById(R.id.sunBackground);
        splashTask = new SplashBuilder.SplashTask() {
            @Override
            public void run(SplashBuilder.OnCompleteListener listener) {
                ApiRequest();
            }
        };
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        if (s.equals(getString(R.string.pref_time_format_key))) {
            boolean is12Hours = sharedPreferences.getBoolean(s, getResources().getBoolean(R.bool.pref_time_format_default_value));
            setTimeFormat(is12Hours);
            updateUI(timeData);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDatabase != null) {
            List<DateEntry> savedEntriesDates = mDatabase.datedao().getLastUpdates();
            Log.d(DEBUG_TAG, "SIZE OF DB ARRAY---------->" + savedEntriesDates.size());
            if (!savedEntriesDates.isEmpty()) {
                Date[] temp = {
                        savedEntriesDates.get(savedEntriesDates.size() - 1).getSunRise(),
                        savedEntriesDates.get(savedEntriesDates.size() - 1).getSunSet(),
                        savedEntriesDates.get(0).getMidnight()
                };
                timeData = temp.clone();
                updateUI(temp);
            } else {
                Log.d(DEBUG_TAG, "No data found in Database");
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_error)
                        .setTitle("No Previous Data Found in Database")
                        .setCancelable(false)
                        .setNeutralButton("Refresh", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ApiRequest();
                            }
                        })
                        .show();
            }

        }
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        mDatabase.close();
        Log.d(DEBUG_TAG, "DATABASE CLOSED");
//        mDatabase.
//        Toast.makeText(this, "DB Closed", Toast.LENGTH_SHORT).show();
    }
}
