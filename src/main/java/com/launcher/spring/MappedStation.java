package com.launcher.spring;

//Maven dependencies
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MappedStation {

    private class routeArrival implements Comparable<routeArrival> {
        String routeId;
        Long arrival;
        String timestamp;
        long minsETA;

        routeArrival(String _routeId, long _arrival, long _updateTime) {
            this.routeId = _routeId;
            this.arrival = _arrival;
            this.timestamp = convertTime(_arrival);
            this.minsETA = getETA(_arrival, _updateTime);
        }

        @Override
        public int compareTo(routeArrival o) {
            return this.arrival.compareTo(o.arrival);
        }

        public Map<String, Object> toMap() {
            Map<String, Object> returnedMap = new HashMap<>();
            returnedMap.put("routeId", routeId);
            returnedMap.put("arrival", arrival);
            returnedMap.put("minsETA", minsETA);
            returnedMap.put("timestamp", timestamp);
            return returnedMap;
        }

        private String convertTime(Long arrivalSeconds) {
            TimeZone tz = TimeZone.getTimeZone("America/New_York");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long arrivalMilli = arrivalSeconds * 1000;
            Date arrivalEpoch = new Date(arrivalMilli);
            sdf.setTimeZone(tz);
            String timestamp = sdf.format(arrivalEpoch);
            return timestamp;
        }

        private long getETA(long arrivalSeconds, long _updateTime) {
            long arrivalMilli = arrivalSeconds * 1000;
            long millisDiff = Math.abs(arrivalMilli - _updateTime);
            long minsETA = TimeUnit.MINUTES.convert(millisDiff, TimeUnit.MILLISECONDS);
            return minsETA;
        }
    }

    Station Station;
    Set<String> routesSet;
    List<routeArrival> northArrivals;
    List<routeArrival> southArrivals;

    MappedStation(Station station) {
        this.Station = station;
        this.routesSet = new HashSet<>();
        this.northArrivals = new ArrayList<>();
        this.southArrivals = new ArrayList<>();
    }

    public void addTrain(String direction, String routeId, long arrival, long updateTime) throws IllegalArgumentException{
        String routeIDUpper = routeId.toUpperCase();
        routesSet.add(routeIDUpper);
        routeArrival currRouteArrival = new routeArrival(routeIDUpper, arrival, updateTime);
        if(direction == "NORTH") {
            this.northArrivals.add(currRouteArrival);
        }
        else if(direction == "SOUTH") {
            this.southArrivals.add(currRouteArrival);
        }
        else {
            throw new IllegalArgumentException("Direction must be value 'SOUTH' or 'NORTH'");
        }
    }

    //Sort for earliest arrivals per northbound/southbound trains - the comparator is using Epoch time
    public MappedStation sortTrains() {
        Collections.sort(northArrivals);
        Collections.sort(southArrivals);
        return this;
    }

    //Generate into JSON
    public String toJSONString() {
        Map<String, Object> result = this.Station.toMapDict();
        result.put("routes", routesSet);

        List<Map<String, Object>> uptownDirectionTripUpdate = new ArrayList<>();
        for(routeArrival uptownArrivals : northArrivals) {
            uptownDirectionTripUpdate.add(uptownArrivals.toMap());
        }

        List<Map<String, Object>> downtownDirectionTripUpdate = new ArrayList<>();
        for(routeArrival downtownArrivals : southArrivals) {
            downtownDirectionTripUpdate.add(downtownArrivals.toMap());
        }

        Map<String, List<Map<String, Object>>> tripUpdates = new HashMap<>();
        tripUpdates.put("north", uptownDirectionTripUpdate);
        tripUpdates.put("south", downtownDirectionTripUpdate);
        result.put("trip_updates", tripUpdates);


        Gson gson = new Gson();
        String jsonString = gson.toJson(result);

        return jsonString;
    }
}
