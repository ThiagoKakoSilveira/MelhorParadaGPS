package aplicacao;

import java.io.FileNotFoundException;
import java.util.Scanner;

import aplicacao.mineracao.PreCarregaDados;
import datastructures.KDData;

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
            preCarregaDados.buscarParadasProximas(transformToKdData(latitudePartida, longitudePartida), 8);

            preCarregaDados.buscarParadasProximas(transformToKdData(latitudeDestino, longitudePDestino), 8);
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
