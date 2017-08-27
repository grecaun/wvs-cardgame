/**
 * Copyright (c) 2017 James Sentinella.
 * <p>
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.sentinella.james.wvs_android;

import com.sentinella.james.Client;

public interface MainAppCallback {
    public void setClientInfo(Client client, Thread clientThread);
}
