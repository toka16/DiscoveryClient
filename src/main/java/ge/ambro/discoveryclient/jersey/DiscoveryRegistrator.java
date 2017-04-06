/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ge.ambro.discoveryclient.jersey;

import ge.ambro.discoveryclient.DiscoveryClient;
import ge.ambro.discoveryclient.dto.EventDTO;
import ge.ambro.discoveryclient.dto.ServiceDTO;
import ge.ambro.discoveryclient.dto.TargetDTO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tabramishvili
 */
class DiscoveryRegistrator {

    ServiceDTO service = new ServiceDTO();
    final DiscoveryClient client;

    private int registerAttempts = 0;

    public DiscoveryRegistrator(DiscoveryClient c) {
        client = c;
        service.setTargets(new ArrayList<>());
        service.setEvents(new ArrayList<>());
    }

    void addTarget(TargetDTO target) {
        service.getTargets().add(target);
    }

    void addEvent(EventDTO event) {
        service.getEvents().add(event);
    }

    void register() {
        try {
            service.setId(client.register(service));
        } catch (IOException ex) {
            Logger.getLogger(DiscoveryRegistrator.class.getName()).log(Level.SEVERE, null, ex);
            if (registerAttempts++ < 3) {
                Thread t = new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        register();
                    } catch (InterruptedException ex1) {
                        Logger.getLogger(DiscoveryRegistrator.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                });
                t.setDaemon(true);
                t.start();
            }
        }
    }
}