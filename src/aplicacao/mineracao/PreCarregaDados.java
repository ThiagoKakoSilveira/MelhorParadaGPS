package aplicacao.mineracao;

import aplicacao.data.StopData;
import aplicacao.data.TripCustom;
import datastructures.KDData;
import datastructures.KDTree;
import smartcity.gtfs.*;
import smartcity.util.CSVReader;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe de MineraÃ§ao dos Dados, obtem os dados dos arquivos .txt  presentes na pasta dat,
 * e manipula esses dados conforme a necessidade.
 * <p>
 * Created by pvmeira on 01/07/17.
 */
public class PreCarregaDados {
    /**
     * Rotas : contem o nome abreviado e o nome
     * das rotas de POA
     */
    private Map<String, Route> rotas;
    /**
     * Parada:contem nome da parada e cordenadas da mesma
     * de toda as paradas de POA
     */
    private Map<String, Stop> paradas;
    /**
     * Forma : contem uma lista de cordenadas para ser utlilizada
     * conforme necessidade de polignos, polilinha etc..
     */
    private Map<String, Shape> formas;
    /**
     * Servico: contem ativo/naoAtivo  e data de inicio
     * e de fim de algum serviço nas rotas de POA
     */
    private Map<String, Service> servicos;
    /**
     * Viagem:Contem Rota, Serviço, forma, acessivel a cadeiras de rodas
     * e lista de paradas por onde essa viagem passa.
     */
    private Map<String, TripCustom> viagens;
    /**
     * ArvoreKdParadas:Contem um arvore no estilo KD com todas as paradas de POA
     */
    private KDTree arvoreKdParadas;

    /**
     * Metodo que inicializa todas as variáveis declaradas nessa classe
     * apartir dos arquivos armazenados na pasta data, são esses arquivos os seguintes:
     * -paradas.txt
     * -rotas.txt
     * -formas.txt
     * -calendar.txt
     * -viagens.txt
     * Exeptions : FileNotFoundException <p>Caso alguma ocorra, o mesmo método descarta todas as alterações,
     * pois para este app, sao necessárias todas as variáveis inicializadas com dados</p>
     */
    public void starUp() {

        try {
            System.out.println("Carregamento iniciado");

            this.paradas = GTFSReader.loadStops("data/stops.txt");

            this.rotas = GTFSReader.loadRoutes("data/routes.txt");

            this.formas = GTFSReader.loadShapes("data/shapes.txt");

            this.servicos = GTFSReader.loadServices("data/calendar.txt");

            this.viagens = this.loadTrips("data/trips.txt");
            this.loadStopTimes("data/stop_times.txt");


            System.out.println("Carregamento concluído");
            System.out.println("Criando ArvoreKD com base nas paradas lidas acima .");
            this.criarArvoreKdApartirDasParadas();
        } catch (FileNotFoundException e) {
            System.err.println(String.format("Erro ao fazer carregamendo dos dados : %s   -->  ", e.getMessage()));
        }

    }

    /**
     * Cria uma arvore KD com base no Map<Paradas>, para cada parada presente no map,
     * um novo no[StopData] e criado e adicionado ao array de paradas.Finalmente após todos os array criados
     * e então criada um nova KDTree com o array de paradas<p>É utilizado KDTree.class do pacote de estruturas
     * da bibliteca usada nesse App</p>.
     */
    public void criarArvoreKdApartirDasParadas() {
        KDData[] paradas = new StopData[this.paradas.values().size()];
        int cont = 0;
        for (Stop parada : this.paradas.values()) {
            paradas[cont] = new StopData(parada, parada.getGPSCoordinate().latitude, parada.getGPSCoordinate().longitude);
            cont++;
        }
        this.arvoreKdParadas = new KDTree(paradas);
        System.out.println("Árvore KD com as paradas foi criada");
    }

    /**
     * Método  para a busca das parada(s) mais próxima(s), utiliza a KDTree presente
     * nessa classe<storng>Deve estar populada</storng>
     *
     * @param data         KDData que contém Latidude e Longitude do ponto de GPS que será usado para comparar com a árvoreKD
     *                     já previamente populada nesta mesma classe.
     * @param tamanhoBusca Range do número de paradas que serão retornados pelo algoritimos presente
     *                     no método <p>findKNearestPoints</p>
     */
    public List<Stop> buscarParadasProximas(KDData data, int tamanhoBusca) {

        //TODO Fazer retorna a lista de paradas encontradas na busca do algoritimo
        KDData[] dataRetorno = new KDData[tamanhoBusca];
        arvoreKdParadas.findKNearestPoints(data, dataRetorno);

        System.out.println("Parada Mais Perto: " + ((StopData) dataRetorno[0]).getParada());
        System.out.println("Distância: " + dataRetorno[0].distance(data));
        System.out.println("------");
        for (KDData n : dataRetorno) {
            System.out.print(n + " \n");
        }
        List<Stop> paradasProximas = new ArrayList<>();
        for (int i = 0; i < dataRetorno.length; i++) {
            paradasProximas.add(((StopData) dataRetorno[i]).getParada());
        }
        return paradasProximas;
    }

    public Map<String, List<TripCustom>> obtemViagensDeParadas(List<Stop> stopList) {
        Map<String, List<TripCustom>> viagensDaParada = new HashMap<>();
        stopList.stream().forEach(stop -> {
            List<TripCustom> collect = new ArrayList<>();
            this.viagens.values().stream().forEach(trip -> {
                List<Stop> stops = trip.getStops();
                stops.stream().forEach(stop1 -> {
                    if (stop1.getId().equalsIgnoreCase(stop.getId())) {
                        collect.add(trip);
                    }
                });

            });
            viagensDaParada.put(stop.getId(), collect);
        });
        return viagensDaParada;
    }

    public Map<String, TripCustom> loadTrips(String filename) throws FileNotFoundException {
        Map<String, TripCustom> trips = new HashMap<>();
        CSVReader reader = new CSVReader(filename, ",");
        while (reader.hasNext()) {
            // read route id
            String route_id = reader.next();
            // read service id
            String service_id = reader.next();
            // read this trip id
            String id = reader.next();
            // ignore headsign, short_name
            reader.skipNext(2);
            // read direction id
            int dir = reader.nextInt();
            // ignore block_id
            reader.skipNext();
            // read shape_id
            String shape_id = reader.next();
            // read wheelchair support
            boolean w = (reader.nextInt() == 1);
            // ignore extra fields
            for (int i = 9; i < reader.getRecordSize(); i++)
                reader.skipNext();

            Route r = rotas.get(route_id);
            Service s = servicos.get(service_id);
            Shape sh = formas.get(shape_id);

            trips.put(id, new TripCustom(id, r, s, sh, dir, w));
        }
        return trips;
    }

    public void loadStopTimes(String filename) throws FileNotFoundException {
        CSVReader reader = new CSVReader(filename, ",");
        String lastTripId = null;
        TripCustom lastTrip = null;
        while (reader.hasNext()) {
            // read trip id
            String trip_id = reader.next();
            // ignore arrival and departure time
            reader.skipNext(2);
            // read stop id
            String stop_id = reader.next();
            // ignore stop_sequence
            reader.skipNext();
            if (lastTrip == null || !trip_id.equals(lastTripId)) {
                TripCustom trip = viagens.get(trip_id);
                lastTrip = trip;
            }
            Stop stop = paradas.get(stop_id);
            lastTrip.addStop(stop);
        }
    }

    public List<String> obterListaDeChavesIguais(Map<String, List<TripCustom>> mapPartida, Map<String, List<TripCustom>> mapDestino) {
        List<String> ret = new ArrayList<>();
        mapDestino.values().stream().forEach(list -> {
            for (int i = 0; i < list.size(); i++) {
                String idViagemDestino = list.get(i).getId();
                mapPartida.values().stream().forEach(a -> {
                    for (int j = 0; j <a.size(); j++) {
                        if(a.get(j).getId().equalsIgnoreCase(idViagemDestino)){

                            ret.add(a.get(j).getRoute().getShortName());

                        }

                    }
                });
            }
        });

        return ret.stream().distinct().collect(Collectors.toList());
    }

    public Map<String, Route> getRotas() {
        return rotas;
    }

    public void setRotas(Map<String, Route> rotas) {
        this.rotas = rotas;
    }

    public Map<String, Stop> getParadas() {
        return paradas;
    }

    public void setParadas(Map<String, Stop> paradas) {
        this.paradas = paradas;
    }

    public Map<String, Shape> getFormas() {
        return formas;
    }

    public void setFormas(Map<String, Shape> formas) {
        this.formas = formas;
    }

    public Map<String, Service> getServicos() {
        return servicos;
    }

    public void setServicos(Map<String, Service> servicos) {
        this.servicos = servicos;
    }

    public Map<String, TripCustom> getViagens() {
        return viagens;
    }

    public void setViagens(Map<String, TripCustom> viagens) {
        this.viagens = viagens;
    }

    public KDTree getArvoreKdParadas() {
        return arvoreKdParadas;
    }

    public void setArvoreKdParadas(KDTree arvoreKdParadas) {
        this.arvoreKdParadas = arvoreKdParadas;
    }
}
