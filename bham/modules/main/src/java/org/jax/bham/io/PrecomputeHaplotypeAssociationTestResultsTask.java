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

package org.jax.bham.io;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jax.bham.BhamApplication;
import org.jax.haplotype.analysis.experimentdesign.CachingHaplotypeAssociationTest;
import org.jax.util.concurrent.AbstractLongRunningTask;
import org.jax.util.gui.MessageDialogUtilities;

/**
 * A long running task for precomputing haplotype association test results
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class PrecomputeHaplotypeAssociationTestResultsTask
extends AbstractLongRunningTask
implements Runnable
{
    private static final Logger LOG = Logger.getLogger(
            PrecomputeHaplotypeAssociationTestResultsTask.class.getName());
    
    private final CachingHaplotypeAssociationTest haplotypeTest;
    
    private volatile int workUnitsCompleted = 0;

    /**
     * Constructor
     * @param haplotypeTest
     *          the haplotype association test data to export
     */
    public PrecomputeHaplotypeAssociationTestResultsTask(
            CachingHaplotypeAssociationTest haplotypeTest)
    {
        this.haplotypeTest = haplotypeTest;
    }
    
    /**
     * {@inheritDoc}
     */
    public void run()
    {
        try
        {
            this.haplotypeTest.getEquivalenceClassTestResults();
        }
        catch(Exception ex)
        {
            String title = "Failed to Precompute Haplotype Test Results";
            LOG.log(Level.SEVERE,
                    title,
                    ex);
            MessageDialogUtilities.errorLater(
                    BhamApplication.getInstance().getBhamFrame(),
                    ex.getMessage(),
                    title);
        }
        finally
        {
            // no matter what we need to finish up
            this.workUnitsCompleted = 1;
            this.fireChangeEvent();
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getTaskName()
    {
        return "Precomputing Results For: " + this.haplotypeTest.getName();
    }

    /**
     * {@inheritDoc}
     */
    public int getTotalWorkUnits()
    {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    public int getWorkUnitsCompleted()
    {
        return this.workUnitsCompleted;
    }
}
