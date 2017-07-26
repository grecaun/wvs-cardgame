package com.sentinella.james;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class ClientSocket implements ClientConnection {
    private SocketChannel socket;
    private Selector      selector;
    private InetAddress   hostname;
    private int           port;

    protected LogBook log = new LogBook();

    public ClientSocket(String hostname, int port) throws UnknownHostException {
        this.hostname = InetAddress.getByName(hostname);
        this.port     = port;
    }

    public ClientSocket() throws UnknownHostException {
        this.hostname = InetAddress.getLocalHost();
        this.port     = 36788;
    }

    public ClientSocket(String hostname, int port, LogBook l, String debugStr) throws UnknownHostException {
        this(hostname,port);
        this.log = LogBookFactory.getLogBook(l,debugStr);
    }

    public void sendMessage(String message) throws IOException {
        ByteBuffer outbuff = ByteBuffer.allocate(2056);
        outbuff.put(message.getBytes());
        outbuff.flip();
        while (outbuff.hasRemaining()) {
            socket.write(outbuff);
        }
    }

    public String readLine() throws IOException {
        ByteBuffer    buffer = ByteBuffer.allocate(256);
        StringBuilder strbuf = new StringBuilder();
        int           bSize  = socket.read(buffer);
        do {
            if (bSize == -1) {
                throw new IOException();
            } else {
                strbuf.append(new String(buffer.array()));
            }
            bSize = socket.read(buffer);
        } while (bSize > 0);
        return strbuf.toString();
    }

    public boolean ready() throws IOException {
        Iterator<SelectionKey> iterator = select(0).iterator();
        SelectionKey  key = iterator.next();
        if (key.isReadable()) return true;
        return false;
    }

    public Set<SelectionKey> select(int timeout) throws IOException {
        selector.select(timeout);
        return selector.selectedKeys();
    }

    public void establishConnection() throws IOException {
        socket = SocketChannel.open();
        socket.connect(new InetSocketAddress(this.hostname,this.port));
        socket.configureBlocking(false);
        selector = Selector.open();
        socket.register(selector, SelectionKey.OP_READ);
    }

    public void establishConnection(String hostname, int port) throws IOException {
        this.hostname = InetAddress.getByName(hostname);
        this.port = port;
        establishConnection();
    }

    public void close() throws IOException {
        socket.close();
    }

    @Override
    public void println(String msg) throws IOException {
        sendMessage(msg);
    }
}
