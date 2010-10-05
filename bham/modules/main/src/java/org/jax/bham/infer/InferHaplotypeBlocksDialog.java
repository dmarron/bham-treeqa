/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.bham.infer;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.SpinnerNumberModel;

import org.jax.bham.project.BhamProject;
import org.jax.haplotype.analysis.experimentdesign.GenotypeInferenceHaplotypeDataSource;
import org.jax.haplotype.data.GenomeDataSource;
import org.jax.util.gui.MessageDialogUtilities;

public class InferHaplotypeBlocksDialog extends JDialog
{
    private static final Logger LOG = Logger.getLogger(
            InferHaplotypeBlocksDialog.class.getName());
    
    private final SpinnerNumberModel minSNPCountSpinnerModel = new SpinnerNumberModel(
            1,
            1,
            Integer.MAX_VALUE,
            1);
    
    private final SpinnerNumberModel minStrainCountSpinnerModel = new SpinnerNumberModel(
            2,
            2,
            Integer.MAX_VALUE,
            1);

    private final BhamProject project;
    
    /**
     * Constructor
     * @param parent
     *          the parent frame for this dialog
     * @param project
     *          the project for this dialog
     */
    public InferHaplotypeBlocksDialog(Frame parent, BhamProject project)
    {
        super(parent, "Infer Haplotype Block Structure", false);
        this.project = project;
        
        this.initComponents();
        this.postGuiInit();
    }

    /**
     * do the initialization that the GUI builder doesn't handle
     */
    private void postGuiInit()
    {
        this.minSNPCountSpinner.setModel(this.minSNPCountSpinnerModel);
        this.minStrainCountSpinner.setModel(this.minStrainCountSpinnerModel);
        
        this.helpButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                InferHaplotypeBlocksDialog.this.showHelp();
            }
        });
        
        this.okButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                InferHaplotypeBlocksDialog.this.ok();
            }
        });
        
        this.cancelButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                InferHaplotypeBlocksDialog.this.cancel();
            }
        });
        
        for(GenomeDataSource genomeDataSource: this.project.getGenomeDataSources())
        {
            this.genoDataSourceComboBox.addItem(genomeDataSource);
        }
    }

    private void ok()
    {
        try
        {
            if(this.validateData())
            {
                GenotypeInferenceHaplotypeDataSource haploDataSource =
                    new GenotypeInferenceHaplotypeDataSource(
                            this.nameTextField.getText().trim(),
                            this.getSelectedGenomeDataSource(),
                            null,
                            this.minSNPCountSpinnerModel.getNumber().intValue(),
                            this.minStrainCountSpinnerModel.getNumber().intValue());
                this.project.addHaplotypeDataSource(haploDataSource);
                
                this.dispose();
            }
        }
        catch(Exception ex)
        {
            String title = "Failed to Infer Haplotype Blocks";
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
        if(this.getSelectedGenomeDataSource() == null)
        {
            errorMessage = "No Genotype Data Source is Selected";
        }
        else if(this.nameTextField.getText().trim().length() == 0)
        {
            errorMessage =
                "Please enter a name for the haplotype data before continuing";
        }
        
        if(errorMessage == null)
        {
            return true;
        }
        else
        {
            MessageDialogUtilities.error(
                    this,
                    errorMessage,
                    "Validation Failed");
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
    
    private GenomeDataSource getSelectedGenomeDataSource()
    {
        return (GenomeDataSource)this.genoDataSourceComboBox.getSelectedItem();
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
        javax.swing.JLabel genoDataSourceLabel = new javax.swing.JLabel();
        genoDataSourceComboBox = new javax.swing.JComboBox();
        javax.swing.JLabel minSNPCountLabel = new javax.swing.JLabel();
        minSNPCountSpinner = new javax.swing.JSpinner();
        javax.swing.JLabel minStrainCountLabel = new javax.swing.JLabel();
        minStrainCountSpinner = new javax.swing.JSpinner();
        actionPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        nameLabel.setText("Name Haplotype Data:");

        genoDataSourceLabel.setText("Genotype Data Source:");

        minSNPCountLabel.setText("Min SNP Count in Block:");

        minStrainCountLabel.setText("Min Strains in Block:");

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
            .add(org.jdesktop.layout.GroupLayout.TRAILING, actionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(nameLabel)
                    .add(genoDataSourceLabel)
                    .add(minSNPCountLabel)
                    .add(minStrainCountLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(minStrainCountSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                            .add(minSNPCountSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                        .add(layout.createSequentialGroup()
                            .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                            .add(27, 27, 27))
                        .add(layout.createSequentialGroup()
                            .add(genoDataSourceComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
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
                    .add(genoDataSourceLabel)
                    .add(genoDataSourceComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(minSNPCountLabel)
                    .add(minSNPCountSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(minStrainCountLabel)
                    .add(minStrainCountSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(actionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox genoDataSourceComboBox;
    private javax.swing.JButton helpButton;
    private javax.swing.JSpinner minSNPCountSpinner;
    private javax.swing.JSpinner minStrainCountSpinner;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables

}
