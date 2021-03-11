package io.yuna.test;

import io.yuna.core.Reactor;

public class Server {
    public static void main(String[] args) {
        new Thread(new Reactor(9999)).start();
    }
}
