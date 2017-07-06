package com.sentinella.james;

import java.io.PrintWriter;

/**
 * Created by James on 4/8/2016.
 */
public interface ClientCallback {
    void finished();
    void unableToConnect();
    void setOutConnection(PrintWriter out);
}
