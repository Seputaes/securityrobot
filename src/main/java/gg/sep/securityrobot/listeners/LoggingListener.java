package gg.sep.securityrobot.listeners;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.engio.mbassy.listener.Handler;
import org.bson.Document;
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent;

import gg.sep.securityrobot.SecurityRobot;
import gg.sep.securityrobot.models.twitch.tmi.TwitchChannelMessage;

/**
 * IRC Event listener for handling logging of channel messages when appropriate.
 * Listens to {@link ChannelMessageEvent} events.
 */
public class LoggingListener {

    private SecurityRobot securityRobot;
    private MongoCollection<Document> logCollection;

    /**
     * Create an instance of the Logging Listener for the specified bot instance, and the database.
     * @param securityRobot Bot instance which will use the listener.
     * @param mongoDatabase Mongo Database object to be used for logging.
     */
    public LoggingListener(final SecurityRobot securityRobot, final MongoDatabase mongoDatabase) {
        this.securityRobot = securityRobot;
        logCollection = mongoDatabase.getCollection("security_robot_logs");
    }

    /**
     * Receives all channel message events and logs them in the database.
     * @param event Raw Kitteh channel message event.
     */
    @Handler
    public void logChannelMessage(final ChannelMessageEvent event) {
        final TwitchChannelMessage message = new TwitchChannelMessage(event, this.securityRobot);

        if (message.getCleanChannelName().equals(securityRobot.getConfig().getTwitch().getStreamChannel())) {
            logCollection.insertOne(Document.parse(message.toLog().toJson()));

        }
    }
}
