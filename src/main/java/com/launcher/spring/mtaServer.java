package com.launcher.spring;

//Maven Dependencies
import com.google.gson.Gson;

import java.io.IOException;
import java.util.*;

public class mtaServer {
    StationLoad stationsLoad;
    TripStationRouter tripStationRouter;
    Map<String, String> feedMap;
    Map<String, String> trainMap;
    Gson gson = new Gson();

    public mtaServer() throws IOException {
        String staticFeedDirectory = "./src/main/java/metadata";
        this.stationsLoad = new StationLoad(staticFeedDirectory);
        this.tripStationRouter = new TripStationRouter(stationsLoad);
        this.feedMap = genFeedURLs();
        this.trainMap = genTrainMappings();
    }

    public String getStationsJSON() {
        String jsonString = gson.toJson(this.stationsLoad.getStationsDict());
        return jsonString;
    }

    public List<String> getRoutesJSON() {
        List<String> routes = new ArrayList<>(trainMap.keySet());
        return routes;
    }

    public String getStationIdData(String stationId) throws IllegalArgumentException {
        String realStationId = tripStationRouter.findStationPrimaryID(stationId);
        if(!this.stationsLoad.stationsContainer.containsKey(realStationId)) {
            throw new IllegalArgumentException("Invalid station code");
        }
        Station target = this.stationsLoad.stationsContainer.get(stationId);
        String jsonString = gson.toJson(target);
        return jsonString;
    }

    public String getRouteArrivalsJSON(String routeId, String apiKey) throws IOException, IllegalArgumentException {
        if(!trainMap.containsKey(routeId)) {
            throw new IllegalArgumentException("Invalid Feed Key!");
        }

        String feedURL = trainMap.get(routeId);
        List<String> routeStations = stationsLoad.routeStationMapper.get(routeId);
        Map<String, MappedStation> staticMappedStation = new HashMap<>();
        for(String routeMapStation : routeStations) {
            MappedStation targetStationContainer = new MappedStation(stationsLoad.stationsContainer.get(routeMapStation));
            staticMappedStation.put(routeMapStation, targetStationContainer);
        }

        Map<String, MappedStation> output = tripStationRouter.getStaticFeedMappingHashMap(feedURL, apiKey, staticMappedStation);
        String jsonString = gson.toJson(output);
        return jsonString;
    }

    public String getStationArrivals(String stationId, String apiKey) throws IOException, IllegalArgumentException {
        String realStationId = tripStationRouter.findStationPrimaryID(stationId);

        if(!stationsLoad.stationsContainer.containsKey(realStationId)) {
            throw new IllegalArgumentException("Invalid Station Code");
        }

        Station targetStation = stationsLoad.stationsContainer.get(realStationId);
        Set<String> FeedURLsSet = new HashSet<>();

        for(String routeId: targetStation.availableRoutes) {
            if(trainMap.containsKey(routeId)) {
                FeedURLsSet.add(trainMap.get(routeId));
            }
        }

        String[] FeedURLs = FeedURLsSet.toArray(new String[0]);;

        MappedStation targetStationContainer = new MappedStation(targetStation);
        Map<String, MappedStation> staticMappedStation = new HashMap<>();
        staticMappedStation.put(realStationId, targetStationContainer);

        List<String> outputContainer = tripStationRouter.getStaticFeedMappingList(FeedURLs, apiKey, staticMappedStation);
        String output = outputContainer.get(0);
        return output;
    }

    public String getFeedMapping(String feedKey, String apiKey) throws IOException, IllegalArgumentException {
        if(!this.feedMap.containsKey(feedKey)) {
            throw new IllegalArgumentException("Invalid Feed Key!");
        }
        Map<String, MappedStation> rawResult = this.tripStationRouter.getFullFeedMappingHashMap(this.feedMap.get(feedKey), apiKey);
        String resultJSON = gson.toJson(rawResult);
        return resultJSON;
    }

    //from https://api.mta.info/#/subwayRealTimeFeeds
    private Map<String, String> genFeedURLs() {
        Map<String, String> feedMap = new HashMap<>();
        feedMap.put("ACE", "https://api-endpoint.mta.info/Dataservice/mtagtfsfeeds/nyct%2Fgtfs-ace");
        feedMap.put("BDFM", "https://api-endpoint.mta.info/Dataservice/mtagtfsfeeds/nyct%2Fgtfs-bdfm");
        feedMap.put("G", "https://api-endpoint.mta.info/Dataservice/mtagtfsfeeds/nyct%2Fgtfs-g");
        feedMap.put("JZ", "https://api-endpoint.mta.info/Dataservice/mtagtfsfeeds/nyct%2Fgtfs-jz");
        feedMap.put("NQRW", "https://api-endpoint.mta.info/Dataservice/mtagtfsfeeds/nyct%2Fgtfs-nqrw");
        feedMap.put("L", "https://api-endpoint.mta.info/Dataservice/mtagtfsfeeds/nyct%2Fgtfs-l");
        feedMap.put("123456", "https://api-endpoint.mta.info/Dataservice/mtagtfsfeeds/nyct%2Fgtfs");
        feedMap.put("7", "https://api-endpoint.mta.info/Dataservice/mtagtfsfeeds/nyct%2Fgtfs-7");
        return feedMap;
    }

    private Map<String, String> genTrainMappings() {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : feedMap.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            for(int i=0; i<k.length(); i++) {
                String train = Character.toString(k.charAt(i));
                result.put(train, v);
            }
        }
        return result;
    }
}
