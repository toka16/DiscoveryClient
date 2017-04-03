/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ge.ambro.discoveryclient;

import java.io.IOException;

/**
 *
 * @author tabramishvili
 */
public class Main {
    
    public static void main(String[] args) throws IOException {
        DiscoveryClient client = new DiscoveryClient(new ConnectionFactory(), "http://bla.ble.blu/api", "http://localhost:8080/DiscoveryService");
        String res = client.target("temp1:test", "my data");
        System.out.println(res);
    }
    
}
