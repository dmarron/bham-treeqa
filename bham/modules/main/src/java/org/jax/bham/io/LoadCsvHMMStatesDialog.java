package org.jax.bham.io;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;

import org.jax.bham.project.BhamProject;
import org.jax.haplotype.data.HiddenMarkovModelStateDataSource;
import org.jax.haplotype.io.GenomicFlatFileParser;
import org.jax.haplotype.io.HiddenMarkovModelStateParser;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.gui.SimplifiedDocumentListener;
import org.jax.util.io.CommonFlatFileFormat;
import org.jax.util.io.FlatFileReader;

/**
 * Dialog for converting a CSV HMM state data source into a binary
 * HMM state data source
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class LoadCsvHMMStatesDialog extends javax.swing.JDialog
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 8134074414517961697L;
    
    private static final Logger LOG = Logger.getLogger(
            LoadCsvHMMStatesDialog.class.getName());
    
    private static final String COLUMNS_NOT_AVAILABLE =
        "Select Files First";

    private final BhamProject project;
    
    /**
     * Constructor
     * @param parent
     *          parent component
     * @param project
     *          the project that we're loading HMM data into
     */
    public LoadCsvHMMStatesDialog(
            Frame parent,
            BhamProject project)
    {
        super(parent, "Convert CSV HMM Haplotype Data to Binary", false);
        
        this.project = project;
        this.initComponents();
        this.postGuiInit();
    }

    /**
     * Do initialization after GUI builder is done
     */
    private void postGuiInit()
    {
        this.loadFilesButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                LoadCsvHMMStatesDialog.this.browseFiles();
            }
        });
        
        this.helpButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                LoadCsvHMMStatesDialog.this.showHelp();
            }
        });
        
        this.okButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                LoadCsvHMMStatesDialog.this.ok();
            }
        });
        
        this.cancelButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                LoadCsvHMMStatesDialog.this.cancel();
            }
        });
        
        this.loadFilesTextField.getDocument().addDocumentListener(new SimplifiedDocumentListener()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            protected void anyUpdate(DocumentEvent e)
            {
                LoadCsvHMMStatesDialog.this.selectedFilesChanged();
            }
        });
        
        // to start with we have no valid columns to select
        this.setImportFileColumnNames(null);
        ItemListener chromoAndPositionItemListener = new ItemListener()
        {
            /**
             * {@inheritDoc}
             */
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    LoadCsvHMMStatesDialog.this.chromosomeOrPositionColumnChanged();
                }
            }
        };
        this.chromosomeColumnComboBox.addItemListener(chromoAndPositionItemListener);
        this.positionColumnComboBox.addItemListener(chromoAndPositionItemListener);
    }

    private void chromosomeOrPositionColumnChanged()
    {
        int maxChrPosIndex = Math.max(
                this.chromosomeColumnComboBox.getSelectedIndex(),
                this.positionColumnComboBox.getSelectedIndex());
        int firstStrainIndex = this.firstStrainComboBox.getSelectedIndex();
        if(maxChrPosIndex >= 0 && maxChrPosIndex >= firstStrainIndex)
        {
            int newIndex = maxChrPosIndex + 1;
            if(newIndex < this.firstStrainComboBox.getItemCount())
            {
                this.firstStrainComboBox.setSelectedIndex(newIndex);
            }
        }
    }

    /**
     * called when there is some change in the import files
     */
    private void selectedFilesChanged()
    {
        try
        {
            String[] importFileNames = this.getImportFileNames();
            
            if(importFileNames == null || importFileNames.length == 0)
            {
                this.setImportFileColumnNames(null);
            }
            else
            {
                File firstFile = new File(importFileNames[0]);
                if(!firstFile.isFile())
                {
                    this.setImportFileColumnNames(null);
                }
                else
                {
                    FlatFileReader csvReader = new FlatFileReader(
                            new FileReader(firstFile),
                            CommonFlatFileFormat.CSV_RFC_4180);
                    
                    String[] firstRow = csvReader.readRow();
                    this.setImportFileColumnNames(firstRow);
                    
                    csvReader.close();
                }
            }
        }
        catch(Exception ex)
        {
            LOG.log(Level.WARNING,
                    "Failed to \"pre-parse\" CSV import file",
                    ex);
            this.setImportFileColumnNames(null);
        }
    }
    
    private void setImportFileColumnNames(String[] columnNames)
    {
        this.chromosomeColumnComboBox.removeAllItems();
        this.positionColumnComboBox.removeAllItems();
        this.firstStrainComboBox.removeAllItems();
        if(columnNames == null || columnNames.length == 0)
        {
            this.chromosomeColumnComboBox.addItem(COLUMNS_NOT_AVAILABLE);
            this.positionColumnComboBox.addItem(COLUMNS_NOT_AVAILABLE);
            this.firstStrainComboBox.addItem(COLUMNS_NOT_AVAILABLE);
        }
        else
        {
            for(String columnName: columnNames)
            {
                this.chromosomeColumnComboBox.addItem(columnName);
                this.positionColumnComboBox.addItem(columnName);
                this.firstStrainComboBox.addItem(columnName);
            }
            
            // try to guess the right index to use
            int chrIndexGuess = GenomicFlatFileParser.guessIndexOfChromosomeIdFromHeader(
                    columnNames);
            if(chrIndexGuess >= 0)
            {
                this.chromosomeColumnComboBox.setSelectedIndex(chrIndexGuess);
            }
            
            int posIndexGuess = GenomicFlatFileParser.guessIndexOfBasePairPositionFromHeader(
                    columnNames);
            if(posIndexGuess >= 0)
            {
                this.positionColumnComboBox.setSelectedIndex(posIndexGuess);
            }
        }
    }

    private void cancel()
    {
        this.dispose();
    }

    private void ok()
    {
        try
        {
            if(this.validateData())
            {
                File[] importFiles = this.getImportFiles();
                
                HiddenMarkovModelStateParser parser = new HiddenMarkovModelStateParser(
                        this.firstStrainComboBox.getSelectedIndex(),
                        this.chromosomeColumnComboBox.getSelectedIndex(),
                        this.positionColumnComboBox.getSelectedIndex());
                HiddenMarkovModelStateDataSource dataSource =
                    new HiddenMarkovModelStateDataSource(
                            this.nameTextField.getText().trim(),
                            Arrays.asList(importFiles),
                            parser);
                this.project.addMultiGroupHaplotypeDataSource(
                        dataSource);
                
                this.dispose();
            }
        }
        catch(Exception ex)
        {
            String title = "Failed to Convert CSV Data";
            LOG.log(Level.SEVERE,
                    title,
                    ex);
            MessageDialogUtilities.error(
                    this,
                    ex.getMessage(),
                    title);
        }
    }

    private File[] getImportFiles()
    {
        String[] importFileNames = this.getImportFileNames();
        File[] importFiles = new File[importFileNames.length];
        for(int i = 0; i < importFileNames.length; i++)
        {
            importFiles[i] = new File(importFileNames[i]);
        }
        
        return importFiles;
    }

    private String[] getImportFileNames()
    {
        String importFilesString = this.loadFilesTextField.getText().trim();
        String[] importFileNames = importFilesString.split(",");
        for(int i = 0; i < importFileNames.length; i++)
        {
            importFileNames[i] = importFileNames[i].trim();
        }
        
        return importFileNames;
    }
    
    /**
     * Determine if the data is all valid
     * @return
     *          true if the data is valid
     */
    private boolean validateData()
    {
        String errorMessage = this.validateSelectedFiles();
        
        if(errorMessage == null)
        {
            int firstStrainIndex = this.firstStrainComboBox.getSelectedIndex();
            int chrIndex = this.chromosomeColumnComboBox.getSelectedIndex();
            int posIndex = this.positionColumnComboBox.getSelectedIndex();
            
            if(chrIndex == posIndex)
            {
                errorMessage =
                    "The base pair position and chromosome columns must " +
                    "be different! Please either change your column " +
                    "selections or reformat your file if necessary."; 
            }
            else if(firstStrainIndex <= Math.max(chrIndex, posIndex))
            {
                errorMessage =
                    "The first strain column must occur after the " +
                    "base pair position and chromosome columns. Please " +
                    "either change your column selections or reformat " +
                    "your file if necessary.";
            }
            else if(this.nameTextField.getText().trim().length() == 0)
            {
                errorMessage =
                    "Please enter a name for the data source before continuing";
            }
        }
        
        if(errorMessage != null)
        {
            MessageDialogUtilities.warn(
                    this,
                    errorMessage,
                    "Validation Failed");
            return false;
        }
        else
        {
            return true;
        }
    }
    
    private String validateSelectedFiles()
    {
        String[] importFileNames = this.getImportFileNames();
        if(importFileNames == null || importFileNames.length == 0)
        {
            return "The list of import files cannot be empty.";
        }
        else
        {
            for(int i = 0; i < importFileNames.length; i++)
            {
                if(importFileNames[i].length() == 0)
                {
                    return "Found an empty filename in the import list.";
                }
                else
                {
                    File currImportFile = new File(importFileNames[i]);
                    if(!currImportFile.exists())
                    {
                        return "\"" + importFileNames[i] + "\" is missing.";
                    }
                    else if(!currImportFile.isFile())
                    {
                        return "\"" + importFileNames[i] + "\" does not have " +
                        	   "a normal file type.";
                    }
                }
            }
        }
        
        return null;
    }

    private void showHelp()
    {
        MessageDialogUtilities.inform(
                this,
                "Sorry, no help yet..",
                "Help Not Implemented");
    }

    private void browseFiles()
    {
        JFileChooser inputFileChooser = new JFileChooser();
        inputFileChooser.setDialogTitle("Select CSV Chromosome Input Files");
        inputFileChooser.setMultiSelectionEnabled(true);
        inputFileChooser.setFileSelectionMode(
                JFileChooser.FILES_ONLY);
        int userSelection = inputFileChooser.showOpenDialog(this);
        if(userSelection == JFileChooser.APPROVE_OPTION)
        {
            File[] selectedInputFiles = inputFileChooser.getSelectedFiles();
            StringBuffer selectedFilesText = new StringBuffer();
            for(int i = 0; i < selectedInputFiles.length; i++)
            {
                if(i >= 1)
                {
                    selectedFilesText.append(", ");
                }
                
                selectedFilesText.append(
                        selectedInputFiles[i].getAbsolutePath());
            }
            
            this.loadFilesTextField.setText(selectedFilesText.toString());
        }
        else
        {
            LOG.fine("user canceled import file selection");
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

        javax.swing.JPanel contentPanel = new javax.swing.JPanel();
        javax.swing.JLabel nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        javax.swing.JLabel loadFilesLabel = new javax.swing.JLabel();
        loadFilesTextField = new javax.swing.JTextField();
        loadFilesButton = new javax.swing.JButton();
        javax.swing.JLabel chromosomeColumnLabel = new javax.swing.JLabel();
        chromosomeColumnComboBox = new javax.swing.JComboBox();
        javax.swing.JLabel positionColumnLabel = new javax.swing.JLabel();
        positionColumnComboBox = new javax.swing.JComboBox();
        javax.swing.JLabel firstStrainLabel = new javax.swing.JLabel();
        firstStrainComboBox = new javax.swing.JComboBox();
        javax.swing.JPanel actionPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        nameLabel.setText("Data Source Name:");

        loadFilesLabel.setText("CSV File(s) To Load:");

        loadFilesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/browse-16x16.png"))); // NOI18N
        loadFilesButton.setText("Browse...");

        chromosomeColumnLabel.setText("Chromosome Column:");

        positionColumnLabel.setText("Base Pair Column:");

        firstStrainLabel.setText("First Strain Column:");

        org.jdesktop.layout.GroupLayout contentPanelLayout = new org.jdesktop.layout.GroupLayout(contentPanel);
        contentPanel.setLayout(contentPanelLayout);
        contentPanelLayout.setHorizontalGroup(
            contentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(contentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(contentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(loadFilesLabel)
                    .add(chromosomeColumnLabel)
                    .add(positionColumnLabel)
                    .add(firstStrainLabel)
                    .add(nameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(contentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                    .add(firstStrainComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(positionColumnComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(chromosomeColumnComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(contentPanelLayout.createSequentialGroup()
                        .add(loadFilesTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(loadFilesButton)))
                .addContainerGap())
        );
        contentPanelLayout.setVerticalGroup(
            contentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(contentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(contentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabel)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(contentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(loadFilesLabel)
                    .add(loadFilesTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(loadFilesButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(contentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(chromosomeColumnComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(chromosomeColumnLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(contentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(positionColumnLabel)
                    .add(positionColumnComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(contentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(firstStrainLabel)
                    .add(firstStrainComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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
            .add(actionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
            .add(contentPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(contentPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(actionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox chromosomeColumnComboBox;
    private javax.swing.JComboBox firstStrainComboBox;
    private javax.swing.JButton helpButton;
    private javax.swing.JButton loadFilesButton;
    private javax.swing.JTextField loadFilesTextField;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox positionColumnComboBox;
    // End of variables declaration//GEN-END:variables

}
