package com.launcher.spring;

//Maven dependencies
import com.google.gson.Gson;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StationLoad {

    Map<String, String> transfersMapping;
    Map<String, Station> stationsContainer;
    Map<String, List<String>> routeStationMapper;

    public StationLoad(String staticFeedDirectory) throws IOException {
        String stationsPath = staticFeedDirectory + "/stops.txt";
        String transfersPath = staticFeedDirectory + "/transfers.txt";
        String routesPath = staticFeedDirectory + "/Stations.csv";
        this.transfersMapping = loadTransfersMapping(transfersPath);
        this.stationsContainer = loadStations(stationsPath);
        this.routeStationMapper = mapAvaiRoutes(routesPath);
    }

    public Map<String, Station> getStationsDict() {
        return this.stationsContainer;
    }

    private Map<String, String> loadTransfersMapping(String path) throws IOException {
        BufferedReader csvReader = new BufferedReader(new FileReader(path));
        String row;

        //read headers and initialize them into a map
        String[] headerArray = csvReader.readLine().split(",");
        Map<String, Integer> headers= mapHeaders(headerArray);

        Map<String, String> transferMapping = new HashMap<>();

        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(",", -1);

            if(data[headers.get("from_stop_id")].equals(data[headers.get("to_stop_id")])) {
                continue;
            }

            if(transferMapping.containsKey(data[headers.get("to_stop_id")])) {
                continue;
            }

            transferMapping.put(data[headers.get("from_stop_id")], data[headers.get("to_stop_id")]);

        }
        return transferMapping;
    }

    private Map<String, Station> loadStations(String path) throws IOException {
        BufferedReader csvReader = new BufferedReader(new FileReader(path));
        String row;

        //read headers and initialize them into a map
        String[] headerArray = csvReader.readLine().split(",");
        Map<String, Integer> headers= mapHeaders(headerArray);

        Map<String, Station> _stations = new HashMap<>();
        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(",", -1);

            //Only consider stations w/o a parent_station - means that the row denotes a station that is a parent_station
            if(!data[headers.get("parent_station")].equals("")) {
                continue;
            }

            //Check if the read row's stop_id should be mapped to another station's id based off the transfers mapping to get the primary StationID
            String stopKey = data[headers.get("stop_id")];
            if(this.transfersMapping.containsKey(stopKey)) {
                stopKey = this.transfersMapping.get(stopKey);
            }

            //Check if the stop_id's station container is already in the Map. If yes, just add the parameters to the Object
            if(_stations.containsKey(stopKey)) {
                Station targetStation = _stations.get(stopKey);
                //Objects are passed by reference in the HashMap <- means the object stored in the HashMap is updated
                //Read in the columns of interest (stop_id, stop_name, stop_lat, stop_lon
                targetStation.addStation(data[headers.get("stop_id")], //This is the read in Key, to ensure the original stop_id pre-mapping is stored
                                         data[headers.get("stop_name")],
                                         data[headers.get("stop_lat")],
                                         data[headers.get("stop_lon")]);
            }
            //Else..make a new station with the stopKey
            else {
                Station createStation = new Station(stopKey);
                //Read in the columns of interest (stop_id, stop_name, stop_lat, stop_lon
                createStation.addStation(data[headers.get("stop_id")], //This is the read in Key, to ensure the original stop_id pre-mapping is stored
                                         data[headers.get("stop_name")],
                                         data[headers.get("stop_lat")],
                                         data[headers.get("stop_lon")]);
                _stations.put(stopKey, createStation);
            }
        }
        return _stations;
    }

    //once we have the Stations Mapped, get all the available routes per station from this csv
    private Map<String, List<String>> mapAvaiRoutes(String path) throws IOException {
        BufferedReader csvReader = new BufferedReader(new FileReader(path));
        String row;
        Map<String, List<String>> routeStationMappings = new HashMap<>();

        //read headers and initialize them into a map
        String[] headerArray = csvReader.readLine().split(",");
        Map<String, Integer> headers = mapHeaders(headerArray);
        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(",", -1);
            String stopId = data[headers.get("GTFS Stop ID")];
            if(this.transfersMapping.containsKey(stopId)) {
                stopId = this.transfersMapping.get(stopId);
            }

            if(!this.stationsContainer.containsKey(stopId)) {
                continue;
            }

            String routesString = data[headers.get("Daytime Routes")];
            String[] avaiRoutes = routesString.split(" ");

            Station targetStation = this.stationsContainer.get(stopId);
            for(String route: avaiRoutes) {
                targetStation.addAvailableRoute(route);

                if(routeStationMappings.containsKey(route)) {
                    routeStationMappings.get(route).add(stopId);
                }

                else {
                    List<String> entry= new ArrayList<>();
                    entry.add(stopId);
                    routeStationMappings.put(route, entry);
                }
            }
        }
        return routeStationMappings;
    }

    private Map<String, Integer> mapHeaders(String[] headerArray) {
        Map<String, Integer> headers= new HashMap<>();
        for(int i=0; i<headerArray.length;i++) {
            headers.put(headerArray[i], i);
        }
        return headers;
    }
}
