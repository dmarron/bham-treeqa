/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.bham.io;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JFileChooser;

import org.jax.bham.BhamApplication;
import org.jax.bham.project.BhamProject;
import org.jax.haplotype.analysis.experimentdesign.PhylogenyDataSource;
import org.jax.util.concurrent.MultiTaskProgressPanel;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.io.CommonFlatFileFormat;
import org.jax.util.io.FlatFileFormat;
import org.jax.util.io.FlatFileWriter;

/**
 * A dialog for exporting phylogeny data to the newick format
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ExportPhylogenyToNewickDialog extends JDialog
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -1940230316225163444L;

    private static final Logger LOG = Logger.getLogger(
            ExportPhylogenyToNewickDialog.class.getName());
    
    private static final String MULTI_STRAIN_SEPARATE =
        "Separate Strain Names With a Pipe \"|\"";
    
    private static final String MULTI_STRAIN_RESOLVE =
        "Resolve Strains With 0-Length Branches";
    
    private final BhamProject project;
    
    /**
     * Constructor
     * @param parent
     *          the parent frame
     * @param project
     *          the project
     */
    public ExportPhylogenyToNewickDialog(Frame parent, BhamProject project)
    {
        super(parent, "Export Phylogeny Data to Newick Format");
        
        this.project = project;
        this.initComponents();
        this.postGuiInit();
    }

    /**
     * Take care of the initialization that was not handled by the GUI builder
     */
    private void postGuiInit()
    {
        this.outputFileButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                ExportPhylogenyToNewickDialog.this.browseOutputFiles();
            }
        });
        
        this.okButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                ExportPhylogenyToNewickDialog.this.ok();
            }
        });
        
        this.cancelButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                ExportPhylogenyToNewickDialog.this.cancel();
            }
        });
        
        this.helpButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                ExportPhylogenyToNewickDialog.this.showHelp();
            }
        });
        
        for(PhylogenyDataSource phyloData: this.project.getPhylogenyDataSources())
        {
            this.phylogenyDataComboBox.addItem(phyloData);
        }
        
        this.fileFormatComboBox.addItem(CommonFlatFileFormat.CSV_UNIX);
        this.fileFormatComboBox.addItem(CommonFlatFileFormat.TAB_DELIMITED_UNIX);
        
        this.forMultiStrainNodesComboBox.addItem(MULTI_STRAIN_SEPARATE);
        this.forMultiStrainNodesComboBox.addItem(MULTI_STRAIN_RESOLVE);
    }
    
    private boolean getFullyResolveStrains()
    {
        return this.forMultiStrainNodesComboBox.getSelectedItem().equals(
                MULTI_STRAIN_RESOLVE);
    }
    
    private FlatFileFormat getFileFormat()
    {
        return (FlatFileFormat)this.fileFormatComboBox.getSelectedItem();
    }
    
    private PhylogenyDataSource getSelectedPhylogenyData()
    {
        return (PhylogenyDataSource)this.phylogenyDataComboBox.getSelectedItem();
    }
    
    private void ok()
    {
        try
        {
            if(this.validateData())
            {
                Writer writer = new BufferedWriter(
                        new FileWriter(this.getOutputFile()));
                FlatFileFormat format = this.getFileFormat();
                FlatFileWriter flatFileWriter = new FlatFileWriter(
                        writer,
                        format);
                PhylogenyDataSource phyloData = this.getSelectedPhylogenyData();
                ExportPhylogenyToNewickTask exportTask =
                    new ExportPhylogenyToNewickTask(
                            phyloData,
                            this.getFullyResolveStrains(),
                            flatFileWriter,
                            true);
                MultiTaskProgressPanel progressTracker =
                    BhamApplication.getInstance().getBhamFrame().getMultiTaskProgress();
                progressTracker.addTaskToTrack(exportTask, true);
                new Thread(exportTask).start();
                
                this.dispose();
            }
        }
        catch(Exception ex)
        {
            String title = "Failed to Export Phylogenetic Data";
            LOG.log(Level.SEVERE,
                    title,
                    ex);
            MessageDialogUtilities.error(
                    this,
                    ex.getMessage(),
                    title);
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
    
    private void browseOutputFiles()
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(
                "Output File");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(
                JFileChooser.FILES_ONLY);
        int outputUserSelection =
            fileChooser.showSaveDialog(this);
        
        if(outputUserSelection == JFileChooser.APPROVE_OPTION)
        {
            File selectedOutputFile =
                fileChooser.getSelectedFile();
            this.outputFileTextField.setText(
                    selectedOutputFile.getAbsolutePath());
        }
        else
        {
            LOG.fine("user canceled output file selection");
        }
    }

    private boolean validateData()
    {
        String errorMessage = null;
        File outputFile = this.getOutputFile();
        if(outputFile.isDirectory())
        {
            errorMessage =
                "Cannot write output to \"" + outputFile.getAbsolutePath() +
                "\" because it is a directory";
        }
        else if(outputFile.isFile())
        {
            boolean overwrite = MessageDialogUtilities.ask(
                    this,
                    "\"" + outputFile.getAbsolutePath() + "\" already exists. " +
                    "Would you like to overwrite this file?",
                    "Confim File Overwrite");
            if(!overwrite)
            {
                return false;
            }
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
    
    private File getOutputFile()
    {
        String fileName = this.outputFileTextField.getText().trim();
        return new File(fileName);
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

        javax.swing.JLabel outputFileLabel = new javax.swing.JLabel();
        outputFileTextField = new javax.swing.JTextField();
        outputFileButton = new javax.swing.JButton();
        phylogenyDataLabel = new javax.swing.JLabel();
        phylogenyDataComboBox = new javax.swing.JComboBox();
        actionPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();
        javax.swing.JLabel fileFormatLabel = new javax.swing.JLabel();
        fileFormatComboBox = new javax.swing.JComboBox();
        forMultiStrainNodesLabel = new javax.swing.JLabel();
        forMultiStrainNodesComboBox = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        outputFileLabel.setText("Output File:");

        outputFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/browse-16x16.png"))); // NOI18N
        outputFileButton.setText("Browse...");

        phylogenyDataLabel.setText("Phylogeny Data:");

        okButton.setText("OK");
        actionPanel.add(okButton);

        cancelButton.setText("Cancel");
        actionPanel.add(cancelButton);

        helpButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/help-16x16.png"))); // NOI18N
        helpButton.setText("Help...");
        actionPanel.add(helpButton);

        fileFormatLabel.setText("File Format:");

        forMultiStrainNodesLabel.setText("For Multi-Strain Nodes:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(phylogenyDataLabel)
                    .add(outputFileLabel)
                    .add(fileFormatLabel)
                    .add(forMultiStrainNodesLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(forMultiStrainNodesComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                            .add(fileFormatComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap(261, Short.MAX_VALUE))
                        .add(layout.createSequentialGroup()
                            .add(outputFileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(outputFileButton)
                            .add(27, 27, 27))
                        .add(layout.createSequentialGroup()
                            .add(phylogenyDataComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap(261, Short.MAX_VALUE)))))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, actionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(outputFileLabel)
                    .add(outputFileButton)
                    .add(outputFileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fileFormatLabel)
                    .add(fileFormatComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(phylogenyDataLabel)
                    .add(phylogenyDataComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(forMultiStrainNodesLabel)
                    .add(forMultiStrainNodesComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 8, Short.MAX_VALUE)
                .add(actionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox fileFormatComboBox;
    private javax.swing.JComboBox forMultiStrainNodesComboBox;
    private javax.swing.JLabel forMultiStrainNodesLabel;
    private javax.swing.JButton helpButton;
    private javax.swing.JButton okButton;
    private javax.swing.JButton outputFileButton;
    private javax.swing.JTextField outputFileTextField;
    private javax.swing.JComboBox phylogenyDataComboBox;
    private javax.swing.JLabel phylogenyDataLabel;
    // End of variables declaration//GEN-END:variables

}
