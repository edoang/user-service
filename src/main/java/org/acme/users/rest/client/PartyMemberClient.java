package org.acme.users.rest.client;


import io.quarkus.oidc.token.propagation.common.AccessToken;
import jakarta.ws.rs.*;
import org.acme.users.model.FightRequest;
import org.acme.users.model.Hero;
import org.acme.users.model.PartyMember;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.Collection;
import java.util.List;

@RegisterRestClient(configKey = "party")
@AccessToken
@Path("party")
public interface PartyMemberClient {

    @GET
    @Path("all")
    @Retry(maxRetries = 5, delay = 1000)
    @Fallback(fallbackMethod = "allPartyMembersFallback")
    Collection<PartyMember> allPartyMembers();

    @POST
    @Path("make")
    PartyMember make(PartyMember partyMember);

    @DELETE
    @Path("remove-user-parties")
    Void removeUserPartyMember(String userId);

    @PUT
    @Path("fight")
    Void fight(FightRequest fightRequest);

    @GET
    @Path("availability")
    @Retry(maxRetries = 5, delay = 1000)
    @Fallback(fallbackMethod = "availabilityFallback")
    Collection<Hero> availability();

    default Collection<PartyMember> allPartyMembersFallback() {
        return List.of();
    }

    default Collection<Hero> availabilityFallback() {
        return List.of();
    }
}
