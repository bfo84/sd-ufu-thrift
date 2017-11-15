package br.ufu.miguelpereira.control;

import br.ufu.miguelpereira.thrift.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Dijkstra{

    private List<Vertex> vertices;
    private List<Edge> arestas;
    private Set<Vertex> verticesMarcados;
    private Set<Vertex> verticesNaoMarcados;
    private HashMap<Vertex, Vertex> antecessores;
    private HashMap<Vertex, Double> distancia;

    public Dijkstra(Graph grafo) {

        this.vertices = new ArrayList<Vertex>(grafo.getV());
        this.arestas = new ArrayList<Edge>(grafo.getA());
    }

    public void executa(Vertex inicial) {

        verticesMarcados = new HashSet<Vertex>();
        verticesNaoMarcados = new HashSet<Vertex>();
        distancia = new HashMap<Vertex, Double>();
        antecessores = new HashMap<Vertex, Vertex>();

        distancia.put(inicial, 0.0);

        verticesNaoMarcados.add(inicial);

        while (verticesNaoMarcados.size() > 0) {

            Vertex nodo = getMinimo(verticesNaoMarcados);
            verticesMarcados.add(nodo);
            verticesNaoMarcados.remove(nodo);
            buscaDistanciasMinimas(nodo);

        }
    }

    private void buscaDistanciasMinimas(Vertex nodo) {

        List<Vertex> nodosAdjacentes = getVizinhos(nodo);

        for (Vertex alvo : nodosAdjacentes) {

            if (getMenorDistancia(alvo) > getMenorDistancia(nodo)
                    + getDistancia(nodo, alvo)) {
                distancia.put(alvo, getMenorDistancia(nodo)
                        + getDistancia(nodo, alvo));
                antecessores.put(alvo, nodo);
                verticesNaoMarcados.add(alvo);
            }
        }

    }

    private double getDistancia(Vertex nodo, Vertex alvo) {

        for (Edge aresta: arestas) {

            //direcionada
            if (aresta.getFlag() == 1 && aresta.getV1() == nodo.getNome()
                    && aresta.getV2() == alvo.getNome()) {
                return aresta.getPeso();
            }
            else { //não direcionada

                if((aresta.getV1() == nodo.getNome()
                        && aresta.getV2() == alvo.getNome()) ||
                        (aresta.getV2() == nodo.getNome()
                                && aresta.getV1() == alvo.getNome()))
                    return aresta.getPeso();

            }
        }
        throw new RuntimeException("Nao deveria acontecer");
    }

    private List<Vertex> getVizinhos(Vertex nodo) {

        List<Vertex> vizinhos = new ArrayList<Vertex>();
        //aqui tbmmm
        for (Edge aresta: arestas) {

            if (aresta.getFlag() == 1 && aresta.getV1() == nodo.getNome()
                    && !verificaMarcado(findVertice(aresta.getV2()))) {
                vizinhos.add(findVertice(aresta.getV2()));
            }
            else {

                if(aresta.getV1() == nodo.getNome()
                        && !verificaMarcado(findVertice(aresta.getV2()))) {

                    vizinhos.add(findVertice(aresta.getV2()));
                }
                if(aresta.getV2() == nodo.getNome()
                        && !verificaMarcado(findVertice(aresta.getV1()))) {

                    vizinhos.add(findVertice(aresta.getV1()));
                }
            }
        }
        return vizinhos;
    }

    private Vertex getMinimo(Set<Vertex> vertices) {

        Vertex minimo = null;

        for (Vertex v : vertices) {

            if (minimo == null) {
                minimo = v;
            } else {
                if (getMenorDistancia(v) < getMenorDistancia(minimo)) {
                    minimo = v;
                }
            }
        }

        return minimo;
    }

    private boolean verificaMarcado(Vertex vertice) {
        return verticesMarcados.contains(vertice);
    }

    private double getMenorDistancia(Vertex destino) {

        Double d = distancia.get(destino);

        if (d == null) {
            return Double.MAX_VALUE;
        }
        else {
            return d;
        }

    }

    /*
     * Esse método retorna o caminho do vertice inicial até o destino e
     * NULL caso não exista caminho
     */
    public LinkedList<Vertex> getCaminho(Vertex alvo) {

        LinkedList<Vertex> caminho = new LinkedList<Vertex>();
        Vertex atual = alvo;

        // verifica se um caminho existe
        if (antecessores.get(atual) == null) {
            return null;
        }

        caminho.add(atual);

        while (antecessores.get(atual) != null) {
            atual = antecessores.get(atual);
            caminho.add(atual);
        }

        //Coloca na ordem correta
        Collections.reverse(caminho);
        return caminho;
    }

    private Vertex findVertice(int vertice) {

        for(Vertex v: this.vertices){
            if(v.nome == vertice){
                return v;
            }
        }
        return null;

    }

}
