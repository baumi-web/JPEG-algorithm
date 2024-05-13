package JPEG_Verfahren;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;

public class ImgReader {

    private static final int WIDTH = 512;
    private static final int HEIGHT = 512;
    private static final int BLOCK_SIZE = 8;

    private static final int[][] Q50 = {
        {16, 11, 10, 16, 24, 40, 51, 61},
        {12, 12, 14, 19, 26, 58, 60, 55},
        {14, 13, 16, 24, 40, 57, 69, 56},
        {14, 17, 22, 29, 51, 87, 80, 62},
        {18, 22, 37, 56, 68, 109, 103, 77},
        {24, 35, 55, 64, 81, 104, 113, 92},
        {49, 64, 78, 87, 103, 121, 120, 101},
        {72, 92, 95, 98, 112, 100, 103, 99}
    };

    public static void main(String[] args) {
        String filePath = "/Users/leonbaumgarten/eclipse-workspace/Numerik/src/JPEG_Verfahren/picdat.dat"; // Update this path
        try {
            byte[] imageData = readImageFile(filePath);
            BufferedImage originalImage = createBufferedImage(imageData);
            displayImage(originalImage, "Original Image");
            
            int[][][] blocks = splitIntoBlocks(imageData, WIDTH, HEIGHT, BLOCK_SIZE);
            applyJPEGAlgorithm(blocks);
            byte[] reconstructedData = reconstructImage(blocks, WIDTH, HEIGHT, BLOCK_SIZE);
            BufferedImage reconstructedImage = createBufferedImage(reconstructedData);
            displayImage(reconstructedImage, "Reconstructed Image");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] readImageFile(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        BufferedInputStream bis = new BufferedInputStream(fis);
        byte[] imageData = new byte[WIDTH * HEIGHT];
        bis.read(imageData);
        bis.close();
        fis.close();
        return imageData;
    }

    private static BufferedImage createBufferedImage(byte[] imageData) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        int index = 0;
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int pixelValue = imageData[index++] & 0xFF;
                int rgb = (pixelValue << 16) | (pixelValue << 8) | pixelValue;
                image.setRGB(x, y, rgb);
            }
        }
        return image;
    }

    private static int[][][] splitIntoBlocks(byte[] imageData, int width, int height, int blockSize) {
        int[][][] blocks = new int[width / blockSize][height / blockSize][blockSize * blockSize];
        for (int blockY = 0; blockY < height / blockSize; blockY++) {
            for (int blockX = 0; blockX < width / blockSize; blockX++) {
                for (int y = 0; y < blockSize; y++) {
                    for (int x = 0; x < blockSize; x++) {
                        int pixelIndex = ((blockY * blockSize + y) * width) + (blockX * blockSize + x);
                        blocks[blockX][blockY][(y * blockSize) + x] = imageData[pixelIndex] & 0xFF;
                    }
                }
            }
        }
        return blocks;
    }

    private static void applyJPEGAlgorithm(int[][][] blocks) {
        for (int blockY = 0; blockY < blocks.length; blockY++) {
            for (int blockX = 0; blockX < blocks[0].length; blockX++) {
                int[] block = blocks[blockX][blockY];
                block = subtract128(block);
                block = applyDCT(block);
                block = quantize(block, Q50);
                block = dequantize(block, Q50);
                block = applyInverseDCT(block);
                block = add128(block);
                blocks[blockX][blockY] = block;
            }
        }
    }

    private static int[] subtract128(int[] block) {
        for (int i = 0; i < block.length; i++) {
            block[i] -= 128;
        }
        return block;
    }

    private static int[] add128(int[] block) {
        for (int i = 0; i < block.length; i++) {
            block[i] += 128;
        }
        return block;
    }

    private static int[] applyDCT(int[] block) {
        // Implement the Discrete Cosine Transform (DCT)
        // This is a placeholder and needs the actual DCT algorithm
        return block;
    }

    private static int[] applyInverseDCT(int[] block) {
        // Implement the Inverse Discrete Cosine Transform (IDCT)
        // This is a placeholder and needs the actual IDCT algorithm
        return block;
    }

    private static int[] quantize(int[] block, int[][] Q) {
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                block[i * BLOCK_SIZE + j] /= Q[i][j];
            }
        }
        return block;
    }

    private static int[] dequantize(int[] block, int[][] Q) {
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                block[i * BLOCK_SIZE + j] *= Q[i][j];
            }
        }
        return block;
    }

    private static byte[] reconstructImage(int[][][] blocks, int width, int height, int blockSize) {
        byte[] reconstructedData = new byte[width * height];
        for (int blockY = 0; blockY < height / blockSize; blockY++) {
            for (int blockX = 0; blockX < width / blockSize; blockX++) {
                int[] block = blocks[blockX][blockY];
                for (int y = 0; y < blockSize; y++) {
                    for (int x = 0; x < blockSize; x++) {
                        int pixelIndex = ((blockY * blockSize + y) * width) + (blockX * blockSize + x);
                        reconstructedData[pixelIndex] = (byte) block[(y * blockSize) + x];
                    }
                }
            }
        }
        return reconstructedData;
    }

    private static void displayImage(BufferedImage img, String title) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(title);
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
