package com.launcher.spring;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
public class mtaController {
    mtaServer run = new mtaServer();

    public mtaController() throws IOException {
    }

    @RequestMapping("/")
    public String index() {
        return "Greetings from mtaNextTrain REST API! See https://github.com/JaySeanlu/mtaNextTrain for usage instructions.";
    }

    @RequestMapping(value = "stations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> getStations() {
        try {
            return ResponseEntity.ok(run.getStationsJSON());
        }

        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error", e);
        }
    }

    @RequestMapping(value = "routes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<String>> getRoutes() {
        try {
            return ResponseEntity.ok(run.getRoutesJSON());
        }

        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error", e);
        }
    }

    @RequestMapping(value = "stations/{stationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> getStationById(@PathVariable String stationId) {
        try {
            return ResponseEntity.ok(run.getStationIdData(stationId));
        }
        catch(IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid StationId: " + stationId, e);
        }

        catch(Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Error", e);
        }
    }

    @RequestMapping(value = "arrivals/{apiKey}/feed/{feedId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> getFeed(@PathVariable String feedId, @PathVariable String apiKey) {
        try {
            return ResponseEntity.ok(run.getFeedMapping(feedId, apiKey));
        }

        catch(AccessDeniedException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid API Key", e);
        }

        catch(IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid StationId: " + feedId, e);
        }

        catch(Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Error", e);
        }
    }

    @RequestMapping(value = "arrivals/{apiKey}/station/{stationId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> getStationArrivalById(@PathVariable String stationId, @PathVariable String apiKey) {
        try {
            return ResponseEntity.ok(run.getStationArrivals(stationId, apiKey));
        }

        catch(AccessDeniedException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid API Key", e);
        }

        catch(IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid StationId: " + stationId, e);
        }

        catch(Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Error", e);
        }
    }

    @RequestMapping(value = "arrivals/{apiKey}/route/{routeId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> getRouteArrivalById(@PathVariable String routeId, @PathVariable String apiKey) {
        try {
            return ResponseEntity.ok(run.getRouteArrivalsJSON(routeId, apiKey));
        }

        catch(AccessDeniedException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid API Key", e);
        }

        catch(IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid RouteId: " + routeId, e);
        }

        catch(Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Error", e);
        }
    }
}
