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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jax.bham.project.BhamProject;
import org.jax.haplotype.analysis.experimentdesign.MPDIndividualPhenotypeDataSource;
import org.jax.haplotype.analysis.experimentdesign.MPDIndividualStrainPhenotypeParser;
import org.jax.haplotype.analysis.experimentdesign.SexFilter;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.gui.SimplifiedDocumentListener;

/**
 * A dialog for loading MPD phenotype data
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class LoadMPDIndividualPhenotypeDataSourceDialog extends JDialog
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -6945174643264764658L;
    
    private static final String NO_PHENOTYPES_AVAILABLE_STRING =
        "No Phenotypes Available";
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            LoadMPDIndividualPhenotypeDataSourceDialog.class.getName());
    
    /**
     * The parser that we use
     */
    private final MPDIndividualStrainPhenotypeParser phenoParser =
        new MPDIndividualStrainPhenotypeParser();

    private final BhamProject project;
    
    /**
     * Constructor
     * @param parent
     *          the parent frame for this dialog
     * @param project
     *          the project that this dialog should add phenotype data to
     */
    public LoadMPDIndividualPhenotypeDataSourceDialog(Frame parent, BhamProject project)
    {
        super(parent, "Load Individual Phenotype Data (Text Format)", false);
        
        this.project = project;
        
        this.initComponents();
        this.postGuiInit();
    }

    /**
     * Take care of the initialization that the GUI builder doesn't handle
     */
    private void postGuiInit()
    {
        this.phenotypeNameComboBox.addItem(NO_PHENOTYPES_AVAILABLE_STRING);
        
        this.sexFilterComboBox.addItem(SexFilter.AGNOSTIC);
        this.sexFilterComboBox.addItem(SexFilter.ALLOW_FEMALE);
        this.sexFilterComboBox.addItem(SexFilter.ALLOW_MALE);
        
        this.allStrainsList.setModel(new DefaultListModel());
        
        this.browseFilesButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                LoadMPDIndividualPhenotypeDataSourceDialog.this.browsePhenoFiles();
            }
        });
        
        this.okButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                LoadMPDIndividualPhenotypeDataSourceDialog.this.ok();
            }
        });
        
        this.cancelButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                LoadMPDIndividualPhenotypeDataSourceDialog.this.cancel();
            }
        });
        
        this.helpButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                LoadMPDIndividualPhenotypeDataSourceDialog.this.showHelp();
            }
        });
        
        this.delimitedFileTextField.getDocument().addDocumentListener(new SimplifiedDocumentListener()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            protected void anyUpdate(DocumentEvent e)
            {
                LoadMPDIndividualPhenotypeDataSourceDialog.this.phenotypeFileSelectionChanged();
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
                    LoadMPDIndividualPhenotypeDataSourceDialog.this.maybeSelectLoadSelectedButton();
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
                    LoadMPDIndividualPhenotypeDataSourceDialog.this.deselectAllStrains();
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

    private void phenotypeFileSelectionChanged()
    {
        DefaultListModel allStrainsModel =
            (DefaultListModel)this.allStrainsList.getModel();
        
        try
        {
            // handle strains
            {
                int oldSize = allStrainsModel.getSize();
                Set<String> oldStrains = new HashSet<String>(oldSize);
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
            
            // handle phenotypes
            {
                int oldSize = this.phenotypeNameComboBox.getItemCount();
                Set<String> oldPhenos = new HashSet<String>(oldSize);
                for(int i = 0; i < oldSize; i++)
                {
                    String currPheno =
                        (String)this.phenotypeNameComboBox.getItemAt(i);
                    
                    if(!currPheno.equals(NO_PHENOTYPES_AVAILABLE_STRING))
                    {
                        oldPhenos.add(currPheno);
                    }
                }
                
                // maybe we don't need an update. see if the old strains match the
                // new ones before we try
                Set<String> newPhenos = this.getAllPhenotypes();
                if(!oldPhenos.equals(newPhenos))
                {
                    // new and old are different so we have to do an update
                    this.phenotypeNameComboBox.removeAllItems();
                    if(newPhenos.isEmpty())
                    {
                        this.phenotypeNameComboBox.addItem(
                                NO_PHENOTYPES_AVAILABLE_STRING);
                    }
                    else
                    {
                        String[] sortedNewPhenos = newPhenos.toArray(
                                new String[newPhenos.size()]);
                        Arrays.sort(sortedNewPhenos);
                        for(int i = 0; i < sortedNewPhenos.length; i++)
                        {
                            this.phenotypeNameComboBox.addItem(
                                    sortedNewPhenos[i]);
                        }
                    }
                }
            }
        }
        catch(Exception ex)
        {
            LOG.log(Level.WARNING,
                    "Caught exception trying to update the strain and " +
                    "phenotype lists",
                    ex);
            allStrainsModel.clear();
            this.phenotypeNameComboBox.removeAllItems();
            this.phenotypeNameComboBox.addItem(NO_PHENOTYPES_AVAILABLE_STRING);
        }
    }
    
    private Set<String> getAllPhenotypes()
    {
        File phenoFile = this.getMPDIndividualPhenotypeFile();
        
        if(phenoFile != null && phenoFile.isFile())
        {
            try
            {
                InputStream phenoStream = new FileInputStream(phenoFile);
                return this.phenoParser.parseAvailablePhenotypes(phenoStream);
            }
            catch(IOException ex)
            {
                LOG.log(Level.INFO,
                        "Ignoring exception getting phenotypes. " +
                        "This is normal if the user is in the middle of " +
                        "typing out the file name.");
                
                return Collections.emptySet();
            }
        }
        else
        {
            return Collections.emptySet();
        }
    }
    
    private Set<String> getAllStrains()
    {
        File phenoFile = this.getMPDIndividualPhenotypeFile();
        
        if(phenoFile != null && phenoFile.isFile())
        {
            try
            {
                InputStream phenoStream = new FileInputStream(phenoFile);
                return this.phenoParser.parseAvailableStrainNames(phenoStream);
            }
            catch(IOException ex)
            {
                LOG.log(Level.INFO,
                        "Ignoring exception getting strain names. " +
                        "This is normal if the user is in the middle of " +
                        "typing out the file name.");
                
                return Collections.emptySet();
            }
        }
        else
        {
            return Collections.emptySet();
        }
    }

    private File getMPDIndividualPhenotypeFile()
    {
        return new File(this.delimitedFileTextField.getText().trim());
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
            Set<String> selectedStrains = new HashSet<String>(selectedIndices.length);
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
    
    private String getSelectedPhenotype()
    {
        String selectedPhenotype =
            (String)this.phenotypeNameComboBox.getSelectedItem();
        if(NO_PHENOTYPES_AVAILABLE_STRING.equals(selectedPhenotype))
        {
            return null;
        }
        else
        {
            return selectedPhenotype;
        }
    }
    
    private SexFilter getSelectedSexFilter()
    {
        return (SexFilter)this.sexFilterComboBox.getSelectedItem();
    }

    private void ok()
    {
        try
        {
            if(this.validateData())
            {
                MPDIndividualPhenotypeDataSource phenoDataSource =
                    new MPDIndividualPhenotypeDataSource(
                            this.nameTextField.getText().trim(),
                            this.getMPDIndividualPhenotypeFile(),
                            this.getSelectedPhenotype(),
                            this.getSelectedStrains(),
                            this.getSelectedSexFilter());
                this.project.addPhenotypeDataSource(phenoDataSource);
                
                this.dispose();
            }
        }
        catch(Exception ex)
        {
            String title = "Failed to Load Phenotype Data";
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
        File phenoFile = this.getMPDIndividualPhenotypeFile();
        if(!phenoFile.isFile())
        {
            errorMessage =
                "The given file \"" + phenoFile.getAbsolutePath() +
                "\" does not appear to be a valid file.";
        }
        //else if(this.getAllPhenotypes().isEmpty() || this.getAllStrains().isEmpty())
        else if(false)
        {
            errorMessage =
                "The given file \"" + phenoFile.getAbsolutePath() +
                "\" does not appear to be correctly formatted as a " +
                "tab-delimited file using the MPD format (see help " +
                "for details).";
        }
        else if(this.nameTextField.getText().trim().length() == 0)
        {
            errorMessage =
                "Please enter a name for the phenotype data before continuing";
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
    
    private void browsePhenoFiles()
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(
                "Select a Tab-Delimited Phenotype File (Text Format)");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(
                JFileChooser.FILES_ONLY);
        int outputUserSelection =
            fileChooser.showOpenDialog(this);
        
        if(outputUserSelection == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile =
                fileChooser.getSelectedFile();
            this.delimitedFileTextField.setText(
                    selectedFile.getAbsolutePath());
        }
        else
        {
            LOG.fine("user canceled phenotype file selection");
        }
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
        javax.swing.JLabel nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        javax.swing.JLabel delimitedFileLabel = new javax.swing.JLabel();
        delimitedFileTextField = new javax.swing.JTextField();
        browseFilesButton = new javax.swing.JButton();
        javax.swing.JLabel sexFilterLabel = new javax.swing.JLabel();
        sexFilterComboBox = new javax.swing.JComboBox();
        javax.swing.JLabel phenotypeNameLabel = new javax.swing.JLabel();
        phenotypeNameComboBox = new javax.swing.JComboBox();
        loadAllStrainsRadioButton = new javax.swing.JRadioButton();
        loadSelectedStrainsRadioButton = new javax.swing.JRadioButton();
        javax.swing.JScrollPane allStrainsScrollPane = new javax.swing.JScrollPane();
        allStrainsList = new javax.swing.JList();
        javax.swing.JPanel actionPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        nameLabel.setText("Data Source Name:");

        delimitedFileLabel.setText("Tab-Delimited File:");

        browseFilesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/browse-16x16.png"))); // NOI18N
        browseFilesButton.setText("Browse...");

        sexFilterLabel.setText("Sex Filter:");

        phenotypeNameLabel.setText("Phenotype Name:");

        strainSelectionButtonGroup.add(loadAllStrainsRadioButton);
        loadAllStrainsRadioButton.setSelected(true);
        loadAllStrainsRadioButton.setText("Load All Strains Shown Below");

        strainSelectionButtonGroup.add(loadSelectedStrainsRadioButton);
        loadSelectedStrainsRadioButton.setText("Only Load Selected Strains");

        allStrainsScrollPane.setViewportView(allStrainsList);

        okButton.setText("OK");
        actionPanel.add(okButton);

        cancelButton.setText("Cancel");
        actionPanel.add(cancelButton);

        helpButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/help-16x16.png"))); // NOI18N
        helpButton.setText("Help...");
        actionPanel.add(helpButton);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, actionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(nameLabel)
                    .add(delimitedFileLabel)
                    .add(phenotypeNameLabel)
                    .add(sexFilterLabel))
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(phenotypeNameComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                            .add(sexFilterComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap(260, Short.MAX_VALUE))
                        .add(layout.createSequentialGroup()
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(layout.createSequentialGroup()
                                    .add(delimitedFileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(browseFilesButton))
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE))
                            .add(27, 27, 27)))))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(loadAllStrainsRadioButton)
                .addContainerGap(228, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(loadSelectedStrainsRadioButton)
                .addContainerGap(245, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(allStrainsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
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
                    .add(delimitedFileLabel)
                    .add(browseFilesButton)
                    .add(delimitedFileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(sexFilterLabel)
                    .add(sexFilterComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(phenotypeNameLabel)
                    .add(phenotypeNameComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(loadAllStrainsRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(loadSelectedStrainsRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(allStrainsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(actionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList allStrainsList;
    private javax.swing.JButton browseFilesButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField delimitedFileTextField;
    private javax.swing.JButton helpButton;
    private javax.swing.JRadioButton loadAllStrainsRadioButton;
    private javax.swing.JRadioButton loadSelectedStrainsRadioButton;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox phenotypeNameComboBox;
    private javax.swing.JComboBox sexFilterComboBox;
    // End of variables declaration//GEN-END:variables

}
