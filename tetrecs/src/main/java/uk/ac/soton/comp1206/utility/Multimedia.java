package uk.ac.soton.comp1206.utility;

import javafx.scene.image.Image;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
public class Multimedia {
    private static final Logger logger = LogManager.getLogger(Multimedia.class);

    private static boolean audioEnabled = true;

    private static MediaPlayer mediaPlayer;

    private static double mediaVolume = 0.5;

    private static MediaPlayer backgroundPlayer;

    private static double backgroundVolume = 0.4;

    public static void changeMusic(String music) {
        changeMusic(music, false);
    }

    /**
     * Stops all sounds from being played.
     */
    public static void stopAll() {
        if (mediaPlayer != null)
            mediaPlayer.stop();
        if (backgroundPlayer != null)
            backgroundPlayer.stop();
    }

    /**
     * Changes music from background music to an audio.
     */
    public static void changeMusic(String music, boolean resume) {
        if (!audioEnabled)
            return;
        logger.info("Change music to: " + music);
        try {
            String toPlay = Multimedia.class.getResource("/music/" + music).toExternalForm();
            javafx.scene.media.Media play = new javafx.scene.media.Media(toPlay);
            Duration previous = null;
            if (backgroundPlayer != null) {
                previous = backgroundPlayer.getCurrentTime();
                backgroundPlayer.stop();
            } else {
                resume = false;
            }
            backgroundPlayer = new MediaPlayer(play);
            backgroundPlayer.setVolume(backgroundVolume);
            if (resume)
                backgroundPlayer.setStartTime(previous);
            backgroundPlayer.play();
            backgroundPlayer.setOnEndOfMedia(() -> startBackgroundMusic(music));
        } catch (Exception e) {
            audioEnabled = false;
            e.printStackTrace();
            logger.error("Unable to play audio file");
        }
    }

    /**
     * Play background music
     * @param musicIntro
     * @param music
     */
    public static void startBackgroundMusic(String musicIntro, String music) {
        if (!audioEnabled)
            return;
        logger.info("Start background music: " + musicIntro + ", " + music);
        if (backgroundPlayer != null)
            backgroundPlayer.stop();
        try {
            String toPlay = Multimedia.class.getResource("/music/" + musicIntro).toExternalForm();
            javafx.scene.media.Media play = new javafx.scene.media.Media(toPlay);
            backgroundPlayer = new MediaPlayer(play);
            backgroundPlayer.setVolume(backgroundVolume);
            backgroundPlayer.setOnEndOfMedia(() -> startBackgroundMusic(music));
            backgroundPlayer.play();
        } catch (Exception e) {
            audioEnabled = false;
            e.printStackTrace();
            logger.error("Unable to play audio file");
        }
    }

    public static void startBackgroundMusic(String music) {
        startBackgroundMusic(music, true);
    }

    /**
     * Loop through the background music
     * @param music
     * @param loop
     */
    public static void startBackgroundMusic(String music, boolean loop) {
        if (!audioEnabled)
            return;
        logger.info("Start background music: " + music);
        if (backgroundPlayer != null)
            backgroundPlayer.stop();
        try {
            String toPlay = Multimedia.class.getResource("/music/" + music).toExternalForm();
            javafx.scene.media.Media play = new javafx.scene.media.Media(toPlay);
            backgroundPlayer = new MediaPlayer(play);
            backgroundPlayer.setVolume(backgroundVolume);
            if (loop)
                backgroundPlayer.setCycleCount(-1);
            backgroundPlayer.play();
        } catch (Exception e) {
            audioEnabled = false;
            e.printStackTrace();
            logger.error("Unable to play audio file");
        }
    }

    /**
     * Playing audio file
     * @param file
     */
    public static void playAudio(String file) {
        if (!audioEnabled)
            return;
        String toPlay = Multimedia.class.getResource("/sounds/" + file).toExternalForm();
        logger.info("Play audio: " + toPlay);
       try {
            javafx.scene.media.Media play = new javafx.scene.media.Media(toPlay);
            mediaPlayer = new MediaPlayer(play);
           mediaPlayer.setVolume(mediaVolume);
            mediaPlayer.play();
        } catch (Exception e) {
            audioEnabled = false;
            e.printStackTrace();
            logger.error("Unable to play audio file, disabling audio");
        }
    }

    /**
     * Get images from the images folder
     * @param image
     * @return selected image
     */
    public static Image getImage(String image) {
        try {
            Image result = new Image(Multimedia.class.getResource("/images/" + image).toExternalForm());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to load image: {}", image);
            return null;
        }
    }

    /**
     * Use of css stylesheet
     * @param stylesheet
     * @return selected stylesheet
     */
    public static String getStyle(String stylesheet) {
        String css = Multimedia.class.getResource("/style/" + stylesheet).toExternalForm();
        return css;
    }
}
