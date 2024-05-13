package JPEG_Verfahren;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;


public class GrayScaleImageBuilder {

    private static final int WIDTH = 512;
    private static final int HEIGHT = 512;

    public static void main(String[] args) {
        String filePath = "/Users/leonbaumgarten/eclipse-workspace/Numerik/src/JPEG_Verfahren/picdat.dat";  // Pfad zur Datei anpassen
        try {
        	System.out.println("Bild wurde erfolgreich erstellt.");
            byte[] imageData = readImageData(filePath);
            BufferedImage image = createRGBImageFromGrayScale(imageData);
            displayImage(image, "Graustufenbild");
        } catch (IOException e) {
            System.err.println("Ein Fehler ist aufgetreten: " + e.getMessage());
        }
    }

    private static byte[] readImageData(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        byte[] imageData = new byte[WIDTH * HEIGHT];
        String line;
        int index = 0;

        while ((line = reader.readLine()) != null && index < imageData.length) {
            int grayValue = Integer.parseInt(line.trim());
            imageData[index++] = (byte) grayValue;
        }

        reader.close();
        if (index != WIDTH * HEIGHT) {
            throw new IOException("Die Datei hat nicht die erwartete Anzahl von Pixeln.");
        }

        return imageData;
    }

    private static BufferedImage createRGBImageFromGrayScale(byte[] grayData) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        int index = 0;
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int px = grayData[index++] & 0xFF;
                int rgb = (px << 16) | (px << 8) | px;
                image.setRGB(x, y, rgb);
            }
        }
        return image;
    }

    private static void displayImage(BufferedImage img, String title) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(title);
            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(img, 0, 0, this);
                }
            };
            panel.setPreferredSize(new java.awt.Dimension(img.getWidth(), img.getHeight()));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
