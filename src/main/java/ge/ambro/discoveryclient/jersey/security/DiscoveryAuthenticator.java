/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ge.ambro.discoveryclient.jersey.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import ge.ambro.discoveryclient.JwtHelper;
import ge.ambro.jerseyutils.security.Authenticator;
import ge.ambro.jerseyutils.security.impl.SimpleSecurityContext;
import ge.ambro.jerseyutils.security.impl.UsernamePrincipal;
import ge.ambro.jerseyutils.security.model.AuthenticationData;
import java.util.Date;
import java.util.Objects;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author tabramishvili
 */
public class DiscoveryAuthenticator implements Authenticator {

    private final JwtHelper validator;

    public DiscoveryAuthenticator(JwtHelper tf) {
        validator = tf;
    }

    @Override
    public boolean supports(String type) {
        return Objects.equals(DiscoveryAuthenticationDataExtractor.AUTHENTICATION_TYPE, type);
    }

    @Override
    public SecurityContext authenticateUser(AuthenticationData token, ContainerRequestContext context) {
        DiscoveryAuthenticationData data = (DiscoveryAuthenticationData) token;
        if (data.getToken() == null) {
            return null;
        }

        return authenticateUser(data, context.getUriInfo().getAbsolutePath().getScheme());
    }

    protected SecurityContext authenticateUser(DiscoveryAuthenticationData authData, String scheme) {
        String token = authData.getToken();
        try {
            DecodedJWT jwt = validator.decodeToken(token);
            if (jwt.getExpiresAt().before(new Date())) {
                return null;
            }
            return new SimpleSecurityContext(new UsernamePrincipal(jwt.getClaim("name").asString()), scheme)
                    .addRoles(jwt.getClaim("roles").asArray(String.class));
        } catch (JWTVerificationException ex) {
            return null;
        }

    }

}
