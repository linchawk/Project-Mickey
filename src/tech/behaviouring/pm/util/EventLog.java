package tech.behaviouring.pm.util;

import java.io.*;
import java.util.regex.*;
import java.util.*;
import java.text.*;

/*
 * Created by Mohan on 19/11/15.
 */

public class EventLog {
	private static File f;
	private static FileWriter outFile;
	private static PrintWriter out;
	private static String destinationFolder = "logs";

	public static void init() {
		try {
			String eventLoggerName = "Events.txt";
			File logFolder = new File(destinationFolder);
			/*
			 * Check and create logs folder if it doesn't exist
			 */
			if (!logFolder.exists())
				logFolder.mkdir();
			/*
			 * Check and create event log file if it doesn't exist
			 */
			f = new File(destinationFolder + "/" + eventLoggerName);
			if (!f.exists()) {
				f.createNewFile();
			}
			final Calendar c = Calendar.getInstance();
			resetOrAppend();
			out = new PrintWriter(outFile);
			final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			out.println("*********Events starting from " + sdf.format(c.getTime()) + "*********");
			out.flush();
			System.out.println("Event Log" + " - Log file initialized");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * If events are 15 days old, reset the log file else append new events to
	 * the log file
	 */

	public static void resetOrAppend() {
		try {
			final Scanner scan = new Scanner(f);
			if (scan.hasNextLine()) {
				final String line = scan.nextLine();
				final Pattern p = Pattern.compile("\\d+/\\d+/\\d+\\s+\\d+:\\d+:\\d+");
				final Matcher m = p.matcher(line);
				if (m.find()) {
					final Date startDate = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.ENGLISH).parse(m.group());
					final long dateDiff = System.currentTimeMillis() - startDate.getTime();
					if (dateDiff > 1296000000L) {
						outFile = new FileWriter(f);
					} else {
						outFile = new FileWriter(f, true);
					}
				} else {
					outFile = new FileWriter(f);
				}
			} else {
				outFile = new FileWriter(f);
			}
			scan.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void e(final String tag, final String eventTxt) {
		try {
			final Date now = new Date();
			out.println(String.valueOf(DateFormat.getDateTimeInstance(1, 1).format(now)) + "\t" + "<" + tag + ">");
			out.println(eventTxt);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void e(final String tag, Exception ex) {
		try {
			final Date now = new Date();
			out.println(String.valueOf(DateFormat.getDateTimeInstance(1, 1).format(now)) + "\t" + "<" + tag + ">");
			out.println(ex.getMessage());
			ex.printStackTrace(out);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void close() {
		try {
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
