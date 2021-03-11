package io.yuna.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * @author Lin Yang <geekya215@gmail.com>
 */
public class Reactor implements Runnable {
    private final int port;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ExecutorService workerPool;
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public Reactor() {
        this(9999);
    }

    public Reactor(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            workerPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            logger.info("server start up at port " + port);
        } catch (IOException e) {
            logger.warning(e.getMessage() + " when opening server socket channel");
            e.printStackTrace();
            System.exit(1);
        }

        while (!Thread.interrupted()) {
            try {
                if (selector.selectNow() < 0) {
                    continue;
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> keys = selectionKeys.iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (key.isAcceptable()) {
                        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                        SocketChannel channel = serverChannel.accept();
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ);
                    } else if (key.isValid() && key.isReadable()) {
                        workerPool.submit(new Worker(key));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
