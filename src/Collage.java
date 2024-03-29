import classes.Picture;
import classes.Pixel;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Used to generate a collage from a picture.
 */
public class Collage {

    private Color[] palette;
    private File[] images;
    private Picture source;
    private int picScl;
    private int scl;
    private ExecutorService executorService;

    /**
     * @param source    Picture to make the collage from.
     * @param picScl    How large each individual picture in the collage will be.
     * @param scl       Resolution of the collage.
     * @param directory Source directory of the images to be used in the collage.
     */
    public Collage(Picture source, int picScl, int scl, String directory, ExecutorService executorService) {
        this.executorService = executorService;
        this.source = source;
        this.picScl = picScl;
        this.scl = scl;

        File[] files = new File(directory).listFiles();

        int imgCnt = 0;
        for (File file : files) {
            if (file.getPath().endsWith(".jpg"))
                imgCnt++;
        }

        images = new File[imgCnt];

        for (int i = 0, j = 0; i < files.length; i++) {
            if (files[i].getPath().endsWith(".jpg")) {
                images[j] = files[i];
                j++;
            }
        }

        palette = generatePalette();
    }

    /**
     * @param dither Whether to apply Floyd–Steinberg dithering or not.
     */
    public Picture createCollage(boolean dither) {
        Picture scaled = source.scale((double) 1 / scl, (double) 1 / scl);
        Picture collage = new Picture(source.getHeight() / scl * picScl, source.getWidth() / scl * picScl);
        DecimalFormat df = new DecimalFormat("0.000");

        System.out.println("Creating collage...");
        List<CompletableFuture<Void>> copyTasks = new ArrayList<>();
        for (int y = 0; y < scaled.getHeight(); y++) {
            for (int x = 0; x < scaled.getWidth(); x++) {
                Color current = scaled.getPixel(x, y).getColor();

                int index = findPalette(current);
                Color picColor = palette[index];

                if (dither) {
                    distributeError(scaled, picColor, x, y);
                }

                int finalX = x, finalY = y;
                copyTasks.add(CompletableFuture.runAsync(() -> {
                    Picture sclPic = scale(new Picture(images[index].getAbsolutePath()), picScl, picScl, picColor);
                    collage.copy(sclPic, finalY * picScl, finalX * picScl);
                }, executorService));

                System.out.println("Progress: " + df.format((double) (y * scaled.getWidth() + x + 1) * 100 / (scaled.getWidth() * scaled.getHeight())) + "%");
            }
        }

        try {
            System.out.println("Finishing collage...");
            CompletableFuture.allOf(copyTasks.toArray(new CompletableFuture[0])).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Issue with encountered when completing collage.", e);
        }

        return collage;
    }

    public static Picture scale(Picture pic, int width, int height, Color background) {
        AffineTransform scale = new AffineTransform();
        scale.scale((double) width / pic.getWidth(), (double) height / pic.getHeight());

        Picture scaled = new Picture(height, width);
        Graphics2D g = (Graphics2D) scaled.getGraphics();

        g.setBackground(background);
        g.drawImage(pic.getImage(), scale, null);

        return scaled;
    }

    /**
     * Floyd-Steinberg dithering, implemented as described at:
     * <p>
     * https://en.wikipedia.org/wiki/Floyd-Steinberg_dithering
     *
     * @param pic      Picture to be dithered.
     * @param newColor New color.
     * @param x        Current x position.
     * @param y        Current y position.
     */
    private static void distributeError(Picture pic, Color newColor, int x, int y) {
        Pixel current = pic.getPixel(x, y);

        int errorR = current.getRed() - newColor.getRed();
        int errorG = current.getGreen() - newColor.getGreen();
        int errorB = current.getBlue() - newColor.getBlue();

        if (x + 1 < pic.getWidth()) {
            Pixel next = pic.getPixel(x + 1, y);
            next.setColor(new Color(fix(next.getRed() + errorR * 7 / 16),
                fix(next.getGreen() + errorG * 7 / 16),
                fix(next.getBlue() + errorB * 7 / 16)));
        }

        if (x - 1 > 0 && y + 1 < pic.getHeight()) {
            Pixel next = pic.getPixel(x - 1, y + 1);
            next.setColor(new Color(fix(next.getRed() + errorR * 3 / 16),
                fix(next.getGreen() + errorG * 3 / 16),
                fix(next.getBlue() + errorB * 3 / 16)));
        }

        if (y + 1 < pic.getHeight()) {
            Pixel next = pic.getPixel(x, y + 1);
            next.setColor(new Color(fix(next.getRed() + errorR * 5 / 16),
                fix(next.getGreen() + errorG * 5 / 16),
                fix(next.getBlue() + errorB * 5 / 16)));
        }

        if (x + 1 < pic.getWidth() && y + 1 < pic.getHeight()) {
            Pixel next = pic.getPixel(x + 1, y + 1);
            next.setColor(new Color(fix(next.getRed() + errorR / 16),
                fix(next.getGreen() + errorG / 16),
                fix(next.getBlue() + errorB / 16)));
        }
    }

    /**
     * Makes sure a value is within the range 0 to 255 (inclusive).
     *
     * @param val Value.
     * @return The value, now in range.
     */
    private static int fix(int val) {
        if (val < 0) {
            return 0;
        }

        if (val > 255) {
            return 255;
        }

        return val;
    }

    /**
     * Generates pictures to be used in collage, scaled appropriately.
     *
     * @return Pictures to be used in collage.
     */
    private Color[] generatePalette() {
        System.out.println("Generating palette...");
        Color[] palette;

        palette = new Color[images.length];

        List<CompletableFuture<Void>> averageTasks = new ArrayList<>();
        int count = 0;
        for (File picFile : images) {
            System.out.println("Loading file: " + picFile.getName() + " (" + (count + 1) + " of " + images.length + ").");

            int finalCount = count;
            averageTasks.add(CompletableFuture.runAsync(() -> palette[finalCount] = avgColor(new Picture(picFile.getAbsolutePath()), 100), executorService));
            count++;
        }

        try {
            System.out.println("Finishing palette...");
            CompletableFuture.allOf(averageTasks.toArray(new CompletableFuture[0])).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Issue encountered with generating palette.", e);
        }

        return palette;
    }

    /**
     * Finds the closest color in the palette to the inputted color.
     *
     * @param original Color you're trying to approximate.
     * @return Closest color in the palette.
     */
    private int findPalette(Color original) {
        double maxErr = Double.MAX_VALUE;
        int closest = -1;

        int index = 0;
        for (Color color : palette) {
            double err = colorDif(original, color);

            if (err < maxErr) {
                closest = index;
                maxErr = err;
            }

            index++;
        }

        return closest;
    }

    public void setPicScl(int picScl) {
        this.picScl = picScl;
    }

    public void setScl(int scl) {
        this.scl = scl;
    }

    public void setSource(Picture source) {
        this.source = source;
    }

    public void setImages(File[] images) {
        this.images = images;
    }

    /**
     * Finds the average color of a picture.
     *
     * @param pic     Picture to find average color.
     * @param samples How many pixels to sample for average color.
     * @return Average color of the picture.
     */
    public static Color avgColor(Picture pic, int samples) {
        int r = 0;
        int g = 0;
        int b = 0;

        for (int i = 0; i < samples; i++) {
            Pixel pixel = pic.getPixel((int) (pic.getWidth() * Math.random()), (int) (pic.getHeight() * Math.random()));

            r += pixel.getRed();
            g += pixel.getGreen();
            b += pixel.getBlue();
        }

        return new Color(r / samples, g / samples, b / samples);
    }

    /**
     * Finds the difference between two colors.
     * Uses formula described at:
     * <p>
     * https://en.wikipedia.org/wiki/Color_difference
     * <p>
     * under section "Euclidean".
     *
     * @param c1 First color.
     * @param c2 Second color.
     * @return Difference between the colors.
     */
    public static double colorDif(Color c1, Color c2) {
        double r = (double) (c1.getRed() + c2.getRed()) / 2;

        double dR = Math.pow(c1.getRed() - c2.getRed(), 2);
        double dG = Math.pow(c1.getGreen() - c2.getGreen(), 2);
        double dB = Math.pow(c1.getBlue() - c2.getBlue(), 2);

        return Math.sqrt(2 * dR + 4 * dG + 3 * dB + r * (dR - dB) / 256);
    }

}