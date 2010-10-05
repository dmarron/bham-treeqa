/*
 * Copyright (c) 2009 The Jackson Laboratory
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

import java.awt.Frame;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.jax.bham.BhamApplication;
import org.jax.util.project.AbstractSaveProjectAction;
import org.jax.util.project.ProjectManager;

/**
 * Save the current BHAM project
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class SaveBhamProjectAction extends AbstractSaveProjectAction
{
    /**
     * logger
     */
    private static final Logger LOG = Logger.getLogger(
            SaveBhamProjectAction.class.getName());
    
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = -8861769332760442577L;

    /**
     * the name the user sees
     */
    private static final String ACTION_NAME = "Save Project";
    
    /**
     * the icon resource location
     */
    private static final String ICON_RESOURCE_LOCATION =
        "/images/action/save-16x16.png";
    
    /**
     * Constructor
     */
    public SaveBhamProjectAction()
    {
        super(ACTION_NAME,
              new ImageIcon(SaveBhamProjectAction.class.getResource(
                      ICON_RESOURCE_LOCATION)));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Frame getParentFrame()
    {
        return BhamApplication.getInstance().getBhamFrame();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ProjectManager getProjectManager()
    {
        return BhamProjectManager.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performSave()
    {
        ProjectManager projectManager = BhamProjectManager.getInstance();
        File activeProjFile = projectManager.getActiveProjectFile();
        
        if(activeProjFile == null)
        {
            // since we don't have an active file this is the same as a
            // "save as"
            if(LOG.isLoggable(Level.FINE))
            {
                LOG.fine(
                        "calling save as since we don't have an existing " +
                        "file name for the project");
            }
            
            SaveBhamProjectAsAction saveAsAction = new SaveBhamProjectAsAction();
            saveAsAction.actionPerformed(null);
        }
        else
        {
            projectManager.saveActiveProject(activeProjFile);
        }
    }
}
