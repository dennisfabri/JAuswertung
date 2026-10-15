package de.df.jauswertung.print.util;

import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import de.df.jutils.gui.JIcon;

public class BarcodeUtils {

    public static JComponent getQRCode(String key) {
        if ((key == null) || (key.trim().length() == 0)) {
            return null;
        }
        return new JIcon(getQRCodeImage(key, 300), true);
    }

    public static Image getQRCodeImage(String key, int size) {
        if ((key == null) || (key.trim().length() == 0)) {
            return null;
        }

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = writer.encode(key, BarcodeFormat.QR_CODE, size, size);
            BufferedImage i = toBufferedImage(bitMatrix);
            return cropImage2(i);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        int onColor = 0xFF000000;
        int offColor = 0xFFFFFFFF;
        int[] rowPixels = new int[width];
        BitArray row = new BitArray(width);
        for (int y = 0; y < height; y++) {
            row = matrix.getRow(y, row);
            for (int x = 0; x < width; x++) {
                rowPixels[x] = row.get(x) ? onColor : offColor;
            }
            image.setRGB(0, y, width, 1, rowPixels, 0, width);
        }
        return image;
    }

    private static BufferedImage cropImage2(BufferedImage source) {
        return cropImage2(source, 0.01);
    }

    private static BufferedImage cropImage2(BufferedImage source, double tolerance) {
        // Get our top-left pixel color as our "baseline" for cropping
        int baseColor = source.getRGB(0, 0);

        int width = source.getWidth();
        int height = source.getHeight();

        int topY = Integer.MAX_VALUE, topX = Integer.MAX_VALUE;
        int bottomY = -1, bottomX = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (colorWithinTolerance(baseColor, source.getRGB(x, y), tolerance)) {
                    if (x < topX)
                        topX = x;
                    if (y < topY)
                        topY = y;
                    if (x > bottomX)
                        bottomX = x;
                    if (y > bottomY)
                        bottomY = y;
                }
            }
        }

        BufferedImage destination = new BufferedImage((bottomX - topX + 1), (bottomY - topY + 1),
                BufferedImage.TYPE_INT_ARGB);

        destination.getGraphics().drawImage(source, 0, 0, destination.getWidth(), destination.getHeight(), topX, topY,
                bottomX, bottomY, null);

        return destination;
    }

    private static boolean colorWithinTolerance(int a, int b, double tolerance) {
        int aAlpha = ((a & 0xFF000000) >>> 24); // Alpha level
        int aRed = ((a & 0x00FF0000) >>> 16); // Red level
        int aGreen = ((a & 0x0000FF00) >>> 8); // Green level
        int aBlue = (a & 0x000000FF); // Blue level

        int bAlpha = ((b & 0xFF000000) >>> 24); // Alpha level
        int bRed = ((b & 0x00FF0000) >>> 16); // Red level
        int bGreen = ((b & 0x0000FF00) >>> 8); // Green level
        int bBlue = (b & 0x000000FF); // Blue level

        double distance = Math.sqrt((aAlpha - bAlpha) * (aAlpha - bAlpha) + (aRed - bRed) * (aRed - bRed)
                + (aGreen - bGreen) * (aGreen - bGreen) + (aBlue - bBlue) * (aBlue - bBlue));

        // 510.0 is the maximum distance between two colors
        // (0,0,0,0 -> 255,255,255,255)
        double percentAway = distance / 510.0d;

        return (percentAway > tolerance);
    }
}