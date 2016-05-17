package com.company.View;

import com.company.Main;
import com.company.Model.Fish;
import com.company.Model.Shark;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.awt.TextRenderer;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Renderer class.
 * <p>
 * All drawing to the screen done here.
 *
 * @author Ian Weeks 6204848
 * @version 7.0
 */
public class Renderer
{
    private final TextRenderer textRenderer;
    private float[][] school, sColour, shiver;
    private int numFish, numSharks;
    private float yRot, xRot;
    private float yRotSpeed, xRotSpeed;
    private BorderCube borderCube;

    /**
     * Renderer constructor.
     *
     * @param fish         Reference to the fish object.
     * @param shark        Reference to the shark object.
     * @param textRenderer Jogl's text rendering object.
     */
    public Renderer(Fish fish, Shark shark, TextRenderer textRenderer)
    {
        this.school = fish.getSchool();
        this.sColour = fish.getColour();
        this.shiver = shark.getShiver();

        this.numFish = fish.getNumFish();
        this.numSharks = shark.getNumSharks();

        this.textRenderer = textRenderer;

        final float CUBE_SIZE = 50f;
        borderCube = new BorderCube(CUBE_SIZE);
    }

    /**
     * Render all of the simulation data here.
     *
     * @param drawable Jogl's auto drawable.
     * @param aspect   Previously calculated aspect ratio.
     * @param fps      Current frames per second.
     * @param sDuration The start time of the program.
     */
    public void renderAll(GLAutoDrawable drawable, float aspect, float fps, long sDuration)
    {
        final float SIZE_CORRECTION_3D = 0.1f, SIZE_CORRECTION_2D = 0.25f;
        //Get the required gl and glu objects.
        GL2 gl = drawable.getGL().getGL2();
        GLU glu = GLU.createGLU(gl);
        //Note this is only for the left (OpenGL) panel.
        //3d Rendering
        //Projection matrix
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(75.0, aspect, 1.0f, 500.0);
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glLoadIdentity();
        //Camera position.
        glu.gluLookAt(15.0f, 0.0f, 0.0f,
                      15.0f, 0.0f, -120f,
                      0.0f, 1.0f, 0.0f);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glTranslatef(15f, 0.0f, -130.0f);
        gl.glRotatef(yRot, 0.0f, 1.0f, 0.0f);
        gl.glRotatef(xRot, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(180, 1.0f, 0.0f, 0.0f);
        yRot += yRotSpeed;
        xRot += xRotSpeed;

        borderCube.renderBorderCube(gl);

        gl.glPointSize(2);
        gl.glBegin(GL.GL_POINTS);
        gl.glPushMatrix();

        //Draw the 3d fish.
        for (int i = 0; i < numFish; i++)
        {
            gl.glColor4f(sColour[0][(int) school[6][i]], sColour[1][(int) school[6][i]],
                         sColour[2][(int) school[6][i]], 1.0f);
            gl.glVertex3f((school[0][i] - 500) * SIZE_CORRECTION_3D, (school[1][i] - 500) * SIZE_CORRECTION_3D,
                          (school[2][i] - 500) * SIZE_CORRECTION_3D);
        }
        gl.glPopMatrix();
        gl.glEnd();


        gl.glPushMatrix();

        //Lighting for shark spheres.
        float SHINE_ALL_DIRECTIONS = 1;
        float[] lightPos = {-30, 0, 0, SHINE_ALL_DIRECTIONS};
        float[] lightColorAmbient = {0.7f, 0.7f, 0.7f, 0.7f};
        float[] lightColorSpecular = {1f, 1f, 1f, 1f};

        //Set light parameters.
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPos, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightColorAmbient, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, lightColorSpecular, 0);

        //Enable lighting in GL.
        gl.glEnable(GL2.GL_LIGHT1);
        gl.glEnable(GL2.GL_LIGHTING);

        float[] rgba = {1.0f, 0.0f, 0.0f};
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, rgba, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, rgba, 0);
        gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 1.0f);

        //Create the shark sphere.
        GLUquadric sharkSphere = glu.gluNewQuadric();
        glu.gluQuadricTexture(sharkSphere, true);
        glu.gluQuadricDrawStyle(sharkSphere, GLU.GLU_FILL);
        glu.gluQuadricNormals(sharkSphere, GLU.GLU_FLAT);
        glu.gluQuadricOrientation(sharkSphere, GLU.GLU_OUTSIDE);

        final float radius = 0.7f;
        final int slices = 6;
        final int stacks = 6;

        //Draw 3d sharks
        for (int i = 0; i < numSharks; i++)
        {
            gl.glPushMatrix();
            gl.glTranslatef(((shiver[0][i] - 500) * SIZE_CORRECTION_3D), (shiver[1][i] - 500) * SIZE_CORRECTION_3D,
                            (shiver[2][i] - 500) * SIZE_CORRECTION_3D);
            glu.gluSphere(sharkSphere, radius, slices, stacks);
            gl.glPopMatrix();
        }
        gl.glDisable(GL2.GL_LIGHT1);
        gl.glDisable(GL2.GL_LIGHTING);
        glu.gluDeleteQuadric(sharkSphere);
        gl.glPopMatrix();

        //Prep for 2d display
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(0, (int) (Main.FULLSCREEN_WIDTH * 0.7), Main.FULLSCREEN_HEIGHT, 0, 0.0, 30.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        //2d borders
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        //Top
        gl.glPushMatrix();
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(20.0f, 300.0f);
        gl.glVertex2f(300.0f, 300.0f);
        gl.glVertex2f(300.0f, 300.0f);
        gl.glVertex2f(300.0f, 20.0f);
        gl.glVertex2f(300.0f, 20.0f);
        gl.glVertex2f(20.0f, 20.0f);
        gl.glEnd();
        gl.glPopMatrix();
        //Middle
        gl.glPushMatrix();
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(20.0f, 620.0f);
        gl.glVertex2f(300.0f, 620.0f);
        gl.glVertex2f(300.0f, 620.0f);
        gl.glVertex2f(300.0f, 340.0f);
        gl.glVertex2f(300.0f, 340.0f);
        gl.glVertex2f(20.0f, 340.0f);
        gl.glEnd();
        gl.glPopMatrix();
        //Bottom
        gl.glPushMatrix();
        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex2f(20.0f, 940.0f);
        gl.glVertex2f(300.0f, 940.0f);
        gl.glVertex2f(300.0f, 940.0f);
        gl.glVertex2f(300.0f, 660.0f);
        gl.glVertex2f(300.0f, 660.0f);
        gl.glVertex2f(20.0f, 660.0f);
        gl.glEnd();
        gl.glPopMatrix();

        gl.glTranslatef(160.0f, 160.0f, 0.0f);

        //2d Rendering
        gl.glPointSize(1);

        gl.glBegin(GL.GL_POINTS);
        gl.glPushMatrix();
        //Draw 2d fish.
        for (int i = 0; i < numFish; i++)
        {
            gl.glColor4f(sColour[0][(int) school[6][i]], sColour[1][(int) school[6][i]],
                         sColour[2][(int) school[6][i]], 1.0f);
            gl.glVertex2f((school[0][i] - 500) * SIZE_CORRECTION_2D, (school[1][i] - 500) * SIZE_CORRECTION_2D);

            gl.glColor4f(sColour[0][(int) school[6][i]], sColour[1][(int) school[6][i]],
                         sColour[2][(int) school[6][i]], 1.0f);
            gl.glVertex2f(((school[0][i] - 500) * SIZE_CORRECTION_2D),
                          ((school[2][i] - 500) * SIZE_CORRECTION_2D) + 320);

            gl.glColor4f(sColour[0][(int) school[6][i]], sColour[1][(int) school[6][i]],
                         sColour[2][(int) school[6][i]], 1.0f);
            gl.glVertex2f(((school[2][i] - 500) * SIZE_CORRECTION_2D),
                          ((school[1][i] - 500) * SIZE_CORRECTION_2D) + 640);
        }
        gl.glPopMatrix();
        gl.glEnd();

        gl.glPointSize(2);

        gl.glBegin(GL.GL_POINTS);
        gl.glPushMatrix();
        gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
        //Draw 2d sharks
        for (int i = 0; i < numSharks; i++)
        {
            gl.glVertex2f((shiver[0][i] - 500) * SIZE_CORRECTION_2D, (shiver[1][i] - 500) * SIZE_CORRECTION_2D);
            gl.glVertex2f(((shiver[0][i] - 500) * SIZE_CORRECTION_2D),
                          ((shiver[2][i] - 500) * SIZE_CORRECTION_2D) + 320);
            gl.glVertex2f(((shiver[2][i] - 500) * SIZE_CORRECTION_2D),
                          ((shiver[1][i] - 500) * SIZE_CORRECTION_2D) + 640);
        }
        gl.glPopMatrix();
        gl.glEnd();
        textRenderer.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
        long time =  System.nanoTime() - sDuration;
        textRenderer.draw(String.format("Elapsed time: %d", SECONDS.convert(time, NANOSECONDS)), 5, 30);
        textRenderer.draw(String.format("Frames Per Second: %.2f", fps), 5, 5);
        textRenderer.endRendering();

        gl.glFlush();
    }

    /**
     * Get the current Y rotation.
     *
     * @return The current Y rotation.
     */
    public float getYRot()
    {
        return yRot;
    }

    /**
     * Set the y rotation.
     *
     * @param yRot The y rotation.
     */
    public void setYRot(float yRot)
    {
        this.yRot = yRot;
    }

    /**
     * Get the current x rotation.
     *
     * @return The current x rotation.
     */
    public float getXRot()
    {
        return xRot;
    }

    /**
     * Set the x rotation.
     *
     * @param xRot The x rotation.
     */
    public void setXRot(float xRot)
    {
        this.xRot = xRot;
    }

    /**
     * Set the y rotation speed.
     *
     * @param yRotSpeed The y rotation speed.
     */
    public void setYRotSpeed(float yRotSpeed)
    {
        this.yRotSpeed = yRotSpeed;
    }

    /**
     * Set the x rotation speed.
     *
     * @param xRotSpeed The x rotation speed.
     */
    public void setXRotSpeed(float xRotSpeed)
    {
        this.xRotSpeed = xRotSpeed;
    }
}
