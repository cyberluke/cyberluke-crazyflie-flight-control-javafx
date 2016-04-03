package se.bitcraze.crazyflie.client.weather;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import se.bitcraze.crazyflie.client.util.Url;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
public class Location {
    JSONObject IpInfoGeo;
    public Location() throws IOException {
        IpInfoGeo = new JSONObject(Url.getBody("https://ipinfo.io/geo"));
    }
    public String getCity() {
        return IpInfoGeo.optString("city");
    }
    public String getLocation() {
        //JSONArray jsonArray = jsonObject.getJSONArray("someJsonArray");
        //String value = jsonArray.optJSONObject(i).getString("someJsonValue");
        return IpInfoGeo.optString("loc");
    }
    public String getLatitude() {
        return IpInfoGeo.optString("loc").split(",")[0];
    }
    public String getLongitude() {
        return IpInfoGeo.optString("loc").split(",")[1];
    }
}
