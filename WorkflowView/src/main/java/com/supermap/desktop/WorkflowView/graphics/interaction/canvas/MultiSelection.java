package com.supermap.desktop.WorkflowView.graphics.interaction.canvas;

import com.supermap.desktop.Application;
import com.supermap.desktop.WorkflowView.graphics.GraphCanvas;
import com.supermap.desktop.WorkflowView.graphics.GraphicsUtil;
import com.supermap.desktop.WorkflowView.graphics.events.GraphSelectedChangedEvent;
import com.supermap.desktop.WorkflowView.graphics.graphs.IGraph;
import com.supermap.desktop.WorkflowView.graphics.graphs.decorators.SelectedDecoratorFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Created by highsad on 2017/3/2.
 */
public class MultiSelection extends Selection {
	private final static String DECORATOR_KEY = MultiSelection.class.getName();

	private final static Color DEFAULT_REGION_COLOR = GraphicsUtil.transparentColor(Color.decode("#63B8FF"), 60);
	private final static Color DEFAULT_BORDER_COLOR = Color.LIGHT_GRAY;

	private int borderWidth = 1;
	private Color regionColor = DEFAULT_REGION_COLOR;
	private Color borderColor = DEFAULT_BORDER_COLOR;
	private Point selectionStart = Selection.UNKOWN_POINT;
	private Rectangle dirtyRegion = new Rectangle(0, 0, 0, 0);
	private Rectangle selectionRegion = new Rectangle(0, 0, 0, 0);

	public MultiSelection(GraphCanvas canvas) {
		super(canvas);
	}

	public void setBorderWidth(int borderWidth) {
		this.borderWidth = borderWidth;
	}

	public void setRegionColor(Color regionColor) {
		this.regionColor = regionColor;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	public int getBorderWidth() {
		return borderWidth;
	}

	public Color getRegionColor() {
		return regionColor;
	}

	public Color getBorderColor() {
		return borderColor;
	}

	@Override
	public boolean isSelecting() {
		return this.selectionStart != Selection.UNKOWN_POINT;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			this.selectionStart = e.getPoint();
			fireCanvasActionStart();
		}
	}

	/***
	 *
	 * @param e
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			IGraph hit = getCanvas().findGraph(e.getPoint());

			if (hit != null) {
				selectItem(hit);
			} else {
				if (this.selectedItems.size() > 0) {
					cleanSelection();
					fireGraphSelectChanged(new GraphSelectedChangedEvent(getCanvas(), this));
				}
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		try {
			if (isSelecting()) {
				Point selectionEnd = e.getPoint();

				if (!selectionEnd.equals(Selection.UNKOWN_POINT) && !this.selectionStart.equals(selectionEnd)) {

					// get selection region.
					int x = Math.min(this.selectionStart.x, selectionEnd.x);
					int y = Math.min(this.selectionStart.y, selectionEnd.y);
					int width = Math.abs(this.selectionStart.x - selectionEnd.x);
					int height = Math.abs(this.selectionStart.y - selectionEnd.y);

					IGraph[] selected = getCanvas().findContainedGraphs(x, y, width, height);
					selectItems(selected);
				} else {
					if (this.selectedItems.size() > 0) {
						cleanSelection();
						fireGraphSelectChanged(new GraphSelectedChangedEvent(getCanvas(), this));
					}
				}
			} else {

				// 有可能鼠标在其他组件按下，然后移动到画布上释放，这时候就单纯的什么都不做
				// do nothing if mouse released without being pressed.for example,mouse pressed on other component and dragging to release on the canvas.
				this.selectionStart = Selection.UNKOWN_POINT;
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		} finally {
			getCanvas().repaint(this.dirtyRegion);
			resetStatus();
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e) && !this.selectionStart.equals(Selection.UNKOWN_POINT)) {

			// reset the dirty region.
			if (isDirty()) {
				getCanvas().repaint(this.dirtyRegion);
			}

			// 往四周扩展几个像素，以便能完整的绘制出边框
			// grow a few pixels the equivalent of borderWidth both horizontally and vertically
			// so that the selection border should be drawn completely.
			Point selectionEnd = e.getPoint();
			int x = Math.min(this.selectionStart.x, selectionEnd.x);
			int y = Math.min(this.selectionStart.y, selectionEnd.y);
			int width = Math.abs(this.selectionStart.x - selectionEnd.x);
			int height = Math.abs(this.selectionStart.y - selectionEnd.y);
			this.selectionRegion.setBounds(x, y, width, height);
			this.dirtyRegion.setBounds(x - this.borderWidth - 1, y - this.borderWidth - 1, width + (this.borderWidth + 1) * 2, height + (this.borderWidth + 1) * 2);
			getCanvas().repaint(this.dirtyRegion);
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void paint(Graphics graphics) {
		Graphics2D graphics2D = (Graphics2D) graphics;

		if (GraphicsUtil.isRegionValid(this.selectionRegion)) {
			graphics2D.setColor(this.regionColor);
			graphics2D.fill(this.selectionRegion);

			graphics2D.setColor(this.borderColor);
			BasicStroke stroke = new BasicStroke(this.borderWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			graphics2D.setStroke(stroke);
			graphics2D.draw(this.selectionRegion);
		}
	}

	@Override
	public void clean() {
		resetStatus();
		if (this.selectedItems.size() > 0) {
			cleanSelection();
			fireGraphSelectChanged(new GraphSelectedChangedEvent(getCanvas(), this));
		}
	}

	private void cleanSelection() {
		for (IGraph graph :
				this.selectedItems) {
			graph.removeDecorator(DECORATOR_KEY);
		}

		this.selectedItems.clear();
		getCanvas().repaint();
	}

	private void resetStatus() {
		this.selectionStart = Selection.UNKOWN_POINT;
		this.dirtyRegion.setBounds(0, 0, 0, 0);
		this.selectionRegion.setBounds(0, 0, 0, 0);
		fireCanvasActionStop();
	}

	private boolean isDirty() {
		return isSelecting() &&
				GraphicsUtil.isRegionValid(this.dirtyRegion);
	}

	/**
	 * select single item which specified by the parameter graph.
	 * null for select nothing.
	 *
	 * @param graph
	 */
	private void selectItem(IGraph graph) {
		if (graph != null) {

			// do nothing if the specified graph is the only graph which has been selected.
			// otherwise,clean this selected items, and then select this specified graph and also fire graph selected events.
			if (!this.selectedItems.contains(graph) || this.selectedItems.size() != 1) {
				cleanSelection();

				this.selectedItems.add(graph);
				graph.addDecorator(DECORATOR_KEY, SelectedDecoratorFactory.createDecorator(graph));
				fireGraphSelectChanged(new GraphSelectedChangedEvent(getCanvas(), this));
				getCanvas().repaint();
//				getCanvas().repaint(getCanvas().getCoordinateTransform().transform(graph.getDecorator(DECORATOR_KEY).getBounds()));
			}
		} else {
			if (this.selectedItems.size() > 0) {
				cleanSelection();
				fireGraphSelectChanged(new GraphSelectedChangedEvent(getCanvas(), this));
			}
		}
	}

	/**
	 * select multi-items which specified by the parameter graphs.
	 * null for select nothing.
	 *
	 * @param graphs
	 */
	private void selectItems(IGraph[] graphs) {
		if (graphs != null && graphs.length > 0) {
			cleanSelection();

			for (int i = 0; i < graphs.length; i++) {
				if (graphs[i] != null) {
					this.selectedItems.add(graphs[i]);
					graphs[i].addDecorator(DECORATOR_KEY, SelectedDecoratorFactory.createDecorator(graphs[i]));
				}
			}
			fireGraphSelectChanged(new GraphSelectedChangedEvent(getCanvas(), this));
		} else {
			if (this.selectedItems.size() > 0) {
				cleanSelection();
				fireGraphSelectChanged(new GraphSelectedChangedEvent(getCanvas(), this));
			}
		}

		getCanvas().repaint();
//		for (int i = 0, size = this.selectedItems.size(); i < size; i++) {
//			getCanvas().repaint(getCanvas().getCoordinateTransform().transform(this.selectedItems.get(i).getDecorator(DECORATOR_KEY).getBounds()));
//		}
	}

	@Override
	public void deselectItem(IGraph graph) {
		super.deselectItem(graph);
		graph.removeDecorator(DECORATOR_KEY);
	}

	@Override
	public void deselectItems(IGraph[] graphs) {
		if (graphs == null || graphs.length == 0) {
			return;
		}
		for (int i = 0; i < graphs.length; i++)
			deselectItem(graphs[i]);
	}


	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_A && e.isControlDown()) {
			IGraph[] graphs = canvas.getGraphStorage().getGraphs();
			selectItems(graphs);
		}
	}
}
