package com.sentinella.james;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public interface ClientCallback {
    void finished();
    void unableToConnect();
    void setOutConnection(ClientSocket out);
}
