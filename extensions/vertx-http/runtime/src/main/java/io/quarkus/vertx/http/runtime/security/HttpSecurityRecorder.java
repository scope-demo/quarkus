package io.quarkus.vertx.http.runtime.security;

import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;

import javax.enterprise.inject.spi.CDI;

import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.SecurityIdentity;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class HttpSecurityRecorder {

    public Handler<RoutingContext> authenticationMechanismHandler() {
        return new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext event) {
                HttpAuthenticator authenticator = CDI.current().select(HttpAuthenticator.class).get();
                //we put the authenticator into the routing context so it can be used by other systems
                event.put(HttpAuthenticator.class.getName(), authenticator);
                authenticator.attemptAuthentication(event).handle(new BiFunction<SecurityIdentity, Throwable, Object>() {
                    @Override
                    public Object apply(SecurityIdentity identity, Throwable throwable) {
                        if (throwable != null) {
                            while (throwable instanceof CompletionException && throwable.getCause() != null) {
                                throwable = throwable.getCause();
                            }
                            //auth failed
                            if (throwable instanceof AuthenticationFailedException) {
                                authenticator.sendChallenge(event, new Runnable() {
                                    @Override
                                    public void run() {
                                        event.response().end();
                                    }
                                });
                            } else {
                                event.fail(throwable);
                            }
                            return null;
                        }
                        if (identity != null) {
                            event.setUser(new QuarkusHttpUser(identity));
                        }
                        event.next();
                        return null;
                    }
                });
            }
        };
    }

}
