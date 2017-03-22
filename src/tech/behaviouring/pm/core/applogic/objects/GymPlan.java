package tech.behaviouring.pm.core.applogic.objects;

/*
 * Created by Mohan on 21/11/2015
 */

public class GymPlan {

	private int planId;
	private String planName;
	private int planFee1Month;
	private int planFee3Month;
	private int planFee6Month;
	private int planFee12Month;

	public GymPlan() {
		planName = "New Plan";
		planFee1Month = planFee3Month = planFee6Month = planFee12Month = 0;
	}

	public void setId(int id) {
		planId = id;
	}

	public int getId() {
		return planId;
	}

	public void setName(String name) {
		planName = name;
	}

	public String getName() {
		return planName;
	}

	public void setFee1Month(int fee1Month) {
		planFee1Month = fee1Month;
	}

	public int getFee1Month() {
		return planFee1Month;
	}

	public void setFee3Month(int fee3Month) {
		planFee3Month = fee3Month;
	}

	public int getFee3Month() {
		return planFee3Month;
	}

	public void setFee6Month(int fee6Month) {
		planFee6Month = fee6Month;
	}

	public int getFee6Month() {
		return planFee6Month;
	}

	public void setFee12Month(int fee12Month) {
		planFee12Month = fee12Month;
	}

	public int getFee12Month() {
		return planFee12Month;
	}

	public String toString() {
		return planName;
	}

}
