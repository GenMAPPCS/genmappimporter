/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.genmapp.genmappimport.ui;

import cytoscape.Cytoscape;

import cytoscape.data.CyAttributes;

import cytoscape.util.URLUtil;

import cytoscape.util.swing.ColumnResizer;

import static org.genmapp.genmappimport.ui.theme.ImportDialogColorTheme.ALIAS_COLOR;
import static org.genmapp.genmappimport.ui.theme.ImportDialogColorTheme.ONTOLOGY_COLOR;
import static org.genmapp.genmappimport.ui.theme.ImportDialogColorTheme.PRIMARY_KEY_COLOR;
import static org.genmapp.genmappimport.ui.theme.ImportDialogColorTheme.SPECIES_COLOR;
import static org.genmapp.genmappimport.ui.theme.ImportDialogFontTheme.LABEL_FONT;
import static org.genmapp.genmappimport.ui.theme.ImportDialogIconSets.RIGHT_ARROW_ICON;
import static org.genmapp.genmappimport.ui.theme.ImportDialogIconSets.SPREADSHEET_ICON;
import static org.genmapp.genmappimport.ui.theme.ImportDialogIconSets.TEXT_FILE_ICON;

import org.apache.poi.hssf.model.Model;
import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSDocument;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import org.genmapp.genmappimport.reader.TextFileDelimiters;
import org.genmapp.genmappimport.ui.ImportTextTableDialog.FileTypes;
import org.jdesktop.layout.GroupLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * General purpose preview table panel.
 * 
 * @author kono
 * 
 */
public class PreviewTablePanel extends JPanel {
	/*
	 * Define type of preview.
	 */

	/**
	 *
	 */
	public static final int ATTRIBUTE_PREVIEW = 1;

	/**
	 *
	 */
	public static final int ONTOLOGY_PREVIEW = 2;

	/**
	 *
	 */
	public static final int NETWORK_PREVIEW = 3;

	/*
	 * Default messages
	 */
	private static final String DEF_MESSAGE = "Legend:";
	private static final String DEF_TAB_MESSAGE = "Data File Preview Window";
	private static final String EXCEL_EXT = ".xls";

	// Lines start with this char will be ignored.
	private String commentChar;
	private final String message;
	private boolean loadFlag = false;

	// Tracking attribute data type.
	// private Byte[] dataTypes;
	private Map<String, Byte[]> dataTypeMap;
	private Map<String, Byte[]> listDataTypeMap;

	/*
	 * GUI Components
	 */
	private JLabel legendLabel;
	private javax.swing.JLabel aliasLabel;
	private javax.swing.JLabel primaryKeyLabel;
	private JLabel ontologyTermLabel;
	private JLabel taxonomyLabel;
	private javax.swing.JLabel instructionLabel;
	private JLabel rightArrowLabel;
	private JLabel fileTypeLabel;
	private JScrollPane previewScrollPane;
	private JTable previewTable;

	// Tables for each worksheet.
	private Map<String, FileTypes> fileTypes;
	private Map<String, JTable> previewTables;
	private JTabbedPane tableTabbedPane;
	private JScrollPane keyPreviewScrollPane;
	private JList keyPreviewList;
	private DefaultListModel keyListModel;
	private PropertyChangeSupport changes = new PropertyChangeSupport(this);
	private int panelType;
	private String listDelimiter;

	/**
	 * Creates a new PreviewTablePanel object.
	 */
	public PreviewTablePanel() {
		this(DEF_MESSAGE, ATTRIBUTE_PREVIEW);
	}

	/**
	 * Creates a new PreviewTablePanel object.
	 * 
	 * @param message
	 *            DOCUMENT ME!
	 */
	public PreviewTablePanel(String message) {
		this(message, ATTRIBUTE_PREVIEW);
	}

	/**
	 * Creates a new PreviewTablePanel object.
	 * 
	 * @param message
	 *            DOCUMENT ME!
	 * @param panelType
	 *            DOCUMENT ME!
	 */
	public PreviewTablePanel(String message, int panelType) {
		if (message == null) {
			this.message = DEF_MESSAGE;
		} else {
			this.message = message;
		}

		this.panelType = panelType;

		dataTypeMap = new HashMap<String, Byte[]>();
		listDataTypeMap = new HashMap<String, Byte[]>();

		// This object will track the file types of each table.
		fileTypes = new HashMap<String, FileTypes>();

		initComponents();

		hideUnnecessaryComponents();
	}

	private void hideUnnecessaryComponents() {
		fileTypeLabel.setVisible(false);

		if (panelType == NETWORK_PREVIEW) {
			keyPreviewScrollPane.setVisible(false);
			rightArrowLabel.setVisible(false);
			legendLabel.setVisible(false);
			primaryKeyLabel.setVisible(false);
			aliasLabel.setVisible(false);
			ontologyTermLabel.setVisible(false);
			taxonomyLabel.setVisible(false);
		} else if (panelType == ATTRIBUTE_PREVIEW) {
			ontologyTermLabel.setVisible(false);
			taxonomyLabel.setVisible(false);
		}

		repaint();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param data
	 *            DOCUMENT ME!
	 */
	public void setKeyAttributeList(Set data) {
		keyPreviewScrollPane.setBackground(Color.white);
		keyListModel.clear();

		for (Object item : data) {
			keyListModel.addElement(item);
		}

		keyPreviewList.repaint();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param l
	 *            DOCUMENT ME!
	 */
	public void addPropertyChangeListener(PropertyChangeListener l) {
		changes.addPropertyChangeListener(l);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param l
	 *            DOCUMENT ME!
	 */
	public void removePropertyChangeListener(PropertyChangeListener l) {
		changes.removePropertyChangeListener(l);
	}

	private void initComponents() {
		legendLabel = new JLabel();
		instructionLabel = new javax.swing.JLabel();

		primaryKeyLabel = new javax.swing.JLabel();
		aliasLabel = new javax.swing.JLabel();
		ontologyTermLabel = new JLabel();
		taxonomyLabel = new JLabel();
		previewScrollPane = new JScrollPane();
		rightArrowLabel = new JLabel();
		tableTabbedPane = new JTabbedPane();
		keyListModel = new DefaultListModel();
		keyPreviewList = new JList(keyListModel);
		keyPreviewScrollPane = new JScrollPane();

		previewTables = new HashMap<String, JTable>();
		previewTable = new JTable();
		previewTable.setName("previewTable");
		previewTable.setOpaque(false);
		previewTable.setBackground(Color.white);

		fileTypeLabel = new JLabel();
		fileTypeLabel.setFont(new Font("Sans-Serif", Font.BOLD, 14));

		keyPreviewScrollPane.setBorder(javax.swing.BorderFactory
				.createTitledBorder("Key Attributes"));

		keyPreviewList.setOpaque(false);
		keyPreviewList.setCellRenderer(new KeyAttributeListRenderer());
		keyPreviewScrollPane.setViewportView(keyPreviewList);

		previewScrollPane.setOpaque(false);
		previewScrollPane.setViewportView(previewTable);
		previewScrollPane.setBackground(Color.WHITE);

		final BufferedImage datasourceImage = getBufferedImage(Cytoscape.class
				.getResource("images/ximian/data_sources_trans.png"));

		final BufferedImage bi = getBufferedImage(Cytoscape.class
				.getResource("images/icon100_trans.png"));

		tableTabbedPane.setBackground(Color.white);
		tableTabbedPane
				.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(javax.swing.event.ChangeEvent evt) {
						tableTabbedPaneStateChanged(evt);
					}
				});

		previewScrollPane.getViewport().setOpaque(false);
		previewScrollPane.setViewportBorder(new CentredBackgroundBorder(
				datasourceImage));
		keyPreviewScrollPane.getViewport().setOpaque(false);
		keyPreviewScrollPane.setViewportBorder(new CentredBackgroundBorder(bi));

		tableTabbedPane.addTab(DEF_TAB_MESSAGE, previewScrollPane);

		rightArrowLabel.setIcon(RIGHT_ARROW_ICON.getIcon());

		JTableHeader hd = previewTable.getTableHeader();
		hd.setReorderingAllowed(false);
		hd
				.setDefaultRenderer(new HeaderRenderer(hd.getDefaultRenderer(),
						null));

		/*
		 * Setting table properties
		 */
		previewTable.setCellSelectionEnabled(false);
		previewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		previewTable.setDefaultEditor(Object.class, null);

		this
				.setBorder(BorderFactory
						.createTitledBorder(javax.swing.BorderFactory
								.createTitledBorder(
										null,
										"Preview",
										javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
										javax.swing.border.TitledBorder.DEFAULT_POSITION,
										new java.awt.Font("Dialog", 1, 11))));

		// instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		// instructionLabel.setText(
		// "Left Click: Enable/Disable Column, Right Click: Edit Column");
		// instructionLabel.setFont(LABEL_FONT.getFont());
		// instructionLabel.setForeground(Color.red);

		// legendLabel.setFont(LABEL_FONT.getFont());
		// legendLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		// legendLabel.setText(message);
		//
		// primaryKeyLabel.setFont(LABEL_FONT.getFont());
		// primaryKeyLabel.setForeground(Color.WHITE);
		// primaryKeyLabel.setBackground(PRIMARY_KEY_COLOR.getColor());
		// primaryKeyLabel.setHorizontalAlignment(javax.swing.SwingConstants.
		// CENTER);
		// // onLabel
		// // .setIcon(CHECKED_ICON.getIcon());
		// primaryKeyLabel.setText("Key");
		// primaryKeyLabel.setToolTipText(
		// "Column in this color is the Primary Key.");
		// // onLabel.setBorder(new javax.swing.border.LineBorder(new
		// // java.awt.Color(
		// // 0, 0, 0), 1, true));
		// primaryKeyLabel.setOpaque(true);
		//
		// aliasLabel.setFont(LABEL_FONT.getFont());
		// aliasLabel.setForeground(Color.WHITE);
		// aliasLabel.setBackground(ALIAS_COLOR.getColor());
		// aliasLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		// // offLabel
		// // .setIcon(UNCHECKED_ICON.getIcon());
		// aliasLabel.setText("Alias");
		// aliasLabel.setToolTipText("Columns in this color are Aliases.");
		// // offLabel.setBorder(new javax.swing.border.LineBorder(
		// // new java.awt.Color(0, 0, 0), 1, true));
		// aliasLabel.setOpaque(true);
		//
		// ontologyTermLabel.setFont(LABEL_FONT.getFont());
		// ontologyTermLabel.setForeground(Color.WHITE);
		// ontologyTermLabel.setBackground(ONTOLOGY_COLOR.getColor());
		// ontologyTermLabel.setHorizontalAlignment(javax.swing.SwingConstants.
		// CENTER);
		// ontologyTermLabel.setText("Ontology");
		// ontologyTermLabel.setToolTipText(
		// "Column in this color is Ontology Term.");
		// ontologyTermLabel.setOpaque(true);
		//
		// taxonomyLabel.setFont(LABEL_FONT.getFont());
		// taxonomyLabel.setForeground(Color.WHITE);
		// taxonomyLabel.setBackground(SPECIES_COLOR.getColor());
		//taxonomyLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER
		// );
		// taxonomyLabel.setText("Taxon");
		// taxonomyLabel.setToolTipText(
		// "Columns in this color is Taxon (for Gene Association files only).");
		// taxonomyLabel.setOpaque(true);

		GroupLayout previewPanelLayout = new GroupLayout(this);
		this.setLayout(previewPanelLayout);

		previewPanelLayout.setHorizontalGroup(previewPanelLayout
				.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(
						org.jdesktop.layout.GroupLayout.TRAILING,
						previewPanelLayout.createSequentialGroup().add(
								tableTabbedPane,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								250, Short.MAX_VALUE)));
		previewPanelLayout
				.setVerticalGroup(previewPanelLayout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								previewPanelLayout
										.createSequentialGroup()
										.add(
												previewPanelLayout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.TRAILING)
														.add(
																tableTabbedPane,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																200,
																Short.MAX_VALUE))));
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public JTable getPreviewTable() {
		JScrollPane selected = (JScrollPane) tableTabbedPane
				.getSelectedComponent();

		if (selected == null) {
			return null;
		}

		return (JTable) selected.getViewport().getComponent(0);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public int getTableCount() {
		return tableTabbedPane.getTabCount();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param index
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getSheetName(int index) {
		return tableTabbedPane.getTitleAt(index);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param selectedTabName
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Byte[] getDataTypes(final String selectedTabName) {
		return dataTypeMap.get(selectedTabName);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Byte[] getCurrentDataTypes() {
		return dataTypeMap.get(getSelectedSheetName());
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Byte[] getCurrentListDataTypes() {
		return listDataTypeMap.get(getSelectedSheetName());
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public FileTypes getFileType() {
		final String sheetName = getSheetName(tableTabbedPane
				.getSelectedIndex());

		return FileTypes.ATTRIBUTE_FILE;
	}

	/**
	 * Get selected tab name.
	 * 
	 * @return name of the selected tab (i.e., sheet name)
	 */
	public String getSelectedSheetName() {
		return tableTabbedPane.getTitleAt(tableTabbedPane.getSelectedIndex());
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param index
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public JTable getPreviewTable(int index) {
		JScrollPane selected = (JScrollPane) tableTabbedPane
				.getComponentAt(index);

		return (JTable) selected.getViewport().getComponent(0);
	}

	private void tableTabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {
		if ((tableTabbedPane.getSelectedComponent() != null)
				&& (((JScrollPane) tableTabbedPane.getSelectedComponent())
						.getViewport().getComponent(0) != null)
				&& (loadFlag == true)) {
			changes.firePropertyChange(ImportTextTableDialog.SHEET_CHANGED,
					null, null);
		}
	}

	/**
	 * Get backgroung images for table & list.
	 * 
	 * @param url
	 * @return
	 */
	private BufferedImage getBufferedImage(URL url) {
		BufferedImage image;

		try {
			image = ImageIO.read(url);
		} catch (IOException ioe) {
			ioe.printStackTrace();

			return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		}

		return image;
	}

	/**
	 * Load file and show preview.
	 * 
	 * @param sourceURL
	 * @param delimiters
	 * @param renderer
	 *            renderer for this table. Can be null.
	 * @param size
	 * @param commentLineChar
	 *            TODO
	 * @param startLine
	 *            TODO
	 * @throws IOException
	 */
	public void setPreviewTable(URL sourceURL, List<String> delimiters,
			TableCellRenderer renderer, int size, final String commentLineChar,
			final int startLine) throws IOException {
		TableCellRenderer curRenderer = renderer;

		if ((commentLineChar != null) && (commentLineChar.trim().length() != 0)) {
			this.commentChar = commentLineChar;
		}

		/*
		 * If rendrer is null, create default one.
		 */
		if (curRenderer == null) {
			curRenderer = new AttributePreviewTableCellRenderer(0,
					new ArrayList<Integer>(),
					AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST,
					AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST,
					null, TextFileDelimiters.PIPE.toString());
		}

		/*
		 * Reset current state
		 */
		for (int i = 0; i < tableTabbedPane.getTabCount(); i++) {
			tableTabbedPane.removeTabAt(i);
		}

		previewTables = new HashMap<String, JTable>();

		TableModel newModel;

		fileTypeLabel.setVisible(true);

		if (sourceURL.toString().endsWith(EXCEL_EXT)) {
			fileTypeLabel.setIcon(SPREADSHEET_ICON.getIcon());
			fileTypeLabel.setText("Excel" + '\u2122' + " Workbook");

			POIFSFileSystem excelIn = new POIFSFileSystem(sourceURL
					.openStream());
			HSSFWorkbook wb = new HSSFWorkbook(excelIn, true);

			if (wb.getNumberOfSheets() == 0) {
				return;
			}

			/*
			 * Load each sheet in the workbook.
			 */
			System.out.println("# of Sheets = " + wb.getNumberOfSheets());

			HSSFSheet sheet = wb.getSheetAt(0);
			System.out.println("Sheet name = " + wb.getSheetName(0)
					+ ", ROW = " + sheet.rowIterator().hasNext());

			System.out.println("TS = " + sheet.toString());

			newModel = parseExcel(sourceURL, size, curRenderer, sheet,
					startLine);

			if (newModel.getRowCount() == 0) {
				return;
			}

			guessDataTypes(newModel, wb.getSheetName(0));
			listDataTypeMap
					.put(wb.getSheetName(0), initListDataTypes(newModel));
			addTableTab(newModel, wb.getSheetName(0), curRenderer);
		} else {
			if (isCytoscapeAttributeFile(sourceURL)) {
				fileTypeLabel.setText("Cytoscape Attribute File");
				fileTypeLabel.setIcon(new ImageIcon(Cytoscape.class
						.getResource("images/icon48.png")));
				newModel = parseText(sourceURL, size, curRenderer, null, 1);
			} else {
				fileTypeLabel.setText("Text File");
				fileTypeLabel.setIcon(TEXT_FILE_ICON.getIcon());
				newModel = parseText(sourceURL, size, curRenderer, delimiters,
						startLine);
			}

			String[] urlParts = sourceURL.toString().split("/");
			final String tabName = urlParts[urlParts.length - 1];
			guessDataTypes(newModel, tabName);
			listDataTypeMap.put(tabName, initListDataTypes(newModel));
			addTableTab(newModel, tabName, curRenderer);
		}

		loadFlag = true;
	}

	protected boolean isCytoscapeAttributeFile(final URL sourceURL)
			throws IOException {
		final BufferedReader bufRd = new BufferedReader(new InputStreamReader(
				URLUtil.getInputStream(sourceURL)));
		String line = null;
		int i = 0;

		boolean testResult = true;

		// Test first two lines to check the file type.
		while ((line = bufRd.readLine()) != null) {
			if (i == 0) {
				String[] elements = line.split(" +");

				if (elements.length == 1) {
					// True so far.
				} else {
					elements = line.split("[(]");

					if ((elements.length == 2)
							&& elements[1].startsWith("class=")) {
						// true so far.
					} else {
						testResult = false;

						break;
					}
				}
			} else if (i == 1) {
				String[] elements = line.split(" += +");

				if (elements.length != 2)
					testResult = false;
			} else if (i >= 2) {
				break;
			}

			i++;
		}

		bufRd.close();

		return testResult;
	}

	private void addTableTab(TableModel newModel, final String tabName,
			TableCellRenderer renderer) {
		JTable newTable = new JTable(newModel);
		previewTables.put(tabName, newTable);

		JScrollPane newScrollPane = new JScrollPane();
		newScrollPane.setViewportView(newTable);
		newScrollPane.setBackground(Color.WHITE);

		tableTabbedPane.add(tabName, newScrollPane);

		/*
		 * Initialize data type atrray. By default, everything is a String.
		 */

		// dataTypes = new Byte[newModel.getColumnCount()];
		// dataTypeMap.put(tabName, new Byte[newModel.getColumnCount()]);
		// for (int j = 0; j < newModel.getColumnCount(); j++) {
		// dataTypes[j] = CyAttributes.TYPE_STRING;
		// }
		// Setting table properties
		newTable.setCellSelectionEnabled(false);
		newTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		newTable.setDefaultEditor(Object.class, null);

		if (panelType == NETWORK_PREVIEW) {
			final int colCount = newTable.getColumnCount();
			final boolean[] importFlag = new boolean[colCount];

			for (int i = 0; i < colCount; i++) {
				importFlag[i] = false;
			}

			TableCellRenderer netRenderer = new AttributePreviewTableCellRenderer(
					AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST,
					new ArrayList<Integer>(),
					AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST,
					AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST,
					importFlag, TextFileDelimiters.PIPE.toString());

			newTable.setDefaultRenderer(Object.class, netRenderer);
		} else {
			newTable.setDefaultRenderer(Object.class, renderer);
		}

		JTableHeader hd = newTable.getTableHeader();
		hd.setReorderingAllowed(false);
		hd.setDefaultRenderer(new HeaderRenderer(hd.getDefaultRenderer(),
				dataTypeMap.get(tabName)));

		newTable.getTableHeader().addMouseListener(new TableHeaderListener());

		ColumnResizer.adjustColumnPreferredWidths(newTable);

		newTable.revalidate();
		newTable.repaint();
		newTable.getTableHeader().repaint();
	}

	private Byte[] initListDataTypes(final TableModel model) {
		final Byte[] listTypes = new Byte[model.getColumnCount()];

		for (int i = 0; i < listTypes.length; i++) {
			listTypes[i] = null;
		}

		return listTypes;
	}

	/**
	 * Based on the file type, setup the initial column names.
	 * <p>
	 * </p>
	 * 
	 * @param colCount
	 * @return
	 */
	private Vector<String> getDefaultColumnNames(final int colCount,
			final URL sourceURL) {

		final Vector<String> colNames = new Vector<String>();

		String[] parts = sourceURL.toString().split("/");
		final String fileName = parts[parts.length - 1];

		for (int i = 0; i < colCount; i++) {
			colNames.add("Column " + (i + 1));
		}

		return colNames;
	}

	private TableModel parseText(URL sourceURL, int size,
			TableCellRenderer renderer, List<String> delimiters, int startLine)
			throws IOException {
		final BufferedReader bufRd = new BufferedReader(new InputStreamReader(
				URLUtil.getInputStream(sourceURL)));
		String line;

		/*
		 * Generate reg. exp. for delimiter.
		 */
		final String delimiterRegEx;
		String attrName = "Attr1";

		if (delimiters != null) {
			StringBuffer delimiterBuffer = new StringBuffer();

			if (delimiters.size() != 0) {
				delimiterBuffer.append("[");

				for (String delimiter : delimiters) {
					delimiterBuffer.append(delimiter);
				}

				delimiterBuffer.append("]");
			}

			delimiterRegEx = delimiterBuffer.toString();
		} else {
			// treat as cytoscape attribute files.
			delimiterRegEx = " += +";
			// Extract first column for attr name.
			line = bufRd.readLine();
			String[] line1 = line.split(" +");
			attrName = line1[0];
		}

		/*
		 * Read & extract one line at a time. The line can be Tab delimited,
		 */
		boolean importAll = false;

		if (size == -1) {
			importAll = true;
		}

		int counter = 0;
		int maxColumn = 0;
		String[] parts;
		Vector data = new Vector();

		while ((line = bufRd.readLine()) != null) {
			if (((commentChar != null) && line.startsWith(commentChar))
					|| (line.trim().length() == 0) || (counter < startLine)) {
				// ignore
			} else {
				Vector row = new Vector();

				if (delimiterRegEx.length() == 0) {
					parts = new String[1];
					parts[0] = line;
				} else {
					parts = line.split(delimiterRegEx);
				}

				for (String entry : parts) {
					row.add(entry);
				}

				if (parts.length > maxColumn) {
					maxColumn = parts.length;
				}

				data.add(row);
			}

			counter++;

			if ((importAll == false) && (counter >= size)) {
				break;
			}
		}

		bufRd.close();

		if (delimiters == null) {
			// Cytoscape attr file.
			Vector<String> columnNames = new Vector<String>();
			columnNames.add("Key");
			columnNames.add(attrName);
			return new DefaultTableModel(data, columnNames);
		} else
			return new DefaultTableModel(data, getDefaultColumnNames(maxColumn,
					sourceURL));
	}

	private TableModel parseExcel(URL sourceURL, int size,
			TableCellRenderer renderer, HSSFSheet sheet, int startLine)
			throws IOException {
		int maxCol = 0;
		Vector data = new Vector();

		int rowCount = 0;
		HSSFRow row;

		while (((row = sheet.getRow(rowCount)) != null) && (rowCount < size)) {
			if (rowCount >= startLine) {
				Vector<Object> rowVector = new Vector<Object>();

				if (maxCol < row.getPhysicalNumberOfCells()) {
					maxCol = row.getPhysicalNumberOfCells();
				}

				for (short j = 0; j < maxCol; j++) {
					HSSFCell cell = row.getCell(j);

					if (cell == null) {
						rowVector.add(null);
					} else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
						rowVector
								.add(cell.getRichStringCellValue().getString());
					} else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
						final Double dblValue = cell.getNumericCellValue();
						final Integer intValue = dblValue.intValue();

						if (intValue.doubleValue() == dblValue) {
							rowVector.add(intValue.toString());
						} else {
							rowVector.add(dblValue.toString());
						}
					} else if (cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN) {
						rowVector.add(Boolean.toString(cell
								.getBooleanCellValue()));
					} else if ((cell.getCellType() == HSSFCell.CELL_TYPE_BLANK)
							|| (cell.getCellType() == HSSFCell.CELL_TYPE_ERROR)) {
						rowVector.add(null);
					} else {
						rowVector.add(null);
					}
				}

				data.add(rowVector);
			}

			rowCount++;
		}

		return new DefaultTableModel(data, this.getDefaultColumnNames(maxCol,
				sourceURL));
	}

	private void guessDataTypes(final TableModel model, final String tableName) {
		/*
		 * Assume: Row1 = Boolean Row2 = Integer Row3 = Double Row4 = String
		 */
		final Integer[][] typeChecker = new Integer[4][model.getColumnCount()];

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < model.getColumnCount(); j++) {
				typeChecker[i][j] = 0;
			}
		}

		String cell = null;

		for (int i = 0; i < model.getRowCount(); i++) {
			for (int j = 0; j < model.getColumnCount(); j++) {
				cell = (String) model.getValueAt(i, j);

				boolean found = false;

				if ((cell != null) && Boolean.valueOf(cell)) {
					try {
						Integer.valueOf(cell);
						typeChecker[1][j]++;
						found = true;
					} catch (NumberFormatException e) {
					}

					if (found == false) {
						typeChecker[0][j]++;
						found = true;
					}
				} else if (cell != null) {
					try {
						Integer.valueOf(cell);
						typeChecker[1][j]++;
						found = true;
					} catch (NumberFormatException e) {
					}

					try {
						Double.valueOf(cell);
						typeChecker[2][j]++;
						found = true;
					} catch (NumberFormatException e) {
					}
				}

				if (found == false) {
					typeChecker[3][j]++;
				}
			}
		}

		Byte[] dataType = dataTypeMap.get(tableName);

		if ((dataType == null) || (dataType.length != model.getColumnCount())) {
			dataType = new Byte[model.getColumnCount()];
		}

		for (int i = 0; i < dataType.length; i++) {
			int maxVal = 0;
			int maxIndex = 0;

			for (int j = 0; j < 4; j++) {
				if (maxVal < typeChecker[j][i]) {
					maxVal = typeChecker[j][i];
					maxIndex = j;
				}
			}

			if (maxIndex == 0)
				dataType[i] = CyAttributes.TYPE_BOOLEAN;
			else if (maxIndex == 1)
				dataType[i] = CyAttributes.TYPE_INTEGER;
			else if (maxIndex == 2)
				dataType[i] = CyAttributes.TYPE_FLOATING;
			else
				dataType[i] = CyAttributes.TYPE_STRING;
		}

		dataTypeMap.put(tableName, dataType);
	}

	/**
	 * Not yet implemented.
	 * <p>
	 * </p>
	 * 
	 * @param targetColumn
	 * @return
	 */
	public int checkKeyMatch(int targetColumn) {
		final List fileKeyList = Arrays
				.asList(((DefaultListModel) keyPreviewList.getModel())
						.toArray());
		int matched = 0;

		TableModel curModel = getPreviewTable().getModel();

		try {
			curModel.getValueAt(0, targetColumn);
		} catch (ArrayIndexOutOfBoundsException e) {
			return 0;
		}

		for (int i = 0; i < curModel.getRowCount(); i++) {
			if (fileKeyList.contains(curModel.getValueAt(i, targetColumn))) {
				matched++;
			}
		}

		return matched;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param column
	 *            DOCUMENT ME!
	 * @param flag
	 *            DOCUMENT ME!
	 */
	public void setAliasColumn(int column, boolean flag) {
		AttributePreviewTableCellRenderer rend = (AttributePreviewTableCellRenderer) getPreviewTable()
				.getCellRenderer(0, column);
		rend.setAliasFlag(column, flag);
		// rend.setImportFlag(column, !rend.getImportFlag(column));
		getPreviewTable().getTableHeader().resizeAndRepaint();
		getPreviewTable().repaint();
	}

	private final class TableHeaderListener implements MouseListener {
		public void mouseClicked(MouseEvent e) {
			final JTable targetTable = getPreviewTable();
			final String selectedTabName = getSelectedSheetName();
			final Byte[] dataTypes = dataTypeMap.get(selectedTabName);
			final Byte[] listDataTypes = listDataTypeMap.get(selectedTabName);

			final int column = targetTable.getColumnModel().getColumnIndexAtX(
					e.getX());

			if (SwingUtilities.isRightMouseButton(e)) {
				/*
				 * Right click: This action pops up an dialog to edit the
				 * attribute type and name.
				 */
				AttributeTypeDialog atd = new AttributeTypeDialog(Cytoscape
						.getDesktop(), true, targetTable.getColumnModel()
						.getColumn(column).getHeaderValue().toString(),
						dataTypes[column], column, listDelimiter);

				atd.setLocationRelativeTo(targetTable.getParent());
				atd.setVisible(true);

				final String name = atd.getName();
				final byte newType = atd.getType();
				final byte newListType = atd.getListDataType();

				if (name != null) {
					targetTable.getColumnModel().getColumn(column)
							.setHeaderValue(name);
					targetTable.getTableHeader().resizeAndRepaint();

					if (newType == CyAttributes.TYPE_SIMPLE_LIST) {
						// listDelimiter = atd.getListDelimiterType();
						listDelimiter = atd.getListDelimiterType();

						changes.firePropertyChange(
								ImportTextTableDialog.LIST_DELIMITER_CHANGED,
								null, atd.getListDelimiterType());

						listDataTypes[column] = newListType;
						changes.firePropertyChange(
								ImportTextTableDialog.LIST_DATA_TYPE_CHANGED,
								null, listDataTypes);
						listDataTypeMap.put(selectedTabName, listDataTypes);
					}

					final Vector keyValPair = new Vector();
					keyValPair.add(column);
					keyValPair.add(newType);
					changes.firePropertyChange(
							ImportTextTableDialog.ATTR_DATA_TYPE_CHANGED, null,
							keyValPair);

					final Vector colNamePair = new Vector();
					colNamePair.add(column);
					colNamePair.add(name);
					changes.firePropertyChange(
							ImportTextTableDialog.ATTRIBUTE_NAME_CHANGED, null,
							colNamePair);

					dataTypes[column] = newType;

					targetTable.getTableHeader().setDefaultRenderer(
							new HeaderRenderer(targetTable.getTableHeader()
									.getDefaultRenderer(), dataTypes));
					dataTypeMap.put(selectedTabName, dataTypes);
				}
			} else if (SwingUtilities.isLeftMouseButton(e)
					&& (e.getClickCount() == 1)) {
				final AttributePreviewTableCellRenderer rend = (AttributePreviewTableCellRenderer) targetTable
						.getCellRenderer(0, column);
				rend.setImportFlag(column, !rend.getImportFlag(column));
				targetTable.getTableHeader().resizeAndRepaint();
				targetTable.repaint();
			}
		}

		public void mouseEntered(MouseEvent arg0) {
		}

		public void mouseExited(MouseEvent arg0) {
		}

		public void mousePressed(MouseEvent arg0) {
		}

		public void mouseReleased(MouseEvent arg0) {
		}
	}
}

class KeyAttributeListRenderer extends JLabel implements ListCellRenderer {
	private static final Font KEY_LIST_FONT = new Font("Sans-Serif", Font.BOLD,
			16);
	private static final Color FONT_COLOR = Color.BLACK;

	/**
	 * Creates a new KeyAttributeListRenderer object.
	 */
	public KeyAttributeListRenderer() {
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
		setFont(KEY_LIST_FONT);
		setForeground(FONT_COLOR);
		setText(value.toString());

		this.setOpaque(false);

		return this;
	}
}

class CentredBackgroundBorder implements Border {
	private final BufferedImage image;

	/**
	 * Creates a new CentredBackgroundBorder object.
	 * 
	 * @param image
	 *            DOCUMENT ME!
	 */
	public CentredBackgroundBorder(BufferedImage image) {
		this.image = image;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param c
	 *            DOCUMENT ME!
	 * @param g
	 *            DOCUMENT ME!
	 * @param x
	 *            DOCUMENT ME!
	 * @param y
	 *            DOCUMENT ME!
	 * @param width
	 *            DOCUMENT ME!
	 * @param height
	 *            DOCUMENT ME!
	 */
	public void paintBorder(Component c, Graphics g, int x, int y, int width,
			int height) {
		x += ((width - image.getWidth()) / 2);
		y += ((height - image.getHeight()) / 2);
		((Graphics2D) g).drawRenderedImage(image, AffineTransform
				.getTranslateInstance(x, y));
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param c
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Insets getBorderInsets(Component c) {
		return new Insets(0, 0, 0, 0);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean isBorderOpaque() {
		return true;
	}
}
