/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ge.ambro.discoveryclient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author tabramishvili
 */
public class ConnectionFactory {

    private final JwtHelper tokenFactory;

    public ConnectionFactory(JwtHelper tokenFactory) {
        this.tokenFactory = tokenFactory;
    }

    public HttpURLConnection createConnection(String address) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(address).openConnection();
        con.setRequestProperty("Authorization", "Discovery " + tokenFactory.getToken());
        con.setConnectTimeout(5000);
        con.setReadTimeout(3000);
        return con;
    }
}
