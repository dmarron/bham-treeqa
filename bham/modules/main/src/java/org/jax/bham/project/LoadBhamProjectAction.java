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
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jax.bham.BhamApplication;
import org.jax.util.TextWrapper;
import org.jax.util.project.ProjectManager;

/**
 * An action class for loading a BHAM project file
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class LoadBhamProjectAction extends AbstractAction
{
    /**
     * every {@link java.io.Serializable} is supposed to have one of these
     */
    private static final long serialVersionUID = 8091426647586900801L;
    
    private static final Logger LOG = Logger.getLogger(
            LoadBhamProjectAction.class.getName());
    
    private static final String ACTION_NAME = "Open BHAM Project...";
    
    /**
     * the icon resource location
     */
    private static final String ICON_RESOURCE_LOCATION =
        "/images/action/open-project-16x16.png";
    
    /**
     * Constructor
     */
    public LoadBhamProjectAction()
    {
        super(ACTION_NAME,
              new ImageIcon(LoadBhamProjectAction.class.getResource(
                      ICON_RESOURCE_LOCATION)));
    }
    
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e)
    {
        // prompt the user if they're about to lose unsaved changes
        ProjectManager projectManager = BhamProjectManager.getInstance();
        if(projectManager.isActiveProjectModified())
        {
            String message =
                "The current project contains unsaved modifications. Loading " +
                "a new project will cause these modifications to be lost. " +
                "Would you like to continue without saving?";
            int response = JOptionPane.showConfirmDialog(
                    this.getParentFrame(),
                    TextWrapper.wrapText(
                            message,
                            TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                    "Unsaved Project Modifications",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if(response == JOptionPane.CLOSED_OPTION || response == JOptionPane.CANCEL_OPTION)
            {
                return;
            }
        }
        
        // TODO try to be smart about the file dialogs starting dir
        
        // slect the project file to load
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(
                projectManager.getProjectFileFilter());
        fileChooser.setFileFilter(
                projectManager.getProjectFileFilter());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setApproveButtonText("Open BHAM Project");
        fileChooser.setDialogTitle("Open BHAM Project");
        fileChooser.setMultiSelectionEnabled(false);
        int response = fileChooser.showOpenDialog(this.getParentFrame());
        if(response == JFileChooser.APPROVE_OPTION)
        {
            if(!projectManager.loadActiveProject(fileChooser.getSelectedFile()))
            {
                // there was a problem... tell the user
                String message =
                    "Failed to load selected BHAM project file: " +
                    fileChooser.getSelectedFile().getAbsolutePath();
                LOG.info(message);
                
                JOptionPane.showMessageDialog(
                        this.getParentFrame(),
                        TextWrapper.wrapText(
                                message,
                                TextWrapper.DEFAULT_DIALOG_COLUMN_COUNT),
                        "Error Loading Project",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Getter for the parent frame to use for any popups
     * @return  the parent frame
     */
    private Frame getParentFrame()
    {
        return BhamApplication.getInstance().getBhamFrame();
    }
}
