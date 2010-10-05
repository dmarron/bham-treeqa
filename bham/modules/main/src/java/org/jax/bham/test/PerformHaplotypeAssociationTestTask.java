/*
 * Copyright (c) 2008 The Jackson Laboratory
 *
 * Permission is hereby granted, free of charge, to any person obtaining  a copy
 * of this software and associated documentation files (the  "Software"), to
 * deal in the Software without restriction, including  without limitation the
 * rights to use, copy, modify, merge, publish,  distribute, sublicense, and/or
 * sell copies of the Software, and to  permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be  included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,  EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF  MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY  CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,  TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE  SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.jax.bham.test;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jax.bham.BhamApplication;
import org.jax.haplotype.analysis.experimentdesign.HaplotypeAssociationTest;
import org.jax.haplotype.analysis.experimentdesign.HaplotypeBlockTestResult;
import org.jax.util.concurrent.AbstractLongRunningTask;
import org.jax.util.gui.MessageDialogUtilities;

/**
 * A long running task that converts the p-values in the given
 * haplotype test results into -log10(p-value) scores
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class PerformHaplotypeAssociationTestTask
extends AbstractLongRunningTask
implements Enumeration<HaplotypeBlockTestResult[]>
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            PerformHaplotypeAssociationTestTask.class.getName());
    
    private final HaplotypeAssociationTest test;
    
    private final List<Integer> chromosomeNumbers;
    
    private volatile int chromosomeIndex = 0;

    /**
     * Constructor
     * @param test
     *          the association test to convert
     * @param chromosomeNumbers
     *          the chromosome numbers
     */
    public PerformHaplotypeAssociationTestTask(
            HaplotypeAssociationTest test,
            List<Integer> chromosomeNumbers)
    {
        this.test = test;
        this.chromosomeNumbers = chromosomeNumbers;
    }
    
    /**
     * Returns the chromosome number that will be processed on the next call
     * to {@link #nextElement()}
     * @return  the chromosome number
     */
    public int getNextChromosome()
    {
        return this.chromosomeNumbers.get(this.chromosomeIndex);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean hasMoreElements()
    {
        return !this.isComplete();
    }
    
    /**
     * {@inheritDoc}
     */
    public HaplotypeBlockTestResult[] nextElement()
    {
        HaplotypeBlockTestResult[] testResults = null;
        try
        {
            testResults = this.test.getHaplotypeTestResults(
                    this.getNextChromosome());
            Arrays.sort(testResults);
        }
        catch(Exception ex)
        {
            String errorMsg =
                "Encountered Error During Haplotype Association Test";
            LOG.log(Level.SEVERE,
                    errorMsg,
                    ex);
            MessageDialogUtilities.errorLater(
                    BhamApplication.getInstance().getBhamFrame(),
                    ex.getMessage(),
                    errorMsg);
        }
        finally
        {
            this.chromosomeIndex++;
            this.fireChangeEvent();
        }
        
        return testResults;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getTaskName()
    {
        if(!this.isComplete())
        {
            return
                this.test.getName() + " (Chr " +
                this.getNextChromosome() + ")";
        }
        else
        {
            return "Complete";
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getTotalWorkUnits()
    {
        return this.chromosomeNumbers.size();
    }

    /**
     * {@inheritDoc}
     */
    public int getWorkUnitsCompleted()
    {
        return this.chromosomeIndex;
    }
}
