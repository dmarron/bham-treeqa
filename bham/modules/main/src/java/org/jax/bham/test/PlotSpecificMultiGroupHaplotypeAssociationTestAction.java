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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.jax.bham.BhamApplication;
import org.jax.haplotype.analysis.experimentdesign.MultiGroupHaplotypeAssociationTest;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.gui.desktoporganization.Desktop;

/**
 * Graph a given mult group association test
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class PlotSpecificMultiGroupHaplotypeAssociationTestAction extends AbstractAction
{
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            PlotSpecificMultiGroupHaplotypeAssociationTestAction.class.getName());
    
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -9054618788766176542L;
    
    private final MultiGroupHaplotypeAssociationTest testToPlot;
    
    /**
     * Constructor
     * @param testToPlot
     *          the test that we want to plot
     */
    public PlotSpecificMultiGroupHaplotypeAssociationTestAction(
            MultiGroupHaplotypeAssociationTest testToPlot)
    {
        super("Plot " + testToPlot.getName());
        this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
        this.testToPlot = testToPlot;
    }
    
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        try
        {
            MultiHaplotypeBlockTestGraphPanel hapAssocGraphPanel =
                new MultiHaplotypeBlockTestGraphPanel(this.testToPlot);
            Desktop desktop = BhamApplication.getInstance().getBhamFrame().getDesktop();
            desktop.createInternalFrame(
                    hapAssocGraphPanel,
                    "Multi-Group Haplotype Association",
                    null,
                    "multi group assoc graph: " + this.testToPlot.getName());
        }
        catch(Exception ex)
        {
            String title = "Error Plotting Multi-Group Association Test";
            LOG.log(Level.SEVERE,
                    title,
                    ex);
            MessageDialogUtilities.errorLater(
                    BhamApplication.getInstance().getBhamFrame(),
                    ex.getMessage(),
                    title);
        }
    }
}
