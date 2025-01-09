# Image-Copier

TLDR:
- run "Image Copier.bat" to start application
- need java installed (google on how to install java)

----------------Image Copier-----------

Image Copier is a Java-based application designed to navigate through images, copy selected images to a target folder, and visually distinguish between copied and uncopied images. It offers an intuitive full-screen menu for easy configuration.

--------------------------------------------------------------------------------------------

## Features :

1. Full-Screen Image Viewer: Navigate through images using keyboard keys.
2. Menu for Configuration:
   - Select the folder containing images.
   - Choose the target folder for copied images.
   - Specify the number of images to skip at the start.
3. Keyboard Shortcuts:
   - Right Arrow: View the next image.
   - Left Arrow: View the previous image.
   - Up Arrow: Copy the current image to the target folder.
   - Down Arrow: Delete the current image from the target folder.
   - Escape: Exit the application.
4. Color Indicators:
   - Green: Indicates the image has been copied.
   - Black: Indicates the image has not been copied.
5. Seamless File Selection: Temporarily exits full-screen mode for folder selection and restores full-screen afterward.

--------------------------------------------------------------------------------------------

## How to Use :

### Prerequisites :
	1. Ensure you have Java 8 or higher installed. (Preferably 23)

### Running the Application :
	1. Clone or download the repository.
	2. Open a terminal in the project directory.
	3. Run the file Image Copier.bat

### Configuration Menu
	1. Select Images Folder:
	   - Click the "Select Images Folder" button and choose the folder containing your images.
	2. Select Target Folder:
	   - Click the "Select Target Folder" button and choose where to copy the selected images.
	3. Skip First N Images:
	   - Enter the number of images to skip initially (default is 0).
	4. Click Start to launch the Image Copier.

### Using the Image Copier
	1. Use the Right Arrow and Left Arrow keys to navigate through images.
	2. Press the Up Arrow to copy the current image to the target folder.
	2. Press the Down Arrow to uncopy the current image to the target folder.
	3. Press Escape to exit the application.

--------------------------------------------------------------------------------------------

## Keyboard Controls :

| Key          | Action                              |
|--------------|-------------------------------------|
| Right Arrow  | View the next image                 |
| Left Arrow   | View the previous image             |
| Up Arrow     | Copy the current image              |
| Down Arrow   | Uncopy the current image            |
| Escape       | Exit the application                |

--------------------------------------------------------------------------------------------

## Credits :

- Developed by Omey Bhosale
- Contributions: Design, development, and user experience improvements.

--------------------------------------------------------------------------------------------

## Troubleshooting :

1. JAR File Doesn't Run:
   - Ensure Java is installed and properly configured.
   - Run `java -version` to verify.
2. Focus Issues During Folder Selection:
   - The application temporarily exits full-screen mode for folder selection. This is expected behavior.
3. Error: Invalid Folder Paths:
   - Ensure the selected folders exist and are accessible.

--------------------------------------------------------------------------------------------

## Future Improvements :
- Implement advanced image filtering options.
- Allow customization of keyboard shortcuts.

Feel free to report any issues or suggestions for improvement!