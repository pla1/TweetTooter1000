package net.pla1.tweettooter;


import org.sikuli.basics.Settings;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Match;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Logger;

public class Utils {
    public static final String PROPERTY_TWITTER_USER_NAME = "twitterUserName";
    public static final String PROPERTY_TWITTER_PASSWORD = "twitterPassword";
    public static final String PROPERTY_MASTODON_USER_NAME = "mastodonUserName";
    public static final String PROPERTY_MASTODON_PASSWORD = "mastodonPassword";
    public static final String PROPERTY_FILE_NAME = "/etc/net.pla1.tweettooter.Bot.properties";

    public static boolean click(Screen screen, String imageName) {
        try {
            screen.click(imageName);
            System.out.format("%s clicked.\n", imageName);
            return true;
        } catch (FindFailed e) {
            System.out.format("%s not clicked. %s\n", imageName, e.getLocalizedMessage());
        }
        return false;
    }

    public static boolean clickFirstFound(Screen screen, String... imageNames) {
        for (String imageName : imageNames) {
            try {
                screen.click(imageName);
                System.out.format("%s clicked.\n", imageName);
                return true;
            } catch (FindFailed e) {
                System.out.format("%s not clicked. %s\n", imageName, e.getLocalizedMessage());
            }
        }
        return false;
    }

    public static void clickIgnoreFail(Screen screen, String image) {
        try {
            screen.click(image);
            System.out.format("Clicked on %s.\n", image);
        } catch (FindFailed e) {
            System.out.format("Did not click on %s.\n", image);
        }
    }

    public static void close(BufferedReader bufferedReader) {
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (IOException e) {
            }
        }
    }

    public static void close(Socket socket) {
        if (socket == null) {
            return;
        }
        try {
            socket.close();
        } catch (IOException e) {
        }
    }

    public static void close(ServerSocket serverSocket) {
        if (serverSocket == null) {
            return;
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
        }
    }

    public static boolean doubleClickFirstFound(Screen screen, String... imageNames) {
        for (String imageName : imageNames) {
            try {
                screen.doubleClick(imageName);
                System.out.format("%s double clicked.\n", imageName);
                return true;
            } catch (FindFailed e) {
                System.out.format("%s not double clicked. %s\n", imageName, e.getLocalizedMessage());
            }
        }
        return false;
    }

    public static int getInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return 0;
        }
    }

    public static String[] getStringArrayFromFileLines(String fileName) {
        return readFileToString(fileName).split("\n");
    }

    public static boolean isBlank(String s) {
        return (s == null || s.trim().length() == 0);
    }

    public static boolean isBlank(StringBuilder sb) {
        if (sb == null) {
            return true;
        } else {
            return isBlank(sb.toString());
        }
    }

    public static boolean isDeveloperEnvironment() {
        String[] commandParts = {"/bin/hostname", "-I"};
        String output = run(commandParts);
        System.out.println(output);
        return output.contains("10.6.0.43");
    }

    public static boolean isFound(Screen screen, String imageName) {
        try {
            screen.find(imageName);
            return true;
        } catch (FindFailed e) {
            return false;
        }
    }

    public static boolean isNotBlank(String s) {
        return !isBlank(s);
    }

    public static boolean isNotBlank(StringBuilder sb) {
        return !isBlank(sb);
    }

    public static boolean isRdesktopRunning() {
        long start = System.currentTimeMillis();
        System.out.println("Check for rdesktop process using ps. ");
        String[] commandParts = {"ps", "-A"};
        if (Utils.run(commandParts).contains("rdesktop")) {
            System.out.println("rdesktop is running. Elapsed milliseconds: " + (System.currentTimeMillis() - start));
            return true;
        } else {
            System.out.println("rdesktop is NOT running. Elapsed milliseconds: " + (System.currentTimeMillis() - start));
            return false;
        }
    }

    public static String killRemoteDesktop() {
        String[] commandParts = {"/usr/bin/killall", "rdesktop"};
        return run(commandParts);
    }

    public static void main(String[] args) {
        if (true) {
            Screen s = new Screen();
            Utils.waitForImage(s, "images/index_out_of_bounds.png", 20);
            s.click();
            System.exit(0);
        }
        if (false) {
            String[] stringArray = getStringArrayFromFileLines("/tmp/usps.txt");
            for (String s : stringArray) {
                System.out.println(s);
            }
            System.out.format("%d strings in array\n", stringArray.length);
            System.exit(0);
        }
    }

    public static String readFileToString(String fileName) {
        StringBuilder sb = new StringBuilder();
        File file = new File(fileName);
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            String line = bufferedReader.readLine();
            String lineSeparator = System.getProperty("line.separator");
            while (line != null) {
                sb.append(line);
                sb.append(lineSeparator);
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        close(bufferedReader);
        return sb.toString();
    }

    public static String run(String[] commandParts) {
        Logger.getAnonymousLogger().info("Command parts quantity: " + commandParts.length);
        for (String part : commandParts) {
            Logger.getAnonymousLogger().info("Command part: " + part);
        }
        BufferedReader reader = null;
        StringBuilder output = new StringBuilder();
        int exitValue = 0;
        try {
            // Runtime runtime = Runtime.getRuntime();
            // Process process = runtime.exec(commandParts);
            ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList(commandParts));
            Process process = processBuilder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String lineRead = null;
            while ((lineRead = reader.readLine()) != null) {
                output.append(lineRead);
                output.append("\n");
            }
            exitValue = process.waitFor();
        } catch (Exception e) {
            output.append("Exception: " + e.getLocalizedMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        Logger.getAnonymousLogger().info("\nProcess exit value: " + exitValue);
        return output.toString();
    }

    public static void runNoOutput(String[] commandParts) {
        Logger.getAnonymousLogger().info("Command parts quantity: " + commandParts.length);
        for (String part : commandParts) {
            Logger.getAnonymousLogger().info("Command part: " + part);
        }
        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(commandParts);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void screenCapture(Screen s, File file) {
        ScreenImage screenImage = s.capture(s.getBounds());
        File screenshot = new File(screenImage.getFile());
        screenshot.renameTo(file);
        System.out.format("Screenshot saved to file: %s\n", file.getAbsolutePath());
    }

    public static void sleep(int seconds) {
        try {
            System.out.format("Sleeping %d seconds.\n", seconds);
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String trim(String s) {
        if (s == null) {
            return "";
        } else {
            return s.trim();
        }
    }

    public static boolean waitClick(Screen screen, String image, int seconds) throws FindFailed {
        if (waitForImage(screen, image, seconds)) {
            screen.click(image);
            return true;
        }
        return false;
    }

    public static boolean waitForImage(Screen screen, String image, int seconds) {
        boolean found = false;
        long start = System.currentTimeMillis();
        long expired = (System.currentTimeMillis() - start) / 1000;
        while (expired < seconds && !found) {
            System.out.format("Expired seconds: %d Threshold: %d\n", expired, seconds);
            try {
                Settings.MinSimilarity = 0.9;
                Iterator<Match> iter = screen.findAll(image);
                while (iter.hasNext()) {
                    System.out.println("FOUND: " + iter.next());
                    found = true;
                }
            } catch (FindFailed e) {
                System.out.format("%s\n", e.getLocalizedMessage());
            }
            if (!found) {
                sleep(1);
            }
            expired = (System.currentTimeMillis() - start) / 1000;
        }
        System.out.format("Expired seconds: %d Threshold: %d\n", expired, seconds);
        return found;
    }
}
