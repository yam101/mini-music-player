import java.util.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;


public class musicPlayer implements ActionListener{
	// Declares the variables used in this class
	JLabel title, label, volume, albumCover, subtitle, endTime, currentPosition;
	JPanel displayPanel, controlPanel, emptyPanel;
	JFrame frame;
	JButton select, play, pause, skip, previous, songName, shuffle, song;
	JSlider volumeSlider, seek;
	ImageIcon iconPlay, iconPause, iconSkip, iconPrevious, iconShuffle, iconVolume, albumImage;
	ArrayList<JButton> songButtonList;
	ArrayList<File> songFiles;
	ArrayList<JLabel> songLength;
	int i, length, width, height, seekValue, minutes=0;
	long clipTimePosition, audioFileLength;
	float currentVolume;
	String musicName;
	File musicPath;
	Clip clip;
	GridBagConstraints gbc, gbc2;
	GridBagLayout gbl;
	Color purple, darkGray;
	FloatControl fc;
	Thread updateSeekBar;
	private volatile boolean running;
	private final Object pauseLock = new Object();
	private volatile boolean paused = false;
	Runnable myRunnable;
	boolean restart=false, reset=false;

	public musicPlayer(ArrayList<JButton> songButtons, ArrayList<File> songList, ArrayList<JLabel> songTimes) {
		// A constructor which declares some of the major array lists used in the class
		songButtonList = songButtons;
		songFiles = songList;
		songLength = songTimes;
	}
	
	public void display() { // This method sets the format of the frame and panels, as well as create the buttons for controlling the music, and labels for the text 
		// Initializes a LayoutManager for jframe (this type of layout manager allows us to place each component at a specific x,y coordinate
		gbc = new GridBagConstraints();
		gbc2 = new GridBagConstraints();
		gbl = new GridBagLayout();
		
		// Creates an empty panel for formatting
		emptyPanel = new JPanel();

		// Creates colours used for formatting from rgb values
		darkGray = new Color(26,26,26);
		purple  = new Color(158, 106, 154);
		
		// Initializes volume slider
		volumeSlider = new JSlider(-30,6) {
			@Override
            public void updateUI() {
                setUI(new SliderUI());
            }
		};
		volumeSlider.setPreferredSize(new Dimension(150, 10));
		volumeSlider.setOpaque(false);
		volumeSlider.setValue(0);
		
		// Initializes frame/window where the GUI is displayed
		frame = new JFrame("Music Player");
	
		
		// Initializes the container/panel inside of the frame where the songs will be displayed
		displayPanel = new JPanel() {
			// The following code changes the look of the display panel, which is not something you can do normally, but the override allows to 
			@Override
			protected void paintComponent(Graphics g)
		     {
				// Creates a gradient from the top to the bottom of the panel (code was taken with some adjustments from one of the sources)
		        super.paintComponent(g);
		        Graphics2D background = (Graphics2D) g;
		        background.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		        // Gets width and height of the frame in order to format the gradient
		        width = getWidth();
		        height = getHeight();
		        // Initializes the two colours used in the gradient
		        Color pink = new Color(232,180,227);
		        Color black = Color.black;
		        // Declares and initializes gradient with the arguments of x1 coord, y1 coord, colour1, x2 coord, y2 coord, and colour2
		        GradientPaint gradient = new GradientPaint(5, 0, pink, 5, height, black);
		        // Fills the background of the frame with the gradient for the full length and width of the panel
		        background.setPaint(gradient);
		        background.fillRect(0,0, width, height);
		     }
		};
		
		// Initializes the container/panel where the control buttons will be located
	    controlPanel = new JPanel(); 
			
	    //Setting Layout of displayPanel using gridbaglayout
		displayPanel.setLayout(gbl);
		
	    //Setting Layout of ControlPanel using grid layout (allows us to create cells to place each component in)
	    controlPanel.setLayout(gbl);
	    
	    
	    // Sets the background colour of the control panel to dark gray
	    controlPanel.setBackground(darkGray);
		
		// Creating image icons using pngs for each of the buttons and the album cover
		iconPlay = new ImageIcon("./images/play.png");
		iconPause = new ImageIcon("./images/pause.png");
		iconSkip = new ImageIcon("./images/skip.png");
		iconPrevious = new ImageIcon("./images/previous.png");
		iconShuffle = new ImageIcon("./images/shuffle.png");
		iconVolume = new ImageIcon("./images/volume.png");
		albumImage = new ImageIcon("./images/album cover.png");
		
		// Creates label from the album cover icon and the volume icon
		albumCover = new JLabel(albumImage);
		volume = new JLabel(iconVolume);

		//Creating buttons from each of the image icons
		play = new JButton(iconPlay);
		pause = new JButton(iconPause);
		skip = new JButton(iconSkip);
		previous = new JButton(iconPrevious);
		shuffle = new JButton(iconShuffle);

		// Changing the background of the buttons to transparent
		play.setBorderPainted(false);
		pause.setBorderPainted(false);
		previous.setBorderPainted(false);
		skip.setBorderPainted(false);
		shuffle.setBorderPainted(false);
		
		// Changing background of the empty panel to be transparent
		emptyPanel.setOpaque(false);
		
		// Formats the buttons/labels to left/right side of the cell that they lie in
		previous.setHorizontalAlignment(SwingConstants.RIGHT);
		skip.setHorizontalAlignment(SwingConstants.LEFT);
		volume.setHorizontalAlignment(SwingConstants.RIGHT);
		
		// Adding action events which allows for the program to detect when the button is pressed (equivalent to scanner)
		pause.addActionListener(this);
		play.addActionListener(this);
		previous.addActionListener(this);
		skip.addActionListener(this);
		shuffle.addActionListener(this);
		
		// Creates label for the title and subtitle of the album
		title = new JLabel("YOUNG-LUV.COM");
		subtitle = new JLabel("STAYC • 2022 • 6 songs, 19 min 32 sec");
		
		// Creates fonts for the title and subtitle labels
		Font titleFont = new Font("Futura Bold", Font.BOLD, 36);
		Font smallFont = new Font("Futura Medium", Font.PLAIN, 14);
		
		// Sets the fonts of title and subtitle
		title.setFont(titleFont);
		subtitle.setFont(smallFont);
		
		// Sets the colour of the text to white
		title.setForeground(Color.white);
		subtitle.setForeground(Color.white);

		// Adding the display and control panels into the frame
		frame.add(displayPanel);
		frame.add(controlPanel, BorderLayout.SOUTH);

		// Formatting for the grid layout in order to place the album cover
		// Starts at coordinate (0,0) with width and height of 4
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 4;
		gbc.gridwidth = 4;
		
		// Creates a "cushion" around the cell
		gbc.insets = new Insets(10, 10, 10, 10);
		
		// Fills the cell once the album cover is added
		gbc.fill = GridBagConstraints.BOTH;
		
		//Adds album cover component to the display panel using gbc layout
		displayPanel.add(albumCover, gbc);
		
		// Formatting for the addition of the title
		gbc.gridx = 4;
		gbc.gridy = 2;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weighty=1;
		gbc.fill = GridBagConstraints.NONE;
		
		// Moves the title to the bottom of the cell
		gbc.anchor = GridBagConstraints.SOUTH;
		
		// Adds the title to the display panel using gbc layout
		displayPanel.add(title, gbc);
		
		// Formatting for the subtitle
		gbc.gridy=3;
		gbc.weighty=0.05;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTH;
		displayPanel.add(subtitle, gbc);
		
		// Adds all of the components to the control panel that control the music player
		gbc.gridx=1;
		gbc.gridy=0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx=0;
		gbc.weighty=0;
		gbc.gridwidth=1;
		gbc.gridheight=1;
		gbc.anchor = GridBagConstraints.CENTER;
		controlPanel.add(shuffle, gbc);
		gbc.gridx=2;
		controlPanel.add(previous, gbc);
		gbc.gridx=3;
		controlPanel.add(play, gbc);
		gbc.gridx=4;
		controlPanel.add(skip, gbc);
		gbc.gridx=5;
		controlPanel.add(volume, gbc);
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.gridx=6;
		controlPanel.add(volumeSlider, gbc);
		
		gbc.gridx=1;
		gbc.gridy=1;
		gbc.gridwidth=6;
		// Creates a slider with the custom graphics 
		seek = new JSlider() {
			@Override
            public void updateUI() {
                setUI(new SliderUI());
            }
		};

		// Sets slider 
		seek.setMinimum(0);
		seek.setValue(0);
		seek.setOpaque(false);
		controlPanel.add(seek, gbc);
		
	
	}
	
	public void showFrame() { // This method does the necessary processes in order to show the frame
		// Setting background colour and frame size of the frame
		frame.getContentPane().setBackground(Color.black);
		frame.setSize(800,700);	
		// Shows the frame
		frame.setVisible(true);
		// Closes the frame and stops the program when the exit button is pressed
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void createButton (ArrayList<JButton> songButtons, ArrayList<JLabel> songLength) { // This method passes through two parameters, and displays the song buttons created from the main class on the display panel

		// Formats the buttons
		gbc.gridy=6;
		gbc.gridheight=1;
		gbc.weightx=0;
		gbc.insets = new Insets(0,20,0,20);
		
		for (i=0;i<songButtons.size();i++) { // Iterates through each button in arraylist songButtons
			// Formats the placement of the buttons
			gbc.gridx=0;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.gridwidth=10;
			// Adds the buttons one by one, as the loop iterates, to the display panel using gbc layout
			displayPanel.add(songButtons.get(i), gbc);
			
			// Formats the placement of the song duration times to be placed next to the buttons
			gbc.gridx=11;
			gbc.gridwidth=1;
			gbc.weighty=1;
			// Adds the song durations to the display panel using gbc layout
			displayPanel.add(songLength.get(i), gbc);
			// As the loop iterates, goes down the display panel by adding 1 to the y coordinate
			gbc.gridy+=1;
			
			// Sets action listeners for each of the song buttons, which allows for the program to detect when it is clicked 
			songButtons.get(i).addActionListener(this);
		}
		// Calls the method which shows the frame
		showFrame();

	}

	public void volumeSlider() {
		pause();
		// Gets access to the properties of the clip allowing the program to change volume
		fc=(FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
		// Adds an event listener to the slider, to detect when it changes volumes
		if (reset==true) {
			currentVolume=volumeSlider.getValue();
		}
		if (reset==false) {
			currentVolume=0;
		}
		fc.setValue(currentVolume);
		volumeSlider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				// Sets current volume to the value of the slider 
				currentVolume = volumeSlider.getValue();
				if (currentVolume==-30) {
					// When the current volume is 
					currentVolume=-80; 
				}
				// Sets the volume of the clip 
				fc.setValue(currentVolume);
				// Sets previous volume to current volume to save the volume when the method is called again
			}
		});
		resume();
	}
	
	public void seekSlider() {
		// Creates a new thread for the process of the slider
		myRunnable = new Runnable() {
	        public void run() {
	        	while(running) {
	        		synchronized(pauseLock) // Synchronizes the thread with the pause method
		        	{ 
	        			// Checks to see if thread is not running, then breaks
	        			if (!running) { 
	                        break;
	                    }
	        			// Checks to see if thread is paused
	        			if (paused) {
	        				try {
	                            synchronized (pauseLock) {
	                            	// If so, pauses the thread
	                                pauseLock.wait(); 
	                            }
	                        } catch (InterruptedException ex) {
	                            break;
	                        }
	        			}
	        			if (!running) { // may have changed while waiting to
	                        // synchronize on pauseLock
	                        break;
	                    }
		        	}
	        		try {
	        			// If the program has processed this code more than once, then it will remove the last time stamp from the control panel
	        			if (reset==true) {
	        				controlPanel.remove(currentPosition);
	        			}
	        			
	        			// Following code calculates the current position in the song and displays it on the control panel
	        			int seconds=seekValue;
	        			minutes=0;
	        			if (seekValue>60) {
	        				minutes=(int) ((seekValue%3600)/60);
	        				seconds = (int) (seekValue%60);
	        			}
	        			String timeString = String.format("%2d:%02d", minutes, seconds);
	        			currentPosition= new JLabel(timeString);
	        			currentPosition.setForeground(Color.white);
	        			gbc.gridy=1;
	        			gbc.gridx=0;
	        			controlPanel.add(currentPosition, gbc);
	        			controlPanel.revalidate();
	        			// Sets the value of the seek bar to the current position
		            	seek.setValue(seekValue);
		    			seekValue++;
		    			reset=true;
		    			// Sleeps the thread for one second, and then updates the seek bar again
		    			Thread.sleep(1000);
		            } 
		            catch (InterruptedException e) {
		                e.printStackTrace();
		            }
	        	}
	        }
	    };
	    
	    // Sets the flag of the thread to true
		running=true;
		// Creates object from the thread 
		updateSeekBar = new Thread(myRunnable);
		// If the program hasn't already run through this code, then starts the thread
		if (restart==false) {
			updateSeekBar.start();
		}
		restart=true;
		

		// Calculates the total duration of the audio in seconds 
		int length = (int)clip.getMicrosecondLength();
		int totalSeconds = length/1000000;

		// Sets the maximum of the seek bar to the total duration
		seek.setMaximum(totalSeconds);
		seek.setValue(0);
		seekValue=0;
		
		// Adds mouse listeners to the seek bar
	    seek.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}
			
			// When the mouse releases the seek bar, it will find that psotion on the seek bar and change the clip position to match it
			@Override
			public void mouseReleased(MouseEvent e) {
				clip.setMicrosecondPosition(seek.getValue()*1000000);
				seekValue=seek.getValue();
				//resume();
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {	}
	    });
	}

	//
	public void resume() {
		// Synchronizes the thread with this method to unpause it 
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll(); // Unblocks thread
        }
    }
	
	public void pause() {
		// Method that pauses the created thread 
        paused = true;
    }
	
	public void playToPause() { // Changes the play button to a pause button when the music is played
		gbc.gridx=3;
		gbc.gridy=0;
		//gbc.insets = new Insets(10, 10, 10, 10);
		controlPanel.add(pause, gbc);
		play.setVisible(false);
		pause.setVisible(true);
		controlPanel.revalidate();
	}
	
	public void playMusic(String musicName) { // This method plays the music and formats the buttons
		// Finds the length of the array list
		length = songButtonList.size();
		
		// Calls the method which changes the play button to a pause button
		playToPause();
		
		for (int k=0; k<length;k++) {
			// Creates the time stamp for the total length of the song
			if (songFiles.get(k).getName().equals(musicName)) { // Finds the file in song files that matches the song playing 
				// if the program has already played a song before, then it will remove the old timestamp and replace it with the new one
				if (restart==true) {
					controlPanel.remove(endTime);
				}
				// Adds the end time stamp to the control panel
				endTime=new JLabel();
				endTime.setText(songLength.get(k).getText());
				endTime.setForeground(Color.white);
			}
		}
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.gridx=8;
		gbc.gridy=1;
		gbc.gridwidth=1;
		gbc.gridheight=1;
		controlPanel.add(endTime, gbc);
		
		// Tries to stop the clip if there is one playing
		try {
			clip.stop();
		}
		catch (Exception noSong){ // If there is no clip playing, then this is the first time the music player plays a song
			System.out.println("Starting music...");
		}
		
		for (int j=0;j<length;j++) { // Iterates through each button in songbutton 
			// Initializes the song variable to each button in the arraylist
			song = songButtonList.get(j);
			
			// Formats the button when the method is called again in order to reset the formatting from when the song is highlighted
			song.setForeground(Color.white);
			song.setOpaque(false);
			song.setContentAreaFilled(false);
			
			if (songFiles.get(j).getName().equals(musicName)) { // If the name of the file equals the button that is pressed, then highlight the button 
				// Formats the button to have a purple background and be opaque
				song.setBackground(purple);
				song.setOpaque(true);
				song.setContentAreaFilled(true);
				audioFileLength = songFiles.get(j).length();
				
			}
		}
	
		try {
			// Finds the default path for the program
			Path path = FileSystems.getDefault().getPath("").toAbsolutePath();
			// Concatenates the path and the music name inside of the song folders for the path to the song file
			musicPath = new File(path+"/songs/"+musicName);
			if (musicPath.exists()) {
				// Creates audioinput stream for the file at the music path
				AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
				// Initiazlizes a default audio clip
				clip = AudioSystem.getClip();
				clip.flush();
				// Opens and starts a new clip using the file found in audioinputstream
				clip.open(audioInput);
				clip.start();
				// Creates a listener for the clip
				LineListener listener = new LineListener() {
					@Override // This is necessary because the following code overrides any other code that is already running
					public void update(LineEvent event) { // This method plays the next song in the playlist once the previous song has finished playing
						// If the clip stops and the clip is a the end of it's duration, then it skips to the next song
						if (event.getType().equals(Type.STOP) && clip.getMicrosecondLength()==clip.getMicrosecondPosition()) {
							skip.doClick();
						}
					}
				};
				clip.addLineListener(listener);	
				// Calls the volume slider method
				volumeSlider();
				seekSlider();
			}
			else { // If it does not find a file in the song folder, prints error message
				System.out.println("Can't find file.");
			}	
		}
		catch (Exception noFile) { // If the clip cannot be created, prints an error message
			System.out.println("Clip cannot be created");
		}
	}
	
	@Override                                       
	public void actionPerformed(ActionEvent e) { // Method checks the action event and performs actions for each button
		// Initializes the length of the song button arraylist since this is used repeatedly through this method
		length = songButtonList.size();
		for (int j=0;j<length;j++) { // Iterates for each file in array list
			if (e.getSource().equals(songButtonList.get(j))) { // Checks to see which button called the action event 
				// Calls method to change play button to pause button
				playToPause();
				// Finds the name of the audio file to locate the path in the playmusic method
				musicName=songFiles.get(j).getName();
				gbc.insets = new Insets(10, 10, 10, 10);
				playMusic(musicName);
			}
		}
		
		// If the action event equals pause
		if (e.getSource().equals(pause)) {
			try {
				// Changes pause button to play button
				gbc.gridx=3;
				gbc.gridy=0;
				gbc.insets = new Insets(10, 10, 10, 10);
				controlPanel.add(play, gbc);
				pause.setVisible(false);
				play.setVisible(true);
				controlPanel.revalidate();
				// Stores the exact time where the clip is stopped 
				clipTimePosition = clip.getMicrosecondPosition();
				clip.stop();
				pause();
			}
			catch (Exception noClip) { // Handles the exception error
				System.out.println("Cannot pause a clip that isn't playing.");
			}
		}
		
		// Checks to see if action event is from the play button
		else if (e.getSource().equals(play)) {
			// Resumes the seek bar thread
			resume();
			try {
				// Tries to first play the previous clip that was playing from the paused time
				clip.setMicrosecondPosition(clipTimePosition);
				seekValue=(int) clipTimePosition/1000000;
				clip.start();
			}
			catch (Exception songNotPicked){
				// If there was no previous clip, then starts the music from the first song in the playlist
				musicName = songFiles.get(0).getName();
				playMusic(musicName);
			}
			// Calls method to change play button to pause button
			playToPause();
		}

		// Action event for when user clicks previous button
		else if (e.getSource().equals(previous)) {
			try {
				for (int j=0;j<length;j++) {// Iterates through each file in arraylist
					if (musicName.equals(songFiles.get(j).getName()) && j>0) {
						// Plays the previous song in the arraylist by decrementing index by one
						musicName = songFiles.get(j-1).getName();
						playMusic(musicName);
						break;
					}
					// If the song playing currently has index 0,then it will instead play the last song in the array
					else if (musicName.equals(songFiles.get(j).getName()) && j==0) {
						musicName = songFiles.get(length-1).getName();
						playMusic(musicName);
						break;
					}
				}
			}
			catch (Exception noSong) { // Displays error message when the user tries to click previous but there is no song chosen previously
				System.out.println("No song selected.");
			}
		}
		
		// Action event for when user clicks the skip button
		else if (e.getSource().equals(skip)) {
			try {
				for (int j=0;j<length;j++) { // Iterates through each song in arraylist
					if (musicName.equals(songFiles.get(j).getName()) && j<length-1) {
						// Stores the name of the next element in the arraylist by incrementing index by one
						musicName = songFiles.get(j+1).getName();
						playMusic(musicName);
						break;
					}
					else if (musicName.equals(songFiles.get(j).getName()) && j==length-1) {
						// If the song playing is the last one in the arraylist, then loops back to the beginning of the arraylist and plays that song
						musicName = songFiles.get(0).getName();
						playMusic(musicName);
						break;
					}
				}
			}
			catch (Exception noSong) {
				System.out.println("No song selected.");
			}
		}
		
		// Action event for shuffle button
		else if (e.getSource().equals(shuffle)) {
			// Generates a random integer from 0 to the length of the array
			int randomInt = (int)(Math.random()*length);
			// Plays song in the position of this random integer
			musicName=songFiles.get(randomInt).getName();
			playMusic(musicName);	
		}
	}
}