package com.company.Model;

import static com.company.Main.DRAW_AREA;
import static com.company.Main.RANDOM;

/**
 * Fish class.
 *
 * Stores the details of all fish.
 *
 * @author Ian Weeks 6204848
 * @version 7.0
 */
public class Fish
{
    private float[][] school, target, colour;
    private float[] schoolCount;
    private int numFish, numSchools, fishEaten;

    /**
     * Fish initialisation.
     *
     * @param numFish       The number of fish.
     * @param numSchools    The number of fish schools.
     */
    public Fish( int numFish, int numSchools )
    {
        this.numFish = numFish;
        this.numSchools = numSchools;
        this.schoolCount = new float[numSchools];

        school = new float[7][this.numFish];
        target = new float[3][this.numSchools];
        colour = new float[3][this.numSchools];

        //Random fish position initialisation.
        for (int i = 0; i < numFish; i++)
        {
            school[0][i] = RANDOM.nextInt( DRAW_AREA );
            school[1][i] = RANDOM.nextInt( DRAW_AREA );
            school[2][i] = RANDOM.nextInt( DRAW_AREA );

            //Divide fish into groups
            school[6][i] = i % this.numSchools;
        }

        //School initialisation.
        for (int i = 0; i < this.numSchools; i++)
        {
            //Give each group a random starting target.
            target[0][i] = RANDOM.nextInt( DRAW_AREA );
            target[1][i] = RANDOM.nextInt( DRAW_AREA );
            target[2][i] = RANDOM.nextInt( DRAW_AREA );

            //Give each group a random colour.
            colour[0][i] = rColor();
            colour[1][i] = rColor();
            colour[2][i] = rColor();
        }
    }

    /**
     * Get the school array.
     *
     * @return The school array.
     */
    public float[][] getSchool()
    {
        return school;
    }

    /**
     * Get the target array.
     *
     * @return The target array.
     */
    public float[][] getTarget()
    {
        return target;
    }

    /**
     * Get the colour array.
     *
     * @return The colour array.
     */
    public float[][] getColour()
    {
        return colour;
    }

    /**
     * Get the number of fish.
     *
     * @return The number of fish.
     */
    public int getNumFish()
    {
        return numFish;
    }

    /**
     * Get the number of schools.
     *
     * @return The number of schools.
     */
    public int getNumSchools()
    {
        return numSchools;
    }

    /**
     * Get the size of each group of fish.
     *
     * @return The size of each group of fish.
     */
    public float[] getSchoolCount()
    {
        return schoolCount;
    }

    /**
     * Get the number of fish that have been eaten by sharks.
     *
     * @return The number of fish eaten by sharks.
     */
    public int getFishEaten()
    {
        return fishEaten;
    }

    public void setFishEaten(int fishEaten)
    {
        this.fishEaten = fishEaten;
    }

    /**
     * Get a random number to generate a bright colour.
     *
     * @return A random number used to generate a bright colour.
     */
    private float rColor()
    {
        return RANDOM.nextFloat() / 2f + 0.5f;
    }
}
