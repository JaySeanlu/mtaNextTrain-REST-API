# mtaNextTrain
mtaNextTrain is a simple HTTPs Web Server that maps train updates from the NYC MTA Realtime Feed to provide live train arrival times by station or route

This Java-based RESTful API converts the MTA's GTFS Protocol Buffers into JSON responses. 

Application launched via Spring Boot, deployed on Heroku with host url https://mtanexttrain.herokuapp.com.

## Usage

Requires an API token to the MTA GTFS Realtime Feed. To receive a token, register a Developer account [here](https://api.mta.info/#/landing)

For Documentation on GTFS and the MTA API see below: 

* GTFS Protocol Buffers: [Link](https://developers.google.com/transit/gtfs-realtime) 

* MTA Realtime GTFS Feed: [Link](http://datamine.mta.info/sites/all/files/pdfs/GTFS-Realtime-NYC-Subway%20version%201%20dated%207%20Sep.pdf)

## Endpoints

#### GET **/stations**

##### Sample Request

`https://mtanexttrain.herokuapp.com/stations`

Returns metadata about all stations across the NYC Subway system. Station responses are organized by `primaryStationId` as keys. 

Several subway stations are grouped into complexes (i.e Court Square-23 St Station). The response object groups such complexes's stations, with one station's ID representing the entire complex as the `primaryStationId`.
 
 If the station is a standalone and not grouped with other stations, its regular stationId takes the `primaryStationId` field

##### Sample Response

```
{
  "253": {
    "primaryStationId": "253",
    "stationsNodeList": [
      {
        "stop_id": "253",
        "stop_name": "Rockaway Av",
        "location": {
          "stop_lon": -73.908946,
          "stop_lat": 40.662549
        }
      }
    ],
    "availableRoutes": [
      "3"
    ]
  },

  ...

  "J21": {
    "primaryStationId": "J21",
    "stationsNodeList": [
      {
        "stop_id": "J21",
        "stop_name": "Norwood Av",
        "location": {
          "stop_lon": -73.880039,
          "stop_lat": 40.68141
        }
      }
    ],
    "availableRoutes": [
      "J",
      "Z"
    ]
  }
}

```

---

#### GET **/stations/{stationId}**

##### Sample Request

`https://mtanexttrain.herokuapp.com/stations/G22`

Returns metadata about a designated stationId's primary station (see `/stations` to see the relationship between stationId and primaryStationId)

##### Sample Response

```
{
  "primaryStationId": "G22",
  "stationsNodeList": [
    {
      "stop_id": "719",
      "stop_name": "Court Sq",
      "location": {
        "stop_lon": -73.945264,
        "stop_lat": 40.747023
      }
    },
    {
      "stop_id": "F09",
      "stop_name": "Court Sq",
      "location": {
        "stop_lon": -73.946,
        "stop_lat": 40.747846
      }
    },
    {
      "stop_id": "G22",
      "stop_name": "Court Sq - 23 St",
      "location": {
        "stop_lon": -73.943832,
        "stop_lat": 40.746554
      }
    }
  ],
  "availableRoutes": [
    "E",
    "G",
    "7",
    "M"
  ]
}
```

---

#### GET **/routes**

##### Sample Request

`https://mtanexttrain.herokuapp.com/routes`

Returns a list of all available DAYTIME train lines across the NYC Subway System

###### Sample Response

```
["A","B","C","D","E","F","G","J","L","M","N","1","Q","2","R","3","4","5","6","7","W","Z"]
```

---

#### GET **/arrivals/{apiKey}/station/{stationId}**

##### Sample Request

`https://mtanexttrain.herokuapp.com/arrivals/{REDACTED_API_KEY}/station/J21`

Returns trip update data for a designated station's primary station (see `/stations` to see the relationship between stationId and primaryStationId)

##### Sample Response

```
{
  "routes": [
    "J"
  ],
  "availableRoutes": [
    "J",
    "Z"
  ],
  "stop_id": "J21",
  "stops": {
    "J21": {
      "stop_lon": -73.880039,
      "stop_lat": 40.68141
    }
  },
  "trip_updates": {
    "south": [
      {
        "routeId": "J",
        "arrival": 1598330739,
        "minsETA": 0,
        "timestamp": "2020-08-25 00:45:39"
      }
    ],
    "north": [
      {
        "routeId": "J",
        "arrival": 1598331276,
        "minsETA": 9,
        "timestamp": "2020-08-25 00:54:36"
      },
      {
        "routeId": "J",
        "arrival": 1598332232,
        "minsETA": 25,
        "timestamp": "2020-08-25 01:10:32"
      },
      {
        "routeId": "J",
        "arrival": 1598333370,
        "minsETA": 44,
        "timestamp": "2020-08-25 01:29:30"
      }
    ]
  },
  "stop_name": "Norwood Av"
}
```

---

#### GET **/arrivals/{apiKey}/route/{routeId}**

##### Sample Request

`https://mtanexttrain.herokuapp.com/arrivals/{REDACTED_API_KEY}/route/A`

Returns all trip updates on train arrivals across all primary stations throughout a specified train route line. Individual station trip update responses are organized by `primaryStationId` as keys (see `/stations` to see the relationship between stationId and primaryStationId). 

Note that other train lines across the specified route's feed will still be displayed for each station. (See `/arrivals/{apiKey}/feed/{feedId}` endpoint for a description of `Feed`)

##### Sample Response

```
{
  "A65": {
    "Station": {
      "primaryStationId": "A65",
      "stationsNodeList": [
        {
          "stop_id": "A65",
          "stop_name": "Ozone Park - Lefferts Blvd",
          "location": {
            "stop_lon": -73.825798,
            "stop_lat": 40.685951
          }
        }
      ],
      "availableRoutes": [
        "A"
      ]
    },
    "routesSet": [
      "A"
    ],
    "northArrivals": [
      {
        "routeId": "A",
        "arrival": 1598330400,
        "timestamp": "2020-08-25 00:40:00",
        "minsETA": 8
      }
    ],
    "southArrivals": [
      {
        "routeId": "A",
        "arrival": 1598331000,
        "timestamp": "2020-08-25 00:50:00",
        "minsETA": 18
      },
      {
        "routeId": "A",
        "arrival": 1598333093,
        "timestamp": "2020-08-25 01:24:53",
        "minsETA": 53
      }
    ]
  },

  ...

  "A64": {
    "Station": {
      "primaryStationId": "A64",
      "stationsNodeList": [
        {
          "stop_id": "A64",
          "stop_name": "111 St",
          "location": {
            "stop_lon": -73.832163,
            "stop_lat": 40.684331
          }
        }
      ],
      "availableRoutes": [
        "A"
      ]
    },
    "routesSet": [
      "A"
    ],
    "northArrivals": [
      {
        "routeId": "A",
        "arrival": 1598330460,
        "timestamp": "2020-08-25 00:41:00",
        "minsETA": 9
      }
    ],
    "southArrivals": [
      {
        "routeId": "A",
        "arrival": 1598330940,
        "timestamp": "2020-08-25 00:49:00",
        "minsETA": 17
      },
      {
        "routeId": "A",
        "arrival": 1598333033,
        "timestamp": "2020-08-25 01:23:53",
        "minsETA": 52
      }
    ]
  }
}
```

---

#### GET **/arrivals/{apiKey}/feed/{feedId}**

##### Sample Request

`https://mtanexttrain.herokuapp.com/arrivals/{REDACTED_API_KEY}/feed/ACE`

Returns all trip updates on train arrivals across all primary stations throughout a specified feed. Individual station trip update responses are organized by `primaryStationId` as keys (see `/stations` to see the relationship between stationId and primaryStationId). 

The MTA organizes its realtime updates in `Feeds` which group multiple route lines. For all available feed groupings, see [here](https://api.mta.info/#/subwayRealTimeFeeds) (MTA Developer Login required). 

Please order the `{feedId}` string in the order displayed in the above link's mappings

##### Sample Response

```
{
  "A47": {
    "Station": {
      "primaryStationId": "A47",
      "stationsNodeList": [
        {
          "stop_id": "A47",
          "stop_name": "Kingston - Throop Avs",
          "location": {
            "stop_lon": -73.940858,
            "stop_lat": 40.679921
          }
        }
      ],
      "availableRoutes": [
        "C"
      ]
    },
    "routesSet": [
      "A"
    ],
    "northArrivals": [
      {
        "routeId": "A",
        "arrival": 1598331000,
        "timestamp": "2020-08-25 00:50:00",
        "minsETA": 0
      },
      {
        "routeId": "A",
        "arrival": 1598331851,
        "timestamp": "2020-08-25 01:04:11",
        "minsETA": 15
      },
      {
        "routeId": "A",
        "arrival": 1598333031,
        "timestamp": "2020-08-25 01:23:51",
        "minsETA": 34
      }
    ],
    "southArrivals": [
      {
        "routeId": "A",
        "arrival": 1598331570,
        "timestamp": "2020-08-25 00:59:30",
        "minsETA": 10
      },
      {
        "routeId": "A",
        "arrival": 1598331696,
        "timestamp": "2020-08-25 01:01:36",
        "minsETA": 12
      },
      {
        "routeId": "A",
        "arrival": 1598332426,
        "timestamp": "2020-08-25 01:13:46",
        "minsETA": 24
      },
      {
        "routeId": "A",
        "arrival": 1598333430,
        "timestamp": "2020-08-25 01:30:30",
        "minsETA": 41
      },
      {
        "routeId": "A",
        "arrival": 1598334630,
        "timestamp": "2020-08-25 01:50:30",
        "minsETA": 61
      }
    ]
  },
  
  ...

  "A48": {
    "Station": {
      "primaryStationId": "A48",
      "stationsNodeList": [
        {
          "stop_id": "A48",
          "stop_name": "Utica Av",
          "location": {
            "stop_lon": -73.930729,
            "stop_lat": 40.679364
          }
        }
      ],
      "availableRoutes": [
        "A",
        "C"
      ]
    },
    "routesSet": [
      "A"
    ],
    "northArrivals": [
      {
        "routeId": "A",
        "arrival": 1598331761,
        "timestamp": "2020-08-25 01:02:41",
        "minsETA": 13
      },
      {
        "routeId": "A",
        "arrival": 1598332941,
        "timestamp": "2020-08-25 01:22:21",
        "minsETA": 33
      }
    ],
    "southArrivals": [
      {
        "routeId": "A",
        "arrival": 1598331660,
        "timestamp": "2020-08-25 01:01:00",
        "minsETA": 11
      },
      {
        "routeId": "A",
        "arrival": 1598331786,
        "timestamp": "2020-08-25 01:03:06",
        "minsETA": 13
      },
      {
        "routeId": "A",
        "arrival": 1598332516,
        "timestamp": "2020-08-25 01:15:16",
        "minsETA": 26
      },
      {
        "routeId": "A",
        "arrival": 1598333520,
        "timestamp": "2020-08-25 01:32:00",
        "minsETA": 42
      },
      {
        "routeId": "A",
        "arrival": 1598334720,
        "timestamp": "2020-08-25 01:52:00",
        "minsETA": 62
      }
    ]
  }
}
```