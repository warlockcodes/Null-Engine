package nullEngine.object;

import com.sun.istack.internal.Nullable;
import math.Matrix4f;
import nullEngine.control.layer.Layer;
import nullEngine.control.physics.PhysicsEngine;
import nullEngine.graphics.renderer.Renderer;
import nullEngine.input.CharEvent;
import nullEngine.input.EventListener;
import nullEngine.input.KeyEvent;
import nullEngine.input.MouseEvent;
import nullEngine.input.NotificationEvent;
import nullEngine.input.PostResizeEvent;
import util.ListOperator;
import nullEngine.util.logs.Logs;
import util.BitFieldInt;
import util.ListOperatorPool;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Represents an object in the game
 */
public class GameObject implements EventListener {

	public static final ListOperatorPool<GameObject> GAME_OBJECT_LIST_OPERATORS = new ListOperatorPool<>();
	public static final ListOperatorPool<GameComponent> GAME_COMPONENT_LIST_OPERATORS = new ListOperatorPool<>();

	private List<GameObject> children = new ArrayList<>();
	private List<GameComponent> components = new ArrayList<>();

	private Queue<ListOperator<GameObject>> childrenOps = new ArrayDeque<>();
	private Queue<ListOperator<GameComponent>> componentOps = new ArrayDeque<>();

	private List<GameObject> renderChildren = new ArrayList<>();
	private List<GameComponent> renderComponents = new ArrayList<>();

	private Transform transform = new Transform(this);
	private Matrix4f postUpdateMatrix = new Matrix4f();
	private Matrix4f preRenderMatrix = new Matrix4f();
	private boolean renderMatrixValid = false;
	private boolean renderMatrixWaiting = false;

	private boolean renderChildrenVaild = false;
	private boolean renderComponentsValid = false;
	private List<GameObject> postUpdateChildren = new ArrayList<>();
	private List<GameComponent> postUpdateComponents = new ArrayList<>();

	private GameObject parent;

	private void onAdded(GameObject parent) {
		this.parent = parent;
		transform.setParent(parent.getTransform());
	}

	/**
	 * Add a child GameObject to this
	 *
	 * @param child The GameObject to add
	 */
	public void addChild(GameObject child) {
		if (child.getParent() == this) {
			Logs.w("Trying to add a child that is already on this object");
		} else if (child.getParent() != null) {
			Logs.f(new IllegalArgumentException("GameObject is already a child of another object"));
		} else if (child instanceof RootObject) {
			Logs.f(new IllegalArgumentException("Canno add a root object"));
		}
		childrenOps.add(GAME_OBJECT_LIST_OPERATORS.add(child));
		child.onAdded(this);
	}

	/**
	 * Add a component to this object
	 *
	 * @param component the component to add
	 */
	public void addComponent(GameComponent component) {
		if (component.getObject() == this) {
			Logs.w("Trying to add a component that is already on this object");
		} else if (component.getObject() != null) {
			Logs.f(new IllegalArgumentException("GameComponent is already attached to an object"));
		}
		componentOps.add(GAME_COMPONENT_LIST_OPERATORS.add(component));
		component.onAdded(this);
	}

	/**
	 * Render this object
	 *
	 * @param renderer The renderer that is rendering this object
	 * @param flags    The render flags
	 * @see Renderer
	 */
	public void render(Renderer renderer, BitFieldInt flags) {
		for (GameComponent component : renderComponents) {
			if (component.isEnabled())
				component.render(renderer, this, flags);
		}

		for (GameObject child : renderChildren) {
			child.render(renderer, flags);
		}
	}

	public <T extends GameComponent> T getComponent(Class<T> clazz, boolean allowSubclasses) {
		if (allowSubclasses) {
			for (GameComponent component : components)
				if (clazz.isAssignableFrom(component.getClass()))
					return (T) component;
			return null;
		}else {
			for (GameComponent component : components)
				if (component.getClass() == clazz)
					return (T) component;
			return null;
		}
	}

	public <T extends GameComponent> T getComponent(Class<T> clazz) {
		return getComponent(clazz, false);
	}

	public <T extends GameComponent> T[] getAllComponents(Class<T> clazz, boolean allowSubclasses) {
		if (allowSubclasses) {
			return components.stream().filter(component -> clazz.isAssignableFrom(component.getClass()))
					.toArray(length -> (T[]) Array.newInstance(clazz, length));
		} else {
			return components.stream().filter(component -> component.getClass() == clazz)
					.toArray(length -> (T[]) Array.newInstance(clazz, length));
		}
	}

	public <T extends GameComponent> T[] getAllComponents(Class<T> clazz) {
		return getAllComponents(clazz, false);
	}

	/**
	 * Update this object
	 *
	 * @param delta The time since it was last updated
	 */
	public void update(@Nullable PhysicsEngine physics, double delta) {
		for (GameComponent component : components) {
			if (component.isEnabled())
				component.update(physics, this, delta);
		}
		for (GameObject child : children) {
			child.update(physics, delta);
		}
	}

	/**
	 * Update the matrix for multithreading synchronization
	 * <strong>Do not run expensive code here as it is intended for copying data only, otherwise the performance will be bad</strong>
	 */
	protected void postUpdate() {
		{
			ListOperator<GameObject> op;
			while ((op = childrenOps.poll()) != null) {
				if (op.run(children))
					renderChildrenVaild = false;
				GAME_OBJECT_LIST_OPERATORS.delete(op);
			}

			if (!renderChildrenVaild) {
				postUpdateChildren.clear();
				postUpdateChildren.addAll(children);
			}
		}
		{
			ListOperator<GameComponent> op;
			while ((op = componentOps.poll()) != null) {
				if (op.run(components))
					renderComponentsValid = false;
				GAME_COMPONENT_LIST_OPERATORS.delete(op);
			}

			if (!renderComponentsValid) {
				postUpdateComponents.clear();
				postUpdateComponents.addAll(components);
			}
		}

		for (GameComponent component : components)
			if (component.isEnabled())
				component.postUpdate();
		for (GameObject child : children)
			child.postUpdate();

		if (!renderMatrixValid) {
			postUpdateMatrix.set(transform.getMatrix());
			renderMatrixWaiting = true;
		}
	}

	/**
	 * Update the rendering data for multithreading synchronization
	 * <strong>Do not run expensive code here as it is intended for copying data only, otherwise the performance will be bad</strong>
	 */
	protected void preRender() {
		if (!renderChildrenVaild) {
			renderChildren.clear();
			renderChildren.addAll(postUpdateChildren);
			renderChildrenVaild = true;
		}

		if (!renderComponentsValid) {
			renderComponents.clear();
			renderComponents.addAll(postUpdateComponents);
			renderComponentsValid = true;
		}

		for (GameComponent component : renderComponents)
			if (component.isEnabled())
				component.preRender();
		for (GameObject child : renderChildren)
			child.preRender();
		if (!renderMatrixValid && renderMatrixWaiting) {
			preRenderMatrix.set(postUpdateMatrix);
			renderMatrixValid = true;
			renderMatrixWaiting = false;
		}
	}

	/**
	 * Get the transform of this object
	 *
	 * @return The transform
	 */
	public Transform getTransform() {
		return transform;
	}

	/**
	 * Get the layer this object belongs to
	 *
	 * @return The layer
	 */
	public Layer getLayer() {
		return parent.getLayer();
	}

	/**
	 * Get the objects children
	 *
	 * @return The children
	 */
	public List<GameObject> getChildren() {
		return children;
	}

	/**
	 * Get the objects components
	 *
	 * @return The components
	 */
	public List<GameComponent> getComponents() {
		return components;
	}

	/**
	 * Get the objects parent object
	 *
	 * @return The parent
	 */
	public GameObject getParent() {
		return parent;
	}

	/**
	 * Get the matrix used for rendering
	 *
	 * @return The matrix
	 */
	public Matrix4f getRenderMatrix() {
		return preRenderMatrix;
	}

	/**
	 * @param event
	 * @return
	 * @see nullEngine.input.EventHandler
	 */
	@Override
	public boolean keyRepeated(KeyEvent event) {
		for (GameComponent component : components)
			if (component.isEnabled() && component.keyRepeated(event))
				return true;
		for (GameObject child : children)
			if (child.keyRepeated(event))
				return true;
		return false;
	}

	/**
	 * @param event
	 * @return
	 * @see nullEngine.input.EventHandler
	 */
	@Override
	public boolean keyPressed(KeyEvent event) {
		for (GameComponent component : components)
			if (component.isEnabled() && component.keyPressed(event))
				return true;
		for (GameObject child : children)
			if (child.keyPressed(event))
				return true;
		return false;
	}

	/**
	 * @param event
	 * @return
	 * @see nullEngine.input.EventHandler
	 */
	@Override
	public boolean keyReleased(KeyEvent event) {
		for (GameComponent component : components)
			if (component.isEnabled() && component.keyReleased(event))
				return true;
		for (GameObject child : children)
			if (child.keyReleased(event))
				return true;
		return false;
	}

	/**
	 * @param event
	 * @return
	 * @see nullEngine.input.EventHandler
	 */
	@Override
	public boolean mousePressed(MouseEvent event) {
		for (GameComponent component : components)
			if (component.isEnabled() && component.mousePressed(event))
				return true;
		for (GameObject child : children)
			if (child.mousePressed(event))
				return true;
		return false;
	}

	/**
	 * @param event
	 * @return
	 * @see nullEngine.input.EventHandler
	 */
	@Override
	public boolean mouseReleased(MouseEvent event) {
		for (GameComponent component : components)
			if (component.isEnabled() && component.mouseReleased(event))
				return true;
		for (GameObject child : children)
			if (child.mouseReleased(event))
				return true;
		return false;
	}

	/**
	 * @param event
	 * @return
	 * @see nullEngine.input.EventHandler
	 */
	@Override
	public boolean mouseScrolled(MouseEvent event) {
		for (GameComponent component : components)
			if (component.isEnabled() && component.mouseScrolled(event))
				return true;
		for (GameObject child : children)
			if (child.mouseScrolled(event))
				return true;
		return false;
	}

	/**
	 * @param event
	 * @return
	 * @see nullEngine.input.EventHandler
	 */
	@Override
	public boolean mouseMoved(MouseEvent event) {
		for (GameComponent component : components)
			if (component.isEnabled() && component.mouseMoved(event))
				return true;
		for (GameObject child : children)
			if (child.mouseMoved(event))
				return true;
		return false;
	}

	/**
	 * @param event
	 * @return
	 * @see nullEngine.input.EventHandler
	 */
	@Override
	public boolean charTyped(CharEvent event) {
		for (GameComponent component : components)
			if (component.isEnabled() && component.charTyped(event))
				return true;
		for (GameObject child : children)
			if (child.charTyped(event))
				return true;
		return false;
	}

	/**
	 * @param event
	 * @see nullEngine.input.EventListener
	 */
	@Override
	public void notified(NotificationEvent event) {

	}

	/**
	 * @param event
	 * @see nullEngine.input.EventListener
	 */
	@Override
	public void postResize(PostResizeEvent event) {
		for (GameComponent component : renderComponents)
			component.postResize(event);
		for (GameObject child : renderChildren)
			child.postResize(event);
	}

	/**
	 * @see nullEngine.input.EventListener
	 */
	@Override
	public void preResize() {
		for (GameComponent component : renderComponents)
			if (component.isEnabled())
			component.preResize();
		for (GameObject child : renderChildren)
			child.preResize();
	}

	public void matrixUpdated() {
		renderMatrixValid = false;
		for (GameComponent component : components)
			component.matrixUpdated();
		for (GameObject child : children)
			child.matrixUpdated();
	}
}
