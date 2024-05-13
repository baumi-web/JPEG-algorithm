
package JPEG_Verfahren;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;

public class ImageReader {

    public static void main(String[] args) {
        // Dateipfad angeben
        String filePath = "/Users/leonbaumgarten/eclipse-workspace/Numerik/src/JPEG_Verfahren/picdat.dat";

        try {
            // Datei als InputStream öffnen
            FileInputStream fis = new FileInputStream(filePath);
            BufferedInputStream bis = new BufferedInputStream(fis);

            // Bildgröße definieren
            int width = 512;
            int height = 512;

            // Bild erstellen
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            // Bilddaten aus der Datei lesen
            byte[] imageData = new byte[width * height];
            bis.read(imageData);

            // Bilddaten in das BufferedImage einfügen
            int index = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixelValue = imageData[index++] & 0xFF; // Umwandlung in ein vorzeichenloses Byte
                    int rgb = (pixelValue << 16) | (pixelValue << 8) | pixelValue; // Grauwert in RGB umwandeln (R=G=B)
                    image.setRGB(x, y, rgb);
                }
            }

            // Streams schließen
            bis.close();
            fis.close();

            // Bild anzeigen
            displayImage(image);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Methode zur Anzeige des Bildes mit Swing
    private static void displayImage(BufferedImage img) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Bildanzeige");
            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(img, 0, 0, null);
                }
            };

            panel.setPreferredSize(new java.awt.Dimension(img.getWidth(), img.getHeight()));
            frame.getContentPane().add(panel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
