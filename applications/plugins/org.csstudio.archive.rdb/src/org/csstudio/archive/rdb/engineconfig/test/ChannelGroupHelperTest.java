package org.csstudio.archive.rdb.engineconfig.test;

import static org.junit.Assert.assertEquals;

import java.sql.Statement;

import org.csstudio.archive.rdb.RDBArchive;
import org.csstudio.archive.rdb.TestSetup;
import org.csstudio.archive.rdb.engineconfig.ChannelGroupConfig;
import org.csstudio.archive.rdb.engineconfig.ChannelGroupHelper;
import org.junit.Test;

/** ChannelGroupHelper test
 *  <p>
 *  @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class ChannelGroupHelperTest
{
    @Test
    public void test() throws Exception
    {
        final RDBArchive archive = RDBArchive.connect(TestSetup.URL, TestSetup.USER, TestSetup.PASSWORD);
        
        final ChannelGroupHelper groups = new ChannelGroupHelper(archive);
        ChannelGroupConfig group = groups.find("Test", 1);
        System.out.println(group);
        
        group = groups.add("Another Test", 2, 0);
        System.out.println(group);
        
        final Statement statement = archive.getRDB().getConnection().createStatement();
        final int rows = statement.executeUpdate(
                "DELETE FROM chan_grp WHERE grp_id=" + group.getId());
        assertEquals(1, rows);
        statement.close();
        
        archive.close();
    }
}