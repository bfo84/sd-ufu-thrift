package br.ufu.miguelpereira.view;

import br.ufu.miguelpereira.control.GraphHandler;
import br.ufu.miguelpereira.thrift.*;

import org.apache.thrift.server.*;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

public class Server {

    public static void main(String [] args){

        try{
            GraphHandler handler = new GraphHandler();
            Operations.Processor processor = new Operations.Processor(handler);
            TServerTransport serverTransport = new TServerSocket(Integer.parseInt(args[2]));
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
            System.out.println("Servidor Inicializado...");
            server.serve();
        } catch (Exception x){
            x.printStackTrace();
        }
    }
}
