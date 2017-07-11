package com.sentinella.james;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class ServerSocket {
    ServerSocketChannel      socket;
    Selector selector;
    int ops;
    int port;

    public ServerSocket(int port) {
        this.port = port;
    }

    public ServerSocket() {
        this.port = 36788;
    }

    public void establishConnection(int port) {
        this.port = port;
    }

    public void establishConnection() throws IOException {
        socket = ServerSocketChannel.open();
        selector = Selector.open();
        ServerSocketChannel.open();
        socket.bind(new InetSocketAddress(this.port));
        socket.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        socket.configureBlocking(false);
        ops = socket.validOps();
        socket.register(selector,ops);
    }

    public Set<SelectionKey> select(int timeout) throws IOException {
        selector.select(timeout);
        return selector.selectedKeys();
    }

    public SocketChannel accept() throws IOException {
        SocketChannel client = socket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        return client;
    }

    public String read(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer    buffer = ByteBuffer.allocate(256);
        StringBuilder strbuf = new StringBuilder();
        int           bSize  = client.read(buffer);
        do {
            if (bSize == -1) {
                throw new IOException();
            } else {
                strbuf.append(new String(buffer.array()));
            }
            bSize = client.read(buffer);
        } while (bSize > 0);
        return strbuf.toString().trim();
    }

    public void close() throws IOException {
        selector.close();
        socket.close();
    }

}