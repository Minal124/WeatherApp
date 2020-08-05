package com.minal.weatherapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Typeface weatherFont;
    TextView cityField;
    TextView updatedField;
    TextView temperatureField;
    TextView descriptionField;
    TextView humidityField;
    TextView windField;
    TextView pressureField;
    TextView weatherIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityField = (TextView) findViewById(R.id.city);
        updatedField = (TextView) findViewById(R.id.last_updated);
        temperatureField = (TextView) findViewById(R.id.temperature);
        descriptionField = (TextView) findViewById(R.id.description);
        humidityField = (TextView) findViewById(R.id.humidity);
        windField = (TextView) findViewById(R.id.wind);
        pressureField = (TextView) findViewById(R.id.pressure);

        weatherIcon = (TextView) findViewById(R.id.weather_icon);
        weatherFont = Typeface.createFromAsset(getAssets(), "weather.ttf");
        weatherIcon.setTypeface(weatherFont);

        fetchWeatherData(new AppPreferences(this).getCity());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInputDialog();
            }
        });
    }

    private void fetchWeatherData(final String city) {
        new AsyncTask<String, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(String... params) {
                return NetworkFetch.getJson(city);
            }

            @Override
            protected void onPostExecute(JSONObject json) {
                if (json == null) {
                    Toast.makeText(MainActivity.this, getString(R.string.place_not_found), Toast.LENGTH_LONG).show();
                } else {
                    showWeather(json);
                }
            }
        }.execute();
    }

    private void showWeather(JSONObject json) {
        try {
            cityField.setText(capitalize(json.getString("name")) + ", " + json.getJSONObject("sys").getString("country"));

            JSONObject main = json.getJSONObject("main");
            temperatureField.setText(String.format("%d", main.getInt("temp")) + "°");
            humidityField.setText(main.getString("humidity") + "%");
            pressureField.setText(main.getString("pressure") + " hPa");

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            descriptionField.setText(capitalize(details.getString("description")));

            JSONObject wind = json.getJSONObject("wind");
            windField.setText(wind.getString("speed") + " mph");

            DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
            String updatedOn = df.format(new Date(json.getLong("dt") * 1000));
            updatedField.setText("Last updated: " + updatedOn);

            setWeatherIcon(details.getInt("id"), json.getJSONObject("sys").getLong("sunrise") * 1000,
                           json.getJSONObject("sys").getLong("sunset") * 1000);
        } catch (Exception e) {
            Log.e("SimpleWeather", "One or more fields not found in the JSON data");
        }
    }

    private void setWeatherIcon(int actualId, long sunrise, long sunset) {
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
            long currentTime = new Date().getTime();
            if (currentTime >= sunrise && currentTime < sunset) {
                icon = getString(R.string.weather_sunny);
            } else {
                icon = getString(R.string.weather_clear_night);
            }
        } else {
            switch (id) {
                case 2:
                    icon = getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = getString(R.string.weather_drizzle);
                    break;
                case 7:
                    icon = getString(R.string.weather_foggy);
                    break;
                case 8:
                    icon = getString(R.string.weather_cloudy);
                    break;
                case 6:
                    icon = getString(R.string.weather_snowy);
                    break;
                case 5:
                    icon = getString(R.string.weather_rainy);
                    break;
            }
        }
        weatherIcon.setText(icon);
    }

    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change city");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeCity(input.getText().toString());
            }
        });
        builder.show();
    }

    private void changeCity(String city) {
        fetchWeatherData(city);
        new AppPreferences(this).setCity(city);
    }

    private String capitalize(String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }
}
