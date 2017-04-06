/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ge.ambro.discoveryclient.jersey;

import ge.ambro.discoveryclient.ConnectionFactory;
import ge.ambro.discoveryclient.DiscoveryClient;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Singleton;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 *
 * @author tabramishvili
 */
public class DiscoveryFeature implements Feature {

    public static String PROPERTIES_PATH = "discovery.properties";

    @Override
    public boolean configure(FeatureContext context) {
        Properties props = readProperties();

        String[] ss = props.getProperty("discovery.server.address", "").split(",\\s*");
        DiscoveryClient client = new DiscoveryClient(new ConnectionFactory(), ss);

        DiscoveryRegistrator registrator = new DiscoveryRegistrator(client);
        registrator.service.setBase(props.getProperty("discovery.base"));
        registrator.service.setName(props.getProperty("discovery.name"));
        registrator.service.setServiceDescrip(props.getProperty("discovery.descrip"));

        context.register(new AppEventListener(registrator));
        context.register(new DiscoveryDynamicResourceLoader(registrator, props.getProperty("app.path")));

        // register discovery client
        context.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(new Factory<DiscoveryClient>() {
                    @Override
                    public DiscoveryClient provide() {
                        return client;
                    }

                    @Override
                    public void dispose(DiscoveryClient instance) {
                        // pass
                    }
                }).to(DiscoveryClient.class).in(Singleton.class);
            }
        });

        return true;
    }

    protected Properties readProperties() {
        Properties props = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROPERTIES_PATH)) {
            props.load(inputStream);
        } catch (IOException ex) {
            Logger.getLogger(DiscoveryFeature.class.getName()).log(Level.SEVERE, null, ex);
        }
        return props;
    }

}
