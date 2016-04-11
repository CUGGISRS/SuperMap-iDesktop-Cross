package com.supermap.desktop.dialog.symbolDialogs.JpanelSymbols;

import com.supermap.data.GeoStyle;
import com.supermap.data.Resources;
import com.supermap.data.Symbol;
import com.supermap.data.SymbolGroup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * @author XiaJt
 */
public abstract class JPanelSymbols extends JPanel {

	protected SymbolGroup symbolGroup;
	protected Resources resources;
	private SymbolPanel lastSelectedPanel;
	private java.util.List<SymbolSelectedChangedListener> symbolSelectedChangedListeners;
	/**
	 * 总共有多少行
	 */
	private int panelRow;
	/**
	 * 总共有多少列
	 */
	private int panelColumn;
	protected GeoStyle geoStyle;

	public JPanelSymbols() {
		this.setBackground(Color.WHITE);
		FlowLayout mgr = new FlowLayout(FlowLayout.LEADING, 1, 1) {

			public Dimension preferredLayoutSize(Container target) {
				return computeSize(target);
			}

			private Dimension computeSize(Container target) {
				synchronized (target.getTreeLock()) {
					panelColumn = -1;
					int hgap = getHgap();
					int vgap = getVgap();
					int w = target.getWidth();

					// Let this behave like a regular FlowLayout (single row)
					// if the container hasn't been assigned any size yet
					if (w == 0) {
						w = Integer.MAX_VALUE;
					}

					Insets insets = target.getInsets();
					if (insets == null) {
						insets = new Insets(0, 0, 0, 0);
					}
					int reqdWidth = 0;

					int maxWidth = w - (insets.left + insets.right + hgap * 2);
					int n = target.getComponentCount();
					int x = 0;
					int y = insets.top + vgap; // FlowLayout starts by adding vgap, so do that here too.
					int rowHeight = 0;
					int rowCount = 0;
					int columnCount = 0;
					for (int i = 0; i < n; i++) {
						Component c = target.getComponent(i);
						if (c.isVisible()) {
							Dimension d = c.getPreferredSize();
							if (x == 0 || x + d.width <= maxWidth) {
								// fits in current row.
								if (x > 0) {
									x += hgap;
								}
								x += d.width;
								columnCount++;
								rowHeight = Math.max(rowHeight, d.height);
							} else {
								// Start of new row
								if (panelColumn == -1) {
									panelColumn = columnCount;
								}
								rowCount++;
								x = d.width;
								y += vgap + rowHeight;
								rowHeight = d.height;
							}
							reqdWidth = Math.max(reqdWidth, x);
						}
					}
					y += rowHeight;
					y += insets.bottom;
					panelRow = rowCount;
					return new Dimension(reqdWidth + insets.left + insets.right, y);
				}
			}
		};
		mgr.setAlignOnBaseline(true);
		this.setLayout(mgr);
	}

	public void setSymbolGroup(Resources resources, final SymbolGroup symbolGroup) {
		if (symbolGroup == null || this.symbolGroup == symbolGroup) {
			return;
		}
		this.resources = resources;
		this.removeAll();
		this.symbolGroup = symbolGroup;
//		SwingUtilities.invokeLater(new Runnable() {
//			@Override
//			public void run() {
				if (symbolGroup.getLibrary().getRootGroup() == symbolGroup) {
					initSystemPanels();
				}
				initDefaultPanel();
		addListeners();
//			}
//		});
	}

	private void addListeners() {
		for (int i = 0; i < this.getComponentCount(); i++) {
			this.getComponent(i).addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (e.getSource() instanceof SymbolPanel) {
						if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
							fireSymbolDoubleClicked();
						}
						SymbolPanel symbolPanel = (SymbolPanel) e.getSource();
						if (symbolPanel == lastSelectedPanel) {
							return;
						}
						if (lastSelectedPanel != null) {
							lastSelectedPanel.setUnselected();
						}
						symbolPanel.setSelected();
						lastSelectedPanel = symbolPanel;
						changeGeoStyleId(lastSelectedPanel.getSymbolID());
						fireSymbolSelectedChanged(lastSelectedPanel.getSymbol());
					}
				}
			});
		}
	}

	private void fireSymbolDoubleClicked() {
		if (symbolSelectedChangedListeners != null) {
			for (SymbolSelectedChangedListener symbolSelectedChangedListener : symbolSelectedChangedListeners) {
				symbolSelectedChangedListener.SymbolSelectedDoubleClicked();
			}
		}
	}

	private void fireSymbolSelectedChanged(Symbol symbol) {
		if (symbolSelectedChangedListeners != null) {
			for (SymbolSelectedChangedListener symbolSelectedChangedListener : symbolSelectedChangedListeners) {
				symbolSelectedChangedListener.SymbolSelectedChangedEvent(symbol);
			}
		}
	}

	public void addSymbolSelectedChangedListener(SymbolSelectedChangedListener symbolSelectedChangedListener) {
		if (this.symbolSelectedChangedListeners == null) {
			symbolSelectedChangedListeners = new ArrayList<>();
		}
		symbolSelectedChangedListeners.add(symbolSelectedChangedListener);
	}

	public void removeSymbolSelectedChangedListener(SymbolSelectedChangedListener symbolSelectedChangedListener) {
		symbolSelectedChangedListeners.remove(symbolSelectedChangedListener);
	}

	/**
	 * 点线面分别设置对应ID
	 *
	 * @param symbolID 修改后的id
	 */
	protected abstract void changeGeoStyleId(int symbolID);

	private int getComponentRow(SymbolPanel symbolPanel) {
		return this.getComponentZOrder(symbolPanel) / panelColumn;
	}

	private int getComponentColumn(SymbolPanel symbolPanel) {
		return this.getComponentZOrder(symbolPanel) % panelColumn;
	}

	protected abstract void initSystemPanels();

	protected abstract void initDefaultPanel();

	public void setSelectSymbol(int symbolId) {
		boolean isFind = false;
		for (int i = 0; i < this.getComponentCount(); i++) {
			if (((SymbolPanel) this.getComponent(i)).getSymbolID() == symbolId) {
				if (lastSelectedPanel != null) {
					lastSelectedPanel.setUnselected();
				}
				lastSelectedPanel = (SymbolPanel) this.getComponent(i);
				lastSelectedPanel.setSelected();
				if (this.getParent() != null && this.getParent().getParent() instanceof JScrollPane) {
					((JScrollPane) this.getParent().getParent()).getVerticalScrollBar().setValue((int) lastSelectedPanel.getLocation().getY());
				}
				isFind = true;
				break;
			}
		}
		if (isFind) {
			fireSymbolSelectedChanged(lastSelectedPanel.getSymbol());
		}
	}

	public void setGeoStyle(GeoStyle geoStyle) {
		this.geoStyle = geoStyle;
	}
}
