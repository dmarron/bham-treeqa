/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.bham.infer;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.SpinnerNumberModel;

import org.jax.bham.project.BhamProject;
import org.jax.haplotype.data.GenomeDataSource;
import org.jax.haplotype.data.SlidingWindowHaplotypeDataSource;
import org.jax.util.gui.MessageDialogUtilities;

/**
 * This dialog allows the user to specify the parameters for a sliding window
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class SlidingWindowHaplotypeInferenceDialog extends JDialog
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 4757322435942626174L;
    
    private static final String STEP_ONE_SNP_AT_A_TIME = "One SNP at a Time";
    
    private static final String STEP_WHOLE_WINDOW_WIDTH = "One Window Width at a Time";
    
    private final SpinnerNumberModel windowSizeSpinnerModel = new SpinnerNumberModel(
            3,
            1,
            Integer.MAX_VALUE,
            1);
    
    private final BhamProject project;
    
    /**
     * Constructor
     * @param parent
     *          the parent to use for this dialog
     * @param project
     *          the project
     */
    public SlidingWindowHaplotypeInferenceDialog(
            Frame parent,
            BhamProject project)
    {
        super(parent, "Sliding Window Haplotype Inference", false);
        
        this.project = project;
        
        this.initComponents();
        this.postGuiInit();
    }

    /**
     * handle the initialization not done by the GUI builder
     */
    private void postGuiInit()
    {
        for(GenomeDataSource genoDataSrc: this.project.getGenomeDataSources())
        {
            this.genotypeDataSourceComboBox.addItem(genoDataSrc);
        }
        
        this.moveWindowLabelComboBox.addItem(STEP_ONE_SNP_AT_A_TIME);
        this.moveWindowLabelComboBox.addItem(STEP_WHOLE_WINDOW_WIDTH);
        
        this.windowSizeSpinner.setModel(this.windowSizeSpinnerModel);
        
        this.okButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                SlidingWindowHaplotypeInferenceDialog.this.ok();
            }
        });
        
        this.cancelButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                SlidingWindowHaplotypeInferenceDialog.this.cancel();
            }
        });
        
        this.helpButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                SlidingWindowHaplotypeInferenceDialog.this.showHelp();
            }
        });
    }
    
    private void showHelp()
    {
        MessageDialogUtilities.inform(
                this,
                "Sorry, no help yet..",
                "Help Not Implemented");
    }
    
    private void cancel()
    {
        this.dispose();
    }
    
    /**
     * Getter for the selected genome data source
     * @return
     *          the selected genome data source
     */
    private GenomeDataSource getSelectedGenomeDataSource()
    {
        return (GenomeDataSource)this.genotypeDataSourceComboBox.getSelectedItem();
    }
    
    private String getSelectedName()
    {
        return this.nameTextField.getText().trim();
    }
    
    private void ok()
    {
        if(this.validateData())
        {
            SlidingWindowHaplotypeDataSource dataSource =
                new SlidingWindowHaplotypeDataSource(
                        this.nameTextField.getText().trim(),
                        this.getSelectedGenomeDataSource(),
                        this.getWindowSizeInSnps(),
                        this.getStepWindowOneSnpAtATime());
            this.project.addMultiGroupHaplotypeDataSource(dataSource);
            this.dispose();
        }
    }
    
    private boolean getStepWindowOneSnpAtATime()
    {
        return STEP_ONE_SNP_AT_A_TIME.equals(
                this.moveWindowLabelComboBox.getSelectedItem());
    }
    
    private int getWindowSizeInSnps()
    {
        return this.windowSizeSpinnerModel.getNumber().intValue();
    }
    
    private boolean validateData()
    {
        String errorMessage = null;
        if(this.getSelectedName().length() == 0)
        {
            errorMessage =
                "Please enter a name for the data source before continuing";
        }
        else if(this.getSelectedGenomeDataSource() == null)
        {
            errorMessage =
                "You cannot perform sliding window inference without " +
                "any genome data sources.";
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
    
    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("all")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JLabel nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        javax.swing.JLabel genotypeDataSourceLabel = new javax.swing.JLabel();
        genotypeDataSourceComboBox = new javax.swing.JComboBox();
        javax.swing.JLabel moveWindowLabel = new javax.swing.JLabel();
        moveWindowLabelComboBox = new javax.swing.JComboBox();
        javax.swing.JLabel windowSizeLabel = new javax.swing.JLabel();
        windowSizeSpinner = new javax.swing.JSpinner();
        javax.swing.JPanel actionPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        nameLabel.setText("Data Source Name:");

        genotypeDataSourceLabel.setText("Genotype Data:");

        moveWindowLabel.setText("Move Window:");

        windowSizeLabel.setText("Window Size (SNPs):");

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
            .add(org.jdesktop.layout.GroupLayout.TRAILING, actionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(nameLabel)
                    .add(genotypeDataSourceLabel)
                    .add(moveWindowLabel)
                    .add(windowSizeLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(windowSizeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                            .add(moveWindowLabelComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap(213, Short.MAX_VALUE))
                        .add(layout.createSequentialGroup()
                            .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                            .add(27, 27, 27))
                        .add(layout.createSequentialGroup()
                            .add(genotypeDataSourceComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap()))))
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
                    .add(genotypeDataSourceLabel)
                    .add(genotypeDataSourceComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(moveWindowLabel)
                    .add(moveWindowLabelComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(windowSizeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(windowSizeLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(actionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox genotypeDataSourceComboBox;
    private javax.swing.JButton helpButton;
    private javax.swing.JComboBox moveWindowLabelComboBox;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JSpinner windowSizeSpinner;
    // End of variables declaration//GEN-END:variables

}
