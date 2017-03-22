package tech.behaviouring.pm.core.applogic.objects;

import java.util.Date;
import tech.behaviouring.pm.util.DataStructures.TimeOfDay;

/*
 * Created by Mohan on 22/11/2015
 */

public class DailyAttendance {

	private Date attDate;
	private StringBuilder attMorningComers;
	private int attMorningNComers;
	private StringBuilder attEveningComers;
	private int attEveningNComers;

	public DailyAttendance() {
		attDate = new Date();
		attMorningComers = new StringBuilder();
		attMorningNComers = 0;
		attEveningComers = new StringBuilder();
		attEveningNComers = 0;
	}

	public void setDate(Date date) {
		attDate = date;
	}

	public Date getDate() {
		return attDate;
	}

	public void registerAttendance(int memId, TimeOfDay time) {
		if (time == TimeOfDay.Morning) {
			attMorningComers.append(memId + ";");
			attMorningNComers++;
		} else {
			attEveningComers.append(memId + ";");
			attEveningNComers++;
		}
	}

	public String getAttendees(TimeOfDay time) {
		if (time == TimeOfDay.Morning)
			return attMorningComers.toString();
		else
			return attEveningComers.toString();
	}

	public void setAttendees(String attString, TimeOfDay time) {
		if (time == TimeOfDay.Morning)
			attMorningComers.append(attString);
		else
			attEveningComers.append(attString);
	}

	public int getNAttendees(TimeOfDay time) {
		if (time == TimeOfDay.Morning)
			return attMorningNComers;
		else
			return attEveningNComers;
	}

	public void setNAttendees(int nAttendees, TimeOfDay time) {
		if (time == TimeOfDay.Morning)
			attMorningNComers = nAttendees;
		else
			attEveningNComers = nAttendees;
	}

	public void printAttendanceRegister() {
		System.out.println("----Attendance Register----");
		System.out.println("Morning Comers: " + attMorningComers.toString() + " Count: " + attMorningNComers);
		System.out.println("Evening Comers: " + attEveningComers.toString() + " Count: " + attEveningNComers);
	}

}
