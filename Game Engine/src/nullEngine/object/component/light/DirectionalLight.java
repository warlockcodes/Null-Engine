package nullEngine.object.component.light;

import math.Vector4f;
import nullEngine.control.Layer;
import nullEngine.gl.renderer.Renderer;
import nullEngine.object.GameComponent;
import nullEngine.object.GameObject;
import util.BitFieldInt;

public class DirectionalLight extends GameComponent {
	private Vector4f lightColor;
	private Vector4f direction;

	public DirectionalLight(Vector4f lightColor, Vector4f direction) {
		this.lightColor = lightColor;
		this.direction = direction.normalize(null);
	}

	@Override
	public void render(Renderer renderer, GameObject object, BitFieldInt flags) {
		if (flags.get(Layer.DEFERRED_RENDER_BIT))
			renderer.add(this);
	}

	@Override
	public void update(double delta, GameObject object) {

	}

	public Vector4f getLightColor() {
		return lightColor;
	}

	public void setLightColor(Vector4f lightColor) {
		this.lightColor = lightColor;
	}

	public Vector4f getDirection() {
		return direction;
	}

	public void setDirection(Vector4f direction) {
		this.direction = direction.normalize(null);
	}
}