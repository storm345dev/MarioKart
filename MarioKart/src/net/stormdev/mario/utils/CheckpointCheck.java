package net.stormdev.mario.utils;

public class CheckpointCheck {
	public int checkpoint = 0;
	public Boolean at = false;

	public CheckpointCheck(Boolean at, int checkpoint) {
		this.checkpoint = checkpoint;
		this.at = at;
	}
}
