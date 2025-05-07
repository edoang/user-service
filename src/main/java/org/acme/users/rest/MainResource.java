package org.acme.users.rest;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.acme.users.model.*;
import org.acme.users.rest.client.BattleClient;
import org.acme.users.rest.client.GameClient;
import org.acme.users.rest.client.PartyMemberClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestResponse;

import java.net.URI;
import java.util.Collection;
import java.util.List;

@Path("/quarkus-gate")
@Named
public class MainResource {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance index(
                Integer won,
                Integer lost,
                String name,
                Integer rank);

        public static native TemplateInstance listofparties(
                Integer won,
                Integer lost,
                Collection<PartyMember> parties,
                Boolean activeGame);

        public static native TemplateInstance availableheroes(
                Collection<Hero> heroes,
                Boolean activeGame
        );
    }

    @Inject
    SecurityContext securityContext;

    @Inject
    @ConfigProperty(name = "quarkus.oidc.logout.path")
    String logoutPath;

    @RestClient
    BattleClient battleClient;

    @RestClient
    GameClient gameClient;

    @RestClient
    PartyMemberClient partyMemberClient;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance index() {

        String username = securityContext.getUserPrincipal().getName();

        Integer won = battleClient.count(
                username, true);

        Integer lost = battleClient.count(
                username, false);

        Integer rank = calculateRankingPosition(username);

        return Templates.index(won, lost,
                username, rank);
    }

    @GET
    @Path("keepalive")
    public Response keepalive() {
        return Response.status(Response.Status.OK)
                .build();
    }

    @GET
    @Path("exit")
    public Response exit() {
        String username = securityContext.getUserPrincipal().getName();
        partyMemberClient.removeUserPartyMember(username);
        gameClient.overUserGames(username);
        return Response.status(Response.Status.FOUND) // 302 Found
                .location(URI.create(logoutPath))
                .build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/get")
    public TemplateInstance getParties() {
        String username = securityContext.getUserPrincipal().getName();
        Collection<PartyMember> partiesCollection
                = partyMemberClient.allPartyMembers();
        Integer won = battleClient.count(
                username, true);

        Integer lost = battleClient.count(
                username, false);
        Collection<Game> activeGames = gameClient.get(false);
        return Templates.listofparties(won, lost, partiesCollection, activeGames.size() >= 1);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/available")
    public TemplateInstance getAvailableHeroes() {
        Collection<Hero> availableHeroes
                = partyMemberClient.availability();
        Collection<Game> activeGames = gameClient.get(false);
        return Templates.availableheroes(
                availableHeroes,
                activeGames.size() >= 1);
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Path("/create")
    public RestResponse<TemplateInstance> create(
            @RestForm Long heroId,
            @RestForm String heroName
    ) {
        PartyMember partyMember = new PartyMember();
        partyMember.heroId = heroId;
        partyMember.heroName = heroName;
        partyMember.fighting = false;
        partyMember.health = 30L;
        partyMemberClient.make(partyMember);
        return RestResponse.ResponseBuilder
                .ok(getParties())
                .header("HX-Trigger-After-Swap",
                        "update-available-heroes-list")
                .build();
    }


    @POST
    @Produces(MediaType.TEXT_HTML)
    @Path("/fight")
    public RestResponse<TemplateInstance> fight(
            @RestForm Long partyMemberId
    ) {

        List<Game> activeGames = (List<Game>) gameClient.get(false);

        if (activeGames.size() != 1) {
            throw new IllegalStateException("too many games dude.");
        }

        partyMemberClient.fight(new FightRequest(partyMemberId, activeGames.get(0).id));
        return RestResponse.ResponseBuilder
                .ok(getParties())
                .header("HX-Trigger-After-Swap",
                        "update-available-heroes-list")
                .build();
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Path("/heal")
    public RestResponse<TemplateInstance> healMe(
            @RestForm Long partyMemberId,
            @RestForm Boolean healAll
    ) {

        List<Game> activeGames = (List<Game>) gameClient.get(false);

        if (activeGames.size() != 1) {
            throw new IllegalStateException("too many games dude.");
        }

        partyMemberClient.heal(new HealRequest(partyMemberId, activeGames.get(0).id, healAll));
        return RestResponse.ResponseBuilder
                .ok(getParties())
                .header("HX-Trigger-After-Swap",
                        "update-available-heroes-list")
                .build();
    }

    private Integer calculateRankingPosition(String user) {
        return battleClient.getRank(
                securityContext.getUserPrincipal().getName());
    }

}
