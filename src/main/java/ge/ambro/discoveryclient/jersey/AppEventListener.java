/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ge.ambro.discoveryclient.jersey;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

/**
 *
 * @author tabramishvili
 */
public class AppEventListener implements ApplicationEventListener {

    private final DiscoveryRegistrator registrator;

    public AppEventListener(DiscoveryRegistrator r) {
        registrator = r;
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.getType() == ApplicationEvent.Type.INITIALIZATION_FINISHED) {
            registrator.register();
        } else if (event.getType() == ApplicationEvent.Type.DESTROY_FINISHED) {
            registrator.unregister();
        }
    }

    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {
        return null;
    }
}
