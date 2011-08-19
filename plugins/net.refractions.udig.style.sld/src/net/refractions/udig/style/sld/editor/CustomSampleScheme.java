package net.refractions.udig.style.sld.editor;

import org.geotools.brewer.color.SampleScheme;

public class CustomSampleScheme extends SampleScheme {
   private int[][] sampleScheme ;
    int minCount = -1;
    int maxCount = -1;

    /**
     * Creates a new instance of SampleScheme
     */
    public CustomSampleScheme(int length) {
        sampleScheme = new int[length][];
    }

    /**
     * Indexed getter for property sampleScheme.
     *
     * @param length Index of the property.
     *
     * @return Value of the property at <CODE>index</CODE>.
     */
    public int[] getSampleScheme(int length) {
        return this.sampleScheme[length - 2];
    }

    /**
     * Indexed setter for property sampleScheme.
     *
     * @param length Index of the property.
     * @param sampleScheme New value of the property at <CODE>index</CODE>.
     */
    public void setSampleScheme(int length, int[] sampleScheme) {
        this.sampleScheme[length - 2] = sampleScheme;

        if ((minCount == -1) || (minCount > length)) {
            minCount = length;
        }

        if ((maxCount == -1) || (maxCount < length)) {
            maxCount = length;
        }
    }

    /**
     * Getter for the min colour count
     *
     * @return the smallest number of colours we have a scheme for
     */
    public int getMinCount() {
        return minCount;
    }

    /**
     * Getter for the max colour count
     *
     * @return the largest number of colours we have a scheme for
     */
    public int getMaxCount() {
        return maxCount;
    }
}