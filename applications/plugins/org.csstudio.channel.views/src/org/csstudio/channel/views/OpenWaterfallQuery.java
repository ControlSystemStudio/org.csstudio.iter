package org.csstudio.channel.views;

import gov.bnl.channelfinder.api.ChannelQuery;

import java.util.List;

import org.csstudio.utility.channel.ChannelQueryCommandHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenWaterfallQuery extends ChannelQueryCommandHandler {
	
	@Override
	protected void execute(List<ChannelQuery> queries, ExecutionEvent event) {
		try {
			IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
			if (queries.size() > 0) {
				WaterfallView waterfall = (WaterfallView) page
				.showView(WaterfallView.ID);
				waterfall.setPVName(queries.get(0).getQuery());
			}
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}