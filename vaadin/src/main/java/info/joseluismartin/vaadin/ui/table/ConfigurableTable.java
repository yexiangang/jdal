/*
 * Copyright 2009-2011 the original author or authors.
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
package info.joseluismartin.vaadin.ui.table;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Container;
import com.vaadin.ui.Table;

/**
 * Add methods to vaadin Table for friendly configuration on spring bean defintion file.
 * Use <code>info.joseluismartin.ui.table.Column</code> as inner bean for configure columns:
 * 
 * <property name="columns">
 * 	<list>
 * 		<bean class="info.joseluismartin.ui.table.Column"/>
 * 			<property name="name" value="a_bean_property_name"/>
 * 			<property name="displayName" value="String_to_show_in_table_header"/>
 * 			<property name="width" value="column_width"/>
 * 			...
 * 		</bean>
 * 		<bean class="info.joseluismartin.vaadin.ui.table.Column">
 * 			...
 * 		</bean>
 * 	</list>
 * <property name="service" value="a_persistent_service"/>
 * </property>
 * 
 * ConfigurableTable use persistenService.getPage() to get List of models.
 * 
 * @author Jose Luis Martin - (jlm@joseluismartin.info)
 */
@SuppressWarnings("serial")
public class ConfigurableTable extends Table {
	
	private boolean usingChecks = false;
	private List<Column> columns;
	private TableSorter sorter;
	private Map<String, Column> columnMap = new HashMap<String, Column>();
	
	public List<Column> getColumns() {
		return this.columns;
	}
	
	
	/**
	 * Configure Vaadin table with column definitions. 
	 * Method useful for use from context bean definition file.
	 * @param columns
	 */
	public void setColumns(List<Column> columns) {
		this.columns = columns;
		columnMap.clear();
		for (Column c : columns)
			columnMap.put(c.getName(), c);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setContainerDataSource(Container newDataSource) {
		super.setContainerDataSource(newDataSource);
		configure();
	}
	/**
	 * Vaadin Table throw an exception when set this properties 
	 * with an empty Container datasource. 
	 */
	protected void configure() {
		if (columns == null)
			return;
		
		int size = columns.size();
		String[] visibleColumns = new String[size];
		String[] displayNames = new String[size];
		String[] alignments = new String[size];
		int[] widths = new int[size];
				
		for (int i = 0; i < size; i++) {
			visibleColumns[i] = columns.get(i).getName();
			displayNames[i] = columns.get(i).getDisplayName();
			alignments[i] = columns.get(i).getAlign();
			widths[i] = columns.get(i).getWidth();
		}
		
		this.setVisibleColumns(visibleColumns);
		this.setColumnHeaders(displayNames);
		this.setColumnAlignments(alignments);
		
		for (int i = 0; i < size; i++) {
			if ( widths[i] != -1)
				this.setColumnWidth(visibleColumns[i], widths[i]);
		}
	}
	
	
	
	/**
	 * Gets usingChecks property, if true, table show checkboxes for row selection
	 * @return the usingChecks
	 */
	public boolean isUsingChecks() {
		return usingChecks;
	}


	/**
	 * Sets usingChecks property, if true, table show checkboxes for row selection
	 * @param usingChecks the usingChecks to set
	 */
	public void setUsingChecks(boolean usingChecks) {
		this.usingChecks = usingChecks;
	}


	/**
	 * Sort on container or 
	 */
	@Override
	public void sort(Object[] propertyId, boolean[] ascending) {
		if (sorter != null)
			sorter.sort(propertyId, ascending);
		else {
			super.sort(propertyId, ascending);
		}
	}

	/**
	 * Override to handle server side sorting
	 * {@inheritDoc}
	 */
	@Override
	public Collection<?> getSortableContainerPropertyIds() {
		List<Object> sortableIds = new LinkedList<Object>();
		for (Column c :columns ) {
			if (c.isSortable())
				sortableIds.add(c.getName());
		}
		
		return sortableIds;
	}
	
	public Column getColumn(String name) {
		return columnMap.get(name);
	}

	public TableSorter getSorter() {
		return sorter;
	}


	public void setSorter(TableSorter sorter) {
		this.sorter = sorter;
	}
}