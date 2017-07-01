package aplicacao.mineracao;

import aplicacao.data.StopData;
import datastructures.KDData;
import datastructures.KDTree;
import smartcity.gtfs.*;

import java.io.FileNotFoundException;
import java.util.Map;

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
    private Map<String, Trip> viagens;
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

            this.viagens = GTFSReader.loadTrips("data/trips.txt", rotas, servicos, formas);


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
    public void buscarParadasProximas(KDData data, int tamanhoBusca) {

        //TODO Fazer retorna a lista de paradas encontradas na busca do algoritimo
        KDData[] dataRetorno = new KDData[tamanhoBusca];
        arvoreKdParadas.findKNearestPoints(data, dataRetorno);

        System.out.println("Parada Mais Perto: " + ((StopData) dataRetorno[0]).getParada());
        System.out.println("Distância: " + dataRetorno[0].distance(data));
        System.out.println("------");
        for (KDData n : dataRetorno) {
            System.out.print(n + " \n");
        }
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

    public Map<String, Trip> getViagens() {
        return viagens;
    }

    public void setViagens(Map<String, Trip> viagens) {
        this.viagens = viagens;
    }

    public KDTree getArvoreKdParadas() {
        return arvoreKdParadas;
    }

    public void setArvoreKdParadas(KDTree arvoreKdParadas) {
        this.arvoreKdParadas = arvoreKdParadas;
    }
}
