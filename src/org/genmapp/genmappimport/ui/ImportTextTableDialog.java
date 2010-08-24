/*******************************************************************************
 * Copyright 2010 Alexander Pico
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.genmapp.genmappimport.ui;

import static org.genmapp.genmappimport.reader.TextFileDelimiters.PIPE;
import static org.genmapp.genmappimport.ui.theme.ImportDialogFontTheme.TITLE_FONT;
import static org.genmapp.genmappimport.ui.theme.ImportDialogIconSets.BOOLEAN_ICON;
import static org.genmapp.genmappimport.ui.theme.ImportDialogIconSets.FLOAT_ICON;
import static org.genmapp.genmappimport.ui.theme.ImportDialogIconSets.ID_ICON;
import static org.genmapp.genmappimport.ui.theme.ImportDialogIconSets.INT_ICON;
import static org.genmapp.genmappimport.ui.theme.ImportDialogIconSets.LIST_ICON;
import static org.genmapp.genmappimport.ui.theme.ImportDialogIconSets.RIGHT_ARROW_ICON;
import static org.genmapp.genmappimport.ui.theme.ImportDialogIconSets.SPREADSHEET_ICON_LARGE;
import static org.genmapp.genmappimport.ui.theme.ImportDialogIconSets.STRING_ICON;
import giny.model.Node;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.xml.bind.JAXBException;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.genmapp.genmappimport.commands.GenMAPPImportCyCommandHandler;
import org.genmapp.genmappimport.reader.AttributeMappingParameters;
import org.genmapp.genmappimport.reader.DefaultAttributeTableReader;
import org.genmapp.genmappimport.reader.ExcelAttributeSheetReader;
import org.genmapp.genmappimport.reader.TextFileDelimiters;
import org.genmapp.genmappimport.reader.TextTableReader;
import org.jdesktop.layout.GroupLayout;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;
import cytoscape.util.CyFileFilter;
import cytoscape.util.FileUtil;
import cytoscape.util.swing.ColumnResizer;
import cytoscape.util.swing.JStatusBar;

/**
 * Main UI for Table Import.
 * 
 */
@SuppressWarnings("serial")
public class ImportTextTableDialog extends JDialog implements
		PropertyChangeListener {
	/**
	 * This dialog GUI will be switched based on the following parameters:
	 * 
	 * SIMPLE_ATTRIBUTE_IMPORT: Import attributes in text table.
	 * 
	 */
	public static final int SIMPLE_ATTRIBUTE_IMPORT = 1;

	/**
	 * Enums for file types.
	 */
	public static enum FileTypes {
		ATTRIBUTE_FILE, CUSTOM_ANNOTATION_FILE;
	}

	/*
	 * Signals used among Swing components in this dialog:
	 */
	public static final String LIST_DELIMITER_CHANGED = "listDelimiterChanged";

	public static final String LIST_DATA_TYPE_CHANGED = "listDataTypeChanged";

	public static final String ATTR_DATA_TYPE_CHANGED = "attrDataTypeChanged";

	public static final String ATTRIBUTE_NAME_CHANGED = "aliasTableChanged";

	public static final String SHEET_CHANGED = "sheetChanged";

	public static final String NETWORK_IMPORT_TEMPLATE_CHANGED = "networkImportTemplateChanged";

	private static final String[] keyTable = { "Alias?",
			"Column (Attribute Name)", "Data Type" };

	private static final String ID = "ID";

	private static final String EXCEL_EXT = ".xls";

	// Key column index
	private int keyInFile;

	// Case sensitivity
	private Boolean caseSensitive = false;

	// Data Type
	private Map<String, String> annotationUrlMap;

	private Map<String, String> annotationFormatMap;

	private Map<String, Map<String, String>> annotationAttributesMap;

	private List<Byte> attributeDataTypes;

	/*
	 * This is for storing data type in the list object.
	 */
	private Byte[] listDataTypes;

	/*
	 * Tracking multiple sheets.
	 */
	private Map<String, Integer> primaryKeyMap;

	private String[] columnHeaders;

	private String listDelimiter;

	private boolean[] importFlag;

	private CyAttributes selectedAttributes;

	private PropertyChangeSupport changes = new PropertyChangeSupport(this);

	private File[] inputFiles;

	/**
	 * Creates new form ImportAttributesDialog
	 * 
	 * @throws IOException
	 * @throws JAXBException
	 */
	public ImportTextTableDialog(Frame parent, boolean modal)
			throws JAXBException, IOException {
		this(parent, modal, ImportTextTableDialog.SIMPLE_ATTRIBUTE_IMPORT);
	}

	public ImportTextTableDialog(Frame parent, boolean modal, int dialogType)
			throws JAXBException, IOException {
		super(parent, modal);

		// Default Attribute is node attr.
		selectedAttributes = Cytoscape.getNodeAttributes();

		this.listDelimiter = PIPE.toString();
		this.primaryKeyMap = new HashMap<String, Integer>();

		annotationUrlMap = new HashMap<String, String>();
		annotationFormatMap = new HashMap<String, String>();
		annotationAttributesMap = new HashMap<String, Map<String, String>>();

		attributeDataTypes = new ArrayList<Byte>();
		initComponents();
		updateComponents();

		previewPanel.addPropertyChangeListener(this);
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		changes.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		changes.removePropertyChangeListener(l);
	}

	/**
	 * Listening to local signals used among Swing components in this dialog.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(LIST_DELIMITER_CHANGED)) {
			/*
			 * List delimiter has been changed by preview table GUI.
			 */
			listDelimiter = evt.getNewValue().toString();
		} else if (evt.getPropertyName().equals(LIST_DATA_TYPE_CHANGED)) {
			listDataTypes = (Byte[]) evt.getNewValue();
		} else if (evt.getPropertyName().equals(ATTR_DATA_TYPE_CHANGED)) {
			/*
			 * Data type of an attribute has been chabged.
			 */
			final Vector vec = (Vector) evt.getNewValue();
			final Integer key = (Integer) vec.get(0);
			final Byte newType = (Byte) vec.get(1);

			if (key > attributeDataTypes.size()) {
				attributeDataTypes = new ArrayList<Byte>();

				for (Byte type : previewPanel.getCurrentDataTypes()) {
					attributeDataTypes.add(type);
				}
			}

			attributeDataTypes.set(key, newType);

		} else if (evt.getPropertyName().equals(ATTRIBUTE_NAME_CHANGED)) {
			// Update Primary Key combo box
			updatePrimaryKeyComboBox();

		} else if (evt.getPropertyName().equals(SHEET_CHANGED)) {
			/*
			 * Only when the file is in Excel format.
			 */
			final int columnCount = previewPanel.getPreviewTable()
					.getColumnCount();

			updatePrimaryKeyComboBox();
		} else if (evt.getPropertyName()
				.equals(NETWORK_IMPORT_TEMPLATE_CHANGED)) {

			previewPanel.getPreviewTable().getTableHeader().resizeAndRepaint();
			previewPanel.getPreviewTable().repaint();

			// previewPanel.repaint();
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc=" Generated Code">
	private void initComponents() {
		statusBar = new JStatusBar();

		importTypeButtonGroup = new ButtonGroup();

		counterSpinner = new javax.swing.JSpinner();
		counterLabel = new javax.swing.JLabel();
		reloadButton = new javax.swing.JButton();
		showAllRadioButton = new javax.swing.JRadioButton();
		counterRadioButton = new javax.swing.JRadioButton();

		attrTypeButtonGroup = new javax.swing.ButtonGroup();
		titleIconLabel1 = new javax.swing.JLabel();
		titleIconLabel2 = new javax.swing.JLabel();
		titleIconLabel3 = new javax.swing.JLabel();
		titleLabel = new javax.swing.JLabel();
		titleSeparator = new javax.swing.JSeparator();
		importButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		helpButton = new javax.swing.JButton();
		basicPanel = new javax.swing.JPanel();
		nodeRadioButton = new javax.swing.JRadioButton();
		edgeRadioButton = new javax.swing.JRadioButton();
		networkRadioButton = new javax.swing.JRadioButton();

		targetDataSourceTextField = new javax.swing.JTextField();
		selectAttributeFileButton = new javax.swing.JButton();
		advancedPanel = new javax.swing.JPanel();
		textImportCheckBox = new javax.swing.JCheckBox();
		mappingAttributeComboBox = new javax.swing.JComboBox();
		textImportOptionPanel = new javax.swing.JPanel();
		delimiterPanel = new javax.swing.JPanel();
		tabCheckBox = new javax.swing.JCheckBox();
		commaCheckBox = new javax.swing.JCheckBox();
		semicolonCheckBox = new javax.swing.JCheckBox();
		spaceCheckBox = new javax.swing.JCheckBox();
		otherCheckBox = new javax.swing.JCheckBox();
		otherDelimiterTextField = new javax.swing.JTextField();
		transferNameCheckBox = new javax.swing.JCheckBox();

		headerRowsPanel = new JPanel();

		defaultInteractionLabel = new JLabel();
		defaultInteractionTextField = new JTextField();

		startRowSpinner = new JSpinner();
		startRowLabel = new JLabel();

		commentLineLabel = new JLabel();
		commentLineTextField = new JTextField();
		commentLineTextField.setName("commentLineTextField");

		titleLabel.setFont(TITLE_FONT.getFont());

		defaultInteractionLabel.setEnabled(false);
		defaultInteractionTextField.setEnabled(false);

		previewPanel = new PreviewTablePanel();

		primaryLabel = new JLabel("ID column: ");
		primaryKeyComboBox = new JComboBox();
		primaryKeyComboBox.setEnabled(false);
		primaryKeyComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				primaryKeyComboBoxActionPerformed(evt);
			}
		});

		/*
		 * Set tooltips options.
		 */
		ToolTipManager tp = ToolTipManager.sharedInstance();
		tp.setInitialDelay(40);
		tp.setDismissDelay(50000);

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		titleIconLabel2.setIcon(RIGHT_ARROW_ICON.getIcon());

		titleIconLabel3.setIcon(new ImageIcon(Cytoscape.class
				.getResource("images/icon48.png")));

		titleSeparator.setForeground(java.awt.Color.blue);

		importButton.setText("Import");
		importButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					importButtonActionPerformed(evt);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		helpButton.setBackground(new java.awt.Color(255, 255, 255));
		helpButton.setText("?");
		helpButton.setToolTipText("Display help page...");
		helpButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
		helpButton.setPreferredSize(new java.awt.Dimension(14, 14));
		helpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				helpButtonActionPerformed(arg0);
			}
		});

		/*
		 * Data Source Panel Layouts.
		 */
		basicPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
				"Import",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Dialog", 1, 11)));

		titleIconLabel1.setIcon(SPREADSHEET_ICON_LARGE.getIcon());

		selectAttributeFileButton.setText("Select File(s)");
		selectAttributeFileButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						try {
							selectAttributeFileButtonActionPerformed(evt);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});

		GroupLayout basicPanelLayout = new GroupLayout(basicPanel);
		basicPanel.setLayout(basicPanelLayout);

		basicPanelLayout
				.setHorizontalGroup(basicPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								basicPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.add(
												targetDataSourceTextField,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												300, Short.MAX_VALUE)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(selectAttributeFileButton)
										.addContainerGap()));
		basicPanelLayout.setVerticalGroup(basicPanelLayout.createParallelGroup(
				org.jdesktop.layout.GroupLayout.LEADING).add(
				selectAttributeFileButton).add(targetDataSourceTextField,
				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE));

		/*
		 * Layout panel for additional options
		 */
		textImportCheckBox.setText("Show additional options");
		textImportCheckBox.setBorder(javax.swing.BorderFactory
				.createEmptyBorder(0, 0, 0, 0));
		textImportCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		textImportCheckBox
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						textImportCheckBoxActionPerformed(evt);
					}
				});

		textImportOptionPanel.setBorder(javax.swing.BorderFactory
				.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

		delimiterPanel.setBorder(javax.swing.BorderFactory
				.createTitledBorder("Delimiter"));
		tabCheckBox.setText("Tab");
		tabCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0,
				0, 0));
		tabCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		tabCheckBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					delimiterCheckBoxActionPerformed(evt);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		commaCheckBox.setText("Comma");
		commaCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,
				0, 0, 0));
		commaCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		commaCheckBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					delimiterCheckBoxActionPerformed(evt);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		semicolonCheckBox.setText("Semicolon");
		semicolonCheckBox.setBorder(javax.swing.BorderFactory
				.createEmptyBorder(0, 0, 0, 0));
		semicolonCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		semicolonCheckBox
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						try {
							delimiterCheckBoxActionPerformed(evt);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});

		spaceCheckBox.setText("Space");
		spaceCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,
				0, 0, 0));
		spaceCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		spaceCheckBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					delimiterCheckBoxActionPerformed(evt);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		otherCheckBox.setText("Other");
		otherCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,
				0, 0, 0));
		otherCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		otherCheckBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					delimiterCheckBoxActionPerformed(evt);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		otherDelimiterTextField.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent evt) {
				try {
					otherTextFieldActionPerformed(evt);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			public void keyReleased(KeyEvent arg0) {
			}

			public void keyTyped(KeyEvent evt) {
			}
		});

		GroupLayout delimiterPanelLayout = new GroupLayout(delimiterPanel);
		delimiterPanel.setLayout(delimiterPanelLayout);
		delimiterPanelLayout
				.setHorizontalGroup(delimiterPanelLayout.createParallelGroup(
						GroupLayout.LEADING).add(
						delimiterPanelLayout.createSequentialGroup().add(
								tabCheckBox).addPreferredGap(
								org.jdesktop.layout.LayoutStyle.RELATED).add(
								commaCheckBox).addPreferredGap(
								org.jdesktop.layout.LayoutStyle.RELATED).add(
								semicolonCheckBox).addPreferredGap(
								org.jdesktop.layout.LayoutStyle.RELATED).add(
								spaceCheckBox).addPreferredGap(
								org.jdesktop.layout.LayoutStyle.RELATED).add(
								otherCheckBox).addPreferredGap(
								org.jdesktop.layout.LayoutStyle.RELATED).add(
								otherDelimiterTextField,
								GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)));
		delimiterPanelLayout.setVerticalGroup(delimiterPanelLayout
				.createParallelGroup(GroupLayout.LEADING).add(
						delimiterPanelLayout.createSequentialGroup().add(
								delimiterPanelLayout.createParallelGroup(
										GroupLayout.BASELINE).add(tabCheckBox)
										.add(commaCheckBox).add(
												semicolonCheckBox).add(
												spaceCheckBox).add(
												otherCheckBox).add(
												otherDelimiterTextField,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE))
								.addContainerGap(GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)));

		transferNameCheckBox.setEnabled(false);

		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(100, 1,
				10000000, 10);
		counterSpinner.setModel(spinnerModel);
		counterSpinner
				.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
					public void mouseWheelMoved(
							java.awt.event.MouseWheelEvent evt) {
						counterSpinnerMouseWheelMoved(evt);
					}
				});
		counterSpinner
				.setToolTipText("<html><body>Click <strong text=\"red\"><i>Refresh Preview</i></strong> button to update the table.</body></html>");

		counterLabel.setText("entries.");

		reloadButton.setText("Refresh Preview");
		reloadButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				try {
					reloadButtonActionPerformed(evt);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		headerRowsPanel.setBorder(javax.swing.BorderFactory
				.createTitledBorder("Headers"));

		transferNameCheckBox.setText("Use first line as column names");

		transferNameCheckBox.setBorder(null);
		transferNameCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		transferNameCheckBox
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						transferNameCheckBoxActionPerformed(evt);
					}
				});

		startRowLabel.setText("  Start importing at line: ");
		startRowLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		startRowSpinner.setName("startRowSpinner");

		SpinnerNumberModel startRowSpinnerModel = new SpinnerNumberModel(1, 1,
				10000000, 1);
		startRowSpinner.setModel(startRowSpinnerModel);
		startRowSpinner
				.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
					public void mouseWheelMoved(
							java.awt.event.MouseWheelEvent evt) {
						startRowSpinnerMouseWheelMoved(evt);
					}
				});
		startRowSpinner
				.setToolTipText("<html>Start importing from this row. <p>"
						+ "(Click on the <strong><i>Refresh Preview</i></strong> button to refresh preview.)</p></html>");

		commentLineLabel.setText("  Ignore lines with:");

		commentLineTextField
				.setToolTipText("<html>Rows starting with this symbol or string will be ignored. <br>"
						+ "(Click on the <strong><i>Refresh Preview</i></strong> button to refresh preview.)</html>");

		GroupLayout headerRowsPanelLayout = new org.jdesktop.layout.GroupLayout(
				headerRowsPanel);
		headerRowsPanel.setLayout(headerRowsPanelLayout);

		headerRowsPanelLayout
				.setHorizontalGroup(headerRowsPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								headerRowsPanelLayout
										.createSequentialGroup()
										.add(transferNameCheckBox)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(startRowLabel)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												startRowSpinner,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
												51,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(commentLineLabel)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												commentLineTextField,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
												24,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
										.addContainerGap(
												GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));
		headerRowsPanelLayout
				.setVerticalGroup(headerRowsPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								headerRowsPanelLayout
										.createSequentialGroup()
										.add(
												headerRowsPanelLayout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(
																transferNameCheckBox)
														.add(startRowLabel)
														.add(startRowSpinner,
																21, 21, 21)
														.add(commentLineLabel)
														.add(
																commentLineTextField,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.addContainerGap(
												GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));

		org.jdesktop.layout.GroupLayout textImportOptionPanelLayout = new org.jdesktop.layout.GroupLayout(
				textImportOptionPanel);
		textImportOptionPanel.setLayout(textImportOptionPanelLayout);

		textImportOptionPanelLayout
				.setHorizontalGroup(textImportOptionPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								org.jdesktop.layout.GroupLayout.TRAILING,
								textImportOptionPanelLayout
										.createSequentialGroup()
										.add(
												delimiterPanel,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED))
						.add(
								org.jdesktop.layout.GroupLayout.TRAILING,
								textImportOptionPanelLayout
										.createSequentialGroup()
										.add(
												headerRowsPanel,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(reloadButton).addContainerGap()));
		textImportOptionPanelLayout
				.setVerticalGroup(textImportOptionPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								textImportOptionPanelLayout
										.createSequentialGroup()
										.add(
												textImportOptionPanelLayout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.LEADING)
														.add(
																delimiterPanel,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																49,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.add(
																org.jdesktop.layout.GroupLayout.TRAILING,
																reloadButton))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												textImportOptionPanelLayout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.LEADING)
														.add(
																headerRowsPanel,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																49,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))));

		org.jdesktop.layout.GroupLayout advancedPanelLayout = new org.jdesktop.layout.GroupLayout(
				advancedPanel);
		advancedPanel.setLayout(advancedPanelLayout);

		advancedPanelLayout
				.setHorizontalGroup(advancedPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								advancedPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.add(primaryLabel)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(primaryKeyComboBox, 0, 0,
												Short.MAX_VALUE)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(textImportCheckBox)).add(
								textImportOptionPanel,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE));
		advancedPanelLayout
				.setVerticalGroup(advancedPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								advancedPanelLayout
										.createSequentialGroup()
										.add(
												advancedPanelLayout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(primaryLabel)
														.add(
																primaryKeyComboBox,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.add(textImportCheckBox))
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												textImportOptionPanel,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));

		globalLayout();

		basicPanel.repaint();
		textImportOptionPanel.setVisible(false);

		pack();
	} // </editor-fold>

	/**
	 * Update UI based on the primary key selection.
	 * 
	 * @param evt
	 */
	private void primaryKeyComboBoxActionPerformed(ActionEvent evt) {
		// Update primary key index.
		keyInFile = primaryKeyComboBox.getSelectedIndex();

		// Update
		previewPanel.getPreviewTable().setDefaultRenderer(Object.class,
				getRenderer(previewPanel.getFileType()));

		try {
			setStatusBar(new URL(targetDataSourceTextField.getText()));

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		previewPanel.repaint();

		primaryKeyMap.put(previewPanel.getSelectedSheetName(),
				primaryKeyComboBox.getSelectedIndex());

		// Update table view
		ColumnResizer.adjustColumnPreferredWidths(previewPanel
				.getPreviewTable());
		previewPanel.getPreviewTable().repaint();
	}

	private void helpButtonActionPerformed(ActionEvent evt) {
		// TODO: Quick help should be implemented!
	}

	private void otherTextFieldActionPerformed(KeyEvent evt) throws IOException {
		if (otherCheckBox.isSelected()) {
			displayPreview();
		}
	}

	private void nodeKeyComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
		previewPanel.getPreviewTable().setDefaultRenderer(Object.class,
				getRenderer(previewPanel.getFileType()));

		setKeyList();
	}

	private void transferNameCheckBoxActionPerformed(
			java.awt.event.ActionEvent evt) {
		final DefaultTableModel model = (DefaultTableModel) previewPanel
				.getPreviewTable().getModel();

		if (transferNameCheckBox.isSelected()) {
			if ((previewPanel.getPreviewTable() != null) && (model != null)) {
				columnHeaders = new String[previewPanel.getPreviewTable()
						.getColumnCount()];

				for (int i = 0; i < columnHeaders.length; i++) {
					// Save the header
					columnHeaders[i] = previewPanel.getPreviewTable()
							.getColumnModel().getColumn(i).getHeaderValue()
							.toString();
					previewPanel.getPreviewTable().getColumnModel()
							.getColumn(i).setHeaderValue(
									(String) model.getValueAt(0, i));
				}

				model.removeRow(0);
				previewPanel.getPreviewTable().getTableHeader()
						.resizeAndRepaint();
			}

			startRowSpinner.setEnabled(false);
		} else {
			// Restore row
			String currentName = null;
			Object headerVal = null;

			for (int i = 0; i < columnHeaders.length; i++) {
				headerVal = previewPanel.getPreviewTable().getColumnModel()
						.getColumn(i).getHeaderValue();

				if (headerVal == null) {
					currentName = "";
				} else {
					currentName = headerVal.toString();
				}

				previewPanel.getPreviewTable().getColumnModel().getColumn(i)
						.setHeaderValue(columnHeaders[i]);
				columnHeaders[i] = currentName;
			}

			model.insertRow(0, columnHeaders);
			previewPanel.getPreviewTable().getTableHeader().resizeAndRepaint();
			startRowSpinner.setEnabled(true);
		}

		updatePrimaryKeyComboBox();
		repaint();
	}

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
		dispose();
	}

	/**
	 * Load from the data source.<br>
	 * 
	 * @param evt
	 * @throws Exception
	 */
	private void importButtonActionPerformed(ActionEvent evt) throws Exception {
		if (checkDataSourceError() == false)
			return;

		/*
		 * Get start line number. If "transfer" check box is true, then start
		 * reading from the second line.
		 */
		int startLineNumber;
		final int spinnerNumber = Integer.parseInt(startRowSpinner.getValue()
				.toString());

		if (transferNameCheckBox.isSelected()) {
			startLineNumber = spinnerNumber;
		} else {
			startLineNumber = spinnerNumber - 1;
		}

		/*
		 * Get import flags
		 */
		final int colCount = previewPanel.getPreviewTable().getColumnModel()
				.getColumnCount();
		importFlag = new boolean[colCount];

		for (int i = 0; i < colCount; i++) {
			importFlag[i] = ((AttributePreviewTableCellRenderer) previewPanel
					.getPreviewTable().getCellRenderer(0, i)).getImportFlag(i);
			// System.out.println("col"+i+": "+importFlag[i]);
		}

		/*
		 * Get Attribute Names
		 */
		final String[] attributeNames;
		final List<String> attrNameList = new ArrayList<String>();

		Object curName = null;

		for (int i = 0; i < colCount; i++) {
			curName = previewPanel.getPreviewTable().getColumnModel()
					.getColumn(i).getHeaderValue();

			if (attrNameList.contains(curName)) {
				int dupIndex = 0;

				for (int idx = 0; idx < attrNameList.size(); idx++) {
					if (curName.equals(attrNameList.get(idx))) {
						dupIndex = idx;

						break;
					}
				}

				if (importFlag[i] && importFlag[dupIndex]) {
					final JLabel label = new JLabel(
							"Duplicate Attribute Name Found: " + curName);
					label.setForeground(Color.RED);
					JOptionPane.showMessageDialog(this, label);

					return;
				}
			}

			if (curName == null) {
				attrNameList.add("Column " + i);
			} else {
				attrNameList.add(curName.toString());
			}
		}

		attributeNames = attrNameList.toArray(new String[0]);

		/*
		 * Get attribute data types
		 */
		final Byte[] test = previewPanel.getDataTypes(previewPanel
				.getSelectedSheetName());
		final Byte[] attributeTypes = new Byte[test.length];

		for (int i = 0; i < test.length; i++) {
			attributeTypes[i] = test[i];
		}

		// Extract URL from the text table.
		final URL source = new URL(targetDataSourceTextField.getText());
		GenMAPPImportCyCommandHandler.IMPORT_SOURCE_URL = source.toString();
		// Make sure primary key index is up-to-date.
		keyInFile = primaryKeyComboBox.getSelectedIndex();

		// Build mapping parameter object.
		final AttributeMappingParameters mapping;

		if (previewPanel.isCytoscapeAttributeFile(source)) {
			List<String> del = new ArrayList<String>();
			del.add(" += +");
			mapping = new AttributeMappingParameters(del, listDelimiter,
					keyInFile, attributeNames, attributeTypes, listDataTypes,
					importFlag);
		} else
			mapping = new AttributeMappingParameters(checkDelimiter(),
					listDelimiter, keyInFile, attributeNames, attributeTypes,
					listDataTypes, importFlag);

		if (source.toString().endsWith(EXCEL_EXT)) {
			/*
			 * Read one sheet at a time
			 */
			POIFSFileSystem excelIn = new POIFSFileSystem(source.openStream());
			HSSFWorkbook wb = new HSSFWorkbook(excelIn);

			// Load all sheets in the table
			for (int i = 0; i < wb.getNumberOfSheets(); i++) {
				HSSFSheet sheet = wb.getSheetAt(i);

				loadAnnotation(new ExcelAttributeSheetReader(sheet, mapping,
						startLineNumber), source.toString());

			}
		} else {
			loadAnnotation(new DefaultAttributeTableReader(source, mapping,
					startLineNumber, null), source.toString());

		}

		Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);

		dispose();
	}

	private void selectAttributeFileButtonActionPerformed(ActionEvent evt)
			throws IOException {
		final File[] multiSource = FileUtil.getFiles("Select local file",
				FileUtil.LOAD, new CyFileFilter[] {});

		if ((multiSource == null) || (multiSource[0] == null))
			return;

		// Pick the first one and show preview.
		this.inputFiles = multiSource;

		final File sourceFile = multiSource[0];

		targetDataSourceTextField
				.setText(sourceFile.toURI().toURL().toString());

		// Set tooltip as HTML
		StringBuilder builder = new StringBuilder();
		builder
				.append("<html><body><strong text=\"red\">File(s) to be imported</strong><ul>");

		for (int i = 0; i < multiSource.length; i++) {
			builder.append("<li>" + multiSource[i].getName() + "</li>");
		}

		builder.append("</ul></body></html>");
		targetDataSourceTextField.setToolTipText(builder.toString());

		final URL sourceURL = new URL(targetDataSourceTextField.getText());

		readAnnotationForPreview(sourceURL, checkDelimiter());

		if (previewPanel.getPreviewTable() == null) {
			JLabel label = new JLabel("File is broken or empty!");
			label.setForeground(Color.RED);
			JOptionPane.showMessageDialog(this, label);

			return;
		}

		columnHeaders = new String[previewPanel.getPreviewTable()
				.getColumnCount()];
		transferNameCheckBox.setEnabled(true);
		transferNameCheckBox.setSelected(true);
		this.transferNameCheckBoxActionPerformed(null);

		ColumnResizer.adjustColumnPreferredWidths(previewPanel
				.getPreviewTable());
		previewPanel.getPreviewTable().repaint();
	}

	private void delimiterCheckBoxActionPerformed(ActionEvent evt)
			throws IOException {
		// transferNameCheckBox.setSelected(false);
		// this.transferNameCheckBoxActionPerformed(null);
		displayPreview();
	}

	private void textImportCheckBoxActionPerformed(
			java.awt.event.ActionEvent evt) {
		if (textImportCheckBox.isSelected()) {
			textImportOptionPanel.setVisible(true);
		} else {
			textImportOptionPanel.setVisible(false);
		}

		pack();
	}

	private void reloadButtonActionPerformed(java.awt.event.ActionEvent evt)
			throws IOException {
		displayPreview();

		if (transferNameCheckBox.isSelected())
			this.transferNameCheckBoxActionPerformed(null);
	}

	/**
	 * Actions for mouse wheel movement
	 * 
	 * @param evt
	 */
	private void counterSpinnerMouseWheelMoved(
			java.awt.event.MouseWheelEvent evt) {
		JSpinner source = (JSpinner) evt.getSource();

		SpinnerNumberModel model = (SpinnerNumberModel) source.getModel();
		Integer oldValue = (Integer) source.getValue();
		int intValue = oldValue.intValue()
				- (evt.getWheelRotation() * model.getStepSize().intValue());
		Integer newValue = new Integer(intValue);

		if ((model.getMaximum().compareTo(newValue) >= 0)
				&& (model.getMinimum().compareTo(newValue) <= 0)) {
			source.setValue(newValue);
		}
	}

	/**
	 * Actions for selecting start line.
	 * 
	 * @param evt
	 */
	private void startRowSpinnerMouseWheelMoved(
			java.awt.event.MouseWheelEvent evt) {
		JSpinner source = (JSpinner) evt.getSource();

		SpinnerNumberModel model = (SpinnerNumberModel) source.getModel();
		Integer oldValue = (Integer) source.getValue();
		int intValue = oldValue.intValue()
				- (evt.getWheelRotation() * model.getStepSize().intValue());
		Integer newValue = new Integer(intValue);

		if ((model.getMaximum().compareTo(newValue) >= 0)
				&& (model.getMinimum().compareTo(newValue) <= 0)) {
			source.setValue(newValue);
		}
	}

	/**
	 * @throws IOException
	 */
	private void displayPreview() throws IOException {
		final String selectedSourceName;
		final URL sourceURL;

		selectedSourceName = targetDataSourceTextField.getText();
		sourceURL = new URL(selectedSourceName);

		readAnnotationForPreview(sourceURL, checkDelimiter());
		previewPanel.repaint();
	}

	/**
	 * @throws JAXBException
	 * @throws IOException
	 */
	private void updateComponents() throws JAXBException, IOException {

		setTitle("GenMAPP Data Import");
		titleLabel.setText("Import GenMAPP Data");

		reloadButton.setEnabled(false);
		startRowSpinner.setEnabled(false);
		startRowLabel.setEnabled(false);
		previewPanel.getPreviewTable().getTableHeader().setReorderingAllowed(
				false);
		setRadioButtonGroup();
		pack();

		updateMappingAttributeComboBox();

		setStatusBar("-", "-", "File Size: Unknown");
	}

	/**
	 * 
	 */
	private void updatePrimaryKeyComboBox() {
		final DefaultTableModel model = (DefaultTableModel) previewPanel
				.getPreviewTable().getModel();

		primaryKeyComboBox
				.setRenderer(new ComboBoxRenderer(attributeDataTypes));

		if ((model != null) && (model.getColumnCount() > 0)) {
			primaryKeyComboBox.removeAllItems();

			Object curValue = null;

			for (int i = 0; i < model.getColumnCount(); i++) {
				curValue = previewPanel.getPreviewTable().getColumnModel()
						.getColumn(i).getHeaderValue();

				if (curValue != null) {
					primaryKeyComboBox.addItem(curValue.toString());
				} else {
					primaryKeyComboBox.addItem("");
				}
			}
		}

		primaryKeyComboBox.setEnabled(true);

		Integer selectedIndex = primaryKeyMap.get(previewPanel
				.getSelectedSheetName());

		if (selectedIndex == null) {
			primaryKeyComboBox.setSelectedIndex(0);
		} else {
			primaryKeyComboBox.setSelectedIndex(selectedIndex);
		}
	}

	/**
	 * @param dataType
	 * @return
	 */
	protected static ImageIcon getDataTypeIcon(byte dataType) {
		ImageIcon dataTypeIcon = null;

		if (dataType == CyAttributes.TYPE_STRING) {
			dataTypeIcon = STRING_ICON.getIcon();
		} else if (dataType == CyAttributes.TYPE_INTEGER) {
			dataTypeIcon = INT_ICON.getIcon();
		} else if (dataType == CyAttributes.TYPE_FLOATING) {
			dataTypeIcon = FLOAT_ICON.getIcon();
		} else if (dataType == CyAttributes.TYPE_BOOLEAN) {
			dataTypeIcon = BOOLEAN_ICON.getIcon();
		} else if (dataType == CyAttributes.TYPE_SIMPLE_LIST) {
			dataTypeIcon = LIST_ICON.getIcon();
		}

		return dataTypeIcon;
	}

	/**
	 * 
	 */
	private void setRadioButtonGroup() {
		attrTypeButtonGroup.add(nodeRadioButton);
		attrTypeButtonGroup.add(edgeRadioButton);
		attrTypeButtonGroup.add(networkRadioButton);
		attrTypeButtonGroup.setSelected(nodeRadioButton.getModel(), true);

		importTypeButtonGroup.add(showAllRadioButton);
		importTypeButtonGroup.add(counterRadioButton);
		importTypeButtonGroup.setSelected(counterRadioButton.getModel(), true);

		tabCheckBox.setSelected(true);
		tabCheckBox.setEnabled(false);
		commaCheckBox.setEnabled(false);

		spaceCheckBox.setEnabled(false);

		spaceCheckBox.setSelected(false);

		semicolonCheckBox.setEnabled(false);
		otherCheckBox.setEnabled(false);
		otherDelimiterTextField.setEnabled(false);
	}

	/**
	 * Generate preview table.
	 * 
	 * @throws IOException
	 */
	private void readAnnotationForPreview(URL sourceURL, List<String> delimiters)
			throws IOException {
		/*
		 * Check number of lines we should load. if -1, load everything in the
		 * file.
		 */
		final int previewSize;

		if (showAllRadioButton.isSelected()) {
			previewSize = -1;
		} else {
			previewSize = Integer
					.parseInt(counterSpinner.getValue().toString());
		}

		/*
		 * Load data from the given URL.
		 */
		final String commentChar = commentLineTextField.getText();
		final int startLine = Integer.parseInt(startRowSpinner.getValue()
				.toString());
		previewPanel.setPreviewTable(sourceURL, delimiters, null, previewSize,
				commentChar, startLine - 1);

		if (previewPanel.getPreviewTable() == null) {
			return;
		}

		// Initialize import flags.
		final int colSize = previewPanel.getPreviewTable().getColumnCount();
		importFlag = new boolean[colSize];

		for (int i = 0; i < colSize; i++) {
			importFlag[i] = true;
		}

		listDataTypes = previewPanel.getCurrentListDataTypes();

		/*
		 * If this is not an Excel file, enable delimiter checkboxes.
		 */
		// FileTypes type = checkFileType(sourceURL);
		if (sourceURL.toString().endsWith(EXCEL_EXT) == false) {
			switchDelimiterCheckBoxes(true);
		}

		/*
		 * Set Status bar
		 */
		setStatusBar(sourceURL);

		pack();
		repaint();

		reloadButton.setEnabled(true);
		startRowSpinner.setEnabled(true);
		startRowLabel.setEnabled(true);
	}

	private void switchDelimiterCheckBoxes(Boolean state) {
		tabCheckBox.setEnabled(state);
		commaCheckBox.setEnabled(state);
		spaceCheckBox.setEnabled(state);
		semicolonCheckBox.setEnabled(state);
		otherCheckBox.setEnabled(state);
		otherDelimiterTextField.setEnabled(state);
	}

	private void setStatusBar(URL sourceURL) {
		final String centerMessage;
		final String rightMessage;

		if (showAllRadioButton.isSelected()) {
			centerMessage = "All entries are loaded for preview.";
		} else {
			centerMessage = "First " + counterSpinner.getValue().toString()
					+ " entries are loaded for preview.";
		}

		if (sourceURL.toString().startsWith("file:")) {
			int fileSize = 0;

			try {
				BufferedInputStream fis = (BufferedInputStream) sourceURL
						.openStream();
				fileSize = fis.available();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if ((fileSize / 1000) == 0) {
				rightMessage = "File Size: " + fileSize + " Bytes";
			} else {
				rightMessage = "File Size: " + (fileSize / 1000) + " KBytes";
			}
		} else {
			rightMessage = "File Size Unknown (Remote Data Source)";
		}

		setStatusBar("Key Matched: "
				+ previewPanel.checkKeyMatch(primaryKeyComboBox
						.getSelectedIndex()), centerMessage, rightMessage);
	}

	/**
	 * Update the list of mapping attributes.
	 * 
	 */
	private void setKeyList() {
		if (mappingAttributeComboBox.getSelectedItem() == null) {
			return;
		}

		String selectedKeyAttribute = mappingAttributeComboBox
				.getSelectedItem().toString();

		Iterator<Node> it;

		Set<Object> valueSet = new TreeSet<Object>();

		if (selectedKeyAttribute.equals(ID)) {
			it = Cytoscape.getRootGraph().nodesIterator();

			while (it.hasNext()) {
				Node node = (Node) it.next();
				valueSet.add(node.getIdentifier());
			}

		} else {
			final byte attrType = selectedAttributes
					.getType(selectedKeyAttribute);

			Object value = null;

			Iterator attrIt = selectedAttributes.getMultiHashMap()
					.getObjectKeys(selectedKeyAttribute);

			String stringValue = null;
			Double dblValue = 0.;
			Integer intValue = 0;
			Boolean boolValue = false;
			List listValue = null;

			while (attrIt.hasNext()) {
				value = attrIt.next();

				switch (attrType) {
				case CyAttributes.TYPE_STRING:
					stringValue = selectedAttributes.getStringAttribute(
							(String) value, selectedKeyAttribute);
					valueSet.add(stringValue);

					break;

				case CyAttributes.TYPE_FLOATING:
					dblValue = selectedAttributes.getDoubleAttribute(
							(String) value, selectedKeyAttribute);
					valueSet.add(dblValue);

					break;

				case CyAttributes.TYPE_INTEGER:
					intValue = selectedAttributes.getIntegerAttribute(
							(String) value, selectedKeyAttribute);
					valueSet.add(intValue);

					break;

				case CyAttributes.TYPE_BOOLEAN:
					boolValue = selectedAttributes.getBooleanAttribute(
							(String) value, selectedKeyAttribute);
					valueSet.add(boolValue);

					break;

				case CyAttributes.TYPE_SIMPLE_LIST:
					listValue = selectedAttributes.getListAttribute(
							(String) value, selectedKeyAttribute);
					valueSet.addAll(listValue);

					break;

				default:
					break;
				}
			}
		}

		previewPanel.setKeyAttributeList(valueSet);

		// nodeKeyList.setListData(valueSet.toArray());
	}

	private void updateMappingAttributeComboBox() {
		mappingAttributeComboBox.removeAllItems();

		final ListCellRenderer lcr = mappingAttributeComboBox.getRenderer();
		mappingAttributeComboBox.setRenderer(new ListCellRenderer() {
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel cmp = (JLabel) lcr.getListCellRendererComponent(list,
						value, index, isSelected, cellHasFocus);

				if (value.equals(ID)) {
					cmp.setIcon(ID_ICON.getIcon());
				} else {
					cmp.setIcon(getDataTypeIcon(selectedAttributes
							.getType(value.toString())));
				}

				return cmp;
			}
		});

		mappingAttributeComboBox.addItem(ID);

		for (String name : selectedAttributes.getAttributeNames()) {
			if ((selectedAttributes.getType(name) != CyAttributes.TYPE_COMPLEX)
					&& (selectedAttributes.getType(name) != CyAttributes.TYPE_SIMPLE_MAP)
					&& (selectedAttributes.getType(name) != CyAttributes.TYPE_UNDEFINED)) {
				mappingAttributeComboBox.addItem(name);
			}
		}
	}

	private TableCellRenderer getRenderer(FileTypes type) {
		final TableCellRenderer rend;

		rend = new AttributePreviewTableCellRenderer(keyInFile, importFlag,
				listDelimiter);

		return rend;
	}

	/**
	 * Create task for annotation reader and run it. tablechanged
	 * 
	 * @param reader
	 * @param ontology
	 * @param source
	 */
	private void loadAnnotation(TextTableReader reader, String source) {
		// Create LoadNetwork Task
		ImportAttributeTableTask task = new ImportAttributeTableTask(reader,
				source);

		// Configure JTask Dialog Pop-Up Box
		JTaskConfig jTaskConfig = new JTaskConfig();
		jTaskConfig.setOwner(Cytoscape.getDesktop());
		jTaskConfig.displayCloseButton(true);
		jTaskConfig.displayStatus(true);
		jTaskConfig.setAutoDispose(false);

		// Execute Task in New Thread; pops open JTask Dialog Box.
		TaskManager.executeTask(task, jTaskConfig);
	}

	private void setStatusBar(String message1, String message2, String message3) {
		statusBar.setLeftLabel(message1);
		statusBar.setCenterLabel(message2);
		statusBar.setRightLabel(message3);
	}

	private List<String> checkDelimiter() {
		final List<String> delList = new ArrayList<String>();

		if (tabCheckBox.isSelected()) {
			delList.add(TextFileDelimiters.TAB.toString());
		}

		if (commaCheckBox.isSelected()) {
			delList.add(TextFileDelimiters.COMMA.toString());
		}

		if (spaceCheckBox.isSelected()) {
			delList.add(TextFileDelimiters.SPACE.toString());
		}

		if (semicolonCheckBox.isSelected()) {
			delList.add(TextFileDelimiters.SEMICOLON.toString());
		}

		if (otherCheckBox.isSelected()) {
			delList.add(otherDelimiterTextField.getText());
		}

		return delList;
	}

	/**
	 * Error checker for imput table.<br>
	 * 
	 * @return true if table looks OK.
	 */
	private boolean checkDataSourceError() {
		/*
		 * Number of ENABLED columns should be 2 or more.
		 */
		final JTable table = previewPanel.getPreviewTable();

		if ((table == null) || (table.getModel() == null)
				|| (table.getColumnCount() == 0)) {
			JOptionPane.showMessageDialog(this, "No table selected.",
					"Invalid Table!", JOptionPane.INFORMATION_MESSAGE);

			return false;
		} else if (table.getColumnCount() < 2) {
			JOptionPane.showMessageDialog(this,
					"Table should contain at least 2 columns.",
					"Invalid Table!", JOptionPane.INFORMATION_MESSAGE);

			return false;
		}

		return true;
	}

	/*
	 * Layout Information for the entire dialog.<br>
	 * 
	 * <p> This layout will be switched by dialog type parameter. </p>
	 */
	private void globalLayout() {
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);

		/*
		 * Case 1: Simple Attribute Import
		 */
		layout
				.setHorizontalGroup(layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								layout
										.createSequentialGroup()
										.addContainerGap()
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.LEADING)
														.add(
																org.jdesktop.layout.GroupLayout.TRAILING,
																layout
																		.createSequentialGroup()
																		.add(
																				statusBar,
																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																				Short.MAX_VALUE)
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.RELATED)
																		.add(
																				importButton)
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.RELATED)
																		.add(
																				cancelButton))
														.add(
																previewPanel,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.add(
																org.jdesktop.layout.GroupLayout.TRAILING,
																advancedPanel,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.add(
																basicPanel,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.add(
																org.jdesktop.layout.GroupLayout.TRAILING,
																layout
																		.createSequentialGroup()
																		.add(
																				titleIconLabel1)
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.RELATED)
																		.add(
																				titleIconLabel2)
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.RELATED)
																		.add(
																				titleIconLabel3)
																		.add(
																				20,
																				20,
																				20)
																		.add(
																				titleLabel)
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.RELATED,
																				350,
																				Short.MAX_VALUE)
																		.add(
																				helpButton,
																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																				org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
														.add(
																titleSeparator,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																700,
																Short.MAX_VALUE))
										.addContainerGap()));
		layout
				.setVerticalGroup(layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								layout
										.createSequentialGroup()
										.addContainerGap()
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.TRAILING)
														.add(
																layout
																		.createSequentialGroup()
																		.add(
																				layout
																						.createParallelGroup(
																								org.jdesktop.layout.GroupLayout.BASELINE)
																						.add(
																								helpButton,
																								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																								20,
																								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
																						.add(
																								titleIconLabel1)
																						.add(
																								titleIconLabel2)
																						.add(
																								titleIconLabel3))
																		.add(
																				2,
																				2,
																				2))
														.add(
																layout
																		.createSequentialGroup()
																		.add(
																				titleLabel)
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.RELATED)))
										.add(
												titleSeparator,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
												10,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												basicPanel,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												advancedPanel,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												previewPanel,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.LEADING,
																false)
														.add(
																layout
																		.createParallelGroup(
																				org.jdesktop.layout.GroupLayout.BASELINE)
																		.add(
																				cancelButton)
																		.add(
																				importButton))
														.add(
																statusBar,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
										.addContainerGap()));
		pack();

	}

	// Variables declaration
	private javax.swing.JPanel advancedPanel;

	private javax.swing.JCheckBox transferNameCheckBox;

	private javax.swing.ButtonGroup attrTypeButtonGroup;

	private javax.swing.JPanel basicPanel;

	private javax.swing.JButton cancelButton;

	private javax.swing.JCheckBox commaCheckBox;

	private javax.swing.JPanel delimiterPanel;

	private javax.swing.JRadioButton edgeRadioButton;

	private javax.swing.JButton helpButton;

	private javax.swing.JButton importButton;

	private javax.swing.JRadioButton networkRadioButton;

	private javax.swing.JComboBox mappingAttributeComboBox;

	private javax.swing.JRadioButton nodeRadioButton;

	private javax.swing.JTextField otherDelimiterTextField;

	private javax.swing.JCheckBox otherCheckBox;

	private PreviewTablePanel previewPanel;

	private javax.swing.JButton selectAttributeFileButton;

	private javax.swing.JCheckBox semicolonCheckBox;

	private javax.swing.JCheckBox spaceCheckBox;

	private javax.swing.JCheckBox tabCheckBox;

	private javax.swing.JTextField targetDataSourceTextField;

	private javax.swing.JCheckBox textImportCheckBox;

	private javax.swing.JPanel textImportOptionPanel;

	private javax.swing.JLabel titleIconLabel1;

	private javax.swing.JLabel titleIconLabel2;

	private javax.swing.JLabel titleIconLabel3;

	private javax.swing.JLabel titleLabel;

	private javax.swing.JSeparator titleSeparator;

	private JComboBox primaryKeyComboBox;

	private JLabel primaryLabel;

	private javax.swing.JRadioButton showAllRadioButton;

	private javax.swing.JLabel counterLabel;

	private javax.swing.JRadioButton counterRadioButton;

	private javax.swing.JButton reloadButton;

	private javax.swing.JSpinner counterSpinner;

	private javax.swing.ButtonGroup importTypeButtonGroup;

	private JPanel headerRowsPanel;

	private JLabel defaultInteractionLabel;

	private JTextField defaultInteractionTextField;

	private JLabel startRowLabel;

	private JSpinner startRowSpinner;

	private JLabel commentLineLabel;

	private JTextField commentLineTextField;

	// End of variables declaration
	JStatusBar statusBar;

}

@SuppressWarnings("serial")
class ComboBoxRenderer extends JLabel implements ListCellRenderer {
	private List<Byte> attributeDataTypes;

	/**
	 * Creates a new ComboBoxRenderer object.
	 * 
	 * @param attributeDataTypes
	 *            DOCUMENT ME!
	 */
	public ComboBoxRenderer(List<Byte> attributeDataTypes) {
		this.attributeDataTypes = attributeDataTypes;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param list
	 *            DOCUMENT ME!
	 * @param value
	 *            DOCUMENT ME!
	 * @param index
	 *            DOCUMENT ME!
	 * @param isSelected
	 *            DOCUMENT ME!
	 * @param cellHasFocus
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		setText(value.toString());

		if ((attributeDataTypes != null) && (attributeDataTypes.size() != 0)
				&& (index < attributeDataTypes.size()) && (index >= 0)) {
			final Byte dataType = attributeDataTypes.get(index);

			if (dataType == null) {
				setIcon(null);
			} else {
				setIcon(ImportTextTableDialog.getDataTypeIcon(dataType));
			}
		} else if ((attributeDataTypes != null)
				&& (attributeDataTypes.size() != 0)
				&& (index < attributeDataTypes.size())) {
			setIcon(ImportTextTableDialog.getDataTypeIcon(attributeDataTypes
					.get(list.getSelectedIndex())));
		}

		return this;
	}
}

