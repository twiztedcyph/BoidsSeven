package com.company.View;

import javax.swing.*;
import java.awt.*;

/**
 * Program parameter input panel.
 *
 * @author Ian Weeks 6204848
 * @version 7.0
 */
public class ProgramInput extends JPanel
{
    private JSlider numFish, numSchools, numSharks;

    /**
     * Input parameters for the program.
     *
     * Creates a new <code>JPanel</code> with a double buffer
     * and a flow layout.
     */
    public ProgramInput()
    {
        this.setPreferredSize(new Dimension(120, 180));
        this.setLayout(null);

        JLabel numFishLabel, numSchoolsLabel, numSharksLabel;

        numFishLabel = new JLabel("Fish:");
        numFishLabel.setBounds(5, 20, 50, 20);
        this.add(numFishLabel);

        numFish = new JSlider(JSlider.HORIZONTAL, 0, 2000, 1000);
        numFish.setMajorTickSpacing(500);
        numFish.setMinorTickSpacing(100);
        numFish.setPaintTicks(true);
        numFish.setPaintLabels(true);
        numFish.setBounds(60, 5, 170, 52);
        numFish.addChangeListener(e -> {
            if (numFish.getValue() < 1)
            {
                numFish.setValue(1);
            }
        });
        this.add(numFish);

        numSchoolsLabel = new JLabel("Schools:");
        numSchoolsLabel.setBounds(5, 70, 50, 20);
        this.add(numSchoolsLabel);

        numSchools = new JSlider(JSlider.HORIZONTAL, 1, 10, 3);
        numSchools.setMajorTickSpacing(1);
        numSchools.setPaintTicks(true);
        numSchools.setPaintLabels(true);
        numSchools.setBounds(60, 55, 170, 52);
        numSchools.addChangeListener(e -> {
            if (numSchools.getValue() < 1)
            {
                numSchools.setValue(1);
            }
        });
        this.add(numSchools);

        numSharksLabel = new JLabel("Sharks:");
        numSharksLabel.setBounds(5, 120, 50, 20);
        this.add(numSharksLabel);

        numSharks = new JSlider(JSlider.HORIZONTAL, 1, 6, 3);
        numSharks.setMajorTickSpacing(1);
        numSharks.setPaintTicks(true);
        numSharks.setPaintLabels(true);
        numSharks.setBounds(60, 105, 170, 52);
        numSharks.addChangeListener(e -> {
            if (numSharks.getValue() < 1)
            {
                numSharks.setValue(1);
            }
        });
        this.add(numSharks);

    }

    /**
     * Get the number of fish.
     *
     * @return The number of fish
     */
    public int getNumFish()
    {
        return numFish.getValue();
    }

    /**
     * Get the number of schools.
     *
     * @return The number of schools.
     */
    public int getNumSchools()
    {
        return numSchools.getValue();
    }

    /**
     * Get the number of sharks.
     *
     * @return The number of sharks.
     */
    public int getNumSharks()
    {
        return numSharks.getValue();
    }
}
