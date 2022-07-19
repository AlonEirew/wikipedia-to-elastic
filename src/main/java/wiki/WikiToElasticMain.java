/**
 * @author  Alon Eirew
 */

package wiki;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wiki.data.relations.ExtractorsManager;
import wiki.config.LangConfiguration;
import wiki.config.MainConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class WikiToElasticMain {

    private final static Logger LOGGER = LogManager.getLogger(WikiToElasticMain.class);
    private final static Gson GSON = new Gson();

    public static void main(String[] args) {
        try {
            LOGGER.info("Initiating all resources...");
            MainConfiguration config = GSON.fromJson(new FileReader("conf.json"), MainConfiguration.class);
            InputStream inputStream = Objects.requireNonNull(WikiToElasticMain.class.getClassLoader().getResourceAsStream("lang/" + config.getLang() + ".json"));
            LangConfiguration langConfiguration = GSON.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), LangConfiguration.class);

            if (config.getWikipediaDump() != null && !config.getWikipediaDump().isEmpty()) {
                MainProcess process = new MainProcess(config, langConfiguration);
                ExtractorsManager.initExtractors(config, langConfiguration);
                LOGGER.info("Process configuration loaded");

                long startTime = System.currentTimeMillis();
                process.startProcess();
                long endTime = System.currentTimeMillis();

                long durationInMillis = endTime - startTime;
                long took = TimeUnit.MILLISECONDS.toMinutes(durationInMillis);
                LOGGER.info("Process Done, took~" + took + "min (" + durationInMillis + "ms)");
            } else {
                LOGGER.error("Wikipedia dump file not set in configuration");
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Failed to start process", e);
        } catch (IOException e) {
            LOGGER.error("I/O Error", e);
        } catch (Exception e) {
            LOGGER.error("Something went wrong..", e);
        }
    }
}
