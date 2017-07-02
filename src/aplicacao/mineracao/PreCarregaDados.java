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
 * Classe de Mineração dos Dados, obtem os dados dos arquivos .txt  presentes na pasta dat,
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
     * Servico: contém ativo/nãoAtivo  e data de inicio
     * e de fim de algum serviço nas rotas de POA
     */
    private Map<String, Service> servicos;
    /**
     * Viagem:Contém Rota, Serviço, forma, acessivel a cadeiras de rodas
     * e lista de paradas por onde essa viagem passa.
     */
    private Map<String, TripCustom> viagens;
    /**
     * ArvoreKdParadas:Contém um árvore no estilo KD com todas as paradas de POA
     */
    private KDTree arvoreKdParadas;

    /**
     * Método que inicializa todas as variáveis declaradas nessa classe
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
     * um novo no[StopData] e criado e adicionado ao array de paradas.Finalmente após todos os array criados
     * e então criada um nova KDTree com o array de paradas<p> utilizado KDTree.class do pacote de estruturas
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

    /**
     * Método que recebe uma lista de paradas e para cada parada, busca todas as
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
     * Carrega todas as viagens de Porto Alegre que estão contidas no arquivo passado
     *
     * @param nomeArquivo local onde esta armazenado o arquivo contenso as Viagens
     * @return <p>Map<IdViagem,ViagemCustom> </p>  contendo todas as viagens que existem no arquivo
     * @throws FileNotFoundException quando o nomeArquivo for inválido
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
     * Método que adiciona todas as paradas que pertence as viagens
     *
     * @param nomeArquivo local onde esta armazenado o arquivo contenso as Viagens
     * @throws FileNotFoundException quando o nomeArquivo for inválido
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
     * Método que obtém a lista de ônibus em comum entre o Destino e o ponto de Partida
     * 1ºPasso - Obtém lista de Viagens do Destino
     * 2ºPara cada Viagens da lista de Viagens do Destino obtém idViagemDestino em questao
     * 3ºPara cada Viagem da Lista de Viagens da Partida obtém o idViagemPartida
     * 4ºCompara os dois IDs (idViagemDestino , idViagemPartida)
     * 5ºSe for igual adiciona a listaDeOnibusParaDestino.
     *
     * @param mapaPartida Map<IdParada,List<TripCustom> listDeViagensDaParada> contém lista de viagens das paradas de Partida
     * @param mapaDestino Map<IdParada,List<TripCustom> listDeViagensDaParada> contém lista de viagens das paradas de Destino
     * @return Lista contendo o nome completo do ônibus / o nome abreviado do ônibus
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
