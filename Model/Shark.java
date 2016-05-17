package com.company.Model;

import static com.company.Main.DRAW_AREA;
import static com.company.Main.RANDOM;

/**
 * Shark class.
 *
 * Stores details of all starks.
 *
 * @author Ian Weeks 6204848
 * @version 7.0
 */
public class Shark
{
    private int numSharks;

    //Today I learned that a group of sharks is called a shiver....
    private float[][] shiver;

    /**
     * Constructor for shark class.
     *
     * @param numSharks The number of sharks.
     * @param numFish   The number of fish.
     */
    public Shark( int numSharks, int numFish )
    {
        this.numSharks = numSharks;

        shiver = new float[7][this.numSharks];

        //Shiver initialisation.
        for (int i = 0; i < this.numSharks; i++)
        {
            shiver[0][i] = RANDOM.nextInt( DRAW_AREA );
            shiver[1][i] = RANDOM.nextInt( DRAW_AREA );
            shiver[2][i] = RANDOM.nextInt( DRAW_AREA );

            //Random target for each shark.
            int fishTarget = RANDOM.nextInt( numFish );
            shiver[6][i] = fishTarget;
        }
    }

    /**
     * Get the number of sharks.
     *
     * @return The number of sharks.
     */
    public int getNumSharks()
    {
        return numSharks;
    }

    /**
     * Get the shark (shiver) array.
     *
     * @return The shark (shiver) array.
     */
    public float[][] getShiver()
    {
        return shiver;
    }
}
