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

import org.jax.haplotype.data.MultiGroupHaplotypeDataSource;

/**
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class MultiGroupHaplotypeDataSourceTreeNode extends DefaultMutableTreeNode
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 2587645269817947542L;

    /**
     * Constructor
     * @param dataSource
     *          the data source that this tree node represents
     */
    public MultiGroupHaplotypeDataSourceTreeNode(
            MultiGroupHaplotypeDataSource dataSource)
    {
        super(dataSource);
    }
    
    /**
     * Getter for the data source that this node is holding
     * @return
     *          the data source
     */
    public MultiGroupHaplotypeDataSource getDataSource()
    {
        return (MultiGroupHaplotypeDataSource)this.getUserObject();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        String name = this.getDataSource().getName();
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
