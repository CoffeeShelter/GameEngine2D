package myGameEngine2D.components;

public class FontRenderComponent extends Component{
	
	@Override
	public void start() {
		if(gameObject.getComponent(SpriteComponent.class)!=null) {
			System.out.println("FontRenderComponent Starting!");
		}
	}
	
	@Override
	public void update(float deltaTime) {
		// TODO Auto-generated method stub
		
	}

}
