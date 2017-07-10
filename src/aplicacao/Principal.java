package aplicacao;

import java.io.FileNotFoundException;
import java.util.*;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

import aplicacao.data.TripCustom;
import aplicacao.mineracao.PreCarregaDados;
import datastructures.KDData;
import smartcity.gtfs.Stop;

public class Principal {

    private static PreCarregaDados preCarregaDados;

    public static void main(String[] args) throws FileNotFoundException {
        Scanner scanner = new Scanner(System.in);
        preCarregaDados = new PreCarregaDados();

        preCarregaDados.starUp();

        System.out.println("Dados da Partida ");
        System.out.println("Latitude , Longitude");
        String latitudeLongitude = scanner.nextLine();
        String[] splitPartida = latitudeLongitude.split(",");

        System.out.println("Dados do Destino ");
        System.out.println("Latitude , Longitude");
        String dadosDestino = scanner.nextLine();
        String[] splitDestino = dadosDestino.split(",");

/*
       -  - -  EXEMPLOS -  - -
         Campos Velho
       -30.0940906, -51.2267142

          Senac POA
        -30.0351924, -51.2266259

        //perto casa(Nutripao)
        -30.0937542, -51.230038

        //PUC
        -30.0570755, -51.1741819
        
        //Costa Gama (perto de casa)
        -30.150693, -51.161494
*/
        try {
            List<Stop> stopListPartida = preCarregaDados.buscarParadasProximas(transformToKdData(splitPartida[0], splitPartida[1]), 5);
            System.out.println("A parada mais próxima da sua partida é a "+stopListPartida.get(0));
            Map<String, List<TripCustom>> mapViagensParadaPartida = preCarregaDados.obtemViagensDeParadas(stopListPartida);
            System.out.println();
            List<Stop> stopListDestino = preCarregaDados.buscarParadasProximas(transformToKdData(splitDestino[0], splitDestino[1]), 5);
            System.out.println("A parada mais próxima do seu destino é a "+stopListDestino.get(0));
            Map<String, List<TripCustom>> mapViagensParadaDestino = preCarregaDados.obtemViagensDeParadas(stopListDestino);

            List<TripCustom> onibus = preCarregaDados.obtemListaDeOnibusCompartilhados(mapViagensParadaPartida, mapViagensParadaDestino);
            if (onibus.size() > 0) {
                printParadas(stopListPartida, onibus);
            } else {

                System.out.println("__________________________________________________");
                System.out.println("Necessário Pegar dois ônibus -- At this point, just use google maps plz");
                System.out.println("__________________________________________________");

                // TODO Dizer onibus inicial e possiveis onibus apartir desse mesmo
                preCarregaDados.executaAlgoritimo2Paradas(mapViagensParadaPartida, mapViagensParadaDestino, 1);
            }
        } catch (ParseException e) {
            System.err.println("Impossível parsear essa String para Inteiro    :  " + e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Estourou o array da lista    :  " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro ao processar paradas próximas   :  " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    /**
     * Faz o print dos onibus necessários , ordenando por parada
     *
     * @param stopListPartida
     * @param onibus
     */
    public static void printParadas(List<Stop> stopListPartida, List<TripCustom> onibus) {
        System.out.println("____________________________________________________________________________________");
        System.out.println("  Lista de ônibus possíveis de partida  ");
        for (int i = 0; i < onibus.size(); i++) {
            for (int j = 0; j < stopListPartida.size(); j++) {
                if (onibus.get(i).getStops().contains(stopListPartida.get(j))) {
                    System.out.println("Na parada " + stopListPartida.get(j).getName() + "  com latitude: " +
                            stopListPartida.get(j).getGPSCoordinate().latitude + " e longitude: " +
                            stopListPartida.get(j).getGPSCoordinate().longitude + " você pode pegar o(s) ônibus: " +
                            "\n" + onibus.get(i).getRoute().getLongName() + " | " + onibus.get(i).getRoute().getShortName());
                }
            }
        }
        System.out.println("____________________________________________________________________________________");
    }

    /**
     * Método que transforma duas cordenadas(Latitude e Longitude) em um KDData
     *
     * @param latitude
     * @param longitude
     * @return KDData com latitude e longitude em Double
     * @pvmeira
     */
    public static KDData transformaParaKDData(String latitude, String longitude) {
        return new KDData(Double.valueOf(latitude), Double.valueOf(longitude));
    }
}
