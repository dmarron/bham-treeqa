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

import javax.swing.tree.DefaultMutableTreeNode;

import org.jax.haplotype.analysis.experimentdesign.HaplotypeDataSource;

/**
 * Tree node representation of a haplotype data source
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class HaplotypeDataSourceTreeNode extends DefaultMutableTreeNode
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 4683051789151160525L;

    /**
     * Constructor
     * @param haplotypeDataSource
     *          the data source
     */
    public HaplotypeDataSourceTreeNode(HaplotypeDataSource haplotypeDataSource)
    {
        super(haplotypeDataSource);
    }
    
    /**
     * Getter for the data source
     * @return
     *          the data source
     */
    public HaplotypeDataSource getHaplotypeDataSource()
    {
        return (HaplotypeDataSource)this.getUserObject();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        String name = this.getHaplotypeDataSource().getName();
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
