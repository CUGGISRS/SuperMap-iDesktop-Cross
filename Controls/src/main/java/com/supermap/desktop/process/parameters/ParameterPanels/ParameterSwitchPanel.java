package com.supermap.desktop.process.parameters.ParameterPanels;

import com.supermap.desktop.process.enums.ParameterType;
import com.supermap.desktop.process.parameter.interfaces.IParameter;
import com.supermap.desktop.process.parameter.interfaces.ParameterPanelDescribe;
import com.supermap.desktop.process.parameter.ipls.ParameterSwitch;
import com.supermap.desktop.ui.controls.GridBagConstraintsHelper;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author XiaJT
 */
@ParameterPanelDescribe(parameterPanelType = ParameterType.SWITCH)
public class ParameterSwitchPanel extends SwingPanel {
	private ParameterSwitch parameter;

	public ParameterSwitchPanel(IParameter parameter) {
		super(parameter);
		this.parameter = ((ParameterSwitch) parameter);
		this.parameter.addPropertyListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(ParameterSwitch.CURRENT_PARAMETER)) {
					panel.removeAll();
					if (evt.getNewValue() != null && ((IParameter) evt.getNewValue()).getParameterPanel().getPanel() != null) {
						panel.add(((JPanel) ((IParameter) evt.getNewValue()).getParameterPanel().getPanel()), new GridBagConstraintsHelper(0, 0, 1, 1).setFill(GridBagConstraints.HORIZONTAL).setWeight(1, 1));
					}
					panel.revalidate();
					panel.repaint();
				}
			}
		});
	}

	@Override
	public Object getPanel() {
		panel.setLayout(new GridBagLayout());
		if (parameter.getCurrentParameter() != null) {
			panel.add((JPanel) parameter.getCurrentParameter().getParameterPanel().getPanel(), new GridBagConstraintsHelper(0, 0, 1, 1).setFill(GridBagConstraints.HORIZONTAL).setWeight(1, 1));
		} else {
			panel.add(new JPanel(), new GridBagConstraintsHelper(0, 0, 1, 1).setFill(GridBagConstraints.HORIZONTAL).setWeight(1, 1).setInsets(-5, 0, 0, 0));
		}
		return panel;
	}

}
