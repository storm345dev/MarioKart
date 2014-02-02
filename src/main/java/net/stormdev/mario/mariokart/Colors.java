package net.stormdev.mario.mariokart;

public class Colors {
	private String success = "";
	private String error = "";
	private String info = "";
	private String title = "";
	private String tp = "";

	public Colors(String success, String error, String info, String title,
			String tp) {
		this.success = MarioKart.colorise(success);
		this.error = MarioKart.colorise(error);
		this.info = MarioKart.colorise(info);
		this.title = MarioKart.colorise(title);
		this.tp = MarioKart.colorise(tp);
	}

	public String getSuccess() {
		return this.success;
	}

	public String getError() {
		return this.error;
	}

	public String getInfo() {
		return this.info;
	}

	public String getTitle() {
		return this.title;
	}

	public String getTp() {
		return this.tp;
	}
}
