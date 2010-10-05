/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.bham.io;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jax.bham.project.BhamProject;
import org.jax.haplotype.data.GenomeDataSource;
//import org.jax.haplotype.data.StreamingBinaryChromosomeDataSource;
import org.jax.haplotype.data.CommaSeparatedChromosomeDataSource;
import org.jax.haplotype.io.SnpStreamUtil;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.gui.SimplifiedDocumentListener;

/**
 * Dialog for loading a binary genotype datasource
 * @see ConvertCsvGenotypeDataToBinaryDialog
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class LoadBinaryGenotypeDataSourceDialog extends JDialog
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 8662677707199432019L;

    private static final Logger LOG = Logger.getLogger(
            LoadBinaryGenotypeDataSourceDialog.class.getName());
    
    private final BhamProject project;
    
    /**
     * Constructor
     * @param parent
     *          the parent frame
     * @param project
     *          the project
     */
    public LoadBinaryGenotypeDataSourceDialog(Frame parent, BhamProject project)
    {
        super(parent, "Load Genotype Data (csv)", false);
        this.project = project;
        
        this.initComponents();
        this.postGuiInit();
    }

    /**
     * take care of the initialization not handled by the GUI builder
     */
    private void postGuiInit()
    {
        this.allStrainsList.setModel(new DefaultListModel());
        
        //this.dataDirButton.addActionListener(new ActionListener()
        this.fileButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                //LoadBinaryGenotypeDataSourceDialog.this.browseDataDirs();
                LoadBinaryGenotypeDataSourceDialog.this.browseFiles();
            }
        });
        
        this.okButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                LoadBinaryGenotypeDataSourceDialog.this.ok();
            }
        });
        
        this.cancelButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                LoadBinaryGenotypeDataSourceDialog.this.cancel();
            }
        });
        
        this.helpButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                LoadBinaryGenotypeDataSourceDialog.this.showHelp();
            }
        });
        
        //this.dataDirTextField.getDocument().addDocumentListener(new SimplifiedDocumentListener()
        this.fileTextField.getDocument().addDocumentListener(new SimplifiedDocumentListener()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            protected void anyUpdate(DocumentEvent e)
            {
                LoadBinaryGenotypeDataSourceDialog.this.updateUnfilteredStrains();
            }
        });
        
        this.allStrainsList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void valueChanged(ListSelectionEvent e)
            {
                // if the user made a selection make sure that
                // the right radio button is selected
                if(!e.getValueIsAdjusting())
                {
                    LoadBinaryGenotypeDataSourceDialog.this.maybeSelectLoadSelectedButton();
                }
            }
        });
        
        this.loadAllStrainsRadioButton.addItemListener(new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    LoadBinaryGenotypeDataSourceDialog.this.deselectAllStrains();
                }
            }
        });
    }
    
    private void deselectAllStrains()
    {
        this.allStrainsList.clearSelection();
    }
    
    /**
     * This method ensures that if any strains are selected then the
     * {@link #loadSelectedStrainsRadioButton} will be selected
     */
    private void maybeSelectLoadSelectedButton()
    {
        if(!this.allStrainsList.isSelectionEmpty())
        {
            this.loadSelectedStrainsRadioButton.setSelected(true);
        }
    }

    private void updateUnfilteredStrains()
    {
        DefaultListModel allStrainsModel =
            (DefaultListModel)this.allStrainsList.getModel();
        
        try
        {
            int oldSize = allStrainsModel.getSize();
            //Set<String> oldStrains = new HashSet<String>(oldSize);
            Set<String> oldStrains = new TreeSet<String>();
            for(int i = 0; i < oldSize; i++)
            {
                oldStrains.add((String)allStrainsModel.get(i));
            }
            
            // maybe we don't need an update. see if the old strains match the
            // new ones before we try
            Set<String> newStrains = this.getAllStrains();
            if(!oldStrains.equals(newStrains))
            {
                // new and old are different so we have to do an update
                allStrainsModel.clear();
                String[] sortedNewStrains = newStrains.toArray(new String[newStrains.size()]);
                Arrays.sort(sortedNewStrains);
                for(int i = 0; i < sortedNewStrains.length; i++)
                {
                    allStrainsModel.addElement(sortedNewStrains[i]);
                }
            }
        }
        catch(Exception ex)
        {
            LOG.log(Level.WARNING,
                    "Caught exception trying to update the strain list",
                    ex);
            allStrainsModel.clear();
        }
    }
    
    private Set<String> getAllStrains()
    {
        //Map<Integer, StreamingBinaryChromosomeDataSource> chromosomeDataSources =
            //this.getChromosomeDataSources();
        Map<Integer, CommaSeparatedChromosomeDataSource> chromosomeDataSources =
            this.getChromosomeDataSources();
        
        if(chromosomeDataSources == null || chromosomeDataSources.isEmpty())
        {
            return Collections.emptySet();
        }
        else
        {
            //StreamingBinaryChromosomeDataSource anyChromosome =
                //chromosomeDataSources.values().iterator().next();
            CommaSeparatedChromosomeDataSource anyChromosome =
                chromosomeDataSources.values().iterator().next();
            return anyChromosome.getAvailableStrains();
        }
    }

    //private File getDataDirectory()
    private File getFile()
    {
        //return new File(this.dataDirTextField.getText().trim());
        return new File(this.fileTextField.getText().trim());
    }
    
    /*private Map<Integer, StreamingBinaryChromosomeDataSource> getChromosomeDataSources()
    {
        return SnpStreamUtil.getBinaryChromosomeDataSources(
                this.getDataDirectory());
    }*/
    private Map<Integer, CommaSeparatedChromosomeDataSource> getChromosomeDataSources()
    {
	Map<Integer, CommaSeparatedChromosomeDataSource> output = new TreeMap<Integer, CommaSeparatedChromosomeDataSource>();
	CommaSeparatedChromosomeDataSource dataSource = new CommaSeparatedChromosomeDataSource(this.getFile(),-1);
	ArrayList<Integer> allChromNums = dataSource.getAllChromosomeNumbers();
	for (int i = 0; i < allChromNums.size(); i++) {
		output.put(allChromNums.get(i), new CommaSeparatedChromosomeDataSource(this.getFile(),allChromNums.get(i).intValue()));
	}
	//output.put(new Integer(dataSource.getChromosomeNumber()),dataSource);
	//output.put(new Integer(19),dataSource);
	return output;
    }

    /**
     * Get the selected strains
     * @return
     *          the selected strains or return null to indicate that all
     *          should be selected
     */
    private Set<String> getSelectedStrains()
    {
        if(this.loadSelectedStrainsRadioButton.isSelected())
        {
            int[] selectedIndices = this.allStrainsList.getSelectedIndices();
            //Set<String> selectedStrains = new HashSet<String>(selectedIndices.length);
            Set<String> selectedStrains = new TreeSet<String>();
            for(int i = 0; i < selectedIndices.length; i++)
            {
                selectedStrains.add((String)this.allStrainsList.getModel().getElementAt(
                        selectedIndices[i]));
            }
            
            return selectedStrains;
        }
        else
        {
            return null;
        }
    }

    private void ok()
    {
        try
        {
            if(this.validateData())
            {
                //Map<Integer, StreamingBinaryChromosomeDataSource> chromosomeDataSources =
                    //this.getChromosomeDataSources();
                Map<Integer, CommaSeparatedChromosomeDataSource> chromosomeDataSources =
                    this.getChromosomeDataSources();
                Set<String> selectedStrains = this.getSelectedStrains();
                if(selectedStrains != null)
                {
                    //for(StreamingBinaryChromosomeDataSource chromoDataSource:
                    //    chromosomeDataSources.values())
                    for(CommaSeparatedChromosomeDataSource chromoDataSource:
                        chromosomeDataSources.values())
                    {
                        //chromoDataSource.setPersistentStrainsToAcceptFilter(
                        //        selectedStrains);
                    }
                }
                
                GenomeDataSource genomeDataSource = new GenomeDataSource(
                        this.nameTextField.getText().trim(),
                        this.ncbiBuildVersionTextField.getText().trim(),
                        chromosomeDataSources);
                this.project.addGenomeDataSource(
                        genomeDataSource);
                
                this.dispose();
            }
        }
        catch(Exception ex)
        {
            String title = "Failed to Load Binary Genome Data";
            LOG.log(Level.SEVERE,
                    title,
                    ex);
            MessageDialogUtilities.error(
                    this,
                    ex.getMessage(),
                    title);
        }
    }
    
    private boolean validateData()
    {
        String errorMessage = null;
        //File dataDir = this.getDataDirectory();
        File dataDir = this.getFile();
        //if(!dataDir.isDirectory())
	if (false)
        {
            errorMessage =
                "The given data directory \"" + dataDir.getAbsolutePath() +
                "\" does not appear to be a valid directory";
        }
        else if(this.nameTextField.getText().trim().length() == 0)
        {
            errorMessage =
                "Please enter a name for the genotype data before continuing";
        }
        else if(this.loadSelectedStrainsRadioButton.isSelected() &&
                this.allStrainsList.isSelectionEmpty())
        {
            errorMessage =
                "Please either select strains to load or select the " +
                "\"Load All...\" option";
        }
        
        if(errorMessage == null)
        {
            return true;
        }
        else
        {
            MessageDialogUtilities.warn(
                    this,
                    errorMessage,
                    "Invalid User Input");
            return false;
        }
    }

    private void cancel()
    {
        this.dispose();
    }
    
    private void showHelp()
    {
        MessageDialogUtilities.inform(
                this,
                "Sorry, no help yet..",
                "Help Not Implemented");
    }
    
    //private void browseDataDirs()
    private void browseFiles()
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(
                "Select a Genotype File (CSV format)");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(
                JFileChooser.FILES_ONLY);
        int outputUserSelection =
            fileChooser.showOpenDialog(this);
        
        if(outputUserSelection == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile =
                fileChooser.getSelectedFile();
            this.fileTextField.setText(
                    selectedFile.getAbsolutePath());
        }
        else
        {
            LOG.fine("user canceled genotype file selection");
        }
        /*JFileChooser dataDirectoryChooser = new JFileChooser();
        dataDirectoryChooser.setDialogTitle(
                "Select a Binary Genotype Data Directory");
        dataDirectoryChooser.setMultiSelectionEnabled(false);
        dataDirectoryChooser.setFileSelectionMode(
                JFileChooser.DIRECTORIES_ONLY);
        int outputUserSelection =
            dataDirectoryChooser.showOpenDialog(this);
        
        if(outputUserSelection == JFileChooser.APPROVE_OPTION)
        {
            File selectedDataDirectory =
                dataDirectoryChooser.getSelectedFile();
            this.dataDirTextField.setText(
                    selectedDataDirectory.getAbsolutePath());
        }
        else
        {
            LOG.fine("user canceled binary genotype data dir selection");
        }*/
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("all")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.ButtonGroup strainSelectionButtonGroup = new javax.swing.ButtonGroup();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        //javax.swing.JLabel dataDirLabel = new javax.swing.JLabel();
        //dataDirTextField = new javax.swing.JTextField();
        //dataDirButton = new javax.swing.JButton();
	javax.swing.JLabel fileLabel = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        fileButton = new javax.swing.JButton();
        ncbiBuildVersionLabel = new javax.swing.JLabel();
        ncbiBuildVersionTextField = new javax.swing.JTextField();
        actionPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();
        loadAllStrainsRadioButton = new javax.swing.JRadioButton();
        loadSelectedStrainsRadioButton = new javax.swing.JRadioButton();
        javax.swing.JScrollPane allStrainsScrollPane = new javax.swing.JScrollPane();
        allStrainsList = new javax.swing.JList();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        nameLabel.setText("Data Source Name:");

        //dataDirLabel.setText("Data Directory:");
        //fileLabel.setText("Data Directory:");
        fileLabel.setText("File:");


        //dataDirButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/browse-16x16.png"))); // NOI18N
        //dataDirButton.setText("Browse...");
        fileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/browse-16x16.png"))); // NOI18N
        fileButton.setText("Browse...");

        ncbiBuildVersionLabel.setText("NCBI Build Version:");

        ncbiBuildVersionTextField.setText("Unspecified");

        okButton.setText("OK");
        actionPanel.add(okButton);

        cancelButton.setText("Cancel");
        actionPanel.add(cancelButton);

        helpButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/help-16x16.png"))); // NOI18N
        helpButton.setText("Help...");
        actionPanel.add(helpButton);

        strainSelectionButtonGroup.add(loadAllStrainsRadioButton);
        loadAllStrainsRadioButton.setSelected(true);
        loadAllStrainsRadioButton.setText("Load All Strains Shown Below");

        strainSelectionButtonGroup.add(loadSelectedStrainsRadioButton);
        loadSelectedStrainsRadioButton.setText("Only Load Selected Strains");

        allStrainsScrollPane.setViewportView(allStrainsList);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(nameLabel)
                    //.add(dataDirLabel)
                    .add(fileLabel)
                    .add(ncbiBuildVersionLabel))
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(ncbiBuildVersionTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        //.add(dataDirTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                        .add(fileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        //.add(dataDirButton))
                        .add(fileButton))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE))
                .add(27, 27, 27))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, actionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(loadAllStrainsRadioButton)
                .addContainerGap(235, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(loadSelectedStrainsRadioButton)
                .addContainerGap(252, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(allStrainsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabel)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    //.add(dataDirLabel)
                    //.add(dataDirButton)
                    //.add(dataDirTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(fileLabel)
                    .add(fileButton)
                    .add(fileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ncbiBuildVersionLabel)
                    .add(ncbiBuildVersionTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(loadAllStrainsRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(loadSelectedStrainsRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(allStrainsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(actionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionPanel;
    private javax.swing.JList allStrainsList;
    private javax.swing.JButton cancelButton;
    //private javax.swing.JButton dataDirButton;
    //private javax.swing.JTextField dataDirTextField;
    private javax.swing.JButton fileButton;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JButton helpButton;
    private javax.swing.JRadioButton loadAllStrainsRadioButton;
    private javax.swing.JRadioButton loadSelectedStrainsRadioButton;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JLabel ncbiBuildVersionLabel;
    private javax.swing.JTextField ncbiBuildVersionTextField;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables

}
