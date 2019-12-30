package de.df.jauswertung.util;

import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JLabel;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import de.df.jutils.gui.JIcon;
import net.sourceforge.barbecue.BarcodeException;
import net.sourceforge.barbecue.BarcodeFactory;

public class BarcodeUtils {

    public static final byte VALUE_OK     = 1;
    public static final byte VALUE_NOT_OK = 0;
    public static final byte VALUE_DNS    = 3;

    public enum ZWResultType {
        OK, NOT_OK, DNS
    }

    public static class ZWResult {

        public final int          sn;
        public final int          offset;
        public final ZWResultType ok;

        public ZWResult(int sn, int offset, ZWResultType ok) {
            this.sn = sn;
            this.offset = offset;
            this.ok = ok;
        }
    }

    public static String toCode(int sn, int offset, ZWResultType ok) {
        if (offset > 9) {
            throw new IllegalArgumentException("Offset must not be bigger than 9 but was " + offset + ".");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must not be lower than 0 but was " + offset + ".");
        }
        if (sn < 0) {
            throw new IllegalArgumentException("Startnumber must not be lower than 0 but was " + sn + ".");
        }
        StringBuffer sb = new StringBuffer();
        sb.append(sn);
        sb.append(offset);
        switch (ok) {
        case OK:
            sb.append(VALUE_OK);
            break;
        case NOT_OK:
            sb.append(VALUE_NOT_OK);
            break;
        case DNS:
            sb.append(VALUE_DNS);
            break;
        }

        sb.append(calculateChecksum(Integer.parseInt(sb.toString())));

        if (sb.length() % 2 != 0) {
            sb.insert(0, '0');
        }

        return sb.toString();
    }

    public static ZWResult fromZWCode(String code) {
        try {
            if (!checkZWCode(code)) {
                return null;
            }
            code = code.trim();
            String ssn = code.substring(0, code.length() - 3);
            String sof = code.substring(code.length() - 3, code.length() - 2);
            String sok = code.substring(code.length() - 2, code.length() - 1);

            int sn = Integer.parseInt(ssn);
            int offset = Integer.parseInt(sof);
            int ok = Integer.parseInt(sok);
            ZWResultType type;
            switch (ok) {
            default:
                return null;
            case VALUE_DNS:
                type = ZWResultType.DNS;
                break;
            case VALUE_OK:
                type = ZWResultType.OK;
                break;
            case VALUE_NOT_OK:
                type = ZWResultType.NOT_OK;
                break;
            }

            return new ZWResult(sn, offset, type);
        } catch (RuntimeException re) {
            return null;
        }
    }

    private static int calculateChecksum(int i) {
        int cs = 0;
        int index = 0;
        while (i > 0) {
            int ziffer = i % 10;
            i = i / 10;
            if (index % 2 == 0) {
                cs += ziffer * 2;
            } else {
                cs += ziffer;
            }
            index++;
        }
        cs = cs % 10;
        return 9 - cs;
    }

    public static boolean checkZWCode(String code) {
        try {
            code = code.trim();
            if (code.length() <= 1) {
                return false;
            }
            String v = code.substring(0, code.length() - 1);
            String c = code.substring(code.length() - 1);
            int value = Integer.valueOf(v);
            int cs = Integer.valueOf(c);
            return (cs == calculateChecksum(value));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean checkZWCode(int code) {
        if (code < 10) {
            return false;
        }
        int value = code / 10;
        int cs = code % 10;
        return (cs == calculateChecksum(value));
    }

    public static JComponent getBarcode(String key, BarcodeType type) {
        if ((key == null) || (key.trim().length() == 0)) {
            return null;
        }

        try {
            switch (type) {
            case NONE: {
                return null;
            }
            case CODE128: {
                return BarcodeFactory.createCode128(key);
            }
            case EAN13: {
                while (key.length() < 12) {
                    key = "0" + key;
                }
                return BarcodeFactory.createEAN13(key);
            }
            }
        } catch (BarcodeException e) {
            e.printStackTrace();
        }
        return new JLabel(key);
    }

    public static JComponent getBarcode(String key) {
        if ((key == null) || (key.trim().length() == 0)) {
            return null;
        }

        try {
            return BarcodeFactory.createCode128(key);
        } catch (BarcodeException e) {
            e.printStackTrace();
            return new JLabel(key);
        }
    }

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
            BufferedImage i = MatrixToImageWriter.toBufferedImage(bitMatrix);
            return cropImage2(i);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
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

        BufferedImage destination = new BufferedImage((bottomX - topX + 1), (bottomY - topY + 1), BufferedImage.TYPE_INT_ARGB);

        destination.getGraphics().drawImage(source, 0, 0, destination.getWidth(), destination.getHeight(), topX, topY, bottomX, bottomY, null);

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

        double distance = Math.sqrt((aAlpha - bAlpha) * (aAlpha - bAlpha) + (aRed - bRed) * (aRed - bRed) + (aGreen - bGreen) * (aGreen - bGreen)
                + (aBlue - bBlue) * (aBlue - bBlue));

        // 510.0 is the maximum distance between two colors
        // (0,0,0,0 -> 255,255,255,255)
        double percentAway = distance / 510.0d;

        return (percentAway > tolerance);
    }
}