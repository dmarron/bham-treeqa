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

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.jax.haplotype.analysis.experimentdesign.HaplotypeAssociationTest;
import org.jax.haplotype.analysis.experimentdesign.HaplotypeDataSource;
import org.jax.haplotype.analysis.experimentdesign.MultiGroupHaplotypeAssociationTest;
import org.jax.haplotype.analysis.experimentdesign.PhenotypeDataSource;
import org.jax.haplotype.analysis.experimentdesign.PhylogenyAssociationTest;
import org.jax.haplotype.analysis.experimentdesign.PhylogenyDataSource;
import org.jax.haplotype.data.GenomeDataSource;
import org.jax.haplotype.data.MultiGroupHaplotypeDataSource;
import org.jax.util.gui.ListTreeNode;
import org.jax.util.gui.SwingTreeUtilities;
import org.jax.util.gui.SwingTreeUtilities.TreeNodeFactory;
import org.jax.util.project.gui.ProjectTree;

/**
 * A {@link javax.swing.JTree} representing our BHAM! project
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class BhamProjectTree extends ProjectTree
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 1834618113393847632L;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultTreeModel getModel()
    {
        return (DefaultTreeModel)super.getModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BhamProject getActiveProject()
    {
        return (BhamProject)super.getActiveProject();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void refreshProjectTree()
    {
        // invoke later to make sure that the refresh happens in the
        // AWT thread because swing isn't thread safe
        SwingUtilities.invokeLater(new Runnable()
        {
            /**
             * {@inheritDoc}
             */
            public void run()
            {
                BhamProjectTree.this.refreshProjectTreeNow();
            }
        });
    }
    
    private void refreshProjectTreeNow()
    {
        BhamProject activeProject = this.getActiveProject();
        final boolean activeProjectIsRoot;
        {
            final Object rootObject = this.getModel().getRoot();
            if(rootObject instanceof BhamProjectTreeNode)
            {
                final BhamProjectTreeNode root = (BhamProjectTreeNode)rootObject;
                activeProjectIsRoot =
                    root.getBhamProject() == activeProject;
            }
            else
            {
                activeProjectIsRoot = false;
            }
        }
        
        final BhamProjectTreeNode projectTreeNode;
        if(activeProjectIsRoot)
        {
            projectTreeNode = (BhamProjectTreeNode)this.getModel().getRoot();
            this.getModel().nodeChanged(projectTreeNode);
        }
        else
        {
            DefaultTreeModel model = this.getModel();
            
            projectTreeNode = new BhamProjectTreeNode(activeProject);
            model.setRoot(projectTreeNode);
            
            model.insertNodeInto(
                    projectTreeNode.getGenomeDataSourcesTreeNode(),
                    projectTreeNode,
                    projectTreeNode.getChildCount());
            model.insertNodeInto(
                    projectTreeNode.getPhenotypeDataSourcesTreeNode(),
                    projectTreeNode,
                    projectTreeNode.getChildCount());
            model.insertNodeInto(
                    projectTreeNode.getHaplotypeDataSourcesTreeNode(),
                    projectTreeNode,
                    projectTreeNode.getChildCount());
            model.insertNodeInto(
                    projectTreeNode.getHaplotypeAssociationTestsTreeNode(),
                    projectTreeNode,
                    projectTreeNode.getChildCount());
            model.insertNodeInto(
                    projectTreeNode.getPhylogenyDataSourcesTreeNode(),
                    projectTreeNode,
                    projectTreeNode.getChildCount());
            model.insertNodeInto(
                    projectTreeNode.getPhylogenyAssociationTestsTreeNode(),
                    projectTreeNode,
                    projectTreeNode.getChildCount());
            model.insertNodeInto(
                    projectTreeNode.getMultiGroupHaplotypeDataSourcesTreeNode(),
                    projectTreeNode,
                    projectTreeNode.getChildCount());
            model.insertNodeInto(
                    projectTreeNode.getMultiGroupHaplotypeAssociationTestsTreeNode(),
                    projectTreeNode,
                    projectTreeNode.getChildCount());
            
            this.expandPath(new TreePath(
                    projectTreeNode.getPath()));
        }
        
        this.refreshBhamProjectChildNodes(projectTreeNode);
    }

    private void refreshBhamProjectChildNodes(BhamProjectTreeNode projectTreeNode)
    {
        this.refreshGenomeDataSourceNodes(
                projectTreeNode.getGenomeDataSourcesTreeNode());
        this.refreshPhenotypeDataSourceNodes(
                projectTreeNode.getPhenotypeDataSourcesTreeNode());
        this.refreshHaplotypeDataSourceNodes(
                projectTreeNode.getHaplotypeDataSourcesTreeNode());
        this.refreshHaplotypeAssociationTestNodes(
                projectTreeNode.getHaplotypeAssociationTestsTreeNode());
        this.refreshPhylogenyDataSourceNodes(
                projectTreeNode.getPhylogenyDataSourcesTreeNode());
        this.refreshPhylogenyAssociationTestNodes(
                projectTreeNode.getPhylogenyAssociationTestsTreeNode());
        this.refreshMultiGroupHaplotypeDataSourceNodes(
                projectTreeNode.getMultiGroupHaplotypeDataSourcesTreeNode());
        this.refreshMultiGroupHaplotypeAssociationTestNodes(
                projectTreeNode.getMultiGroupHaplotypeAssociationTestsTreeNode());
    }
    
    private void refreshGenomeDataSourceNodes(
            ListTreeNode<GenomeDataSource> genomeDataSourcesTreeNode)
    {
        TreeNodeFactory<GenomeDataSource, GenomeDataSourceTreeNode> genomeDataSourceNodeFactory =
            new TreeNodeFactory<GenomeDataSource, GenomeDataSourceTreeNode>()
            {
                /**
                 * {@inheritDoc}
                 */
                public GenomeDataSourceTreeNode createTreeNode(
                        GenomeDataSource data)
                {
                    return new GenomeDataSourceTreeNode(data);
                }
            };
        
        SwingTreeUtilities.updateChildNodes(
                this.getModel(),
                genomeDataSourceNodeFactory,
                genomeDataSourcesTreeNode,
                genomeDataSourcesTreeNode.getList());
    }
    
    private void refreshPhenotypeDataSourceNodes(
            ListTreeNode<PhenotypeDataSource> phenotypeDataSourcesTreeNode)
    {
        TreeNodeFactory<PhenotypeDataSource, PhenotypeDataSourceTreeNode> phenotypeDataSourceNodeFactory =
            new TreeNodeFactory<PhenotypeDataSource, PhenotypeDataSourceTreeNode>()
            {
                /**
                 * {@inheritDoc}
                 */
                public PhenotypeDataSourceTreeNode createTreeNode(
                        PhenotypeDataSource data)
                {
                    return new PhenotypeDataSourceTreeNode(data);
                }
            };
        
        SwingTreeUtilities.updateChildNodes(
                this.getModel(),
                phenotypeDataSourceNodeFactory,
                phenotypeDataSourcesTreeNode,
                phenotypeDataSourcesTreeNode.getList());
    }
    
    private void refreshHaplotypeDataSourceNodes(
            ListTreeNode<HaplotypeDataSource> haplotypeDataSourcesTreeNode)
    {
        TreeNodeFactory<HaplotypeDataSource, HaplotypeDataSourceTreeNode> haplotypeDataSourceNodeFactory =
            new TreeNodeFactory<HaplotypeDataSource, HaplotypeDataSourceTreeNode>()
            {
                /**
                 * {@inheritDoc}
                 */
                public HaplotypeDataSourceTreeNode createTreeNode(
                        HaplotypeDataSource data)
                {
                    return new HaplotypeDataSourceTreeNode(data);
                }
            };
        
        SwingTreeUtilities.updateChildNodes(
                this.getModel(),
                haplotypeDataSourceNodeFactory,
                haplotypeDataSourcesTreeNode,
                haplotypeDataSourcesTreeNode.getList());
    }
    
    private void refreshHaplotypeAssociationTestNodes(
            ListTreeNode<HaplotypeAssociationTest> haplotypeAssociationTestsTreeNode)
    {
        TreeNodeFactory<HaplotypeAssociationTest, HaplotypeAssociationTestTreeNode> haplotypeAssociationTestNodeFactory =
            new TreeNodeFactory<HaplotypeAssociationTest, HaplotypeAssociationTestTreeNode>()
            {
                /**
                 * {@inheritDoc}
                 */
                public HaplotypeAssociationTestTreeNode createTreeNode(
                        HaplotypeAssociationTest data)
                {
                    return new HaplotypeAssociationTestTreeNode(data);
                }
            };
        
        SwingTreeUtilities.updateChildNodes(
                this.getModel(),
                haplotypeAssociationTestNodeFactory,
                haplotypeAssociationTestsTreeNode,
                haplotypeAssociationTestsTreeNode.getList());
    }
    
    private void refreshPhylogenyDataSourceNodes(
            ListTreeNode<PhylogenyDataSource> phylogenyDataSourcesTreeNode)
    {
        TreeNodeFactory<PhylogenyDataSource, PhylogenyDataSourceTreeNode> phylogenyDataSourceNodeFactory =
            new TreeNodeFactory<PhylogenyDataSource, PhylogenyDataSourceTreeNode>()
            {
                /**
                 * {@inheritDoc}
                 */
                public PhylogenyDataSourceTreeNode createTreeNode(
                        PhylogenyDataSource data)
                {
                    return new PhylogenyDataSourceTreeNode(data);
                }
            };
        
        SwingTreeUtilities.updateChildNodes(
                this.getModel(),
                phylogenyDataSourceNodeFactory,
                phylogenyDataSourcesTreeNode,
                phylogenyDataSourcesTreeNode.getList());
    }
    
    private void refreshPhylogenyAssociationTestNodes(
            ListTreeNode<PhylogenyAssociationTest> phylogenyAssociationTestsTreeNode)
    {
        TreeNodeFactory<PhylogenyAssociationTest, PhylogenyAssociationTestTreeNode> phylogenyAssociationTestNodeFactory =
            new TreeNodeFactory<PhylogenyAssociationTest, PhylogenyAssociationTestTreeNode>()
            {
                /**
                 * {@inheritDoc}
                 */
                public PhylogenyAssociationTestTreeNode createTreeNode(
                        PhylogenyAssociationTest data)
                {
                    return new PhylogenyAssociationTestTreeNode(data);
                }
            };
        
        SwingTreeUtilities.updateChildNodes(
                this.getModel(),
                phylogenyAssociationTestNodeFactory,
                phylogenyAssociationTestsTreeNode,
                phylogenyAssociationTestsTreeNode.getList());
    }
    
    private void refreshMultiGroupHaplotypeDataSourceNodes(
            ListTreeNode<MultiGroupHaplotypeDataSource> multiGroupHaploDataSourcesTreeNode)
    {
        TreeNodeFactory<MultiGroupHaplotypeDataSource, MultiGroupHaplotypeDataSourceTreeNode> nodeFactory =
            new TreeNodeFactory<MultiGroupHaplotypeDataSource, MultiGroupHaplotypeDataSourceTreeNode>()
            {
                /**
                 * {@inheritDoc}
                 */
                public MultiGroupHaplotypeDataSourceTreeNode createTreeNode(
                        MultiGroupHaplotypeDataSource data)
                {
                    return new MultiGroupHaplotypeDataSourceTreeNode(data);
                }
            };
        
        SwingTreeUtilities.updateChildNodes(
                this.getModel(),
                nodeFactory,
                multiGroupHaploDataSourcesTreeNode,
                multiGroupHaploDataSourcesTreeNode.getList());
    }
    
    private void refreshMultiGroupHaplotypeAssociationTestNodes(
            ListTreeNode<MultiGroupHaplotypeAssociationTest> multiGroupHaplotypeAssociationTestsTreeNode)
    {
        TreeNodeFactory<MultiGroupHaplotypeAssociationTest, MultiGroupHaplotypeAssociationTestTreeNode> nodeFactory =
            new TreeNodeFactory<MultiGroupHaplotypeAssociationTest, MultiGroupHaplotypeAssociationTestTreeNode>()
            {
                /**
                 * {@inheritDoc}
                 */
                public MultiGroupHaplotypeAssociationTestTreeNode createTreeNode(
                        MultiGroupHaplotypeAssociationTest data)
                {
                    return new MultiGroupHaplotypeAssociationTestTreeNode(data);
                }
            };
        
        SwingTreeUtilities.updateChildNodes(
                this.getModel(),
                nodeFactory,
                multiGroupHaplotypeAssociationTestsTreeNode,
                multiGroupHaplotypeAssociationTestsTreeNode.getList());
    }
}
