package net.stormdev.mariokartAddons;

public class KartAction {
	public Boolean freeze = false;
	public Boolean destroy = false;
	public Action action = Action.UNKNOWN;

	public Object[] args = new Object[] {};

	public KartAction(Boolean freeze, Boolean destroy, Action actionName,
			Object[] args) {
		this.freeze = freeze;
		this.destroy = destroy;
		this.action = actionName;
		this.args = args;
	}

	public Boolean getFreeze() {
		return this.freeze;
	}

	public Boolean getDestroy() {
		return this.destroy;
	}

	public Action getAction() {
		return this.action;
	}
}
