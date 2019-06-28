package classes;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

/**
 * A class that represents a picture.  This class inherits from
 * SimplePicture and allows the student to add functionality to
 * the Picture class.
 *
 * @author Barbara Ericson ericson@cc.gatech.edu
 */
public class Picture extends SimplePicture {
    ///////////////////// constructors //////////////////////////////////

    /**
     * Constructor that takes no arguments
     */
    public Picture() {
        /* not needed but use it to show students the implicit call to super()
         * child constructors always call a parent constructor
         */
        super();
    }

    /**
     * Constructor that takes a file name and creates the picture
     *
     * @param fileName the name of the file to create the picture from
     */
    public Picture(String fileName) {
        // let the parent class handle this fileName
        super(fileName);
    }

    /**
     * Constructor that takes the width and height
     *
     * @param height the height of the desired picture
     * @param width  the width of the desired picture
     */
    public Picture(int height, int width) {
        // let the parent class handle this width and height
        super(width, height);
    }

    /**
     * Constructor that takes a picture and creates a
     * copy of that picture
     *
     * @param copyPicture the picture to copy
     */
    public Picture(Picture copyPicture) {
        // let the parent class do the copy
        super(copyPicture);
    }

    /**
     * Constructor that takes a buffered image
     *
     * @param image the buffered image to use
     */
    public Picture(BufferedImage image) {
        super(image);
    }

    ////////////////////// methods ///////////////////////////////////////

    /**
     * Method to return a string with information about this picture.
     *
     * @return a string with information about the picture such as fileName,
     * height and width.
     */
    public String toString() {
        String output = "Picture, filename " + getFileName() +
            " height " + getHeight()
            + " width " + getWidth();
        return output;

    }

    /**
     * Method to set the blue to 0
     */
    public void zeroBlue() {
        Pixel[][] pixels = this.getPixels2D();
        for (Pixel[] rowArray : pixels) {
            for (Pixel pixelObj : rowArray) {
                pixelObj.setBlue(0);
            }
        }
    }

    /**
     * Mirror just part of a picture of a temple
     */
    public void mirrorTemple() {
        int mirrorPoint = 276;
        Pixel leftPixel = null;
        Pixel rightPixel = null;
        int count = 0;
        Pixel[][] pixels = this.getPixels2D();

        // loop through the rows
        for (int row = 27; row < 97; row++) {
            // loop from 13 to just before the mirror point
            for (int col = 13; col < mirrorPoint; col++) {

                leftPixel = pixels[row][col];
                rightPixel = pixels[row]
                    [mirrorPoint - col + mirrorPoint];
                rightPixel.setColor(leftPixel.getColor());
            }
        }
    }

    /**
     * copy from the passed fromPic to the
     * specified startRow and startCol in the
     * current picture
     *
     * @param fromPic  the picture to copy from
     * @param startRow the start row to copy to
     * @param startCol the start col to copy to
     */
    public void copy(Picture fromPic,
                     int startRow, int startCol) {
        Pixel fromPixel = null;
        Pixel toPixel = null;
        Pixel[][] toPixels = this.getPixels2D();
        Pixel[][] fromPixels = fromPic.getPixels2D();
        for (int fromRow = 0, toRow = startRow;
             fromRow < fromPixels.length &&
                 toRow < toPixels.length;
             fromRow++, toRow++) {
            for (int fromCol = 0, toCol = startCol;
                 fromCol < fromPixels[0].length &&
                     toCol < toPixels[0].length;
                 fromCol++, toCol++) {
                fromPixel = fromPixels[fromRow][fromCol];
                toPixel = toPixels[toRow][toCol];
                toPixel.setColor(fromPixel.getColor());
            }
        }
    }

    /**
     * Method to show large changes in color
     *
     * @param edgeDist the distance for finding edges
     */
    public void edgeDetection(int edgeDist) {
        Pixel leftPixel = null;
        Pixel rightPixel = null;
        Pixel[][] pixels = this.getPixels2D();
        Color rightColor = null;
        for (int row = 0; row < pixels.length; row++) {
            for (int col = 0;
                 col < pixels[0].length - 1; col++) {
                leftPixel = pixels[row][col];
                rightPixel = pixels[row][col + 1];
                rightColor = rightPixel.getColor();
                if (leftPixel.colorDistance(rightColor) >
                    edgeDist)
                    leftPixel.setColor(Color.BLACK);
                else
                    leftPixel.setColor(Color.WHITE);
            }
        }
    }

    public void mirrorDiagonal() {
        /*int height = getHeight(), width = getWidth();
        Pixel[][] pixels = getPixels2D();

        int xStart = 0, yStart = -1;
        while (xStart < width - 1 && yStart < height) {
            if (yStart < height - 1) {
                yStart++;
            } else {
                xStart++;
            }

            int x = xStart;
            int y = yStart;

            int xLength = width - x  + 1;
            int yLength = y + 1;
            int length = (xLength > yLength) ? yLength : xLength;
            int offset = length - 1;
            while (y >= 0 && x < width) {
                Pixel before = pixels[y][x];

                if (offset < 0 || y - offset < 0 || x + offset >= width) {
                    break;
                }

                System.out.println(offset);
                Pixel after = pixels[y - offset][x + offset];

                before.setColor(after.getColor());

                y--;
                x++;
                offset -= 2;
            }
        }*/
    }

    /**
     * Method that mirrors the picture around a
     * vertical mirror in the center of the picture
     * from left to right
     */
    public void mirrorVertical() {
        Pixel[][] pixels = this.getPixels2D();
        Pixel leftPixel = null;
        Pixel rightPixel = null;
        int width = pixels[0].length;
        for (int row = 0; row < pixels.length; row++) {
            for (int col = 0; col < width / 2; col++) {
                leftPixel = pixels[row][col];
                rightPixel = pixels[row][width - 1 - col];
                rightPixel.setColor(leftPixel.getColor());
            }
        }
    }

    /**
     * Method that mirrors the picture around a
     * vertical mirror in the center of the picture
     * from right to left
     */
    public void mirrorVertical2() {
        Pixel[][] pixels = this.getPixels2D();
        Pixel leftPixel = null;
        Pixel rightPixel = null;
        int width = pixels[0].length;
        for (int row = 0; row < pixels.length; row++) {
            for (int col = 0; col < width / 2; col++) {
                leftPixel = pixels[row][col];
                rightPixel = pixels[row][width - 1 - col];
                leftPixel.setColor(rightPixel.getColor());
            }
        }
    }

    /**
     * Method that mirrors the picture around a
     * vertical mirror in the center of the picture
     * from top to bottom
     */
    public void mirrorHorizontal() {
        int height = getHeight(), width = getWidth();
        Pixel[][] pixels = this.getPixels2D();


        Pixel topPixel = null;
        Pixel bottomPixel = null;

        for (int row = 0; row < height / 2; row++) {
            for (int col = 0; col < width; col++) {
                topPixel = pixels[row][col];
                bottomPixel = pixels[height - row - 1][col];

                bottomPixel.setColor(topPixel.getColor());
            }
        }
    }

    /**
     * Method that mirrors the picture around a
     * vertical mirror in the center of the picture
     * from bottom to top
     */
    public void mirrorHorizontal2() {
        int height = getHeight(), width = getWidth();
        Pixel[][] pixels = this.getPixels2D();


        Pixel topPixel = null;
        Pixel bottomPixel = null;

        for (int row = 0; row < height / 2; row++) {
            for (int col = 0; col < width; col++) {
                topPixel = pixels[row][col];
                bottomPixel = pixels[height - row - 1][col];

                topPixel.setColor(bottomPixel.getColor());
            }
        }
    }

    public void negate() {
        for (Pixel p : getPixels()) {
            p.setRed(255 - p.getRed());
            p.setBlue(255 - p.getBlue());
            p.setGreen(255 - p.getGreen());
        }
    }

    public void grayscale() {
        for (Pixel p : getPixels()) {
            int color = (p.getRed() + p.getBlue() + p.getGreen()) / 3;

            p.setRed(color);
            p.setBlue(color);
            p.setGreen(color);
        }
    }

} // this } is the end of class Picture, put all new methods before this
