package br.ufu.miguelpereira.view;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import br.ufu.miguelpereira.control.GraphHandler;
import br.ufu.miguelpereira.thrift.Operations;
public class Server {
	
	private final static Logger logger = Logger.getLogger(Server.class);

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
            logger.error(x.getMessage(), x);
        }
    }
}
    /*
    mvn exec:java -Dexec.mainClass=br.ufu.miguelpereira.view.Client -Dexec.args="9094"
    mvn exec:java -Dexec.mainClass=br.ufu.miguelpereira.view.Server -Dexec.args="1 0 9094"
    */
