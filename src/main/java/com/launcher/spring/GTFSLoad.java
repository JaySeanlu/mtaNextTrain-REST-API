package com.launcher.spring;

//Maven dependencies
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.util.JsonFormat;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.NyctSubway;

public class GTFSLoad {

    private GTFSLoad(){}

    public static List<JSONObject> readGTFS(String urlString, String apiKey) throws IOException {
        URL baseURL = new URL(urlString);
        List<JSONObject> trips = new ArrayList<>();
        HttpURLConnection connection = (HttpURLConnection) baseURL.openConnection();
        connection.setRequestProperty("x-api-key", apiKey);
        connection.setRequestMethod("GET");

        int status = connection.getResponseCode();
        if(status == 403) {
            throw new AccessDeniedException("Permission Denied - Invalid API Key");
        }

        else if (status != 200) {
            throw new IOException("Bad Request");
        }

        InputStream response = connection.getInputStream();
        ExtensionRegistry registry = ExtensionRegistry.newInstance();
        registry.add(NyctSubway.nyctFeedHeader);
        registry.add(NyctSubway.nyctStopTimeUpdate);
        registry.add(NyctSubway.nyctTripDescriptor);
        GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(response, registry);

        for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
            if (entity.hasTripUpdate()) {
                String jsonFormat = JsonFormat.printer().print(entity.getTripUpdate());
                JSONObject wat = new JSONObject(jsonFormat);
                trips.add(wat);
            }
        }

        return trips;
    }
}
