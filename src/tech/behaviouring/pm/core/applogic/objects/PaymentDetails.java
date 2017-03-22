package tech.behaviouring.pm.core.applogic.objects;

import java.util.Date;

/*
 * Created by Mohan on 22/11/2015
 */

public class PaymentDetails {

	private int memId;
	private int payId;
	private int payAmount;
	private Date payDate;

	public PaymentDetails() {
		/*
		 * dum dum dum
		 */
	}

	public void setMemId(int id) {
		memId = id;
	}

	public int getMemId() {
		return memId;
	}

	public void setPayId(int id) {
		payId = id;
	}

	public int getPayId() {
		return payId;
	}

	public void setPayAmount(int amount) {
		payAmount = amount;
	}

	public int getPayAmount() {
		return payAmount;
	}

	public void setPayDate(Date date) {
		payDate = date;
	}

	public Date getPayDate() {
		return payDate;
	}
}
