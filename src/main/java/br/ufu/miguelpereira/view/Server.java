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

    public static Properties getProp() throws IOException {
        Properties props = new Properties();
        FileInputStream file = new FileInputStream(
                "./src/main/resources/host.properties");
        props.load(file);
        return props;
    }

    public static void main(String [] args){
        int port;
        Properties prop;
        try{
            prop = getProp();
            if (!prop.isEmpty()) {
            	logger.debug("Inicializando o servidor");
                port = Integer.parseInt(prop.getProperty("prop.server.port"));
            } else {
                System.out.println("ERRO! Falha ao carregar arquivo de configuracao!");
                return;
            }

            GraphHandler handler = new GraphHandler();
            Operations.Processor processor = new Operations.Processor(handler);
            TServerTransport serverTransport = new TServerSocket(port);
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
            //Criar concorrencia com apenas um thread
            //TNonblockingServerSocket serverTransport = new TNonblockingServerSocket(9080);
            //THsHaServer.Args argus = new THsHaServer.Args(serverTransport);
            //argus.processor(processor);
            //argus.transportFactory(new TFramedTransport.Factory());
            //TServer server = new THsHaServer(argus);
            System.out.println("Servidor Inicializado...");
            server.serve();
        } catch (Exception x){
            x.printStackTrace();
            logger.error(x.getMessage(), x);
        }
    }
}
