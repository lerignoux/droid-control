package lerignoux.droid_control;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by laurent on 6/9/17.
 */

public class Server {
    public String name;
    public String address;
    public Integer port;
    public String username;

    public Server(String name, String address, Integer port, String username) {
        this.name = name;
        this.address = address;
        if (port != null) {
            this.port = port;
        } else {
            port = 22;
        }
        this.username = username;
    }
}