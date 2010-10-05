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

import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jax.bham.BhamApplication;
import org.jax.geneticutil.data.BasePairInterval;
import org.jax.geneticutil.data.PartitionedIntervalSet;
import org.jax.haplotype.analysis.experimentdesign.HaplotypeDataSource;
import org.jax.util.concurrent.AbstractLongRunningTask;
import org.jax.util.datastructure.SequenceUtilities;
import org.jax.util.datastructure.SetUtilities;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.io.FlatFileWriter;

/**
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ExportHaplotypeBlocksToFlatFileTask
extends AbstractLongRunningTask
implements Runnable
{
    private static final Logger LOG = Logger.getLogger(
            ExportHaplotypeBlocksToFlatFileTask.class.getName());
    
    private final HaplotypeDataSource haplotypeData;
    
    private final FlatFileWriter flatFileWriter;
    
    private final boolean closeWriterWhenFinished;
    
    private volatile int workUnitsCompleted = 0;

    /**
     * Constructor
     * @param haplotypeData
     *          the haplotype data to export
     * @param flatFileWriter
     *          the flat file that we're writing to
     * @param closeWriterWhenFinished
     *          should the given writer be closed when this task is done
     *          writing to it?
     */
    public ExportHaplotypeBlocksToFlatFileTask(
            HaplotypeDataSource haplotypeData,
            FlatFileWriter flatFileWriter,
            boolean closeWriterWhenFinished)
    {
        this.haplotypeData = haplotypeData;
        this.flatFileWriter = flatFileWriter;
        this.closeWriterWhenFinished = closeWriterWhenFinished;
    }
    
    /**
     * {@inheritDoc}
     */
    public void run()
    {
        try
        {
            List<PartitionedIntervalSet> haplotypeBlocks =
                this.haplotypeData.getHaplotypeEquivalenceClassData(null);
            
            Set<String> strains = this.haplotypeData.getAvailableStrains();
            String[] sortedStrains = strains.toArray(new String[strains.size()]);
            Arrays.sort(sortedStrains);
            
            Writer plainWriter = this.flatFileWriter.getWriter();
            plainWriter.write("# Haplotype strains (same ordering as bit set)\n");
            String strainsString = SequenceUtilities.toString(
                    Arrays.asList(sortedStrains),
                    ", ");
            plainWriter.write("# " + strainsString + "\n");
            
            this.flatFileWriter.writeRow(new String[] {
                    "chromosomeNumber",
                    "haplotypeBlockStartPositionInBasePairs",
                    "haplotypeBlockEndPositionInBasePairs",
                    "strainsInHaplotypeBlockBitSet"});
            for(PartitionedIntervalSet haploEquivClass: haplotypeBlocks)
            {
                for(BasePairInterval interval: haploEquivClass.getSnpIntervals())
                {
                    this.flatFileWriter.writeRow(new String[] {
                            Integer.toString(interval.getChromosomeNumber()),
                            Long.toString(interval.getStartInBasePairs()),
                            Long.toString(interval.getEndInBasePairs()),
                            SetUtilities.bitSetToBinaryString(haploEquivClass.getStrainBitSet())});
                }
            }
            this.flatFileWriter.flush();
            
            if(this.closeWriterWhenFinished)
            {
                this.flatFileWriter.close();
            }
        }
        catch(Exception ex)
        {
            String title = "Failed to Export Haplotype Blocks";
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
        return "Exporting " + this.haplotypeData.getName();
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