package gg.sep.securityrobot;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;

import gg.sep.twitchapi.helix.Helix;
import gg.sep.twitchapi.helix.model.game.Game;
import gg.sep.twitchapi.helix.model.stream.Stream;
import gg.sep.twitchapi.helix.model.subscription.Subscription;
import gg.sep.twitchapi.helix.model.tag.Tag;
import gg.sep.twitchapi.helix.model.user.Follow;
import gg.sep.twitchapi.helix.model.user.User;
import gg.sep.twitchapi.helix.model.video.Video;
import gg.sep.twitchapi.kraken.Kraken;
import gg.sep.twitchapi.kraken.model.channel.Channel;
import gg.sep.twitchapi.kraken.model.channel.Follower;
import gg.sep.twitchapi.kraken.model.team.Team;

/**
 * Main entry point into the application.
 */
@Log4j2
public final class Main {


    private Main() { }

    /**
     * Main entry point into the application.
     * @param args Command line arguments.
     * @throws Exception Fatal exception thrown indicating the bot cannot proceed and will exit.
     */
    public static void main(final String[] args) throws Exception {
        final SecurityRobot securityRobot = new SecurityRobot();
        securityRobot.start();

        final Kraken kraken = securityRobot.getTwitchAPI().getKraken();
        final Helix helix = securityRobot.getTwitchAPI().getHelix();

        final Optional<Channel> channel = kraken.getChannelsAPI().getChannel(24233949);
        final List<Follower> followers = kraken.getChannelsAPI().getFollowers(24233949, 1);
        final List<Team> teams = kraken.getChannelsAPI().getTeams(24233949);
        final List<User> users = helix.getUsersAPI().getUsers(Collections.emptyList(),
            Arrays.asList("seputaes", "kotnat"));
        final List<Video> helixVideos = helix.getVideosAPI().getVideosByBroadcaster("24233949");
        final List<Follow> follows = helix.getUsersAPI().getFollowsAPI().getFollowers("24233949", 0);
        final List<Game> games = helix.getGamesAPI().getGames(Collections.emptyList(),
            Arrays.asList("Science & Technology", "Undertale"));
        final List<Game> topGames = helix.getGamesAPI().getTopGames(50);
        final List<Stream> topStreams = helix.getStreamsAPI().getTopStreams(10);
        final List<Stream> streams = helix.getStreamsAPI().getStreamsByUserLogin(Arrays.asList("Seputaes", "TwitchPresents"));
        final List<Subscription> subscriptions = helix.getSubscriptionsAPI().getSubscriptions("24233949");
        final List<Tag> tags = helix.getTagsAPI().getTags(500);
    }
}
