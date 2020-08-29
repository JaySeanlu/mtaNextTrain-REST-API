package com.launcher.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Station {

    private class StationNode {
        String stop_id;
        String stop_name;
        Map<String, Double> location;

        StationNode(String _stop_id, String _stop_name, String stop_lat, String stop_lon) {
            this.stop_id = _stop_id;
            this.stop_name = _stop_name;
            this.location = new HashMap<>();
            location.put("stop_lat", Double.parseDouble(stop_lat));
            location.put("stop_lon", Double.parseDouble(stop_lon));
        }
    }

    String primaryStationId;
    List<StationNode> stationsNodeList;
    HashSet<String> availableRoutes;

    Station(String primStationId) {
        this.primaryStationId = primStationId;
        this.stationsNodeList = new ArrayList<>();
        this.availableRoutes = new HashSet<>();
    }

    public void addAvailableRoute(String routeId) {
        this.availableRoutes.add(routeId);
    }

    public void addStation(String _stop_id, String _stop_name, String stop_lat, String stop_lon) {
        StationNode node = new StationNode(_stop_id, _stop_name, stop_lat, stop_lon);
        this.stationsNodeList.add(node);
    }

    public Map<String, Object> toMapDict() {
        Map<String, Object> stationsDict = new HashMap<>();
        stationsDict.put("primaryStationId", this.primaryStationId);
        Map<String, Map<String, Double>> locations = new HashMap<>();
        Set<String> namesCache = new HashSet<>();

        StringBuilder stationNameBuild = new StringBuilder();
        for (StationNode node : this.stationsNodeList) {
            locations.put(node.stop_id, node.location);

            if(namesCache.contains(node.stop_name)) {
                continue;
            }

            if(stationNameBuild.length() != 0) {
                stationNameBuild.append(" / ");
            }
            stationNameBuild.append(node.stop_name);
            namesCache.add(node.stop_name);
        }
        stationsDict.put("stop_name", new String(stationNameBuild));
        stationsDict.put("stationsNodeList" ,locations);
        stationsDict.put("availableRoutes", availableRoutes);
        return stationsDict;
    }

}
