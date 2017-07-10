package com.sentinella.james;

import java.io.IOException;

/**
 * Created by James on 7/9/2017.
 */
public interface ClientConnection {
    public void println(String msg) throws IOException;
}
