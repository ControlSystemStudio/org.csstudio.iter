package org.csstudio.archivereader.channelarchiver;

import java.net.UnknownHostException;

import org.apache.xmlrpc.XmlRpcClient;
import org.csstudio.apputil.text.RegExHelper;
import org.csstudio.archivereader.ArchiveInfo;
import org.csstudio.archivereader.ArchiveReader;
import org.csstudio.archivereader.ValueIterator;
import org.csstudio.platform.data.ITimestamp;
import org.csstudio.platform.data.IValue;

/** Main access point to the ChannelArchiver network data server.
 *  @author Kay Kasemir
 *  @author Blaz Lipuscek worked on org.csstudio.archive.channelarchiver.ArchiveServer
 */
@SuppressWarnings("nls")
public class ChannelArchiverReader implements ArchiveReader
{
    final private String url;
    final private XmlRpcClient xmlrpc;
    final private ServerInfoRequest server_info_request;
    final private ArchivesRequest archives_request;

    /** Connect to a ChannelArchiver's network data server.
     *  
     * @param url_text For example
     *        "xnds://my_server.org/archive/cgi/ArchiveDataServer.cgi"
     * @throws Exception
     */
    ChannelArchiverReader(final String url) throws Exception
    {
        this.url = url;
        try
        {
            // Patch 'xdns' into http, but keep the 'official' URL
            // as received.
            final String real_url;
            if (url.startsWith("xnds://"))
                real_url = "http://" + url.substring(7);
            else
                real_url = url;
            // Create client
            xmlrpc = new XmlRpcClient(real_url);
            
            // Get server info
            server_info_request = new ServerInfoRequest();
            server_info_request.read(xmlrpc);
            
            // .. and archive keys
            archives_request = new ArchivesRequest();
            archives_request.read(xmlrpc);
        }
        catch (UnknownHostException ex)
        {
            throw new Exception("Unknown host in URL " + url);
        }
    }
    
    /** {@inheritDoc}*/
    public String getServerName()
    {
        return "Channel Archiver";
    }

    /** {@inheritDoc}*/
    public String getURL()
    {
        return url;
    }

    /** {@inheritDoc}*/
    public String getDescription()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append(server_info_request.getDescription());
        buf.append("Request Types:\n");
        for (String req : server_info_request.getRequestTypes())
        {
            buf.append(req + "\n");
        }
        return buf.toString();
    }

    /** {@inheritDoc}*/
    public int getVersion()
    {
        return server_info_request.getVersion();
    }

    /** {@inheritDoc}*/
    public ArchiveInfo[] getArchiveInfos()
    {
        return archives_request.getArchiveInfos();
    }

    /** {@inheritDoc}*/
    public String[] getNamesByPattern(final int key, final String glob_pattern)
            throws Exception
    {
        return getNamesByRegExp(key, RegExHelper.fullRegexFromGlob(glob_pattern));
    }

    /** {@inheritDoc}*/
    public String[] getNamesByRegExp(final int key, final String reg_exp) throws Exception
    {
        final NamesRequest infos = new NamesRequest(key, reg_exp);
        infos.read(xmlrpc);
        return infos.getNameInfos();
    }

    /** Helper for locating a request code by name.
     *  <p> 
     * @param request_name For example: GET_RAW.
     * @return The 'request_type' ID for a given request type string.
     * @throws Exception when asking for unsupported request type.
     * @see #getRequestTypes()
     */
    int getRequestCode(String request_name) throws Exception
    {
        final String request_types[] = server_info_request.getRequestTypes();
        for (int i=0; i<request_types.length; ++i)
            if (request_types[i].equalsIgnoreCase(request_name)) // add  IgnoreCase Albert
                return i;
        throw new Exception("Unsupported request type '" + request_name + "'");
    }
    
    /** @return Severity for an EPICS severity code. */
    SeverityImpl getSeverity(final int severity)
    {
        return server_info_request.getSeverity(severity);
    }

    /** @return EPICS/ChannelArchiver status string for given code */
    String getStatus(final SeverityImpl severity, final int status)
    {
        if (severity.statusIsText())
        {
            final String[] status_strings = server_info_request.getStatusStrings();
            if (status >= 0  &&  status < status_strings.length)
                return status_strings[status];
            // else: Fall through...
        }
        // return the number as a string
        return Integer.toString(status);            
    }

    /** Active request. Synchronize on this for access */
    private ValueRequest current_request = null;
    
    /** Issue request for values for one channel to the data server.
     *  @return Samples
     *  @throws Exception
     */
    public IValue[] getSamples(final int key, final String name,
            final ITimestamp start, final ITimestamp end,
            final boolean optimized, final int count) throws Exception
    {
        synchronized (this)
        {
            current_request =
                new ValueRequest(this, key, name, start, end, optimized, count);
        }
        current_request.read(xmlrpc);
        final IValue result[] = current_request.getSamples();
        synchronized (this)
        {
            current_request = null;
        }
        return result;
    }
    
    /** {@inheritDoc}*/
    public ValueIterator getRawValues(final int key, final String name,
            final ITimestamp start, final  ITimestamp end) throws Exception
    {
        return new ValueRequestIterator(this, key, name, start, end, false, 10);
    }

    /** {@inheritDoc}*/
    public ValueIterator getOptimizedValues(final int key, final String name,
         final ITimestamp start, final ITimestamp end, final int count) throws Exception
    {
        return new ValueRequestIterator(this, key, name, start, end, true, count);
    }

    /** {@inheritDoc}*/
    public void cancel()
    {
        synchronized (this)
        {
            if (current_request != null)
                current_request.cancel();
        }
    }

    /** {@inheritDoc}*/
    public void close()
    {
        cancel();
    }
}