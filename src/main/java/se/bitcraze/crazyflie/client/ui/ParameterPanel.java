package se.bitcraze.crazyflie.client.ui;

import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import se.bitcraze.crazyflie.ConnectionAdapter;
import se.bitcraze.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.Parameter;
import se.bitcraze.crazyflie.Parameters;
import se.bitcraze.crazyflie.crtp.CrtpDriver;

public class ParameterPanel extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7602222159913289862L;
	private final Crazyflie crazyflie;
	private ParameterTableModel tableModel;

	public ParameterPanel(Crazyflie crazyflie) {
		this.crazyflie = crazyflie;
		crazyflie.addListener(new ConnectionAdapter() {

			@Override
			public void connectionSetupFinished(CrtpDriver driver) {
				super.connectionSetupFinished(driver);
				if (tableModel != null)
					tableModel.fireTableDataChanged();
			}
		});
		initControls();
	}

	protected void initControls() {
		tableModel = new ParameterTableModel(crazyflie.getParameters());
		JTable table = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		setLayout(new FlowLayout());
		add(scrollPane);
	}

	private static class ParameterTableModel extends AbstractTableModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1548492342070658851L;
		private final Parameters parameters;
		private Parameter[] variables = null;

		public ParameterTableModel(Parameters parameters) {
			this.parameters = parameters;
		}

		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "Name";

			default:
				break;
			}
			return "Value";
		}

		@Override
		public int getRowCount() {
			return parameters.getNames().size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Parameter v = variables[rowIndex];
			if (v == null)
				return "";
			switch (columnIndex) {
			case 0:
				return v.getName();

			default:
				break;
			}
			return v.getValue();
		}

		@Override
		public void fireTableDataChanged() {
			if (variables == null) {
				variables = new Parameter[parameters.getCount()];
				for (Parameter v : parameters.values())
					variables[v.getId()] = v;
			}
			super.fireTableDataChanged();
		}

	}
}
