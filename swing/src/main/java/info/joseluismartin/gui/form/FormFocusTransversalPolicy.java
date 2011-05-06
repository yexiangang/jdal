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
package info.joseluismartin.gui.form;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.util.ArrayList;

/**
 * FocusTraversalProvider for FormBuilder
 * 
 * @author Jose Luis Martin - (jlm@joseluismartin.info)
 */
public class FormFocusTransversalPolicy extends FocusTraversalPolicy {
	
	ArrayList<Component> components = new ArrayList<Component>();
	
	@Override
	public Component getComponentAfter(Container aContainer,
			Component aComponent) {
	
		int index = components.indexOf(aComponent);
		index++;
		
		if (index < components.size() && index >= 0) {
			Component c =  components.get(index);
			if (c.isEnabled()) 
				return c;
			else 
				return getComponentAfter(aContainer, c);
		}
		
		return getFirstComponent(aContainer);
	}

	@Override
	public Component getComponentBefore(Container aContainer,
			Component aComponent) {
		
		int index = components.indexOf(aComponent);
		index--;
		
		if (index < components.size() && index >= 0) {
			Component c =  components.get(index);
			if (c.isEnabled()) 
				return c;
			else 
				return getComponentBefore(aContainer, c);
		}
		
		return getLastComponent(aContainer);
	}

	@Override
	public Component getDefaultComponent(Container aContainer) {
		if (components.size() > 0) {
			Component c = components.get(0);
			if (c.isEnabled())
				return c;
			else 
				return getComponentAfter(aContainer, c);
		}
		
		return null;
	}

	@Override
	public Component getFirstComponent(Container aContainer) {
		return getDefaultComponent(aContainer);
	}

	@Override
	public Component getLastComponent(Container aContainer) {
		if (!components.isEmpty()) {
			Component c = components.get(components.size() - 1);
			if (c.isEnabled())
				return c;
			else
				return getComponentBefore(aContainer, c);
		}
		
		return null;
	}

	public void add(Component c) {
		components.add(c);
	}
}