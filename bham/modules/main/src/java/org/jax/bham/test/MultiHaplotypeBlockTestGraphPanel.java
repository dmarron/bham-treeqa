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
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.jax.bham.BhamApplication;
import org.jax.bham.util.JFreeChartUtil;
import org.jax.geneticutil.data.BasePairInterval;
import org.jax.geneticutil.data.CompositeRealValuedBasePairInterval;
import org.jax.geneticutil.data.MultiGroupStrainPartition;
import org.jax.geneticutil.data.RealValuedBasePairInterval;
import org.jax.geneticutil.gui.GoToMouseIntervalInCGDGBrowseAction;
import org.jax.geneticutil.gui.GoToMouseIntervalInCGDSnpDatabaseAction;
import org.jax.geneticutil.gui.GoToMouseIntervalInUCSCBrowserAction;
import org.jax.haplotype.analysis.experimentdesign.MultiHaplotypeBlockTest;
import org.jax.haplotype.analysis.experimentdesign.MultiHaplotypeBlockTestResult;
import org.jax.haplotype.analysis.visualization.ChromosomeHistogramValues;
import org.jax.haplotype.analysis.visualization.GenomicGraphFactory;
import org.jax.haplotype.analysis.visualization.HighlightedSnpInterval;
import org.jax.haplotype.analysis.visualization.OcclusionFilter;
import org.jax.util.concurrent.MultiTaskProgressPanel;
import org.jax.util.datastructure.SequenceUtilities;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * A panel for rendering a
 * {@link org.jax.haplotype.analysis.experimentdesign.MultiHaplotypeBlockTest}
 * results
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MultiHaplotypeBlockTestGraphPanel extends JPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -9040554034393108249L;
    
    private final GenomicGraphFactory graphFactory = new GenomicGraphFactory();
    
    private final MultiHaplotypeBlockTest testToPlot;

    private final JComboBox chromosomeComboBox;

    private final ChartPanel chartPanel = new ChartPanel(null, true)
    {
        /**
         * every {@link java.io.Serializable} is supposed to have one of these
         */
        private static final long serialVersionUID = 2353239091616674857L;

        /**
         * {@inheritDoc}
         */
        @Override
        protected void displayPopupMenu(int x, int y)
        {
            MultiHaplotypeBlockTestGraphPanel.this.updateClickPosition(x, y);
            super.displayPopupMenu(x, y);
        }
    };

    private volatile int lastClickedIntervalIndex;
    
    private volatile List<JComponent> lastMenuItems = Collections.emptyList();
    
    private final Map<Integer, RealValuedBasePairInterval[]> chromosomeToNegLogValueMap =
        Collections.synchronizedMap(new HashMap<Integer, RealValuedBasePairInterval[]>());
    
    /**
     * Constructor
     * @param testToPlot
     *          the test for this panel to plot
     */
    public MultiHaplotypeBlockTestGraphPanel(
            MultiHaplotypeBlockTest testToPlot)
    {
        super(new BorderLayout());
        
        this.testToPlot = testToPlot;
        this.chromosomeComboBox = new JComboBox();
        
        this.initialize();
    }
    
    /**
     * a function to initialize the components for this panel
     */
    private void initialize()
    {
        this.chromosomeComboBox.addItem("All Chromosomes");
        List<Integer> chromoList = SequenceUtilities.toIntegerList(
                this.testToPlot.getAvailableChromosomes());
        Collections.sort(chromoList);
        for(Integer chromoNum: chromoList)
        {
            this.chromosomeComboBox.addItem(chromoNum);
        }
        if(!chromoList.isEmpty())
        {
            this.chromosomeComboBox.setSelectedIndex(1);
        }
        this.chromosomeComboBox.addItemListener(new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    MultiHaplotypeBlockTestGraphPanel.this.chromosomeSelectionChanged();
                }
            }
        });
        
        JToolBar toolBar = new JToolBar();
        toolBar.add(new JLabel("Chromosome:"));
        
        // limit the size or the toolbar will try to make the drop-down huge
        this.chromosomeComboBox.setMaximumSize(
                this.chromosomeComboBox.getPreferredSize());
        toolBar.add(this.chromosomeComboBox);
        
        this.add(toolBar, BorderLayout.PAGE_START);
        
        this.add(this.chartPanel, BorderLayout.CENTER);
        
        this.chromosomeSelectionChanged();
    }
    
    private int[] getSelectedChromosomes()
    {
        // implementation assumes that the first item indicates that all
        // chromosomes should be displayed and that any other selection is a
        // specific chromosome
        if(this.chromosomeComboBox.getSelectedIndex() == 0)
        {
            // all are selected
            int[] allChromosomes = new int[this.chromosomeComboBox.getItemCount() - 1];
            
            for(int i = 0; i < allChromosomes.length; i++)
            {
                allChromosomes[i] = (Integer)this.chromosomeComboBox.getItemAt(i + 1);
            }
            
            return allChromosomes;
        }
        else
        {
            // a single chromosome is selected
            return new int[] {(Integer)this.chromosomeComboBox.getSelectedItem()};
        }
    }
    
    private void chromosomeSelectionChanged()
    {
        final int[] selectedChromosomes = this.getSelectedChromosomes();
        Thread runTestsThread = new Thread(new Runnable()
        {
            /**
             * {@inheritDoc}
             */
            public void run()
            {
                MultiHaplotypeBlockTestGraphPanel.this.cacheChromosomeTests(
                        selectedChromosomes);
                SwingUtilities.invokeLater(new Runnable()
                {
                    /**
                     * {@inheritDoc}
                     */
                    public void run()
                    {
                        MultiHaplotypeBlockTestGraphPanel.this.repaintGraphNow();
                    }
                });
            }
        });
        runTestsThread.start();
    }
    
    private void cacheChromosomeTests(final int[] chromosomes)
    {
        List<Integer> chromosomesToCalculate =
            SequenceUtilities.toIntegerList(chromosomes);
        chromosomesToCalculate.removeAll(this.chromosomeToNegLogValueMap.keySet());
        
        PerformSlidingWindowAssociationTestTask testTask =
            new PerformSlidingWindowAssociationTestTask(
                    this.testToPlot,
                    chromosomesToCalculate);
        MultiTaskProgressPanel progressTracker =
            BhamApplication.getInstance().getBhamFrame().getMultiTaskProgress();
        progressTracker.addTaskToTrack(testTask, true);
        
        while(testTask.hasMoreElements())
        {
            int nextChromosome = testTask.getNextChromosome();
            MultiHaplotypeBlockTestResult[] results = testTask.nextElement();
            
            List<MultiHaplotypeBlockTestResult> filteredResults =
                OcclusionFilter.filterOutOccludedIntervals(
                        Arrays.asList(results),
                        true);
            
            RealValuedBasePairInterval[] negLogResults =
                new RealValuedBasePairInterval[filteredResults.size()];
            for(int i = 0; i < negLogResults.length; i++)
            {
                negLogResults[i] = new CompositeRealValuedBasePairInterval(
                        filteredResults.get(i).getDelegateInterval(),
                        -Math.log10(filteredResults.get(i).getPValue()));
            }
            
            this.chromosomeToNegLogValueMap.put(
                    nextChromosome,
                    negLogResults);
        }
    }
    
    private void repaintGraphNow()
    {
        int[] chromosomes = this.getSelectedChromosomes();
        if(chromosomes.length == 1)
        {
            int chromosome = chromosomes[0];
            final RealValuedBasePairInterval[] intervals =
                this.chromosomeToNegLogValueMap.get(chromosome);
            
            if(intervals != null)
            {
                HighlightedSnpInterval highlightedSnpInterval =
                    new HighlightedSnpInterval(
                            0,
                            intervals.length,
                            new int[0]);
                
                long startPosition = intervals[0].getStartInBasePairs();
                long endPosition = startPosition;
                for(RealValuedBasePairInterval interval: intervals)
                {
                    long currEndPosition = interval.getEndInBasePairs();
                    if(currEndPosition > endPosition)
                    {
                        endPosition = currEndPosition;
                    }
                }
                JFreeChart jFreeChart = this.graphFactory.createSnpIntervalHistogram(
                        Arrays.asList(intervals),
                        startPosition,
                        1 + endPosition - startPosition,
                        highlightedSnpInterval,
                        "Base Pair Position",
                        "-log10(p-value)");
                
                jFreeChart.setTitle(
                        this.testToPlot.getName() + " - Chromosome " +
                        this.chromosomeComboBox.getSelectedItem());
                
                this.chartPanel.setChart(jFreeChart);
            }
        }
        else
        {
            List<ChromosomeHistogramValues> chromoHistos =
                new ArrayList<ChromosomeHistogramValues>();
            for(int chromosome: chromosomes)
            {
                final RealValuedBasePairInterval[] intervals =
                    this.chromosomeToNegLogValueMap.get(chromosome);
                
                if(intervals != null)
                {
                    HighlightedSnpInterval highlightedSnpInterval =
                        new HighlightedSnpInterval(
                                0,
                                intervals.length,
                                new int[0]);
                    
                    long startPosition = intervals[0].getStartInBasePairs();
                    long endPosition = startPosition;
                    for(RealValuedBasePairInterval interval: intervals)
                    {
                        long currEndPosition = interval.getEndInBasePairs();
                        if(currEndPosition > endPosition)
                        {
                            endPosition = currEndPosition;
                        }
                    }
                    ChromosomeHistogramValues chromosomeHistogramValues = new ChromosomeHistogramValues(
                            chromosome,
                            Arrays.asList(intervals),
                            startPosition,
                            1 + endPosition - startPosition,
                            highlightedSnpInterval);
                    chromoHistos.add(chromosomeHistogramValues);
                }
            }
            
            JFreeChart jFreeChart = this.graphFactory.createMultiChromosomeHistogram(
                    chromoHistos,
                    "Chromosome Base Pair Position",
                    "-log10(p-value)");
            
            jFreeChart.setTitle(
                    this.testToPlot.getName() + " - All Chromosomes");
            
            this.chartPanel.setChart(jFreeChart);
        }
    }
    
    private void updateClickPosition(int x, int y)
    {
        this.lastClickedIntervalIndex = this.getMaximalIntervalIndex(x, y);
        
        // remove the old menu items
        JPopupMenu popupMenu = this.chartPanel.getPopupMenu();
        for(int i = 0; i < this.lastMenuItems.size(); i++)
        {
            popupMenu.remove(0);
        }
        
        // now replace the old menus with some new ones
        this.lastMenuItems = this.createContextMenuItems();
        for(int i = 0; i < this.lastMenuItems.size(); i++)
        {
            popupMenu.insert(this.lastMenuItems.get(i), i);
        }
    }
    
    /**
     * Gets the index of the interval with the highest value that falls
     * under the given Java2D coordinates
     * @param x
     *          the Java2D X coordinate
     * @param y
     *          the Java2D Y coordinate
     * @return
     *          the interval index
     */
    private int getMaximalIntervalIndex(int x, int y)
    {
        int clickIndex = -1;
        
        int[] chromosomes = this.getSelectedChromosomes();
        
        if(chromosomes.length == 1)
        {
            RealValuedBasePairInterval[] selectedValuesList =
                this.chromosomeToNegLogValueMap.get(chromosomes[0]);
            
            if(selectedValuesList != null)
            {
                Point2D clickedGraphPoint = JFreeChartUtil.java2DPointToGraphPoint(
                        new Point(x, y),
                        this.chartPanel);
                
                // exhaustive search for the maximum clicked index (this could
                // be a binary search, but this seems to perform OK for now)
                double graphX = clickedGraphPoint.getX();
                int valueCount = selectedValuesList.length;
                double biggestClickedValue = Double.NEGATIVE_INFINITY;
                for(int i = 0; i < valueCount; i++)
                {
                    RealValuedBasePairInterval currValue = selectedValuesList[i];
                    if(currValue.getStartInBasePairs() < graphX &&
                       currValue.getEndInBasePairs() > graphX &&
                       currValue.getRealValue() > biggestClickedValue)
                    {
                        biggestClickedValue = currValue.getRealValue();
                        clickIndex = i;
                    }
                }
                
                // if we didn't click on anything grab the nearest item
                // (again this could and should be faster)
                if(clickIndex == -1 && valueCount >= 1)
                {
                    clickIndex = 0;
                    double nearestDistance = Math.min(
                            Math.abs(selectedValuesList[0].getStartInBasePairs() - graphX),
                            Math.abs(selectedValuesList[0].getEndInBasePairs() - graphX));
                    for(int i = 1; i < valueCount; i++)
                    {
                        BasePairInterval currValue = selectedValuesList[i];
                        double currDistance = Math.min(
                                Math.abs(currValue.getStartInBasePairs() - graphX),
                                Math.abs(currValue.getEndInBasePairs() - graphX));
                        if(currDistance < nearestDistance)
                        {
                            nearestDistance = currDistance;
                            clickIndex = i;
                        }
                    }
                }
            }
        }
        
        return clickIndex;
    }
    
    private List<JComponent> createContextMenuItems()
    {
        List<JComponent> menuItems = new ArrayList<JComponent>();
        
        int[] chromosomes = this.getSelectedChromosomes();
        if(this.lastClickedIntervalIndex >= 0 && chromosomes.length == 1)
        {
            RealValuedBasePairInterval[] intervals =
                this.chromosomeToNegLogValueMap.get(chromosomes[0]);
            RealValuedBasePairInterval selectedInterval =
                intervals[this.lastClickedIntervalIndex];
            menuItems.add(new JMenuItem(new GoToMouseIntervalInCGDSnpDatabaseAction(
                    selectedInterval,
                    BhamApplication.getInstance().getBhamFrame())));
            menuItems.add(new JMenuItem(new GoToMouseIntervalInCGDGBrowseAction(
                    selectedInterval,
                    BhamApplication.getInstance().getBhamFrame())));
            menuItems.add(new JMenuItem(new GoToMouseIntervalInUCSCBrowserAction(
                    selectedInterval,
                    BhamApplication.getInstance().getBhamFrame())));
            menuItems.add(new JSeparator());
            
            if(selectedInterval instanceof CompositeRealValuedBasePairInterval)
            {
                CompositeRealValuedBasePairInterval selectedCompositeInterval =
                    (CompositeRealValuedBasePairInterval)selectedInterval;
                if(selectedCompositeInterval.getDelegateInterval() instanceof MultiGroupStrainPartition)
                {
                    MultiGroupStrainPartition partition =
                        (MultiGroupStrainPartition)selectedCompositeInterval.getDelegateInterval();
                    Set<String> strains = this.testToPlot.getCommonStrains();
                    String[] sortedStrains = strains.toArray(new String[strains.size()]);
                    Arrays.sort(sortedStrains);
                    short[] strainGroups = partition.getStrainGroups();
                    Map<Short, Set<String>> numberedStrainGroups = new HashMap<Short, Set<String>>();
                    for(int strainIndex = 0; strainIndex < strainGroups.length; strainIndex++)
                    {
                        Set<String> strainsInGroup =
                            numberedStrainGroups.get(strainGroups[strainIndex]);
                        if(strainsInGroup == null)
                        {
                            strainsInGroup = new HashSet<String>();
                            numberedStrainGroups.put(strainGroups[strainIndex], strainsInGroup);
                        }
                        
                        strainsInGroup.add(sortedStrains[strainIndex]);
                    }
                    
                    // create a human readable name for the strain groups
                    Map<String, Set<String>> namedStrainGroups = new HashMap<String, Set<String>>();
                    for(Set<String> strainGroup: numberedStrainGroups.values())
                    {
                        String anyStrain = strainGroup.iterator().next();
                        String groupName = anyStrain + " Group";
                        
                        assert !namedStrainGroups.containsKey(groupName);
                        namedStrainGroups.put(groupName, strainGroup);
                    }
                    
                    menuItems.add(new JMenuItem(new ShowPhenotypeEffectPlotAction(
                            this.testToPlot.getPhenotypeDataSource(),
                            namedStrainGroups)));
                    menuItems.add(new JSeparator());
                }
            }
        }
        
        return menuItems;
    }
}
