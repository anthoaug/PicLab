public class Main {
	
	public static void main(String[] args) {
        Picture source = new Picture("arch.jpg");
        String path = "resources/HousesDataset";

        Collage collage = new Collage(source, 5, 5, path);
        collage.createCollage(true).explore();
	}
	
}