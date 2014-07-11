package com.envolve.api;

import java.math.BigInteger;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class EnvolveAPIEncoder
{
	private static final String ENVOLVE_JS_ROOT =  "\" + envProtoType + \"d.envolve.com/env.nocache.js";
	//private static final String ENVOLVE_JS_ROOT = "/env/env.nocache.js";
	private static final String API_VERSION = "0.3";

	private String mySecret;
	private int mySiteID;

	/**
	 * Constructs an EnvolveAPIEncoder using the given API key. An api key can
	 * be obtained by registering your website at http://www.envolve.com
	 * @param apiKey Your API key
	 * @throws EnvolveAPIException
	 */
	public EnvolveAPIEncoder(String apiKey) throws EnvolveAPIException
	{
		//first, lets validate the args.
		String apiKeyPieces[] = apiKey.split("-");
		if(apiKeyPieces.length != 2)
		{
			throw new EnvolveAPIException("Invalid API Key");
		}
		try
		{
			//make sure the siteID is an integer... just to see that the key's ok.
			this.mySiteID = Integer.parseInt(apiKeyPieces[0]);
		}
		catch(NumberFormatException nfe)
		{
			throw new EnvolveAPIException("Invalid API Key");
		}
		this.mySecret = apiKeyPieces[1];
	}

	/**
	 * This method creates the full HTML that should be included in a page for an anonymous user.
	 * @return HTML that should be added to the page on which Envolve is to be displayed.
	 */
	public String getHTMLForAnonymousUser()
	{
		return this.getHTML(null);
	}

	/**
	 * This method creates the full HTML that should be included in a page for a logged-in user.
	 * @param firstName The first name or username for the user. (required)
	 * @param lastName The last name of the user. Pass null if unused.
	 * @param picture An absolute URL to the location of the user's profile picture.
	 * @param isAdmin Is this user an administrator?
	 * @param profileHTML Optional profile rollover HTML for this user.
	 * @return HTML that should be added to the page on which Envolve is to be displayed.
	 */
	public String getHTMLForLoggedInUser(String firstName, String lastName, String picture, boolean isAdmin, String profileHTML) throws EnvolveAPIException
	{
		String command = this.getLoginCommand(firstName, lastName, picture, isAdmin, profileHTML);
		return this.getHTML(command);
	}

	/**
	 * This method creates a login command string that can be passed to Envolve in order to
	 * programmatically log a user in.
	 * @param firstName The first name or username for the user. (required)
	 * @param lastName The last name of the user. Pass null if unused.
	 * @param picture An absolute URL to the location of the user's profile picture.
	 * @param isAdmin Is this user an administrator?
	 * @param profileHTML Optional profile rollover HTML for this user
	 * @return A login command string that can be passed to Envolve through the javascript API
	 */
	public String getLoginCommand(String firstName, String lastName, String picture, boolean isAdmin, String profileHTML) throws EnvolveAPIException
	{
		if(firstName == null)
		{
			throw new EnvolveAPIException("You must provide at least a first name. If you are providing a username, use it for the first name and set the last name to null");
		}

		String commandString = "v=" + API_VERSION + ",c=login,fn=" + EnvolveAPIEncoder.base64Encode(firstName);
		if(lastName != null)
		{
			commandString += ",ln=" + EnvolveAPIEncoder.base64Encode(lastName);
		}
		if(picture != null)
		{
			commandString += ",pic=" + EnvolveAPIEncoder.base64Encode(picture);
		}
		if(isAdmin)
		{
			commandString += ",admin=t";
		}
		if(profileHTML != null)
		{
			commandString += ",prof=" + EnvolveAPIEncoder.base64Encode(profileHTML);
		}
		return this.signCommand(commandString);
	}

	/**
	 * This method creates a logout command that tells Envolve to generate a new
	 * anonymous user. It can be passed to Envolve through the javascript API.
	 * @return Your logout command string
	 */
	public String getLogoutCommand() throws EnvolveAPIException
	{
		return this.signCommand("c=logout");
	}

	/////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Helper Functions ////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////

	private String getHTML(String command)
	{
		String retVal = "<!-- Envolve Chat -->\n<script type=\"text/javascript\">\n" + "var envoSn=" + this.mySiteID + ";\n";
		if(command != null)
		{
			retVal += "var env_commandString=\"" + command + "\";\n";
		}
		retVal += "var envProtoType = ((\"https:\" == document.location.protocol) ? \"https://\" : \"http://\");\n" +
			"document.write(unescape(\"%3Cscript src='" + ENVOLVE_JS_ROOT + "' type='text/javascript'%3E%3C/script%3E\"));\n"
			+ "</script>\n";
		return retVal;
	}

	private String signCommand(String command) throws EnvolveAPIException
	{
		String commandString = (new Date()).getTime() + ";" + command;
		return EnvolveAPIEncoder.computeHMAC_SHA1Hash(commandString, this.mySecret) + ";" + commandString;
	}

	public static String computeHMAC_SHA1Hash(String data, String key) throws EnvolveAPIException
	{
		try
		{
			// get an hmac_sha1 key from the raw key bytes
			byte keyBytes[] = key.getBytes("UTF-8");
			SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

			// get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(signingKey);

			// compute the hmac on input data bytes
			byte[] dataBytes = data.getBytes("UTF-8");
			byte[] rawHmac = mac.doFinal(dataBytes);

			// hex-encode the hmac
			return EnvolveAPIEncoder.toHex(rawHmac);
		}
		catch (Exception e)
		{
			throw new EnvolveAPIException("Failed to generate HMAC : " + e.getMessage());
		}
	}

	public static String toHex(byte[] bytes)
	{
		BigInteger bi = new BigInteger(1, bytes);
		return String.format("%0" + (bytes.length << 1) + "x", bi);
	}

	//base 64 encoder. I don't want to use the one in Apache Commons because then people would have to install that
	//in order to use our API
	private static final char[] base64code = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ"
		+ "abcdefghijklmnopqrstuvwxyz" + "0123456789" + "+/").toCharArray();

	public static String base64Encode(String string) throws EnvolveAPIException
	{
		StringBuffer encoded = new StringBuffer();
		byte[] dataArray;
		try
		{
			dataArray = string.getBytes("UTF-8");  // use appropriate encoding string!
		}
		catch (Exception ignored)
		{
			throw new EnvolveAPIException("Envolve API requires support for UTF-8");
		}

		// add any necessary padding to the input
		int paddingCount = ((3 - (dataArray.length % 3)) % 3);
		int paddedLength = dataArray.length + paddingCount;
		byte[] padded = new byte[paddedLength]; // initialized to zero by JVM
		System.arraycopy(dataArray, 0, padded, 0, dataArray.length);


		for (int i = 0; i < padded.length; i += 3)
		{
			int j = ((padded[i] & 0xff) << 16) +
			((padded[i + 1] & 0xff) << 8) +
			(padded[i + 2] & 0xff);
			encoded.append(base64code[(j >> 18) & 0x3f]);
			encoded.append(base64code[(j >> 12) & 0x3f]);
			encoded.append(base64code[(j >> 6) & 0x3f]);
			encoded.append(base64code[j & 0x3f]);
		}

		String retVal = encoded.substring(0, encoded.length() -
	            paddingCount) + "==".substring(0, paddingCount);

		return retVal;
	}
}
