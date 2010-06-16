package org.csstudio.trends.databrowser.editor;

import org.csstudio.trends.databrowser.Messages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/** Empty IEditorInput.
 *  <p>
 *  When the editor is started without a file,
 *  for example from the main menu,
 *  this is used as its initial input.
 *  <p>
 *  When the user decides to save the configuration
 *  into an actual file, the input is changed to a FileEditorInput.
 *  <p>
 *  @author Kay Kasemir
 */
public class EmptyEditorInput implements IEditorInput
{
    /** Cause application title to reflect the 'not saved' state. */
    public String getName()
    {
        return Messages.NotSaved;
    }

    /** Cause tool top to reflect the 'not saved' state. */
    public String getToolTipText()
    {
        return Messages.NotSavedTT;
    }
    
    /** @return Returns <code>false</code> since no file exists. */
    public boolean exists()
    {
        return false;
    }

    /** Returns no image. */
    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }

    /** Can't persist. */
    public IPersistableElement getPersistable()
    {
        return null;
    }

    /** Don't adapt. */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter)
    {
        return null;
    }
}