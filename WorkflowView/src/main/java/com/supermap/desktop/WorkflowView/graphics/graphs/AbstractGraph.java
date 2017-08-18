package com.supermap.desktop.WorkflowView.graphics.graphs;

import com.supermap.desktop.WorkflowView.graphics.GraphCanvas;
import com.supermap.desktop.WorkflowView.graphics.GraphicsUtil;
import com.supermap.desktop.WorkflowView.graphics.events.GraphBoundsChangedEvent;
import com.supermap.desktop.WorkflowView.graphics.events.GraphBoundsChangedListener;
import com.supermap.desktop.WorkflowView.graphics.graphs.decorators.IDecorator;
import com.supermap.desktop.utilities.StringUtilities;

import javax.swing.event.EventListenerList;
import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by highsad on 2017/1/20.
 */
public abstract class AbstractGraph implements IGraph {
	private GraphCanvas canvas;
	protected Shape shape;
	protected ConcurrentHashMap<String, IDecorator> decorators = new ConcurrentHashMap<>();
	private java.util.List<IDecorator> sortedDeorators = new ArrayList<>();
	private EventListenerList listenerList = new EventListenerList();

	private AbstractGraph() {
		// 反射用的
	}

	public AbstractGraph(GraphCanvas canvas, Shape shape) {
		// canvas的初始化在添加到画布的时候由画布设值
		this.canvas = canvas;
		this.shape = shape;
	}

	public Shape getShape() {
		return this.shape;
	}

	@Override
	public Rectangle getBounds() {
		if (this.shape != null) {
			return this.shape.getBounds();
		} else {
			return null;
		}
	}

	@Override
	public Rectangle getTotalBounds() {
		Rectangle totalBounds = getBounds();
		if (!GraphicsUtil.isRegionValid(totalBounds)) {
			return null;
		}

		for (String key :
				this.decorators.keySet()) {
			IDecorator decorator = this.decorators.get(key);
			Rectangle decoratorBounds = decorator.getBounds();

			if (GraphicsUtil.isRegionValid(decoratorBounds)) {
				totalBounds = totalBounds.union(decoratorBounds);
			}
		}
		return totalBounds;
	}

	@Override
	public Point getLocation() {
		if (this.shape != null) {
			return this.shape.getBounds().getLocation();
		} else {
			return null;
		}
	}

	@Override
	public Point getCenter() {
		if (this.shape != null) {
			double x = this.shape.getBounds().getX();
			double y = this.shape.getBounds().getY();
			double width = this.shape.getBounds().getWidth();
			double height = this.shape.getBounds().getHeight();
			Point center = new Point();
			center.setLocation(x + width / 2, y + height / 2);
			return center;
		} else {
			return null;
		}
	}

	@Override
	public int getWidth() {
		if (this.shape != null) {
			return this.shape.getBounds().width;
		} else {
			return -1;
		}
	}

	@Override
	public int getHeight() {
		if (this.shape != null) {
			return this.shape.getBounds().height;
		} else {
			return -1;
		}
	}

	@Override
	public final void setLocation(Point point) {
		Point oldLocation = getShape().getBounds().getLocation();

		if (!oldLocation.equals(point)) {
			applyLocation(point);
			fireGraphBoundsChanged(new GraphBoundsChangedEvent(this, oldLocation, point));
		}
	}

	protected abstract void applyLocation(Point point);

	@Override
	public final void setSize(int width, int height) {
		int oldWidth = getShape().getBounds().width;
		int oldHeight = getShape().getBounds().height;

		if (oldWidth != width || oldHeight != height) {
			applySize(width, height);
			fireGraphBoundsChanged(new GraphBoundsChangedEvent(this, oldWidth, width, oldHeight, height));
		}
	}

	protected abstract void applySize(int width, int height);

	@Override
	public boolean contains(Point point) {
		boolean result = this.shape.contains(point);

		if (!result) {
			for (String key :
					this.decorators.keySet()) {
				result = this.decorators.get(key).contains(point);
				if (result) {
					break;
				}
			}
		}
		return result;
	}

	@Override
	public void setCanvas(GraphCanvas canvas) {
		this.canvas = canvas;
	}

	public GraphCanvas getCanvas() {
		return this.canvas;
	}

	@Override
	public IDecorator[] getDecorators() {
		Set<Map.Entry<String, IDecorator>> entrySet = this.decorators.entrySet();
		IDecorator[] result = new IDecorator[entrySet.size()];
		entrySet.toArray(result);
		return result;
	}

	@Override
	public int getDecoratorSize() {
		return this.decorators.size();
	}

	@Override
	public IDecorator getDecorator(String key) {
		return this.decorators.get(key);
	}

	@Override
	public void addDecorator(String key, IDecorator decorator) {
		if (StringUtilities.isNullOrEmpty(key) || decorator == null) {
			return;
		}

		if (this.decorators.containsKey(key)) {
			return;
		}

		this.decorators.put(key, decorator);
		if (!this.sortedDeorators.contains(decorator)) {
			this.sortedDeorators.add(decorator);
		}
		decorator.decorate(this);
		Collections.sort(this.sortedDeorators, new Comparator<IDecorator>() {
			@Override
			public int compare(IDecorator o1, IDecorator o2) {
				return o1.getPriority() - o2.getPriority();
			}
		});
	}

	@Override
	public void removeDecorator(String key) {
		if (this.decorators.containsKey(key)) {
			this.decorators.get(key).undecorate();
			this.sortedDeorators.remove(this.decorators.get(key));
			this.decorators.remove(key);
		}
	}

	@Override
	public void removeDecorator(IDecorator decorator) {
		if (decorator == null) {
			return;
		}

		for (String key :
				this.decorators.keySet()) {
			if (this.decorators.get(key) == decorator) {
				this.decorators.remove(key);
				break;
			}
		}
		decorator.undecorate();
		this.sortedDeorators.remove(decorator);
	}

	@Override
	public boolean isDecoratedBy(String key) {
		return this.decorators.containsKey(key);
	}

	@Override
	public boolean isDecoratedBy(IDecorator decorator) {
		return decorator != null && this.decorators.contains(decorator);
	}

	@Override
	public void addGraphBoundsChangedListener(GraphBoundsChangedListener listener) {
		this.listenerList.add(GraphBoundsChangedListener.class, listener);
	}

	@Override
	public void removeGraghBoundsChangedListener(GraphBoundsChangedListener listener) {
		this.listenerList.remove(GraphBoundsChangedListener.class, listener);
	}

	protected void fireGraphBoundsChanged(GraphBoundsChangedEvent e) {
		Object[] listeners = listenerList.getListenerList();

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == GraphBoundsChangedListener.class) {
				((GraphBoundsChangedListener) listeners[i + 1]).graghBoundsChanged(e);
			}
		}
	}

	@Override
	public void paint(Graphics g) {
		onPaint(g);

		for (int i = this.sortedDeorators.size() - 1; i >= 0; i--) {
			this.sortedDeorators.get(i).paint(g);
		}
	}

	protected abstract void onPaint(Graphics g);
}
