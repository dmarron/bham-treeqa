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

package org.jax.bham;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JSeparator;

import org.jax.bham.infer.InferHaplotypeBlocksAction;
import org.jax.bham.infer.InferPhylogenyAction;
import org.jax.bham.infer.SlidingWindowHaplotypeInferenceAction;
import org.jax.bham.io.ConvertCsvGenotypeDataToBinaryAction;
import org.jax.bham.io.ExportHaplotypeAssociationTestResultsAction;
import org.jax.bham.io.ExportHaplotypeBlocksToFlatFileAction;
import org.jax.bham.io.ExportMultiGroupHaplotypeAssociationTestResultsAction;
import org.jax.bham.io.ExportPhylogenyAssociationTestResultsAction;
import org.jax.bham.io.ExportPhylogenyToNewickAction;
import org.jax.bham.io.LoadBinaryGenotypeDataSourceAction;
import org.jax.bham.io.LoadCsvHMMStatesAction;
import org.jax.bham.io.LoadMPDIndividualPhenotypeDataSourceAction;
import org.jax.bham.project.LoadBhamProjectAction;
import org.jax.bham.project.SaveBhamProjectAction;
import org.jax.bham.project.SaveBhamProjectAsAction;
import org.jax.bham.test.HaplotypeAssociationTestAction;
import org.jax.bham.test.MultiGroupHaplotypeAssociationTestAction;
import org.jax.bham.test.PhylogenyAssociationTestAction;
import org.jax.util.gui.desktoporganization.Desktop;

/**
 * This class provides access to the main menu singleton for BHAM
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class BhamMainMenuManager
{
    private final JMenuBar menuBar;
    
    /**
     * Constructor
     * @param desktop the desktop panel
     */
    public BhamMainMenuManager(Desktop desktop)
    {
        this.menuBar = new JMenuBar();
        this.initializeMenus(desktop);
    }

    /**
     * Add the menu items
     * @param desktop the desktop panel
     */
    private void initializeMenus(Desktop desktop)
    {
        final JMenu fileMenu = new JMenu("File");
        
        fileMenu.add(new SaveBhamProjectAction());
        fileMenu.add(new SaveBhamProjectAsAction());
        
        fileMenu.add(new JSeparator());
        fileMenu.add(new LoadBhamProjectAction());
        
        fileMenu.add(new JSeparator());
        fileMenu.add(new ConvertCsvGenotypeDataToBinaryAction());
        
        fileMenu.add(new JSeparator());
        fileMenu.add(new LoadBinaryGenotypeDataSourceAction());
        fileMenu.add(new LoadCsvHMMStatesAction());
        fileMenu.add(new LoadMPDIndividualPhenotypeDataSourceAction());
        
        fileMenu.add(new JSeparator());
        fileMenu.add(new ExportHaplotypeBlocksToFlatFileAction());
        fileMenu.add(new ExportPhylogenyToNewickAction());
        fileMenu.add(new ExportHaplotypeAssociationTestResultsAction());
        fileMenu.add(new ExportPhylogenyAssociationTestResultsAction());
        fileMenu.add(new ExportMultiGroupHaplotypeAssociationTestResultsAction());
        
        fileMenu.add(new JSeparator());
        fileMenu.add(new AbstractAction("Quit")
        {
            /**
             * every serializable is supposed to have one of these
             */
            private static final long serialVersionUID = 4194872123928292004L;

            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                // TODO be smarter about quiting (see j-maanova code)
                System.exit(0);
            }
        });
        this.menuBar.add(fileMenu);
        
        final JMenu inferenceMenu = new JMenu("Inference");
        inferenceMenu.add(new InferHaplotypeBlocksAction());
        inferenceMenu.add(new SlidingWindowHaplotypeInferenceAction());
        inferenceMenu.add(new InferPhylogenyAction());
        this.menuBar.add(inferenceMenu);
        
        final JMenu testMenu = new JMenu("Test");
        testMenu.add(new HaplotypeAssociationTestAction());
        testMenu.add(new MultiGroupHaplotypeAssociationTestAction());
        testMenu.add(new PhylogenyAssociationTestAction());
        this.menuBar.add(testMenu);
        
        this.menuBar.add(desktop.getWindowMenu());
    }
    
    /**
     * Getter for the menu bar
     * @return the menu bar
     */
    public JMenuBar getMenuBar()
    {
        return this.menuBar;
    }
}
