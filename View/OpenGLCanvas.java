package com.company.View;

import com.company.Controller.Simulator;
import com.company.Model.Fish;
import com.company.Model.Shark;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import static com.company.Main.FULLSCREEN_HEIGHT;
import static com.company.Main.FULLSCREEN_WIDTH;

/**
 * Main canvas for OpenGL (JOGL) rendering.
 * <p>
 * Singleton class.
 *
 * @author Ian Weeks 6204848
 * @version 7.0
 */
public class OpenGLCanvas extends GLCanvas implements GLEventListener, KeyListener
{
    //The single allowed instance of this class.
    private static OpenGLCanvas ourInstance = null;
    //FPS Animator for some control over the FPS of this canvas.
    private final FPSAnimator ANIMATOR;
    //Simulation class.
    Simulator simulator;
    //Render class
    com.company.View.Renderer renderer;
    //DeltaT and FPS
    private long lastTime, startTime;
    private float aspect;
    //Used to pause the program.
    private boolean paused = false;
    //Mouse pointer position storage.
    private int xStart;
    private int yStart;
    //Label used to display information and statistics.
    private JLabel infoDisplay;
    private Fish fish;
    private Shark shark;

    /**
     * Get the existing instance of this class.  If none exists a new one will be created.
     *
     * @param fish  Reference to fish object.
     * @param shark Reference to shark object.
     * @param infoDisplay Information and statistics display label.
     * @return An instance of the OpenGLCanvas class.
     */
    public static OpenGLCanvas getInstance(Fish fish, Shark shark, JLabel infoDisplay)
    {
        if (ourInstance == null)
        {
            ourInstance = new OpenGLCanvas(fish, shark, infoDisplay);
        }
        return ourInstance;
    }

    private OpenGLCanvas(Fish fish, Shark shark, JLabel infoDisplay)
    {
        //This canvas will always fill 70% of the screen's width.
        float SCREEN_PERCENTAGE = 0.7f;

        this.infoDisplay = infoDisplay;

        this.fish = fish;
        this.shark = shark;

        //Get the time now.
        lastTime = System.nanoTime();
        startTime = System.nanoTime();

        //Init options for GLCanvas.
        this.setPreferredSize(new Dimension((int) (FULLSCREEN_WIDTH * SCREEN_PERCENTAGE), FULLSCREEN_HEIGHT));
        this.addGLEventListener(this);
        this.addKeyListener(this);
        //To allow receiving of key/mouse events.
        this.setFocusable(true);
        //Add the mouse event listeners.
        this.addListeners();
        //Text renderer init.
        TextRenderer textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 20));
        //The required number of frames per second for this application.
        final int FPS = 60;
        //Create an animator that calls the display() method at the specified FPS.
        ANIMATOR = new FPSAnimator(this, FPS, true);
        //Set the animator to record the current FPS;
        ANIMATOR.setUpdateFPSFrames(60, null);
        simulator = new Simulator(fish, shark);
        renderer = new Renderer(fish, shark, textRenderer);
    }

    private void addListeners()
    {
        //Mouse listener for mouse button events.
        this.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                super.mouseClicked(e);
            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                //Record the position of the mouse when the user clicks down.
                xStart = e.getX();
                yStart = e.getY();
                super.mousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                //Not used. Pass to super.
                super.mouseReleased(e);
            }
        });

        //Mouse motion for dragging events.
        this.addMouseMotionListener(new MouseAdapter()
        {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                //Smooth and sequential angling of the 3d display area.
                if (SwingUtilities.isLeftMouseButton(e))
                {
                    renderer.setYRot(renderer.getYRot() + (e.getX() - xStart) * 0.2f);
                    renderer.setXRot(renderer.getXRot() + (e.getY() - yStart) * 0.2f);
                }
                xStart = e.getX();
                yStart = e.getY();
                super.mouseDragged(e);
            }

            @Override
            public void mouseMoved(MouseEvent e)
            {
                //Not used. Pass to super.
                super.mouseMoved(e);
            }
        });
    }

    /**
     * Called by the drawable immediately after the OpenGL context is initialized.
     *
     * @param drawable Jogl's auto drawable.
     */
    @Override
    public void init(GLAutoDrawable drawable)
    {
        //Get the graphics context.
        GL2 gl = drawable.getGL().getGL2();
        //Set the clear (background) colour and depth.
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glClearDepth(1.0f);
        //Setup depth testing.
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LESS);
        gl.glHint(GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
        //Blend and smooth lights
        gl.glShadeModel(GLLightingFunc.GL_SMOOTH);

        //Enable Blending
        gl.glEnable(GL.GL_BLEND);
        //Type Of Blending To Perform
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
        //Really Nice Point Smoothing
        gl.glHint(GL2ES1.GL_POINT_SMOOTH_HINT, GL.GL_NICEST);
    }

    /**
     * Notify the listener to perform the release of all OpenGL resources.
     *
     * @param drawable Jogl's auto drawable.
     */
    @Override
    public void dispose(GLAutoDrawable drawable)
    {
        //No need for this as JVM will take care of memory.
    }

    /**
     * Called by the drawable to initiate OpenGL rendering by the client.
     *
     * @param drawable Jogl's auto drawable.
     */
    @Override
    public void display(GLAutoDrawable drawable)
    {
        //deltaT and timer calculation.
        //System nano time for accuracy.
        long now = System.nanoTime();
        long updateLength = now - lastTime;
        lastTime = now;
        int FPS = 60;
        final long OPT_TIME = 1000000000 / FPS;
        float deltaT = updateLength / ((float) OPT_TIME);

        //Updates
        if (!paused)
        {
            simulator.cpuUpdate(deltaT);
        }

        //Display
        renderer.renderAll(drawable, aspect, ANIMATOR.getLastFPS(), startTime);

        infoDisplay.setText(String.format("<html>Number of fish: %d" +
                                                  "<br>Number of schools %d" +
                                                  "<br>Number of sharks %d" +
                                                  "<br>Fish eaten: %d" +
                                                  "<br>School sizes %s" +
                                                  "<br>" +
                                                  "<br>" +
                                                  "<br>" +
                                                  "<br>GPU in use: %s" +
                                          "</html>",
                                          fish.getNumFish(), fish.getNumSchools(), shark.getNumSharks(), fish.getFishEaten(),
                                          Arrays.toString(fish.getSchoolCount()), String.valueOf(simulator.isUseGPU())));
    }

    /**
     * Called by the drawable during the first repaint after the component has been resized.
     *
     * @param drawable Jogl's auto drawable.
     * @param x        x
     * @param y        y
     * @param width    Width
     * @param height   Height
     */
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
    {
        GL2 gl = drawable.getGL().getGL2();
        GLU glu = GLU.createGLU(gl);

        //Divide by 0 protection.
        if (height == 0)
        {
            height = 1;
        }
        //Aspect ratio of the draw area.
        aspect = (float) width / height;

        gl.glViewport(0, 0, width, height);

        //Setup perspective projection, with aspect ratio matches viewport
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);  //choose projection matrix
        gl.glLoadIdentity();   //reset projection matrix
        glu.gluPerspective(75.0, aspect, 1.0f, 500.0);   //fov, aspect, zNear, zFar

        //Enable the model-view transform
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glLoadIdentity(); //reset
    }

    /**
     * Invoked when a key has been typed.
     * See the class description for {@link KeyEvent} for a definition of
     * a key typed event.
     * <p>
     * Not used in this program but must be overridden.
     *
     * @param e KeyEvent.
     */
    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    /**
     * Invoked when a key has been pressed.
     * See the class description for {@link KeyEvent} for a definition of
     * a key pressed event.
     */
    @Override
    public void keyPressed(KeyEvent e)
    {
        switch (e.getKeyCode())
        {
            case KeyEvent.VK_ESCAPE:
                stopAnimation();
                System.exit(0);
                break;
            case KeyEvent.VK_A:
                renderer.setYRotSpeed(0.5f);
                break;
            case KeyEvent.VK_D:
                renderer.setYRotSpeed(-0.5f);
                break;
            case KeyEvent.VK_W:
                renderer.setXRotSpeed(-0.5f);
                break;
            case KeyEvent.VK_S:
                renderer.setXRotSpeed(0.5f);
                break;
            case KeyEvent.VK_R:
                renderer.setXRot(0);
                renderer.setYRot(0);
                break;
            case KeyEvent.VK_P:
                paused = !paused;
                break;
            case KeyEvent.VK_G:

                break;
            default:
                System.out.println(e.getKeyChar() + " is not a valid key.");
        }
    }

    /**
     * Invoked when a key has been released.
     * See the class description for {@link KeyEvent} for a definition of
     * a key released event.
     */
    @Override
    public void keyReleased(KeyEvent e)
    {
        switch (e.getKeyCode())
        {
            case KeyEvent.VK_A:
            case KeyEvent.VK_D:
                renderer.setYRotSpeed(0.0f);
                break;
            case KeyEvent.VK_W:
            case KeyEvent.VK_S:
                renderer.setXRotSpeed(0.0f);
                break;
            case KeyEvent.VK_G:
                simulator.toggleGPU();
                break;
        }
    }

    /**
     * Start the animator animating at the suggested FPS.
     */
    public void startAnimation()
    {
        ANIMATOR.start();
    }

    /**
     * Stop the animator animating.
     */
    public void stopAnimation()
    {
        /*
        Done in a separate thread to ensure the animator is
        stopped no matter what is happening in the main thread.

        Will also clear the OpenCL bindings still in use.
         */
        new Thread()
        {
            @Override
            public void run()
            {
                System.out.println("Stopping animation.");
                ANIMATOR.stop();
            }
        }.start();
        simulator.stop();
    }
}
