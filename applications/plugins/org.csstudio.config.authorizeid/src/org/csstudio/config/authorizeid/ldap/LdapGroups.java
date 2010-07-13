package org.csstudio.config.authorizeid.ldap;

import static org.csstudio.config.authorizeid.LdapEpicsAuthorizeIdConfiguration.OU;
import static org.csstudio.config.authorizeid.LdapEpicsAuthorizeIdConfiguration.ROOT;
import static org.csstudio.utility.ldap.utils.LdapUtils.any;

import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.SearchResult;

import org.csstudio.config.authorizeid.AuthorizeIdActivator;
import org.csstudio.utility.ldap.reader.LDAPReader;
import org.csstudio.utility.ldap.reader.LdapSearchResult;
import org.csstudio.utility.ldap.service.ILdapService;
import org.csstudio.utility.ldap.utils.LdapUtils;

/**
 * Retrieve groups from LDAP.
 *
 * @author bknerr
 * @author $Author$
 * @version $Revision$
 * @since 29.04.2010
 */
public class LdapGroups {

    /**
     * Returns groups from LDAP.
     * @return groups
     */
    public String[] getGroups() {

        final ILdapService service = AuthorizeIdActivator.getDefault().getLdapService();

        final LdapSearchResult result =
            service.retrieveSearchResultSynchronously(LdapUtils.createLdapQuery(ROOT.getNodeTypeName(), ROOT.getRootTypeValue()),
                                                      any(OU.getNodeTypeName()),
                                                      LDAPReader.DEFAULT_SCOPE);



        final List<String> al = new ArrayList<String>();
        for (final SearchResult row : result.getAnswerSet()) {
            String name = row.getName();
            // TODO (rpovsic) : unsafe access - NPEs
            if(!(name.split(",")[0].equals(""))) {
                name = name.substring(3);
                al.add(name.split(",")[0]);
            }
        }
        return al.toArray(new String[al.size()]);
    }
}