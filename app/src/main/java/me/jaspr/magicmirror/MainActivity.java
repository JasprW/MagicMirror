package me.jaspr.magicmirror;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.johnhiott.darkskyandroidlib.ForecastApi;
import com.johnhiott.darkskyandroidlib.RequestBuilder;
import com.johnhiott.darkskyandroidlib.models.Request;
import com.johnhiott.darkskyandroidlib.models.WeatherResponse;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import at.theengine.android.simple_rss2_android.RSSItem;
import at.theengine.android.simple_rss2_android.SimpleRss2Parser;
import at.theengine.android.simple_rss2_android.SimpleRss2ParserCallback;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    private static double mLatitude = 0.0;
    private static double mLongitude = 0.0;

    private TextView mDate;

    private static TextView mWeatherSummary;
    private static TextView mTemperature;
    private static ImageView mWeatherIcon;

    private Boolean isSI = true;
    private int mFeedNo = 0;

    private LinearLayout mNews;

    private TextView mNews1;
    private TextView mNews2;
    private TextView mNews3;
    private TextView mNews4;
    private TextView mNews5;

    //Add Your Feed List
    private String[] mFeedList;

    final public static int REQUEST_CODE_ASK_CALL_PHONE = 123;

    Handler handler=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            //要做的事情
            if (isSI) {
                updateWeather(Request.Units.SI);
            } else {
                updateWeather(Request.Units.US);
            }

            getNewsUpdate(mFeedList[mFeedNo]);

            //Toast.makeText(MainActivity.this, "Updating data", Toast.LENGTH_SHORT).show();
            handler.postDelayed(this, 3600000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set Immersive Mode
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
            decorView.setSystemUiVisibility(uiOptions);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mFeedList = getResources().getStringArray(R.array.feedlist);

        String mForecastApi = getString(R.string.dark_sky_api_key);

        ForecastApi.create(mForecastApi);

        mTemperature = (TextView)findViewById(R.id.temperature);
        mWeatherSummary = (TextView)findViewById(R.id.weather_summary);
        mWeatherIcon = (ImageView)findViewById(R.id.weather_icon);

        mDate = (TextView) findViewById(R.id.date);

        mNews = (LinearLayout)findViewById(R.id.news);

        mNews1 = (TextView)findViewById(R.id.news1);
        mNews2 = (TextView)findViewById(R.id.news2);
        mNews3 = (TextView)findViewById(R.id.news3);
        mNews4 = (TextView)findViewById(R.id.news4);
        mNews5 = (TextView)findViewById(R.id.news5);

        mDate.setText(getDate());

        updateWeather(Request.Units.SI);

        getNewsUpdate(mFeedList[mFeedNo]);

        mNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFeedNo++;
                getNewsUpdate(mFeedList[mFeedNo % mFeedList.length]);
                Toast.makeText(MainActivity.this, "Changing news channel", Toast.LENGTH_SHORT).show();
            }
        });

        mTemperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSI) {
                    updateWeather(Request.Units.US);
                    Toast.makeText(MainActivity.this, "Showing at ℉", Toast.LENGTH_SHORT).show();
                }else {
                    updateWeather(Request.Units.SI);
                    Toast.makeText(MainActivity.this, "Showing at ℃", Toast.LENGTH_SHORT).show();
                }
                isSI = !isSI;
            }
        });

        //Run Every 1 Hour
        handler.postDelayed(runnable, 3600000);

    }


    //Update Weather Info
    private void updateWeather(Request.Units units) {
        getLocation();

        final RequestBuilder weather = new RequestBuilder();

        Request request = new Request();
        request.setLat(Double.toString(mLatitude));
        request.setLng(Double.toString(mLongitude));
        request.setUnits(units);
        request.setLanguage(Request.Language.ENGLISH);
        request.addExcludeBlock(Request.Block.MINUTELY);
        request.addExcludeBlock(Request.Block.DAILY);
        request.addExcludeBlock(Request.Block.ALERTS);
        request.addExcludeBlock(Request.Block.FLAGS);

        weather.getWeather(request, new Callback<WeatherResponse>() {
            @Override
            public void success(WeatherResponse weatherResponse, Response response) {
                //Do something
                mTemperature.setText((int)weatherResponse.getCurrently().getTemperature() + "°");
                mWeatherSummary.setText(weatherResponse.getHourly().getSummary());
                String icon = weatherResponse.getCurrently().getIcon();
                switch (icon) {
                    case "clear-day":
                        mWeatherIcon.setImageResource(R.drawable.clear_day);
                        break;
                    case "clear-night":
                        mWeatherIcon.setImageResource(R.drawable.clear_night);
                        break;
                    case "rain":
                        mWeatherIcon.setImageResource(R.drawable.rain);
                        break;
                    case "snow":
                        mWeatherIcon.setImageResource(R.drawable.snow);
                        break;
                    case "sleet":
                        mWeatherIcon.setImageResource(R.drawable.sleet);
                        break;
                    case "wind":
                        mWeatherIcon.setImageResource(R.drawable.windy);
                        break;
                    case "fog":
                        mWeatherIcon.setImageResource(R.drawable.fog);
                        break;
                    case "cloudy":
                        mWeatherIcon.setImageResource(R.drawable.cloudy);
                        break;
                    case "partly-cloudy-day":
                        mWeatherIcon.setImageResource(R.drawable.partly_cloudy_day);
                        break;
                    case "partly-cloudy-night":
                        mWeatherIcon.setImageResource(R.drawable.partly_cloudy_night);
                        break;
                    case "hail":
                        mWeatherIcon.setImageResource(R.drawable.hail);
                        break;
                    case "thunderstorm":
                        mWeatherIcon.setImageResource(R.drawable.thunder_storm);
                        break;
                    case "tornado":
                        mWeatherIcon.setImageResource(R.drawable.tornado);
                        break;
                    default:
                        mWeatherIcon.setImageResource(R.drawable.weather_na);
                        break;
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Toast.makeText(MainActivity.this, "HOURLY ERROR", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Get Location
    private void getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
        }
    }

    //Get Date Info
    private String getDate() {
        SimpleDateFormat formatDayOfMonth = new SimpleDateFormat("EEEE", Locale.US);
        Calendar now = Calendar.getInstance();
        int dayOfMonth = now.get(Calendar.DAY_OF_MONTH);
        int monthOfYear = now.get(Calendar.MONTH);
        return new String(formatDayOfMonth.format(now.getTime()) + "\n" + getMonthOfYear(monthOfYear) + " " + dayOfMonth);
    }

    //Get Month
    private String getMonthOfYear(final int n) {
        switch (n) {
            case 0:
                return "January";
            case 1:
                return "February";
            case 2:
                return "March";
            case 3:
                return "April";
            case 4:
                return "May";
            case 5:
                return "June";
            case 6:
                return "July";
            case 7:
                return "August";
            case 8:
                return "September";
            case 9:
                return "October";
            case 10:
                return "November";
            case 11:
                return "December";
        }
        return null;
    }

    //Get News List
    private void getNewsUpdate(String feed) {
        SimpleRss2Parser parser = new SimpleRss2Parser(feed,
                new SimpleRss2ParserCallback() {
                    @Override
                    public void onFeedParsed(List<RSSItem> items) {
                        /**
                         for(int i = 0; i < items.size(); i++){
                         Log.d("SimpleRss2ParserDemo",items.get(i).getTitle());
                         }
                         */
                        mNews1.setText(items.get(0).getTitle());
                        mNews2.setText(items.get(1).getTitle());
                        mNews3.setText(items.get(2).getTitle());
                        mNews4.setText(items.get(3).getTitle());
                        mNews5.setText(items.get(4).getTitle());
                    }
                    @Override
                    public void onError(Exception ex) {
                        Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
        parser.parseAsync();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

}