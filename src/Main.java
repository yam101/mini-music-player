// Imports modules necessary
import java.io.File;
import java.util.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import java.awt.*;

public class Main {
	public static void main(String[] args) {
		// Declares some variables used in this class
		int i;
		String song;
		JButton songName;
		
		// Creates a file object from the songs folder
		File folder = new File("songs");
		
		// Initializes three arraylists (song files, labels of the time duration for each song, and the buttons for each song)
		ArrayList <File> songList = new ArrayList<>();
		ArrayList <JLabel> songTimes = new ArrayList<>();
		ArrayList <JButton> songButtons = new ArrayList<>();
		
		// For loop that iterates through each file in the song folder
		for (File fileEntry : folder.listFiles()) {
			// Adds each file to song array
			songList.add(fileEntry);
			// Needs a try catch in order to handle the exception when audio input stream is called
			try {
				// Create an audioinput stream object from each file
				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(fileEntry);
				
				// Finds the format of the audio file
				AudioFormat format = audioInputStream.getFormat();
				
				// Finds length of audio file
				long audioFileLength = fileEntry.length();
				
				// From the format, finds frame size and frame rate in order to find the duration of the song
				int frameSize = format.getFrameSize();
				float frameRate = format.getFrameRate();
				
				// Calculates the duration of the song in seconds
				float totalSeconds = (audioFileLength / (frameSize * frameRate));
				
				// Calculates the duration of the song in minutes and seconds
				int minutes = (int) ((totalSeconds%3600)/60);
				int seconds = (int) (totalSeconds%60);
				
				// Formats the time into a string 
				String timeString = String.format("%2d:%02d", minutes, seconds);
				
				// Creates a JLabel out of the string
				JLabel timeLabel = new JLabel(timeString);
				
				// Sets text colour of the jlabel to white
				timeLabel.setForeground(Color.white);
				
				// Adds the song duration to the array list
				songTimes.add(timeLabel);
			}
			
			catch (Exception noFile) {
				// If there is no file in the song folder, then prints out an error message
				System.out.println("Can't find file.");
			}
		}
		
		// Iterates through each file in songList
		for (i=0;i<songList.size();i++) {
			// Gets the file, then gets the name of the file, then removes the .wav ending
			song = songList.get(i).getName().replace(".wav", "");
			
			// Creates a button using the file name
			songName = new JButton(song);
			
			// Formats the button to have white text, transparent background, text set to the edge of the button
			songName.setForeground(Color.white);
			songName.setBorderPainted(false);
			songName.setHorizontalAlignment(SwingConstants.LEFT);
			
			// Adds each button to the arraylist 
			songButtons.add(songName);
		}
		
		// Creates object out of the musicPlayer class, with the three arraylists as parameters
		musicPlayer obj = new musicPlayer(songButtons, songList, songTimes);
		
		// Calls the display method
		obj.display();
		
		// Calls the create button method 
		obj.createButton(songButtons, songTimes);
	}
}

