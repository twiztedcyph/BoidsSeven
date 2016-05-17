package com.company.Controller;

import com.company.Model.Fish;
import com.company.Model.Shark;
import com.company.Tools.FileReader;
import com.company.View.InfoPanel;
import org.jocl.*;

import java.util.Arrays;

import static com.company.Main.DRAW_AREA;
import static com.company.Main.RANDOM;
import static com.company.View.InfoPanel.fishSwapValue;
import static org.jocl.CL.*;

/**
 * Controller.
 * Simulation class.  All movement and positioning calculations done here.
 *
 * @author Ian Weeks
 * @version 7.0
 */
public class Simulator
{
    private int numFish, numSharks, numSch;
    private float[][] school, sTarget, shiver;
    private float[] schoolCount;
    private Fish fish;
    private int shrSize, fSize;
    private float[] sTargetX, sTargetY, sTargetZ, otherValues;
    private boolean useGPU = false;

    //OpenCL variable declarations.
    private cl_kernel sharkMovKernel, velocityKernel;
    private cl_program sharkMovProg, velocityProg;
    private cl_command_queue commandQueue;
    private cl_context context;
    private Pointer schXPos, schYPos, schZPos, schXVec, schYVec, schZVec, other;
    private Pointer shrXPos, shrYPos, shrZPos, shrXVec, shrYVec, shrZVec, shrTargetX, shrTargetY, shrTargetZ;
    private long[] global_work_size;
    private long[] local_work_size;

    /**
     * Simulator constructor.
     *
     * @param fishRef  Reference to fish object.
     * @param sharkRef Reference to shark object.
     */
    public Simulator( Fish fishRef, Shark sharkRef )
    {
        this.fish = fishRef;

        this.numSch = fishRef.getNumSchools();

        numFish = fishRef.getNumFish();
        numSharks = sharkRef.getNumSharks();

        school = fishRef.getSchool();
        sTarget = fishRef.getTarget();
        shiver = sharkRef.getShiver();
        schoolCount = fishRef.getSchoolCount();

        sTargetX = new float[ numSharks ];
        sTargetY = new float[ numSharks ];
        sTargetZ = new float[ numSharks ];
        otherValues = new float[ 6 ];

        //OpenCL initialisation.
        //Fish
        this.schXPos = Pointer.to( school[ 0 ] );
        this.schYPos = Pointer.to( school[ 1 ] );
        this.schZPos = Pointer.to( school[ 2 ] );
        this.schXVec = Pointer.to( school[ 3 ] );
        this.schYVec = Pointer.to( school[ 4 ] );
        this.schZVec = Pointer.to( school[ 5 ] );
        this.other = Pointer.to( otherValues );
        //sharks
        this.shrXPos = Pointer.to( shiver[ 0 ] );
        this.shrYPos = Pointer.to( shiver[ 1 ] );
        this.shrZPos = Pointer.to( shiver[ 2 ] );
        this.shrXVec = Pointer.to( shiver[ 3 ] );
        this.shrYVec = Pointer.to( shiver[ 4 ] );
        this.shrZVec = Pointer.to( shiver[ 5 ] );
        this.shrTargetX = Pointer.to( sTargetX );
        this.shrTargetY = Pointer.to( sTargetY );
        this.shrTargetZ = Pointer.to( sTargetZ );

        long numBytes[] = new long[ 1 ];

        // Obtain the platform IDs and initialize the context properties
        System.out.println( "Obtaining platform..." );
        cl_platform_id platforms[] = new cl_platform_id[ 1 ];
        clGetPlatformIDs( platforms.length, platforms, null );
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty( CL_CONTEXT_PLATFORM, platforms[ 0 ] );

        fSize = Sizeof.cl_float * numFish;
        shrSize = Sizeof.cl_float * numSharks;

        // Create an OpenCL context on a GPU device
        context = clCreateContextFromType( contextProperties, CL_DEVICE_TYPE_GPU, null, null, null );
        if ( context == null )
        {
            // If no context for a GPU device could be created,
            // try to create one for a CPU device.
            context = clCreateContextFromType( contextProperties, CL_DEVICE_TYPE_CPU, null, null, null );

            if ( context == null )
            {
                System.out.println( "Unable to create a context" );
                return;
            }
        }

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled( true );

        // Get the list of GPU devices associated with the context
        clGetContextInfo( context, CL_CONTEXT_DEVICES, 0, null, numBytes );

        // Obtain the cl_device_id for the first device
        int numDevices = (int) numBytes[ 0 ] / Sizeof.cl_device_id;
        cl_device_id devices[] = new cl_device_id[ numDevices ];
        clGetContextInfo( context, CL_CONTEXT_DEVICES, numBytes[ 0 ], Pointer.to( devices ), null );

        // Create a command-queue
        commandQueue = clCreateCommandQueue( context, devices[ 0 ], 0, null );

        String sharkMovSource = FileReader.readFile( "sharkMovement.cl" );
        String velocitySource = FileReader.readFile( "velocity.cl" );

        // Create the program from the source code
        sharkMovProg = clCreateProgramWithSource( context, 1, new String[]{ sharkMovSource }, null, null );
        velocityProg = clCreateProgramWithSource( context, 1, new String[]{ velocitySource }, null, null );

        // Build the program
        clBuildProgram( sharkMovProg, 0, null, null, null, null );
        clBuildProgram( velocityProg, 0, null, null, null, null );

        // Create the kernel
        sharkMovKernel = clCreateKernel( sharkMovProg, "sharkKernel", null );
        velocityKernel = clCreateKernel( velocityProg, "velocityKernel", null );

        // Set the work-item dimensions
        global_work_size = new long[]{ numFish };
        local_work_size = new long[]{ 1 };
    }

    /**
     * Update the fish and sharks based on their surroundings.
     *
     * @param deltaT Change in time since last update.
     */
    public void cpuUpdate( double deltaT )
    {
        //Reset the school count;
        Arrays.fill( schoolCount, 0 );
        //Float constants used in this method.
        final float TARGET_RESPAWN_DIST = 5f, ZERO_CORRECTION = 0.1f,
                COLLISION_DAMPENING = 0.005f,
                MAX_DIST_SPEED = 2000, MIN_DIST_SPEED = 200;
        //Integer constants used in this method.
        final int FISH_CAUGHT_DIST = 10;

        //"Random" school swapping behaviour for fish and sharks
        if ( RANDOM.nextFloat() > fishSwapValue )
        {
            school[ 6 ][ RANDOM.nextInt( numFish ) ] = RANDOM.nextInt( numSch );
        }
        if ( RANDOM.nextFloat() > InfoPanel.sharkSwapValue )
        {
            shiver[ 6 ][ RANDOM.nextInt( numSharks ) ] = RANDOM.nextInt( numFish );
        }

        for ( int outer = 0; outer < numSharks; outer++ )
        {
            //Get the x, y and z components of the vector between a shark and its target.
            sTargetX[ outer ] = school[ 0 ][ (int) shiver[ 6 ][ outer ] ] - shiver[ 0 ][ outer ];
            sTargetY[ outer ] = school[ 1 ][ (int) shiver[ 6 ][ outer ] ] - shiver[ 1 ][ outer ];
            sTargetZ[ outer ] = school[ 2 ][ (int) shiver[ 6 ][ outer ] ] - shiver[ 2 ][ outer ];

            float distToTargetSq = sTargetX[ outer ] * sTargetX[ outer ] +
                                   sTargetY[ outer ] * sTargetY[ outer ] +
                                   sTargetZ[ outer ] * sTargetZ[ outer ];

            if ( distToTargetSq < FISH_CAUGHT_DIST )
            {
                //Fish was eaten
                //Change target.
                shiver[ 6 ][ outer ] = RANDOM.nextInt( numFish );
                fish.setFishEaten( fish.getFishEaten() + 1 );
            }
        }

        //Shark movement loop.
        if ( useGPU )
        {
            gpuSharkMov( deltaT );
        }
        else
        {
            for ( int outer = 0; outer < numSharks; outer++ )
            {
                //Add the vector to the shark's movement vector.
                shiver[ 3 ][ outer ] += sTargetX[ outer ];
                shiver[ 4 ][ outer ] += sTargetY[ outer ];
                shiver[ 5 ][ outer ] += sTargetZ[ outer ];

                //Get the magnitude squared of the shark's current movement vector.
                float mag = (shiver[ 3 ][ outer ] * shiver[ 3 ][ outer ]) +
                            (shiver[ 4 ][ outer ] * shiver[ 4 ][ outer ]) +
                            (shiver[ 5 ][ outer ] * shiver[ 5 ][ outer ]);

                //Check if it exceeds our current set limit.
                float sharkVLim = InfoPanel.sharkVelocityValue;
                if ( mag > (sharkVLim * sharkVLim) )
                {
                    //If so then limit the speed. **Square root and divide functions only used when needed**
                    mag = (float) (1 / Math.sqrt( mag ));
                    this.shiver[ 3 ][ outer ] = this.shiver[ 3 ][ outer ] * mag * sharkVLim;
                    this.shiver[ 4 ][ outer ] = this.shiver[ 4 ][ outer ] * mag * sharkVLim;
                    this.shiver[ 5 ][ outer ] = this.shiver[ 5 ][ outer ] * mag * sharkVLim;
                }

                //Move each shark as a function of time and its velocity vectors.
                this.shiver[ 0 ][ outer ] += this.shiver[ 3 ][ outer ] * deltaT;
                this.shiver[ 1 ][ outer ] += this.shiver[ 4 ][ outer ] * deltaT;
                this.shiver[ 2 ][ outer ] += this.shiver[ 5 ][ outer ] * deltaT;
            }
        }

        //sum of x velocity, sum of y velocity
        float sxv = 0, syv = 0, szv = 0;
        //sum of x position, sum of y position
        float sxp = 0, syp = 0, szp = 0;
        int divideCount;
        float x, y, z;

        //Fish movement loop.
        for ( int outer = 0; outer < numFish; outer++ )
        {
            //Keep track of group numbers.
            schoolCount[ ((int) school[ 6 ][ outer ]) ]++;

            sxp = syp = szp = sxv = syv = szv = 0;
            divideCount = 0;
            /*
            Collision detection and response.  Radius based as speed was my
            primary concern.

            In the square matrix below the 1s represent
            collision checks and the 0s are ignored.

            This halves the amount of work needed.

            0000000
            1000000
            1100000
            1110000
            1111000
            1111100
            1111110
             */
            for ( int inner = outer; inner < numFish; inner++ )
            {
                x = school[ 0 ][ outer ] - school[ 0 ][ inner ];
                y = school[ 1 ][ outer ] - school[ 1 ][ inner ];
                z = school[ 2 ][ outer ] - school[ 2 ][ inner ];


                //Get the distance squared between fish.
                float d = (x * x + y * y + z * z);
                if ( d < InfoPanel.collisionDistValue )
                {
                    //If less than the set collision distance the fish have a chance to swap groups.
                    if ( RANDOM.nextFloat() > InfoPanel.collisionSwapValue )
                    {
                        school[ 6 ][ outer ] = school[ 6 ][ inner ];
                    }

                    //Adjust the course of the fish involved.
                    //Adjustments are made to both fish.
                    school[ 3 ][ outer ] += x == 0 ? ZERO_CORRECTION : x * COLLISION_DAMPENING;
                    school[ 4 ][ outer ] += y == 0 ? ZERO_CORRECTION : y * COLLISION_DAMPENING;
                    school[ 5 ][ outer ] += z == 0 ? ZERO_CORRECTION : z * COLLISION_DAMPENING;

                    school[ 3 ][ inner ] -= x == 0 ? ZERO_CORRECTION : x * COLLISION_DAMPENING;
                    school[ 4 ][ inner ] -= y == 0 ? ZERO_CORRECTION : y * COLLISION_DAMPENING;
                    school[ 5 ][ inner ] -= z == 0 ? ZERO_CORRECTION : z * COLLISION_DAMPENING;
                }

                if ( school[ 6 ][ outer ] == school[ 6 ][ inner ] )
                {
                    //Add up the velocity and position vectors for all fish within a group.
                    sxv += school[ 3 ][ inner ];
                    syv += school[ 4 ][ inner ];
                    szv += school[ 5 ][ inner ];
                    sxp += school[ 0 ][ inner ];
                    syp += school[ 1 ][ inner ];
                    szp += school[ 2 ][ inner ];
                    //Keep a count of each group.
                    divideCount++;
                }
            }

            //Perform one division to allow for multiplications later.
            float scDiv = 1 / (float) divideCount;

            //Get averages.
            sxv *= scDiv;
            syv *= scDiv;
            szv *= scDiv;

            sxp *= scDiv;
            syp *= scDiv;
            szp *= scDiv;
        }

        //Fish radius based avoidance of sharks.
        float avoidX, avoidY, avoidZ;
        for ( int outer = 0; outer < numFish; outer++ )
        {
            for ( int inner = 0; inner < numSharks; inner++ )
            {
                avoidX = school[ 0 ][ outer ] - shiver[ 0 ][ inner ];
                avoidY = school[ 1 ][ outer ] - shiver[ 1 ][ inner ];
                avoidZ = school[ 2 ][ outer ] - shiver[ 2 ][ inner ];

                //distance to avoid
                float dta = avoidX * avoidX + avoidY * avoidY + avoidZ * avoidZ;

                //Linear algorithm to calculate the strength of the repulsion for each fish.
                if ( dta < 22000 )
                {
                    float t = 1 / (float) Math.sqrt( dta );

                    float avoidStrength = (-0.0000476f) * dta + (1.00f);

                    school[ 3 ][ outer ] += avoidX * t * avoidStrength;
                    school[ 4 ][ outer ] += avoidY * t * avoidStrength;
                    school[ 5 ][ outer ] += avoidZ * t * avoidStrength;
                }
            }
        }

        if ( useGPU )
        {
            otherValues = new float[]{ sxv, syv, szv, sxp, syp, szp };
            gpuVelocity();
        }
        else
        {
            for ( int outer = 0; outer < numFish; outer++ )
            {
                //Group velocity matching
                //Add in the perceived group velocity
                school[ 3 ][ outer ] += (sxv) * InfoPanel.schoolVelocityValue;
                school[ 4 ][ outer ] += (syv) * InfoPanel.schoolVelocityValue;
                school[ 5 ][ outer ] += (szv) * InfoPanel.schoolVelocityValue;

                //Get the distance between the current fish and the perceived center of its group.
                float difX, difY, difZ;
                difX = sxp - school[ 0 ][ outer ];
                difY = syp - school[ 1 ][ outer ];
                difZ = szp - school[ 2 ][ outer ];

                //Check if the fish is too far away from its group.
                if ( Math.sqrt( (difX * difX) + (difY * difY) + (difZ * difZ) ) > InfoPanel.schoolInteractDistValue )
                {
                    //If so then steer the fish back to its group.
                    school[ 3 ][ outer ] += (difX) * InfoPanel.schoolInteractStrValue;
                    school[ 4 ][ outer ] += (difY) * InfoPanel.schoolInteractStrValue;
                    school[ 5 ][ outer ] += (difZ) * InfoPanel.schoolInteractStrValue;
                }
            }
        }

        for ( int outer = 0; outer < numFish; outer++ )
        {
            //Get vector between each fish and its group target.
            //x dist to point, y dist to point, z dist to point.
            float xdp = (sTarget[ 0 ][ (int) school[ 6 ][ outer ] ] - school[ 0 ][ outer ]);
            float ydp = (sTarget[ 1 ][ (int) school[ 6 ][ outer ] ] - school[ 1 ][ outer ]);
            float zdp = (sTarget[ 2 ][ (int) school[ 6 ][ outer ] ] - school[ 2 ][ outer ]);

            //Squared distance to target. **Used later**
            float distSquared = xdp * xdp + ydp * ydp + zdp * zdp;
            float distToPoint = (float) Math.sqrt( distSquared );
            //Force value into correct range for line function.
            if ( distToPoint > MAX_DIST_SPEED )
            {
                distToPoint = MAX_DIST_SPEED;
            }
            else if ( distToPoint < MIN_DIST_SPEED )
            {
                distToPoint = MIN_DIST_SPEED;
            }

            //Steer the fish towards its target.
            school[ 3 ][ outer ] += xdp * InfoPanel.fishTargetingModValue;
            school[ 4 ][ outer ] += ydp * InfoPanel.fishTargetingModValue;
            school[ 5 ][ outer ] += zdp * InfoPanel.fishTargetingModValue;

            //Target respawn based on fish distance
            if ( distSquared < TARGET_RESPAWN_DIST * TARGET_RESPAWN_DIST )
            {
                sTarget[ 0 ][ (int) school[ 6 ][ outer ] ] = RANDOM.nextInt( DRAW_AREA );
                sTarget[ 1 ][ (int) school[ 6 ][ outer ] ] = RANDOM.nextInt( DRAW_AREA );
                sTarget[ 2 ][ (int) school[ 6 ][ outer ] ] = RANDOM.nextInt( DRAW_AREA );
            }

            //Normalise
            float mag = (school[ 3 ][ outer ] * school[ 3 ][ outer ]) +
                        (school[ 4 ][ outer ] * school[ 4 ][ outer ]) +
                        (school[ 5 ][ outer ] * school[ 5 ][ outer ]);

            final float test = lineFunc( distToPoint );
            //fish speed limiter
            float vLim = InfoPanel.fishVelocityValue * test;

            //Check if the fish is exceeding its currently set speed limit.
            if ( mag > vLim * vLim )
            {
                //If so then limit the speed. **Square root and divide functions only used when needed**
                mag = (float) (1 / Math.sqrt( mag ));
                school[ 3 ][ outer ] = (school[ 3 ][ outer ] * mag) * vLim;
                school[ 4 ][ outer ] = (school[ 4 ][ outer ] * mag) * vLim;
                school[ 5 ][ outer ] = (school[ 5 ][ outer ] * mag) * vLim;
            }

            //Move each fish as a function of time and its velocity vectors.
            school[ 0 ][ outer ] += school[ 3 ][ outer ] * deltaT;
            school[ 1 ][ outer ] += school[ 4 ][ outer ] * deltaT;
            school[ 2 ][ outer ] += school[ 5 ][ outer ] * deltaT;
        }
    }

    /**
     * Perform shark movement calculations on the GPU.
     *
     * @param deltaT Change in time since last update.
     */
    public void gpuSharkMov( double deltaT )
    {
        //Set work size, memory and kernel arguments.
        global_work_size = new long[]{ numSharks };

        cl_mem sTarMemX =
                clCreateBuffer( context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, shrSize, shrTargetX, null );
        cl_mem sTarMemY =
                clCreateBuffer( context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, shrSize, shrTargetY, null );
        cl_mem sTarMemZ =
                clCreateBuffer( context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, shrSize, shrTargetZ, null );
        cl_mem sMemXPos = clCreateBuffer( context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, shrSize, shrXPos, null );
        cl_mem sMemYPos = clCreateBuffer( context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, shrSize, shrYPos, null );
        cl_mem sMemZPos = clCreateBuffer( context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, shrSize, shrZPos, null );
        cl_mem sMemXVec = clCreateBuffer( context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, shrSize, shrXVec, null );
        cl_mem sMemYVec = clCreateBuffer( context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, shrSize, shrYVec, null );
        cl_mem sMemZVec = clCreateBuffer( context, CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR, shrSize, shrZVec, null );

        clSetKernelArg( sharkMovKernel, 0, Sizeof.cl_mem, Pointer.to( sTarMemX ) );
        clSetKernelArg( sharkMovKernel, 1, Sizeof.cl_mem, Pointer.to( sTarMemY ) );
        clSetKernelArg( sharkMovKernel, 2, Sizeof.cl_mem, Pointer.to( sTarMemZ ) );
        clSetKernelArg( sharkMovKernel, 3, Sizeof.cl_mem, Pointer.to( sMemXPos ) );
        clSetKernelArg( sharkMovKernel, 4, Sizeof.cl_mem, Pointer.to( sMemYPos ) );
        clSetKernelArg( sharkMovKernel, 5, Sizeof.cl_mem, Pointer.to( sMemZPos ) );
        clSetKernelArg( sharkMovKernel, 6, Sizeof.cl_mem, Pointer.to( sMemXVec ) );
        clSetKernelArg( sharkMovKernel, 7, Sizeof.cl_mem, Pointer.to( sMemYVec ) );
        clSetKernelArg( sharkMovKernel, 8, Sizeof.cl_mem, Pointer.to( sMemZVec ) );
        clSetKernelArg( sharkMovKernel, 9, Sizeof.cl_float, Pointer.to( new float[]{ InfoPanel.sharkVelocityValue } ) );
        clSetKernelArg( sharkMovKernel, 10, Sizeof.cl_float, Pointer.to( new float[]{ (float) deltaT } ) );

        // Execute the kernel
        clEnqueueNDRangeKernel( commandQueue, sharkMovKernel, 1, null, global_work_size, local_work_size, 0, null,
                                null );

        // Read the output data
        //Positions
        clEnqueueReadBuffer( commandQueue, sMemXPos, CL_TRUE, 0, shrSize, shrXPos, 0, null, null );
        clEnqueueReadBuffer( commandQueue, sMemYPos, CL_TRUE, 0, shrSize, shrYPos, 0, null, null );
        clEnqueueReadBuffer( commandQueue, sMemZPos, CL_TRUE, 0, shrSize, shrZPos, 0, null, null );
        //Movement vectors
        clEnqueueReadBuffer( commandQueue, sMemXVec, CL_TRUE, 0, shrSize, shrXVec, 0, null, null );
        clEnqueueReadBuffer( commandQueue, sMemYVec, CL_TRUE, 0, shrSize, shrYVec, 0, null, null );
        clEnqueueReadBuffer( commandQueue, sMemZVec, CL_TRUE, 0, shrSize, shrZVec, 0, null, null );

        //Release the memory.
        clReleaseMemObject( sTarMemX );
        clReleaseMemObject( sTarMemY );
        clReleaseMemObject( sTarMemZ );
        clReleaseMemObject( sMemXVec );
        clReleaseMemObject( sMemYVec );
        clReleaseMemObject( sMemZVec );
        clReleaseMemObject( sMemXPos );
        clReleaseMemObject( sMemYPos );
        clReleaseMemObject( sMemZPos );
    }

    /**
     * Perform fish group velocity calculations on the GPU.
     */
    public void gpuVelocity()
    {
        //Set work size, memory and kernel arguments.
        global_work_size = new long[]{ numFish };

        cl_mem memXPos = clCreateBuffer( context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, fSize, schXPos, null );
        cl_mem memYPos = clCreateBuffer( context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, fSize, schYPos, null );
        cl_mem memZPos = clCreateBuffer( context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, fSize, schZPos, null );
        cl_mem memXVec = clCreateBuffer( context, CL_MEM_WRITE_ONLY | CL_MEM_COPY_HOST_PTR, fSize, schXVec, null );
        cl_mem memYVec = clCreateBuffer( context, CL_MEM_WRITE_ONLY | CL_MEM_COPY_HOST_PTR, fSize, schYVec, null );
        cl_mem memZVec = clCreateBuffer( context, CL_MEM_WRITE_ONLY | CL_MEM_COPY_HOST_PTR, fSize, schZVec, null );
        cl_mem memOther = clCreateBuffer( context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, fSize, other, null );

        clSetKernelArg( velocityKernel, 0, Sizeof.cl_mem, Pointer.to( memXPos ) );
        clSetKernelArg( velocityKernel, 1, Sizeof.cl_mem, Pointer.to( memYPos ) );
        clSetKernelArg( velocityKernel, 2, Sizeof.cl_mem, Pointer.to( memZPos ) );
        clSetKernelArg( velocityKernel, 3, Sizeof.cl_mem, Pointer.to( memXVec ) );
        clSetKernelArg( velocityKernel, 4, Sizeof.cl_mem, Pointer.to( memYVec ) );
        clSetKernelArg( velocityKernel, 5, Sizeof.cl_mem, Pointer.to( memZVec ) );
        clSetKernelArg( velocityKernel, 6, Sizeof.cl_mem, Pointer.to( memOther ) );
        clSetKernelArg( velocityKernel, 7, Sizeof.cl_float,
                        Pointer.to( new float[]{ InfoPanel.schoolVelocityValue } ) );
        clSetKernelArg( velocityKernel, 8, Sizeof.cl_float,
                        Pointer.to( new float[]{ InfoPanel.schoolInteractStrValue } ) );
        clSetKernelArg( velocityKernel, 9, Sizeof.cl_float,
                        Pointer.to( new float[]{ InfoPanel.schoolInteractDistValue } ) );

        // Execute the kernel
        clEnqueueNDRangeKernel( commandQueue, velocityKernel, 1, null, global_work_size, local_work_size, 0, null,
                                null );

        // Read the output data
        //Movement vectors
        clEnqueueReadBuffer( commandQueue, memXVec, CL_TRUE, 0, fSize, schXVec, 0, null, null );
        clEnqueueReadBuffer( commandQueue, memYVec, CL_TRUE, 0, fSize, schYVec, 0, null, null );
        clEnqueueReadBuffer( commandQueue, memZVec, CL_TRUE, 0, fSize, schZVec, 0, null, null );

        //Release the memory.
        clReleaseMemObject( memXPos );
        clReleaseMemObject( memYPos );
        clReleaseMemObject( memZPos );
        clReleaseMemObject( memXVec );
        clReleaseMemObject( memYVec );
        clReleaseMemObject( memZVec );
        clReleaseMemObject( memOther );
    }

    /**
     * Toggle the useGPU boolean.
     */
    public void toggleGPU()
    {
        useGPU = !useGPU;
    }

    /**
     * Check if the GPU is being used for some calculations.
     *
     * @return True if the GPU is being used, false otherwise.
     */
    public boolean isUseGPU()
    {
        return useGPU;
    }

    /**
     * Release all OpenCL memory.
     */
    public void stop()
    {
        clReleaseContext( context );
        clReleaseKernel( sharkMovKernel );
        clReleaseKernel( velocityKernel );
        clReleaseProgram( sharkMovProg );
        clReleaseProgram( velocityProg );
        clReleaseCommandQueue( commandQueue );
        System.out.println( "Released all OpenCL memory" );
    }

    private float lineFunc( float input )
    {
        //Line function used to increase fish speed based on distance to its target.
        return -0.0005f * input + 1.1f;
    }
}
