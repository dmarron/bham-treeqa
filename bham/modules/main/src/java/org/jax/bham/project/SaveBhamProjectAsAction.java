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

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jax.bham.BhamApplication;
import org.jax.util.TextWrapper;
import org.jax.util.project.AbstractSaveProjectAction;
import org.jax.util.project.ProjectManager;

/**
 * Action that allows the user to do a "Save As" for thier BHAM project.
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class SaveBhamProjectAsAction extends AbstractSaveProjectAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 2307880919839710575L;
    
    /**
     * the name the user sees
     */
    private static final String ACTION_NAME = "Save Project As...";
    
    /**
     * our logger
     */
    private static final Logger LOG = Logger.getLogger(
            SaveBhamProjectAsAction.class.getName());

    /**
     * Constructor
     */
    public SaveBhamProjectAsAction()
    {
        super(ACTION_NAME);
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
        // try to be smart about the file dialogs starting file
        ProjectManager projectManager = this.getProjectManager();
        File activeProjFile = projectManager.getActiveProjectFile();
        
        // select the project file to save
        JFileChooser fileChooser = new JFileChooser(activeProjFile);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setApproveButtonText("Save BHAM Project");
        fileChooser.setDialogTitle("Save BHAM Project");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.addChoosableFileFilter(
                projectManager.getProjectFileFilter());
        fileChooser.setFileFilter(
                projectManager.getProjectFileFilter());
        int response = fileChooser.showSaveDialog(this.getParentFrame());
        if(response == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            
            // tack on the project extension if there isn't one
            // already
            String dotBham = "." + BhamProjectManager.BHAM_PROJECT_EXTENSION;
            if(!selectedFile.exists() && !selectedFile.getName().toLowerCase().endsWith(dotBham))
            {
                String newFileName =
                    selectedFile.getName() + "." + BhamProjectManager.BHAM_PROJECT_EXTENSION;
                selectedFile =
                    new File(selectedFile.getParentFile(), newFileName);
            }
            
            if(selectedFile.exists() && !selectedFile.equals(activeProjFile))
            {
                // ask the user if they're sure they want to overwrite
                String message =
                    "Saving the current BHAM project to " +
                    selectedFile.getAbsolutePath() + " will overwrite an " +
                    " existing file. Would you like to continue anyway?";
                if(LOG.isLoggable(Level.FINE))
                {
                    LOG.fine(message);
                }
                
                int overwriteResponse = JOptionPane.showConfirmDialog(
                        this.getParentFrame(),
                        TextWrapper.wrapText(
                                message,
                                TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                        "Overwriting Existing File",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if(overwriteResponse != JOptionPane.OK_OPTION)
                {
                    if(LOG.isLoggable(Level.FINE))
                    {
                        LOG.fine("overwrite canceled");
                    }
                    return;
                }
            }
            
            if(!projectManager.saveActiveProject(selectedFile))
            {
                // there was a problem... tell the user
                String message =
                    "Failed to save to selected BHAM project file: " +
                    selectedFile.getAbsolutePath();
                LOG.info(message);
                
                JOptionPane.showMessageDialog(
                        this.getParentFrame(),
                        TextWrapper.wrapText(
                                message,
                                TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                        "Error Saving Project",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
