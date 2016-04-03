package se.bitcraze.crazyflie.client.weather;

import org.json.JSONArray;
import org.json.JSONObject;
import se.bitcraze.crazyflie.client.util.Url;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/*
 *  Copyright (C) 2014 Lukas Satin
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
public class Weather {
    private String APIURL = "http://api.openweathermap.org/data/2.5/";
    private String IMGURL = "http://www.openweathermap.org/img/w/%s.png";
    private JSONObject WeatherInfo;
    private JSONObject ForecastInfo;

    public Weather(Location myLocation) throws IOException {
        getByLocation(myLocation);
    }

    public Weather() {

    }

    public Weather getByCity(String city) throws IOException {
        if (WeatherInfo == null) {
            WeatherInfo = new JSONObject(Url.getBody(String.format("%sweather?q=%s&units=metric", APIURL, city)));
        }
        return this;
    }

    public Weather getByLocation(Location loc) throws IOException {
        if (WeatherInfo == null) {
            WeatherInfo = new JSONObject(Url.getBody(String.format("%sweather?lat=%s9&lon=%s&units=metric", APIURL, loc.getLatitude(), loc.getLongitude())));
        }
        return this;
    }

    public Weather getForecastByCity(String city) throws IOException {
        if (ForecastInfo == null) {
            ForecastInfo = new JSONObject(Url.getBody(String.format("%sforecast?q=%s&units=metric", APIURL, city)));
        }
        return this;
    }

    public Weather getForecastByLocation(Location loc) throws IOException {
        if (ForecastInfo == null) {
            ForecastInfo = new JSONObject(Url.getBody(String.format("%sforecast?lat=%s9&lon=%s&units=metric", APIURL, loc.getLatitude(), loc.getLongitude())));
        }
        return this;
    }

    public String getCloudsInfo() {
        JSONArray jsonArray = WeatherInfo.getJSONArray("weather");
        return jsonArray.optJSONObject(0).getString("description");
    }

    public int getCloudsAlt() {
        JSONArray jsonArray = WeatherInfo.getJSONArray("clouds");
        return jsonArray.optJSONObject(0).getInt("all");
    }

    public String getCloudsImagePath() {
        JSONArray jsonArray = WeatherInfo.getJSONArray("weather");
        return String.format(IMGURL, jsonArray.optJSONObject(0).getString("icon"));
    }

    public int getTemperatureActual() {
        JSONObject jsonArray = WeatherInfo.getJSONObject("main");
        return jsonArray.getInt("temp");
    }

    public int getTemperatureMin() {
        JSONObject jsonArray = WeatherInfo.getJSONObject("main");
        return jsonArray.getInt("temp_min");
    }

    public int getTemperatureMax() {
        JSONObject jsonArray = WeatherInfo.getJSONObject("main");
        return jsonArray.getInt("temp_max");
    }

    public int getPressure() {
        JSONObject jsonArray = WeatherInfo.getJSONObject("main");
        return jsonArray.getInt("pressure");
    }

    public String getPressureTxt() {
        JSONObject jsonArray = WeatherInfo.getJSONObject("main");
        return jsonArray.getInt("pressure") + " hpa";
    }

    public int getHumidity() {
        JSONObject jsonArray = WeatherInfo.getJSONObject("main");
        return jsonArray.getInt("humidity");
    }

    public String getHumidityTxt() {
        JSONObject jsonArray = WeatherInfo.getJSONObject("main");
        return jsonArray.getInt("humidity") + " %";
    }

    public int getWindSpeed() {
        JSONObject jsonArray = WeatherInfo.getJSONObject("wind");
        return jsonArray.getInt("speed");
    }

    public String getWindSpeedTxt() {
        JSONObject jsonArray = WeatherInfo.getJSONObject("wind");
        return jsonArray.getInt("speed") + " m/s";
    }

    public int getWindAngle() {
        JSONObject jsonArray = WeatherInfo.getJSONObject("wind");
        return jsonArray.getInt("deg");
    }

    public String getCityName() {
        return WeatherInfo.getString("name");
    }

    public String getForecastDatetime(int num) {
        JSONArray jsonArray = ForecastInfo.getJSONArray("list");
        return jsonArray.optJSONObject(num).getString("dt_txt");
    }

    public String getForecastHour(int num) {
        JSONArray jsonArray = ForecastInfo.getJSONArray("list");
        int unixSeconds = jsonArray.optJSONObject(num).getInt("dt");
        Date date = new Date(unixSeconds*1000L); // *1000 is to convert seconds to milliseconds
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); // the format of your date
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss"); // the format of your date
        //sdf.setTimeZone(TimeZone.getTimeZone("GMT-4"));
        return sdf.format(date);
    }

    public String getForecastImagePath(int num) {
        JSONArray jsonArray = ForecastInfo.getJSONArray("list");
        JSONArray jsonArray2 = jsonArray.optJSONObject(num).getJSONArray("weather");
        return String.format(IMGURL, jsonArray2.optJSONObject(0).getString("icon"));
    }
}
