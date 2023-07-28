import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicSliderUI;

public class SliderUI extends BasicSliderUI { // This following class is adapted from a source found online 
    // Initializes the variables used to draw in the slider and thumb
	private static final int TRACK_HEIGHT = 8;
    private static final int TRACK_ARC = 5;
    private static final Dimension THUMB_SIZE = new Dimension(12, 12);
    private final RoundRectangle2D.Float trackShape = new RoundRectangle2D.Float();

    @Override
    protected void calculateTrackRect() {
    	// Calculates the size of the track
        super.calculateTrackRect();
        trackRect.y = trackRect.y + (trackRect.height - TRACK_HEIGHT) / 2;
        trackRect.height = TRACK_HEIGHT;

        trackShape.setRoundRect(trackRect.x, trackRect.y, trackRect.width, trackRect.height, TRACK_ARC, TRACK_ARC);
    }

    @Override
    public void paint(final Graphics g, final JComponent c) {
    	// Allows access to graphics in order to paint the custom slider
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paint(g, c);
    }

    @Override
    protected Dimension getThumbSize() {
    	// Returns thumb size of the slider
        return THUMB_SIZE;
    }
    
    @Override
    public void paintTrack(final Graphics g) {
    	// Paints the customer slider
        Graphics2D graphics = (Graphics2D) g;
        Shape clip = graphics.getClip();

        // Paints track background
        graphics.setColor(Color.darkGray);
        graphics.setClip(trackShape);
        trackShape.y += 1;
        graphics.fill(trackShape);
        trackShape.y = trackRect.y;
        graphics.setClip(clip);

        // Paints selected track
        int thumbPos = thumbRect.x + thumbRect.width / 2;
        graphics.clipRect(0, 0, thumbPos, slider.getHeight());
   
        
        graphics.setColor(new Color(158, 106, 154));
        graphics.fill(trackShape);
        graphics.setClip(clip);
    }
        
    @Override
    public void paintThumb(final Graphics g) {
    	// Paints the thumb of the slider
        g.setColor(Color.white);
        g.fillOval(thumbRect.x, thumbRect.y-1, thumbRect.width, thumbRect.height);
    }
}