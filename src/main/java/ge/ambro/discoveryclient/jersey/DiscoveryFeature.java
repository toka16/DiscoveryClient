/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ge.ambro.discoveryclient.jersey;

import ge.ambro.discoveryclient.ConnectionFactory;
import ge.ambro.discoveryclient.DiscoveryClient;
import ge.ambro.discoveryclient.DiscoveryClientLogger;
import ge.ambro.discoveryclient.JwtHelper;
import ge.ambro.discoveryclient.jersey.security.DiscoveryAuthenticationDataExtractor;
import ge.ambro.discoveryclient.jersey.security.DiscoveryAuthenticator;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
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
        String issuer = props.getProperty("discovery.security.issuer", "");
        String secret = props.getProperty("discovery.security.secret", "");
        String name = props.getProperty("discovery.security.name", "");
        String[] roles = props.getProperty("discovery.security.roles", "").split(",\\s*");
        DiscoveryClientLogger.LOGGER.log(Level.CONFIG, "discovery.security.issuer: {0}", issuer);
        DiscoveryClientLogger.LOGGER.log(Level.CONFIG, "discovery.security.name: {0}", name);
        DiscoveryClientLogger.LOGGER.log(Level.CONFIG, "discovery.security.roles: {0}", Arrays.toString(roles));
        JwtHelper jh;
        try {
            jh = new JwtHelper(issuer, secret);
            jh.builder()
                    .withClaim("name", name)
                    .withArrayClaim("roles", roles);
        } catch (UnsupportedEncodingException ex) {
            DiscoveryClientLogger.LOGGER.log(Level.SEVERE, null, ex);
            DiscoveryClientLogger.LOGGER.log(Level.CONFIG, "Discovery feature not registered");
            return false;
        }

        DiscoveryClient client = new DiscoveryClient(new ConnectionFactory(jh), ss);

        DiscoveryRegistrator registrator = new DiscoveryRegistrator(client);
        registrator.service.setBase(props.getProperty("discovery.base"));
        registrator.service.setName(props.getProperty("discovery.name"));
        registrator.service.setServiceDescrip(props.getProperty("discovery.descrip"));

        context.register(new AppEventListener(registrator));
        context.register(new DiscoveryDynamicResourceLoader(registrator, props.getProperty("app.path")));

        //security
        context.register(DiscoveryAuthenticationDataExtractor.class);
        context.register(new DiscoveryAuthenticator(jh));

        // injections
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
        context.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(new Factory<DiscoveryRegistrator>() {
                    @Override
                    public DiscoveryRegistrator provide() {
                        return registrator;
                    }

                    @Override
                    public void dispose(DiscoveryRegistrator instance) {
                        // pass
                    }
                }).to(DiscoveryRegistrator.class).in(Singleton.class);
            }
        });

        return true;
    }

    protected Properties readProperties() {
        Properties props = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROPERTIES_PATH)) {
            props.load(inputStream);
        } catch (IOException ex) {
            DiscoveryClientLogger.LOGGER.log(Level.SEVERE, null, ex);
        }
        return props;
    }

}
