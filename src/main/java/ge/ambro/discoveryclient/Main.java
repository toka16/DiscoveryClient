/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ge.ambro.discoveryclient;

import ge.ambro.discoveryclient.dto.DependencyDTO;
import ge.ambro.discoveryclient.dto.ServiceDTO;
import ge.ambro.discoveryclient.dto.TargetDTO;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author tabramishvili
 */
class Main {

    public static void main(String[] args) throws IOException {
        DiscoveryClient client = new DiscoveryClient(new ConnectionFactory(), "http://localhost:8080/DiscoveryService");
        String res = client.target("temp:generic", null);
        System.out.println(res);
        res = client.target("temp:bla", "my data");

        ServiceDTO service = new ServiceDTO();
        service.setBase("http://localhost:8080/");
        service.setName("temp1");
        service.setServiceDescrip("dynamically added");
        TargetDTO target = new TargetDTO();
        target.setName("ble");
        target.setPath("/TestMaven/api/generic/ble");
        target.setMethod("POST");
        DependencyDTO dep = new DependencyDTO();
        dep.setAddress("temp:sec");
        dep.setPriority(0);
        target.setDependencies(Arrays.asList(dep));
        service.setTargets(Arrays.asList(target));
        client.register(service);
//        res = client.target("temp1:ble", null);
//        System.out.println(res);
    }

}
