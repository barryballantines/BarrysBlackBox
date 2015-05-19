/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.mbuse.flightgear.pireprecorder.udp;

import de.mbuse.pipes.Pipe;
import de.mbuse.pipes.PipeUpdateListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import org.json.JSONObject;

/**
 *
 * @author mbuse
 */
public class UDPServer implements PipeUpdateListener<Object>{
    
    public static final int PORT = 5555;
    
    public final Pipe<Boolean> runningPipe = Pipe.newInstance("udpServer.running", this);
    public final Pipe<Integer> portPipe = Pipe.newInstance("udpServer.port", this);
    public final Pipe<FlightData> flightDataPipe = Pipe.newInstance("udpServer.flightData", this);

    @Override
    public void pipeUpdated(Pipe<Object> pipe) {
        System.out.println("[UDP SERVER] Model updated : " + pipe.id() + " -> " + pipe.get());
        if ("udpServer.running".equals(pipe.id())) {
            boolean running = (boolean) pipe.get();
            if (running) {
                start();
            } else {
                stop();
            }
        }
    }
    
    public void start() {
        final int port = portPipe.get();
        final boolean running = runningPipe.get();
        
        if (!running) {
            System.out.println("Server not starting... " + runningPipe);
            return;
        }
        
        serverThread = new Thread(new Runnable() {

            @Override
            public void run() {
                System.out.println("Starting UDP Server on port " + port + ".");
                try {
                    DatagramChannel channel = createChannel(port);
                    try {
                        Selector s = createSelector(channel);
                        loop(s);
                    } finally {
                        selector.close();
                        channel.close();
                        if (!channel.isOpen()) {
                            System.out.println("Channel closed on port " + port + ".");
                        }
                    }
                } catch (IOException e) {
                    System.err.println("UDP Server failed..." + e);
                }
                System.out.println("Stopping UDP Server on port " + port);
            }
        });
        serverThread.start();
    }
    
    public void stop() {
        runningPipe.set(false);
        if (selector!=null) {
            selector.wakeup();
        }
    }
    
    
    

    private DatagramChannel createChannel(int port) throws IOException {
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.bind(new InetSocketAddress(port));
        return channel;
    }
    
    private Selector createSelector(DatagramChannel channel) throws IOException {
        selector = Selector.open();
        SelectionKey key = channel.register(selector, SelectionKey.OP_READ); 
        return selector;
    }
    
    private void loop(Selector selector) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate( 1024 );
        while(runningPipe.get()) {
            int n = selector.select();
            System.out.println(n + " messages received...");
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                DatagramChannel channel = (DatagramChannel) key.channel();
                buffer.clear();

                channel.receive(buffer);

                buffer.flip();
                CharBuffer chars = Charset.defaultCharset().decode(buffer);
                String message =chars.toString();
                JSONObject obj = new JSONObject(message);
                flightDataPipe.set(new FlightData(obj));
                //iterator.remove();
            }
        }
        System.out.println("UDP Server loop finished.");
    }
    
    private Thread serverThread;
    private Selector selector;
    private DatagramChannel channel;
    
}
