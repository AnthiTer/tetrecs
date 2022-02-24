package uk.ac.soton.comp1206.utility;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Utility {
    private static final Logger logger = LogManager.getLogger(Utility.class);

    /**
     * Loads scores from scores.txt file.
     * If scores.txt doesn't exists, generates a new one with designated scores.
     */
    public static ArrayList<Pair<String, Integer>> loadScores() {
        logger.info("Loading scores from scores.txt");
        ArrayList<Pair<String, Integer>> result = new ArrayList<>();
        File file = new File("scores.txt");
        try {
            var x = file.createNewFile();
            if (x) {
                for (int i = 0; i < 10; i++) {
                    result.add(new Pair("Anthi", 1000 * (10 - i)));
                }
                writeScores(result);
            } else {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                Scanner scanner = new Scanner(reader);
                while (scanner.hasNext()) {
                    String[] parts = scanner.next().split(":");
                    var y = new Pair<>(parts[0], Integer.parseInt(parts[1]));
                    result.add(y);
                }
                scanner.close();
            }

        } catch (Exception e) {
            logger.error("Unable to read from scores.txt: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Writes the scores in the scores.txt file.
     */
    public static void writeScores(List<Pair<String, Integer>> scores) {
        logger.info("Writing scores to scores.txt");
        scores.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        File file = new File("scores.txt");
        try {
            file.createNewFile();
            FileWriter filewriter = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(filewriter);

            for (Pair<String, Integer> i : scores) {
                String s = i.getKey() + ":" + i.getValue() + "\n";
                bw.write(s);
            }
            bw.close();
            filewriter.close();
        } catch (Exception e) {
            logger.error("Unable to write to scores.txt: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
