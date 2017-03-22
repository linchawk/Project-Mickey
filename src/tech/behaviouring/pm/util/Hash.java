package tech.behaviouring.pm.util;

import java.security.*;

/*
 * Created by Mohan on 21/11/2015
 */

public class Hash {
	private final String tag = "Hash";
	private String input;

	public Hash(final String inputString) {
		input = inputString;
	}

	/*
	 * Return MD5 hash of input string
	 */

	public String getMD5Hash() {
		String temp = null;
		try {
			final MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(input.getBytes());
			final byte[] output = md.digest();
			temp = Convert.bytesToHex(output);
		} catch (Exception e) {
			EventLog.e(tag, "Error hashing string -> " + input);
			EventLog.e(tag, e);
		}
		return temp;
	}
}
