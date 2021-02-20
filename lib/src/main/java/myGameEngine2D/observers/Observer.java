package myGameEngine2D.observers;

import myGameEngine2D.game.GameObject;
import myGameEngine2D.observers.events.Event;

public interface Observer {
	void onNotify(GameObject object, Event event);
}
