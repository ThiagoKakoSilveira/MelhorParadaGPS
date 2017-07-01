package aplicacao;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;

import smartcity.gtfs.GTFSReader;
import smartcity.gtfs.Route;
import smartcity.gtfs.Service;
import smartcity.gtfs.Shape;
import smartcity.gtfs.Stop;
import smartcity.gtfs.Trip;
import smartcity.util.GPSCoordinate;
import util.Console;

public class Principal {

	public static void main(String[] args) throws FileNotFoundException {
		
		Scanner scanner = new Scanner (System.in);
		
		System.out.println("Reading stops.");
		Map<String,Stop> stops = GTFSReader.loadStops("data/stops.txt");
		System.out.println("Reading routes.");
		Map<String,Route> routes = GTFSReader.loadRoutes("data/routes.txt");
		System.out.println("Reading shapes.");
		Map<String,Shape> shapes = GTFSReader.loadShapes("data/shapes.txt");
		System.out.println("Reading calendar.");
		Map<String,Service> services = GTFSReader.loadServices("data/calendar.txt");
		System.out.println("Reading trips.");
		Map<String,Trip> trips = GTFSReader.loadTrips("data/trips.txt",routes,services,shapes);
		System.out.println("Reading stop times.");
		long s = System.currentTimeMillis();
		GTFSReader.loadStopTimes("data/stop_times.txt", trips, stops);
		long e = System.currentTimeMillis();
		System.out.println("\nTempo = " + ((e-s)/1000.0));
				
		double latitudeOrigem = -30.150988899999994;//scanner.nextDouble();
		double longitudeOrigem = -51.165521;//Console.scanDouble("Escreva agora a Longitude: ");//aqui está estourando o double creio eu. 
		
		GPSCoordinate coordenadaOrigem = new GPSCoordinate(latitudeOrigem, longitudeOrigem);
		
		double latitudeDestino = -30.021297;//Console.scanDouble("Bem Vindo ao Melhor Parada! Escreva a coordenada de Origem\n Começando pela Latitude: ");
		double longitudeDestino = -51.107397;//Console.scanDouble("Escreva agora a Longitude: ");
		
		GPSCoordinate coordenadaDestino = new GPSCoordinate(latitudeDestino, longitudeDestino);
		
	}

}
