package tech.behaviouring.pm.core.applogic.objects;

import java.util.Date;

/*
 * Created by Mohan on 22/11/2015
 */

public class MemberDetails {

	private int memId;
	/* Hex string of fingerprints */
	private Fingerprint fingerprint;
	private String memName;
	private String memPicLocation;
	private Date memDob;
	private String memAddr;
	private String memPh;
	private String memEmail;
	private int memHeightCm;
	private int memWeightKg;
	private String memBloodGroup;
	private Date memDateJoined;
	private int memPlanId;
	private int memFeePaidForNMonth;
	private int firstPaymentAmount;
	private Date memNextRenewal;
	private int memIsVerified; /* Implies is phone number verified? */
	private Date memLastSeenOn;
	private int nRemainderSmsSent;

	public MemberDetails() {
		fingerprint = null;
		firstPaymentAmount = 0;
	}

	public void setId(int id) {
		memId = id;
		/*
		 * For integrity we have to set the member id in fingerprint object as
		 * well
		 */
		if (fingerprint != null)
			fingerprint.setMemberId(id);
	}

	public int getId() {
		return memId;
	}

	public void setFingerprint(Fingerprint fp) {
		fingerprint = fp;
	}

	public Fingerprint getFingerprint() {
		return fingerprint;
	}

	public void setName(String name) {
		memName = name;
	}

	public String getName() {
		return memName;
	}

	public void setPicLocation(String location) {
		memPicLocation = location;
	}

	public String getPicLocation() {
		return memPicLocation;
	}

	public void setDob(Date dob) {
		memDob = dob;
	}

	public Date getDob() {
		return memDob;
	}

	public void setAddr(String addr) {
		memAddr = addr;
	}

	public String getAddr() {
		return memAddr;
	}

	public void setPh(String phone) {
		memPh = phone;
	}

	public String getPh() {
		return memPh;
	}

	public void setEmail(String email) {
		memEmail = email;
	}

	public String getEmail() {
		return memEmail;
	}

	public void setHeightCm(int heightInCm) {
		memHeightCm = heightInCm;
	}

	public int getHeightCm() {
		return memHeightCm;
	}

	public void setWeightKg(int weightInKg) {
		memWeightKg = weightInKg;
	}

	public int getWeightKg() {
		return memWeightKg;
	}

	public void setBloodGroup(String bloodGroup) {
		memBloodGroup = bloodGroup;
	}

	public String getBloodGroup() {
		return memBloodGroup;
	}

	public void setDateJoined(Date dateJoined) {
		memDateJoined = dateJoined;
	}

	public Date getDateJoined() {
		return memDateJoined;
	}

	public void setPlanId(int planId) {
		memPlanId = planId;
	}

	public int getPlanId() {
		return memPlanId;
	}

	public void setFeePaidForNMonth(int nMonth) {
		memFeePaidForNMonth = nMonth;
	}

	public int getFeePaidForNMonth() {
		return memFeePaidForNMonth;
	}

	public void setFirstPaymentAmount(int amount) {
		firstPaymentAmount = amount;
	}

	public int getFirstPaymentAmount() {
		return firstPaymentAmount;
	}

	public void setNextRenewal(Date nextRenewal) {
		memNextRenewal = nextRenewal;
	}

	public Date getNextRenewal() {
		return memNextRenewal;
	}

	public void setIsVerified(int isVerified) {
		memIsVerified = isVerified;
	}

	public int getIsVerified() {
		return memIsVerified;
	}

	public void setLastSeenOn(Date lastSeenDate) {
		memLastSeenOn = lastSeenDate;
	}

	public Date getLastSeenOn() {
		return memLastSeenOn;
	}

	public void setNRemainderSmsSent(int nRemainders) {
		nRemainderSmsSent = nRemainders;
	}

	public int getNRemainderSmsSent() {
		return nRemainderSmsSent;
	}
}
