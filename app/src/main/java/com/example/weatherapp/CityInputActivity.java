package com.example.weatherapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;



public class CityInputActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String CITY_LIST_KEY = "city_list";
    private static final String CITY_KEY = "city";

    private SharedPreferences settings;
    private EditText editCityName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_input);

        settings = getSharedPreferences(PREFS_NAME, 0);

        editCityName = findViewById(R.id.edit_city_name);
        Button btnAddCity = findViewById(R.id.btn_add_city);
        Button btnCancel = findViewById(R.id.btn_cancel);

        btnAddCity.setOnClickListener(v -> addCity());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void addCity() {
        String city = editCityName.getText().toString().trim();

        if (isValidCityName(city)) {
            if (isCityInFile(city, "cities_all.txt") || isCityInFile(city, "cities_ru.txt")) {
                city = city.replace(" ", "");
                SharedPreferences.Editor editor = settings.edit();

                editor.putString(CITY_KEY, city);

                Set<String> citySet = settings.getStringSet(CITY_LIST_KEY, new HashSet<>());
                Set<String> newCitySet = new HashSet<>(citySet);
                newCitySet.add(city);

                editor.putStringSet(CITY_LIST_KEY, newCitySet);
                editor.apply();

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, R.string.error + city, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.errorValid, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidCityName(String city) {
        if (city.isEmpty()) {
            return false;
        }
        char firstChar = city.charAt(0);
        if (!Character.isUpperCase(firstChar)) {
            return false;
        }
        return true;
    }

    private boolean isCityInFile(String cityName, String fileName) {
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = getAssets().open(fileName);
            reader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append(" ");
            }

            String[] cities = fileContent.toString().split("\\s+");
            for (String city : cities) {
                if (city.trim().equals(cityName)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}