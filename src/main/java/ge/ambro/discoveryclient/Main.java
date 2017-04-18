/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ge.ambro.discoveryclient;

import ge.ambro.discoveryclient.dto.EventDTO;
import ge.ambro.discoveryclient.dto.ServiceDTO;
import java.io.IOException;
import java.util.Arrays;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author tabramishvili
 */
class Main {

    public static void main(String[] args) throws IOException {
        JwtHelper tf = new JwtHelper("ge.ambro.belote", "secret");
        tf.builder().withClaim("name", "temp")
                .withArrayClaim("roles", new String[]{"microservice", "role2"});
        DiscoveryClient client = new DiscoveryClient(tf, "http://localhost:8080/DiscoveryService");
//        String res = client.target("temp:generic", null);
//        System.out.println(res);
//        res = client.target("temp:bla", "my data");

//        client.target("temp2", (target, wrap) -> {
//            String res = wrap.apply(target.path("api/generic").request()).get(String.class);
//            System.out.println("res: " + res);
//            return false;
//        });
        client.event("my_event", (target, wrapper) -> {
            target.request().post(Entity.entity("bla", MediaType.APPLICATION_JSON));
            return false;
        });
//        ServiceDTO service = new ServiceDTO();
//        service.setId(4);
//        service.setName("temp4");
//        service.setBase("http://localhost:8080/TestMa");
//        service.setServiceDescrip("dynamically added");
//
//        EventDTO ev = new EventDTO();
//        ev.setName("my_event");
//        ev.setPath("api/generic/bla");
////        EventDTO ev2 = new EventDTO();
////        ev2.setName("my_event");
////        ev2.setPath("api/generic/bla");
////        service.setEvents(Arrays.asList(ev, ev2));
//        service.setEvents(Arrays.asList(ev));
//
//        client.register(service);
//        
//        service = new ServiceDTO();
//        service.setId(2);
//        service.setName("temp2");
//        service.setBase("http://localhost:8080/TestMaven");
//        service.setServiceDescrip("dynamically added");
//        client.register(service);
//        TargetDTO target = new TargetDTO();
//        target.setName("ble");
//        target.setPath("/TestMaven/api/generic/ble");
//        target.setMethod("POST");
//        DependencyDTO dep = new DependencyDTO();
//        dep.setAddress("temp:sec");
//        dep.setPriority(0);
//        target.setDependencies(Arrays.asList(dep));
//        service.setTargets(Arrays.asList(target));
//        client.register(service);
//        res = client.target("temp1:ble", null);
//        System.out.println(res);
    }

}
