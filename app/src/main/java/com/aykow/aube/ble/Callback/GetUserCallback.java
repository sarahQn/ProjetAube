package com.aykow.aube.ble.Callback;

import com.aykow.aube.ble.User;

/**
 * Created by sarah on 09/03/2016.
 */
public interface GetUserCallback {
    public abstract void done(User returnedUser);

    public abstract void doneString(String result);
}
