package io.yuna.core;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * @author Lin Yang <geekya215@gmail.com>
 */
public class Worker implements Callable {
    private SelectionKey selectionKey;
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public Worker(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    @Override
    public Object call() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        int count = channel.read(buffer);
        if (count < 0) {
            selectionKey.cancel();
            channel.close();
            logger.warning("Received invalid data, close the connection");
            return null;
        } else if (count == 0) {
            return null;
        }
        System.out.println(Thread.currentThread().toString() + " Received message ===> " + new String(buffer.array()));
        return null;
    }
}
