/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ge.ambro.discoveryclient.jersey;

import ge.ambro.discoveryclient.dto.DependencyDTO;
import ge.ambro.discoveryclient.dto.EventDTO;
import ge.ambro.discoveryclient.dto.TargetDTO;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.server.model.AnnotatedMethod;

/**
 *
 * @author tabramishvili
 */
public class DiscoveryDynamicResourceLoader implements DynamicFeature {

    private final String rootPath;
    private final DiscoveryRegistrator registrator;

    DiscoveryDynamicResourceLoader(DiscoveryRegistrator registrator, String path) {
        this.registrator = registrator;
        rootPath = path;
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        final AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());
        if (am.isAnnotationPresent(DiscoveryTarget.class)) {
            URI uri = UriBuilder.fromPath(rootPath)
                    .path(resourceInfo.getResourceClass())
                    .path(resourceInfo.getResourceMethod())
                    .build();

            DiscoveryTarget ma = am.getAnnotation(DiscoveryTarget.class);
            TargetDTO target = new TargetDTO();
            target.setName(ma.value());
            target.setMethod(detectHttpMethod(resourceInfo.getResourceMethod()));
            target.setPath(uri.toString());
            target.setDependencies(Arrays.stream(ma.dependencies()).map((d) -> {
                DependencyDTO dto = new DependencyDTO();
                dto.setAddress(d.value());
                dto.setPriority(d.priority());
                return dto;
            }).collect(Collectors.toList()));
            registrator.addTarget(target);
        }
        if (am.isAnnotationPresent(DiscoveryEvent.class)) {
            URI uri = UriBuilder.fromPath(rootPath)
                    .path(resourceInfo.getResourceClass())
                    .path(resourceInfo.getResourceMethod())
                    .build();

            DiscoveryEvent ma = am.getAnnotation(DiscoveryEvent.class);
            EventDTO event = new EventDTO();
            event.setName(ma.value());
            event.setMethod(detectHttpMethod(resourceInfo.getResourceMethod()));
            event.setPath(uri.toString());
            registrator.addEvent(event);
        }
    }

    public String detectHttpMethod(Method m) {
        for (Annotation an : m.getDeclaredAnnotations()) {
            if (Arrays.asList("GET", "POST", "PUT", "DELETE").contains(an.annotationType().getSimpleName())) {
                return an.annotationType().getSimpleName();
            }
        }
        return null;
    }

}
