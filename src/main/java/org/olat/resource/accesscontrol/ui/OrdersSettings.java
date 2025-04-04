/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.resource.accesscontrol.ui;

/**
 * 
 * Initial date: 21 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrdersSettings {
	
	private final boolean canEditOrder;
	
	private final boolean withTools;
	private final boolean withActivities;
	private final boolean withResourceDisplayName;
	
	private OrdersSettings(boolean withActivities, boolean withTools, boolean withResourceDisplayName, boolean canEditOrder) {
		this.canEditOrder = canEditOrder;
		this.withTools = withTools;
		this.withActivities = withActivities;
		this.withResourceDisplayName = withResourceDisplayName;
	}
	
	public static OrdersSettings defaultSettings() {
		return new OrdersSettings(false, true, true, false);
	}
	
	public static OrdersSettings valueOf(boolean withActivities, boolean withTools, boolean withResourceDisplayName, boolean canEditOrder) {
		return new OrdersSettings(withActivities, withTools, withResourceDisplayName, canEditOrder);
	}
	
	public boolean canEditOrder() {
		return canEditOrder;
	}
	
	public boolean withTools() {
		return withTools;
	}
	
	public boolean withActivities() {
		return withActivities;
	}
	
	public boolean withResourceDisplayName() {
		return withResourceDisplayName;
	}
}
