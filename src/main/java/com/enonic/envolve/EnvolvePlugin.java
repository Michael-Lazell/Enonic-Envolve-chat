package com.enonic.envolve;

import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;

import com.envolve.api.EnvolveAPIEncoder;
import com.envolve.api.EnvolveAPIException;

import com.enonic.cms.api.client.Client;
import com.enonic.cms.api.client.ClientFactory;
import com.enonic.cms.api.client.model.GetUserParams;

/*
Sample datasource
<datasources>
    <datasource name="invokeExtension">
        <parameter name="name">envolveChat.envolveLogin</parameter>
        <parameter name="param1">123456-zDEf1iL1qvcetjqwPRR4l3r7jWMtUm6E</parameter>
        <parameter name="param2">http://slidesolutions.com</parameter>
        <parameter name="param3">Chat admin</parameter>
        <parameter name="param4">#title,#phone label(Phone ),#mobile label(Mobile ),#location label(Location)</parameter>
    </datasource>
</datasources>
*/

/**
 * Created by IntelliJ IDEA.
 * User: mla - Michael Lazell -
 * Date: 04/10/2013
 * Creates the string for Envolve chat
 * https://enonic.com/en/try-now  (Community edition demo kit)
 * http://www.enonic.com/en/contact-us
 */
public class EnvolvePlugin
{
    private EnvolveAPIEncoder apiEncoder;
    private Element envolve = new Element( "envolve" );
    private Logger log = Logger.getLogger( EnvolvePlugin.class.getName() );

    /**
     * Called by a portlet or page template datasource. This returns a JDOM Document with the javascript required to
     * sign in the logged in user to chat or set up a guest chat user when not logged in.
     * @param apiKey This is the Envolve chat API key that you get from your Envolve account page.
     * @param baseImageUrl The base URL of your website admin console. This URL is used to retrieve the users image.
     * @param chatAdminGroupName The name of a group in the the Enonic userstore that is used to designate chat admins.
     * @param html Any custom HTML that you want to see in the user field when hovering over a chat user's name.
     * @return The JDOM Document with JavaScript for SSO if logged in user, or creating a guest user if not logged in.
     * @throws Exception
     */
    public Document envolveLogin( String apiKey, String baseImageUrl, String chatAdminGroupName, String html )
        throws Exception
    {
        log.info( "Starting the Envolve chat plugin" );

        apiEncoder = new EnvolveAPIEncoder(apiKey);

        // Log in and get the user info
        Client cl = ClientFactory.getLocalClient();
        String userName = cl.getUserName();
        //log.info( "User name is: " + userName );


        if ( userName.equalsIgnoreCase( "anonymous" ) )
        {
            envolve.setText( apiEncoder.getHTMLForAnonymousUser() );
        }
        else
        {
            envolve.setText( getLoginString( cl, baseImageUrl, chatAdminGroupName, html ) );
        }

        envolve.detach();

        return new Document( envolve );
    }

    public String getLoginString(Client cl, String baseImageUrl, String chatAdminGroupName, String html)
        throws EnvolveAPIException
    {
        GetUserParams up = new GetUserParams();
        up.user = null;
        up.includeCustomUserFields = true;
        up.includeMemberships = true;
        up.normalizeGroups = true;
        Element user = cl.getUser( up ).getRootElement();

        String firstName = getFirstName( user );
        String lastName = getLastName( user );
        String fullImageUrl = getImageUrl( user, baseImageUrl );
        boolean chatAdmin = isChatAdmin( user, chatAdminGroupName );
        String htmlInput = getHtml( user, html, fullImageUrl );

        //log.info( "firstName is: " + firstName + " -- lastName is : " + lastName + " -- fullImageUrl is: " +
        //    fullImageUrl + " -- chatAdmin is: " + chatAdmin + " -- html is: " + html);

        return apiEncoder.getHTMLForLoggedInUser( firstName, lastName, fullImageUrl, chatAdmin, htmlInput );
    }

    public String getFirstName(Element user)
    {
        return (user.getChild("first-name") != null && user.getChild("first-name").getText().length() > 0) ?
            user.getChild( "first-name" ).getText() :
            user.getChild( "display-name" ).getText();
    }

    public String getLastName(Element user)
    {
        return (user.getChild("last-name") != null && user.getChild("last-name").getText().length() > 0) ?
            user.getChild( "last-name" ).getText() :
            null;
    }

    public String getImageUrl(Element user, String baseImageUrl)
    {
        try
        {
            if ( baseImageUrl.length() > 5 && user.getChild( "photo" ).getAttributeValue( "exists" ).equalsIgnoreCase( "true" ) )
            {
                return baseImageUrl + "/_image/user/" + user.getAttributeValue( "key" );
            }
        }
        catch ( Exception e )
        {
            return null;
        }
        return null;
    }

    public boolean isChatAdmin(Element user, String chatAdminGroupName)
    {
        try
        {
            List<Element> groups = user.getChild( "memberships" ).getChildren( "group" );
            for ( Element el : groups ){
                if ( el.getChild( "name" ).getValue().equalsIgnoreCase( chatAdminGroupName ) )
                {
                    return true;
                }
            }
        }
        catch ( Exception e )
        {
            return false;
        }
        return false;
    }

    public String getHtml(Element user, String html, String imageUrl)
    {
        // #title,#phone label(Phone ),#mobile label(Mobile ), #location label(Location )
        StringBuilder htmlOutput = new StringBuilder();

        if ( html == null || html.trim().isEmpty() )
        {
            htmlOutput.append( defaultHtml(user, imageUrl) );
        }
        else if ( html.contains("#title") || html.contains("#phone") || html.contains("#mobile") || html.contains("#email") || html.contains( "#location" ) )
        {
            htmlOutput.append( defaultHtml(user, imageUrl) );

            String[] inputArray = html.split( "," );
            for ( String str: inputArray )
            {

                if ( str.contains( "#title" ) || str.contains( "#phone" ) || str.contains("#mobile") || str.contains("#email") || html.contains( "#location" ) )
                {

                    if (str.contains( "#title" ))
                    {
                        htmlOutput.append( lineOutput( user, "title", str ) );
                    }

                    if (str.contains( "#phone" ))
                    {
                        htmlOutput.append( lineOutput( user, "phone", str ) );
                    }

                    if (str.contains( "#mobile" ))
                    {
                        htmlOutput.append( lineOutput( user, "mobile", str ) );
                    }
                    if (str.contains( "#email" ))
                    {
                        htmlOutput.append( lineOutput( user, "email", str ) );
                    }

                    if ( str.contains( "#location" ))
                    {
                        htmlOutput.append( getLocation( user, str ) );
                    }


                }
            }

        }
        else
        {
            return html;
        }

        return htmlOutput.toString();

    }

    public String getLocation( Element user, String str )
    {
        StringBuilder sb = new StringBuilder();


        String city = new String();
        String country = new String();

        Element address = new Element( "address" );
        if ( user.getChild( "addresses" ) != null && user.getChild( "addresses" ).getChild( "address" ) != null )
        {
            address = user.getChild( "addresses" ).getChild( "address" );
        }

        if ( address.getChild( "postal-address" ) != null && address.getChild( "postal-address" ).getText().trim().length() > 0 )
        {
            city = address.getChild( "postal-address" ).getText().trim();
        }

        if ( address.getChild( "iso-country" ) != null && address.getChild( "iso-country" ).getText().length() > 0 )
        {
            String countryCode = address.getChild( "iso-country" ).getText();
            Locale l = new Locale( "", countryCode );
            country = l.getDisplayCountry();
        }

        if ( city.length() > 0 || country.length() > 0 )
        {
            sb.append( "<p style='text-align:left;'>" );
            sb.append( getLabel( str ) );

            if ( city.length() > 0 && country.length() > 0 )
            {
                sb.append( city + ", " + country );
            } else if ( city.length() > 0 )
            {
                sb.append( city );
            } else if ( country.length() > 0 )
            {
                sb.append( country );
            }
            sb.append( "</p>" );

        }

        return sb.toString();
    }

    public String defaultHtml(Element user, String imageUrl)
    {
        StringBuilder sb = new StringBuilder();
        if ( imageUrl != null )
        {
            sb.append(
                "<div class='envProfileRolloverPic'>" +
                    "<img src='" + imageUrl + "' style='max-width:73px;max-height:73px;'>" +
                "</div>"
            );
        }

        sb.append( "<div class='envUserProfileNameText'>" + getFirstName( user ) + " " );
        if ( getLastName(user) != null )
        {
            sb.append( getLastName( user ) );
        }
        sb.append("</div>" );
        return sb.toString();
    }

    public String lineOutput(Element user, String input, String str)
    {
        StringBuilder sb = new StringBuilder();

        if ( user.getChild(input) != null && user.getChild(input).getText().length() > 0 )
        {
            sb.append( "<p style='text-align:left;'>" ) ;

            /*if ( str.contains( "label(" ) && str.substring( str.indexOf( "label(" ), str.length() ).contains( ")" ) )
            {
                sb.append( str.substring( str.indexOf( "label(") + 6, str.indexOf( ")" ) ) );
            }*/
            sb.append( getLabel( str ) );

            sb.append( user.getChild(input).getText() ) ;
            sb.append( "</p>" ) ;
        }

        return sb.toString();
    }

    public String getLabel(String str)
    {
        StringBuilder sb = new StringBuilder();
        if ( str.contains( "label(" ) && str.substring( str.indexOf( "label(" ), str.length() ).contains( ")" ) )
        {
            sb.append( str.substring( str.indexOf( "label(") + 6, str.indexOf( ")" ) ) );
        }
        return sb.toString();
    }


}
