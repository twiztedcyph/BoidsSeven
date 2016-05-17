package com.company.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import static com.company.Main.FULLSCREEN_HEIGHT;
import static com.company.Main.FULLSCREEN_WIDTH;

/**
 * Singleton InfoPanel class.
 * Right hand panel containing all the sliders and information display.
 *
 * Creates a new <code>JPanel</code> with a double buffer
 * and a flow layout.
 *
 * @author Ian Weeks
 * @version 7.0
 */
public class InfoPanel extends JPanel
{
    private JSlider fishSwapSlider, sharkSwapSlider, collisionSwapSlider,
            schoolVelocitySlider, fishVelocitySlider, sharkVelocitySlider,
            schoolInteractDistSlider, collisionDistSlider, schoolInteractStrSlider,
            fishTargetingModSlider;

    private JLabel infoDisplay;

    //These are static as direct access made the most sense (to me).
    public static float fishSwapValue;
    public static float sharkSwapValue = 1.0f;
    public static float collisionSwapValue;
    public static float schoolVelocityValue;
    public static float fishVelocityValue = 2.2f;
    public static float sharkVelocityValue = 2.5f;
    public static float schoolInteractDistValue = 5500;
    public static float schoolInteractStrValue = 0.001f;
    public static double fishTargetingModValue = 0.0005;
    public static float collisionDistValue = 500f;

    private static InfoPanel ourInstance = null;

    /**
     * Get the instance of this InfoPanel
     *
     * @return The instance of this infoPanel.
     */
    public static InfoPanel getInstance()
    {
        if ( ourInstance == null )
        {
            ourInstance = new InfoPanel();
        }
        return ourInstance;
    }

    private InfoPanel()
    {
        //Panel initialisation.
        this.setBackground( Color.lightGray );
        //This panel will use 30% of the screen width.
        final double PANEL_WIDTH = 0.3;
        this.setPreferredSize( new Dimension( (int) (FULLSCREEN_WIDTH * PANEL_WIDTH), FULLSCREEN_HEIGHT ) );
        //Null layout for explicit input of component positions.
        this.setLayout( null );
        //Initialise all labels.
        setLabels();
        //Initialise all sliders.
        setSliders();
    }

    private void setLabels()
    {
        //Each label is created, placed and added here.
        //Could have used a factory but would not have saved me much typing...
        JLabel fishSwapLabel = new JLabel( "Fish school swap chance" );
        fishSwapLabel.setBounds( 10, 10, 200, 25 );
        this.add( fishSwapLabel );

        JLabel sharkSwapLabel = new JLabel( "Shark target swap chance" );
        sharkSwapLabel.setBounds( 250, 10, 200, 25 );
        this.add( sharkSwapLabel );

        JLabel collisionSwapLabel = new JLabel( "Collision swap chance" );
        collisionSwapLabel.setBounds( 10, 110, 200, 25 );
        this.add( collisionSwapLabel );

        JLabel schoolVelocityLabel = new JLabel( "School velocity modifier" );
        schoolVelocityLabel.setBounds( 250, 110, 200, 25 );
        this.add( schoolVelocityLabel );

        JLabel fishVelocityLabel = new JLabel( "Fish velocity modifier" );
        fishVelocityLabel.setBounds( 10, 210, 200, 25 );
        this.add( fishVelocityLabel );

        JLabel sharkVelocityLabel = new JLabel( "Shark velocity modifier" );
        sharkVelocityLabel.setBounds( 250, 210, 200, 25 );
        this.add( sharkVelocityLabel );

        JLabel schoolInteractDistLabel = new JLabel( "School interaction distance modifier" );
        schoolInteractDistLabel.setBounds( 10, 310, 200, 25 );
        this.add( schoolInteractDistLabel );

        JLabel schoolInteractStrLabel = new JLabel( "School interaction strength" );
        schoolInteractStrLabel.setBounds( 250, 310, 200, 25 );
        this.add( schoolInteractStrLabel );

        JLabel fishTargetingModLabel = new JLabel( "Fish targeting mod" );
        fishTargetingModLabel.setBounds( 10, 410, 200, 25 );
        this.add( fishTargetingModLabel );

        JLabel collisionDistLabel = new JLabel( "Fish collision distance" );
        collisionDistLabel.setBounds( 250, 410, 200, 25 );
        this.add( collisionDistLabel );

        Font displayFont =  new Font("Serif", Font.BOLD, 18);
        JLabel infoLabel = new JLabel( "Information");
        infoLabel.setBounds( 10, 610, 200, 25 );
        infoLabel.setFont( displayFont );
        this.add( infoLabel );

        infoDisplay = new JLabel( String.valueOf(fishVelocityValue) );
        infoDisplay.setBounds( 10, 640, 400, 300 );
        infoDisplay.setFont(displayFont);
        this.add( infoDisplay );
    }

    private void setSliders()
    {
        //Each slider is created, placed and added here.
        //A factory is used to produce the sliders.  See report for saved space.
        //Lambdas are used to shorten the syntax even more.
        fishSwapSlider = new SliderFactory().buildSlider( 0, 1000, 0, 10, 30, "fish" );
        fishSwapSlider.addChangeListener( e -> fishSwapValue = ((1000f - (float) fishSwapSlider.getValue()) * 0.001f) );
        this.add( fishSwapSlider );

        sharkSwapSlider = new SliderFactory().buildSlider( 0, 1000, 0, 250, 30, "shark" );
        sharkSwapSlider
                .addChangeListener( e -> sharkSwapValue = ((1000f - (float) sharkSwapSlider.getValue()) * 0.001f) );
        this.add( sharkSwapSlider );

        collisionSwapSlider = new SliderFactory().buildSlider( 0, 1000, 0, 10, 130, "collision" );
        collisionSwapSlider.addChangeListener(
                e -> collisionSwapValue = ((1000f - (float) collisionSwapSlider.getValue()) * 0.001f) );
        this.add( collisionSwapSlider );

        schoolVelocitySlider = new SliderFactory().buildSlider( 0, 1000, 0, 250, 130, "groupVel" );
        schoolVelocitySlider.addChangeListener(
                e -> schoolVelocityValue = ((schoolVelocitySlider.getValue()) * 0.001f) );
        this.add( schoolVelocitySlider );

        fishVelocitySlider = new SliderFactory().buildSlider( 0, 400, 220, 10, 230, "fishVel" );
        fishVelocitySlider.addChangeListener(
                e -> fishVelocityValue = ((fishVelocitySlider.getValue()) * 0.01f) );
        this.add( fishVelocitySlider );

        sharkVelocitySlider = new SliderFactory().buildSlider( 0, 400, 200, 250, 230, "sharkVel" );
        sharkVelocitySlider.addChangeListener(
                e -> sharkVelocityValue = ((sharkVelocitySlider.getValue()) * 0.01f) );
        this.add( sharkVelocitySlider );

        schoolInteractDistSlider = new SliderFactory().buildSlider( 0, 10000, 5500, 10, 330, "interact" );
        schoolInteractDistSlider.addChangeListener(
                e -> schoolInteractDistValue = ((schoolInteractDistSlider.getValue())) );
        this.add( schoolInteractDistSlider );

        schoolInteractStrSlider = new SliderFactory().buildSlider( 0, 1000, 1, 250, 330, "schStr" );
        schoolInteractStrSlider.addChangeListener(
                e -> schoolInteractStrValue = ((schoolInteractStrSlider.getValue()) * 0.001f) );
        this.add( schoolInteractStrSlider );

        fishTargetingModSlider = new SliderFactory().buildSlider( 0, 10000, 5, 10, 430, "fishTargetingMod" );
        fishTargetingModSlider.addChangeListener(
                e -> fishTargetingModValue = ((fishTargetingModSlider.getValue())) * 0.0001f );
        this.add( fishTargetingModSlider );

        collisionDistSlider = new SliderFactory().buildSlider( 0, 5000, 500, 250, 430, "collisionDist" );
        collisionDistSlider.addChangeListener(
                e -> collisionDistValue = collisionDistSlider.getValue() );
        this.add( collisionDistSlider );
    }

    //IntelliJ gets quite upset about similar looking code.
    @SuppressWarnings( "all" )
    private class WheelListener implements MouseWheelListener
    {
        private String callerName;

        public WheelListener( String name )
        {
            this.callerName = name;
        }

        /*
        The mouse wheel can be used to move the sliders.
        This was done for user convenience.  The code
        however is long, repetative and ugly...
        You have been warned. :)
         */
        @Override
        public void mouseWheelMoved( MouseWheelEvent e )
        {
            switch ( callerName )
            {
                case "fish":
                    switch ( e.getWheelRotation() )
                    {
                        case 1:
                            fishSwapSlider.setValue( fishSwapSlider.getValue() - 1 );
                            fishSwapValue = ((1000f - (float) fishSwapSlider.getValue()) * 0.001f);
                            System.out.println( fishSwapSlider.getValue() + " " + fishSwapValue );
                            break;
                        case -1:
                            fishSwapSlider.setValue( fishSwapSlider.getValue() + 1 );
                            fishSwapValue = ((1000f - (float) fishSwapSlider.getValue()) * 0.001f);
                            System.out.println( fishSwapSlider.getValue() + " " + fishSwapValue );
                            break;
                    }
                    break;
                case "shark":
                    switch ( e.getWheelRotation() )
                    {
                        case 1:
                            sharkSwapSlider.setValue( sharkSwapSlider.getValue() - 1 );
                            sharkSwapValue = ((1000f - (float) sharkSwapSlider.getValue()) * 0.001f);
                            System.out.println( sharkSwapSlider.getValue() + " " + sharkSwapValue );
                            break;
                        case -1:
                            sharkSwapSlider.setValue( sharkSwapSlider.getValue() + 1 );
                            sharkSwapValue = ((1000f - (float) sharkSwapSlider.getValue()) * 0.001f);
                            System.out.println( sharkSwapSlider.getValue() + " " + sharkSwapValue );
                            break;
                        default:
                    }
                    break;
                case "collision":
                    switch ( e.getWheelRotation() )
                    {
                        case 1:
                            collisionSwapSlider.setValue( collisionSwapSlider.getValue() - 1 );
                            collisionSwapValue = ((1000f - (float) collisionSwapSlider.getValue()) * 0.001f);
                            System.out.println( collisionSwapSlider.getValue() + " " + collisionSwapValue );
                            break;
                        case -1:
                            collisionSwapSlider.setValue( collisionSwapSlider.getValue() + 1 );
                            collisionSwapValue = ((1000f - (float) collisionSwapSlider.getValue()) * 0.001f);
                            System.out.println( collisionSwapSlider.getValue() + " " + collisionSwapValue );
                            break;
                        default:
                    }
                    break;
                case "groupVel":
                    switch ( e.getWheelRotation() )
                    {
                        case 1:
                            schoolVelocitySlider.setValue( schoolVelocitySlider.getValue() - 1 );
                            schoolVelocityValue = ((schoolVelocitySlider.getValue()) * 0.01f);
                            System.out.println( schoolVelocitySlider.getValue() + " " + schoolVelocityValue );
                            break;
                        case -1:
                            schoolVelocitySlider.setValue( schoolVelocitySlider.getValue() + 1 );
                            schoolVelocityValue = ((schoolVelocitySlider.getValue()) * 0.01f);
                            System.out.println( schoolVelocitySlider.getValue() + " " + schoolVelocityValue );
                            break;
                        default:
                    }
                    break;
                case "fishVel":
                    switch ( e.getWheelRotation() )
                    {
                        case 1:
                            fishVelocitySlider.setValue( fishVelocitySlider.getValue() - 1 );
                            fishVelocityValue = ((fishVelocitySlider.getValue()) * 0.01f);
                            System.out.println( fishVelocitySlider.getValue() + " " + fishVelocityValue );
                            break;
                        case -1:
                            fishVelocitySlider.setValue( fishVelocitySlider.getValue() + 1 );
                            fishVelocityValue = ((fishVelocitySlider.getValue()) * 0.01f);
                            System.out.println( fishVelocitySlider.getValue() + " " + fishVelocityValue );
                            break;
                        default:
                    }
                    break;
                case "sharkVel":
                    switch ( e.getWheelRotation() )
                    {
                        case 1:
                            sharkVelocitySlider.setValue( sharkVelocitySlider.getValue() - 1 );
                            sharkVelocityValue = ((sharkVelocitySlider.getValue()) * 0.01f);
                            System.out.println( sharkVelocitySlider.getValue() + " " + sharkVelocityValue );
                            break;
                        case -1:
                            sharkVelocitySlider.setValue( sharkVelocitySlider.getValue() + 1 );
                            sharkVelocityValue = ((sharkVelocitySlider.getValue()) * 0.01f);
                            System.out.println( sharkVelocitySlider.getValue() + " " + sharkVelocityValue );
                            break;
                        default:
                    }
                    break;
                case "interact":
                    switch ( e.getWheelRotation() )
                    {
                        case 1:
                            schoolInteractDistSlider.setValue( schoolInteractDistSlider.getValue() - 1 );
                            schoolInteractDistValue = ((schoolInteractDistSlider.getValue()) * 0.01f);
                            System.out.println( schoolInteractDistSlider.getValue() + " " + schoolInteractDistValue );
                            break;
                        case -1:
                            schoolInteractDistSlider.setValue( schoolInteractDistSlider.getValue() + 1 );
                            schoolInteractDistValue = ((schoolInteractDistSlider.getValue()) * 0.01f);
                            System.out.println( schoolInteractDistSlider.getValue() + " " + schoolInteractDistValue );
                            break;
                        default:
                    }
                    break;
                case "schStr":
                    switch ( e.getWheelRotation() )
                    {
                        case 1:
                            schoolInteractStrSlider.setValue( schoolInteractStrSlider.getValue() - 1 );
                            schoolInteractStrValue = ((schoolInteractStrSlider.getValue()) * 0.01f);
                            System.out.println( schoolInteractStrSlider.getValue() + " " + schoolInteractStrValue );
                            break;
                        case -1:
                            schoolInteractStrSlider.setValue( schoolInteractStrSlider.getValue() + 1 );
                            schoolInteractStrValue = ((schoolInteractStrSlider.getValue()) * 0.01f);
                            System.out.println( schoolInteractStrSlider.getValue() + " " + schoolInteractStrValue );
                            break;
                        default:
                    }
                    break;
                case "fishTargetingMod":
                    switch ( e.getWheelRotation() )
                    {
                        case 1:
                            fishTargetingModSlider.setValue( fishTargetingModSlider.getValue() - 1 );
                            fishTargetingModValue = (((fishTargetingModSlider.getValue())) * 0.0001);
                            System.out.println( fishTargetingModSlider.getValue() + " " + fishTargetingModValue );
                            break;
                        case -1:
                            fishTargetingModSlider.setValue( fishTargetingModSlider.getValue() + 1 );
                            fishTargetingModValue = (((fishTargetingModSlider.getValue())) * 0.0001);
                            System.out.println( fishTargetingModSlider.getValue() + " " + fishTargetingModValue );
                            break;
                        default:
                    }
                    break;
                case "collisionDist":
                    switch ( e.getWheelRotation() )
                    {
                        case 1:
                            collisionDistSlider.setValue( collisionDistSlider.getValue() - 1 );
                            collisionDistValue = (collisionDistSlider.getValue());
                            System.out.println( collisionDistSlider.getValue() + " " + collisionDistValue );
                            break;
                        case -1:
                            collisionDistSlider.setValue( collisionDistSlider.getValue() + 1 );
                            collisionDistValue = (collisionDistSlider.getValue());
                            System.out.println( collisionDistSlider.getValue() + " " + collisionDistValue );
                            break;
                        default:
                    }
                    break;
                default:
            }
        }
    }

    private class SliderFactory
    {
        /*
        Slider factory takes care of most of the initialisation for each
        JSlider.  This saves writing a ton of code and can reduce careless
        bugs.
         */
        private final int WIDTH = 200, HEIGHT = 50;

        public JSlider buildSlider( int min, int max, int value, int x, int y, String id )
        {
            //Orientation and values
            JSlider result = new JSlider( JSlider.HORIZONTAL, min, max, value );
            //Auto set ticks
            result.setMajorTickSpacing( max / 5 );
            result.setMinorTickSpacing( max / 10 );
            //Add the mouse wheel listener with custom id.
            result.addMouseWheelListener( new WheelListener( id ) );
            //Positioning with predefined width and height.
            result.setBounds( x, y, WIDTH, HEIGHT );
            result.setBackground( Color.lightGray );
            result.setPaintTicks( true );
            result.setPaintLabels( true );
            //Return the created slider.
            return result;
        }
    }

    /**
     * Get the information display label
     *
     * @return The information display label.
     */
    public JLabel getInfoDisplay()
    {
        return infoDisplay;
    }
}
