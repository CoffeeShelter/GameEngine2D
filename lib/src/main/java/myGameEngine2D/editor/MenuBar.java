package myGameEngine2D.editor;

import imgui.ImGui;
import myGameEngine2D.observers.EventSystem;
import myGameEngine2D.observers.events.Event;
import myGameEngine2D.observers.events.EventType;

public class MenuBar {
	public void imgui() {
		ImGui.beginMainMenuBar();
		
		if(ImGui.beginMenu("File")) {
			if(ImGui.menuItem("Save", "Ctrl+S")) {
				EventSystem.notify(null, new Event(EventType.SaveLevel));
			}
			
			if(ImGui.menuItem("Load","Ctrl+O")) {
				EventSystem.notify(null, new Event(EventType.LoadLevel));
			}
			ImGui.endMenu();
		}

		ImGui.endMainMenuBar();
	}
}
