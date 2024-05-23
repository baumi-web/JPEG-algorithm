package JPEG_Verfahren;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;

public class GrayScale10 {
	//Bildgröße
    private static final int WIDTH = 512;
    private static final int HEIGHT = 512;
    private static final int BLOCK_SIZE = 8; // Größe Untermatrizen
    
    private static int totalZeros = 0;
    private static int totalEntries = 0;
    
    private static final int[][] Q10 = {
    	    {80, 60, 50, 80, 120, 200, 255, 255},
    	    {55, 60, 70, 95, 130, 255, 255, 255},
    	    {70, 65, 80, 120, 200, 255, 255, 255},
    	    {70, 85, 110, 145, 255, 255, 255, 255},
    	    {90, 110, 185, 255, 255, 255, 255, 255},
    	    {120, 175, 255, 255, 255, 255, 255, 255},
    	    {245, 255, 255, 255, 255, 255, 255, 255},
    	    {155, 255, 255, 255, 255, 255, 255, 255}
    	};

    	private static final int[][] Q50 = {
    	    {16, 11, 10, 16, 24, 40, 51, 61},
    	    {12, 12, 14, 19, 26, 58, 60, 55},
    	    {14, 13, 16, 24, 40, 57, 69, 46},
    	    {14, 17, 22, 29, 51, 87, 80, 62},
    	    {18, 22, 37, 56, 68, 109, 103, 77},
    	    {24, 35, 55, 64, 81, 104, 113, 92},
    	    {49, 64, 78, 87, 103, 121, 120, 101},
    	    {72, 92, 95, 98, 112, 100, 103, 99}
    	};

    	private static final int[][] Q90 = {
    	    {3, 2, 2, 3, 5, 8, 10, 12},
    	    {2, 2, 3, 4, 5, 12, 12, 11},
    	    {3, 3, 3, 5, 8, 11, 14, 11},
    	    {3, 3, 4, 6, 10, 17, 16, 12},
    	    {4, 4, 7, 11, 14, 22, 21, 15},
    	    {5, 7, 11, 13, 16, 12, 23, 18},
    	    {10, 13, 16, 17, 21, 24, 24, 21},
    	    {14, 18, 19, 20, 22, 20, 20, 20}
    	};


    public static void main(String[] args) {
		String filePath = "./src/JPEG_Verfahren/picdat.dat";
        File file = new File(filePath);
        try {
        	// Auslesen der Bilddaten von picdat.dat
            byte[] imageData = readImageData(filePath);
            BufferedImage image = createRGBImageFromGrayScale(imageData);  // BufferedImage aus den Grauwerten erstellen
            System.out.println("Bild wurde erfolgreich erstellt.");
            displayImage(image, "Graustufenbild");

            // Aufteilen des Bildes in 8x8 Blöcke
            BufferedImage[][] blocks = splitImageIntoBlocks(image);
            System.out.println("Bild wurde in 8x8 Blöcke unterteilt.");
            // Hier können Sie Operationen auf jedem Block durchführen.
            
            double[][] dctMatrix = createDCTMatrix(BLOCK_SIZE);
            
            System.out.println("Anwendung des JPEG-Algorithmus läuft...");

            // Anwenden des JPEG-Algorithmus auf jeden Block
            for (int i = 0; i < blocks.length; i++) {
                for (int j = 0; j < blocks[i].length; j++) {
                    double[][] blockPixels = convertToDoubleArray(blocks[i][j]);

                    // Elementweise Subtraktion von 128
                    subtract128(blockPixels);

                    // Anwendung der DCT
                    double[][] transformedBlock = applyDCT(blockPixels, dctMatrix);

                    // Quantisierung mit der Matrix Q10
                    quantizeBlock(transformedBlock, Q10);

                    //System.out.println("Quantisierte Matrix für Block (" + i + "," + j + "):");
                    //printMatrixQ(transformedBlock);

                    // Rücktransformation starten
                    // Dequantisierung
                    dequantizeBlock(transformedBlock, Q10);


                    // Anwendung der inversen DCT
                    double[][] recoveredBlock = applyIDCT(transformedBlock, dctMatrix);

                    // Elementweise Addition von 128
                    add128(recoveredBlock);
                    blocks[i][j] = convertToBufferedImage(recoveredBlock);
                }
            }
            System.out.println("Anwendung des JPEG-Algorithmus abgeschlossen");
            printZeroPercentage();  // Prozentsatz der Nullen

            reassembleImage(blocks, BLOCK_SIZE);
            System.out.println("Resultierendes Bild wird ausgegeben");

        } catch (IOException e) {
        	// falls Fehler -> Fehlermeldung ausgeben
            System.err.println("Es gibt einen Fehler: " + e.getMessage());
        }
    }

    // Daten aus Datei in byte-aArray speichern
    private static byte[] readImageData(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        byte[] imageData = new byte[WIDTH * HEIGHT];
        String line;
        int index = 0;

        while ((line = reader.readLine()) != null && index < imageData.length) {
            int grayValue = Integer.parseInt(line.trim()); // als Integer formatieren
            imageData[index++] = (byte) grayValue; // Wert im byte-Array speichern
        }

        reader.close();
        if (index != WIDTH * HEIGHT) {
            throw new IOException("Die Datei hat nicht die erwartete Anzahl von Pixeln.");
        }

        return imageData;
    }
    //BufferedImage konvertieren zu RGB
    private static BufferedImage createRGBImageFromGrayScale(byte[] grayData) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        int index = 0;
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int px = grayData[index++] & 0xFF; // Vorzeichen korrigieren
                int rgb = (px << 16) | (px << 8) | px; // Grauwerte in RGB-format umwandeln (weil RGB-BufferedImage)
                image.setRGB(x, y, rgb);
            }
        }
        return image;
    }
    //Aufteilen Bild in 8x8 Blöcke
    private static BufferedImage[][] splitImageIntoBlocks(BufferedImage image) {
        int numBlocksPerDimension = WIDTH / BLOCK_SIZE;
        BufferedImage[][] blocks = new BufferedImage[numBlocksPerDimension][numBlocksPerDimension];
        
        for (int i = 0; i < numBlocksPerDimension; i++) {
            for (int j = 0; j < numBlocksPerDimension; j++) {
            	// 8x8 Blöcke aus Gesamtbild ziehen
                blocks[i][j] = image.getSubimage(j * BLOCK_SIZE, i * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
            }
        }

        return blocks;
    }
    // Berechnung der DCT Matrix
    private static double[][] createDCTMatrix(int size) {
        double[][] dctMatrix = new double[size][size];
        double c = Math.sqrt(2.0 / size);
        double scaleC0 = Math.sqrt(1.0 / size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                dctMatrix[i][j] = (i == 0 ? scaleC0 : c) * Math.cos((2 * j + 1) * i * Math.PI / (2.0 * size));
            }
        }
        return dctMatrix;
    }
    // Anwendung der DCT
    private static double[][] applyDCT(double[][] block, double[][] dctMatrix) {
        int n = dctMatrix.length;
        double[][] transformed = new double[n][n]; //Array für die transformierten Daten
        double sum;

        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                sum = 0.0;
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        sum += dctMatrix[u][i] * block[i][j] * dctMatrix[v][j]; // DCT-Formel
                    }
                }
                transformed[u][v] = sum;
            }
        }
        return transformed;
    }
    
    
    // Konvertieren von ganzzahligen Werten zu double Arrays
    private static double[][] convertToDoubleArray(BufferedImage block) {
        int width = block.getWidth();
        int height = block.getHeight();
        double[][] result = new double[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = block.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF;
                result[y][x] = (double) gray;
            }
        }
        return result;
    }
    // Subtraktion mit 128 
    private static void subtract128(double[][] block) {
        for (int i = 0; i < block.length; i++) {
            for (int j = 0; j < block[i].length; j++) {
                block[i][j] -= 128;
            }
        }
    }
    // Quantisierung mit Matrix Q
    private static void quantizeBlock(double[][] block, int[][] quantMatrix) {
        for (int i = 0; i < block.length; i++) {
            for (int j = 0; j < block[i].length; j++) {
                block[i][j] = Math.round(block[i][j] / quantMatrix[i][j]);
                if (block[i][j] == 0) {
                    totalZeros++;  // Zähle Nullen
                }
            }
        }
        totalEntries += block.length * block[0].length;  // Aktualisiere die Gesamtanzahl der Einträge (für Berechnung Nullanteil)
    }
    
    //Dequantifizierung der Matrizen mit Q
    private static void dequantizeBlock(double[][] block, int[][] quantMatrix) {
        for (int i = 0; i < block.length; i++) {
            for (int j = 0; j < block[i].length; j++) {
                block[i][j] *= quantMatrix[i][j];
            }
        }
    }
    //inverse DCT
    private static double[][] applyIDCT(double[][] block, double[][] dctMatrix) {
        int n = dctMatrix.length;
        double[][] transformed = new double[n][n];
        double sum;

        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                sum = 0.0;
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        sum += dctMatrix[i][u] * block[i][j] * dctMatrix[j][v];
                    }
                }
                transformed[u][v] = sum;
            }
        }
        return transformed;
    }
    // Elementweise Addition mit 128
    private static void add128(double[][] block) {
        for (int i = 0; i < block.length; i++) {
            for (int j = 0; j < block[i].length; j++) {
                block[i][j] += 128;
            }
        }
    }
    // Untermatrizen zu einer Matrix zusammenfügen
    private static void reassembleImage(BufferedImage[][] blocks, int blockSize) {
        BufferedImage fullImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < blocks.length; i++) {
            for (int j = 0; j < blocks[i].length; j++) {
                BufferedImage block = blocks[i][j];
                if (block == null) {
                    throw new IllegalStateException("Block at " + i + ", " + j + " is null");
                }
                int startRow = i * blockSize;
                int startCol = j * blockSize;
                for (int row = 0; row < block.getHeight(); row++) {
                    for (int col = 0; col < block.getWidth(); col++) {
                        int pixel = block.getRGB(col, row);
                        fullImage.setRGB(startCol + col, startRow + row, pixel);
                    }
                }
            }
        }
        displayImage(fullImage, "Zusammengesetztes Bild");
    }
    
    private static BufferedImage convertToBufferedImage(double[][] block) {
        int height = block.length;
        int width = block[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = (int) Math.round(block[y][x]);
                value = Math.max(0, Math.min(255, value));
                int rgb = (value << 16) | (value << 8) | value;
                image.setRGB(x, y, rgb);
            }
        }

        return image;
    }
    // Berechnung Anteil an Nullen
    private static void printZeroPercentage() {
        if (totalEntries > 0) {
            double percentage = (double) totalZeros / totalEntries * 100;
            System.out.println("Prozentsatz der Nullen nach der Quantisierung: " + String.format("%.2f", percentage) + "%");
        }
    }

///////////////////////////
    //Ausgabe der quantifizierten Matrizen (Aufruf in Main auskommentiert)
    private static void printMatrixQ(double[][] matrix) {
        for (double[] row : matrix) {
            for (double value : row) {
                System.out.printf("%9.2f ", value);
            }
            System.out.println();
        }
        System.out.println();
    }
///////////////////////////

    // Anzeige des Bildes
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
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
} 