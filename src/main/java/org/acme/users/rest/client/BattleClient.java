package org.acme.users.rest.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestQuery;

@RegisterRestClient(configKey = "battle")
@Path("/battle")
public interface BattleClient {

    @GET
    @Path("/count")
    @Retry(maxRetries = 25, delay = 1000)
    Integer count(@RestQuery String userId,
                  @RestQuery Boolean won);

    @GET
    @Path("/rank")
    @Retry(maxRetries = 25, delay = 1000)
    Integer getRank(@RestQuery String userId);
}
