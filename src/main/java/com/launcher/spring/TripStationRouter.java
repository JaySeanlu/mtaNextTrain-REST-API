package com.launcher.spring;

//Maven dependencies
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripStationRouter {

    StationLoad stationsLoad;

    public TripStationRouter(StationLoad inputStation) {
        this.stationsLoad = inputStation;
    }

    public List<String> getFullFeedMappingList(String[] FeedURL, String apiKey) throws IOException {
        Map<String, MappedStation> mappedStation = new HashMap<>();
        return getFeedMappingList(FeedURL, apiKey, mappedStation, false);
    }

    public List<String> getStaticFeedMappingList(String[] FeedURL, String apiKey, Map<String, MappedStation> mappedStation) throws IOException {
        return getFeedMappingList(FeedURL, apiKey, mappedStation, true);
    }

    public Map<String, MappedStation> getFullFeedMappingHashMap(String[] FeedURL, String apiKey) throws IOException {
        Map<String, MappedStation> mappedStation = new HashMap<>();
        return getFeedMappingHashMap(FeedURL, apiKey, mappedStation, false);
    }


    public Map<String, MappedStation> getStaticFeedMappingHashMap(String[] FeedURL, String apiKey, Map<String, MappedStation> mappedStation) throws IOException {
        return getFeedMappingHashMap(FeedURL, apiKey, mappedStation, true);
    }

    private Map<String, MappedStation> getFeedMappingHashMap(String[] FeedURL, String apiKey, Map<String, MappedStation> mappedStations, boolean static_mapping) throws IOException {
        //If we want to load in multiple Feeds at once, then you'll have a list of these URLs to parse through, and would like a running feed of com.launcher.spring.StationLoad, so this constructor might want to go to a method
        long currEpoch = new Date().getTime();
        for(String feedURL: FeedURL) {

            List<JSONObject> Feed = GTFSLoad.readGTFS(feedURL, apiKey);

            for (JSONObject feedTripUpdate : Feed) {
                transformTrips(feedTripUpdate, mappedStations, currEpoch, static_mapping);
            }
        }

        List<MappedStation> mappedStationsResult = new ArrayList<>(mappedStations.values());

        Map<String, MappedStation> resultingMappedStations = new HashMap<>();
        for(MappedStation stationFeed : mappedStationsResult) {
            //null pointer exceptions
            if(stationFeed.Station == null) {
                continue;
            }
            MappedStation sortedStation = stationFeed.sortTrains();
            resultingMappedStations.put(sortedStation.Station.primaryStationId, sortedStation);
            System.out.println(sortedStation.toJSONString());
        }
        return resultingMappedStations;
    }

    private List<String> getFeedMappingList(String[] FeedURL, String apiKey, Map<String, MappedStation> mappedStations, boolean static_mapping) throws IOException {
        //If we want to load in multiple Feeds at once, then you'll have a list of these URLs to parse through, and would like a running feed of com.launcher.spring.StationLoad, so this constructor might want to go to a method
        long currEpoch = new Date().getTime();
        for(String feedURL: FeedURL) {

            List<JSONObject> Feed = GTFSLoad.readGTFS(feedURL, apiKey);

            for (JSONObject feedTripUpdate : Feed) {
                transformTrips(feedTripUpdate, mappedStations, currEpoch, static_mapping);
            }
        }

        List<MappedStation> mappedStationsResult = new ArrayList<>(mappedStations.values());
        List<String> resultingJSONString = new ArrayList<>();
        for(MappedStation stationFeed : mappedStationsResult) {
            //null pointer exceptions
            if(stationFeed.Station == null) {
                continue;
            }
            String sortedStation = stationFeed.sortTrains().toJSONString();
            resultingJSONString.add(sortedStation);
            System.out.println(sortedStation);
        }
        return resultingJSONString;
    }

    private void transformTrips(JSONObject tripUpdate, Map<String, MappedStation> mappedStations, long currTimeEpoch, boolean static_mapping) {

        if(!tripUpdate.has("stopTimeUpdate")) {
            return;
        }

        JSONArray stopTimeUpdatesHolder = tripUpdate.getJSONArray("stopTimeUpdate");
        JSONObject nyctTripDesc = tripUpdate.getJSONObject("trip").getJSONObject("nyctTripDescriptor");

        //Each feed will give us the Direction and Route for each Arrival
        String direction = (nyctTripDesc.has("direction")) ? "SOUTH" : "NORTH";
        String routeId = (String) tripUpdate.getJSONObject("trip").get("routeId");

        //Now look at every TripUpdate get pull the StationID - put into the HashMap if it's the first update for the station, else append it the updated station's arrivalList
        for(int i=0;i<stopTimeUpdatesHolder.length();i++) {
            JSONObject tripArrival = stopTimeUpdatesHolder.getJSONObject(i);
            String rawStopId = (String) tripArrival.get("stopId");
            String primaryStopId = findStationPrimaryID(rawStopId);
            long arrival;

            //departure and arrival times are identical in feed - however, documentation says its possible 1 field or both might be missing
            try {
                arrival = Long.parseLong((String) tripArrival.getJSONObject("arrival").get("time"));
            }
            catch(Exception e) {
                arrival = Long.parseLong((String) tripArrival.getJSONObject("departure").get("time"));
            }

            //Commenting this out to catch any errors that do appear here
            //finally {
            //    continue;
            //}

            //FYI below might skew displayed results from reality
            if(arrival*1000 < currTimeEpoch) {
                continue;
            }

            if(mappedStations.containsKey(primaryStopId)) {
                MappedStation existingMappedStation = mappedStations.get(primaryStopId);

                //Objects are passed by reference in the HashMap <- means the object stored in the HashMap is updated
                existingMappedStation.addTrain(direction, routeId, arrival, currTimeEpoch);
            }

            else if(static_mapping == false){
                Station targetStation = stationsLoad.stationsContainer.get(primaryStopId);
                MappedStation newMappedStation = new MappedStation(targetStation);
                newMappedStation.addTrain(direction, routeId, arrival, currTimeEpoch);
                mappedStations.put(primaryStopId, newMappedStation);
            }
        }
    }

    public String findStationPrimaryID(String rawStopId) {
        String stopIdChar = rawStopId.substring(0, Math.min(rawStopId.length(), 3));
        if(stationsLoad.transfersMapping.containsKey(stopIdChar)) {
            return stationsLoad.transfersMapping.get(stopIdChar);
        }
        return stopIdChar;
    }

    //overloaded to include single URL put in as a String
    public List<String> getFullFeedMappingList(String feedURL, String apiKey) throws IOException {
        String[] FeedURL = new String[] {feedURL};
        return getFullFeedMappingList(FeedURL, apiKey);
    }

    //overloaded to include single URL put in as a String
    public List<String> getStaticFeedMappingList(String feedURL, String apiKey, Map<String, MappedStation> mappedStation) throws IOException {
        String[] FeedURL = new String[] {feedURL};
        return getStaticFeedMappingList(FeedURL, apiKey, mappedStation);
    }

    //overloaded to include single URL put in as a String
    public Map<String, MappedStation> getStaticFeedMappingHashMap(String feedURL, String apiKey, Map<String, MappedStation> mappedStation) throws IOException {
        String[] FeedURL = new String[] {feedURL};
        return getStaticFeedMappingHashMap(FeedURL, apiKey, mappedStation);
    }

    //overloaded to include single URL put in as a String
    public Map<String, MappedStation> getFullFeedMappingHashMap(String feedURL, String apiKey) throws IOException {
        String[] FeedURL = new String[] {feedURL};
        return getFullFeedMappingHashMap(FeedURL, apiKey);
    }
}
