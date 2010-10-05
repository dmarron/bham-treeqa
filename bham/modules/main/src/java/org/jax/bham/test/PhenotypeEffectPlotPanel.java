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

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.jax.haplotype.analysis.experimentdesign.PhenotypeDataSource;
import org.jax.util.datastructure.SequenceUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

/**
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class PhenotypeEffectPlotPanel extends JPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 3819240536305070987L;
    
    private final ChartPanel chartPanel;
    
    private final JCheckBox showIndividualStrainEffectsCheckBox;

    private final PhenotypeDataSource phenotypeDataSource;

    private final Map<String, List<Double>> phenotypeData;
    
    private final Map<String, ? extends Collection<String>> strainGroups;

    /**
     * Constructor
     * @param phenotypeDataSource
     *          the phenotype data source
     * @param strainGroups
     *          the strain groupings
     */
    public PhenotypeEffectPlotPanel(
            PhenotypeDataSource phenotypeDataSource,
            Map<String, ? extends Collection<String>> strainGroups)
    {
        super(new BorderLayout());
        
        this.chartPanel = new ChartPanel(null, true);
        this.showIndividualStrainEffectsCheckBox = new JCheckBox(
                "Show Individual Strain Effects",
                false);
        this.phenotypeDataSource = phenotypeDataSource;
        this.phenotypeData = phenotypeDataSource.getPhenotypeData();
        this.strainGroups = strainGroups;
        
        this.updateChart();
        
        JToolBar toolBar = new JToolBar();
        toolBar.add(this.showIndividualStrainEffectsCheckBox);
        this.showIndividualStrainEffectsCheckBox.addItemListener(new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                PhenotypeEffectPlotPanel.this.updateChart();
            }
        });
        
        this.add(toolBar, BorderLayout.PAGE_START);
        this.add(this.chartPanel, BorderLayout.CENTER);
    }
    
    private void updateChart()
    {
        BoxAndWhiskerCategoryDataset dataset = this.createDataset();
        JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
                "Phenotype Effect Plot",
                "Groups",
                this.phenotypeDataSource.getName(),
                dataset,
                true);
        CategoryPlot plot = (CategoryPlot)chart.getPlot();
        BoxAndWhiskerRenderer renderer =
            (BoxAndWhiskerRenderer)plot.getRenderer();
        renderer.setMaximumBarWidth(0.05);
        
        this.chartPanel.setChart(chart);
    }

    private BoxAndWhiskerCategoryDataset createDataset()
    {
        DefaultBoxAndWhiskerCategoryDataset dataset =
            new DefaultBoxAndWhiskerCategoryDataset();
        
        if(this.showIndividualStrainEffectsCheckBox.isSelected())
        {
            for(Entry<String, ? extends Collection<String>> strainGroupEntry: this.strainGroups.entrySet())
            {
                for(String strain: strainGroupEntry.getValue())
                {
                    dataset.add(
                            this.phenotypeData.get(strain),
                            strain,
                            strainGroupEntry.getKey());
                }
            }
        }
        else
        {
            Map<String, Double> meanPhenoData = new HashMap<String, Double>(
                    this.phenotypeData.size());
            for(Entry<String, List<Double>> phenoEntry: this.phenotypeData.entrySet())
            {
                if(!phenoEntry.getValue().isEmpty())
                {
                    double sum = 0.0;
                    for(double value: phenoEntry.getValue())
                    {
                        sum += value;
                    }
                    double mean = sum / phenoEntry.getValue().size();
                    
                    meanPhenoData.put(phenoEntry.getKey(), mean);
                }
            }
            
            for(Entry<String, ? extends Collection<String>> strainGroupEntry: this.strainGroups.entrySet())
            {
                List<Double> valueList = new ArrayList<Double>(
                        strainGroupEntry.getValue().size());
                for(String strain: strainGroupEntry.getValue())
                {
                    valueList.add(meanPhenoData.get(strain));
                }
                dataset.add(
                        valueList,
                        SequenceUtilities.toString(strainGroupEntry.getValue(), ", "),
                        strainGroupEntry.getKey());
            }
        }
        
        return dataset;
    }
}
