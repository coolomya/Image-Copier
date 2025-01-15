package imagenavigator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

public class ImageNavigator {
	private final static String LOAD_IMG_TIME_LOG_PREFIX = "Loaded image in time - ";
	private final static int CARASOUL_SIZE = 20;
	private static int CARASOUL_MID = (int) Math.floor((double) CARASOUL_SIZE / 2);

	private JFrame frame;
	private JPanel namePanel;
	private JLabel imageLabel, imageName, existImageLabel;
	private File[] imageFiles;
	private Set<String> copiedImages;
	private int currentIndex = 0;
	private int skippedStart = 0;
//    private Deque<BufferedImage> imageCarasoul = new ArrayDeque<>(CARASOUL_SIZE);
	private Map<String, BufferedImage> imageCarasoul = new HashMap<>(CARASOUL_SIZE);
	private Map<Integer, String> imageCarasoulNames = new TreeMap<>();
	//private Deque<String> imageCarasoulNames = new ArrayDeque<>(CARASOUL_SIZE);
	private List<Integer> disableLogChannels = Arrays.asList(1, 4);

	public ImageNavigator(String folderPath, String pasteFolderPath2) {

		// initPropertyRead();
		long starttime = System.currentTimeMillis();
		
		File imagesFolder = new File(folderPath);
		File targetFolder = new File(pasteFolderPath2);

		if (targetFolder.exists() && targetFolder.isDirectory()) {
			copiedImages = Arrays.asList(targetFolder.listFiles(file -> file.isFile() && isImageFile(file))).stream()
					.map(file -> file.getName()).collect(Collectors.toSet());
			//System.out.println("copied images : " + copiedImages.size());
			starttime = timeLog(starttime, "Loading target folder copied images done", 0);
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
			timeLog(starttime, "Loading source folder images done", 0);
		} else {
			JOptionPane.showMessageDialog(null, "Invalid folder path.");
		}

	}

	public ImageNavigator(String folderPath, String pasteFolderPath, int skipCount) {
		File imagesFolder = new File(folderPath);
		File targetFolder = new File(pasteFolderPath);
		long starttime = System.currentTimeMillis();
		
		if (targetFolder.exists() && targetFolder.isDirectory()) {
			copiedImages = Arrays.asList(targetFolder.listFiles(file -> file.isFile() && isImageFile(file))).stream()
					.map(File::getName).collect(Collectors.toSet());
			//System.out.println("Copied images: " + copiedImages.size());
			starttime = timeLog(starttime, "Main - Loading target folder copied images done", 0);
		} else {
			JOptionPane.showMessageDialog(null, "Invalid target folder path.");
		}

		if (imagesFolder.exists() && imagesFolder.isDirectory()) {
			imageFiles = imagesFolder.listFiles(file -> file.isFile() && isImageFile(file));

			timeLog(starttime, "Main - Loading source folder images done", 0);
			
			if (imageFiles != null && imageFiles.length > 0) {
				// Skip first 'n' images
				if (skipCount < imageFiles.length) {
					skippedStart = skipCount;
					currentIndex = skipCount;
				} else {
					JOptionPane.showMessageDialog(null, "Skip count exceeds the number of images.");
					currentIndex = 0;
				}
				
				JDialog loadingDialog = createLoadingDialog();
				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
					long starttime = System.currentTimeMillis();
					@Override
					protected Void doInBackground() throws Exception {
						preloadImages(currentIndex, currentIndex + 1);
						starttime = timeLog(starttime, "Main - PreLoading first image is done", 0);
						//preloadImagesLazy(currentIndex + 1);
						preloadImages(currentIndex);
						starttime = timeLog(starttime, "Main - PreLoading first " + (CARASOUL_SIZE + skippedStart > imageFiles.length ? imageFiles.length  - skippedStart : CARASOUL_SIZE) + " images is done", 0);
						setupUI(pasteFolderPath);
						starttime = timeLog(starttime, "Main - Setup ui is done", 0);
						loadImage(currentIndex);
						starttime = timeLog(starttime, "Main - Load first image is done", 0);
						return null;
					}
					
					@Override
					protected void done() {
						loadingDialog.dispose();
						 SwingUtilities.invokeLater(() -> {
						        frame.requestFocusInWindow(); // Request focus for the frame
						        frame.requestFocus(); // Ensure focus is explicitly set
						    });
					};
				};
				worker.execute();
				loadingDialog.setVisible(true);
			} else {
				JOptionPane.showMessageDialog(null, "No images found in the specified folder.");
			}
		} else {
			JOptionPane.showMessageDialog(null, "Invalid folder path.");
		}
	}

	private JDialog createLoadingDialog() {
		JDialog dialog = new JDialog();
		dialog.setTitle("Loading...");
		dialog.setModal(true); // Block user interaction
		dialog.setUndecorated(true); // Remove title bar
		dialog.setSize(200, 100);
		dialog.setLocationRelativeTo(null); // Center on screen

		JPanel panel = new JPanel(new BorderLayout());
		JLabel loadingLabel = new JLabel("Loading, please wait...", JLabel.CENTER);
		loadingLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		panel.add(loadingLabel, BorderLayout.CENTER);

		dialog.add(panel);
		return dialog;
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

		//System.out.println("carasould mid : " + CARASOUL_MID);

		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				//System.out.println("------------------------");
				switch (e.getKeyCode()) {
				case KeyEvent.VK_RIGHT: // Next image
					if (currentIndex < imageFiles.length - 1) {
						currentIndex++;
						shiftImages(currentIndex, "right");
						loadImage(currentIndex);
					}
					break;

				case KeyEvent.VK_LEFT: // Previous image
					if (currentIndex > 0) {
						currentIndex--;
						shiftImages(currentIndex, "left");
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

	private void shiftImages(int index, String direction) {
		//System.out.println("image carasoul b4 : " + imageCarasoulNames);
		if (direction.toLowerCase().equals("left")) {
			loadPrevImage(index);
		} else if (direction.toLowerCase().equals("right")) {
			loadNextImage(index);
		}
		log("image carasoul a8 : " + imageCarasoulNames, 4);
	}

	private void loadNextImage(int index) {
		Thread thread = new Thread(() -> {
			synchronized (imageCarasoul) {
				if (index > skippedStart + CARASOUL_MID && index + CARASOUL_MID < imageFiles.length) {
					long startTime = System.currentTimeMillis();
					int nextImgIdx = index + CARASOUL_MID - 1;
					String nextImageName = imageFiles[nextImgIdx].getName();

					if (!imageCarasoulNames.containsKey(nextImgIdx)) {
						try {
							//System.out.println("shifting carasoul to left : " + index + " : " + nextImageName);
							BufferedImage originalImage = ImageIO.read(imageFiles[nextImgIdx]);

							// Pre-scale during loading
							BufferedImage scaledImage = scaleImageToScreen(originalImage);

							// Update map and deque
							int minImgIdx = Collections.min(imageCarasoulNames.keySet());
							String removedImgName = imageCarasoulNames.get(minImgIdx);
							if (imageCarasoul.size() >= CARASOUL_SIZE) {
								imageCarasoul.remove(removedImgName);								
							}
							imageCarasoul.put(nextImageName, scaledImage);

							if (imageCarasoulNames.size() >= CARASOUL_SIZE) {
								imageCarasoulNames.remove(minImgIdx);
							}
							imageCarasoulNames.put(nextImgIdx, nextImageName);

							timeLog(startTime, "shift carasoul left done removed " + removedImgName + " << " + nextImageName + " added", 2);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});

		thread.start();
	}

	private void reLoadImage(int index) {
		Thread thread = new Thread(() -> {
			synchronized (imageCarasoul) {
				long startTime = System.currentTimeMillis();
				int imgIdx = index;
				String imageName = imageFiles[imgIdx].getName();

				if (!imageCarasoulNames.containsKey(imgIdx)) {
					try {
						//System.out.println("reloading carasoul to : " + index + " : " + imageName);
						BufferedImage originalImage = ImageIO.read(imageFiles[imgIdx]);

						// Pre-scale during loading
						BufferedImage scaledImage = scaleImageToScreen(originalImage);

						// Update map and deque
						int minImgIdx = Collections.min(imageCarasoulNames.keySet());
						String removedImgName = imageCarasoulNames.get(minImgIdx);
						if (imageCarasoul.size() >= CARASOUL_SIZE) {
							imageCarasoul.remove(removedImgName);
						}
						imageCarasoul.put(imageName, scaledImage);

						if (imageCarasoulNames.size() >= CARASOUL_SIZE) {
							imageCarasoulNames.remove(minImgIdx);
						}
						imageCarasoulNames.put(imgIdx, imageName);

						timeLog(startTime, "reload carasoul is done removed " + removedImgName + " << " + imageName + " added", 2);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		thread.start();
	}

	private void loadPrevImage(int index) {
		Thread thread = new Thread(() -> {
			synchronized (imageCarasoul) {
				if (index >= CARASOUL_MID + skippedStart && index - (CARASOUL_MID + skippedStart) >= 0
						&& index + (CARASOUL_MID) < imageFiles.length) {
					long startTime = System.currentTimeMillis();
					int prevImgIdx = index - (CARASOUL_MID);
					String prevImageName = imageFiles[prevImgIdx].getName();

					if (!imageCarasoulNames.containsKey(prevImgIdx)) {
						try {
							//System.out.println("shifting carasoul to right : " + index + " : " + prevImageName);
							BufferedImage originalImage = ImageIO.read(imageFiles[prevImgIdx]);

							// Pre-scale during loading
							BufferedImage scaledImage = scaleImageToScreen(originalImage);

							// Update map and deque
//							String removedImgName = imageCarasoulNames.getLast();
							int lastImgIdx = Collections.max(imageCarasoulNames.keySet());
							String removedImgName = imageCarasoulNames.get(lastImgIdx);
							if (imageCarasoul.size() >= CARASOUL_SIZE) {
								imageCarasoul.remove(removedImgName);
							}
							imageCarasoul.put(prevImageName, scaledImage);
							
							if (imageCarasoulNames.size() >= CARASOUL_SIZE) {
								imageCarasoulNames.remove(lastImgIdx);
							}
							imageCarasoulNames.put(prevImgIdx, prevImageName);

							timeLog(startTime, "shift carasoul right done added " + prevImageName + " >> " + removedImgName + " removed", 2);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});

		thread.start();
	}

	private BufferedImage scaleImageToScreen(BufferedImage originalImage) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenWidth = (int) screenSize.getWidth();
		int screenHeight = (int) screenSize.getHeight();

		int imageWidth = originalImage.getWidth();
		int imageHeight = originalImage.getHeight();

		double widthScale = (double) screenWidth / imageWidth;
		double heightScale = (double) screenHeight / imageHeight;
		double scale = Math.min(widthScale, heightScale);

		int scaledWidth = (int) (imageWidth * scale);
		int scaledHeight = (int) (imageHeight * scale);

		Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
		BufferedImage finalImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d = finalImage.createGraphics();
		g2d.drawImage(scaledImage, 0, 0, null);
		g2d.dispose();

		return finalImage;
	}

	private void preloadImagesLazy(int currentIndex) {
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			long starttime = System.currentTimeMillis();

			@Override
			protected Void doInBackground() {
				//System.out.println("Preloading started");
				preloadImages(currentIndex);
				return null;
			}

			@Override
			protected void done() {
				timeLog(starttime, "Preading done ", 3);
				//System.out.println("Preloading completed." + imageCarasoulNames);
			}
		};
		worker.execute();
	}

	private void preloadImages(int currentIndex) {
		int preloadStart = 0;
		int preloadEnd = CARASOUL_SIZE;
		
		if (currentIndex > 0) {//skipped some images
			if (skippedStart + CARASOUL_SIZE > imageFiles.length) {
				preloadStart = imageFiles.length - CARASOUL_SIZE < 0 ? 0 : imageFiles.length - CARASOUL_SIZE;
				preloadEnd = imageFiles.length;
			} else {
				preloadStart = skippedStart;
				preloadEnd = skippedStart + CARASOUL_SIZE;
			}
		} else if (CARASOUL_SIZE > imageFiles.length) {//
			preloadStart = currentIndex;//0
			preloadEnd = imageFiles.length;
		}
		
		//System.out.println("Preloading from : " + preloadStart + " to : " + preloadEnd);
		preloadImages(preloadStart, preloadEnd);
	}

	private void preloadImages(int currentIndex, int preloadEndIndex) {//preloadEndIndex not inclusive
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenWidth = (int) screenSize.getWidth();
		int screenHeight = (int) screenSize.getHeight();
		long starttime = System.currentTimeMillis();
		
		// do all of following in parallel stream as it is unrelated
		Arrays.stream(Arrays.copyOfRange(imageFiles, currentIndex, preloadEndIndex)).parallel()
				.forEach(img -> {
					//System.out.println("parallel preloading: " + img.getName());
					try {
						double scale = Math.min( (double) screenWidth / ImageIO.read(img).getWidth(), (double) screenHeight / ImageIO.read(img).getHeight());
						
						imageCarasoul.put(img.getName(), 
								new BufferedImage(
										(int) (ImageIO.read(img).getWidth() * scale), 
										(int) (ImageIO.read(img).getHeight() * scale), 
										BufferedImage.TYPE_INT_ARGB)
								);

						Graphics2D g2d = imageCarasoul.get(img.getName()).createGraphics();
						g2d.drawImage(
								ImageIO.read(img).getScaledInstance(
										(int) (ImageIO.read(img).getWidth() * scale), 
										(int) (ImageIO.read(img).getHeight() * scale), 
										Image.SCALE_SMOOTH), 
									0, 
									0, 
									null
								);
						g2d.dispose();

					} catch (IOException e) {
						e.printStackTrace();
					}
					//System.out.println("parallel preloading done: " + img.getName());
					
				});
		;

		for (int i = currentIndex; i < preloadEndIndex && i < imageFiles.length; i++) {
			//System.out.println("adding image : " + imageFiles[i].getName() + " at : " + i);
			imageCarasoulNames.put(i, imageFiles[i].getName());
		}
		
		timeLog(starttime, "pre processing images done", 3);
	}

	private void closeApp() {
		// maybe do cleanup here
		System.exit(0);
	}

	@SuppressWarnings("unused")
	private void loadImage(int index) {
		//System.out.println("index : " + index);
		int logchannel = 1;
		try {
			imageName.setText(imageFiles[index].getName());

			if (copiedImages.contains(imageFiles[index].getName())) {
				setCopiedColors();
			} else {
				setUncopiedColors();
			}

			//System.out.println("image: " + imageFiles[index].getName());
			long starttime = System.currentTimeMillis();
			long mainstarttime = starttime;

			BufferedImage image = null;

			if (imageCarasoul.containsKey(imageFiles[index].getName())) {
				image = imageCarasoul.get(imageFiles[index].getName());
				//System.out.println("Loaded carasoul image 0 : " + imageFiles[index].getName());
			} else {
				reLoadImage(index);
			}

			log("Image Loaded : " + image.toString(), 4);
			starttime = timeLog(starttime, LOAD_IMG_TIME_LOG_PREFIX + "1", logchannel);

			if (image != null) {
				image = rotateImageIfRequired(imageFiles[index], image);

				starttime = timeLog(starttime, LOAD_IMG_TIME_LOG_PREFIX + "2", logchannel);

				// Get screen dimensions
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				int screenWidth = (int) screenSize.getWidth();
				int screenHeight = (int) screenSize.getHeight();

				starttime = timeLog(starttime, LOAD_IMG_TIME_LOG_PREFIX + "3", logchannel);
				// Calculate scaling dimensions
				int imageWidth = image.getWidth();
				int imageHeight = image.getHeight();

				double widthScale = (double) screenWidth / imageWidth;
				double heightScale = (double) screenHeight / imageHeight;
				double scale = Math.min(widthScale, heightScale); // Maintain aspect ratio

				int scaledWidth = (int) (imageWidth * scale);
				int scaledHeight = (int) (imageHeight * scale);

				starttime = timeLog(starttime, LOAD_IMG_TIME_LOG_PREFIX + "4", logchannel);

				Image scaledImage = image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

				starttime = timeLog(starttime, LOAD_IMG_TIME_LOG_PREFIX + "5", logchannel);

				imageLabel.setIcon(new ImageIcon(scaledImage));

				starttime = timeLog(starttime, LOAD_IMG_TIME_LOG_PREFIX + "6", logchannel);
			} else {
				System.err.println("Failed to load image: " + imageFiles[index].getName());
			}
			timeLog(starttime, LOAD_IMG_TIME_LOG_PREFIX + "7", logchannel);
			timeLog(mainstarttime, LOAD_IMG_TIME_LOG_PREFIX + "all", logchannel);

		} catch (Exception e) {
			//System.out.println("loading image png");
			imageLabel.setIcon(new ImageIcon( getClass().getResource("/loading.png")));
			System.err.println("Error loading image: " + imageFiles[index].getName() + " - " + e.getMessage());
		}
	}

	private void setUncopiedColors() {
		existImageLabel.setText("");
		namePanel.setBackground(Color.BLACK);
		imageName.setForeground(Color.WHITE);
	}

	private void log(String msg, int channel) {
		if (disableLogChannels.contains(channel)) {
			return;
		}
		//System.out.println(msg);
	}
	
	private long timeLog(long starttime, int channel) {
		return timeLog(starttime, "", channel);
	}

	private long timeLog(long starttime, String checkpoint, int channel) {
		if (disableLogChannels.contains(channel)) {
			return starttime;
		}
		
		long outTime = System.currentTimeMillis();
		//System.out.println("Time - " + checkpoint + " : " + ((outTime - starttime)) + " ms");
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
