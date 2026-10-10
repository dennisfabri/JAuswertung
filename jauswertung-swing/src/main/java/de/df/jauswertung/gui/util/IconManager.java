/*
 * Created on 17.02.2004
 */
package de.df.jauswertung.gui.util;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.util.AIconBundle;
import de.df.jutils.swing.icons.Icons;

/**
 * @author Dennis Fabri
 * @date 17.02.2004
 */
public final class IconManager {

    static ResourceBundle names;

    private static AIconBundle icons;

    private static ImageIcon banner;
    private static ImageIcon[] titles;
    private static LinkedList<Image> iconimages;

    static {
        banner = null;
        titles = null;
        iconimages = null;

        names = ResourceBundle.getBundle("icons");
        icons = new ManagerIconBundle();

        // Every application loads its icons through this class before building forms, so jutils components
        // (e.g. JWarningTextField) get our icons as well
        Icons.setIconProvider(new JutilsIconProvider());
    }

    private IconManager() {
        // Never called
    }

    private static synchronized ImageIcon getIcon(String name, boolean small) {
        try {
            synchronized (icons) {
                int size = (small ? 16 : 32);
                return icons.getIcon(name, size);
            }
        } catch (RuntimeException re) {
            re.printStackTrace();
            return null;
        }
    }

    public static synchronized ImageIcon getBanner() {
        try {
            synchronized (icons) {
                if (banner != null) {
                    return banner;
                }
                banner = new ImageIcon(getBannerImage());
                return banner;
            }
        } catch (RuntimeException re) {
            re.printStackTrace();
            return null;
        }
    }

    public static BufferedImage getBannerImage() {
        try {
            return ImageIO.read(new File(Utils.getUserDir() + names.getString("banner")));
        } catch (IOException e) {
            System.err.println(Utils.getUserDir() + names.getString("banner"));
            e.printStackTrace();
            return null;
        }
    }

    private static synchronized ImageIcon[] getTitleIcons() {
        if (titles != null) {
            return titles;
        }
        titles = new ImageIcon[] { getImageIcon("jauswertung-16"), getImageIcon("jauswertung-32"),
                getImageIcon("jauswertung-48"),
                getImageIcon("jauswertung-128"), getImageIcon("jauswertung-256") };
        return titles;
    }

    public static synchronized ImageIcon getImageIcon(String name) {
        try {
            synchronized (icons) {
                return new ImageIcon(Toolkit.getDefaultToolkit().getImage(Utils.getUserDir() + names.getString(name)));
            }
        } catch (RuntimeException re) {
            re.printStackTrace();
            return null;
        }
    }

    public static synchronized List<Image> getTitleImages() {
        if (iconimages == null) {
            ImageIcon[] iconx = getTitleIcons();
            if (iconx == null) {
                return null;
            }
            iconimages = new LinkedList<>();
            for (ImageIcon icon : iconx) {
                iconimages.addLast(icon.getImage());
            }
        }
        return iconimages;
    }

    public static synchronized Image getImage(String name) {
        ImageIcon icon = getImageIcon(name);
        if (icon == null) {
            return null;
        }
        return icon.getImage();
    }

    public static synchronized ImageIcon getSmallIcon(String name) {
        return getIcon(name, true);
    }

    public static synchronized ImageIcon getBigIcon(String name) {
        return getIcon(name, false);
    }

    public static synchronized ImageIcon getGrayIcon(String name, boolean small) {
        return toGrayIcon(getIcon(name, small));
    }

    public static synchronized ImageIcon toGrayIcon(ImageIcon icon) {
        if (icon.getImage() instanceof MultiResolutionImage image) {
            // Keep the larger variants for screens with display scaling
            Image[] grays = image.getResolutionVariants().stream()
                    .map(variant -> toGray(variant, variant.getWidth(null), variant.getHeight(null)))
                    .toArray(Image[]::new);
            return new ImageIcon(new BaseMultiResolutionImage(grays));
        }
        return new ImageIcon(toGray(icon.getImage(), icon.getIconWidth(), icon.getIconHeight()));
    }

    private static BufferedImage toGray(Image image, int width, int height) {
        BufferedImage i = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        i.getGraphics().drawImage(image, 0, 0, null);
        return i;
    }

    public static AIconBundle getIconBundle() {
        return icons;
    }

    static final class ManagerIconBundle extends AIconBundle {

        @Override
        protected ImageIcon readIcon(String name, int size) {
            String id = names.getString(name);
            if (id == null) {
                throw new NullPointerException();
            }
            id = themed(id, size);
            Path filename = Path.of("images", "" + size + "x" + size, id);
            if (!Files.exists(filename)) {
                return new ImageIcon(filename.toString());
            }
            // Larger variants (2x, 4x) are used on screens with display scaling, e.g. 32 px for a 16 px icon at 200 %
            List<Image> variants = new ArrayList<>();
            for (int s = size; s <= 4 * size; s *= 2) {
                BufferedImage image = readImage(Path.of("images", "" + s + "x" + s, id));
                if (image != null) {
                    variants.add(image);
                }
            }
            if (variants.size() <= 1) {
                return new ImageIcon(filename.toString());
            }
            return new ImageIcon(new BaseMultiResolutionImage(variants.toArray(new Image[0])));
        }

        /**
         * With a dark look and feel, use the variant from the folder with the suffix "-dark" (e.g.
         * lucide-dark/save.png instead of lucide/save.png), if it exists.
         */
        private static String themed(String id, int size) {
            int slash = id.indexOf('/');
            if (slash < 0 || !isDarkTheme()) {
                return id;
            }
            String dark = id.substring(0, slash) + "-dark" + id.substring(slash);
            return Files.exists(Path.of("images", "" + size + "x" + size, dark)) ? dark : id;
        }

        private static boolean isDarkTheme() {
            Color background = UIManager.getColor("Panel.background");
            if (background == null) {
                return false;
            }
            // Perceived brightness (ITU-R BT.601)
            double brightness = (0.299 * background.getRed() + 0.587 * background.getGreen()
                    + 0.114 * background.getBlue()) / 255;
            return brightness < 0.5;
        }

        private static BufferedImage readImage(Path path) {
            if (!Files.exists(path)) {
                return null;
            }
            try {
                return ImageIO.read(path.toFile());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}