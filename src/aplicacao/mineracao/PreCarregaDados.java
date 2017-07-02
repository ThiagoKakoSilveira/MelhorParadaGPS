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
 * Classe de Mineração dos Dados, obtém os dados dos arquivos .txt  presentes na pasta dat,
 * e manipula esses dados conforme a necessidade.
 * <p>
 * Created by pvmeira on 01/07/17.
 */
public class PreCarregaDados {
    /**
     * Rotas : contém o nome abreviado e o nome
     * das rotas de POA
     */
    private Map<String, Route> rotas;
    /**
     * Parada:contém nome da parada e cordenadas da mesma
     * de toda as paradas de POA
     */
    private Map<String, Stop> paradas;
    /**
     * Forma : contém uma lista de cordenadas para ser utlilizada
     * conforme necessidade de polignos, polilinha etc..
     */
    private Map<String, Shape> formas;
    /**
     * Servico: contém ativo/nãoAtivo  e data de início
     * e de fim de algum serviços nas rotas de POA
     */
    private Map<String, Service> servicos;
    /**
     * Viagem:Contem Rota, Serviço, forma, acessível a cadeiras de rodas
     * e lista de paradas por onde essa viagem passa.
     */
    private Map<String, TripCustom> viagens;
    /**
     * ArvoreKdParadas:Contem um árvore no estilo KD com todas as paradas de POA
     */
    private KDTree arvoreKdParadas;

    /**
     * Metodo que inicializa todas as variáveis declaradas nessa classe
     * apartir dos arquivos armazenados na pasta data, serão esses arquivos os seguintes:
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

            this.viagens = this.carregarViagens("data/trips.txt");
            this.carregarTempoParadas("data/stop_times.txt");


            System.out.println("Carregamento concluído");
            System.out.println("Criando ArvoreKD com base nas paradas lidas acima .");
            this.criarArvoreKdApartirDasParadas();
        } catch (FileNotFoundException e) {
            System.err.println(String.format("Erro ao fazer carregamendo dos dados : %s   -->  ", e.getMessage()));
        }

    }

    /**
     * Cria uma árvore KD com base no Map<Paradas>, para cada parada presente no map,
     * um novo no[StopData] e criado e adicionado ao array de paradas.Finalmente apï¿½s todos os array criados
     * e entï¿½o criada um nova KDTree com o array de paradas<p>ï¿½ utilizado KDTree.class do pacote de estruturas
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
        System.out.println("árvore KD com as paradas foi criada");
    }

    /**
     * Mï¿½todo  para a busca das parada(s) mais prï¿½xima(s), utiliza a KDTree presente
     * nessa classe<storng>Deve estar populada</storng>
     *
     * @param data         KDData que contï¿½m Latidude e Longitude do ponto de GPS que serï¿½ usado para comparar com a ï¿½rvoreKD
     *                     jï¿½ previamente populada nesta mesma classe.
     * @param tamanhoBusca Range do nï¿½mero de paradas que serï¿½o retornados pelo algoritimos presente
     *                     no mï¿½todo <p>findKNearestPoints</p>
     */
    public List<Stop> buscarParadasProximas(KDData data, int tamanhoBusca) {

        //TODO Fazer retorna a lista de paradas encontradas na busca do algoritimo
        KDData[] dataRetorno = new KDData[tamanhoBusca];
        arvoreKdParadas.findKNearestPoints(data, dataRetorno);

        System.out.println("Parada Mais Perto: " + ((StopData) dataRetorno[0]).getParada());
        System.out.println("Distï¿½ncia: " + dataRetorno[0].distance(data));
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

    /**
     * Metodo que recebe uma lista de paradas e para cada parada, busca todas as
     * viagens que passam por aquela parada e armazena em uma <p> MAP<idParada,ListaDeViagensN> </p>
     *
     * @param listaDeParadas proximas a cordenada
     * @return Lista de Viagens para cada parada
     */
    public Map<String, List<TripCustom>> obtemViagensDeParadas(List<Stop> listaDeParadas) {
        Map<String, List<TripCustom>> viagensDaParada = new HashMap<>();
        listaDeParadas.stream().forEach(parada -> {
            List<TripCustom> listaDeViagens = new ArrayList<>();
            this.viagens.values().stream().forEach(viagem -> {
                List<Stop> paradas = viagem.getStops();
                paradas.stream().forEach(paradaViagemAtual -> {
                    if (paradaViagemAtual.getId().equalsIgnoreCase(parada.getId())) {
                        listaDeViagens.add(viagem);
                    }
                });

            });
            viagensDaParada.put(parada.getId(), listaDeViagens);
        });
        return viagensDaParada;
    }

    /**
     * Carrega todas as viagens de Porto Alegre que estao contidas no arquivo passado
     *
     * @param nomeArquivo local onde esta armazenado o arquivo contenso as Viagens
     * @return <p>Map<IdViagem,ViagemCustom> </p>  contendo todas as viagens que existem no arquivo
     * @throws FileNotFoundException quando o nomeArquivo for invalido
     */
    public Map<String, TripCustom> carregarViagens(String nomeArquivo) throws FileNotFoundException {
        Map<String, TripCustom> viagens = new HashMap<>();
        CSVReader leitor = new CSVReader(nomeArquivo, ",");
        while (leitor.hasNext()) {
            // read route id
            String route_id = leitor.next();
            // read service id
            String service_id = leitor.next();
            // read this trip id
            String id = leitor.next();
            // ignore headsign, short_name
            leitor.skipNext(2);
            // read direction id
            int dir = leitor.nextInt();
            // ignore block_id
            leitor.skipNext();
            // read shape_id
            String shape_id = leitor.next();
            // read wheelchair support
            boolean w = (leitor.nextInt() == 1);
            // ignore extra fields
            for (int i = 9; i < leitor.getRecordSize(); i++)
                leitor.skipNext();

            Route rota = rotas.get(route_id);
            Service service = servicos.get(service_id);
            Shape forma = formas.get(shape_id);

            viagens.put(id, new TripCustom(id, rota, service, forma, dir, w));
        }
        return viagens;
    }


    /**
     * Metodo que adiciona todas as paradas que pertence as viagens
     *
     * @param nomeArquivo local onde esta armazenado o arquivo contenso as Viagens
     * @throws FileNotFoundException quando o nomeArquivo for invalido
     */
    public void carregarTempoParadas(String nomeArquivo) throws FileNotFoundException {
        CSVReader leitor = new CSVReader(nomeArquivo, ",");
        String ultimaViagemId = null;
        TripCustom ultimaViagem = null;
        while (leitor.hasNext()) {
            // read trip id
            String trip_id = leitor.next();
            // ignore arrival and departure time
            leitor.skipNext(2);
            // read stop id
            String stop_id = leitor.next();
            // ignore stop_sequence
            leitor.skipNext();
            if (ultimaViagem == null || !trip_id.equals(ultimaViagemId)) {
                TripCustom viagem = viagens.get(trip_id);
                ultimaViagem = viagem;
            }
            Stop parada = paradas.get(stop_id);
            ultimaViagem.addStop(parada);
        }
    }


    /**
     * Metodo que obtem a lista de onibus em comum entre o Destino e o ponto de Partida
     * 1ÂºPasso - Obtem lista de Viagens do Destino
     * 2ÂºPara cada Viagens da lista de Viagens do Destino obtem idViagemDestino em questao
     * 3ÂºPara cada Viagem da Lista de Viagens da Partida obtem o idViagemPartida
     * 4ÂºCompara os dois IDs (idViagemDestino , idViagemPartida)
     * 5ÂºSe for igual adiciona a listaDeOnibusParaDestino.
     *
     * @param mapaPartida Map<IdParada,List<TripCustom> listDeViagensDaParada> contem lista de viagens das paradas de Partida
     * @param mapaDestino Map<IdParada,List<TripCustom> listDeViagensDaParada> contem lista de viagens das paradas de Destino
     * @return Lista contendo o nome completo do onibus / o nome abreviado do onibus
     */
    public List<String> obtemListaDeOnibusCompartilhados(Map<String, List<TripCustom>> mapaPartida, Map<String, List<TripCustom>> mapaDestino) {
        List<String> listaDeOnibusParaDestino = new ArrayList<>();

        mapaDestino.values().stream().forEach(listaDeViagensDestino -> {

            for (int posicaoListaViagemDestino = 0; posicaoListaViagemDestino < listaDeViagensDestino.size(); posicaoListaViagemDestino++) {
                String idViagemDestino = listaDeViagensDestino.get(posicaoListaViagemDestino).getId();

                mapaPartida.values().stream().forEach(listaDeViagensPartida -> {

                    for (int posicaoListaViagemPartida = 0; posicaoListaViagemPartida < listaDeViagensPartida.size(); posicaoListaViagemPartida++) {

                        if (listaDeViagensPartida.get(posicaoListaViagemPartida).getId().equalsIgnoreCase(idViagemDestino)) {
                            String nomeOnibusDestino = listaDeViagensPartida.get(posicaoListaViagemPartida).getRoute().getLongName()
                                    + " | " + listaDeViagensPartida.get(posicaoListaViagemPartida).getRoute().getShortName();
                            listaDeOnibusParaDestino.add(nomeOnibusDestino);

                        }

                    }
                });
            }
        });

        return listaDeOnibusParaDestino.stream().distinct().collect(Collectors.toList());
    }


}
