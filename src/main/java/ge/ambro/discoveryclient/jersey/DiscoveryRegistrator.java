/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ge.ambro.discoveryclient.jersey;

import ge.ambro.discoveryclient.DiscoveryClient;
import ge.ambro.discoveryclient.DiscoveryClientLogger;
import ge.ambro.discoveryclient.dto.EventDTO;
import ge.ambro.discoveryclient.dto.ServiceDTO;
import ge.ambro.discoveryclient.exeptions.DiscoveryException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 *
 * @author tabramishvili
 */
public class DiscoveryRegistrator {

    ServiceDTO service = new ServiceDTO();
    final DiscoveryClient client;

    private int registerAttempts = 0;

    public DiscoveryRegistrator(DiscoveryClient c) {
        client = c;
        service.setEvents(new ArrayList<>());
    }

    public void addEvent(EventDTO event) {
        service.getEvents().add(event);
    }

    public void register() {
        try {
            client.register(service);
        } catch (IOException | DiscoveryException ex) {
            DiscoveryClientLogger.LOGGER.log(Level.SEVERE, null, ex);
            if (registerAttempts++ < 3) {
                Thread t = new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        register();
                    } catch (InterruptedException ex1) {
                        DiscoveryClientLogger.LOGGER.log(Level.SEVERE, null, ex1);
                    }
                });
                t.setDaemon(true);
                t.start();
            }
        }
    }

    public void unregister() {
        client.unregisterTarget(service.getId());
    }
}
