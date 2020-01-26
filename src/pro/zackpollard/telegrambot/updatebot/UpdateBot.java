package pro.zackpollard.telegrambot.updatebot;

import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Zack Pollard
 */
public class UpdateBot {

    private final TelegramBot telegramBot;

    public UpdateBot() {

        telegramBot = TelegramBot.login(System.getenv("BOT_API_KEY"));
    }

    public void start() {

        String oldVersion = null;
        String cachedVersion = "";

        while (true) {

            String updateContents = "";

            try {

                URL apiUrl = new URL("https://raw.githubusercontent.com/zackpollard/JavaTelegramBot-API/master/CHANGELOG.md");

                URLConnection apiConnection = apiUrl.openConnection();
                apiConnection.setUseCaches(false);
                apiConnection.setConnectTimeout(2000);
                apiConnection.setReadTimeout(2000);
                apiConnection.connect();

                BufferedReader in = new BufferedReader(new InputStreamReader(apiUrl.openStream()));
                String buffer;

                while ((buffer = in.readLine()) != null) {

                    updateContents += buffer + '\n';
                }

                in.close();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    Thread.sleep(10000);
                    continue;
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }

            if (updateContents.isEmpty()) {

                System.err.println("An error occurred whilst retrieving the changelog from GitHub and the contents were empty.");
            } else {

                String newVersion;
                String changelog;

                //Removes first line
                updateContents = updateContents.substring(updateContents.indexOf('\n') + 1);
                //Gets contents of second line
                newVersion = updateContents.substring(updateContents.indexOf(' '), updateContents.indexOf('\n')).trim();
                updateContents = updateContents.substring(updateContents.indexOf('\n') + 1);

                /** Debug code.
                System.out.println(newVersion);
                System.out.println("========================");
                System.out.println(updateContents);
                System.out.println("========================");
                */

                //Gets the latest versions changelog
                changelog = updateContents.substring(updateContents.indexOf('*'), updateContents.indexOf("####")).trim();

                if(!newVersion.equals(cachedVersion)) {

                    if (oldVersion != null && !newVersion.equals(oldVersion)) {

                        //Runs if the oldVersion doesn't match the new version and isn't null.

                        telegramBot.sendMessage(
                                TelegramBot.getChat("@javatelegrambotapi"),
                                SendableTextMessage.builder().disableWebPagePreview(true).message("New API Version Released\n" + newVersion + "\nChangelog:\n" + changelog).build()
                        );

                        System.out.println(newVersion);
                        System.out.println(changelog);
                    } else {

                        System.out.println("No changes found!");
                    }

                    System.out.println(changelog);

                    cachedVersion = oldVersion;
                    oldVersion = newVersion;

                    //Sleep and check again shortly.
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
