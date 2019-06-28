import classes.Picture;

public class Main {
	
	public static void main(String[] args) {
        Picture source = new Picture("src/arch.jpg");
        String path = "src/resources/HousesDataset/";

        Collage collage = new Collage(source, 5, 5, path);
        collage.createCollage(true).explore();
	}
	
}