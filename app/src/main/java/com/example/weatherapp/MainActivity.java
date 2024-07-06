package com.example.weatherapp;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String CITY_LIST_KEY = "city_list";
    private static final String CITY_KEY = "city";
    private static final int REQUEST_CODE_ADD_CITY = 1;
    private static final String key = "4192951698dba4198b49099cbd0ceae2";

    private SharedPreferences settings;
    private ArrayList<String> cityList;
    private ArrayAdapter<String> adapter;
    private String selectedCity;
    private SharedPreferences.Editor editor;
    private String city;
    private boolean isMenuOpen = false;
    private boolean isDarkTheme = false;

    private static final int numberOfHours = 9;
    private static final int numberOfDays = 5;

    private LinearLayout slidingMenu;
    private ListView cityListView;
    private ImageButton menuButton;
    private ImageButton menuBack;
    private Button addCityButton;
    private View transparentView;
    private ImageButton theme_button;
    private TextView city_text;
    private TextView current_temperature;
    private ImageView current_temperature_image;
    private TextView weather_description;
    private TextView feels_like;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.primaryVariant));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.primary));

        settings = getSharedPreferences(PREFS_NAME, 0);
        city = settings.getString(CITY_KEY, null);
        if (city == null) {
            Intent intent = new Intent(this, FirstCityInputActivity.class);
            startActivity(intent);
            finish();
        } else {

            InitializingVariables();

            ClickHandler();

            dataUpdates();
        }
    }


    private void InitializingVariables() {


        Set<String> citySet = settings.getStringSet(CITY_LIST_KEY, new HashSet<>());
        cityList = new ArrayList<>(citySet);

        cityListView = findViewById(R.id.city_list);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cityList);
        cityListView.setAdapter(adapter);

        menuButton = findViewById(R.id.menu_button);
        menuBack = findViewById(R.id.menu_back);
        addCityButton = findViewById(R.id.add_city_button);
        slidingMenu = findViewById(R.id.sliding_menu);
        transparentView = findViewById(R.id.transparent_view);
        city_text = findViewById(R.id.city_text);
        theme_button = findViewById(R.id.theme_button);
        current_temperature = findViewById(R.id.current_temperature);
        current_temperature_image = findViewById(R.id.current_temperature_image);
        weather_description = findViewById(R.id.weather_description);
        feels_like = findViewById(R.id.feels_like);

        isDarkTheme = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        theme_button.setImageResource(isDarkTheme ? R.drawable.sun : R.drawable.moon);
    }

    private void ClickHandler() {
        menuButton.setOnClickListener(v -> toggleMenu(slidingMenu));
        menuBack.setOnClickListener(v -> toggleMenu(slidingMenu));
        transparentView.setOnClickListener(v -> toggleMenu(slidingMenu));
        addCityButton.setOnClickListener(v -> showAddCityDialog());

        cityListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedCity = cityList.get(position);
            editor = settings.edit();
            editor.putString(CITY_KEY, selectedCity);
            editor.apply();
            city = selectedCity;

            dataUpdates();

            toggleMenu(slidingMenu);
        });

        theme_button.setOnClickListener(v -> {
            int nightMode = AppCompatDelegate.getDefaultNightMode();
            if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            recreate();
        });
    }


    @SuppressLint("StaticFieldLeak")
    private class GetURLDateCurrent extends AsyncTask<String, String, String> {
        public boolean running = false;

        protected void onPreExecute() {
            super.onPreExecute();
            running = true;
        }


        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while((line = reader.readLine()) != null)
                    buffer.append(line).append("\n");

                return  buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(connection != null)
                    connection.disconnect();

                try {
                    if(reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }


        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject jsonObject = new JSONObject((result));

                city_text.setText(city);

                current_temperature.setText(jsonObject.getJSONObject("main").getDouble("temp") + "°");

                JSONObject weatherObject = jsonObject.getJSONArray("weather").getJSONObject(0);
                String icon = "i" + weatherObject.getString("icon");
                int iconID = getResources().getIdentifier(icon, "drawable", getPackageName());
                current_temperature_image.setImageResource(iconID);

                String description = weatherObject.getString("description");
                weather_description.setText(description);

                feels_like.setText(getString(R.string.feelsLike) + " " + jsonObject.getJSONObject("main").getDouble("feels_like") + "°");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            running = false;
        }
    }


    private class GetURLDateForecast extends AsyncTask<String, String, String> {
        public boolean running = false;

        protected void onPreExecute() {
            super.onPreExecute();
            running = true;
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while((line = reader.readLine()) != null)
                    buffer.append(line).append("\n");

                return  buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(connection != null)
                    connection.disconnect();

                try {
                    if(reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject jsonObject = new JSONObject((result));
                    HourlyWeatherFill(jsonObject);
                    DailyWeatherFill(jsonObject);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            running = false;
        }
    }

    protected void dataUpdates() {
        String urlMC = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + key + "&units=metric&lang=ru";
        new GetURLDateCurrent().execute(urlMC);
        String urlMF = "https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&cnt=40&appid=" + key + "&units=metric&lang=ru";
        new GetURLDateForecast().execute(urlMF);
    }

    private void toggleMenu(LinearLayout menu) {
        ObjectAnimator animator;
        if (isMenuOpen) {
            animator = ObjectAnimator.ofFloat(menu, "translationX", 0f, -menu.getWidth());
            transparentView.setVisibility(View.GONE);
        } else {
            animator = ObjectAnimator.ofFloat(menu, "translationX", -menu.getWidth(), 0f);
            transparentView.setVisibility(View.VISIBLE);
        }
        animator.setDuration(500);
        animator.start();
        isMenuOpen = !isMenuOpen;
    }

    private void showAddCityDialog() {
        Intent intent = new Intent(this, CityInputActivity.class);
        startActivityForResult(intent, REQUEST_CODE_ADD_CITY);
    }



    @SuppressLint("SetTextI18n")
    private void HourlyWeatherFill(JSONObject jsonObject) {
        HorizontalScrollView hscrollView = findViewById(R.id.hourly_weather_scroll);
        LinearLayout hourlyWeatherLayout = (LinearLayout) hscrollView.getChildAt(0);
        hourlyWeatherLayout.removeAllViews();
        int timezone;
        try {
            timezone = jsonObject.getJSONObject("city").getInt("timezone");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < numberOfHours; i++) {
            try {
                JSONObject weatherObject = jsonObject.getJSONArray("list").getJSONObject(i);

                // новый LinearLayout для блока погоды
                LinearLayout weatherBlock = new LinearLayout(this);
                weatherBlock.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(32, 0, 32, 0);
                weatherBlock.setLayoutParams(layoutParams);

                // TextView для времени
                TextView timeTextView = new TextView(this);
                timeTextView.setText(parseData(weatherObject.getString("dt_txt"), timezone / 3600, 1));
                timeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                timeTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                timeTextView.setTextColor(ContextCompat.getColor(this, R.color.text));
                weatherBlock.addView(timeTextView);

                //ImageView для изображения
                ImageView weatherIcon = new ImageView(this);
                JSONObject object = weatherObject.getJSONArray("weather").getJSONObject(0);
                String icon = "i" + object.getString("icon");
                int iconID = getResources().getIdentifier(icon, "drawable", getPackageName());
                weatherIcon.setImageResource(iconID);
                int sizeInPixels = (int) (70 * getResources().getDisplayMetrics().scaledDensity + 0.5f);
                LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(sizeInPixels, sizeInPixels);
                imageLayoutParams.setMargins(0, 8, 0, 8);
                weatherIcon.setLayoutParams(imageLayoutParams);
                weatherBlock.addView(weatherIcon);

                //TextView для температуры
                TextView tempTextView = new TextView(this);
                tempTextView.setText(weatherObject.getJSONObject("main").getDouble("temp") + "°");
                tempTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                tempTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                tempTextView.setTextColor(ContextCompat.getColor(this, R.color.text));
                weatherBlock.addView(tempTextView);

                hourlyWeatherLayout.addView(weatherBlock);

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void DailyWeatherFill(JSONObject jsonObject) {
        ScrollView scrollView = findViewById(R.id.daily_weather_scroll);
        LinearLayout dailyWeatherLayout = (LinearLayout) scrollView.getChildAt(0);
        dailyWeatherLayout.removeAllViews();
        String day;
        try {
            JSONObject weatherObject = jsonObject.getJSONArray("list").getJSONObject(0);
            day = parseData(weatherObject.getString("dt_txt"), 0, 3);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        for (int i = 1; i < 40; i++) {
            try {
                JSONObject weatherObject = jsonObject.getJSONArray("list").getJSONObject(i);
                String nextDay = parseData(weatherObject.getString("dt_txt"), 0, 3);
                if(!nextDay.equals(day)) {



                    // новый LinearLayout для блока дня
                    LinearLayout dayBlock = new LinearLayout(this);
                    dayBlock.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams dayBlockParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    dayBlockParams.setMargins(0, 0, 0, 32); // layout_marginBottom
                    dayBlock.setLayoutParams(dayBlockParams);
                    dayBlock.setPadding(32, 32, 32, 32);
                    dayBlock.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary));
                    dayBlock.setElevation(8f);


                    //  TextView для даты
                    TextView dateTextView = new TextView(this);
                    dateTextView.setLayoutParams(new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                    dateTextView.setText(parseData(weatherObject.getString("dt_txt"), 0, 2));
                    dateTextView.setGravity(Gravity.CENTER_VERTICAL);
                    dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    dateTextView.setTextColor(ContextCompat.getColor(this, R.color.text2));
                    ((LinearLayout.LayoutParams) dateTextView.getLayoutParams()).setMarginEnd(20);
                    dayBlock.addView(dateTextView);

                    //  ImageView для изображения
                    ImageView weatherIcon = new ImageView(this);
                    LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 40, getResources().getDisplayMetrics()),
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 40, getResources().getDisplayMetrics()));
                    imageParams.setMarginEnd(20);
                    weatherIcon.setLayoutParams(imageParams);
                    int val = i + 4;
                    if(val >= 40) {val = 39;}
                    JSONObject object = jsonObject.getJSONArray("list").getJSONObject(val).getJSONArray("weather").getJSONObject(0);
                    String icon = "i" + object.getString("icon");
                    int iconID = getResources().getIdentifier(icon, "drawable", getPackageName());
                    weatherIcon.setImageResource(iconID);
                    weatherIcon.setScaleX(1.5f);
                    weatherIcon.setScaleY(1.5f);
                    weatherIcon.setContentDescription("image");
                    dayBlock.addView(weatherIcon);

                    //  LinearLayout для дневной температуры
                    LinearLayout dayTempBlock = new LinearLayout(this);
                    dayTempBlock.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout.LayoutParams dayTempBlockParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    dayTempBlockParams.setMarginEnd(20);
                    dayTempBlock.setLayoutParams(dayTempBlockParams);

                    TextView dayTimeTextView = new TextView(this);
                    dayTimeTextView.setText("День");
                    dayTimeTextView.setTextSize(18);
                    dayTimeTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                    dayTimeTextView.setTextColor(ContextCompat.getColor(this, R.color.text3));
                    dayTempBlock.addView(dayTimeTextView);

                    TextView dayTempTextView = new TextView(this);
                    dayTempTextView.setTextColor(ContextCompat.getColor(this, R.color.text2));
                    dayTempTextView.setText(jsonObject.getJSONArray("list").getJSONObject(val).getJSONObject("main").getDouble("temp") + "°");
                    dayTempTextView.setTextSize(20);
                    dayTempTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                    dayTempBlock.addView(dayTempTextView);

                    dayBlock.addView(dayTempBlock);

                    //  LinearLayout для ночной температуры
                    LinearLayout nightTempBlock = new LinearLayout(this);
                    nightTempBlock.setOrientation(LinearLayout.VERTICAL);
                    nightTempBlock.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                    TextView nightTimeTextView = new TextView(this);
                    nightTimeTextView.setText("Ночь");
                    nightTimeTextView.setTextSize(18);
                    nightTimeTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                    nightTimeTextView.setTextColor(ContextCompat.getColor(this, R.color.text3));
                    nightTempBlock.addView(nightTimeTextView);

                    TextView nightTempTextView = new TextView(this);
                    nightTempTextView.setTextColor(ContextCompat.getColor(this, R.color.text3));
                    nightTempTextView.setText(weatherObject.getJSONObject("main").getDouble("temp") + "°");
                    nightTempTextView.setTextSize(20);
                    nightTempTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                    nightTempBlock.addView(nightTempTextView);

                    dayBlock.addView(nightTempBlock);

                    dailyWeatherLayout.addView(dayBlock);
                }
                day = nextDay;
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String parseData(String dateTimeString, int deltaTimeInHours, int flag) {
        if (!dateTimeString.isEmpty()) {
            // Исходный формат даты и времени
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // Формат для вывода только времени
            SimpleDateFormat timeFormat  = new SimpleDateFormat("HH:mm");
            // Формат для вывода даты
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM\nEEEE", new Locale("ru", "RU"));
            // Формат для вывода дня
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd");

            try {
                Date date = originalFormat.parse(dateTimeString);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.HOUR, deltaTimeInHours);

                Date newDate = calendar.getTime();

                if (flag == 1) {
                    return timeFormat.format(newDate);
                } else if (flag == 2){
                    return dateFormat.format(newDate);
                } else if (flag == 3){
                    return dayFormat.format(newDate);
                }
            } catch (ParseException e) {
                e.printStackTrace();
                return "N/A";
            }
        }
        return "N/A";
    }
}