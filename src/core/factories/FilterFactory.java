package core.factories;

import java.util.ArrayList;
import java.util.Arrays;

public class FilterFactory {
	
	private final String IPADDRESS_PATTERN = 
			"^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
	
	private final String safeChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890-=`\\~!@#$%^&*()_+[]{};':\",.<>/? ";
	
	private final ArrayList<String> blackList = 
			new ArrayList<String>(Arrays.asList("http://", "https://", "www.", ".com", ".org", ".net", ".co", ".uk", ".webs.com", ".tv",
												"fuck", "cunt", "ass", "bitch", "dick", "shit", "faggot", "fag")); //TODO: load more from SQL?
	
	public boolean safeMessage(String message) {
		char[] messageChars = message.toCharArray();
		for (char c : messageChars) {
			if (!this.safeChars.contains(Character.toString(c))) {
				return false;
			}
		}
		return true;
	}
	
	public ArrayList<String> getBlacklist() {
		return this.blackList;
	}
	
	public String getSafeChars() {
		 return this.safeChars;
	}
	
	public String getIPPattern() {
		return this.IPADDRESS_PATTERN;
	}
	
}
