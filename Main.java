package com.company;

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import com.company.View.Frame;

/**
 * Main class for Boids simulation.
 *
 * @author Ian Weeks 6204848
 * @version 7.0
 */
public class Main
{
    //Screen width and height.  Used by various classes.
    public static final int FULLSCREEN_WIDTH = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    public static final int FULLSCREEN_HEIGHT = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    //Width, height and depth of the 3d draw space.
    public static int DRAW_AREA = 1000;
    //Random used by the program. Initialized once and used statically.
    public static final Random RANDOM = new Random();

    /**
     * Main entry point for Boids simulation.
     *
     * @param args Starting arguments for t`his program. **NOT USED**
     */
    public static void main(String[] args)
    {
        //Nice lambda to get the program started. **See report for equivalent code.**
        SwingUtilities.invokeLater( Frame::getInstance );
    }
}
