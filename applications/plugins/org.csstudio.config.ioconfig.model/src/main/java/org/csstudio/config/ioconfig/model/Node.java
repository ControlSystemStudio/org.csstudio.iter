/*
 * Copyright (c) 2007 Stiftung Deutsches Elektronen-Synchrotron,
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
 */
package org.csstudio.config.ioconfig.model;

import java.awt.Image;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.csstudio.config.ioconfig.model.tools.NodeMap;
import org.csstudio.platform.security.SecurityFacade;
import org.csstudio.platform.security.User;
import org.hibernate.annotations.Cascade;

/**
 * 
 * @author gerke
 * @author $Author$
 * @version $Revision$
 * @since 21.03.2007
 */

@Entity
@Table(name = "ddb_node")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Node extends NamedDBClass implements Comparable<Node> {

    protected static final int DEFAULT_MAX_STATION_ADDRESS = 255;

    /**
     * The highest accept station address.
     */
    @Transient
    public static final int MAX_STATION_ADDRESS = 128;

    /**
     * The Node Patent.
     */
    private Node _parent;

    /**
     * The Version of the Node.
     */
    private int _version;

    /**
     * A set of all manipulated Node from this node.
     */
    private Set<Node> _alsoChanfedNodes = new HashSet<Node>();

    private Set<Node> _children = new HashSet<Node>();

    /**
     * A collection of documents that relate to this node.
     */
    private Set<Document> _documents = new HashSet<Document>();

    private String _description;

    private NodeImage _icon;

    /**
     * Default Constructor needed by Hibernate.
     */
    public Node() {

    }

    /**
     * 
     * @param parent
     *            set the Parent of this Node
     */
    public void setParent(final Node parent) {
        this._parent = parent;
    }

    /**
     * 
     * @return The parent of this Node.
     */
    @ManyToOne
    public Node getParent() {
        return _parent;
    }

    
    /**
     * 
     * @param id
     *            set the Node key ID.
     */
    @Override
    public void setId(final int id) {
        super.setId(id);
        NodeMap.put(id, this);
    }
    
    /**
     * 
     * @return the Children of this node.
     */
    @OneToMany(mappedBy = "parent", targetEntity = Node.class, fetch = FetchType.LAZY, cascade = {
            CascadeType.PERSIST, CascadeType.MERGE })
    @Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE,
            org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<? extends Node> getChildren() {
//        CentralLogger.getInstance().info(this, "Id\t"+getId()+"\tTime\t"+System.currentTimeMillis()+"\tClass\t"+
//                this.getClass().getSimpleName()+"");
        return _children;
    }

    /**
     * Set the Children to this node.
     * 
     * @param children
     *            The Children for this node.
     */
    public void setChildren(Set<Node> children) {
        _children = children;
    }

    /**
     * Add the Child to this node.
     * 
     * @param <T> The Type of the Children.
     * @param child the Children to add.
     * @return null or the old Node for the SortIndex Position.
     */
    public <T extends Node> Node addChild(T child) {
        short sortIndex = child.getSortIndex();
        Node oldNode = getChildrenAsMap().get(sortIndex);

        if(oldNode!=null&&oldNode.equals(child)) {
            return null;
        }
        child.setParent(this);
        child.setSortIndexNonHibernate(sortIndex);
        _children.add(child);

        while (oldNode != null ) {
            Node node = oldNode;
            sortIndex++;
            oldNode = getChildrenAsMap().get(sortIndex);
            node.setSortIndexNonHibernate(sortIndex);
        }
        return oldNode;
    }

    /**
     * Clear all children of this node.
     */
    protected void clearChildren() {
        _children.clear();
    }

    /**
     * Remove a children from this Node.
     * 
     * @param child
     *            the children that remove.
     */
    public void removeChild(Node child) {
        _children.remove(child);
    }

    /**
     * Remove a children from this Node.
     */
    public void removeAllChild() {
        clearChildren();
    }

    /**
     * Get the Children of the Node as Map. The Key is the Sort Index.
     * @return the children as map. 
     */
    @Transient
    public Map<Short, ? extends Node> getChildrenAsMap() {
        Map<Short, Node> nodeMap = new TreeMap<Short, Node>();
        for (Node child : getChildren()) {
            nodeMap.put(child.getSortIndex(), child);
        }
        return nodeMap;
    }

    /**
     * 
     * @param maxStationAddress
     *            the maximum Station Address.
     * @return the first free Station Address.
     */
    @Transient
    public short getfirstFreeStationAddress(int maxStationAddress) {
        Map<Short, ? extends Node> children = getChildrenAsMap();
        Short nextKey = 0;
        if (!children.containsKey(nextKey)) {
            return nextKey;
        }
        Set<Short> descendingKeySet = children.keySet();
        for (Short key : descendingKeySet) {
            if ((key - nextKey) > 1) {
                return (short) (nextKey + 1);
            }
            if(key>=0) {
                nextKey = key;
            }
        }
        return (short) (nextKey + 1);
    }

    /**
     * 
     * @return have this Node one or more children then return true else false.
     */
    public final boolean hasChildren() {
        return _children.size() > 0;
    }


    /**
     *  Die Tabellen MIME_FILES und MIME_FILES_DDB_NODE liegen auf einer anderen DB.
     *  Daher wird hier mit einem Link gearbeitet der folgenden Rechte ben�tigt.
     *  -  F�r MIME_FILES ist das Grand: select.
     *  -  F�r MIME_FILES_DDB_NODE ist das Grand: select, insert, update, delete.
     * 
     * @return Documents for the Node.
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.REFRESH })
    @JoinTable(name = "MIME_FILES_DDB_NODES_LINK", joinColumns = @JoinColumn(name = "docs_id", referencedColumnName = "id", unique = true), inverseJoinColumns = @JoinColumn(name = "nodes_id", referencedColumnName = "id"))
//    @JoinTable(name = "MIME_FILES_DDB_NODES_LINK_TEST", joinColumns = @JoinColumn(name = "docs_id", referencedColumnName = "id", unique = true), inverseJoinColumns = @JoinColumn(name = "nodes_id", referencedColumnName = "id"))
    public Set<Document> getDocuments() {
        return _documents;
    }

    /**
     * 
     * @param documents set the Documents for this node.
     */
    public void setDocuments(Set<Document> documents) {
        _documents = documents;
    }

    /**
     * 
     * @param document add the Document to this node. 
     * @return this Node.
     */
    public Node addDocument(Document document) {
        this._documents.add(document);
        return this;
    }

    /**
     * 
     * @return the Version of this node.
     */
    public int getVersion() {
        return _version;
    }

    /**
     * 
     * @param version
     *            the Version of this node.
     */
    public void setVersion(final int version) {
        this._version = version;
    }

    /**
     * 
     * @param sortIndex
     *            set the Index to sort the node inside his parent.
     */
    public void setSortIndexNonHibernate(short sortIndex) {
        if (getSortIndex() != sortIndex) {
            setSortIndex(sortIndex);
            if (getSortIndex() >= 0) {
                localUpdate();
            }
        }
    }

    /**
     * 
     * @return the Description of the Node.
     */
    public String getDescription() {
        return _description;
    }

    /**
     * 
     * @param description set the Description for this node.
     */
    public void setDescription(String description) {
        this._description = description;
    }

    @Transient
    private boolean isSortIndexDuplicate() {
        // the set can contain nodes with duplicate ids, but the map cannot!
        return getChildren().size() == getChildrenAsMap().size();
    }

    /**
     * Swap the SortIndex of two nodes. Is the given SortIndex in use the other node became the old
     * SortIndex of this node.
     * 
     * @param toIndex
     *            the new sortIndex for this node.
     */
    public void moveSortIndex(short toIndex) {
        short direction = 1;
        short index = this.getSortIndex();
        if (toIndex == index) {
            return;
        }
        if(getParent()==null) {
            setSortIndexNonHibernate(toIndex);
            return;
        }
        if (index == -1) {
            // Put a new Node in.
            if (index > toIndex) {
                direction = -1;
            }
            Node node = this;
            index = toIndex;
            do {
                Node nextNode = getParent().getChildrenAsMap().get(index);

                node.setSortIndexNonHibernate(index);
                node = nextNode;
                index = (short) (index + direction);
            } while (node != null);
        } else {
            // Move a exist Node
            short start = index;
            Node moveNode = getParent().getChildrenAsMap().get(index);
            if (index > toIndex) {
                direction = -1;
            }
            for (; start != toIndex; start+=direction) {
                Node nextNode = getParent().getChildrenAsMap().get((short)(start+direction));
                if(nextNode!=null) {
                    nextNode.setSortIndexNonHibernate(start);
                }
            }
            moveNode.setSortIndexNonHibernate(toIndex);
        }
    }

    /**
     * @param oldNode
     *            a node that a manipulated.
     */
    @Transient
    public void addAlsoChangedNodes(final Node oldNode) {
        _alsoChanfedNodes.add(oldNode);
    }

    /**
     * 
     */
    @Transient
    public void clearAlsoChangedNodes() {
        _alsoChanfedNodes.clear();
    }

    /**
     * {@link Comparable}.
     * 
     * @param other
     *            the node to compare whit this node.
     * @return if this node equals whit the give node return 0.
     */
    public int compareTo(final Node other) {
        if (this.getClass() != other.getClass()) {
            return -1;
        }
        int comper = getId() - other.getId();
        if (comper == 0 && getId() == 0) {
            comper = this.getSortIndex() - other.getSortIndex();
        }
        return comper;
    }

    @Deprecated
    public void setImage(Image image) {
        if (image != null) {
            // setImageBytes(image.getImageData().data);
        }
    }

//    @Transient
//    public final HashSet<Node> getChangeNodeSet() {
//        return _changeNodeSet;
//    }

    /**
     * Copy this node to the given Parent Node.
     * 
     * @param parentNode
     *            the target parent node.
     * @return the copy of this node.
     */
    public Node copyThisTo(Node parentNode) {
        String createdBy = "Unknown";
        try {
            User user = SecurityFacade.getInstance().getCurrentUser();
            if (user != null) {
                createdBy = user.getUsername();
            }
        } catch (NullPointerException e) {
            createdBy = "Unknown";
        }
        Node copy = copyParameter(parentNode);
        copy.setCreatedBy(createdBy);
        copy.setUpdatedBy(createdBy);
        copy.setCreatedOn(new Date());
        copy.setUpdatedOn(new Date());
        //TODO: so umbauen das "Copy of" als prefix parameter �bergeben wird.
//        copy.setName("Copy of " + getName());
        copy.setName(getName());
        copy.setVersion(getVersion());
        if(parentNode!=null) {
            parentNode.localUpdate();
        }
        return copy;
    }

    
    // ---- Test Start
    
//    @ManyToOne
    @Transient
    public NodeImage getIcon() {
        return _icon;
    }
//
//    /**
//     * Set the Children to this node.
//     * 
//     * @param children
//     *            The Children for this node.
//     */
    public void setIcon(NodeImage icon) {
        _icon = icon;
    }
    
    // ---- Test End
    
    
    
    /**
     * Copy this node and set Special Parameter.
     * 
     * @param parent the parent Node for the Copy.
     * 
     * @return a Copy of this node.
     */
    protected abstract Node copyParameter(NamedDBClass parent);

    /**
     * Save his self. 
     * @throws PersistenceException
     */
    public void localSave() throws PersistenceException {
        save();
    }

    /**
     * make the data update for his self. 
     */
    protected void localUpdate() {
    }

    /**
     * Update date it self and his siblings. 
     */
    public void update() {
        if (isRootNode()) {
            localUpdate();
            updateChildrenOf(this);
        } else {
            updateChildrenOf(getParent());
        }
    }

    /**
     * Update the node an his children.
     * @param parent the node to update.
     */
    protected void updateChildrenOf(Node parent) {
        for (Node n : parent.getChildrenAsMap().values()) {
            n.localUpdate();
            updateChildrenOf(n);
        }
    }

    /**
     * 
     * @return is only true if this Node a Root Node.
     */
    @Transient
    public boolean isRootNode() {
        return getParent() == null;
    }

    /**
     * Assemble the Epics Address String of the children Channels. 
     */
    @Transient
    public void assembleEpicsAddressString() throws PersistenceException {
        for (Node node : getChildren()) {
            if (node != null) {
                node.assembleEpicsAddressString();
                if (node.isDirty()) {
                    node.save();
                }
            }
        }
    }

    /** 
     * (@inheritDoc)
     */
    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (obj instanceof Node ) {
            
            Node node = (Node) obj;
            if(getId()==node.getId()) {
                if(getId()>0) {
                    return true;
                }
                return false;
            }
        }
        return false;
   }

    
}