package org.acme.users.rest.client;


import io.quarkus.oidc.token.propagation.common.AccessToken;
import jakarta.ws.rs.*;
import org.acme.users.model.Game;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.Collection;
import java.util.List;

@RegisterRestClient(configKey = "party")
@AccessToken
@Path("game")
public interface GameClient {

    @POST
    @Path("play")
    Game play(Game game);

    @PUT
    @Path("over")
    Game over(Long id);

    @GET
    @Path("get")
    @Retry(maxRetries = 5, delay = 1000)
    @Fallback(fallbackMethod = "allGamesFallback")
    Collection<Game> get(@QueryParam("over") Boolean over);

    @PUT
    @Path("game-over-user-games")
    void overUserGames(String name);

    default Collection<Game> allGamesFallback(Boolean over) {
        return List.of();
    }
}
