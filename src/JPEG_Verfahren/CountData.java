package JPEG_Verfahren;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CountData {
    public static void main(String[] args) {
        String filePath = "/Users/leonbaumgarten/eclipse-workspace/Numerik/src/JPEG_Verfahren/picdat.dat"; // Der Dateipfad könnte je nach Standort variieren

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            int count = 0;
            while (reader.readLine() != null) {
                count++;
            }

            System.out.println("Anzahl der Datensätze: " + count);
        } catch (IOException e) {
            System.err.println("Fehler beim Lesen der Datei: " + e.getMessage());
        }
    }
}

