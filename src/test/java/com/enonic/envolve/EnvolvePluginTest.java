package com.enonic.envolve;

import java.io.InputStream;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.Before;

import org.junit.Test;
import static org.junit.Assert.*;

public class EnvolvePluginTest
{
    /*EnvolvePlugin plugin;
    Element user;
    Element user2;

    @Before
    public void setUp()
        throws Exception
    {
        plugin = new EnvolvePlugin();
        try
        {
            InputStream stream = EnvolvePlugin.class.getResourceAsStream( "/user.xml" );
            user = new SAXBuilder().build( stream ).getRootElement();

            InputStream stream2 = EnvolvePlugin.class.getResourceAsStream( "/user2.xml" );
            user2 = new SAXBuilder().build( stream2 ).getRootElement();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetFirstName()
        throws Exception
    {
        String firstName = plugin.getFirstName( user );
        assertEquals("Michael", firstName);
    }

    @Test
    public void testGetLastName()
        throws Exception
    {
        String lastName = plugin.getLastName( user );
        assertEquals( "Lazell", lastName );
    }

    @Test
    public void testGetImageUrl()
        throws Exception
    {
        String imageUrl = plugin.getImageUrl( user, "http://example.com" );
        String noImageUrl = plugin.getImageUrl( user, "" );
        String noPhoto = plugin.getImageUrl( user2, "http://example.com" );

        assertEquals( "http://example.com/_image/user/9E17AB86497176A4B8E27EE9E20610E4AF1860E5", imageUrl );
        assertEquals( null, noImageUrl );
        assertEquals( null, noPhoto );
    }

    @Test
    public void testIsChatAdmin()
        throws Exception
    {
        boolean chatAdmin = plugin.isChatAdmin( user, "Chat admin" );
        boolean notChatAdmin = plugin.isChatAdmin( user2, "Chat" );

        assertEquals( chatAdmin, true );
        assertEquals( notChatAdmin, false );
    }

    @Test
    public void testGetHtml()
        throws Exception
    {
        String html = plugin.getHtml( user, "#title,#phone(Mobile)", "http://example.com/_image/user/9E17AB86497177A4B8E27EE9E20610E4AF1860E5" );

        assertEquals("<div class='envProfileRolloverPic'>" +
                         "<img src='http://example.com/_image/user/9E17AB86497177A4B8E27EE9E20610E4AF1860E5' style='max-width:73px;max-height:73px;'>" +
                         "</div>" +
                         "<div class='envUserProfileNameText'>Michael Lazell</div>", html);
    }

    @Test
    public void testLabel()
    {
        String labelText = "#mobile label(Mobile )";

        System.out.println(labelText.substring( labelText.indexOf( "label(" ) + 6, labelText.indexOf( ")" ) ));
    }

    @Test
    public void testGetLabel()
    {
        String label = plugin.getLabel( "#mobile label(Mobile: )" );
        assertEquals( label, "Mobile: " );
    }

    @Test
    public void testGetLabel2()
    {
        String label = plugin.getLabel( "#mobile" );
        assertEquals( label, "" );
    }

    @Test
    public void testGetLocation()
    {
        assertEquals( "<p style='text-align:left;'>Location: Oslo, Norway</p>", plugin.getLocation( user, "#location label(Location: )" ) );
    }*/

}
