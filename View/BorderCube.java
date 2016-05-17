package com.company.View;

import com.jogamp.opengl.GL2;

/**
 * Border cube.
 *
 * This class just hides the ugly cube vertices.
 *
 * @author Ian Weeks 6204848
 * @version 7.0
 */
public class BorderCube
{
    private float size;

    /**
     * Constructor for the BorderCube class.
     *
     * @param size The size of the cube.
     */
    public BorderCube(float size)
    {
        this.size = size;
    }

    /**
     * Render this cube.
     *
     * @param gl OpenGL interface class.
     */
    public void renderBorderCube(GL2 gl)
    {
        gl.glPushMatrix();
        gl.glTranslatef(10f, 0.0f, 0.0f);
        gl.glBegin(GL2.GL_LINES);

        //3d Border cube vertices.
        //Red
        gl.glColor3f(1.0f, 0.0f, 0.0f);
        gl.glVertex3f( size,  size, -size);
        gl.glVertex3f(-size,  size, -size);
        gl.glVertex3f(-size,  size,  size);
        gl.glVertex3f( size,  size,  size);

        //White
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        gl.glVertex3f(-size, -size, -size);
        gl.glVertex3f( size, -size, -size);

        //And back to red.
        gl.glColor3f(1.0f, 0.0f, 0.0f);
        gl.glVertex3f( size, -size,  size);
        gl.glVertex3f(-size, -size,  size);
        gl.glVertex3f(-size, -size, -size);
        gl.glVertex3f( size, -size, -size);

        gl.glVertex3f( size,  size,  size);
        gl.glVertex3f(-size,  size,  size);
        gl.glVertex3f(-size, -size,  size);
        gl.glVertex3f( size, -size,  size);

        gl.glVertex3f( size, -size, -size);
        gl.glVertex3f(-size, -size, -size);
        gl.glVertex3f(-size,  size, -size);
        gl.glVertex3f( size,  size, -size);

        gl.glVertex3f(-size,  size,  size);
        gl.glVertex3f(-size,  size, -size);
        gl.glVertex3f(-size, -size, -size);
        gl.glVertex3f(-size, -size,  size);

        gl.glVertex3f( size,  size, -size);
        gl.glVertex3f( size,  size,  size);
        gl.glVertex3f( size, -size,  size);
        gl.glVertex3f( size, -size, -size);

        gl.glEnd();
        gl.glPopMatrix();
    }
}
