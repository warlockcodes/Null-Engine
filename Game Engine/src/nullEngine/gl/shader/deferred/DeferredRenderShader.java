package nullEngine.gl.shader.deferred;


import math.Matrix4f;
import nullEngine.gl.shader.Shader;

public class DeferredRenderShader extends Shader {

	public static final DeferredRenderShader INSTANCE = new DeferredRenderShader();

	private int location_modelMatrix;
	private int location_mvp;

	private DeferredRenderShader() {
		super("default/deferred/deferred-render", "default/deferred/deferred-render");
	}

	@Override
	protected void bindAttributes() {
		bindAttribute(0, "inPosition");
		bindAttribute(1, "inTexCoords");
		bindAttribute(2, "inNormal");
		bindFragData(0, "outColor");
		bindFragData(1, "outPosition");
		bindFragData(2, "outNormal");
		bindFragData(3, "outSpecular");
	}

	@Override
	protected void getUniformLocations() {
		location_modelMatrix = getUniformLocation("modelMatrix");
		location_mvp = getUniformLocation("mvp");
		addUserTexture("diffuse");
		addUserFloat("reflectivity");
		addUserFloat("shineDamper");
	}

	public void loadModelMatrix(Matrix4f mat) {
		loadMat4(location_modelMatrix, mat);
	}

	public void loadMVP(Matrix4f mvp) {
		loadMat4(location_mvp, mvp);
	}
}