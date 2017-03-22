package tech.behaviouring.pm.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import CogentBioSDK.CgtBioSdkApi;

/*
 * Created by Mohan on 21/11/2015
 */

public class Convert {
	/*
	 * Convert a byte array into a hex string
	 */
	public static String bytesToHex(final byte[] b) {
		final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		final StringBuffer buf = new StringBuffer();
		for (int j = 0; j < b.length; ++j) {
			buf.append(hexDigit[b[j] >> 4 & 0xF]);
			buf.append(hexDigit[b[j] & 0xF]);
		}
		return buf.toString();
	}

	/*
	 * Convert a hex string into a byte array
	 */
	public static byte[] hexToBytes(String b) {
		byte bytes[] = new byte[b.length() / 2];
		for (int i = 0; i < b.length(); i = i + 2) {
			int high = hexToBin(b.charAt(i));
			int low = hexToBin(b.charAt(i + 1));
			bytes[i / 2] = (byte) (high * 16 + low);
		}
		return bytes;
	}

	private static int hexToBin(char ch) {
		if ('0' <= ch && ch <= '9')
			return ch - '0';
		if ('a' <= ch && ch <= 'f')
			return ch - 'a' + 10;
		return -1;
	}

	/*
	 * Extracts the image data buffer from a BMP file byte array
	 */

	public static byte[] bmpToRaw(byte[] bmpBytes) {
		byte[] rawBytes = null;
		if (bmpBytes != null) {
			try {
				BufferedImage bufferedImage;
				bufferedImage = ImageIO.read(new ByteArrayInputStream(bmpBytes));

				WritableRaster raster = bufferedImage.getRaster();
				DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
				rawBytes = buffer.getData();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return rawBytes;
	}

	/*
	 * Extracts fingerprint template from image data buffer
	 */

	public static byte[] rawToISO(byte[] rawBytes, int height, int width, int resolution_h, int resolution_v,
			int fingerPosition) {
		byte[] isoBytes = null;

		if (rawBytes != null) {
			isoBytes = CgtBioSdkApi.extractTemplate(rawBytes, height, width, resolution_h, resolution_v,
					fingerPosition);

		}
		return isoBytes;
	}

	/*
	 * Convert Java date object into sql date object
	 */

	public static java.sql.Date javaDateToSqlDate(java.util.Date date) {
		return new java.sql.Date(date.getTime());
	}

	/*
	 * Convert sql date object into Java date object
	 */

	public static java.util.Date sqlDateToJavaDate(java.sql.Date date) {
		return date;
	}
}
