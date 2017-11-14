package br.ufu.miguelpereira.view;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import br.ufu.miguelpereira.thrift.Edge;
import br.ufu.miguelpereira.thrift.Operations;
import br.ufu.miguelpereira.thrift.Vertex;

public class Client {
	
	private final static Logger logger = Logger.getLogger(Client.class);

    public static void main(String[] args) {
        String hostname = "localhost";
        int port = Integer.parseInt(args[0]);
        try {

            TTransport transport = new TSocket(hostname, port);
            transport.open();

            TProtocol protocol = new TBinaryProtocol(transport);
            Operations.Client client = new Operations.Client(protocol);

            int menu = 0;
            int v1, v2, cor;
            String descricao, file;
            double peso;
            Scanner scan = new Scanner(System.in);

            logger.debug("Inicializando o menu");
            //Menu
            while (menu != 16) {
                System.out.println("1 - Inserir Vertice");
                System.out.println("2 - Inserir Aresta");
                System.out.println("3 - Carregar Grafo");
                System.out.println("4 - Salvar Grafo");
                System.out.println("5 - Exibir Grafo");
                System.out.println("6 - Remover Vertice");
                System.out.println("7 - Remover Aresta");
                System.out.println("8 - Alterar Vertice");
                System.out.println("9 - Alterar Aresta");
                //   System.out.println("10 - Listar Vertices de uma aresta");
                System.out.println("10 - Listar Arestas de um vertice");
                System.out.println("11 - Listar Vértices adjacentes a outros");
                System.out.println("12 - Sair");
                menu = scan.nextInt();
                switch (menu) {
                    case 1://Inserir vertice

                        System.out.println("<- Digite os devidos atributos do vertice ->");
                        System.out.println("ID do vertice <int>: ");
                        v1 = scan.nextInt();
                        System.out.println("Cor <int>: ");
                        cor = scan.nextInt();
                        System.out.println("Descricao <string>: ");
                        scan.nextLine();
                        descricao = scan.nextLine();
                        System.out.println("Peso <double>: ");
                        peso = scan.nextDouble();
                        if (client.createVertex(v1, cor, descricao, peso)) {
                            ArrayList<Vertex> listOfVertex = (ArrayList<Vertex>) client.showVertex();
                            System.out.println("<- Vertice inserido! ->");
                            for (Vertex v: listOfVertex) {
                                System.out.println(v.nome);
                            }
                        } else {
                            System.out.println("<- Erro! ->");
                        }

                        break;

                    case 2://Inserir aresta

                        System.out.println("<- Digite os devidos atributos da aresta ->");
                        System.out.println("Digite o vertice 'X' da aresta: ");
                        v1 = scan.nextInt();
                        System.out.println("Digite o vertice 'Y' da aresta: ");
                        v2 = scan.nextInt();
                        System.out.println("Digite '1' <- direcionado -> '2' <- Bi-Direcionado ->: ");
                        cor = scan.nextInt();
                        System.out.println("Descrição: ");
                        scan.nextLine();
                        descricao = scan.nextLine();
                        System.out.println("Digite o peso: ");
                        peso = scan.nextDouble();
                        if (client.createEdge(v1, v2, peso, cor, descricao)) {
                            ArrayList<Edge> edgesofGraph = (ArrayList<Edge>) client.showEdge();
                            System.out.println("<- Aresta Inserida! ->");
                            for (Edge e: edgesofGraph) {
                                System.out.println("Aresta (" + e.v1 + "," + e.v2 + ")");
                            }
                        } else {
                            System.out.println("<- Erro! ->");
                        }

                        break;

                    case 3://Carregar grafo

                        System.out.println("<- Informe o arquivo a ser carregado -> ");
                        System.out.println("Arquivo:");
                        scan.nextLine();
                        file = scan.nextLine();
                        client.loadGraph(file);

                        break;

                    case 4://Salvar grafo

                        System.out.println("<- Informe o arquivo para salvar ->");
                        System.out.println("Arquivo: ");
                        scan.nextLine();
                        file = scan.nextLine();
                        client.saveGraph(file);

                        break;

                    case 5://Mostrar grafo
                        System.out.println(client.showGraph());
                        break;

                    case 6://Remover vertice

                        System.out.println("<- Digite o vertice que deseja remover ->");
                        System.out.println("Vertice: ");
                        v1 = scan.nextInt();
                        if (client.deleteVertex(v1)) {
                            System.out.println("<- Vértice Removido! ->");
                            System.out.println(client.showGraph());
                        } else {
                            System.out.println("<- Erro! ->");
                        }
                        break;

                    case 7://Remover aresta

                        System.out.println("<- Entre com o vertice 'X' da aresta que será removida ->");
                        v1 = scan.nextInt();
                        System.out.println("<- Entre com o vertice 'Y' ->");
                        v2 = scan.nextInt();
                        if (client.deleteEdge(v1, v2)) {
                            System.out.println("<- Aresta removida! ->");
                            System.out.println(client.showEdge());
                        } else {
                            System.out.println("<- Erro! ->");
                        }
                        break;

                    case 8://Alterar vertice

                        System.out.println("<- Digite os atributos do vertice a ser alterado ->");
                        System.out.println("Número do vértice a ser alterado: ");
                        v2 = v1 = scan.nextInt();
                        System.out.println("Nova cor: ");
                        cor = scan.nextInt();
                        System.out.println("Descrição do vértice novo: ");
                        scan.nextLine();
                        descricao = scan.nextLine();
                        System.out.println("Digite o peso: ");
                        peso = scan.nextDouble();
                        if (client.updateVertex(v1, new Vertex(v2, cor, descricao, peso))) {
                            System.out.println("<- Vertice Alterado! ->");
                            System.out.println(client.showVertex());
                        } else {
                            System.out.println("<- Erro! ->");
                        }
                        break;

                    case 9://Alterar aresta

                        System.out.println("<- Digite os atributos da aresta a ser alterada ->");
                        System.out.println("Vertice X: ");
                        v1 = scan.nextInt();
                        System.out.println("Vertice Y: ");
                        v2 = scan.nextInt();
                        System.out.println("Nova descrição: ");
                        scan.nextLine();
                        descricao = scan.nextLine();
                        System.out.println("Digite o peso: ");
                        peso = scan.nextDouble();
                        System.out.println("Digite o novo flag: ");
                        cor = scan.nextInt(); //cor sendo utilizado pra peso
                        if (client.updateEdge(v1, v2, new Edge(v1, v2, peso, cor, descricao))) {//cor é peso
                            System.out.println("<- Aresta Alterada! ->");
                            System.out.println(client.showEdge());
                        } else {
                            System.out.println("<- Erro! ->");
                        }
                        break;
            
                    /*case 10://Listar vertices de uma aresta
                        
                        System.out.println("<- Digite uma aresta para listar seus vértices ->");
                        System.out.println("Vertice X: ");
                        v1 = scan.nextInt();
                        System.out.println("Vertice Y: ");
                        v2 = scan.nextInt();
                        System.out.println(client.showVertexOfEdges(v1,v2));
                        break;*/

                    case 10://Listar arestas de um vertice

                        System.out.println("<- Digite um vertice para listar suas arestas -> ");
                        System.out.println("Vertice X: ");
                        v1 = scan.nextInt();
                        System.out.println(client.showEdgesOfVertex(v1));
                        break;

                    case 11://Listar vertice adjacentes

                        System.out.println("<- Digite o vertice para exibir suas adjacências -> ");
                        v1 = scan.nextInt();
                        System.out.println(client.showAdjacency(v1));
                        break;

                    case 12:

                        System.out.println("<- Disconnecting... ->");
                        return;

                    default:
                        System.out.println("<- Opção Inválida! ->");
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            transport.close();
        } catch (Exception x) {
            x.printStackTrace();
            logger.error(x.getMessage(), x);
        }
    }
}
