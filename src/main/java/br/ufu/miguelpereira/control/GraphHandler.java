package br.ufu.miguelpereira.control;

import br.ufu.miguelpereira.thrift.*;

import br.ufu.miguelpereira.hash.*;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class GraphHandler implements Operations.Iface {

    //private ArrayList<Graph> Graphs = new ArrayList<Graph>();
    private Graph G = new Graph(new ArrayList<Vertex>(), new ArrayList<Edge>());
    private Object fileLock = new Object();
    private static Map<String, String> ports;

    private TTransport transports;
    private TProtocol protocols;
    private Graph.Client clients;
    private static int ports[]; // array of ports for others servers
    private int selfPort; // number of the port of this server
    private static int N; // number of servers
    private int selfId;

    public void GraphHandler(String []args) {
        ports = TableServer.getMapServers(args[0], args[2]);

        N = Integer.parseInt(args[0]);
        selfId = Integer.parseInt(args[1]);
        int firstPort = Integer.parseInt(args[2]);
        selfPort = firstPort + selfId;
        
        transports = new TTransport;
        protocols = new TProtocol;
        clients = new Graph.Client;
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
                    synchronized (G) {
                        G = (Graph) aux;
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
                synchronized (G) {
                    stream.writeObject(G);
                }
                stream.close();
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        }
    }


    @Override
    public boolean createVertex(int nome, int cor, String descricao, double peso) {
        synchronized (G.getV()) { //Lock na lista para evitar duplicidade de nome
            if (G.getV() != null) {
                for (Vertex v : G.getV()) {
                    if (v.getNome() == nome) {
                        return false;
                    }
                }
            }
            G.getV().add(new Vertex(nome, cor, descricao, peso));
        }
        return true;
    }

    @Override
    public boolean createEdge(int v1, int v2, double peso, int flag, String descricao) {
        int criaControl = 0;
        synchronized (G.getV()) { //Lock nos vertex caso haja delecao em um dos vertex da edge
            for (Vertex v : G.getV()) {
                if (v.getNome() == v1 || v.getNome() == v2) {
                    criaControl++;
                }
            }
            if (criaControl > 1) {
                Edge aux2 = new Edge(v1, v2, peso, flag, descricao);
                synchronized (G.getA()) { //Lock nas edges para evitar duplicidade
                    if (!ifEquals(aux2)) {
                        G.getA().add(aux2);
                        if (flag == 2) {
                            Edge aux = new Edge(v2, v1, peso, flag, descricao);
                            if (!ifEquals(aux)) { //Se nao existir, cria
                                G.getA().add(aux);
                            }else{//Se existir, atualiza
                                updateEdge(aux.getV1(),aux.getV2(), aux);
                            }
                        }
                        return true;
                    }
                }

            }
        }
        return false;
    }

    @Override
    public boolean deleteVertex(int nome) {
        ArrayList<Edge> forDeletion = new ArrayList<>();
        for (Vertex v : G.getV()) {
            synchronized (v) {
                if (v.getNome() == nome) {
                    synchronized (G.getA()) {
                        for (Edge a : G.getA()) {
                            if (a.getV1() == nome || a.getV2() == nome) {
                                forDeletion.add(a);
                            }
                        }
                        for (Edge a : forDeletion) {
                            G.getA().remove(a);
                        }
                    }
                    G.getV().remove(v);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean deleteEdge(int v1, int v2) {
        for (Edge a : G.getA()) {
            synchronized (a) {
                if (a.getV1() == v1 && a.getV2() == v2) {
                    G.getA().remove(a);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean updateVertex(int nomeUp, Vertex V) {
        if (V == null) {
            return false;
        }
        if (nomeUp != V.getNome()) {
            return false;
        }
        for (Vertex v : G.getV()) {
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
        synchronized (G.getA()) {
            for (Edge a : G.getA()) {
                if (a.getV1() == A.getV1() && a.getV2() == A.getV2()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean updateEdge(int nomeV1, int nomeV2, Edge A) {
        if (A == null) {
            return false;
        }
        if (nomeV1 != A.getV1() || nomeV2 != A.getV2()) {
            return false;
        }
        for (Edge a : G.getA()) {
            synchronized (a) {
                if (a.getV1() == nomeV1 && a.getV2() == nomeV2) {
                    if(a.getFlag() == 2){// Se aresta antiga for bi-direcional, pega aresta v2,v1
                        Edge b = getEdge(a.getV2(), a.getV1());
                        synchronized (b){
                            if(A.getFlag() == 1){// Se aresta nova for direcionada, remove aresta v2,v1
                                G.getA().remove(b);
                            }else{ // Senao, update aresta v2,v1
                                b.setPeso(A.getPeso());
                                b.setFlag(A.getFlag());
                                b.setDescricao(A.getDescricao());
                            }
                        }
                    }else{// Se aresta antiga for direcionada
                        if(A.getFlag() == 2){// E aresta nova for bi-direcional, cria aresta v2,v1
                            Edge aux = new Edge(A.getV2(), A.getV1(), A.getPeso(), A.getFlag(), A.getDescricao());
                            if (!ifEquals(aux)) {
                                G.getA().add(aux);
                            }
                        }
                    }// Em todos os casos, update aresta v1,v2
                    a.setPeso(A.getPeso());
                    a.setFlag(A.getFlag());
                    a.setDescricao(A.getDescricao());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean updateGraph(java.util.List<Vertex> V, java.util.List<Edge> A) {
        synchronized (G) {
            G.setV(V);
            G.setA(A);
            return true;
        }
    }

    @Override
    public Vertex getVertex(int nome) {
        synchronized (G.getV()) {
            if (!G.getV().isEmpty()) {
                for (Vertex v : G.getV()) {
                    if (v.getNome() == nome) {
                        return v;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Edge getEdge(int v1, int v2) {
        synchronized (G.getA()) {
            if (!G.getA().isEmpty()) {
                for (Edge a : G.getA()) {
                    if (a.getV1() == v1 && a.getV2() == v2) {
                        return a;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Graph showGraph() {
        synchronized (G) {
            return G;
        }
    }

    @Override
    public List<Vertex> showVertex() {
        ArrayList<Vertex> vertices = new ArrayList<>();
        synchronized (G.getV()) {
            for (Vertex v : G.getV()) {
                vertices.add(v);
            }
        }
        return vertices;
    }

    @Override
    public List<Edge> showEdge() {
        ArrayList<Edge> arestas = new ArrayList<>();
        synchronized (G.getA()) {
            for (Edge a : G.getA()) {
                arestas.add(a);
            }
        }
        return arestas;
    }

    @Override
    public List<Vertex> showVertexOfEdges(int v1, int v2) {
        ArrayList<Vertex> vertices = new ArrayList<>();
        vertices.add(getVertex(v1));
        vertices.add(getVertex(v2));
        return vertices;
    }

    @Override
    public List<Edge> showEdgesOfVertex(int nomeV) {
        ArrayList<Edge> arestas = new ArrayList<>();
        synchronized (G.getA()) {
            for (Edge a : G.getA()) {
                if (a.getV1() == nomeV || a.getV2() == nomeV) {
                    arestas.add(a);
                }
            }
        }
        return arestas;
    }

    @Override
    public List<Vertex> showAdjacency(int nomeV) {
        ArrayList<Vertex> adjacentes = new ArrayList<>();
        synchronized (G.getA()) {
            for (Edge a : G.getA()) {
                if (a.getV1() == nomeV) {
                    if(!adjacentes.contains(getVertex(a.getV2())))
                    adjacentes.add(getVertex(a.getV2()));

                } else if (a.getV2() == nomeV) {
                    if(!adjacentes.contains(getVertex(a.getV1())))
                    adjacentes.add(getVertex(a.getV1()));
                }
            }
        }
        return adjacentes;
    }

    @Override
    public List<Edge> smallerPath(int nomeV1, int nomeV2) {
        return null;
    }
    //TODO Adicinar algoritmo de menor caminho
}
