/* 
 *  Copyright (C) 2014 Andreas Huber
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package se.bitcraze.crazyflie.client.ui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import se.bitcraze.crazyflie.Crazyflie;

public class ConnectDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6373099441223910412L;
	private JButton connect;

	public ConnectDialog(Frame owner, final Crazyflie crazyflie) {
		super(owner, "Connect...", true);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());
		setSize(200, 200);
		final JList list = new JList();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSize(200, 180);
		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index = list.locationToIndex(e.getPoint());
					if (index >= 0)
						connect(crazyflie, (String) list.getModel()
								.getElementAt(index));
				}
			}
		});
		list.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent event) {
				if (event.getFirstIndex() >= 0) {
					connect.setEnabled(true);
				}
			}
		});
		add(list, BorderLayout.CENTER);
		JPanel buttons = new JPanel();
		final JButton scan = new JButton("Scan");
		scan.setSize(50, 20);
		scan.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				scan.setEnabled(false);
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						String[] interfaces = Crazyflie.scanInterfaces();
						list.setListData(interfaces);
						scan.setEnabled(true);
					}
				});

			}
		});
		buttons.add(scan);
		connect = new JButton("Connect");
		connect.setSize(50, 20);
		connect.setEnabled(false);
		connect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int index = list.getSelectedIndex();
				if (index >= 0)
					connect(crazyflie,
							(String) list.getModel().getElementAt(index));
			}
		});
		buttons.add(connect);
		add(buttons, BorderLayout.SOUTH);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				super.windowClosed(e);
				list.setListData(new String[] {});
			}

		});
	}

	private void connect(final Crazyflie crazyflie, final String uri) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					crazyflie.connect(uri);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		setVisible(false);
	}

}
