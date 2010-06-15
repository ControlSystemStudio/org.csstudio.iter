package org.csstudio.nams.configurator.treeviewer.model;

import java.beans.PropertyChangeSupport;

public class AlarmtopicBean extends AbstractConfigurationBean<AlarmtopicBean> {

	public static enum AlarmtopicBeanPropertyNames {
		topicID, topicName, humanReadableName, description

	}

	private int topicID;// PRIMARY KEY
	private String topicName;
	private String humanReadableName;
	private String description;
	private PropertyChangeSupport propertyChangeSupport;

	public AlarmtopicBean() {
		topicID = -1;
		propertyChangeSupport = getPropertyChangeSupport();
	}

	public int getTopicID() {
		return topicID;
	}

	public void setTopicID(int topicID) {
		int oldValue = getTopicID();
		this.topicID = topicID;
		propertyChangeSupport.firePropertyChange(
				AlarmtopicBeanPropertyNames.topicID.name(), oldValue,
				getTopicID());
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		String oldValue = getTopicName();
		this.topicName = topicName;
		propertyChangeSupport.firePropertyChange(
				AlarmtopicBeanPropertyNames.topicName.name(), oldValue,
				getTopicName());
	}

	public String getHumanReadableName() {
		return humanReadableName;
	}

	public void setHumanReadableName(String humanReadableName) {
		String oldValue = getHumanReadableName();
		this.humanReadableName = humanReadableName;
		propertyChangeSupport.firePropertyChange(
				AlarmtopicBeanPropertyNames.humanReadableName.name(), oldValue,
				getHumanReadableName());
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		String oldValue = getDescription();
		this.description = description;
		propertyChangeSupport.firePropertyChange(
				AlarmtopicBeanPropertyNames.description.name(), oldValue,
				getDescription());
	}

	public String getDisplayName() {
		return getHumanReadableName() != null ? getHumanReadableName()
				: "(ohne Namen)";
	}

	public void copyStateOf(AlarmtopicBean otherBean) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("not implemented yet.");
	}

	@Override
	public AlarmtopicBean getClone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AlarmtopicBean) {
			AlarmtopicBean bean = (AlarmtopicBean) obj;
			return this.getTopicID() == bean.getTopicID();
		}
		return super.equals(obj);
	}

	@Override
	public void updateState(AlarmtopicBean bean) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getID() {
		return this.getTopicID();
	}

}