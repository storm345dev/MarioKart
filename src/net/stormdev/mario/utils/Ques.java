package net.stormdev.mario.utils;

import java.util.Set;

import net.stormdev.mario.mariokart.main;

public class Ques {

	public Ques() {
	}

	public void setQue(String name, RaceQue toAdd) {
		main.plugin.ques.put(name, toAdd);
		return;
	}

	public void removeQue(String name) {
		Set<String> keys = getQues();
		for (String key : keys) {
			if (key.equalsIgnoreCase(name)) {
				name = key;
			}
		}
		RaceQue q = main.plugin.ques.get(name);
		if(q != null){
			q.clear();
		}
		main.plugin.ques.remove(name);
		return;
	}

	public RaceQue getQue(String name) {
		return main.plugin.ques.get(name);
	}

	public Boolean queExists(String name) {
		Set<String> keys = getQues();
		for (String key : keys) {
			if (key.equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	public Set<String> getQues() {
		return main.plugin.ques.keySet();
	}
}
