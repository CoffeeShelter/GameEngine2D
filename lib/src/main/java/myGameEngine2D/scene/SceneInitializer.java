package myGameEngine2D.scene;

public abstract class SceneInitializer {
	public abstract void init(Scene scene);	// 초기화
	public abstract void loadResources(Scene scene);	// 자원 로드
	public abstract void imgui();	// gui 로직
}
