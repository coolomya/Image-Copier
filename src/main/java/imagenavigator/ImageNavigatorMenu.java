/*
 * MIT-NonCommercial License
 * Copyright (c) 2025 Omey Bhosale
 * See the LICENSE file in the project root for license information.
 */

package imagenavigator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ImageNavigatorMenu {

    /**
     * @param args
     */
    /**
     * @param args
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Create the menu frame
            JFrame menuFrame = new JFrame("Image Navigator Menu");
            menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            menuFrame.setUndecorated(true); // Remove title bar

            // Set full-screen mode
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            gd.setFullScreenWindow(menuFrame);

            // Main panel for content
            JPanel mainPanel = new JPanel();
            mainPanel.setBackground(Color.BLACK);
            mainPanel.setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Title
            JLabel titleLabel = new JLabel("        Image Copier        ", JLabel.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
            titleLabel.setForeground(Color.WHITE);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            mainPanel.add(titleLabel, gbc);

            // Title
            JLabel creatorLabel = new JLabel("by Omey Bhosale. v1.3", JLabel.CENTER);
            creatorLabel.setFont(new Font("Arial", Font.BOLD, 8));
            creatorLabel.setForeground(Color.WHITE);
            gbc.gridx = 0;
            gbc.gridy = 6;
            gbc.gridwidth = 10;
            mainPanel.add(creatorLabel, gbc);

            // Images folder input
            JTextField imagesFolderField = new JTextField();
            JButton imagesFolderButton = new JButton("Select Images Folder");
			// Images folder selection
//            imagesFolderField.setText("E:\\Photos lagn\\swisstransfer_3de32f4f-a03c-4ebf-94d0-35b8fc1a351a");
//            imagesFolderField.setText("E:\\Photos lagn\\inputimges_test");
			imagesFolderButton.addActionListener(e -> {
				String folderPath = selectFolder(menuFrame, gd);
				if (folderPath != null) {
					imagesFolderField.setText(folderPath);
				}
			});
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            mainPanel.add(imagesFolderButton, gbc);
            gbc.gridx = 1;
            mainPanel.add(imagesFolderField, gbc);

            // Target folder input
            JTextField targetFolderField = new JTextField();
            JButton targetFolderButton = new JButton("Select Target Folder");
			// Target folder selection
        //    targetFolderField.setText("E:\\Photos lagn\\omi\\new selected");
			targetFolderButton.addActionListener(e -> {
				String folderPath = selectFolder(menuFrame, gd);
				if (folderPath != null) {
					targetFolderField.setText(folderPath);
				}
			});
            gbc.gridx = 0;
            gbc.gridy = 2;
            mainPanel.add(targetFolderButton, gbc);
            gbc.gridx = 1;
            mainPanel.add(targetFolderField, gbc);

            // Skip images input
            JTextField skipCountField = new JTextField("0"); // Default to 0
            JLabel skipCountLabel = new JLabel("Skip First N Images:");
            skipCountLabel.setForeground(Color.WHITE);
            skipCountLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            gbc.gridx = 0;
            gbc.gridy = 3;
            mainPanel.add(skipCountLabel, gbc);
            gbc.gridx = 1;
            mainPanel.add(skipCountField, gbc);

            // Start button
            JButton startButton = new JButton("Start");
            startButton.setFont(new Font("Arial", Font.BOLD, 18));
            startButton.addActionListener((ActionEvent e) -> {
                String imagesFolderPath = imagesFolderField.getText().trim();
                String targetFolderPath = targetFolderField.getText().trim();
                int skipCount = 0;

                try {
                    skipCount = Integer.parseInt(skipCountField.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(menuFrame, "Invalid number for skip count.");
                    return;
                }

                if (imagesFolderPath.isEmpty() || targetFolderPath.isEmpty()) {
                    JOptionPane.showMessageDialog(menuFrame, "Please provide both folder paths.");
                    return;
                }

                // Launch Image Navigator
                menuFrame.dispose();
                new ImageNavigator(imagesFolderPath, targetFolderPath, skipCount);
            });
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.gridwidth = 2;
            mainPanel.add(startButton, gbc);

            // Exit button
            JButton exitButton = new JButton("Exit");
            exitButton.setFont(new Font("Arial", Font.PLAIN, 16));
            exitButton.addActionListener(e -> System.exit(0));
            gbc.gridy = 5;
            mainPanel.add(exitButton, gbc);

            menuFrame.add(mainPanel);
            menuFrame.setVisible(true);
        });
    }

	// File Chooser Helper Method
	private static String selectFolder(JFrame menuFrame, GraphicsDevice gd) {
		// Temporarily exit full-screen mode
		gd.setFullScreenWindow(null);

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int result = fileChooser.showOpenDialog(menuFrame);

		// Restore full-screen mode
		gd.setFullScreenWindow(menuFrame);

		if (result == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile().getAbsolutePath();
		}
		return null;
	}

}
