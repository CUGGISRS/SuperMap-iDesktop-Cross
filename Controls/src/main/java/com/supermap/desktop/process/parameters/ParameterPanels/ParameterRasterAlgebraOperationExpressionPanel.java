package com.supermap.desktop.process.parameters.ParameterPanels;

import com.supermap.desktop.process.enums.ParameterType;
import com.supermap.desktop.process.parameter.interfaces.IParameter;
import com.supermap.desktop.process.parameter.interfaces.IParameterPanel;
import com.supermap.desktop.process.parameter.interfaces.ParameterPanelDescribe;
import com.supermap.desktop.process.parameter.ipls.ParameterRasterAlgebraOperationExpression;
import com.supermap.desktop.ui.controls.DialogResult;
import com.supermap.desktop.ui.controls.GridBagConstraintsHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by lixiaoyao on 2017/8/29.
 */
@ParameterPanelDescribe(parameterPanelType = ParameterType.RASTER_EXPRESSION)
public class ParameterRasterAlgebraOperationExpressionPanel extends SwingPanel implements IParameterPanel{
	private ParameterRasterAlgebraOperationExpression parameterRasterAlgebraOperationExpression;
	private JButton buttonExpression;
	private boolean isSelectingItem = false;

	public ParameterRasterAlgebraOperationExpressionPanel(IParameter parameter){
		super(parameter);
		this.parameterRasterAlgebraOperationExpression = (ParameterRasterAlgebraOperationExpression) parameter;
		init();
	}

	private void init() {
		this.buttonExpression = new JButton();
		this.buttonExpression.setText(this.parameterRasterAlgebraOperationExpression.getDescribe());
		this.buttonExpression.setEnabled(this.parameterRasterAlgebraOperationExpression.isEnabled());
		this.panel.setLayout(new GridBagLayout());
		this.panel.add(this.buttonExpression, new GridBagConstraintsHelper(0, 0, 1, 1).setAnchor(parameterRasterAlgebraOperationExpression.getAnchor()).setWeight(0, 0).setFill(GridBagConstraints.NONE));
		registEvents();
	}

	private void registEvents() {
		this.buttonExpression.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!isSelectingItem) {
					isSelectingItem = true;
					RasterAlgebraOperationDialog rasterAlgebraOperationDialog = new RasterAlgebraOperationDialog();

					if (rasterAlgebraOperationDialog.showDialog() == DialogResult.OK) {
						parameterRasterAlgebraOperationExpression.setSelectedItem("");//todo 获取表达式
					}
					isSelectingItem = false;
				}
			}
		});
	}
}
