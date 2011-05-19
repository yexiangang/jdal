/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.joseluismartin.gui;

import info.joseluismartin.beans.AppCtx;
import info.joseluismartin.dao.Page;
import info.joseluismartin.dao.PageChangedEvent;
import info.joseluismartin.dao.PageableDataSource;
import info.joseluismartin.dao.Paginator;
import info.joseluismartin.dao.PaginatorListener;
import info.joseluismartin.dao.Page.Order;
import info.joseluismartin.gui.form.FormUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A Paginable JTable with paginator and filter. 
 * 
 * <p>This table view uses a {@link PageableDataSource} to query for data page by page.
 * Has a paginator control to navigate and show page info.
 *  
 * @author Jose Luis Martin - (jlm@joseluismartin.info)
 */
@SuppressWarnings("unchecked")
public class PageableTable extends JPanel implements RowSorterListener, PaginatorListener {
	
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(PageableTable.class);
	
	/** Border layout */
	private BorderLayout layout = new BorderLayout();
	/** table to show data to user */
	private JTable table;
	/** the paginator view */
	private PaginatorView paginatorView;
	/** pageable datasource to request data page by page */
	private PageableDataSource dataSource;
	/** list table model for the table */
	private ListTableModel tableModel;
	/** scroll pane used as table container */
	private JScrollPane tableScrollPane;
	/** row sorter that query a new page for server side order */
	private ModelRowSorter<ListTableModel> sorter;
	/** filter view with filter data */
	private Object filter;
	/** visiblity column descriptor in use */
	private List<ColumnDescriptor> columnDescriptors;
	/** visibility box for select visible columns in visibililty menu */
	private VisibilityBox visibilityBox;
	/** Gui Factory */
	GuiFactory guiFactory;
	/** editor name */
	String editorName;
	private Map<Object, Window> openDialogs = Collections.synchronizedMap(new HashMap<Object, Window>()); 
	
	// Menus
	JMenuBar rightMenuBar;
	
	// Icons 
	private Icon visibilityMenuIcon;
	private Icon okIcon;
	private Icon cancelIcon;
	

	// Attributes needed to work with Page objects in Reports
	private String sortPropertyName;
	private Page.Order order;
	
	
	/**
	 * Initalize component after properties set. Normally called by context vía init-method
	 */
	public void init() {
		okIcon = FormUtils.getIcon(okIcon, "/images/16x16/dialog-ok.png");
		cancelIcon = FormUtils.getIcon(cancelIcon, "/images/16x16/dialog-cancel.png");
		visibilityMenuIcon = FormUtils.getIcon(visibilityMenuIcon, "/images/16x16/view-choose.png");
		
		sorter = new ModelRowSorter<ListTableModel>(tableModel);
		sorter.addRowSorterListener(this);
		getInitialData();
		table = new JTable(tableModel, tableModel.getTableColumnModel());
		table.setAutoCreateRowSorter(false);
		table.setRowSorter(sorter);
		table.setRowHeight(22);
		table.addMouseListener(new TableListener());
		// table.addMouseMotionListener(new HighLightRowListener());
		tableScrollPane = new JScrollPane(table);
		setLayout(layout);
		this.setBackground(Color.WHITE);
		add(tableScrollPane, BorderLayout.CENTER);
		add(paginatorView, BorderLayout.SOUTH);
		paginatorView.getPaginator().addPaginatorListener(this);
		createMenu();
		paginatorView.refresh();
	}
	
	/**
	 * Create the right menu bar
	 */
	private void createMenu() {
		rightMenuBar = new JMenuBar();
		rightMenuBar.setLayout(new BoxLayout(rightMenuBar, BoxLayout.PAGE_AXIS));
		rightMenuBar.setMargin(new Insets(0, 0, 0, 0));
		// Add Visibility menu
		JMenu menu = new JMenu();
		menu.setMargin(new Insets(0,0,0,0));
		menu.setIcon(visibilityMenuIcon);
		menu.setMaximumSize(new Dimension(50,50));
		visibilityBox = new VisibilityBox(columnDescriptors);
		menu.add(visibilityBox);
		menu.getPopupMenu().addPopupMenuListener(new VisibilityPopupListener());
		JMenuItem okMenuItem = new JMenuItem(new OkVisibilityAction());
		JMenuItem cancelMenuItem = new JMenuItem(new CancelVisibilityAction());
		menu.addSeparator();
		menu.add(okMenuItem);
		menu.add(cancelMenuItem);
		rightMenuBar.add(menu);
		rightMenuBar.add(Box.createVerticalGlue());
		// Add menu bar to right
		add (rightMenuBar, BorderLayout.EAST);
	}

	/**
	 * Get intial data and create columns desciptors list for visibility menu
	 */
	private void getInitialData() {
		loadPage(createPage());
		// get info about columns on table model
		columnDescriptors = new ArrayList<ColumnDescriptor>(tableModel.getPropertyCount());
		for (int i = 0; i < tableModel.getPropertyCount(); i++) {
			columnDescriptors.add(new ColumnDescriptor(tableModel.getColumnNames().get(i), 
					tableModel.getDisplayNames().get(i), true));
		}
	}

	/** 
	 * Handle sort changes in model sorter. 
	 * Query PageableDataSource for new page with the sort changes
	 * @see javax.swing.event.RowSorterListener#sorterChanged(javax.swing.event.RowSorterEvent)
	 */
	public void sorterChanged(RowSorterEvent e) {
		if (sorter.getSortKeys().size() > 0 && 
				tableModel.isPropertyColumn(sorter.getSortKeys().get(0).getColumn())) {
			// set first page
			paginatorView.getPaginator().firstPage();
		}
	}

	/**
	 * Create a new Page for query to DataSource
	 * with data from sorter and paginator.
	 * @return a new Page ready to call dataSource.getPage() or null to abort.
	 */
	private Page createPage() {
		Page.Order order = Page.Order.ASC;
		String sortPropertyName = null;
		List<? extends SortKey> keys = sorter.getSortKeys();
		// If sorting, get values to set in page
		if (keys.size() > 0) {
			RowSorter.SortKey key = sorter.getSortKeys().get(0);
			if (tableModel.isPropertyColumn(key.getColumn())) {
				sortPropertyName = tableModel.getSortPropertyName(key.getColumn());
				order = converSortOrder(key);
			}
			
		}
			
		Paginator paginator = paginatorView.getPaginator();
		Page page = new Page(paginator.getPageSize(), paginator.getPage(), sortPropertyName, order);
	
		page.setFilter(filter);

		return page;
	}

	/**
	 * Convert the Order from SortKey to Page.Order
	 * @param key the SortKey
	 * @return  the Page order
	 */
	private Page.Order converSortOrder(RowSorter.SortKey key) {
		Page.Order order = Order.ASC;
		if (key.getSortOrder() == SortOrder.DESCENDING) {
			order = Order.DESC;
		}
		return order;
	}
	
	/**
	 * Load new page from data source
	 * @param page
	 */
	private void loadPage(Page page) {
		dataSource.getPage(page);
		tableModel.setList(page.getData());
		Paginator paginator = paginatorView.getPaginator();
		paginator.setCount(page.getCount());
		paginatorView.refresh();
	}
	
	/**
	 * Handle paginators changes.
	 * @see info.joseluismartin.gui.PaginatorListener#pageChanged(info.joseluismartin.gui.PageChangedEvent)
	 */
	public void pageChanged(PageChangedEvent event) {
		Page page = createPage();
		loadPage(page);
	}
	
	/**
	 * Get a dialog for editing a row
	 */
	
	public Window getEditor() {
		Window dlg = (Window) guiFactory.getObject(editorName);
		return dlg;
	}
	
	/**
	 * @param toEdit model to edit
	 * @return model editor.
	 */
	public Window getEditor(Object toEdit) {
		Window dlg = openDialogs.get(toEdit);
		if (dlg == null) {
			dlg = getEditor();
			openDialogs.put(toEdit, dlg);
			((View) dlg).setModel(toEdit);
			((View) dlg).refresh();
			dlg.addWindowListener(new DialogWindowListener());
			if (dlg instanceof Editor) {
				((Editor) dlg).addEditorListener(new EditorListener() {
					
					public void modelChanged(EditorEvent e) {
						refresh();
					}
				});
			}
		}
		
		return dlg;
	}
	/**
	 * @return the paginatorView
	 */
	public PaginatorView getPaginatorView() {
		return paginatorView;
	}

	/**
	 * @param paginatorView the paginatorView to set
	 */
	public void setPaginatorView(PaginatorView paginatorView) {
		this.paginatorView = paginatorView;
	}

	/**
	 * @return the dataSource
	 */
	public PageableDataSource getDataSource() {
		return dataSource;
	}

	/**
	 * @param dataSource the dataSource to set
	 */
	public void setDataSource(PageableDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public Paginator getPaginator() {
		return paginatorView.getPaginator();
	}

	/**
	 * @return the tableModel
	 */
	public ListTableModel getTableModel() {
		return tableModel;
	}

	/**
	 * @param tableModel the tableModel to set
	 */
	public void setTableModel(ListTableModel tableModel) {
		this.tableModel = tableModel;
	}
	
	
	public Icon getVisibilityMenuIcon() {
		return visibilityMenuIcon;
	}

	public void setVisibilityMenuIcon(Icon visibilityMenuIcon) {
		this.visibilityMenuIcon = visibilityMenuIcon;
	}

	public Icon getOkIcon() {
		return okIcon;
	}

	public void setOkIcon(Icon okIcon) {
		this.okIcon = okIcon;
	}

	public Icon getCancelIcon() {
		return cancelIcon;
	}

	public void setCancelIcon(Icon cancelIcon) {
		this.cancelIcon = cancelIcon;
	}
	
	/**
	 * Listener to watch visibility popup menu and sync visibility state
	 * whene popoup is cancelled externally.
	 * @author Jose Luis Martin - (jlm@joseluismartin.info)
	 */
	class VisibilityPopupListener implements PopupMenuListener {

		/** 
		 * {@inheritDoc}
		 * @see javax.swing.event.PopupMenuListener#popupMenuCanceled(javax.swing.event.PopupMenuEvent)
		 */
		public void popupMenuCanceled(PopupMenuEvent e) {
			visibilityBox.setColumnDescriptors(columnDescriptors);
			
		}

		/** 
		 * {@inheritDoc}
		 * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent)
		 */
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		
		}

		/** 
		 * {@inheritDoc}
		 * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent)
		 */
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {		
		}
	}
	
	class OkVisibilityAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public OkVisibilityAction() {
			super("Aceptar", okIcon);
		}
		
		/**
		 * Copy visibility descriptors to table and change de ListableTableModel
		 * with new properties
		 * @param ActionEvent the JButton ActionEvent
		 */
		public void actionPerformed(ActionEvent e) {
			List<String> displayNames = new ArrayList<String>(columnDescriptors.size());
			List<String> propertyNames = new ArrayList<String>(columnDescriptors.size());
			
			for (int i = 0; i < columnDescriptors.size(); i++) {
				ColumnDescriptor cd = columnDescriptors.get(i);
				cd.setVisible(visibilityBox.getColumnDescriptors().get(i).isVisible());
				if (cd.isVisible()) {
					displayNames.add(cd.getDisplayName());
					propertyNames.add(cd.getPropertyName());
				}
			}
			tableModel.setDisplayNames(displayNames);
			tableModel.setColumnNames(propertyNames);
			tableModel.init();
			table.setColumnModel(tableModel.getTableColumnModel());
			tableModel.fireTableChanged();
		}
	}
	
	class CancelVisibilityAction extends AbstractAction {
		
		private static final long serialVersionUID = 1L;

		public CancelVisibilityAction() {
			super("Cancelar", cancelIcon);
		}

		/**
		 * When cancel, set visibility descriptors from table 
		 * and discard selection changes.
		 * @param e ActionEvent from JButton
		 */
		public void actionPerformed(ActionEvent e) {
			visibilityBox.setColumnDescriptors(columnDescriptors);
			
		}
		
	}
	
	private class TableListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			Point point = e.getPoint();
			int row = table.rowAtPoint(point);
			int col = table.columnAtPoint(point);
			// check Actions
			if (col != -1 && row != -1 && tableModel.isActionColumn(col)) {
				TableRowAction action = (TableRowAction) tableModel.getValueAt(row, col);
				action.setTable(PageableTable.this);
				action.setRow(tableModel.getList().get(row));
				action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "clicked"));
				
			}
			// check double click on rows
			if (row != -1 && e.getClickCount() == 2) {
				Object toEdit = tableModel.getList().get(row);
				Window dlg = getEditor(toEdit);
				if (dlg != null) {
					if (dlg instanceof Frame) {
						((Frame) dlg).setState(Frame.NORMAL);
						((Frame) dlg).requestFocus();
					}
					dlg.setLocationRelativeTo(null);
					dlg.setAlwaysOnTop(true);
					dlg.setVisible(true);
				}
			}
		}
	}
	
	/**
	 * Remove dialogs from openDialog Map when closed. Will fail if the model 
	 * hashcode change after editing it.
	 *
	 * @author Jose Luis Martin - (jlm@joseluismartin.info)
	 */
	private class DialogWindowListener extends WindowAdapter {
		@Override
		public void windowClosed(WindowEvent e) {
			if (openDialogs.remove(((View) e.getWindow()).getModel()) == null)
				log.warn("Tray to remove a non existant Dialog, ¿may be model hashcode changed?");
		}
	}
	

	public GuiFactory getGuiFactory() {
		return guiFactory;
	}

	public void setGuiFactory(GuiFactory guiFactory) {
		this.guiFactory = guiFactory;
	}

	public String getEditorName() {
		return editorName;
	}

	public void setEditorName(String editorName) {
		this.editorName = editorName;
	}

	public void refresh() {
		pageChanged(null);
	}
	
	/**
	 * Simple test main method
	 * @param args
	 */
	public static void main (String[] args) {
		PageableTable incidencesTable = (PageableTable) AppCtx.getInstance().getBean("incidentTable");
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add(incidencesTable);
		f.setSize(new Dimension(800, 600));
		f.setVisible(true);
	}

	/**
	 * @return the filter
	 */
	public Object getFilter() {
		return filter;
	}

	/**
	 * @param filter the filter to set
	 */
	public void setFilter(Object filter) {
		this.filter = filter;
	}

	/**
	 * @param columnClass
	 * @param renderer
	 * @see javax.swing.JTable#setDefaultRenderer(java.lang.Class, javax.swing.table.TableCellRenderer)
	 */
	public void setDefaultRenderer(Class<?> columnClass,
			TableCellRenderer renderer) {
		table.setDefaultRenderer(columnClass, renderer);
	}
	
	public String getSortPropertyName() {
		return sortPropertyName;
	}

	public void setSortPropertyName(String sortPropertyName) {
		this.sortPropertyName = sortPropertyName;
	}

	public Page.Order getOrder() {
		return order;
	}

	public void setOrder(Page.Order order) {
		this.order = order;
	}
}

/**
 * Simple data model for description of table column in visibility menu
 * 
 * @author Jose Luis Martin - (jlm@joseluismartin.info)
 */
class ColumnDescriptor  implements Cloneable {
	private String propertyName;
	private String displayName;
	private boolean visible;

	
	
	/**
	 * Create a new ColumnDescriptor
	 * @param propertyName
	 * @param displayName
	 * @param visible
	 */
	public ColumnDescriptor(String propertyName, String displayName,
			boolean visible) {
		this.propertyName = propertyName;
		this.displayName = displayName;
		this.visible = visible;
	}


	public String getPropertyName() {
		return propertyName;
	}


	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}


	public String getDisplayName() {
		return displayName;
	}


	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}


	public boolean isVisible() {
		return visible;
	}


	public void setVisible(boolean visible) {
		this.visible = visible;
	}


	/**
	 * Clone this object
	 */
	@Override
	public Object clone()  {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}

/**
 * Simple Box Component that hold a ColumnDescriptor and a checkbox
 * to show visibility state of a table column
 * 
 * @author Jose Luis Martin - (jlm@joseluismartin.info)
 */
class VisibilityItem extends JComponent implements ChangeListener {
	
	private static final long serialVersionUID = 1L;
	private ColumnDescriptor cd;
	private JCheckBox check;
	
	/**
	 * @param cd
	 */
	public VisibilityItem(ColumnDescriptor cd) {
		this.cd = cd;
		BoxLayout layout = new BoxLayout(this, BoxLayout.LINE_AXIS);
		setLayout(layout);
		this.setAlignmentX(0f);
		check = new JCheckBox(cd.getDisplayName(), cd.isVisible());
		check.setAlignmentX(0f);
		add(check);
		check.addChangeListener(this);
		refresh();
	}
	
	public void refresh() {
		check.setSelected(cd.isVisible());
	}

	
	/**
	 * Update the columnDescriptor when check change
	 * @param e the ChangeEvent
	 */
	public void stateChanged(ChangeEvent e) {
		cd.setVisible(check.isSelected());
		
	}

	/**
	 * @param columnDescriptor
	 */
	public void setColumnDescriptor(ColumnDescriptor columnDescriptor) {
		this.cd = columnDescriptor;
		refresh();
		
	}
}

/**
 * Box to show visibility column state in a JMenu popup window.
 *  
 * @author Jose Luis Martin - (jlm@joseluismartin.info)
 */
class VisibilityBox extends JComponent {
	
	private static final long serialVersionUID = 1L;
	private final static Log log = LogFactory.getLog(VisibilityBox.class);
	/** column descriptors copy */
	List<ColumnDescriptor> columnDescriptors;

	/**
	 * Create a new VisiblityBox initialized with column descriptors
	 * @param columnDescriptos
	 */
	public VisibilityBox(List<ColumnDescriptor> columnDescriptors) {
		setColumnDescriptors(columnDescriptors);
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		for (ColumnDescriptor cd : this.columnDescriptors) {
			add(new VisibilityItem(cd));
		}
	}

	/**
	 * refresh the state of visibility item when column descriptor list changes
	 */
	public void refresh() {
		System.out.println(columnDescriptors.size());
		for (int i = 0; i < getComponents().length; i++) {
			Component c = getComponent(i);
			if (c instanceof VisibilityItem) {
				if (log.isDebugEnabled())
					log.debug("refresh: " + columnDescriptors.get(i).isVisible());
				
				((VisibilityItem) c).setColumnDescriptor(columnDescriptors.get(i));
			}
		}
	}

	/**
	 * get the column descriptors
	 * @return the column descriptor list
	 */
	public List<ColumnDescriptor> getColumnDescriptors() {
		return columnDescriptors;
	}

	/**
	 * Sets the column descriptor list. 
	 * We clone all column descriptor list to easy discard changes when the 
	 * user cancel changes to visibility column state. 
	 * @param columnDescriptors
	 */
	public void setColumnDescriptors(List<ColumnDescriptor> columnDescriptors) {
		this.columnDescriptors = new ArrayList<ColumnDescriptor>(columnDescriptors.size());
		for (ColumnDescriptor cd : columnDescriptors) {
			this.columnDescriptors.add((ColumnDescriptor) cd.clone());
		}
		refresh();
	}
}

// FIXME: move to appropiate package
class HighLightRowListener extends MouseAdapter {
	
	private int highLightedRow = -1;
	Rectangle dirtyRegion = null;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		JTable table = (JTable) e.getSource();
		try {
			int row = table.rowAtPoint(e.getPoint());
			Graphics g = table.getGraphics();
			// row changed
			if (highLightedRow != row) {
				if (null != dirtyRegion) {
					table.paintImmediately(dirtyRegion);
				}

				if (!table.getSelectionModel().isSelectedIndex(row)) {
					for (int j = 0; j < table.getRowCount(); j++) {
						if (row == j) {
							// highlight
							Rectangle firstRowRect = table.getCellRect(row, 0,
									false);
							Rectangle lastRowRect = table.getCellRect(row,
									table.getColumnCount() - 1, false);
							dirtyRegion = firstRowRect.union(lastRowRect);
							g.setColor(new Color(0xff, 0xff, 0, 100));
							g.fillRect((int) dirtyRegion.getX(),
									(int) dirtyRegion.getY(),
									(int) dirtyRegion.getWidth(),
									(int) dirtyRegion.getHeight());
						}
					}
				}
				
			}
			highLightedRow = row;
		} catch (Exception ex) {}
	}
}
