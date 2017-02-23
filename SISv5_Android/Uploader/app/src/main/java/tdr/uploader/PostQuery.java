package tdr.uploader;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by mzhang on 12/4/16.
 */

public class PostQuery {
    public static boolean PostToPHP(String UrlIn, String QueryIn) {
        HttpURLConnection conn = null;
        boolean response = false;
        try {
            URL url = new URL(UrlIn);
            String agent = "Applet";
            String query = "query=" + QueryIn;
            String type = "application/x-www-form-urlencoded";

            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", agent);
            conn.setRequestProperty("Content-Type", type);
            conn.setRequestProperty("Content-Length", "" + query.length());

//            Log.d("PostQuery", "Ready to post: <" + query + ">");
            OutputStream out = conn.getOutputStream();
            out.write(query.getBytes());
            out.flush();

            conn.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            String reply = "";
            while ((inputLine = in.readLine()) != null) {
                Log.d("PostQuery", inputLine);
                reply += inputLine;
            }
            if (reply.equals("success")) {
                response = true;
            }
            in.close();
        } catch (Exception e) {
            Log.d("PostQuery", e.toString());
        } finally {
            conn.disconnect();
        }
        return response;
    }
}
