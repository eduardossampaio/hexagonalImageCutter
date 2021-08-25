import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.plaf.ColorUIResource;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Point;

public class ImageCutter {

    static Color pixelToRemove = Color.CYAN;
    static Color pixelToBeReplaced = Color.MAGENTA;
    public static void main(String[] args) throws IOException {

        System.out.println("args length: " + args.length);
        if (args.length >= 2) {
            System.out.println("cutting image");
            String imageFile = args[0];
            String partsCountArg = args[1];

            String cropType = args.length >= 3 ? args[2] : null;

            boolean isHex = cropType != null && cropType.equals("hex");
            boolean isGrid = cropType != null && cropType.equals("grid");

            boolean isSquareCut = !isHex && !isGrid;

            int partsCount = (int) Double.parseDouble(partsCountArg);

            Path outputDir = Paths.get("output");
            if (Files.exists(outputDir)) {
                Files.walk(outputDir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }
            Set<PosixFilePermission> perms = EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_WRITE);
            Files.createDirectory(outputDir, PosixFilePermissions.asFileAttribute(perms));

            if (isSquareCut) {
                System.out.println("cutting square");
                squareCut(imageFile, partsCount);
            } else if (isGrid) {
                System.out.println("cutting hexagonal");
                hexagonalGrid(imageFile, partsCount);
            } else {
                System.out.println("cutting hex");
                hexagonalCut(imageFile, partsCount);
                hexagonalGrid(imageFile, partsCount);
            }

        } else {
            System.out.println("Wrong args");
        }
    }

    private static void squareCut(String imageFile, int partsCount) {
        try {

            BufferedImage originalImgage = ImageIO.read(new File(imageFile));
            int squareSize = originalImgage.getWidth() / (int) partsCount;

            System.out.println(
                    "Original Image Dimension: " + originalImgage.getWidth() + "x" + originalImgage.getHeight());
            System.out.println("New Image Dimension: " + squareSize + "x" + squareSize);

            for (int i = 0; i < originalImgage.getWidth(); i += squareSize) {

                for (int j = 0; j < originalImgage.getHeight(); j += squareSize) {

                    int destinationWith = (int) squareSize;
                    int destinationHeight = (int) squareSize;

                    BufferedImage SubImgage = originalImgage.getSubimage(i, j, destinationWith, destinationHeight);
                    System.out
                            .println("Cropped Image Dimension: " + SubImgage.getWidth() + "x" + SubImgage.getHeight());

                    File outputfile = new File("output/line_" + i + "_col_" + j + ".jpg");
                    ImageIO.write(SubImgage, "jpg", outputfile);

                    System.out.println("Image cropped successfully: " + outputfile.getPath());

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void hexagonalCut(String imageFile, int partsCount) {
        // String templateImageFile = "hexagono-quase-perfeito.png";
        try {

            BufferedImage originalImgage = ImageIO.read(new File(imageFile));
            // BufferedImage templateImage = ImageIO.read(new File(templateImageFile));

            BufferedImage templateImage = createHexagonForCut(originalImgage.getWidth() / partsCount);
            saveImageToFile(templateImage, "output/template.jpg");
            int witdhAmount = templateImage.getWidth();
            int heightAmmount = templateImage.getHeight();
            int startX = 0;
            int startY = 0;

            int marginX = 0;
            // BufferedImage newImage = originalImgage;
            for (int x = 0, rowNum = 0; x < originalImgage.getWidth(); rowNum++, x += witdhAmount) {

                for (int y = 0, colNum = 0; y < originalImgage
                        .getHeight(); colNum++, y += (heightAmmount - (heightAmmount / 4))) {

                    if (colNum % 2 == 0) {
                        startX = 0;
                    } else {

                        startX = (witdhAmount / 2);
                    }

                    int destinationWith = witdhAmount;
                    int destinationHeight = heightAmmount;

                    if (destinationHeight + y + startY >= originalImgage.getHeight()) {
                        // destinationHeight =destinationHeight- (y + startY);
                        continue;
                    }

                    if (destinationWith + x + marginX + startX >= originalImgage.getWidth()) {
                        // destinationWith = (int) squareSize / 2;
                        continue;
                    }

                    BufferedImage SubImgage = originalImgage.getSubimage(x + startX + marginX, y + startY, destinationWith,
                            destinationHeight);

                   

                    File outputfile = new File("output/line_" + rowNum + "_col_" + colNum + ".png");

                    BufferedImage newImage = addImage(SubImgage, templateImage, 1, 0, 0);

                    newImage = removeMask(newImage,pixelToRemove.getRGB());

                    if (ImageIO.write(newImage, "png", outputfile)) {
                        System.out.println("Image cropped successfully: " + outputfile.getPath());
                    } else {
                        System.out.println("Error saving: " + outputfile.getPath());
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void hexagonalGrid(String imageFile, int partsCount) {
        // String templateImageFile = "hexagono-quase-perfeito.png";
        try {

            BufferedImage originalImgage = ImageIO.read(new File(imageFile));
            // BufferedImage templateImage = ImageIO.read(new File(templateImageFile));

            BufferedImage templateImage = createHexagon(originalImgage.getWidth() / partsCount);
            saveImageToFile(templateImage, "output/template.jpg");
            int witdhAmount = templateImage.getWidth();
            int heightAmmount = templateImage.getHeight();
            int startX = 0;
            int startY = 0;
            
            int marginX = 0;

            BufferedImage newImage = originalImgage;
            for (int x = 0, rowNum = 0; x < originalImgage.getWidth(); rowNum++, x += witdhAmount) {

                for (int y = 0, colNum = 0; y < originalImgage
                        .getHeight(); colNum++, y += (heightAmmount - (heightAmmount / 4))) {

                    if (colNum % 2 == 0) {
                        startX = 0;
                    } else {

                        startX = (witdhAmount / 2);
                    }

                    int destinationWith = witdhAmount;
                    int destinationHeight = heightAmmount;

                    if (destinationHeight + y + startY >= originalImgage.getHeight()) {
                        // destinationHeight =destinationHeight- (y + startY);
                        continue;
                    }

                    if (destinationWith + x + marginX + startX >= originalImgage.getWidth()) {
                        // destinationWith = (int) squareSize / 2;
                        continue;
                    }

                    newImage = addImage(newImage, templateImage, 1, x + marginX + startX, y + startY);

                }

            }

            File outputfile = new File("output/grid.png");
            if (ImageIO.write(newImage, "png", outputfile)) {
                System.out.println("Image cropped successfully: " + outputfile.getPath());
            } else {
                System.out.println("Error saving: " + outputfile.getPath());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveImageToFile(BufferedImage image, String fileName) throws IOException {

        File outputfile = new File(fileName);
        if (ImageIO.write(image, "png", outputfile)) {
            System.out.println("Image cropped successfully: " + outputfile.getPath());
        } else {
            System.out.println("Error saving: " + outputfile.getPath());
        }
    }

    private static BufferedImage addImage(BufferedImage buff1, BufferedImage buff2, float opaque, int x, int y) {
        BufferedImage temp = copyImage(buff1);
        Graphics2D g2d = temp.createGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opaque));
        g2d.drawImage(buff2, x, y, null);
        g2d.dispose();

        return temp;
    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    public static BufferedImage copyImage(BufferedImage source) {
        BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics g = b.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }

    public static BufferedImage createHexagon(int radius) {
        Point center = new Point(radius / 2, radius / 2);

        Hexagon hexagon = new Hexagon(center, radius / 2);

        int witdth = (int) hexagon.getBounds().getWidth();
        int height = (int) hexagon.getBounds().getHeight();

        hexagon.setCenter(new Point(radius / 2 - ((height - witdth) / 2), radius / 2));
        BufferedImage image = new BufferedImage(witdth , height, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();
        
        hexagon.draw((Graphics2D) graphics, 0, 0, 1, pixelToBeReplaced, false);

        return image;
    }

    public static BufferedImage createHexagonForCut(int radius) {
        Point center = new Point(radius / 2, radius / 2);

        Hexagon hexagon = new Hexagon(center, radius / 2);

        int witdth = (int) hexagon.getBounds().getWidth();
        int height = (int) hexagon.getBounds().getHeight();

        hexagon.setCenter(new Point(radius / 2 - ((height - witdth) / 2), radius / 2));
        BufferedImage image = new BufferedImage(witdth + 1 , height, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = image.getGraphics();
        graphics.setColor(pixelToRemove);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
         
        
        hexagon.draw((Graphics2D) graphics, 0, 0, 1, pixelToBeReplaced, true);

        removePixels(image, pixelToBeReplaced.getRGB());

        return image;
    }

    private static void removePixels(BufferedImage image, int pixelToRemove){
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                if (image.getRGB(i, j) == pixelToRemove ) {
                    image.setRGB(i, j, 0);
                }
                
            }
        }
    }

    private static void removePixels(BufferedImage image, int pixelToRemove, int newPixel){
        
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                if (image.getRGB(i, j) == pixelToRemove ) {
                    image.setRGB(i, j, newPixel);       
                    
                }
                
            }
        }
    }

    private static BufferedImage removeMask (BufferedImage image, int maskColor){
        BufferedImage newImage = new BufferedImage(image.getWidth() , image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                if (image.getRGB(i, j) != maskColor ) {
                    newImage.setRGB(i, j, image.getRGB(i, j));       
                    
                }                
            }
        }

        return newImage;
    }
}