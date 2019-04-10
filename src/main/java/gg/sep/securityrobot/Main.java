package gg.sep.securityrobot;


import lombok.extern.log4j.Log4j2;

import gg.sep.securityrobot.exceptions.SecurityRobotFatal;

/**
 * Main entry point into the application.
 */
@Log4j2
public final class Main {


    private Main() { }

    /**
     * Main entry point into the application.
     * @param args Command line arguments.
     * @throws SecurityRobotFatal Fatal exception thrown indicating the bot cannot proceed and will exit.
     */
    public static void main(final String[] args) throws SecurityRobotFatal {
        final SecurityRobot securityRobot = new SecurityRobot();
        securityRobot.start();
    }
}
