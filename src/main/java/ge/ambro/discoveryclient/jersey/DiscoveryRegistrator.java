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
import java.util.ArrayList;

/**
 *
 * @author tabramishvili
 */
class DiscoveryRegistrator {

    ServiceDTO service = new ServiceDTO();
    final DiscoveryClient client;

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
        System.out.println("register: " + service);
    }
}
