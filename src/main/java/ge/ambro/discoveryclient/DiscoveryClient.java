/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ge.ambro.discoveryclient;

import ge.ambro.discoveryclient.dto.EventResponseDTO;
import ge.ambro.discoveryclient.dto.ServiceDTO;
import ge.ambro.discoveryclient.exeptions.DiscoveyServerNotAvailable;
import ge.ambro.discoveryclient.exeptions.NoTargetFound;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 *
 * @author tabramishvili
 */
public class DiscoveryClient {

    private final String[] discoveryAddresses;

    private final Client client;
    private final JwtHelper factory;

    private long ts = 0;
    private final Random random;

    private final Map<String, List<ServiceDTO>> targetCache;
    private final Map<String, List<EventResponseDTO>> eventCache;

    public DiscoveryClient(JwtHelper factory, String... addresses) {
        client = ClientBuilder.newClient();
        discoveryAddresses = addresses;

        this.factory = factory;
        random = new Random();
        targetCache = new HashMap<>();
        eventCache = new HashMap<>();
    }

    protected WebTarget findAvailableDiscoveryService() throws IOException {
        for (String address : discoveryAddresses) {
            DiscoveryClientLogger.LOGGER.log(Level.INFO, "check address: {0}", Utils.concat(address, "/api/ping"));
            WebTarget target = client.target(address);
            if (wrap(target.path("/api/ping").request()).get().getStatus() == 200) {
                return target;
            }
        }
        throw new DiscoveyServerNotAvailable();
    }

    public int register(ServiceDTO service) throws IOException {
        String res = wrap(findAvailableDiscoveryService()
                .path("api/v1/services")
                .request(MediaType.APPLICATION_JSON))
                .post(Entity.entity(service, MediaType.APPLICATION_JSON), String.class);
        return Integer.parseInt(res);
    }

    public void event(String name, BiFunction<WebTarget, Function<Builder, Builder>, Boolean> consumer) throws IOException {
        long cachTs = eventCache.containsKey(name) ? ts : 0;
        Response response = wrap(findAvailableDiscoveryService()
                .path("api/v1/event-listeners")
                .queryParam("name", name)
                .queryParam("ts", cachTs)
                .request(MediaType.APPLICATION_JSON))
                .get();
        List<EventResponseDTO> res;
        if (response.getStatus() == 304) {
            res = eventCache.get(name);
        } else {
            res = response.readEntity(new GenericType<List<EventResponseDTO>>() {
            });
            eventCache.clear();
            eventCache.put(name, res);
            ts = System.currentTimeMillis();
        }
        response.close();

        MultivaluedMap<String, EventResponseDTO> listenerGroups = groupListeners(res);
        listenerGroups.forEach((key, listeners) -> {
            while (true) {
                int index = chooseTarget(listeners);
                EventResponseDTO target = listeners.get(index);
                boolean isAlive = consumer.apply(client.target(target.getBase())
                        .path(target.getPath()), this::wrap);
                if (isAlive) {
                    break;
                }
                unregisterTarget(target.getServiceId());
                listeners.remove(index);
            }

        });
    }

    public void target(String name, BiFunction<WebTarget, Function<Builder, Builder>, Boolean> consumer) throws IOException {
        long cachTs = targetCache.containsKey(name) ? ts : 0;
        Response response = wrap(findAvailableDiscoveryService()
                .path("api/v1/targets")
                .queryParam("name", name)
                .queryParam("ts", cachTs)
                .request(MediaType.APPLICATION_JSON))
                .get();
        List<ServiceDTO> targets;
        if (response.getStatus() == 304) {
            targets = targetCache.get(name);
        } else {
            targets = response.readEntity(new GenericType<List<ServiceDTO>>() {
            });
            targetCache.clear();
            targetCache.put(name, targets);
            ts = System.currentTimeMillis();
        }

        while (true) {
            int index = chooseTarget(targets);
            ServiceDTO target = targets.get(index);
            boolean isAlive = consumer.apply(client.target(target.getBase()), this::wrap);
            if (isAlive) {
                break;
            }
            unregisterTarget(target.getId());
            targets.remove(index);
        }
    }

    protected MultivaluedMap<String, EventResponseDTO> groupListeners(List<EventResponseDTO> listeners) {
        MultivaluedMap<String, EventResponseDTO> groups = new MultivaluedHashMap<>();
        listeners.forEach((ev) -> {
            groups.add(ev.getService(), ev);
        });
        return groups;
    }

    protected int chooseTarget(List targets) {
        if (targets == null || targets.isEmpty()) {
            throw new NoTargetFound();
        }
        return random.nextInt(targets.size());
    }

    public void unregisterTarget(int id) {
        try {
            wrap(findAvailableDiscoveryService().path("api/v1/services/" + id)
                    .request(MediaType.APPLICATION_JSON))
                    .delete()
                    .close();
        } catch (IOException ex) {
            DiscoveryClientLogger.LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    protected Builder wrap(Builder builder) {
        System.out.println("auth header: " + "Discovery " + factory.getToken());
        return builder.header("Authorization", "Discovery " + factory.getToken());
    }

}
