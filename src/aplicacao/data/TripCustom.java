package aplicacao.data;

import smartcity.gtfs.*;
import smartcity.util.GPSCoordinate;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by pvmeira on 01/07/17.
 */
public class TripCustom extends GTFSObject {
    private Route route;
    private Service service;
    private Shape shape;
    private int direction;
    private boolean wheelchair;


    private List<Stop> stops;

    public TripCustom(String id, Route route, Service service, Shape shape,
                      int direction, boolean w) {
        super(id);
        this.route = route;
        this.service = service;
        this.shape = shape;
        this.direction = direction;
        this.wheelchair = w;

        this.stops = new LinkedList<Stop>();
    }

    public Route getRoute() {
        return route;
    }

    public Service getService() {
        return service;
    }

    public Shape getShape() {
        return shape;
    }

    public boolean isOneWay() {
        return direction == 0;
    }

    public boolean hasWeelchair() {
        return wheelchair;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,,,%d,,,%d",
                route.getId(), service.getId(),
                getId(), isOneWay() ? 0 : 1, wheelchair ? 1 : 2);
    }

    public void addStop(Stop stop) {
        stops.add(stop);
    }

    public boolean hasStopNear(GPSCoordinate place, double threshold) {
        for (Stop s : stops) {
            if (s.getGPSCoordinate().distance(place) < threshold)
                return true;
        }
        return false;
    }

    public List<Stop> getStops() {
        return stops;
    }

}

