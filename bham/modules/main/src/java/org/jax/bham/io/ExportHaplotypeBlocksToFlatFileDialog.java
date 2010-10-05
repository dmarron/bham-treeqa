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
import org.jax.haplotype.analysis.experimentdesign.HaplotypeDataSource;
import org.jax.util.concurrent.MultiTaskProgressPanel;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.io.CommonFlatFileFormat;
import org.jax.util.io.FlatFileFormat;
import org.jax.util.io.FlatFileWriter;

/**
 * A dialog for exporting haplotype block data
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class ExportHaplotypeBlocksToFlatFileDialog extends JDialog
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 6717729767563207908L;

    private static Logger LOG = Logger.getLogger(
            ExportHaplotypeBlocksToFlatFileDialog.class.getName());
    
    private final BhamProject project;
    
    /**
     * Constructor
     * @param parent
     *          the parent frame for this dialog
     * @param project
     *          the project that we're selecting the haplotype data source from
     */
    public ExportHaplotypeBlocksToFlatFileDialog(
            Frame parent,
            BhamProject project)
    {
        super(parent, "Export Haplotype Blocks...", true);
        
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
                ExportHaplotypeBlocksToFlatFileDialog.this.browseOutputFiles();
            }
        });
        
        this.okButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                ExportHaplotypeBlocksToFlatFileDialog.this.ok();
            }
        });
        
        this.cancelButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                ExportHaplotypeBlocksToFlatFileDialog.this.cancel();
            }
        });
        
        this.helpButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                ExportHaplotypeBlocksToFlatFileDialog.this.showHelp();
            }
        });
        
        for(HaplotypeDataSource haplotypeData: this.project.getHaplotypeDataSources())
        {
            this.haplotypeDataComboBox.addItem(haplotypeData);
        }
        
        this.fileFormatComboBox.addItem(CommonFlatFileFormat.CSV_UNIX);
        this.fileFormatComboBox.addItem(CommonFlatFileFormat.TAB_DELIMITED_UNIX);
    }
    
    private FlatFileFormat getFileFormat()
    {
        return (FlatFileFormat)this.fileFormatComboBox.getSelectedItem();
    }
    
    private HaplotypeDataSource getSelectedHaplotypeData()
    {
        return (HaplotypeDataSource)this.haplotypeDataComboBox.getSelectedItem();
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
                HaplotypeDataSource haplotypeData = this.getSelectedHaplotypeData();
                ExportHaplotypeBlocksToFlatFileTask exportTask =
                    new ExportHaplotypeBlocksToFlatFileTask(
                            haplotypeData,
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
            String title = "Failed to Export Haplotype Block Data";
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
        javax.swing.JLabel fileFormatLabel = new javax.swing.JLabel();
        fileFormatComboBox = new javax.swing.JComboBox();
        javax.swing.JLabel haplotypeDataLabel = new javax.swing.JLabel();
        haplotypeDataComboBox = new javax.swing.JComboBox();
        actionPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        outputFileLabel.setText("Output File:");

        outputFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/browse-16x16.png"))); // NOI18N
        outputFileButton.setText("Browse...");

        fileFormatLabel.setText("File Format:");

        haplotypeDataLabel.setText("Haplotype Data:");

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
            .add(org.jdesktop.layout.GroupLayout.TRAILING, actionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(fileFormatLabel)
                    .add(outputFileLabel)
                    .add(haplotypeDataLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(fileFormatComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(haplotypeDataComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(outputFileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(outputFileButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(outputFileLabel)
                    .add(outputFileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(outputFileButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fileFormatLabel)
                    .add(fileFormatComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(haplotypeDataLabel)
                    .add(haplotypeDataComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 8, Short.MAX_VALUE)
                .add(actionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox fileFormatComboBox;
    private javax.swing.JComboBox haplotypeDataComboBox;
    private javax.swing.JButton helpButton;
    private javax.swing.JButton okButton;
    private javax.swing.JButton outputFileButton;
    private javax.swing.JTextField outputFileTextField;
    // End of variables declaration//GEN-END:variables

}
