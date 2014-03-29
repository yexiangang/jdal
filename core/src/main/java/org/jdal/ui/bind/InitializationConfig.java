/*
 * Copyright 2009-2014 Jose Luis Martin.
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
package org.jdal.ui.bind;

import java.io.Serializable;

/**
 * Options to use when initializating controls.
 * 
 * @author Jose Luis Martin
 * @since 2.0
 */
public class InitializationConfig implements Serializable {
	
	private Class<?> type;
	private String sortPropertyName;
	
	/**
	 * @param class1
	 */
	public InitializationConfig(Class<?> type) {
		this.type = type;
	}

	public Class<?> getType() {
		return type;
	}
	
	public void setType(Class<?> type) {
		this.type = type;
	}
	
	public String getSortPropertyName() {
		return sortPropertyName;
	}
	
	public void setSortPropertyName(String sortPropertyName) {
		this.sortPropertyName = sortPropertyName;
	}
	
	
}
