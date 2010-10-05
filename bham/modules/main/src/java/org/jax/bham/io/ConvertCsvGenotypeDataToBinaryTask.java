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

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jax.bham.BhamApplication;
import org.jax.haplotype.io.GenotypeParser;
import org.jax.haplotype.io.SnpStreamUtil;
import org.jax.util.concurrent.AbstractLongRunningTask;
import org.jax.util.gui.MessageDialogUtilities;

/**
 * Long running task for converting CSV genotype data to binary data
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ConvertCsvGenotypeDataToBinaryTask
extends AbstractLongRunningTask
implements Runnable
{
    private static final Logger LOG = Logger.getLogger(
            ConvertCsvGenotypeDataToBinaryTask.class.getName());
    
    private static final String TASK_NAME = "Converting Genotype Data";
    
    private final File[] importFiles;
    
    private final File exportDirectory;
    
    private volatile int importFilesProcessed = 0;

    private final GenotypeParser parser;
    
    /**
     * Constructor
     * @param parser
     *          the parser to use
     * @param importFiles
     *          the files to import
     * @param exportDirectory
     *          the directory to export to
     */
    public ConvertCsvGenotypeDataToBinaryTask(
            GenotypeParser parser,
            File[] importFiles,
            File exportDirectory)
    {
        this.parser = parser;
        this.importFiles = importFiles;
        this.exportDirectory = exportDirectory;
    }

    /**
     * {@inheritDoc}
     */
    public String getTaskName()
    {
        return TASK_NAME;
    }
    
    /**
     * {@inheritDoc}
     */
    public int getTotalWorkUnits()
    {
        return this.importFiles.length;
    }
    
    /**
     * {@inheritDoc}
     */
    public int getWorkUnitsCompleted()
    {
        return this.importFilesProcessed;
    }
    
    /**
     * {@inheritDoc}
     */
    public void run()
    {
        try
        {
            for(File importFile: this.importFiles)
            {
                SnpStreamUtil.writeBinaryChromosomeData(
                        this.parser,
                        importFile,
                        this.exportDirectory);
                this.importFilesProcessed++;
                this.fireChangeEvent();
            }
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "failed to import genotype data",
                    ex);
            MessageDialogUtilities.errorLater(
                    BhamApplication.getInstance().getBhamFrame(),
                    ex.getMessage(),
                    "Failed To Import Genotype Data");
            
            // it might not be pretty, but it is complete...
            this.importFilesProcessed = this.getTotalWorkUnits();
            this.fireChangeEvent();
        }
    }
}
