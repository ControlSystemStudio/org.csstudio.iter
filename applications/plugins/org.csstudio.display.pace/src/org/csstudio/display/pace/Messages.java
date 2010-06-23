package org.csstudio.display.pace;

import org.eclipse.osgi.util.NLS;

/** Access to messages externalized to
 *  language-specific messages*.properties files.
 *  
 *  @author Kay Kasemir
 *  @author Eclipse "Externalize Strings" wizard
 *    reviewed by Delphy 01/28/09
 */
public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.csstudio.display.pace.messages"; //$NON-NLS-1$


    public static String ConfirmChangesFmt;
    public static String FileChangedFmt;
    public static String FileUnchangedFmt;
    public static String InstanceLabelProvider_OrigAppendix;
    public static String InstanceLabelProvider_PVValueFormat;
    public static String InstanceLabelProvider_PVCommentTipFormat;
    public static String InstanceLabelProvider_ReadOnlyAppendix;
    public static String Preferences_DefaultLogbook;
    public static String Preferences_Message;
    public static String RestoreCell;
    public static String RestoreCell_TT;
    public static String SaveError;
    public static String SaveErrorFmt;
    public static String SaveIntro;
    public static String ELogTitleFmt;
    public static String SavePVInfoFmt;
    public static String SaveCommentInfoFmt;
    public static String SaveTitle;
    public static String SetColumnValue_Msg;
    public static String SetValue;
    public static String SetValue_Msg;
    public static String SetValue_Msg_WithReadonlyWarning;
    public static String SetValue_Title;
    public static String SetValue_TT;
    public static String SystemColumn;
    public static String UnknownValue;
    
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {
        // Prevent instantiation
    }
}