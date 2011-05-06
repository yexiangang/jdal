/*
 * Copyright 2009-2011 Jose Lus Martin.
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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

/**
 * @author Jose Luis Martin - (jlm@joseluismartin.info)
 *
 */
public abstract class IconAction extends AbstractAction  {

	private static final long serialVersionUID = 1L;

	public Icon getIcon() {
		return (Icon) getValue(Action.SMALL_ICON);
	}
	
	public void setIcon(Icon icon) {
		putValue(Action.SMALL_ICON, icon);
	}
	
	public void setName(String name) {
		putValue(Action.NAME, name);
	}
	
	public String getName() {
		return (String) getValue(Action.NAME);
	}
}