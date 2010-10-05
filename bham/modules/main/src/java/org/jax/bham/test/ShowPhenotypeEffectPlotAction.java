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
import java.util.Collection;
import java.util.Map;

import javax.swing.AbstractAction;

import org.jax.bham.BhamApplication;
import org.jax.haplotype.analysis.experimentdesign.PhenotypeDataSource;
import org.jax.util.gui.desktoporganization.Desktop;

/**
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ShowPhenotypeEffectPlotAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 518046828168502354L;

    private final PhenotypeDataSource phenotypeDataSource;
    
    private final Map<String, ? extends Collection<String>> strainGroups;
    
    /**
     * Constructor
     * @param phenotypeDataSource
     *          the phenotype data source
     * @param strainGroups
     *          the strain groups
     */
    public ShowPhenotypeEffectPlotAction(
            PhenotypeDataSource phenotypeDataSource,
            Map<String, ? extends Collection<String>> strainGroups)
    {
        super("Show Phenotype Effect Plot");
        this.phenotypeDataSource = phenotypeDataSource;
        this.strainGroups = strainGroups;
    }

    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        PhenotypeEffectPlotPanel phenotypeEffectPlotPanel =
            new PhenotypeEffectPlotPanel(
                    this.phenotypeDataSource,
                    this.strainGroups);
        
        Desktop desktop =
            BhamApplication.getInstance().getBhamFrame().getDesktop();
        desktop.createInternalFrame(
                phenotypeEffectPlotPanel,
                "Phenotype Effect Plot",
                null,
                "phenoplot");
    }
}
