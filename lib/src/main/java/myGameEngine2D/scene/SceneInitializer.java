package myGameEngine2D.scene;

public abstract class SceneInitializer {
	public abstract void init(Scene scene);	// �ʱ�ȭ
	public abstract void loadResources(Scene scene);	// �ڿ� �ε�
	public abstract void imgui();	// gui ����
}
