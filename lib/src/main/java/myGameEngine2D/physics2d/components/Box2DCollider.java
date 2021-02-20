package myGameEngine2D.physics2d.components;

import org.joml.Vector2f;

import myGameEngine2D.components.Component;
import myGameEngine2D.render.DebugDraw;

public class Box2DCollider extends Collider{
	private Vector2f halfSize = new Vector2f(1);
	private Vector2f origin = new Vector2f();
	

	public Vector2f getHalfSize() {
		return halfSize;
	}

	public void setHalfSize(Vector2f halfSize) {
		this.halfSize = halfSize;
	}

	public Vector2f getOrigin() {
		return this.origin;
	}

	@Override
	public void editorUpdate(float deltaTime) {
		Vector2f center = new Vector2f(this.gameObject.transform.position).add(this.offset);
		DebugDraw.addBox2D(center, this.halfSize, this.gameObject.transform.rotation);
	}
	
}
