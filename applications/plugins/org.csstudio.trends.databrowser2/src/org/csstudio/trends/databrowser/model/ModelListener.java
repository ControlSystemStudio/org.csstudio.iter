package org.csstudio.trends.databrowser.model;

/** Listener interface for the Model
 *  @author Kay Kasemir
 */
public interface ModelListener
{
    /** The update period changed */
    void changedUpdatePeriod();

    /** One of the colors (background, ...) changed */
    void changedColors();

    /** The time range (start/end time or span) was changed */
    void changedTimerange();

    /** @param axis Axis that changed its color, range, ....
     *              If <code>null</code>, an axis was added or removed
     */
    void changedAxis(AxisConfig axis);

    /** @param item Item that was added to the model */
    void itemAdded(ModelItem item);

    /** @param item Item that was removed from the model */
    void itemRemoved(ModelItem item);

    /** @param item Item that turned visible/invisible */
    void changedItemVisibility(ModelItem item);
    
    /** @param item Item that changed its visible attributes:
     *              color, line width, display name, ...
     */
    void changedItemLook(ModelItem item);

    /** @param item Item that changed its data configuration:
     *              Archives, request method.
     */
    void changedItemDataConfig(PVItem item);
    
    /** @param scroll_enabled <code>true</code> when scrolling was turned 'on' */
    void scrollEnabled(boolean scroll_enabled);
}