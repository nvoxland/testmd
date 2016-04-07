package testmd.util;

import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class LogUtil {

    private static Set<String> loggedMessages = new HashSet<>();

    public static void debugOnce(Logger log, String message) {
        if (loggedMessages.add(message)) {
            log.debug(message);
        }
    }

    public static void infoOnce(Logger log, String message) {
        if (loggedMessages.add(message)) {
            log.info(message);
        }
    }

    public static void warnOnce(Logger log, String message) {
        if (loggedMessages.add(message)) {
            log.warn(message);
        }
    }

    public static void errorOnce(Logger log, String message) {
        if (loggedMessages.add(message)) {
            log.error(message);
        }
    }
}
