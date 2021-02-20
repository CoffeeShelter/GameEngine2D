package myGameEngine2D.physics2d.components;

import org.joml.Vector2f;

import myGameEngine2D.components.Component;

public abstract class Collider extends Component{
	protected Vector2f offset = new Vector2f();
	
	public Vector2f getOffset() {
		return this.offset;
	}
}
