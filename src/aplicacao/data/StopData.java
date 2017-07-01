package aplicacao.data;

import datastructures.KDData;
import smartcity.gtfs.Stop;

/**
 * Created by pvmeira on 01/07/17.
 */
public class StopData extends KDData {
    private Stop parada;

    public StopData(Stop parada, Double x, Double y) {
        super(x, y);
        this.parada = parada;
    }

    public Stop getParada() {
        return parada;
    }

    public void setParada(Stop paradas) {
        this.parada = paradas;
    }
}
