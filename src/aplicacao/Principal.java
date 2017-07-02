package aplicacao;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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

        System.out.println("Partida  --------");
        System.out.println("");
        System.out.println("Latitude  --------");
        String latitudePartida = scanner.nextLine();

        System.out.println("Longitude  --------");
        String longitudePartida = scanner.nextLine();

        System.out.println("DESTINO  --------");
        System.out.println("");
        System.out.println("Latitude  --------");
        String latitudeDestino = scanner.nextLine();

        System.out.println("Longitude  --------");
        String longitudePDestino = scanner.nextLine();

/*        Campos Velho
       -30.0940906, -51.2267142
          Senac POA
        -30.0351924, -51.2266259
*/

        try {
            List<Stop> stopListPartida = preCarregaDados.buscarParadasProximas(new KDData(-30.0940906, -51.2267142), 8);
            Map<String, List<TripCustom>> mapViagensParadaPartida = preCarregaDados.obtemViagensDeParadas(stopListPartida);
//            -30.0915726,-51.2208331
            System.out.println();
            List<Stop> stopListDestino = preCarregaDados.buscarParadasProximas(new KDData(-30.0410407, -51.071493), 8);
            Map<String, List<TripCustom>> mapViagensParadaDestino = preCarregaDados.obtemViagensDeParadas(stopListDestino);

            List<String> strings = preCarregaDados.obterListaDeChavesIguais(mapViagensParadaPartida, mapViagensParadaDestino);
            if (strings.size() > 0) {
                System.out.println("__________________________________________________");
                System.out.println("Lista de onibus possiveis para ir ate seu destino");
                strings.stream().forEach(System.out::println);
                System.out.println("__________________________________________________");
            } else {
                //TODO Criar algoritimo para identifiicar os onibus(mais de um);
                System.out.println("__________________________________________________");
                System.out.println("Necessario Pegar dois onibus");
                System.out.println("__________________________________________________");
            }

        } catch (Exception e) {
            System.err.println("Erro ao processar paradas proximas   :  " + e.getMessage());
        }

        scanner.close();

    }

    /**
     * Metodo que transforma duas cordenadas(Latitude e Longitude) em um KDData
     *
     * @param latitude
     * @param longitude
     * @return KDData com latitude e longitude em Double
     * @pvmeira
     */
    public static KDData transformToKdData(String latitude, String longitude) {
        return new KDData(Double.valueOf(latitude), Double.valueOf(longitude));
    }
}
