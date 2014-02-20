package net.stormdev.mario.rewards;

import java.io.Serializable;

public class RewardConfiguration implements Serializable {
	private static final long serialVersionUID = 1L;
	private double first, second, third;
	
	public RewardConfiguration(double first, double second, double third){
		this.first = first;
		this.second = second;
		this.third = third;
	}
	
	public double getFirstPlaceAmount(){
		return first;
	}
	
	public double getSecondPlaceAmount(){
		return second;
	}
	
	public double getThirdPlaceAmount(){
		return third;
	}
}
