package org.csstudio.iter.utility.caSnooperUi.ui.ChangeView;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


public class ShowSnooper extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        final IWorkbench workbench = PlatformUI.getWorkbench();
        final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        final IWorkbenchPage page = window.getActivePage();
        try {
            page.showView(SnooperView.ID);
        } catch (final PartInitException e) {
            e.printStackTrace();
        }
        return null;
    }
}
