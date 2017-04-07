/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ge.ambro.discoveryclient;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 *
 * @author tabramishvili
 */
public class JwtHelper {

    private String token;
    private Date expirationDate;
    private long intervalMs;

    private final Builder builder;
    private final JWTVerifier verifier;
    private final Algorithm algorithm;

    public JwtHelper(String issuer, String secret) throws UnsupportedEncodingException {
        algorithm = Algorithm.HMAC256(secret);
        builder = JWT.create()
                .withIssuer(issuer);

        verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build();

        intervalMs = 24 * 60 * 60 * 1000;
        expirationDate = new Date();
    }

    public String getToken() {
        if (token == null || expirationDate.before(new Date(new Date().getTime() + 5 * 60 * 1000))) {
            Date expDate = new Date(new Date().getTime() + intervalMs);
            token = builder
                    .withExpiresAt(expDate)
                    .sign(algorithm);
            expirationDate = expDate;
        }
        return token;
    }

    public DecodedJWT decodeToken(String token) throws JWTVerificationException {
        return verifier.verify(token);
    }

    public Builder builder() {
        return builder;
    }

    public JwtHelper withInterval(long interval) {
        this.intervalMs = interval;
        return this;
    }

}
