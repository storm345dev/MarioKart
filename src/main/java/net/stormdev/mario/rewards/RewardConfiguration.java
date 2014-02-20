package net.stormdev.mario.rewards;

public class RewardConfiguration {
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
