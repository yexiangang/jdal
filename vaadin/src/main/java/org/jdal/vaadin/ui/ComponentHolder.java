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
package org.jdal.vaadin.ui;

import com.vaadin.server.Resource;
import com.vaadin.ui.Component;

/**
 * @author Jose Luis Martin - (jlm@joseluismartin.info)
 */
public interface ComponentHolder {
	String getName();
	void setName(String name);
	Resource getIcon();
	void setIcon(Resource icon);
	Component getComponent();
	void setComponent(Component component);
}
