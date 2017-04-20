package net.pla1.tweettooter;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

public class Bot {
    private final String URL_MASTODON = "https://octodon.social/auth/sign_in";
    private final String URL_TWITTER = "https://twitter.com/search?f=tweets&vertical=default&q=chswx&src=typd";

    public static void main(String[] args) throws Exception {
        System.out.format("%s", new Date());
        Bot bot = new Bot();
        try {
            bot.startBrowser();
            bot.monitorForNewResults();
        } catch (FindFailed findFailed) {
            findFailed.printStackTrace();
        }
        System.out.format("Done. %s\n", new Date());
        System.exit(0);
    }

    private void loginToTwitter() throws FindFailed, IOException {
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
        System.out.println(Utils.run(new String[]{"/usr/bin/killall", "chromium-browser"}));
        Utils.runNoOutput(new String[]{"/usr/bin/chromium-browser", "--incognito", URL_TWITTER});
        Utils.waitForImage(s, "images/latest_label.png", 10);
        s.type(Key.ESC);
        s.type("t", KeyModifier.CTRL);
        s.type(URL_MASTODON);
        s.type(Key.ENTER);
        Utils.waitForImage(s, "images/mastodon_logo.png", 10);
        Properties properties = new Properties();
        properties.load(new FileInputStream(Utils.PROPERTY_FILE_NAME));
        s.type(properties.getProperty(Utils.PROPERTY_MASTODON_EMAIL_ADDRESS));
        s.type(Key.TAB);
        s.type(properties.getProperty(Utils.PROPERTY_MASTODON_PASSWORD));
        s.type(Key.ENTER);
        s.type(Key.TAB, KeyModifier.CTRL);
    }

    private Region getRegionToMonitorForNewResults() throws FindFailed {
        Screen s = new Screen();
        Utils.waitForImage(s, "images/twitter_banner.png", 60);
        Match match = s.find("images/twitter_banner.png");
        System.out.format("Found Twitter banner - X:%d Y:%d H:%d W:%d\n", match.x, match.y, match.h, match.w);
        Region region = new Region(match.x + match.w - 200, match.y + 30, 400, 100);
        String screenCaptureFileName = region.saveScreenCapture();
        Utils.runNoOutput(new String[]{"/usr/bin/xdg-open", screenCaptureFileName});
        return region;
    }

    private void monitorForNewResults() throws FindFailed {
        Screen s = new Screen();
        Region regionToMonitor = getRegionToMonitorForNewResults();
        while (true) {
            System.out.format("Wait for new result. %s\n", new Date());
            regionToMonitor.wait("images/new_result_label.png", Settings.FOREVER);
            System.out.println("Type period");
            s.type(".");
            System.out.println("Sleep 3 seconds.");
            Utils.sleep(3);
            System.out.println("Press ENTER.");
            s.type(Key.ENTER);
            Utils.sleep(5);
            s.type(Key.TAB);
            s.type(Key.TAB);
            s.type(Key.TAB);
            s.type(Key.SPACE);
            System.out.println("Click copy link to tweet.");
            s.type(Key.DOWN);
            s.type(Key.ENTER);
            Utils.waitForImage(s, "images/url_for_this_tweet.png", 10);
            System.out.println("Ctrl-c");
            s.type("c", KeyModifier.CTRL);
            String clipboardContents = Utils.getClipboard();
            System.out.format("Clipboard contents: %s\n", clipboardContents);
            s.type(Key.ESC);
            Utils.sleep(2);
            Match topLeftCornerMatch = s.find("images/top_left_corner.png");
            System.out.format("Top left corner at H: %s W: %d X: %d Y: %d\n", topLeftCornerMatch.h, topLeftCornerMatch.w, topLeftCornerMatch.x, topLeftCornerMatch.y);
            Match bottomRightCornerMatch = s.find("images/bottom_right_corner.png");
            System.out.format("Bottom right corner at H: %s W: %d X: %d Y: %d\n", bottomRightCornerMatch.h, bottomRightCornerMatch.w, bottomRightCornerMatch.x, bottomRightCornerMatch.y);
            Region region = new Region(
                    topLeftCornerMatch.x,
                    topLeftCornerMatch.y,
                    (bottomRightCornerMatch.x - topLeftCornerMatch.x) + bottomRightCornerMatch.w,
                    bottomRightCornerMatch.y - topLeftCornerMatch.y);
            System.out.format("Region  X:%d  Y:%d  W:%d  H:%d.\n", region.x, region.y, region.w, region.h);
            String fileName = region.saveScreenCapture();
            String copiedFile = Utils.copyFile(fileName);
            System.out.format("Image captured as %s and a copy %s.\n", fileName, copiedFile);
            s.type(Key.ESC);
            s.type(Key.TAB, KeyModifier.CTRL);
            if (Utils.isNotBlank(fileName)
                    && Utils.isNotBlank(clipboardContents)
                    && region.w > 200) {
                s.click("images/mastodon_media_button.png");
                s.click("images/file_system_label.png");
                s.type(fileName);
                s.type(Key.ENTER);
                Utils.sleep(3);
                s.type(clipboardContents);
                s.type(" #chswx ");
                s.click("images/toot_button.png");
                Utils.sleep(2);
                s.type(Key.TAB, KeyModifier.CTRL);
            } else {
                System.out.format("Requirements for a successful post were not met.\n\tScreenshot file name: %s\n\tClipboard contents: \"%s\"\n\tRegion width: %d\n",
                        fileName, clipboardContents, region.w);
            }
        }
    }
}
