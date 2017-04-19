package net.pla1.tweettooter;

import org.sikuli.basics.Settings;
import org.sikuli.script.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

public class Bot {
    public static void main(String[] args) throws Exception {

        System.out.format("%s", new Date());
        Bot bot = new Bot();
        try {
            bot.setup();
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

    private void setup() throws FindFailed {
        Screen s = new Screen();
        //  System.out.println(Utils.run(new String[] { "/usr/bin/killall", "chrome" }));
        //     String url = "https://twitter.com/search?f=tweets&vertical=default&q=chswx&src=typd";
        //     String[] commandParts = {"/usr/bin/google-chrome", "--incognito", url};
        //     Utils.runNoOutput(commandParts);
        boolean done = false;
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
                System.out.println("Press ENTER.");
                Match topLeftCornerMatch = s.find("images/top_left_corner.png");
                System.out.format("Top left corner at H: %s W: %d X: %d Y: %d\n", topLeftCornerMatch.h, topLeftCornerMatch.w, topLeftCornerMatch.x, topLeftCornerMatch.y);
                Match bottomRightCornerMatch = s.find("images/bottom_right_corner.png");
                System.out.format("Bottom right corner at H: %s W: %d X: %d Y: %d\n", bottomRightCornerMatch.h, bottomRightCornerMatch.w, bottomRightCornerMatch.x, bottomRightCornerMatch.y);
                Region region = new Region(topLeftCornerMatch.x, topLeftCornerMatch.y, (bottomRightCornerMatch.x - topLeftCornerMatch.x) + bottomRightCornerMatch.w, (bottomRightCornerMatch.y - topLeftCornerMatch.y) - bottomRightCornerMatch.h);
                System.out.format("Region  X:%d  Y:%d  W:%d  H:%d.\n", region.x, region.y, region.w, region.h);
                String fileName = region.saveScreenCapture();
                System.out.format("Image captured %s\n", fileName);
                String[] openImageCommandParts = {"xdg-open", fileName};
                Utils.run(openImageCommandParts);
                s.type(Key.ESC);
            }
        }
    }
}
