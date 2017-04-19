package net.pla1.tweettooter;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

public class Bot {
    private final String URL_MASTODON = "https://octodon.social/auth/sign_in";
    private final String URL_TWITTER = "https://twitter.com/hashtag/chswx?f=tweets&vertical=default&src=tren";

    public static void main(String[] args) throws Exception {

        System.out.format("%s", new Date());
        Bot bot = new Bot();
        if (false) {
            bot.startBrowser();
            System.exit(0);
        }
        try {
            bot.startBrowser();
            bot.monitorForNewResults();
        } catch (FindFailed findFailed) {
            findFailed.printStackTrace();
        }
        System.out.format("Done. %s\n", new Date());
        System.exit(0);
    }

    private void login() throws FindFailed, IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(Utils.PROPERTY_FILE_NAME));
        Screen s = new Screen();
        Utils.sleep(5);
        s.click("images/login.png");
        s.click("images/username_field.png");
        s.type(properties.getProperty(Utils.PROPERTY_TWITTER_USER_NAME));
        s.click("images/password_field.png");
        s.type(properties.getProperty(Utils.PROPERTY_TWITTER_PASSWORD));
        s.type(Key.TAB);
        Settings.MinSimilarity = 0.9;
        s.click("images/login_button_authenticate.png");
        s.click("images/search_twitter_field.png");
        s.type("chswx");
        s.type(Key.ENTER);
        s.click("images/latest_label.png");
    }

    private void startBrowser() throws FindFailed, IOException {

        Screen s = new Screen();
        System.out.println(Utils.run(new String[]{"/usr/bin/killall", "chrome"}));
        String[] commandParts = {"/usr/bin/chromium-browser", URL_TWITTER};
        Utils.runNoOutput(commandParts);
        Utils.waitForImage(s, "images/latest_label.png", 10);
        s.type("t", KeyModifier.CTRL);
        s.type(URL_MASTODON);
        s.type(Key.ENTER);
        Utils.waitForImage(s, "images/email_address_field.png", 10);
        Properties properties = new Properties();
        properties.load(new FileInputStream(Utils.PROPERTY_FILE_NAME));
        s.type(properties.getProperty(Utils.PROPERTY_MASTODON_EMAIL_ADDRESS));
        s.click("images/mastodon_password_field.png");
        s.type(properties.getProperty(Utils.PROPERTY_MASTODON_PASSWORD));
        s.type(Key.ENTER);
        s.type(Key.TAB, KeyModifier.CTRL);
    }

    private void monitorForNewResults() throws FindFailed {
        boolean done = false;
        Screen s = new Screen();
        while (!done) {
            System.out.format("Wait for new result. %s\n", new Date());
            if (Utils.waitForImage(s, "images/new_result_label.png", 60)) {
                System.out.println("Type period");
                s.type(".");
                System.out.println("Sleep 1 second.");
                Utils.sleep(3);
                System.out.println("Press ENTER.");
                s.type(Key.ENTER);
                Utils.sleep(5);
                s.type(Key.TAB);
                s.type(Key.TAB);
                s.type(Key.TAB);
                s.type(Key.SPACE);
                //   System.out.println("Click down arrow.");
                //  s.click("images/down_arrow.png");
                System.out.println("Click copy link to tweet.");
                //   s.type(Key.DOWN);
                s.type(Key.DOWN);
                s.type(Key.ENTER);
                Utils.waitForImage(s, "images/url_for_this_tweet.png", 10);
                System.out.println("Ctrl-c");
                s.type("c", KeyModifier.CTRL);
                String clipboardContents = App.getClipboard();
                System.out.format("Clipboard contents: %s\n", clipboardContents);
                s.type(Key.ESC);
                Utils.sleep(2);
                Match topLeftCornerMatch = s.find("images/top_left_corner.png");
                System.out.format("Top left corner at H: %s W: %d X: %d Y: %d\n", topLeftCornerMatch.h, topLeftCornerMatch.w, topLeftCornerMatch.x, topLeftCornerMatch.y);
                Match bottomRightCornerMatch = s.find("images/bottom_right_corner.png");
                System.out.format("Bottom right corner at H: %s W: %d X: %d Y: %d\n", bottomRightCornerMatch.h, bottomRightCornerMatch.w, bottomRightCornerMatch.x, bottomRightCornerMatch.y);
                Region region = new Region(topLeftCornerMatch.x, topLeftCornerMatch.y, (bottomRightCornerMatch.x - topLeftCornerMatch.x) + bottomRightCornerMatch.w, (bottomRightCornerMatch.y - topLeftCornerMatch.y) - bottomRightCornerMatch.h);
                System.out.format("Region  X:%d  Y:%d  W:%d  H:%d.\n", region.x, region.y, region.w, region.h);
                String fileName = region.saveScreenCapture();
                System.out.format("Image captured %s\n", fileName);
                //   String[] openImageCommandParts = {"xdg-open", fileName};
                //   Utils.run(openImageCommandParts);
                s.type(Key.ESC);
                s.type(Key.TAB, KeyModifier.CTRL);
                if (Utils.isNotBlank(fileName) && Utils.isNotBlank(clipboardContents)) {
                    s.click("images/mastodon_media_button.png");
                    s.click("images/file_system_label.png");
                    s.type(fileName);
                    s.type(Key.ENTER);
                    Utils.sleep(3);
                    s.type(clipboardContents);
                    s.type(Key.SPACE);
                    s.click("images/toot_button.png");
                }
            }
        }
    }
}
