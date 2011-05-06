/*
 * Copyright 2008-2011 the original author or authors.
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
package info.joseluismartin.gui.bind;

import info.joseluismartin.gui.View;

/**
 * PropertyBinder for Views
 * 
 * @author Jose Luis Martin - (jlm@joseluismartin.info)
 */
@SuppressWarnings("unchecked")
public class ViewBinder extends AbstractBinder {


	public void refresh() {
		Object value = getValue();
		View<Object> view = (View<Object>) component;
		view.setModel(value);
		view.refresh();
	}
	
	public void update() {
		View<Object> view = (View<Object>) component;
		view.update();
		setValue(view.getModel());
	}

}