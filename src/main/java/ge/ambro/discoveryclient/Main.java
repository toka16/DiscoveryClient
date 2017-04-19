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
//        client.event("my_event", (target, wrapper) -> {
//            target.request().post(Entity.entity("bla", MediaType.APPLICATION_JSON));
//            return false;
//        });
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
//        client.target("signin", (target, wrapper) -> {
//            try {
//                target = target.path("api/v1/signin/check").queryParam("email", "asd@as.a");
//                System.out.println("uri: " + target.getUri());
//                boolean b = wrapper.apply(target
//                        .request(MediaType.APPLICATION_JSON)).get(Boolean.class);
//                System.out.println("check: " + b);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return true;
//        });
        client.target("signin", (target, wrapper) -> {
            try {
                CredentialsDTO cred = new CredentialsDTO();
                cred.setEmail("asd@as.a");
                cred.setPassword("pass");
                String res = wrapper.apply(target.path("api/v1/signin/").request())
                        .post(Entity.entity(cred, MediaType.APPLICATION_JSON), String.class);
                System.out.println("res: " + res);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        });
//        String res = client.target("signin", (target, wrapper) -> {
//            try {
//                return wrapper.apply(target.path("api/v1/signin/8e088d82-396a-4da7-97ae-ac4082b67214").request())
//                        .get(String.class);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        });
//
//        System.out.println("res: " + res);
    }

}
