package com.manu.dynasty.template;

public class HuoDong {
	private int id;
	private String title;
	private String desc;
	private String awardDesc;
	private int icon;
	public int getHuoDongStatus() {
		return HuoDongStatus;
	}

	public void setHuoDongStatus(int huoDongStatus) {
		HuoDongStatus = huoDongStatus;
	}

	private int type;
	private int buttonColor;
	private String buttonTitle;
	private String buttonTitleComplete;
	private int buttonCompleteTouch;
	private int HuoDongStatus;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getAwardDesc() {
		return awardDesc;
	}

	public void setAwardDesc(String awardDesc) {
		this.awardDesc = awardDesc;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getButtonColor() {
		return buttonColor;
	}

	public void setButtonColor(int buttonColor) {
		this.buttonColor = buttonColor;
	}

	public String getButtonTitle() {
		return buttonTitle;
	}

	public void setButtonTitle(String buttonTitle) {
		this.buttonTitle = buttonTitle;
	}

	public String getButtonTitleComplete() {
		return buttonTitleComplete;
	}

	public void setButtonTitleComplete(String buttonTitleComplete) {
		this.buttonTitleComplete = buttonTitleComplete;
	}

	public int getButtonCompleteTouch() {
		return buttonCompleteTouch;
	}

	public void setButtonCompleteTouch(int buttonCompleteTouch) {
		this.buttonCompleteTouch = buttonCompleteTouch;
	}
}
