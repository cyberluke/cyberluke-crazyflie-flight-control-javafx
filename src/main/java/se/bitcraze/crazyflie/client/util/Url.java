package se.bitcraze.crazyflie.client.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Lukas on 4/20/14.
 */
public class Url {
    public static String getBody(String urlParam) throws IOException {
        URL url = new URL(urlParam);
        URLConnection con = url.openConnection();
        InputStream in = con.getInputStream();
        String encoding = con.getContentEncoding();
        encoding = encoding == null ? "UTF-8" : encoding;
        return IOUtils.toString(in, encoding);
    }
}
