package com.company;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.company.Frame.FULLSCREEN_WIDTH;
import static com.company.Frame.FULLSCREEN_HEIGHT;

/**
 * Main canvas for OpenGL (JOGL) rendering.
 *
 * Singleton class.
 *
 * @author Ian Weeks 6204848
 */
public class Canvas extends GLCanvas implements GLEventListener, KeyListener
{
    //The
    private final int FPS = 60;
    // This canvas will always fill 70% of the screen's width.
    private final float SCREEN_PERCENTAGE = 0.7f;
    // The single allowed instance of this class.
    private static Canvas ourInstance = null;
    //FPS Animator for some control over the FPS of this canvas.
    private final FPSAnimator ANIMATOR;
    //Text rendering object.
    private final TextRenderer textRenderer;

    /**
     * Get the existing instance of this class.  If none exists a new one will be created.
     *
     * @return An instance of the Canvas class.
     */
    public static Canvas getInstance()
    {
        return ourInstance == null ? new Canvas() : ourInstance;
    }

    private Canvas()
    {
        // Init options for GLCanvas.
        this.setPreferredSize(new Dimension((int)(FULLSCREEN_WIDTH * SCREEN_PERCENTAGE), FULLSCREEN_HEIGHT));
        // To allow receiving of key/mouse events.
        this.setFocusable(true);
        // Add the mouse event listeners.
        this.addListeners();

        // Create an animator that calls the display() method at the specified FPS.
        ANIMATOR = new FPSAnimator( this, FPS, true );
        // Set the animator to record the current FPS;
        ANIMATOR.setUpdateFPSFrames(60, null);
        //Font to be used for text rendering.
        Font myFont = new Font("SansSerif", Font.BOLD, 20);
        textRenderer = new TextRenderer(myFont);
    }

    private void addListeners()
    {
        this.addMouseListener( new MouseAdapter()
        {
            @Override
            public void mouseClicked( MouseEvent e )
            {
                super.mouseClicked( e );
            }

            @Override
            public void mousePressed( MouseEvent e )
            {
                super.mousePressed( e );
            }

            @Override
            public void mouseReleased( MouseEvent e )
            {
                super.mouseReleased( e );
            }
        } );

        this.addMouseMotionListener( new MouseAdapter()
        {
            @Override
            public void mouseDragged( MouseEvent e )
            {
                if ( SwingUtilities.isLeftMouseButton( e ) )
                {
//                    yRot += (e.getX() - xStart) * 0.2f;
//                    xRot += (e.getY() - yStart) * 0.2f;
                }
//                xStart = e.getX();
//                yStart = e.getY();
                super.mouseDragged( e );
            }

            @Override
            public void mouseMoved( MouseEvent e )
            {

                super.mouseMoved( e );
            }
        } );
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable)
    {

    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable)
    {

    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable)
    {

    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3)
    {

    }

    @Override
    public void keyTyped(KeyEvent e)
    {

    }

    @Override
    public void keyPressed(KeyEvent e)
    {

    }

    @Override
    public void keyReleased(KeyEvent e)
    {

    }

    public void startAnimation()
    {
        ANIMATOR.start();
    }

    public void stopAnimation()
    {
        //Done in a separate thread to ensure the animator is stopped before the program exits.
        new Thread()
        {
            @Override
            public void run()
            {
                ANIMATOR.stop();
            }
        }.start();
    }
}
