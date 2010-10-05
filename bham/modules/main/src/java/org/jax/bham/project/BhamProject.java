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

package org.jax.bham.project;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.jax.haplotype.analysis.experimentdesign.HaplotypeAssociationTest;
import org.jax.haplotype.analysis.experimentdesign.HaplotypeDataSource;
import org.jax.haplotype.analysis.experimentdesign.MultiGroupHaplotypeAssociationTest;
import org.jax.haplotype.analysis.experimentdesign.PhenotypeDataSource;
import org.jax.haplotype.analysis.experimentdesign.PhylogenyAssociationTest;
import org.jax.haplotype.analysis.experimentdesign.PhylogenyDataSource;
import org.jax.haplotype.data.GenomeDataSource;
import org.jax.haplotype.data.MultiGroupHaplotypeDataSource;
import org.jax.util.project.Project;

/**
 * A class that holds all the relevant info for a BHAM! project
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class BhamProject extends Project
{
    /**
     * the genome data sources property
     * @see Project#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public static final String GENOME_DATA_SOURCES_PROPERTY_NAME =
        "genomeDataSources";
    private final ArrayList<GenomeDataSource> genomeDataSources =
        new ArrayList<GenomeDataSource>();
    
    /**
     * the phenotype data sources property
     * @see Project#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public static final String PHENOTYPE_DATA_SOURCES_PROPERTY_NAME =
        "phenotypeDataSources";
    private final ArrayList<PhenotypeDataSource> phenotypeDataSources =
        new ArrayList<PhenotypeDataSource>();
    
    /**
     * the haplotype data sources property
     * @see Project#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public static final String HAPLOTYPE_DATA_SOURCES_PROPERTY_NAME =
        "haplotypeDataSources";
    private final ArrayList<HaplotypeDataSource> haplotypeDataSources =
        new ArrayList<HaplotypeDataSource>();
    
    /**
     * the haplotype association tests property
     * @see Project#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public static final String HAPLOTYPE_ASSOCIATION_TESTS_PROPERTY_NAME =
        "haplotypeAssociationTests";
    private final ArrayList<HaplotypeAssociationTest> haplotypeAssociationTests =
        new ArrayList<HaplotypeAssociationTest>();
    
    /**
     * the phylogeny data sources property
     * @see Project#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public static final String PHYLOGENY_DATA_SOURCES_PROPERTY_NAME =
        "phylogenyDataSources";
    private final ArrayList<PhylogenyDataSource> phylogenyDataSources =
        new ArrayList<PhylogenyDataSource>();
    
    /**
     * the phylogeny association tests property
     * @see Project#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public static final String PHYLOGENY_ASSOCIATION_TESTS_PROPERTY_NAME =
        "phylogenyAssociationTests";
    private final ArrayList<PhylogenyAssociationTest> phylogenyAssociationTests =
        new ArrayList<PhylogenyAssociationTest>();
    
    /**
     * the multi-group haplotype data source property
     * @see Project#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public static final String MULTI_GROUP_HAPLOTYPTE_DATA_SOURCES_PROPERTY_NAME =
        "multiGroupHaplotypeDataSources";
    private final ArrayList<MultiGroupHaplotypeDataSource> multiGroupHaplotypeDataSources =
        new ArrayList<MultiGroupHaplotypeDataSource>();
    
    /**
     * the multi-group haplotype association tests property
     * @see Project#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public static final String MULTI_GROUP_HAPLOTYPE_ASSOCIATION_TESTS_PROPERTY_NAME =
        "multiGroupHaplotypeAssociationTests";
    private final ArrayList<MultiGroupHaplotypeAssociationTest> multiGroupHaplotypeAssociationTests =
        new ArrayList<MultiGroupHaplotypeAssociationTest>();
    
    /**
     * Constructor
     * @param name
     *          the name of the project
     */
    public BhamProject(String name)
    {
        super(name);
    }
    
    /**
     * Getter for the genome data sources
     * @return the genomeDataSources
     */
    public List<GenomeDataSource> getGenomeDataSources()
    {
        return this.genomeDataSources;
    }
    
    /**
     * Add a genome data source and fire a property change event using
     * {@link #GENOME_DATA_SOURCES_PROPERTY_NAME}
     * @see Project#addPropertyChangeListener(java.beans.PropertyChangeListener)
     * @param genomeDataSource
     *          the genome data source to add
     */
    public void addGenomeDataSource(GenomeDataSource genomeDataSource)
    {
        this.genomeDataSources.add(genomeDataSource);
        this.propertyChangeSupport.firePropertyChange(
                GENOME_DATA_SOURCES_PROPERTY_NAME,
                null,
                this.genomeDataSources);
    }
    
    /**
     * Getter for the phenotype data sources
     * @return the phenotypeDataSources
     */
    public List<PhenotypeDataSource> getPhenotypeDataSources()
    {
        return this.phenotypeDataSources;
    }
    
    /**
     * Add a phenotype data source and fire a property change event using
     * {@link #PHENOTYPE_DATA_SOURCES_PROPERTY_NAME}
     * @see Project#addPropertyChangeListener(java.beans.PropertyChangeListener)
     * @param phenotypeDataSource
     *          the phenotype data source to add
     */
    public void addPhenotypeDataSource(PhenotypeDataSource phenotypeDataSource)
    {
        this.phenotypeDataSources.add(phenotypeDataSource);
        this.propertyChangeSupport.firePropertyChange(
                PHENOTYPE_DATA_SOURCES_PROPERTY_NAME,
                null,
                this.phenotypeDataSources);
    }
    
    /**
     * Getter for the phylogeny data sources
     * @return the phylogenyDataSources
     */
    public List<PhylogenyDataSource> getPhylogenyDataSources()
    {
        return this.phylogenyDataSources;
    }
    
    /**
     * Add a phylogeny data source and fire a property change event using
     * {@link #PHYLOGENY_DATA_SOURCES_PROPERTY_NAME}
     * @see Project#addPropertyChangeListener(java.beans.PropertyChangeListener)
     * @param phylogenyDataSource
     *          the phylogeny data source to add
     */
    public void addPhylogenyDataSource(PhylogenyDataSource phylogenyDataSource)
    {
        this.phylogenyDataSources.add(phylogenyDataSource);
        this.propertyChangeSupport.firePropertyChange(
                PHYLOGENY_DATA_SOURCES_PROPERTY_NAME,
                null,
                this.phylogenyDataSources);
    }
    
    /**
     * Getter for the multi-group haplotype data sources
     * @return the data sources
     */
    public List<MultiGroupHaplotypeDataSource> getMultiGroupHaplotypeDataSources()
    {
        return this.multiGroupHaplotypeDataSources;
    }
    
    /**
     * Add the given data source and fire a property change event using
     * {@link #MULTI_GROUP_HAPLOTYPTE_DATA_SOURCES_PROPERTY_NAME}
     * @param multiGroupHaplotypeDataSource
     *          the data source to add
     */
    public void addMultiGroupHaplotypeDataSource(
            MultiGroupHaplotypeDataSource multiGroupHaplotypeDataSource)
    {
        this.multiGroupHaplotypeDataSources.add(multiGroupHaplotypeDataSource);
        this.propertyChangeSupport.firePropertyChange(
                MULTI_GROUP_HAPLOTYPTE_DATA_SOURCES_PROPERTY_NAME,
                null,
                this.multiGroupHaplotypeDataSources);
    }
    
    /**
     * Getter for the multi-group haplotype association tests
     * @return the multi-group haplotype association tests
     */
    public List<MultiGroupHaplotypeAssociationTest> getMultiGroupHaplotypeAssociationTests()
    {
        return this.multiGroupHaplotypeAssociationTests;
    }
    
    /**
     * Add the given test and fire a property change event using
     * {@link #MULTI_GROUP_HAPLOTYPE_ASSOCIATION_TESTS_PROPERTY_NAME}
     * @param multiGroupHaplotypeAssociationTest
     *          the test to add
     */
    public void addMultiGroupHaplotypeAssociationTest(
            MultiGroupHaplotypeAssociationTest multiGroupHaplotypeAssociationTest)
    {
        this.multiGroupHaplotypeAssociationTests.add(multiGroupHaplotypeAssociationTest);
        this.propertyChangeSupport.firePropertyChange(
                MULTI_GROUP_HAPLOTYPE_ASSOCIATION_TESTS_PROPERTY_NAME,
                null,
                this.multiGroupHaplotypeAssociationTests);
    }
    
    /**
     * Getter for the phylogeny association test
     * @return the phylogeny association test
     */
    public List<PhylogenyAssociationTest> getPhylogenyAssociationTests()
    {
        return this.phylogenyAssociationTests;
    }
    
    /**
     * Add a phylogeny association test and fire a property change event using
     * {@link #PHYLOGENY_ASSOCIATION_TESTS_PROPERTY_NAME}
     * @param phylogenyAssociationTest
     *          the phylogeny test to add
     */
    public void addPhylogenyAssociationTest(
            PhylogenyAssociationTest phylogenyAssociationTest)
    {
        this.phylogenyAssociationTests.add(phylogenyAssociationTest);
        this.propertyChangeSupport.firePropertyChange(
                PHYLOGENY_ASSOCIATION_TESTS_PROPERTY_NAME,
                null,
                this.phylogenyAssociationTests);
    }
    
    /**
     * Getter for the haplotype data sources
     * @return the haplotype data sources
     */
    public List<HaplotypeDataSource> getHaplotypeDataSources()
    {
        return this.haplotypeDataSources;
    }
    
    /**
     * Add a haplotype data source and fire a property change event using
     * {@link #HAPLOTYPE_DATA_SOURCES_PROPERTY_NAME}
     * @param haplotypeDataSource
     *          the haplotype data source to add
     */
    public void addHaplotypeDataSource(HaplotypeDataSource haplotypeDataSource)
    {
        this.haplotypeDataSources.add(haplotypeDataSource);
        this.propertyChangeSupport.firePropertyChange(
                HAPLOTYPE_DATA_SOURCES_PROPERTY_NAME,
                null,
                this.haplotypeDataSources);
    }
    
    /**
     * Getter for the haplotype association tests
     * @return the haplotype association tests
     */
    public List<HaplotypeAssociationTest> getHaplotypeAssociationTests()
    {
        return this.haplotypeAssociationTests;
    }
    
    /**
     * Add a haplotype association test and fire a property change event using
     * {@link #HAPLOTYPE_ASSOCIATION_TESTS_PROPERTY_NAME}
     * @param haplotypeAssociationTest
     *          the haplotype test to add
     */
    public void addHaplotypeAssociationTest(
            HaplotypeAssociationTest haplotypeAssociationTest)
    {
        this.haplotypeAssociationTests.add(haplotypeAssociationTest);
        this.propertyChangeSupport.firePropertyChange(
                HAPLOTYPE_ASSOCIATION_TESTS_PROPERTY_NAME,
                null,
                this.haplotypeAssociationTests);
    }
    
    /**
     * Save this project to the given file
     * @param file
     *          the file to save this project to
     * @throws FileNotFoundException
     *          see {@link FileOutputStream#FileOutputStream(File)} for
     *          details
     * @throws IOException
     *          if writing data to the file fails
     */
    public void saveProjectToFile(File file)
    throws FileNotFoundException, IOException
    {
        // TODO this should be changed to use XML to be more robust to
        //      future modifications
        ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)));
        
        this.genomeDataSources.trimToSize();
        oos.writeObject(this.genomeDataSources);
        
        this.phenotypeDataSources.trimToSize();
        oos.writeObject(this.phenotypeDataSources);
        
        this.haplotypeDataSources.trimToSize();
        oos.writeObject(this.haplotypeDataSources);
        
        this.haplotypeAssociationTests.trimToSize();
        oos.writeObject(this.haplotypeAssociationTests);
        
        this.phylogenyDataSources.trimToSize();
        oos.writeObject(this.phylogenyDataSources);
        
        this.phylogenyAssociationTests.trimToSize();
        oos.writeObject(this.phylogenyAssociationTests);
        
        this.multiGroupHaplotypeDataSources.trimToSize();
        oos.writeObject(this.multiGroupHaplotypeDataSources);
        
        this.multiGroupHaplotypeAssociationTests.trimToSize();
        oos.writeObject(this.multiGroupHaplotypeAssociationTests);
        
        oos.writeObject(this.getName());
        
        oos.flush();
        oos.close();
    }
    
    /**
     * Load the given project file. it replaces all of the project data
     * (except for the project event listeners)
     * @param file
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public void loadProjectFromFile(File file)
    throws FileNotFoundException, IOException, ClassNotFoundException
    {
        ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)));
        
        List<GenomeDataSource> loadedGenomeDataSources =
            (List<GenomeDataSource>)ois.readObject();
        this.genomeDataSources.clear();
        this.genomeDataSources.addAll(loadedGenomeDataSources);
        
        List<PhenotypeDataSource> loadedPhenotypeDataSources =
            (List<PhenotypeDataSource>)ois.readObject();
        this.phenotypeDataSources.clear();
        this.phenotypeDataSources.addAll(loadedPhenotypeDataSources);
        
        List<HaplotypeDataSource> loadedHaplotypeDataSources =
            (List<HaplotypeDataSource>)ois.readObject();
        this.haplotypeDataSources.clear();
        this.haplotypeDataSources.addAll(loadedHaplotypeDataSources);
        
        List<HaplotypeAssociationTest> loadedHaplotypeAssociationTest =
            (List<HaplotypeAssociationTest>)ois.readObject();
        this.haplotypeAssociationTests.clear();
        this.haplotypeAssociationTests.addAll(loadedHaplotypeAssociationTest);
        
        List<PhylogenyDataSource> loadedPhylogenyDataSource =
            (List<PhylogenyDataSource>)ois.readObject();
        this.phylogenyDataSources.clear();
        this.phylogenyDataSources.addAll(loadedPhylogenyDataSource);
        
        List<PhylogenyAssociationTest> loadedPhylogenyAssociationTests =
            (List<PhylogenyAssociationTest>)ois.readObject();
        this.phylogenyAssociationTests.clear();
        this.phylogenyAssociationTests.addAll(loadedPhylogenyAssociationTests);
        
        List<MultiGroupHaplotypeDataSource> loadedMultiGroupHaplotypeDataSource =
            (List<MultiGroupHaplotypeDataSource>)ois.readObject();
        this.multiGroupHaplotypeDataSources.clear();
        this.multiGroupHaplotypeDataSources.addAll(loadedMultiGroupHaplotypeDataSource);
        
        List<MultiGroupHaplotypeAssociationTest> loadedMultiGroupHaplotypeAssociationTests =
            (List<MultiGroupHaplotypeAssociationTest>)ois.readObject();
        this.multiGroupHaplotypeAssociationTests.clear();
        this.multiGroupHaplotypeAssociationTests.addAll(loadedMultiGroupHaplotypeAssociationTests);
        
        String loadedName = (String)ois.readObject();
        this.setName(loadedName);
        
        this.propertyChangeSupport.firePropertyChange(
                GENOME_DATA_SOURCES_PROPERTY_NAME,
                null,
                this.genomeDataSources);

        this.propertyChangeSupport.firePropertyChange(
                PHENOTYPE_DATA_SOURCES_PROPERTY_NAME,
                null,
                this.phenotypeDataSources);
        
        this.propertyChangeSupport.firePropertyChange(
                PHYLOGENY_DATA_SOURCES_PROPERTY_NAME,
                null,
                this.phylogenyDataSources);
        
        this.propertyChangeSupport.firePropertyChange(
                MULTI_GROUP_HAPLOTYPTE_DATA_SOURCES_PROPERTY_NAME,
                null,
                this.multiGroupHaplotypeDataSources);
        
        this.propertyChangeSupport.firePropertyChange(
                MULTI_GROUP_HAPLOTYPE_ASSOCIATION_TESTS_PROPERTY_NAME,
                null,
                this.multiGroupHaplotypeAssociationTests);
        
        this.propertyChangeSupport.firePropertyChange(
                PHYLOGENY_ASSOCIATION_TESTS_PROPERTY_NAME,
                null,
                this.phylogenyAssociationTests);

        this.propertyChangeSupport.firePropertyChange(
                HAPLOTYPE_DATA_SOURCES_PROPERTY_NAME,
                null,
                this.haplotypeDataSources);
        
        this.propertyChangeSupport.firePropertyChange(
                HAPLOTYPE_ASSOCIATION_TESTS_PROPERTY_NAME,
                null,
                this.haplotypeAssociationTests);
        
        ois.close();
    }
    
//    /**
//     * Utility function to go from a native java sex constraint type to a
//     * JAX-B type
//     * @param sexFilter
//     *          the native sex filter
//     * @return
//     *          the JAX-B type
//     */
//    private static SexConstraintType nativeSexFilterToJaxbSexConstraint(
//            SexFilter sexFilter)
//    {
//        switch(sexFilter)
//        {
//            case AGNOSTIC:
//            {
//                return SexConstraintType.SEX_AGNOSTIC;
//            }
//            
//            case ALLOW_FEMALE:
//            {
//                return SexConstraintType.ALLOW_FEMALE;
//            }
//            
//            case ALLOW_MALE:
//            {
//                return SexConstraintType.ALLOW_MALE;
//            }
//            
//            default:
//            {
//                throw new IllegalArgumentException("unknown sex filter type");
//            }
//        }
//    }
}
