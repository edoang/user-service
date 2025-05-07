package org.acme.users.rest;

import io.quarkus.logging.Log;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.acme.users.model.Game;
import org.acme.users.rest.client.GameClient;
import org.acme.users.rest.client.PartyMemberClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.Collection;
import java.util.Date;

@Path("/quarkus-gate/game")
public class GameResource {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance listofgames(
                Collection<Game> games,
                Boolean activeGame);
    }

    @Inject
    SecurityContext securityContext;

    @RestClient
    GameClient gameClient;

    @RestClient
    PartyMemberClient partyMemberClient;

    /**
     * Retrieves a collection of games and determines if there is at least one active game.
     * This method invokes the GameClient to fetch all games and uses the
     * retrieved data to create a TemplateInstance for rendering the list of games.
     *
     * @return A TemplateInstance representing the list of games, along with a boolean indicating
     * if there is at least one active game.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/get")
    public TemplateInstance getGames() {
        Collection<Game> games
                = gameClient.get(false);
        Collection<Game> activeGames = gameClient.get(false);
        return Templates.listofgames(games,
                activeGames.size() >= 1);
    }

    /**
     * Starts a new game, persists it, and retrieves a collection of active games.
     * The method creates a new game instance, invokes the GameClient to persist the game,
     * and fetches all games that are not marked as over. It then returns a
     * TemplateInstance for rendering the list of games, along with a header indicating
     * that the client should update its content after rendering.
     * Emits the update-all event after the htmx swap
     *
     * @return A RestResponse containing a TemplateInstance, which renders the list of
     * all games and indicates whether there is at least one active game.
     */
    @POST
    @Produces(MediaType.TEXT_HTML)
    @Path("/play")
    public RestResponse<TemplateInstance> play() {
        Game game = Game
                .builder()
                .over(false)
                .created(new Date())
                .won(0)
                .lost(0)
                .build();
        Game persisted = gameClient.play(game);
        Log.info("game started: " + persisted);

        Collection<Game> activeGames = gameClient.get(false);

        return RestResponse.ResponseBuilder
                .ok(Templates.listofgames(
                        gameClient.get(false),
                        activeGames.size() >= 1))
                .header("HX-Trigger-After-Swap",
                        "update-all")
                .build();

    }

    /**
     * Marks a game as over and updates the user interface with the updated list of games.
     * This method performs the following actions:
     * - Retrieves the username of the currently authenticated user from the security context.
     * - Removes the user from any party memberships.
     * - Marks the specified game as over using a remote call to the GameClient.
     * - Logs the completion of the game.
     * - Returns an updated list of games as a TemplateInstance wrapped in a RestResponse.
     * - emits the update-all event after the htmx swap
     *
     * @param id The unique identifier of the game to be marked as over.
     * @return A RestResponse containing a TemplateInstance that renders the updated list of games.
     */
    @POST
    @Produces(MediaType.TEXT_HTML)
    @Path("/over")
    public RestResponse<TemplateInstance> over(@RestForm Long id) {
        String name = securityContext.getUserPrincipal().getName();
        partyMemberClient.removeUserPartyMember(name);
        Game persisted = gameClient.over(id);
        Log.info("over game: " + persisted);

        return RestResponse.ResponseBuilder
                .ok(getGames())
                .header("HX-Trigger-After-Swap",
                        "update-all")
                .build();
    }

}
