package com.company.Tools;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * File to string class used for kernel input.
 *
 * @author Ian Weeks 6204848
 * @version 7.0
 */
public class FileReader
{
    /**
     * Convert the contents of a file to a string.
     *
     * @param fileName The file to be be converted,
     * @return A string of the file contents.
     */
    public static String readFile(String fileName)
    {
        StringBuilder result = new StringBuilder();
        java.io.FileReader f = null;
        BufferedReader br = null;
        try
        {
            f = new java.io.FileReader(fileName);
            if (f.ready())
            {
                br = new BufferedReader(f);
                String line;
                while ((line = br.readLine()) != null)
                {
                    result.append("\n").append(line);
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                System.out.println("Closing");
                if (f != null)
                {
                    f.close();
                }
                if (br != null)
                {
                    br.close();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return result.toString();
    }
}