package net.stormdev.mario.races;

public class CheckpointCheck {
	public int checkpoint = 0;
	public Boolean at = false;

	public CheckpointCheck(Boolean at, int checkpoint) {
		this.checkpoint = checkpoint;
		this.at = at;
	}
}
