/*******************************************************************************
 * Copyright (c) 2010-2018 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.actions;

import org.csstudio.opibuilder.editparts.AbstractBaseEditPart;
import org.csstudio.opibuilder.editparts.AbstractPVWidgetEditPart;
import org.csstudio.opibuilder.editparts.PVWidgetEditpartDelegate;
import org.csstudio.opibuilder.model.IPVWidgetModel;
import org.csstudio.opibuilder.util.BeastAlarmSeverityLevel;
import org.csstudio.ui.resources.alarms.AlarmIcons;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class AcknowledgeAlarmAction implements IObjectActionDelegate {

    private IStructuredSelection selection;

    @Override
    public void run(IAction action) {
        final AbstractBaseEditPart selectedWidget = getSelectedWidget();

        if(selectedWidget == null || selectedWidget.getWidgetModel() == null) return;
        if (!(selectedWidget.getWidgetModel() instanceof IPVWidgetModel) || !(selectedWidget instanceof AbstractPVWidgetEditPart)) return;

        PVWidgetEditpartDelegate pvDelegate = ((AbstractPVWidgetEditPart) selectedWidget).getPVWidgetEditpartDelegate();
        if (pvDelegate.isBeastAlarmAndConnected() == false) return;

        pvDelegate.toggleAlarmAcknowledgement();
    }

    private boolean updateAction(IAction action) {
        final AbstractBaseEditPart selectedWidget = getSelectedWidget();

        if(selectedWidget == null || selectedWidget.getWidgetModel() == null) return false;
        if (!(selectedWidget.getWidgetModel() instanceof IPVWidgetModel) || !(selectedWidget instanceof AbstractPVWidgetEditPart)) return false;

        PVWidgetEditpartDelegate pvDelegate = ((AbstractPVWidgetEditPart) selectedWidget).getPVWidgetEditpartDelegate();

        if (!pvDelegate.isBeastAlarmAndConnected()) {
            // cannot ack/unack
            action.setEnabled(false);
            action.setImageDescriptor(null);
            action.setText("Cannot ACK - not connected");
            action.setToolTipText("");
            return true;
        }

        if (pvDelegate.getBeastAlarmInfo().isLatchedAlarmOK()) {
            action.setEnabled(false);
        }

        action.setEnabled(true);
        action.setImageDescriptor(getImageDescriptor(pvDelegate.getBeastAlarmInfo().getCurrentSeverity(), pvDelegate.getBeastAlarmInfo().getLatchedSeverity()));

        String actionDesc = String.format("%1$sAcknowledge %2$s: %3$s",
                pvDelegate.getBeastAlarmInfo().isAcknowledged() ? "Un-" : "",
                        pvDelegate.isBeastAlarmNode() ? ("NODE (" + pvDelegate.getBeastAlarmInfo().getAlarmPVsCount() + ")" ): "PV",
                                pvDelegate.getBeastAlarmInfo().getBeastChannelNameWithoutScheme());
        action.setText(actionDesc);
        return true;
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            this.selection = (IStructuredSelection) selection;
            if (!updateAction(action)) {
                action.setEnabled(false);
                action.setImageDescriptor(null);
                action.setText("[invalid selection for Beast action]");
            }
        }
    }

    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        // nothing to do, we don't need a context for this action
    }

    private AbstractBaseEditPart getSelectedWidget(){
        if(selection != null && selection.getFirstElement() instanceof AbstractBaseEditPart){
            return (AbstractBaseEditPart)selection.getFirstElement();
        }else
            return null;
    }

    private static ImageDescriptor getImageDescriptor(BeastAlarmSeverityLevel currentSeverity, BeastAlarmSeverityLevel latchedSeverity) {
        AlarmIcons icons = AlarmIcons.getInstance();
        switch (latchedSeverity) {
        case UNDEFINED_ACK:
        case INVALID_ACK:
            return icons.getInvalidAcknowledged(false);
        case UNDEFINED:
        case INVALID:
            return currentSeverity == BeastAlarmSeverityLevel.OK ?
                    icons.getInvalidClearedNotAcknowledged(false) : icons.getInvalidNotAcknowledged(false);
        case MAJOR:
            return currentSeverity == BeastAlarmSeverityLevel.OK ?
                    icons.getMajorClearedNotAcknowledged(false) : icons.getMajorNotAcknowledged(false);
        case MAJOR_ACK:
            return icons.getMajorAcknowledged(false);
        case MINOR:
            return currentSeverity == BeastAlarmSeverityLevel.OK ?
                    icons.getMinorClearedNotAcknowledged(false) : icons.getMinorNotAcknowledged(false);
        case MINOR_ACK:
            return icons.getMinorAcknowledged(false);
        case OK:
        default:
            return null;
        }
    }

}
