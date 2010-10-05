/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.bham.test;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JDialog;

import org.jax.bham.project.BhamProject;
import org.jax.haplotype.analysis.experimentdesign.MultiGroupHaplotypeAssociationTest;
import org.jax.haplotype.analysis.experimentdesign.PhenotypeDataSource;
import org.jax.haplotype.data.MultiGroupHaplotypeDataSource;
import org.jax.util.datastructure.SequenceUtilities;
import org.jax.util.gui.MessageDialogUtilities;

/**
 * This dialog allows the user to specify the parameters for a sliding window
 * association test
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MultiGroupHaplotypeAssociationTestDialog extends JDialog
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -1118077394451572390L;
    
    private final BhamProject project;
    
    /**
     * Constructor
     * @param parent
     *          the parent to use for this dialog
     * @param project
     *          the project to create a test for
     */
    public MultiGroupHaplotypeAssociationTestDialog(
            Frame parent,
            BhamProject project)
    {
        super(parent, "Multi-Group Haplotype Association Test", false);
        
        this.project = project;
        
        this.initComponents();
        this.postGuiInit();
    }

    /**
     * handle the initialization not done by the GUI builder
     */
    private void postGuiInit()
    {
        for(MultiGroupHaplotypeDataSource haploDataSrc: this.project.getMultiGroupHaplotypeDataSources())
        {
            this.haplotypeDataSourceComboBox.addItem(haploDataSrc);
        }
        
        for(PhenotypeDataSource phenoDataSrc: this.project.getPhenotypeDataSources())
        {
            this.phenotypeDataSourceComboBox.addItem(phenoDataSrc);
        }
        
        this.okButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                MultiGroupHaplotypeAssociationTestDialog.this.ok();
            }
        });
        
        this.cancelButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                MultiGroupHaplotypeAssociationTestDialog.this.cancel();
            }
        });
        
        this.helpButton.addActionListener(new ActionListener()
        {
            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent e)
            {
                MultiGroupHaplotypeAssociationTestDialog.this.showHelp();
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
     * Getter for the selected haplotype data source
     * @return
     *          the selected haplotype data source
     */
    private MultiGroupHaplotypeDataSource getSelectedHaplotypeDataSource()
    {
        return (MultiGroupHaplotypeDataSource)this.haplotypeDataSourceComboBox.getSelectedItem();
    }
    
    /**
     * Getter for the selected phenotype data source
     * @return
     *          the selected phenotype data source
     */
    private PhenotypeDataSource getSelectedPhenotypeDataSource()
    {
        return (PhenotypeDataSource)this.phenotypeDataSourceComboBox.getSelectedItem();
    }
    
    private String getSelectedName()
    {
        return this.nameTextField.getText().trim();
    }
    
    private void ok()
    {
        if(this.validateData())
        {
            MultiGroupHaplotypeAssociationTest test = new MultiGroupHaplotypeAssociationTest(
                    this.getSelectedName(),
                    this.getSelectedHaplotypeDataSource(),
                    this.getSelectedPhenotypeDataSource());
            this.project.addMultiGroupHaplotypeAssociationTest(test);
            this.dispose();
        }
    }
    
    private boolean validateData()
    {
        String errorMessage = null;
        if(this.getSelectedName().length() == 0)
        {
            errorMessage =
                "Please enter a name for the test before continuing";
        }
        else if(this.getSelectedHaplotypeDataSource() == null)
        {
            errorMessage =
                "You cannot perform an association test without " +
                "any multi-group haplotype data sources.";
        }
        else if(this.getSelectedPhenotypeDataSource() == null)
        {
            errorMessage =
                "You cannot perform an association test without " +
                "any phenotype data sources.";
        }
        
        if(errorMessage == null)
        {
            Set<String> haploStrains = this.getSelectedHaplotypeDataSource().getAvailableStrains();
            Set<String> phenoStrains = this.getSelectedPhenotypeDataSource().getPhenotypeData().keySet();
            
            if(haploStrains.equals(phenoStrains))
            {
                return true;
            }
            else
            {
                // make sure that the user is aware that the strain sets do
                // not completely overlap
                Set<String> haploOnlyStrains = new HashSet<String>(haploStrains);
                haploOnlyStrains.removeAll(phenoStrains);
                Set<String> phenoOnlyStrains = new HashSet<String>(phenoStrains);
                phenoOnlyStrains.removeAll(haploStrains);
                Set<String> commonStrains = new HashSet<String>(haploStrains);
                commonStrains.retainAll(phenoStrains);
                
                boolean performTest = MessageDialogUtilities.ask(
                        this,
                        "[" + SequenceUtilities.toString(haploOnlyStrains, ", ") +
                        "] are unique to the haplotype data and [" +
                        SequenceUtilities.toString(phenoOnlyStrains, ", ") +
                        "] are unique to the phenotype data. Would you " +
                        "like to perform the test using only the subset of " +
                        "strains that they have in common: [" +
                        SequenceUtilities.toString(commonStrains, ", ") +
                        "]?",
                        "Strains Do Not Match");
                return performTest;
            }
        }
        else
        {
            MessageDialogUtilities.warn(
                    this,
                    errorMessage,
                    "Validation Failed");
            return false;
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
        javax.swing.JLabel haplotypeDataSourceLabel = new javax.swing.JLabel();
        haplotypeDataSourceComboBox = new javax.swing.JComboBox();
        javax.swing.JLabel phenotypeDataSourceLabel = new javax.swing.JLabel();
        phenotypeDataSourceComboBox = new javax.swing.JComboBox();
        javax.swing.JPanel actionPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        nameLabel.setText("Test Name:");

        haplotypeDataSourceLabel.setText("Multi-Group Haplotypes:");

        phenotypeDataSourceLabel.setText("Phenotype Data:");

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
            .add(org.jdesktop.layout.GroupLayout.TRAILING, actionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(nameLabel)
                    .add(haplotypeDataSourceLabel)
                    .add(phenotypeDataSourceLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(phenotypeDataSourceComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(haplotypeDataSourceComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE))
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
                    .add(haplotypeDataSourceLabel)
                    .add(haplotypeDataSourceComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(phenotypeDataSourceLabel)
                    .add(phenotypeDataSourceComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 8, Short.MAX_VALUE)
                .add(actionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox haplotypeDataSourceComboBox;
    private javax.swing.JButton helpButton;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JComboBox phenotypeDataSourceComboBox;
    // End of variables declaration//GEN-END:variables

}
