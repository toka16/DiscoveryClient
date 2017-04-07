/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ge.ambro.discoveryclient.jersey.security;

import ge.ambro.jerseyutils.security.AuthenticationDataExtractor;
import ge.ambro.jerseyutils.security.model.AuthenticationData;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;

/**
 *
 * @author tabramishvili
 */
public class DiscoveryAuthenticationDataExtractor implements AuthenticationDataExtractor {

    public static final String AUTHENTICATION_TYPE = "Discovery";

    @Override
    public boolean supports(ContainerRequestContext requestContext) {
        String authorizationHeader
                = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        return authorizationHeader != null && authorizationHeader.startsWith(AUTHENTICATION_TYPE);
    }

    @Override
    public AuthenticationData extractData(ContainerRequestContext requestContext) {
        String authorizationHeader
                = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Extract the token from the HTTP Authorization header
        String token = authorizationHeader.substring(AUTHENTICATION_TYPE.length()).trim();
        return new DiscoveryAuthenticationData(AUTHENTICATION_TYPE, token);
    }

}
