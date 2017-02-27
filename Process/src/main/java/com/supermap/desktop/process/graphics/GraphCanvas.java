package com.supermap.desktop.process.graphics;

import com.supermap.desktop.process.graphics.graphs.*;
import com.supermap.desktop.process.graphics.graphs.decorator.AbstractDecorator;
import com.supermap.desktop.process.graphics.graphs.decorator.HotDecorator;
import com.supermap.desktop.process.graphics.graphs.decorator.PreviewDecorator;
import com.supermap.desktop.process.graphics.graphs.decorator.SelectedDecorator;
import com.supermap.desktop.process.graphics.painter.DefaultGraphPainter;
import com.supermap.desktop.process.graphics.painter.DefaultGraphPainterFactory;
import com.supermap.desktop.process.graphics.painter.IGraphPainter;
import com.supermap.desktop.process.graphics.painter.IGraphPainterFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

/**
 * Created by highsad on 2017/1/17.
 * 画布单位1默认与屏幕像素1相等，画布缩放之后之后的画布单位1则与屏幕像素 1*scale 相等
 * 使用多套数据结构来进行元素的存储，比如是用 List 来进行元素的存储，使用四叉树来做空间关系的存储，使用暂未定的某种结构存储连接关系等
 */
public class GraphCanvas extends JComponent implements MouseListener, MouseMotionListener, MouseWheelListener {
	public final static Color DEFAULT_BACKGROUNDCOLOR = new Color(11579568);
	public final static Color DEFAULT_CANVAS_COLOR = new Color(255, 255, 255);
	public final static Color GRID_MINOR_COLOR = new Color(15461355);
	public final static Color GRID_MAJOR_COLOR = new Color(13290186);

	private IGraphPainterFactory painterFactory = new DefaultGraphPainterFactory();
	private AbstractDecorator hotDecorator = new HotDecorator(this);
	private AbstractDecorator selectedDecorator = new SelectedDecorator(this); // 目前还没有支持多选，就先这样用单例修饰
	private AbstractDecorator previewDecorator = new PreviewDecorator(this);
	private IGraph previewGraph;

	private QuadTree<IGraph> graphQuadTree = new QuadTree<>();
	private ArrayList<LineGraph> lines = new ArrayList<>();
	private double scale = 1.0;
	private IGraph hotGraph;
	private IGraph selectedGraph; // Decorator 的类结构还需要优化，现在接收 AbstractGraph 会导致 hot selected preview Decorator 扩展不易

	private IGraph draggedGraph;
	private Point dragBegin;
	private Point dragCenter;
	private LineGraph line;

	public static void main(String[] args) {
		final JFrame frame = new JFrame();
		frame.setSize(1000, 650);
		final GraphCanvas canvas = new GraphCanvas();


		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(canvas, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.NORTH);

		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		JButton button = new JButton("Rectangle");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RectangleGraph graph = new RectangleGraph(canvas);
				graph.setSize(200, 80);
				graph.setArcHeight(10);
				graph.setArcWidth(10);

				canvas.createGraph(graph);
			}
		});

		JButton button1 = new JButton("Ellipse");
		panel.add(button1);
		button1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				EllipseGraph graph = new EllipseGraph(canvas);
				graph.setSize(160, 60);

				canvas.createGraph(graph);
			}
		});

		JButton button2 = new JButton("Process");
		panel.add(button2);
		button2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				ProcessGraph graph = new ProcessGraph(canvas);
//				graph.setWidth(200);
//				graph.setHeight(80);
//				graph.setArcHeight(10);
//				graph.setArcWidth(10);
//
//				canvas.createGraph(graph);
			}
		});

		JButton button3 = new JButton("Data");
		panel.add(button3);
		button3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				DataGraph graph = new DataGraph(canvas);
//				graph.setWidth(160);
//				graph.setHeight(60);
//
//				canvas.createGraph(graph);
			}
		});

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.setVisible(true);
			}
		});
	}

	public GraphCanvas() {
		setLayout(null);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}

	public void setSelectedDecorator(IGraph selectedDecorator) {

	}

	public void setHotDecorator(IGraph hotDecorator) {

	}

	public void setPreviewDecorator(IGraph previewDecorator) {

	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D graphics2D = (Graphics2D) g;

//		int borderWidth = getScale(2);
//		int x = getScale(1);
//		int y = getScale(1);
//		int width = getScale(4);
//		int height = getScale(4);
//
//		graphics2D.setColor(Color.RED);
//		Stroke stroke = new BasicStroke(borderWidth);
//		graphics2D.setStroke(stroke);
//		Rectangle rectangle = new Rectangle(x, y, width, height);
//		graphics2D.draw(rectangle);


//		graphics2D.setColor(Color.RED);
//		Stroke stroke = new BasicStroke(4);
//		graphics2D.setStroke(stroke);
//		Rectangle rectangle = new Rectangle(3, 3, 5, 5);
//		graphics2D.draw(rectangle);


		setViewRenderingHints(graphics2D);
		paintBackground(graphics2D);
		paintCanvas(graphics2D);
		paintGraphs(graphics2D);
	}

	private int getScale(int i) {
		return i * 100;
	}

	/**
	 * 绘制背景
	 *
	 * @param g
	 */
	private void paintBackground(Graphics2D g) {
		g.setColor(DEFAULT_BACKGROUNDCOLOR);
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	/**
	 * 绘制画布
	 *
	 * @param g
	 */
	private void paintCanvas(Graphics2D g) {
		Rectangle rect = getCanvasViewBounds();
		g.setColor(DEFAULT_CANVAS_COLOR);
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
	}

	private void paintGraphs(Graphics2D g) {
		Vector<IGraph> graphs = this.graphQuadTree.getDatasInside();

		for (int i = 0; i < graphs.size(); i++) {
			IGraph graph = graphs.get(i);
			this.painterFactory.getPainter(graph, g).paint();
		}

		for (int i = 0; i < this.lines.size(); i++) {
			LineGraph lineGraph = this.lines.get(i);
			this.painterFactory.getPainter(lineGraph, g).paint();
		}

		this.hotDecorator.decorate((AbstractGraph) this.hotGraph);
		this.selectedDecorator.decorate((AbstractGraph) this.selectedGraph);
		this.previewDecorator.decorate((AbstractGraph) this.previewGraph);
		this.painterFactory.getPainter(this.hotDecorator, g).paint();
		this.painterFactory.getPainter(this.selectedDecorator, g).paint();
		this.painterFactory.getPainter(this.previewDecorator, g).paint();
	}

	protected Rectangle getCanvasViewBounds() {
		return new Rectangle(0, 0, getWidth(), getHeight());
	}

	/**
	 * 这是个借鉴方法，大约是一些抗锯齿的设置
	 *
	 * @param g
	 */
	private void setViewRenderingHints(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

	public void createGraph(AbstractGraph graph) {
		this.previewGraph = graph;
	}

	private Point2D panelToCanvas(Point point) {
		return new Point2D.Double(point.getX(), point.getY());
	}

	private Point canvasToPanel(Point2D point2D) {
		int panelX = Double.valueOf(point2D.getX()).intValue();
		int panelY = Double.valueOf(point2D.getY()).intValue();
		return new Point(panelX, panelY);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			Point point = e.getPoint();

			if (this.previewGraph == null) {

				// toCreation 为空，则查询
				IGraph graph = findGraph(point);

				if (graph == null) {
					if (this.selectedGraph != null) {
						repaint(this.selectedGraph.getBounds());
						this.selectedGraph = graph;
					}
				} else {
					if (this.selectedGraph != null) {
						if (this.selectedGraph != graph) {
							repaint(this.selectedGraph.getBounds());
							this.selectedGraph = graph;
							repaint(this.selectedGraph.getBounds());
						}
					} else {
						this.selectedGraph = graph;
						repaint(this.selectedGraph.getBounds());
					}
				}
			}
		}
	}

	private IGraph findGraph(Point point) {
		IGraph graph = null;
		List<IGraph> graphs = this.graphQuadTree.search(point);

		if (graphs != null && graphs.size() > 0) {
			graph = graphs.get(0);
		}
		return graph;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			IGraph graph = findGraph(e.getPoint());

			// TODO 不加 contains 判断会导致多对象的时候出现奇怪的拖拽现象，可能是四叉树实现不完整，需要优化
			if (graph != null && graph.contains(e.getPoint())) {
				this.draggedGraph = graph;
				this.dragBegin = e.getPoint();
				this.dragCenter = this.draggedGraph.getCenter();
			} else {
				this.draggedGraph = null;
				this.dragBegin = null;
				this.dragCenter = null;
				this.line = null;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			Point point = e.getPoint();

			if (this.previewGraph != null) {

				// toCreation 不为空，则新建
				repaint(this.previewGraph, point);
				Rectangle bounds = this.previewGraph.getBounds();
				this.graphQuadTree.add(this.previewGraph, bounds);

				if (this.previewGraph instanceof ProcessGraph) {
//					DataGraph graph = new DataGraph(this);
//					graph.setWidth(160);
//					graph.setHeight(60);
//					graph.setX(this.toCreation.getX() + this.toCreation.getWidth() + 150);
//					graph.setY(this.toCreation.getY() + (this.toCreation.getHeight() - graph.getHeight()) / 2);
//					Rectangle graphBounds = graph.getBounds();
//					this.graphQuadTree.add(graph, graphBounds);
//					repaint(graph.getBounds());

					LineGraph lineGraph = new LineGraph(this);
//					lineGraph.setStart(this.toCreation);
//					lineGraph.setEnd(graph);
					this.lines.add(lineGraph);
					repaint();
				}
				this.previewGraph = null;
			}
		} else if (SwingUtilities.isRightMouseButton(e)) {
			this.previewGraph = null;
			repaint();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {
		this.draggedGraph = null;
		this.dragBegin = null;
		this.dragCenter = null;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e) && this.draggedGraph != null && this.dragBegin != null) {
			this.graphQuadTree.remove(this.draggedGraph);
			Point dragged = new Point();
			dragged.setLocation(this.dragCenter.getX(), this.dragCenter.getY());
			dragged.translate(e.getPoint().x - this.dragBegin.x, e.getPoint().y - this.dragBegin.y);
			repaint(this.draggedGraph, dragged);
			this.graphQuadTree.add(this.draggedGraph, this.draggedGraph.getBounds());
//			for (int i = 0; i < this.draggedGraph.getLines().size(); i++) {
//				Rectangle rect = this.draggedGraph.getLines().get(i).getShape().getBounds();
//				rect.grow(1, 1);
//				repaint(rect);
//			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (this.previewGraph != null) {
			repaint(this.previewGraph, e.getPoint());
		} else {
			IGraph graph = findGraph(e.getPoint());

			if (graph == null) {
				if (this.hotGraph != null) {
					repaint(this.hotGraph.getBounds());
					this.hotGraph = graph;
				}
			} else {
				if (this.hotGraph != null) {
					if (this.hotGraph != graph) {
						repaint(this.hotGraph.getBounds());
						this.hotGraph = graph;
						repaint(this.hotGraph.getBounds());
					}
				} else {
					this.hotGraph = graph;
					repaint(this.hotGraph.getBounds());
				}
			}
		}
	}

	private void repaint(IGraph graph, Point point) {
		if (graph.getLocation() != point) {
			Rectangle dirtyRect = graph.getBounds();
			double x = point.getX() - graph.getWidth() / 2;
			double y = point.getY() - graph.getHeight() / 2;
			Point location = new Point();
			location.setLocation(x, y);
			graph.setLocation(location);
			repaint(dirtyRect);
			repaint(graph.getBounds());
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {

	}
}
