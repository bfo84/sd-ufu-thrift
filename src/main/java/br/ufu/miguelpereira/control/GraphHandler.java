package br.ufu.miguelpereira.control;

import br.ufu.miguelpereira.thrift.*;

import br.ufu.miguelpereira.hash.*;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class GraphHandler implements Operations.Iface {

    private static final String LOCALHOST = "localhost";
	//private ArrayList<Graph> Graphs = new ArrayList<Graph>();
    private Graph graph = new Graph(new ArrayList<Vertex>(), new ArrayList<Edge>());
    private Object fileLock = new Object();
    private static Map<String, String> serversPort;

    private TTransport[] transports;
    private TProtocol[] protocols;
    private Operations.Client[] clients;
    public int serverPort; // number of the port of this server
    private int NUMBER_SERVERS; // number of servers
    private int serverId;

    public GraphHandler(String [] args) {
        System.out.println("Primeiro argumento: "+args[0]);
        serversPort = TableServer.getMapServers(args[0], args[2]);

        NUMBER_SERVERS = Integer.parseInt(args[0]);
        serverId = Integer.parseInt(args[1]);
        System.out.println("ServerId: "+serverId);
        int firstPort = Integer.parseInt(args[2]);
        serverPort = firstPort + serverId;

        transports = new TTransport[1];
        protocols = new TProtocol[1];
        clients = new Operations.Client[1];
    }

    public TTransport[] startServers() throws TTransportException {
        TTransport[] listOfServers = new TTransport[NUMBER_SERVERS];
        int counter = 0;

        try {
            for (Map.Entry<String, String> entry : serversPort.entrySet()) {
                listOfServers[counter] = new TSocket(LOCALHOST, Integer.parseInt(entry.getKey()));
                listOfServers[counter].open();
                counter++;
            }
        } catch (TTransportException e) {
            System.out.print(e);
        }

        return listOfServers;
    }

    public Operations.Client[] createClients(TTransport[] transports) {
        Operations.Client[] clients = new Operations.Client[NUMBER_SERVERS];
        TProtocol[] listOfProtocols = new TProtocol[NUMBER_SERVERS];
        int cont = 0;
        for (TTransport transport : transports) {
            listOfProtocols[cont] = new TBinaryProtocol(transport);
            clients[cont] = new Operations.Client(listOfProtocols[cont]);
            cont++;
        }
        return clients;
    }

    public void disconnectServers(TTransport[] transports) {
        for (TTransport transport : transports) {
            transport.close();
        }
    }

    public TTransport connectToServerId(int id) {
        try {
            int port = Integer.valueOf(serversPort.get(Integer.toString(id)));
            TTransport transport = new TSocket(LOCALHOST, port);
            transport.open();
            System.out.println("Server " + serverPort + " connected to server " + port);
            return transport;
        } catch (TTransportException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Operations.Client createClientRequest(TTransport transport) {
        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        Operations.Client client = new Operations.Client(protocol);
        return client;
    }

    public void disconnectServer(TTransport transport) {
        transport.close();
        System.out.println("Connection from " + serverPort + " closed");
    }

    @Override
    public void loadGraph(String caminho) {
        Object aux = null;
        synchronized (fileLock) {
            try {
                FileInputStream restFile = new FileInputStream(caminho);
                ObjectInputStream stream = new ObjectInputStream(restFile);

                aux = stream.readObject();
                if (aux != null) {
                    synchronized (graph) {
                        graph = (Graph) aux;
                    }
                }
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void saveGraph(String caminho) {
        synchronized (fileLock) {
            try {
                FileOutputStream saveFile = new FileOutputStream(caminho);
                ObjectOutputStream stream = new ObjectOutputStream(saveFile);
                synchronized (graph) {
                    stream.writeObject(graph);
                }
                stream.close();
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        }
    }

    private int getServerId(int vertice) {
        try {
            return MD5.getGenerateServerId(String.format("%d", vertice), String.format("%d", NUMBER_SERVERS));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean createVertex(int nome, int cor, String descricao, double peso) {
        int server = getServerId(nome);
        System.out.println("Server: "+server);
        if (server != serverId) {
            try {
                TTransport transport = connectToServerId(server);
                Operations.Client client = createClientRequest(transport);
                boolean p = client.createVertex(nome, cor, descricao, peso);
                disconnectServer(transport);
                return p;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        synchronized (graph.getV()) { //Lock na lista para evitar duplicidade de nome
            if (graph.getV() != null) {
                for (Vertex vertex : graph.getV()) {
                    if (vertex.getNome() == nome) {
                        System.out.println("vertice: "+vertex.getNome());
                        return false;
                    }
                }
            }
            graph.getV().add(new Vertex(nome, cor, descricao, peso));
        }
        return true;
    }

    @Override
    public boolean createEdge(int v1, int v2, double peso, int flag, String descricao) {
        int vertexCounter = 0; //Contador de quantos vertices existem

        int server = getServerId(v1);
        if (server != serverId) {
            try {
                TTransport transport = connectToServerId(server);
                Operations.Client client = createClientRequest(transport);
                boolean p = client.createEdge(v1, v2, peso, flag, descricao);
                disconnectServer(transport);
                return p;
            } catch (Exception e) {
                e.printStackTrace();
                //throw
            }
        }
        synchronized (graph.getV()) { //Lock nos vertex caso haja delecao em um dos vertex da edge
            for (Vertex vertex : graph.getV()) {
                if (vertex.getNome() == v1 || vertex.getNome() == v2) {
                    vertexCounter++;
                }
            }
            if (vertexCounter == 1) {
                Vertex vertex = null;
                int server2 = getServerId(v2);
                if (server2 != serverId) {
                    try {
                        TTransport transport = connectToServerId(server2);
                        Operations.Client client = createClientRequest(transport);
                        vertex = client.getVertex(v2);
                        disconnectServer(transport);
                    } catch (Exception e) {
                        e.printStackTrace();
                        //throw
                    }
                }
                if (vertex == null) {
                    return false;
                } else {
                    vertexCounter++;
                }
            }
            if (vertexCounter > 1) {
                Edge aux2 = new Edge(v1, v2, peso, flag, descricao);
                synchronized (graph.getA()) { //Lock nas edges para evitar duplicidade
                    if (!ifEquals(aux2)) {
                        if (flag == 2) {
                            int server3 = getServerId(v1);
                            if (server3 != serverId) {
                                try {
                                    TTransport transport = connectToServerId(server3);
                                    Operations.Client client = createClientRequest(transport);
                                    boolean p = client.createEdge(v2, v1, peso, flag, descricao);
                                    disconnectServer(transport);
                                    if (!p) return p;
                                    graph.getA().add(aux2);
                                    return p;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    //throw
                                }
                            }
                            Edge aux = new Edge(v2, v1, peso, flag, descricao);
                            if (!ifEquals(aux)) { //Se nao existir, cria
                                graph.getA().add(aux);
                            } else {//Se existir, atualiza
                                updateEdge(aux.getV1(), aux.getV2(), aux);
                            }
                        }
                        graph.getA().add(aux2);
                        return true;
                    }
                    return true;

                }

            }
        }
        return false;
    }

    @Override
    public boolean deleteVertex(int nome) {
        ArrayList<Edge> forDeletion = new ArrayList<>();
        synchronized (graph.getA()) {
            for (Edge a : graph.getA()) {
                if (a.getV1() == nome || a.getV2() == nome) {
                    forDeletion.add(a);
                }
            }
            for (Edge a : forDeletion) {
                graph.getA().remove(a);
            }
        }
        for (Vertex vertex : graph.getV()) {
            synchronized (vertex) {
                if (vertex.getNome() == nome) {
                    graph.getV().remove(vertex);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean deleteEdge(int v1, int v2) {
        int server = getServerId(v1);
        if (server != serverId) {
            try {
                TTransport transport = connectToServerId(server);
                Operations.Client client = createClientRequest(transport);
                boolean p = client.deleteEdge(v1, v2);
                disconnectServer(transport);
                return p;
            } catch (Exception e) {
                e.printStackTrace();
                //throw
            }
        }
        for (Edge edge : graph.getA()) {
            synchronized (edge) {
                if (edge.getV1() == v1 && edge.getV2() == v2) {
                    graph.getA().remove(edge);
                    if (edge.getFlag() == 2) {
                        int server2 = getServerId(v2);
                        if (server2 != serverId) {
                            try {
                                TTransport startedServer = connectToServerId(server2);
                                Operations.Client client = createClientRequest(startedServer);
                                boolean p = client.deleteEdge(v2, v1);
                                disconnectServer(startedServer);
                                return p;
                            } catch (Exception e) {
                                e.printStackTrace();
                                //throw
                            }
                        } else {
                            deleteEdge(v2, v1);
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean updateVertex(int nomeUp, Vertex V) {
        int server = getServerId(nomeUp);
        if (server != serverId) {
            try {
                TTransport startedServer = connectToServerId(server);
                Operations.Client client = createClientRequest(startedServer);
                boolean p = client.updateVertex(nomeUp, V);
                disconnectServer(startedServer);
                return p;
            } catch (Exception e) {
                e.printStackTrace();
                //throw
            }
        }
        if (V == null) {
            return false;
        }
        if (nomeUp != V.getNome()) {
            return false;
        }
        for (Vertex v : graph.getV()) {
            synchronized (v) {
                if (v.getNome() == nomeUp) {
                    v.setCor(V.getCor());
                    v.setDescricao(V.getDescricao());
                    v.setPeso(V.getPeso());
                    return true;
                }
            }
        }
        return false;
    }

    public boolean ifEquals(Edge A) {
        synchronized (graph.getA()) {
            for (Edge edge : graph.getA()) {
                if (edge.getV1() == A.getV1() && edge.getV2() == A.getV2()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean updateEdge(int nomeV1, int nomeV2, Edge edge) {
        int server = getServerId(nomeV1);
        if (server != serverId) {
            try {
                TTransport startedServer = connectToServerId(server);
                Operations.Client client = createClientRequest(startedServer);
                boolean p = client.updateEdge(nomeV1, nomeV2, edge);
                disconnectServer(startedServer);
                return p;
            } catch (Exception e) {
                e.printStackTrace();
                //throw
            }
        }
        if (edge == null) {
            return false;
        }
        if (nomeV1 != edge.getV1() || nomeV2 != edge.getV2()) {
            return false;
        }
        for (Edge edgeGraph : graph.getA()) {
            synchronized (edgeGraph) {
                if (edgeGraph.getV1() == nomeV1 && edgeGraph.getV2() == nomeV2) {
                    if (edgeGraph.getFlag() == 2) {// Se aresta antiga for bi-direcional, pega aresta v2,v1
                        int server2 = getServerId(nomeV2);
                        if (server2 != serverId) {
                            try {
                                TTransport startedServer = connectToServerId(server2);
                                Operations.Client client = createClientRequest(startedServer);
                                boolean p = client.updateEdge(nomeV2, nomeV1, edge);
                                disconnectServer(startedServer);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Edge b = getEdge(edgeGraph.getV2(), edgeGraph.getV1());
                            synchronized (b) {
                                if (edge.getFlag() == 1) {// Se aresta nova for direcionada, remove aresta v2,v1
                                    graph.getA().remove(b);
                                } else { // Senao, update aresta v2,v1
                                    b.setPeso(edge.getPeso());
                                    b.setFlag(edge.getFlag());
                                    b.setDescricao(edge.getDescricao());
                                }
                            }
                        }
                    } else {// Se aresta antiga for direcionada
                        if (edge.getFlag() == 2) {// E aresta nova for bi-direcional, cria aresta v2,v1
                            int server3 = getServerId(nomeV2);
                            if (server3 != serverId) {
                                try {
                                    TTransport startedServer = connectToServerId(server3);
                                    Operations.Client client = createClientRequest(startedServer);
                                    boolean p = client.createEdge(edge.getV2(), edge.getV1(), edge.getPeso(), edge.getFlag(), edge.getDescricao());
                                    disconnectServer(startedServer);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            Edge aux = new Edge(edge.getV2(), edge.getV1(), edge.getPeso(), edge.getFlag(), edge.getDescricao());
                            if (!ifEquals(aux)) {
                                graph.getA().add(aux);
                            }
                        }
                    }// Em todos os casos, update aresta v1,v2
                    edgeGraph.setPeso(edge.getPeso());
                    edgeGraph.setFlag(edge.getFlag());
                    edgeGraph.setDescricao(edge.getDescricao());
                    return true;
                }
            }
        }
        return false;
    }

        @Override
        public boolean updateGraph (java.util.List < Vertex > V, java.util.List < Edge > A){
            synchronized (graph) {
                graph.setV(V);
                graph.setA(A);
                return true;
            }
        }

        @Override
        public Vertex getVertex ( int nome){
            int server = getServerId(nome);
            if (server != serverId) {
                try {
                    TTransport startedServer = connectToServerId(server);
                    Operations.Client client = createClientRequest(startedServer);
                    Vertex vertex = client.getVertex(nome);
                    disconnectServer(startedServer);
                    return vertex;
                } catch (Exception e) {
                    e.printStackTrace();
                    //throw
                }
            }
            synchronized (graph.getV()) {
                if (!graph.getV().isEmpty()) {
                    for (Vertex v : graph.getV()) {
                        if (v.getNome() == nome) {
                            return v;
                        }
                    }
                }
            }
            return null;
        }

        @Override
        public Edge getEdge ( int v1, int v2){
            int server = getServerId(v1);
            if (server != serverId) {
                try {
                    TTransport startedServer = connectToServerId(server);
                    Operations.Client client = createClientRequest(startedServer);
                    Edge edge = client.getEdge(v1, v2);
                    disconnectServer(startedServer);
                    return edge;
                } catch (Exception e) {
                    e.printStackTrace();
                    //throw
                }
            }
            synchronized (graph.getA()) {
                if (!graph.getA().isEmpty()) {
                    for (Edge a : graph.getA()) {
                        if (a.getV1() == v1 && a.getV2() == v2) {
                            return a;
                        }
                    }
                }
            }
            return null;
        }

        @Override
        public Graph showGraph () throws TTransportException {
            int server;
            TTransport[] servers = startServers();
            Operations.Client[] clients = createClients(servers);
            Graph localGraph = new Graph(new ArrayList<Vertex>(), new ArrayList<Edge>());
            ArrayList<Vertex> listOfVertex;
            ArrayList<Edge> listOfEdge;
            for (Map.Entry<String, String> entry : serversPort.entrySet()) {
                server = Integer.parseInt(entry.getValue());
                if (server != serverId) {
                    try {
                        listOfVertex = (ArrayList<Vertex>) clients[server].showVertex();
                        listOfEdge = (ArrayList<Edge>) clients[server].showEdge();
                        localGraph.V.addAll(listOfVertex);
                        localGraph.A.addAll(listOfEdge);
                    } catch (Exception e) {
                        e.printStackTrace();
                        //throw
                    }
                } else {
                    listOfEdge = (ArrayList<Edge>) showEdge();
                    listOfVertex = (ArrayList<Vertex>) showVertex();
                    localGraph.V.addAll(listOfVertex);
                    localGraph.A.addAll(listOfEdge);
                }
            }
            return localGraph;
        }

        @Override
        public List<Vertex> showVertex () {
            ArrayList<Vertex> vertices = new ArrayList<>();
            synchronized (graph.getV()) {
                for (Vertex v : graph.getV()) {
                    vertices.add(v);
                }
            }
            return vertices;
        }

        @Override
        public List<Edge> showEdge () {
            ArrayList<Edge> arestas = new ArrayList<>();
            synchronized (graph.getA()) {
                for (Edge a : graph.getA()) {
                    arestas.add(a);
                }
            }
            return arestas;
        }

        @Override
        public List<Vertex> showVertexOfEdges ( int v1, int v2){
            ArrayList<Vertex> vertices = new ArrayList<>();
            vertices.add(getVertex(v1));
            vertices.add(getVertex(v2));
            return vertices;
        }

        @Override
        public List<Edge> showEdgesOfVertex (int nomeV){
            int server = getServerId(nomeV);
            List<Edge> arestas = new ArrayList<>();
            if (server != serverId) {
                try{
                    TTransport transport = connectToServerId(server);
                    Operations.Client client = createClientRequest(transport);
                    arestas = client.showEdgesOfVertex(nomeV);
                    disconnectServer(transport);
                    return arestas;
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            synchronized (graph.getA()) {
                for (Edge a : graph.getA()) {
                    System.out.println("Aresta: "+a.v1);
                    if (a.getV1() == nomeV || a.getV2() == nomeV) {
                        System.out.println(nomeV);
                        arestas.add(a);
                    }
                }
            }
            return arestas;
        }

        @Override
        public List<Vertex> showAdjacency ( int nomeV){
            ArrayList<Vertex> adjacentes = new ArrayList<>();
            synchronized (graph.getA()) {
                for (Edge a : graph.getA()) {
                    if (a.getV1() == nomeV) {
                        if (!adjacentes.contains(getVertex(a.getV2())))
                            adjacentes.add(getVertex(a.getV2()));

                    } else if (a.getV2() == nomeV) {
                        if (!adjacentes.contains(getVertex(a.getV1())))
                            adjacentes.add(getVertex(a.getV1()));
                    }
                }
            }
            return adjacentes;
        }

    private Vertex findVertice(Graph graph, int vertex) {
        for (Vertex v : graph.getV()) {
            if (v.nome == vertex) {
                return v;
            }
        }
        return null;
    }

    @Override
    public List<Vertex> smallerPath(int nomeV1, int nomeV2) {
        Graph fullGraph = null;
        try {
            fullGraph = showGraph();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Vertex originVertex = findVertice(fullGraph, nomeV1);
        Vertex destinationVertex = findVertice(fullGraph, nomeV2);

        Dijkstra algorithm = new Dijkstra(fullGraph);

        algorithm.executa(originVertex);

        return algorithm.getCaminho(destinationVertex);
    }
}
