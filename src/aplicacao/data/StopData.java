package aplicacao.data;

import datastructures.KDData;
import smartcity.gtfs.Stop;

/**
 * Created by pvmeira and Thiago and Arthur on 01/07/17.
 */
public class StopData extends KDData {
    private Stop parada;

    public StopData(Stop parada, Double x, Double y) {
        super(x, y);
        this.parada = parada;
    }
    
    public StopData(Double x, Double y){
    	super(x,y);
    }

    //Não entendi esses três pontinhos aqui tbm.
    public StopData(double... data) {
		super(data);
	}

	public Stop getParada() {
        return parada;
    }    
    
    public double distanciaTeste(double lat, double lon){
    	double x1 = parada.getGPSCoordinate().latitude;
    	double y1 = parada.getGPSCoordinate().longitude;
    	double x2 = lat;
    	double y2 = lon;
    	return Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
    }

    public void setParada(Stop paradas) {
        this.parada = paradas;
    }
}
