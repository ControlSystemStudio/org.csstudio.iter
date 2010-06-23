/*
 * Copyright (c) 2010 Stiftung Deutsches Elektronen-Synchrotron,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY.
 *
 * THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN "../AS IS" BASIS.
 * WITHOUT WARRANTY OF ANY KIND, EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE DEFECTIVE
 * IN ANY RESPECT, THE USER ASSUMES THE COST OF ANY NECESSARY SERVICING, REPAIR OR
 * CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE.
 * NO USE OF ANY SOFTWARE IS AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.
 * DESY HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS,
 * OR MODIFICATIONS.
 * THE FULL LICENSE SPECIFYING FOR THE SOFTWARE THE REDISTRIBUTION, MODIFICATION,
 * USAGE AND OTHER RIGHTS AND OBLIGATIONS IS INCLUDED WITH THE DISTRIBUTION OF THIS
 * PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU MAY FIND A COPY
 * AT HTTP://WWW.DESY.DE/LEGAL/LICENSE.HTM
 *
 * $Id$
 */
package org.csstudio.alarm.treeView.ldap;

import java.util.List;

import javax.annotation.Nonnull;
import javax.naming.InvalidNameException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.csstudio.alarm.service.declaration.AlarmTreeLdapConstants;
import org.csstudio.alarm.service.declaration.AlarmTreeNodePropertyId;
import org.csstudio.alarm.service.declaration.LdapEpicsAlarmcfgConfiguration;
import org.csstudio.alarm.treeView.model.IAlarmTreeNode;
import org.csstudio.alarm.treeView.model.SubtreeNode;
import org.csstudio.utility.treemodel.ContentModel;
import org.csstudio.utility.treemodel.CreateContentModelException;
import org.csstudio.utility.treemodel.ISubtreeNodeComponent;
import org.csstudio.utility.treemodel.TreeNodeComponent;
import org.csstudio.utility.treemodel.builder.AbstractContentModelBuilder;

/**
 * Builds a content model from the alarm tree view structure.
 *
 * @author bknerr
 * @author $Author$
 * @version $Revision$
 * @since 19.05.2010
 */
public final class AlarmTreeContentModelBuilder extends AbstractContentModelBuilder<LdapEpicsAlarmcfgConfiguration> {

    private final List<IAlarmTreeNode> _alarmTreeNodes;

    /**
     * Constructor.
     */
    public AlarmTreeContentModelBuilder(@Nonnull final List<IAlarmTreeNode> alarmTreeNodes) {
        _alarmTreeNodes = alarmTreeNodes;
    }

    /**
     * Creates a new node in the content for the given alarm tree node.
     * And then recursively for all of the alarm tree node children.
     *
     * @return the content model
     * @throws InvalidNameException
     */
    @Override
    @Nonnull
    protected ContentModel<LdapEpicsAlarmcfgConfiguration> createContentModel() throws CreateContentModelException {

        ContentModel<LdapEpicsAlarmcfgConfiguration> model;
        try {
            model = new ContentModel<LdapEpicsAlarmcfgConfiguration>(LdapEpicsAlarmcfgConfiguration.ROOT,
                                                                   AlarmTreeLdapConstants.EPICS_ALARM_CFG_FIELD_VALUE);

            for (final IAlarmTreeNode node : _alarmTreeNodes) {
                createSubtree(model, node, model.getRoot());
            }

            return model;
        } catch (final InvalidNameException e) {
            throw new CreateContentModelException("Error creating content model from alarm tree.", e);
        }
    }

    private static void createSubtree(@Nonnull final ContentModel<LdapEpicsAlarmcfgConfiguration> model,
                                      @Nonnull final IAlarmTreeNode alarmTreeNode,
                                      @Nonnull final ISubtreeNodeComponent<LdapEpicsAlarmcfgConfiguration> modelParentNode) throws InvalidNameException {

        final LdapEpicsAlarmcfgConfiguration oc = alarmTreeNode.getTreeNodeConfiguration();
        final String modelNodeName = alarmTreeNode.getName();

        final Attributes attributes = getAttributesFromAlarmTreeNode(alarmTreeNode);

        final ISubtreeNodeComponent<LdapEpicsAlarmcfgConfiguration> newModelComponent =
            new TreeNodeComponent<LdapEpicsAlarmcfgConfiguration>(
                    modelNodeName,
                    oc,
                    oc.getNestedContainerClasses(),
                    modelParentNode,
                    attributes,
                    (LdapName) modelParentNode.getLdapName().add(new Rdn(oc.getNodeTypeName(), modelNodeName)));

        model.addChild(modelParentNode, newModelComponent);

        if (alarmTreeNode instanceof SubtreeNode) {
            for (final IAlarmTreeNode alarmTreeChild : ((SubtreeNode) alarmTreeNode).getChildren()) {
                createSubtree(model, alarmTreeChild, newModelComponent);
            }
        }
    }

    private static Attributes getAttributesFromAlarmTreeNode(final IAlarmTreeNode alarmTreeNode) {

        final Attributes attributes = new BasicAttributes();
        for (final AlarmTreeNodePropertyId propId : AlarmTreeNodePropertyId.values()) {
            final String property = alarmTreeNode.getProperty(propId);
            if (property != null) {
                attributes.put(propId.getLdapAttribute(), property);
            }
        }
        return attributes;
    }
}