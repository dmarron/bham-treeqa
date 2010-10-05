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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.jax.bham.infer.PlotPhylogeneticTreeAction;
import org.jax.bham.util.JFreeChartUtil;
import org.jax.geneticutil.data.BasePairInterval;
import org.jax.geneticutil.data.CompositeRealValuedBasePairInterval;
import org.jax.geneticutil.data.RealValuedBasePairInterval;
import org.jax.geneticutil.gui.GoToMouseIntervalInCGDGBrowseAction;
import org.jax.geneticutil.gui.GoToMouseIntervalInCGDSnpDatabaseAction;
import org.jax.geneticutil.gui.GoToMouseIntervalInUCSCBrowserAction;
import org.jax.haplotype.analysis.experimentdesign.PhylogenyAssociationTest;
import org.jax.haplotype.analysis.visualization.ChromosomeHistogramValues;
import org.jax.haplotype.analysis.visualization.GenomicGraphFactory;
import org.jax.haplotype.analysis.visualization.HighlightedSnpInterval;
import org.jax.haplotype.analysis.visualization.OcclusionFilter;
import org.jax.haplotype.phylogeny.data.PhylogenyTestResult;
import org.jax.haplotype.phylogeny.data.PhylogenyTreeEdge;
import org.jax.haplotype.phylogeny.data.PhylogenyTreeNode;
import org.jax.util.concurrent.MultiTaskProgressPanel;
import org.jax.util.datastructure.SequenceUtilities;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * A panel for graphing phylogeney association results
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class PhylogenyAssociationTestGraphPanel extends JPanel
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 618496235032301159L;
    
    private final PhylogenyAssociationTest testToPlot;
    
    private final GenomicGraphFactory graphFactory = new GenomicGraphFactory();
    
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
            PhylogenyAssociationTestGraphPanel.this.updateClickPosition(x, y);
            super.displayPopupMenu(x, y);
        }
    };
    
    private volatile int lastClickedIntervalIndex;
    
    private volatile List<JComponent> lastMenuItems = Collections.emptyList();
    
    private final Map<Integer, List<PhylogenyTestResult>> chromosomeResultsCache =
        Collections.synchronizedMap(new HashMap<Integer, List<PhylogenyTestResult>>());

    private Map<Integer, List<PhylogenyTestResult>> treeqaResultsCache =
        Collections.synchronizedMap(new HashMap<Integer, List<PhylogenyTestResult>>());

    /**
     * Constructor
     * @param testToPlot
     *          the test that we're plotting
     */
    public PhylogenyAssociationTestGraphPanel(
            PhylogenyAssociationTest testToPlot)
    {
        super(new BorderLayout());
        
        this.testToPlot = testToPlot;
        this.chromosomeComboBox = new JComboBox();
        
        this.initialize();
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
            List<PhylogenyTestResult> selectedPhyloAssociationTests =
                this.chromosomeResultsCache.get(chromosomes[0]);
            
            if(selectedPhyloAssociationTests != null)
            {
                RealValuedBasePairInterval[] selectedValuesList =
                    selectedPhyloAssociationTests.toArray(
                            new RealValuedBasePairInterval[selectedPhyloAssociationTests.size()]);
                
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
    
    /**
     * a function to initialize the components for this panel
     */
    private void initialize()
    {
        this.chromosomeComboBox.addItem("All Chromosomes");
        List<Integer> chromoList = SequenceUtilities.toIntegerList(
                this.testToPlot.getPhylogenyDataSource().getAvailableChromosomes());
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
                    PhylogenyAssociationTestGraphPanel.this.chromosomeSelectionChanged();
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
                PhylogenyAssociationTestGraphPanel.this.cacheChromosomeTests(
                        selectedChromosomes);
                SwingUtilities.invokeLater(new Runnable()
                {
                    /**
                     * {@inheritDoc}
                     */
                    public void run()
                    {
                        PhylogenyAssociationTestGraphPanel.this.repaintGraphNow();
                    }
                });
            }
        });
        runTestsThread.start();
    }
    
    private void cacheChromosomeTests(
            final int[] chromosomes)
    {
        List<Integer> chromosomesToCalculate =
            SequenceUtilities.toIntegerList(chromosomes);
        chromosomesToCalculate.removeAll(this.chromosomeResultsCache.keySet());
        
        PerformPhylogenyAssociationTestTask testTask =
            new PerformPhylogenyAssociationTestTask(
                    this.testToPlot,
                    chromosomesToCalculate);
        MultiTaskProgressPanel progressTracker =
            BhamApplication.getInstance().getBhamFrame().getMultiTaskProgress();
        progressTracker.addTaskToTrack(testTask, true);
        
        while(testTask.hasMoreElements())
        {
            int nextChromosome = testTask.getNextChromosome();
            List<PhylogenyTestResult> results = testTask.nextElement();
            List<PhylogenyTestResult> treeqaResults = testTask.nextElement(true);

            
            /*List<PhylogenyTestResult> filteredResults = OcclusionFilter.filterOutOccludedIntervals(
                    results,
                    true);
            
            PhylogenyAssociationTestGraphPanel.this.chromosomeResultsCache.put(
                    nextChromosome,
                    filteredResults);*/

            PhylogenyAssociationTestGraphPanel.this.chromosomeResultsCache.put(
                    nextChromosome,
                    results);

            PhylogenyAssociationTestGraphPanel.this.treeqaResultsCache.put(
                    nextChromosome,
                    treeqaResults);

        }
    }
    
    private RealValuedBasePairInterval[] toNegLogResults(List<PhylogenyTestResult> filteredResults)
    {
        RealValuedBasePairInterval[] negLogResults =
            new RealValuedBasePairInterval[filteredResults.size()];
        for(int i = 0; i < negLogResults.length; i++)
        {
            negLogResults[i] = new CompositeRealValuedBasePairInterval(
                    filteredResults.get(i).getPhylogenyInterval().getInterval(),
                    //-Math.log10(filteredResults.get(i).getPValue()));
                    filteredResults.get(i).getPValue());
        }
        
        return negLogResults;
    }
    
    private void repaintGraphNow()
    {
        int[] chromosomes = this.getSelectedChromosomes();
        if(chromosomes.length == 1)
        {
            final List<PhylogenyTestResult> intervals =
                this.chromosomeResultsCache.get(chromosomes[0]);
            if(intervals != null)
            {
                RealValuedBasePairInterval[] negLog10Intervals =
                    this.toNegLogResults(intervals);
                HighlightedSnpInterval highlightedSnpInterval =
                    new HighlightedSnpInterval(
                            0,
                            negLog10Intervals.length,
                            new int[0]);
                
                long startPosition = negLog10Intervals[0].getStartInBasePairs();
                long endPosition = startPosition;
                for(RealValuedBasePairInterval interval: negLog10Intervals)
                {
                    long currEndPosition = interval.getEndInBasePairs();
                    if(currEndPosition > endPosition)
                    {
                        endPosition = currEndPosition;
                    }
                }
                JFreeChart jFreeChart = this.graphFactory.createSnpIntervalHistogram(
                        Arrays.asList(negLog10Intervals),
                        startPosition,
                        1 + endPosition - startPosition,
                        highlightedSnpInterval,
                        "Base Pair Position",
                        //"-log10(p-value)");
                        "p-value");
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
                    this.toNegLogResults(this.chromosomeResultsCache.get(chromosome));
                
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
                    //"-log10(p-value)");
		    "p-value");
            
            jFreeChart.setTitle(
                    this.testToPlot.getName() + " - All Chromosomes");
            
            this.chartPanel.setChart(jFreeChart);
        }
    }
    
    private List<JComponent> createContextMenuItems()
    {
        List<JComponent> menuItems = new ArrayList<JComponent>();
        
        int[] chromosomes = this.getSelectedChromosomes();
        if(this.lastClickedIntervalIndex >= 0 && chromosomes.length == 1)
        {
            PhylogenyTestResult selectedInterval =
                this.chromosomeResultsCache.get(chromosomes[0]).get(
                        this.lastClickedIntervalIndex);

            PhylogenyTestResult treeqaInterval =
                this.treeqaResultsCache.get(chromosomes[0]).get(
                        this.lastClickedIntervalIndex);
            
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
            
            PhylogenyTreeNode phylogenyTree =
                selectedInterval.getPhylogenyInterval().getPhylogeny();

            PhylogenyTreeNode phylogenyTreeqaTree =
                treeqaInterval.getPhylogenyInterval().getPhylogeny();

            //menuItems.add(new JMenuItem(new PlotPhylogeneticTreeAction(
            //        phylogenyTree,
            //        this.testToPlot)));

            menuItems.add(new JMenuItem(new PlotPhylogeneticTreeAction(
                    phylogenyTree,
                    phylogenyTreeqaTree,
                    this.testToPlot)));
            
            //Map<String, List<String>> strainGroups = this.extractStrainGroups(
                    //phylogenyTree);
            Map<String, List<String>> strainGroups = this.modifiedExtractStrainGroups(phylogenyTreeqaTree);
            menuItems.add(new JMenuItem(new ShowPhenotypeEffectPlotAction(
                    this.testToPlot.getPhenotypeDataSource(),
                    strainGroups)));
        }
        
        return menuItems;
    }

    /**
     * Get all of the strain groups from the given tree
     * @param phylogenyTree
     *          the tree
     * @return
     *          the strains for each node
     */
    private Map<String, List<String>> extractStrainGroups(
            PhylogenyTreeNode phylogenyTree)
    {
        Map<String, List<String>> strainGroups =
            new HashMap<String, List<String>>();
	List<String> strains = phylogenyTree.getStrains();
        if(!strains.isEmpty())
        {
            assert(!strainGroups.containsKey(strains.get(0)));
		System.out.println(strains.get(0));
            strainGroups.put(
                    strains.get(0) + " Group",
                    strains);
        }
        
        List<PhylogenyTreeEdge> children = phylogenyTree.getChildEdges();
        for(PhylogenyTreeEdge childEdge: children)
        {
            strainGroups.putAll(this.extractStrainGroups(
                    childEdge.getNode()));
        }
        return strainGroups;
    }
    private Map<String, List<String>> modifiedExtractStrainGroups(
            PhylogenyTreeNode phylogenyTree)
    {
	String treeToParse = phylogenyTree.resolveToSingleStrainLeafNodes(0.0).toNewickFormat();
	System.out.println("parsing " + treeToParse);
	String[] groups = treeToParse.split("\\)");
	Map<String, List<String>> strainGroups = new HashMap<String, List<String>>();

	for (String splitgroups: groups) {
		splitgroups = splitgroups.replace("(","");
		splitgroups = splitgroups.replace(":","");
		splitgroups = splitgroups.replace(";","");
		splitgroups = splitgroups.replaceAll("\\d+\\.\\d","");
		if (splitgroups.length() > 0) {
			if (splitgroups.charAt(0) == ',') {
				splitgroups = splitgroups.substring(1);
			}

			String[] commaSplitGroups = splitgroups.split(",");
			ArrayList<String> outputList = new ArrayList<String>();
			for (int i = 0; i < commaSplitGroups.length; i++) {
				outputList.add(commaSplitGroups[i]);
			}
			strainGroups.put(outputList.get(0) + " Group",outputList);
		}
	}
        return strainGroups;
    }
}
