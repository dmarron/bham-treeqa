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

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jax.bham.test.PlotSpecificPhylogenyAssociationTestAction;
import org.jax.haplotype.analysis.experimentdesign.PhylogenyAssociationTest;

/**
 * A project tree node that holds the result of a phylogeny association test
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class PhylogenyAssociationTestTreeNode
extends DefaultMutableTreeNode
implements MouseListener
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -7922168513465173626L;

    /**
     * Constructor
     * @param phylogenyAssociationTest
     *          the test that this node represents
     */
    public PhylogenyAssociationTestTreeNode(PhylogenyAssociationTest phylogenyAssociationTest)
    {
        super(phylogenyAssociationTest);
    }
    
    /**
     * Getter for the test that this node represents
     * @return
     *          the test
     */
    public PhylogenyAssociationTest getPhylogenyAssociationTest()
    {
        return (PhylogenyAssociationTest)this.getUserObject();
    }
    
    /**
     * {@inheritDoc}
     */
    public void mouseClicked(MouseEvent e)
    {
        if(e.isPopupTrigger())
        {
            this.contextMenuTriggered(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void mouseEntered(MouseEvent e)
    {
        // no-op
    }

    /**
     * {@inheritDoc}
     */
    public void mouseExited(MouseEvent e)
    {
        // no-op
    }


    /**
     * {@inheritDoc}
     */
    public void mousePressed(MouseEvent e)
    {
        if(e.isPopupTrigger())
        {
            this.contextMenuTriggered(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void mouseReleased(MouseEvent e)
    {
        if(e.isPopupTrigger())
        {
            this.contextMenuTriggered(e);
        }
    }
    
    /**
     * Respond to a popup trigger event.
     * @param e
     *          the event we're responding to
     */
    private void contextMenuTriggered(MouseEvent e)
    {
        JPopupMenu popupMenu = new JPopupMenu(
                this.toString());
        popupMenu.add(new PlotSpecificPhylogenyAssociationTestAction(
                this.getPhylogenyAssociationTest()));
        
        popupMenu.show(
                (Component)e.getSource(),
                e.getX(),
                e.getY());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        String name = this.getPhylogenyAssociationTest().getName();
        if(name == null)
        {
            return "anonymous";
        }
        else
        {
            return name;
        }
    }
}
