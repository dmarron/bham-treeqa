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

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jax.bham.BhamApplication;
import org.jax.geneticutil.data.BasePairInterval;
import org.jax.haplotype.analysis.experimentdesign.PhylogenyDataSource;
import org.jax.haplotype.phylogeny.data.PhylogenyInterval;
import org.jax.haplotype.phylogeny.data.PhylogenyTreeNode;
import org.jax.util.concurrent.AbstractLongRunningTask;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.io.FlatFileWriter;

/**
 * Export the given phylogeny tree to newick format
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ExportPhylogenyToNewickTask
extends AbstractLongRunningTask
implements Runnable
{
    private static final Logger LOG = Logger.getLogger(
            ExportPhylogenyToNewickTask.class.getName());
    
    private final PhylogenyDataSource phylogenyData;
    
    private final FlatFileWriter flatFileWriter;
    
    private volatile int workUnitsCompleted = 0;

    private final boolean fullyResolveTrees;

    private final boolean closeWriterWhenFinished;
    
    /**
     * Constructor
     * @param phylogenyData
     *          the phylogeny data to export
     * @param fullyResolveTrees
     *          if true than strains on the same node will be given a
     *          pseudo branch with length 0, otherwise they will just be
     *          separated with a '|'
     * @param flatFileWriter
     *          the flat file that we're writing to
     * @param closeWriterWhenFinished
     *          should the given writer be closed when this task is done
     *          writing to it?
     */
    public ExportPhylogenyToNewickTask(
            PhylogenyDataSource phylogenyData,
            boolean fullyResolveTrees,
            FlatFileWriter flatFileWriter,
            boolean closeWriterWhenFinished)
    {
        this.phylogenyData = phylogenyData;
        this.fullyResolveTrees = fullyResolveTrees;
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
            Map<Integer, List<PhylogenyInterval>> phylogenyIntervals =
                this.phylogenyData.getPhylogenyData(null);
            
            this.flatFileWriter.writeRow(new String[] {
                    "chromosomeNumber",
                    "phylogenyIntervalStartPositionInBasePairs",
                    "phylogenyIntervalEndPositionInBasePairs",
                    "newickFormattedPhylogenyTree"});
            for(List<PhylogenyInterval> currPhyloIntervals: phylogenyIntervals.values())
            {
                for(PhylogenyInterval currPhyloInterval: currPhyloIntervals)
                {
                    BasePairInterval interval = currPhyloInterval.getInterval();
                    PhylogenyTreeNode phyloTree = currPhyloInterval.getPhylogeny();
                    if(this.fullyResolveTrees)
                    {
                        phyloTree = phyloTree.resolveToSingleStrainLeafNodes(0.0);
                    }
                    
                    this.flatFileWriter.writeRow(new String[] {
                            Integer.toString(interval.getChromosomeNumber()),
                            Long.toString(interval.getStartInBasePairs()),
                            Long.toString(interval.getEndInBasePairs()),
                            phyloTree.toNewickFormat()});
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
            String title = "Failed to Export Phylogeny trees";
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
        return "Exporting " + this.phylogenyData.getName();
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
