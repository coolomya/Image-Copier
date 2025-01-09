package imagenavigator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

public class ImageNavigator {
    private JFrame frame;
    private JPanel namePanel ;
    private JLabel imageLabel, imageName, existImageLabel;
    private File[] imageFiles;
    private Set<String> copiedImages;
    private int currentIndex = 0;

    public ImageNavigator(String folderPath, String pasteFolderPath2) {
    	
		//initPropertyRead();
		
        File imagesFolder = new File(folderPath);
        File targetFolder = new File(pasteFolderPath2);
        
        if (targetFolder.exists() && targetFolder.isDirectory()) {
        	copiedImages = Arrays.asList(targetFolder.listFiles(file -> file.isFile() && isImageFile(file))).stream().map(file -> file.getName()).collect(Collectors.toSet());
            //System.out.println("copied images : " + copiedImages.size());
        } else {
        	JOptionPane.showMessageDialog(null, "Invalid target folder path.");
        }
        
        if (imagesFolder.exists() && imagesFolder.isDirectory()) {
            imageFiles = imagesFolder.listFiles(file -> file.isFile() && isImageFile(file));

            if (imageFiles != null && imageFiles.length > 0) {
                setupUI(pasteFolderPath2);
                loadImage(currentIndex);
            } else {
                JOptionPane.showMessageDialog(null, "No images found in the specified folder.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Invalid folder path.");
        }

        
    }

    public ImageNavigator(String folderPath, String pasteFolderPath, int skipCount) {
        File imagesFolder = new File(folderPath);
        File targetFolder = new File(pasteFolderPath);

        if (targetFolder.exists() && targetFolder.isDirectory()) {
            copiedImages = Arrays.asList(targetFolder.listFiles(file -> file.isFile() && isImageFile(file)))
                    .stream()
                    .map(File::getName)
                    .collect(Collectors.toSet());
            //System.out.println("Copied images: " + copiedImages.size());
        } else {
            JOptionPane.showMessageDialog(null, "Invalid target folder path.");
        }

        if (imagesFolder.exists() && imagesFolder.isDirectory()) {
            imageFiles = imagesFolder.listFiles(file -> file.isFile() && isImageFile(file));

            if (imageFiles != null && imageFiles.length > 0) {
                // Skip first 'n' images
                if (skipCount < imageFiles.length) {
                    currentIndex = skipCount;
                } else {
                    JOptionPane.showMessageDialog(null, "Skip count exceeds the number of images.");
                    currentIndex = 0;
                }

                setupUI(pasteFolderPath);
                loadImage(currentIndex);
            } else {
                JOptionPane.showMessageDialog(null, "No images found in the specified folder.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Invalid folder path.");
        }
    }

    
    private void setupUI(String pasteFolderPath) {
    	namePanel = new JPanel();
        frame = new JFrame();
        frame.setUndecorated(true); // Remove window decorations
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        gd.setFullScreenWindow(frame); // Set full-screen mode

        frame.setLayout(new BorderLayout());

        // Set dark background color for the frame
        frame.getContentPane().setBackground(Color.BLACK);
        namePanel.setBackground(Color.BLACK);
        namePanel.setLayout(new BorderLayout());
        
        imageLabel = new JLabel("", JLabel.CENTER);
        imageLabel.setBackground(Color.BLACK); // Set dark background for image label
        imageLabel.setOpaque(true); // Make the label opaque to show the background color

        imageName = new JLabel(imageFiles[currentIndex].getName());
        imageName.setForeground(Color.white);
        imageName.setHorizontalAlignment(SwingConstants.CENTER);

		existImageLabel = new JLabel("-");
		existImageLabel.setForeground(Color.WHITE);
		
		namePanel.add(existImageLabel, BorderLayout.EAST);
        namePanel.add(imageName, BorderLayout.CENTER);
        frame.add(imageLabel, BorderLayout.CENTER);
		frame.add(namePanel, BorderLayout.NORTH);
		
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_RIGHT: // Next image
                    	if (currentIndex < imageFiles.length - 1) {
                            currentIndex++;
                            loadImage(currentIndex);
                        }
                        break;

                    case KeyEvent.VK_LEFT: // Previous image
                        if (currentIndex > 0) {
                            currentIndex--;
                            loadImage(currentIndex);
                        }
                        break;

                    case KeyEvent.VK_UP: // Copy current image to "selected" folder
                        copyCurrentImage(pasteFolderPath);
                        break;

                    case KeyEvent.VK_DOWN: // Copy current image to "selected" folder
                    	deleteCurrentImage(pasteFolderPath);
                    	break;

                    case KeyEvent.VK_ESCAPE: // Exit full-screen mode
                        gd.setFullScreenWindow(null);
                        closeApp();
                        break;
                }
            }

        });

        frame.setFocusable(true);
        frame.setVisible(true);
    }


	private void closeApp() {
		//maybe do cleanup here
		System.exit(0);
	}
	
	private void loadImage(int index) {
        try {
        	imageName.setText(imageFiles[index].getName());
        	
        	if (copiedImages.contains(imageFiles[index].getName())) {
        		setCopiedColors();
        	} else {
        		setUncopiedColors();
        	}
        	
        	////System.out.println("image: " + imageFiles[index].getName());
        	long starttime = System.currentTimeMillis();
        	long mainstarttime = starttime;
            
            BufferedImage image = ImageIO.read(imageFiles[index]);
            
            starttime = timeLog(starttime, "1");
            
            if (image != null) {
                image = rotateImageIfRequired(imageFiles[index], image);

                starttime = timeLog(starttime, "2");
                
                // Get screen dimensions
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int screenWidth = (int) screenSize.getWidth();
                int screenHeight = (int) screenSize.getHeight();

                starttime = timeLog(starttime, "3");
                // Calculate scaling dimensions
                int imageWidth = image.getWidth();
                int imageHeight = image.getHeight();

                double widthScale = (double) screenWidth / imageWidth;
                double heightScale = (double) screenHeight / imageHeight;
                double scale = Math.min(widthScale, heightScale); // Maintain aspect ratio

                int scaledWidth = (int) (imageWidth * scale);
                int scaledHeight = (int) (imageHeight * scale);
               
                starttime = timeLog(starttime, "4");
                
                Image scaledImage = image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                
                starttime = timeLog(starttime, "5");
                
                imageLabel.setIcon(new ImageIcon(scaledImage));
                
                starttime = timeLog(starttime, "6");
            } else {
                System.err.println("Failed to load image: " + imageFiles[index].getName());
            }
            timeLog(starttime, "7");
            timeLog(mainstarttime, "all");
            
        } catch (Exception e) {
            System.err.println("Error loading image: " + imageFiles[index].getName() + " - " + e.getMessage());
        }
    }

	private void setUncopiedColors() {
		existImageLabel.setText("");
		namePanel.setBackground(Color.BLACK);
		imageName.setForeground(Color.WHITE);
	}

    private long timeLog(long starttime) {
		return timeLog(starttime, "");
	}
    
	private long timeLog(long starttime, String checkpoint) {
		long outTime = System.currentTimeMillis();
//		//System.out.println("Loaded image in time - " + checkpoint + " : " + ((outTime - starttime)) + " ms" );
		return outTime;
	}

    private BufferedImage rotateImageIfRequired(File file, BufferedImage image) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

            if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                int orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);

                switch (orientation) {
                    case 6: // Rotate 90 degrees clockwise
                        return rotateImage(image, 90);
                    case 3: // Rotate 180 degrees
                        return rotateImage(image, 180);
                    case 8: // Rotate 90 degrees counterclockwise
                        return rotateImage(image, -90);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading EXIF data for: " + file.getName() + " - " + e.getMessage());
        }

        return image; // Return original image if no rotation is needed
    }

    private BufferedImage rotateImage(BufferedImage image, double angle) {
        int w = image.getWidth();
        int h = image.getHeight();

        BufferedImage rotated = new BufferedImage(h, w, image.getType());
        Graphics2D g2d = rotated.createGraphics();

        g2d.translate((h - w) / 2.0, (w - h) / 2.0);
        g2d.rotate(Math.toRadians(angle), w / 2.0, h / 2.0);
        g2d.drawRenderedImage(image, null);
        g2d.dispose();

        return rotated;
    }

    private void copyCurrentImage(String pasteFolderPath) {
        try {
        	//System.out.println("copying : " + imageFiles[currentIndex].getName());
        	if (!copiedImages.contains(imageFiles[currentIndex].getName())) {
              File currentImage = imageFiles[currentIndex];
//              File targetDir = new File("E:\\Photos lagn\\omi\\selected");
              File targetDir = new File(pasteFolderPath);

              if (!targetDir.exists()) {
                  targetDir.mkdirs();
              }

              File targetFile = new File(targetDir, currentImage.getName());
              Files.copy(currentImage.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

              //System.out.println("Copied image to: " + targetFile.getAbsolutePath());
              copiedImages.add(imageFiles[currentIndex].getName());
              //System.out.println("copied images : " + copiedImages.size());
              setCopiedColors();
        	} else {
        		//System.out.println("Image already copied");
        	}

        } catch (IOException e) {
            System.err.println("Failed to copy image: " + e.getMessage());
        }
    }

	private void setCopiedColors() {
		existImageLabel.setText("copied");
		existImageLabel.setForeground(Color.BLACK);
		namePanel.setBackground(Color.GREEN);
		imageName.setForeground(Color.BLACK);
	}
    
    private void deleteCurrentImage(String pasteFolderPath) {
    	try {
        	//System.out.println("deleting : " + imageFiles[currentIndex].getName());
        	if (copiedImages.contains(imageFiles[currentIndex].getName())) {
              File currentImage = imageFiles[currentIndex];
//              File targetDir = new File("E:\\Photos lagn\\omi\\selected");
              File targetDir = new File(pasteFolderPath);

              if (!targetDir.exists()) {
                  targetDir.mkdirs();
              }

              File targetFile = new File(targetDir, currentImage.getName());
              Files.delete(targetFile.toPath());

              //System.out.println("Deleted copied image: " + targetFile.toPath());
              copiedImages.remove(imageFiles[currentIndex].getName());
              //System.out.println("copied images : " + copiedImages.size());
              setUncopiedColors();
        	} else {
        		//System.out.println("Cannot delete not copied image");
        	}

        } catch (IOException e) {
            System.err.println("Failed to copy image: " + e.getMessage());
        }
	}


    private static boolean isImageFile(File file) {
        String[] imageExtensions = { "jpg", "jpeg", "png", "gif", "bmp" };
        String fileName = file.getName().toLowerCase();
        for (String extension : imageExtensions) {
            if (fileName.endsWith("." + extension)) {
                return true;
            }
        }
        return false;
    }

    public static void mainold(String[] args) {
    	String imagesFolderPath = "E:\\Photos lagn\\swisstransfer_3de32f4f-a03c-4ebf-94d0-35b8fc1a351h";
    	String pasteFolderPath = "E:\\Photos lagn\\omi\\selected";
        new ImageNavigator(imagesFolderPath, pasteFolderPath);
    }

}
