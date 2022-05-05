import classes.Picture;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Main {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(16);

        long start = System.currentTimeMillis();

        Picture source = new Picture("src/arch.jpg");
        String path = "src/resources/HousesDataset/";

        Collage collage = new Collage(source, 5, 5, path, executor);
        collage.createCollage(true).explore();

        System.out.println("Time taken: " + (System.currentTimeMillis() - start) / 1000d + " s");
    }

}