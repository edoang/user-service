package org.acme.users.rest.sse;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import org.acme.users.model.BattleUpdate;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
@Path("/quarkus-gate/notifications/sse")
public class SseResource {

    private final Map<String, SseEventSink> userClients = new ConcurrentHashMap<>(); // user -> notifications client
    private Sse sse;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Context
    JsonWebToken jwt;

    /**
     * Registers a client for Server-Sent Events (SSE) notifications. The client connection is maintained in the
     * internal user-client map for sending real-time notifications.
     *
     * @param eventSink the SseEventSink instance, representing the connection to the client, allowing
     *                  server-sent events to be pushed to the client.
     * @param sse       the Sse instance used to create server-sent events.
     */
    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void getNotifications(@jakarta.ws.rs.core.Context SseEventSink eventSink,
                                 @jakarta.ws.rs.core.Context Sse sse) {
        this.sse = sse;
        String userId = jwt.getName();
        userClients.put(userId, eventSink);

        Log.info("User registered for notifications: " + userId);

    }

    /**
     * Processes a battle update message received from the "battles-update" channel.
     * Sends a Server-Sent Event (SSE) notification to the user if they have an active connection.
     * Logs the status of the operation, whether the notification was delivered or if the user has no active connection.
     * Acknowledges the received message upon successful processing.
     * Handles any exceptions that occur during the processing of the message or sending the event.
     *
     * @param updateMsg The message containing the battle update, which includes the user ID and the update message payload.
     * @return A {@code CompletionStage<Void>} indicating the completion of the processing and acknowledgment of the message.
     */
    @Incoming("battles-update")
    public CompletionStage<Void> battleUpdate(Message<BattleUpdate> updateMsg) {
        return Uni.createFrom().voidItem()
                .onItem().invoke(() -> {
                    try {
                        BattleUpdate update = updateMsg.getPayload();
                        SseEventSink sink = userClients.get(update.getUser());
                        if (sink != null) {
                            OutboundSseEvent event = sse.newEvent("battle-update:" + update.getMessage());
                            sink.send(event);
                            Log.info("News sent for user: " + update.getUser());
                        } else {
                            Log.info("No active connection for user: " + update.getUser());
                        }
                    } catch (Exception e) {
                        Log.error("Error processing battle update message", e);
                    }
                })
                .chain(() -> Uni.createFrom().completionStage(updateMsg::ack))
                .convert().toCompletionStage();
    }

}