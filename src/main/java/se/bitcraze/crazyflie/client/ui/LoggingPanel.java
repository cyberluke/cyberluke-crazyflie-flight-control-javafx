package se.bitcraze.crazyflie.client.ui;

import java.awt.FlowLayout;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import se.bitcraze.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.LogListener;
import se.bitcraze.crazyflie.LogVariable;
import se.bitcraze.crazyflie.Logging;

public class LoggingPanel extends JComponent implements LogListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7602222159913289862L;
	private final Crazyflie crazyflie;
	private LoggingTableModel tableModel;

	public LoggingPanel(Crazyflie crazyflie) {
		this.crazyflie = crazyflie;
		crazyflie.addListener(this);
		initControls();
	}

	protected void initControls() {
		tableModel = new LoggingTableModel(crazyflie.getLogging());
		JTable table = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		setLayout(new FlowLayout());
		add(scrollPane);
	}

	@Override
	public void valuesReceived(String name, Map<String, Object> values) {
		tableModel.fireTableDataChanged();
    }

	private static class LoggingTableModel extends AbstractTableModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1548492342070658851L;
		private final Logging logging;
		private LogVariable[] variables = null;

		public LoggingTableModel(Logging logging) {
			this.logging = logging;
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
			return logging.getNames().size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			LogVariable v = variables[rowIndex];
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
				variables = new LogVariable[logging.getCount()];
				for (LogVariable v : logging.values())
					variables[v.getId()] = v;
			}
			super.fireTableDataChanged();
		}

	}
}
