package launchserver.helpers;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.WriterConfig;
import launcher.helper.IOHelper;
import launcher.helper.LogHelper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HTTPRequestHelper {
    private static HttpURLConnection makeRequest(URL url, String requestMethod) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod(requestMethod);
        return connection;
    }

    public static JsonObject makeAuthlibRequest(URL url, JsonObject request, String requestType) throws IOException
    {
        HttpURLConnection connection = request == null ?
                (HttpURLConnection) IOHelper.newConnection(url) :
                makeRequest(url, "POST");

        // Make request
        if (request != null)
        {
            connection.setRequestProperty("Content-Type", "application/json");
            try (OutputStream output = connection.getOutputStream())
            {
                output.write(request.toString(WriterConfig.MINIMAL).getBytes(StandardCharsets.UTF_8));
            }
        }
        int statusCode = connection.getResponseCode();

        // Read response
        InputStream errorInput = connection.getErrorStream();
        try (InputStream input = errorInput == null ? connection.getInputStream() : errorInput)
        {
            String charset = connection.getContentEncoding();
            Charset charsetObject = charset == null ?
                    IOHelper.UNICODE_CHARSET : Charset.forName(charset);

            // Parse response
            String json = new String(IOHelper.read(input), charsetObject);
            LogHelper.subDebug("Raw " + requestType + " response: '" + json + '\'');

            if (200 <= statusCode && statusCode < 300)
            {
                return Json.parse(json).asObject();
            }
            else
            {
                return json.isEmpty() ? null : Json.parse(json).asObject();
            }
        }
    }

    public static boolean fileExist(URL url) throws IOException {
        HttpURLConnection request = makeRequest(url, "HEAD");
        int responseCode = request.getResponseCode();
        return responseCode >= 200 && responseCode < 300;
    }

    public static String getFile(URL url) throws IOException {
        HttpURLConnection request = makeRequest(url, "GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    public static int authJoinRequest(URL url, JsonObject request, String authType) throws IOException
    {
        HttpURLConnection connection = request == null ?
                (HttpURLConnection) IOHelper.newConnection(url) :
                makeRequest(url, "POST");

        // Make request
        if (request != null)
        {
            connection.setRequestProperty("Content-Type", "application/json");
            try (OutputStream output = connection.getOutputStream())
            {
                output.write(request.toString(WriterConfig.MINIMAL).getBytes(StandardCharsets.UTF_8));
            }
        }
        int statusCode = connection.getResponseCode();
        LogHelper.subDebug("Raw " + authType + " status сode: '" + statusCode + '\'');
        return statusCode;
    }
}
