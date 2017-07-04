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

        System.out.println("Dados da Partida ");
        System.out.println("Latitude , Longitude");
        String latitudeLongitude = scanner.nextLine();
        String[] splitPartida = latitudeLongitude.split(",");

        System.out.println("Dados do Destino ");
        System.out.println("Latitude , Longitude");
        String dadosDestino = scanner.nextLine();
        String[] splitDestino = dadosDestino.split(",");

/*        Campos Velho
       -30.0940906, -51.2267142

          Senac POA
        -30.0351924, -51.2266259

        //perto casa(Nutripao)
        -30.0937542,-51.230038

        //PUC
        -30.0570755,-51.1741819
*/

        try {
            List<Stop> stopListPartida = preCarregaDados.buscarParadasProximas(transformToKdData(splitPartida[0], splitPartida[1]), 5, 
            		Double.valueOf(splitPartida[0]), Double.valueOf(splitPartida[1]));
            Map<String, List<TripCustom>> mapViagensParadaPartida = preCarregaDados.obtemViagensDeParadas(stopListPartida);
            System.out.println();
            List<Stop> stopListDestino = preCarregaDados.buscarParadasProximas(transformToKdData(splitDestino[0], splitDestino[1]), 5, 
            		Double.valueOf(splitPartida[0]), Double.valueOf(splitPartida[1]));
            Map<String, List<TripCustom>> mapViagensParadaDestino = preCarregaDados.obtemViagensDeParadas(stopListDestino);

            List<String> strings = preCarregaDados.obtemListaDeOnibusCompartilhados(mapViagensParadaPartida, mapViagensParadaDestino);
            if (strings.size() > 0) {
                System.out.println("__________________________________________________");
                System.out.println("Lista de ônibus possíveis para ir até seu destino");
                strings.stream().forEach(System.out::println);
                System.out.println("__________________________________________________");
            } else {
                //TODO Criar algoritimo para identifiicar os ônibus(mais de um);
                System.out.println("__________________________________________________");
                System.out.println("Necessário Pegar dois ônibus");
                System.out.println("__________________________________________________");
            }

        } catch (Exception e) {
            System.err.println("Erro ao processar paradas próximas   :  " + e.getMessage());
        }

        scanner.close();

    }

    /**
     * Método que transforma duas cordenadas(Latitude e Longitude) em um KDData
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
