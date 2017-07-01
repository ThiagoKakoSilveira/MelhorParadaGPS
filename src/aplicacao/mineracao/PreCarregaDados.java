package aplicacao.mineracao;

import aplicacao.data.StopData;
import datastructures.KDData;
import datastructures.KDTree;
import smartcity.gtfs.*;

import java.io.FileNotFoundException;
import java.util.Map;

/**
 * Created by pvmeira on 01/07/17.
 */
public class PreCarregaDados {
    private Map<String, Route> routes;
    private Map<String, Stop> stops;
    private Map<String, Shape> shapes;
    private Map<String, Service> services;
    private Map<String, Trip> trips;
    private KDTree arvoreKdParadas;


    public void starUp() {

        try {
            System.out.println("Reading stops.");

            this.stops = GTFSReader.loadStops("data/stops.txt");
            System.out.println("Reading routes.");

            this.routes = GTFSReader.loadRoutes("data/routes.txt");
            System.out.println("Reading shapes.");

            this.shapes = GTFSReader.loadShapes("data/shapes.txt");
            System.out.println("Reading calendar.");

            this.services = GTFSReader.loadServices("data/calendar.txt");
            System.out.println("Reading trips.");

            this.trips = GTFSReader.loadTrips("data/trips.txt", routes, services, shapes);
            System.out.println("Reading stop times.");

            System.out.println("Carregamento concluido");
            this.criarArvoreKdApartirDasParadas();
        } catch (FileNotFoundException e) {
            System.err.println(String.format("Erro ao fazer carregamendo dos dados : %s   -->  ", e.getMessage()));
        }

    }

    public void criarArvoreKdApartirDasParadas() {
        KDData[] paradas = new StopData[this.stops.values().size()];
        int cont = 0;
        for (Stop parada : this.stops.values()) {
            paradas[cont] = new StopData(parada, parada.getGPSCoordinate().latitude, parada.getGPSCoordinate().longitude);
            cont++;
        }
        this.arvoreKdParadas = new KDTree(paradas);
        System.out.println("Arvore KD com as paradas foi criada");
    }

    public void buscarParadasProximas(KDData data, int tamanhoBusca) {


        KDData[] dataRetorno = new KDData[tamanhoBusca];
        arvoreKdParadas.findKNearestPoints(data, dataRetorno);

        System.out.println("Parada Mais Perto: " + ((StopData) dataRetorno[0]).getParada());
        System.out.println("Distance: " + dataRetorno[0].distance(data));
        System.out.println("------");
        for (KDData n : dataRetorno) {
            System.out.print(n + " \n");
        }
    }


    public Map<String, Route> getRoutes() {
        return routes;
    }

    public void setRoutes(Map<String, Route> routes) {
        this.routes = routes;
    }

    public Map<String, Stop> getStops() {
        return stops;
    }

    public void setStops(Map<String, Stop> stops) {
        this.stops = stops;
    }

    public Map<String, Shape> getShapes() {
        return shapes;
    }

    public void setShapes(Map<String, Shape> shapes) {
        this.shapes = shapes;
    }

    public Map<String, Service> getServices() {
        return services;
    }

    public void setServices(Map<String, Service> services) {
        this.services = services;
    }

    public Map<String, Trip> getTrips() {
        return trips;
    }

    public void setTrips(Map<String, Trip> trips) {
        this.trips = trips;
    }

    public KDTree getArvoreKdParadas() {
        return arvoreKdParadas;
    }

    public void setArvoreKdParadas(KDTree arvoreKdParadas) {
        this.arvoreKdParadas = arvoreKdParadas;
    }
}
