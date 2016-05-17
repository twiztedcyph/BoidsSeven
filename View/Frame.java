package com.company.View;

import com.company.Model.Fish;
import com.company.Model.Shark;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static com.company.Main.FULLSCREEN_HEIGHT;
import static com.company.Main.FULLSCREEN_WIDTH;

/**
 * Singleton Frame class.
 * Base container for GUI.
 *
 * @author Ian Weeks
 * @version 7.0
 */
public class Frame extends JFrame
{
    private static Frame ourInstance = null;
    private OpenGLCanvas openGLCanvas;

    /**
     * Get the instance of this Frame.
     *
     * @return The instance of this Frame.
     */
    public static Frame getInstance()
    {
        //check if the frame is instantiated.
        if ( ourInstance == null )
        {
            //If not then instantiate it.
            ourInstance = new Frame();
        }
        //Return the instance of this Frame.
        return ourInstance;
    }

    /**
     * Private Frame constructor.
     */
    private Frame()
    {
        float SCREEN_PERCENTAGE = 0.7f;

        ProgramInput programInput = new ProgramInput();
        int choice = JOptionPane.showConfirmDialog(null, programInput,
                                                   "Simulation parameter input",
                                                   JOptionPane.OK_CANCEL_OPTION,
                                                   JOptionPane.PLAIN_MESSAGE);
        if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION)
        {
            System.exit(0);
        }

        Fish fish = new Fish(programInput.getNumFish(), programInput.getNumSchools());
        Shark shark = new Shark(programInput.getNumSharks(), fish.getNumFish());

        //Second panel (settings and information) init and add to this frame.
        InfoPanel infoPanel = InfoPanel.getInstance();
        this.add( infoPanel, BorderLayout.EAST );

        //First panel (OpenGL) init and add to this frame.
        openGLCanvas = OpenGLCanvas.getInstance(fish, shark, infoPanel.getInfoDisplay());
        final JPanel graphicsPanel = (JPanel) this.getContentPane();
        graphicsPanel.setPreferredSize(new Dimension( (int) (FULLSCREEN_WIDTH * SCREEN_PERCENTAGE), FULLSCREEN_HEIGHT) );
        graphicsPanel.add(openGLCanvas);

        //Listen for window closing event to ensure proper animator shutdown.
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e )
            {
                openGLCanvas.stopAnimation();
            }
        });

        //Frame settings.
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setTitle("Boids iteration seven");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);

        //Once all the init and settings is complete, start the animator.
        openGLCanvas.startAnimation();
    }
}
