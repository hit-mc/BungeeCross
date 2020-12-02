package com.keuin.bungeecross.notification;

import com.keuin.bungeecross.BungeeCross;
import com.keuin.bungeecross.message.Message;

import java.io.File;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Make a notification in the first startup after a CI/CD deployment.
 */
public class DeployNotification implements Notification {

    public static final String FLAG_FILE_NAME = "ci_deployed";
    private final Logger logger = Logger.getLogger(DeployNotification.class.getName());
    private final boolean shouldNotify;

    public static final Notification INSTANCE = new DeployNotification();

    private DeployNotification() {
        boolean notify = false;
        try {
            File flagFile = new File(FLAG_FILE_NAME);
            logger.fine(String.format("Working dir: %s", System.getProperty("user.dir")));
            if (flagFile.exists()) {
                logger.fine("CI flag exists.");
                notify = true;
                if (!flagFile.delete()) {
                    logger.severe("Failed to delete CI flag file.");
                }
            } else {
                logger.fine("CI flag does not exist.");
            }
        } catch (SecurityException exception) {
            logger.severe(String.format("Failed to delete CI flag file: %s", exception));
        }
        shouldNotify = notify;
    }

    @Override
    public Notification notifyIfNeeded(Consumer<Message> receiver) {
        if (shouldNotify) {
            receiver.accept(Message.build(
                    String.format(
                            "BungeeCross has been updated by the CI/CD service.\nVersion: %s.",
                            BungeeCross.getVersion()
                    ),
                    "BungeeCross"
            ));
        }
        return this;
    }
}
