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

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.jax.bham.BhamApplication;
import org.jax.haplotype.jaxbgenerated.HaplotypeAssociationExperimentDesign;
import org.jax.haplotype.jaxbgenerated.ObjectFactory;
import org.jax.util.gui.MessageDialogUtilities;
import org.jax.util.io.FileChooserExtensionFilter;
import org.jax.util.project.Project;
import org.jax.util.project.ProjectManager;


/**
 * A project manager for BHAM! Note that this project manager does not extend
 * {@link org.jax.util.project.ProjectManager} because it cannot conform
 * to that class's assumptions
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class BhamProjectManager extends ProjectManager
{
    /**
     * the singleton instance
     */
    private static final BhamProjectManager instance =
        new BhamProjectManager();
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            BhamProjectManager.class.getName());
    
    /**
     * The extension that should be used for bham projects
     */
    public static final String BHAM_PROJECT_EXTENSION = "bham";

    private static final FileFilter BHAM_PROJECT_FILE_FILTER =
        new FileChooserExtensionFilter(
                BHAM_PROJECT_EXTENSION,
                "BHAM Project (*.bham)");
    
    private final ObjectFactory objectFactory;
    
    private JAXBContext jaxbContext;
    
    /**
     * Constructor
     */
    public BhamProjectManager()
    {
        this.objectFactory = new ObjectFactory();
        
        try
        {
            this.jaxbContext = JAXBContext.newInstance(
                    HaplotypeAssociationExperimentDesign.class);
        }
        catch(JAXBException ex)
        {
            LOG.log(Level.SEVERE,
                    "failed to initialize project manager",
                    ex);
        }
        
        this.createNewActiveProject();
    }
    
    /**
     * Get the singleton instance of this project manager
     * @return
     *          the instance
     */
    public static BhamProjectManager getInstance()
    {
        return BhamProjectManager.instance;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Project createNewActiveProject()
    {
        this.setActiveProjectFile(null);
        this.setActiveProjectModified(false);
        
        BhamProject newProject = new BhamProject(null);
        this.setActiveProject(newProject);
        
        return newProject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileFilter getProjectFileFilter()
    {
        return BHAM_PROJECT_FILE_FILTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean loadActiveProject(File projectFile)
    {
        try
        {
            System.out.println("loading active project from: " + projectFile);
            
            // create the project
            BhamProject bhamProject = new BhamProject(null);
            bhamProject.loadProjectFromFile(projectFile);
            
            // update and notify
            this.setActiveProjectFile(projectFile);
            this.setActiveProjectModified(false);
            this.setActiveProject(bhamProject);
            
            return true;
        }
        catch(Exception ex)
        {
            LOG.log(Level.SEVERE,
                    "failed to load project file: " + projectFile.getPath(),
                    ex);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshProjectDataStructures()
    {
        // nothing to do here. project data structures are always up to date
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean saveActiveProject(File projectFile)
    {
        BhamProject activeProject = this.getActiveProject();
        
        if(activeProject == null)
        {
            String title = "No Active Project";
            String message =
                "Cannot save project because there is no active " +
                "project to save";
            LOG.severe(message);
            MessageDialogUtilities.errorLater(
                    BhamApplication.getInstance().getBhamFrame(),
                    message,
                    title);
            
            return false;
        }
        else
        {
            try
            {
                activeProject.saveProjectToFile(projectFile);
                
                return true;
            }
            catch(Exception ex)
            {
                String title = "Error Saving Project File";
                LOG.log(Level.SEVERE,
                        title,
                        ex);
                MessageDialogUtilities.error(
                        BhamApplication.getInstance().getBhamFrame(),
                        ex.getMessage(),
                        title);
                
                return false;
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BhamProject getActiveProject()
    {
        return (BhamProject)super.getActiveProject();
    }
}
