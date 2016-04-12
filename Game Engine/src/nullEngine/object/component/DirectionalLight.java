package nullEngine.object.component;

import math.Vector4f;
import nullEngine.gl.Renderer;
import nullEngine.object.GameComponent;
import nullEngine.object.GameObject;

public class DirectionalLight extends GameComponent {
	private Vector4f lightColor;

	public DirectionalLight(Vector4f lightColor) {
		this.lightColor = lightColor;
	}

	@Override
	public void render(Renderer renderer, GameObject object) {
		renderer.add(this);
	}

	@Override
	public void update(float delta, GameObject object) {

	}

	public Vector4f getLightColor() {
		return lightColor;
	}

	public void setLightColor(Vector4f lightColor) {
		this.lightColor = lightColor;
	}
}
