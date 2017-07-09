package aplicacao;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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

/*        Campos Velho
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
            Map<String, List<TripCustom>> mapViagensParadaPartida = preCarregaDados.obtemViagensDeParadas(stopListPartida);
            System.out.println();
            List<Stop> stopListDestino = preCarregaDados.buscarParadasProximas(transformToKdData(splitDestino[0], splitDestino[1]), 5);
            Map<String, List<TripCustom>> mapViagensParadaDestino = preCarregaDados.obtemViagensDeParadas(stopListDestino);

            List<TripCustom> onibus = preCarregaDados.obtemListaDeOnibusCompartilhados(mapViagensParadaPartida, mapViagensParadaDestino);
            if (onibus.size() > 0) {
                System.out.println("____________________________________________________________________________________");
                System.out.println("Lista de ônibus possíveis de partida");
                for (int i = 0; i < onibus.size(); i++) {                	
            		for (int j = 0; j < stopListPartida.size(); j++) {
        				if(onibus.get(i).getStops().contains(stopListPartida.get(j))){
        					System.out.println("Na parada "+ stopListPartida.get(j).getName() + "com latitude: " + 
        								stopListPartida.get(j).getGPSCoordinate().latitude + " e longitude: " + 
        								stopListPartida.get(j).getGPSCoordinate().longitude + " você pode pegar o(s) ônibus: " + 
        							"\n"+onibus.get(i).getRoute().getLongName()+ " | " + onibus.get(i).getRoute().getShortName());
        				}
					}					
				}                
                System.out.println("____________________________________________________________________________________");
            } else {
                //TODO Criar algoritimo para identifiicar os ônibus(mais de um);
                System.out.println("__________________________________________________");
                System.out.println("Necessário Pegar dois ônibus");
                System.out.println("__________________________________________________");

                Collection<List<TripCustom>> partida = mapViagensParadaPartida.values();
                Collection<List<TripCustom>> chegada = mapViagensParadaDestino.values();
                boolean achou = false;
                
                for(List<TripCustom> tclp: partida){
                	for(TripCustom tcp: tclp){
                		for(Stop s: tcp.getStops()){
                			for(List<TripCustom> tclc: chegada){
                            	for(TripCustom tcc: tclc){
                            		if(tcp.getRoute().getShortName().equals(tcc.getRoute().getShortName())){
                            			for(Stop s2: tcc.getStops()){
                                			if(s.getId().equals(s2.getId())){
                                				System.out.println("Onibus origem " + tcp.getRoute().getLongName());
                                				System.out.println("Onibus destino " + tcc.getRoute().getLongName());
                                				System.out.println("Parada intermediária" + s);
                                				achou = true;
                                				break;
                                			}
                                		}
                        			}                            		
                            	}
                            }
                		}
                	}
                }                
            }

        }catch (ParseException e) {
            System.err.println("Impossível parsear essa String para Inteiro    :  " + e.getMessage());
        }
        catch (IndexOutOfBoundsException e) {
            System.err.println("Estourou o array da lista    :  " + e.getMessage());
        } 
        catch (Exception e) {
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
