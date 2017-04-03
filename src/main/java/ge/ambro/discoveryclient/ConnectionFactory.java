/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ge.ambro.discoveryclient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author tabramishvili
 */
public class ConnectionFactory {

    public HttpURLConnection createConnection(String address) throws IOException {
        return (HttpURLConnection) new URL(address).openConnection();
    }
}
