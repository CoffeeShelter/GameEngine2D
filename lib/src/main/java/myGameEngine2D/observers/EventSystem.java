package myGameEngine2D.observers;

import java.util.ArrayList;
import java.util.List;

import myGameEngine2D.game.GameObject;
import myGameEngine2D.observers.events.Event;

public class EventSystem {
	private static List<Observer> observers = new ArrayList<>();
	
	public static void addObserver(Observer observer) {
		observers.add(observer);
	}
	
	public static void notify(GameObject obj, Event event) {
		for(Observer observer : observers) {
			observer.onNotify(obj, event);
		}
	}
}
