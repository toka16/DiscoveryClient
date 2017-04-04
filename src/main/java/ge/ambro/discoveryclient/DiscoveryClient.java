/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ge.ambro.discoveryclient;

import ge.ambro.discoveryclient.dto.ServiceDTO;
import ge.ambro.discoveryclient.exeptions.DiscoveyServerNotAvailable;
import ge.ambro.discoveryclient.exeptions.NoTargetFound;
import ge.ambro.discoveryclient.exeptions.TargetNotAvailable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author tabramishvili
 */
public class DiscoveryClient {

    private final String[] discoveryAddresses;
    private final ConnectionFactory factory;
    private long ts = 0;
    private final Random random;
    private final Map<String, String> targetCache;
    private final Map<String, String> eventCache;

    public DiscoveryClient(ConnectionFactory factory, String... addresses) {
        discoveryAddresses = addresses;
        this.factory = factory;
        random = new Random();
        targetCache = new HashMap<>();
        eventCache = new HashMap<>();
    }

    protected String findAvailableDiscoveryService() throws IOException {
        for (String address : discoveryAddresses) {
            System.out.println("check address: " + Utils.concat(address, "/api/ping"));
            try {
                HttpURLConnection target = factory.createConnection(Utils.concat(address, "/api/ping"));
                target.setRequestMethod("GET");
                if (target.getResponseCode() == 200) {
                    return address;
                }
            } catch (Exception e) {
// ignore
            }
        }
        throw new DiscoveyServerNotAvailable();
    }

    public int register(ServiceDTO service) throws IOException {
        String discovery = findAvailableDiscoveryService();
        HttpURLConnection con = factory.createConnection(
                Utils.concat(discovery, "api/v1/services")
        );
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        System.out.println("service: " + new JSONObject(service));
        con.getOutputStream().write(new JSONObject(service).toString().getBytes("UTF-8"));
        con.connect();
        String res = Utils.readAll(con.getInputStream());
        con.disconnect();
        return Integer.parseInt(res);
    }

    public void event(String name, String dataString) throws IOException {
        String discovery = findAvailableDiscoveryService();
        long cachTs = eventCache.containsKey(name) ? ts : 0;
        String res = makeRequest(
                Utils.concat(discovery, "api/v1/event-listeners?name=" + URLEncoder.encode(name, "UTF-8") + "&ts=" + cachTs),
                eventCache,
                name
        );
        JSONObject listenerGroups = groupListeners(new JSONArray(res));
        JSONObject data = new JSONObject(dataString);
        for (String key : listenerGroups.keySet()) {
            JSONArray listeners = listenerGroups.optJSONArray(key);
            if (listeners == null) {
                listeners = new JSONArray().put(listenerGroups.optJSONObject(key));
            }
            while (true) {
                int index = chooseTarget(listeners);
                JSONObject target = listeners.optJSONObject(index);
                try {
                    sendDataToTarget(target, null, data);
                    break;
                } catch (TargetNotAvailable ex) {
                    notifyInvalidTarget(target);
                    listeners.remove(index);
                }
            }
        }
    }

    public String target(String address, String dataString) throws IOException {
        String discovery = findAvailableDiscoveryService();
        long cachTs = targetCache.containsKey(address) ? ts : 0;
        String res = makeRequest(
                Utils.concat(discovery, "api/v1/targets?address=" + URLEncoder.encode(address, "UTF-8") + "&ts=" + cachTs),
                targetCache,
                address
        );
        if (res == null) {
            throw new NoTargetFound();
        }
        JSONObject response = new JSONObject(res);
        JSONArray targets = response.optJSONArray("targets");
        JSONObject resolves = response.optJSONObject("resolves");
        JSONObject data = new JSONObject();
        if (dataString != null) {
            data.put("data", Utils.strToJSON(dataString));
        }
        String resp = null;
        while (true) {
            int index = chooseTarget(targets);
            JSONObject target = targets.optJSONObject(index);
            try {
                resp = sendDataToTarget(target, resolves, data);
                break;
            } catch (TargetNotAvailable ex) {
                notifyInvalidTarget(target);
                targets.remove(index);
            }
        }
        return resp;
    }

    protected String makeRequest(String url, Map<String, String> cache, String key) throws IOException {
        HttpURLConnection con = factory.createConnection(url);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestMethod("GET");
        con.connect();
        String res;
        if (con.getResponseCode() == 304) {
            res = cache.get(key);
        } else {
            res = Utils.readAll(con.getInputStream());
            cache.clear();
            cache.put(key, res);
            ts = System.currentTimeMillis();
        }
        con.disconnect();
        return res;
    }

    protected String sendDataToTarget(JSONObject target, JSONObject resolves, JSONObject data) throws IOException {
        JSONObject depData = sendDataToDependencies(
                target.optJSONArray("dependencies"),
                resolves,
                data
        );
        String response = null;
        HttpURLConnection con = factory.createConnection(
                Utils.concat(target.optString("base"), target.optString("path"))
        );
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestMethod(target.optString("method"));
        if (depData != null) {
            if (depData.length() > 1 || depData.opt("data") != null) {
                con.setDoOutput(true);
                con.getOutputStream().write(depData.toString().getBytes("UTF-8"));
            }
        }
        try {
            con.connect();

        } catch (IOException ex) {
            Logger.getLogger(DiscoveryClient.class
                    .getName()).log(Level.SEVERE, null, ex);
            throw new TargetNotAvailable();
        }
        response = Utils.readAll(con.getInputStream());
        con.disconnect();
        return response;
    }

    protected JSONObject sendDataToDependencies(JSONArray dependencies, JSONObject resolves, JSONObject data) throws IOException {
        if (dependencies == null || dependencies.length() == 0) {
            return data;
        }
        JSONArray sortedDependencies = Utils.sort(dependencies, "priority");
        for (int i = 0; i < sortedDependencies.length(); i++) {
            JSONObject dep = sortedDependencies.optJSONObject(i);
            JSONArray targets = resolves.optJSONArray(dep.optString("address"));
            String resp = null;
            while (true) {
                int index = chooseTarget(targets);
                JSONObject target = targets.optJSONObject(index);
                try {
                    resp = sendDataToTarget(target, resolves, data);
                    break;
                } catch (TargetNotAvailable ex) {
                    notifyInvalidTarget(target);
                    targets.remove(index);
                }
            }
            data.put(dep.optString("address"), Utils.strToJSON(resp));
        }
        return data;
    }

    protected JSONObject groupListeners(JSONArray listeners) {
        JSONObject o = new JSONObject();
        for (int i = 0; i < listeners.length(); i++) {
            o.accumulate(listeners.optJSONObject(i).optString("service"), listeners.optJSONObject(i));
        }
        return o;
    }

    protected int chooseTarget(JSONArray targets) {
        if (targets.length() == 0) {
            throw new NoTargetFound();
        }
        return random.nextInt(targets.length());
    }

    protected void notifyInvalidTarget(JSONObject target) {
        try {
            String discovery = findAvailableDiscoveryService();
            HttpURLConnection con = factory.createConnection(
                    Utils.concat(discovery, "api/v1/services/" + target.optLong("serviceId"))
            );
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestMethod("PUT");
            con.setDoOutput(true);
            con.getOutputStream().write("{\"alive\": false}".getBytes());
            con.connect();

        } catch (IOException ex) {
            Logger.getLogger(DiscoveryClient.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

}
