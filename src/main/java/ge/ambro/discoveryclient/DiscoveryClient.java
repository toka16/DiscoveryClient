/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ge.ambro.discoveryclient;

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

    public void event(String name, String dataString) throws IOException {
        String discovery = findAvailableDiscoveryService();
        HttpURLConnection con = factory.createConnection(
                Utils.concat(discovery, "api/v1/event-listeners?name=" + URLEncoder.encode(name, "UTF-8") + "&ts=" + ts)
        );
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestMethod("GET");
        con.connect();
        String res;
        if (con.getResponseCode() == 304) {
            res = eventCache.get(name);
        } else {
            res = Utils.readAll(con.getInputStream());
            eventCache.clear();
            eventCache.put(name, res);
            ts = System.currentTimeMillis();
        }
        con.disconnect();
        JSONArray listeners = new JSONArray(res);
        JSONObject data = new JSONObject(dataString);
        for (int i = 0; i < listeners.length(); i++) {
            JSONObject listener = listeners.optJSONObject(i);
            sendDataToTarget(listener, null, data);
        }
    }

    public String target(String address, String dataString) throws IOException {
        String discovery = findAvailableDiscoveryService();
        HttpURLConnection con = factory.createConnection(
                Utils.concat(discovery, "api/v1/targets?address=" + URLEncoder.encode(address, "UTF-8") + "&ts=" + ts)
        );
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestMethod("GET");
        con.connect();
        String res;
        if (con.getResponseCode() == 304) {
            res = targetCache.get(address);
        } else {
            res = Utils.readAll(con.getInputStream());
            targetCache.clear();
            targetCache.put(address, res);
            ts = System.currentTimeMillis();
        }
        con.disconnect();
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
            con.setDoOutput(true);
            con.getOutputStream().write(depData.toString().getBytes("UTF-8"));
        }
        try {
            con.connect();
        } catch (IOException ex) {
            Logger.getLogger(DiscoveryClient.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(DiscoveryClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
