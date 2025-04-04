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
package org.olat.modules.project;

import java.util.Date;

import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 28 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public interface ProjProjectUserInfo {

	/**
	 * retrieve the date, when the user registered initially for a project
	 * @return date value
	 */
	public Date getCreationDate();
	
	public Long getKey();
	
	public Date getLastVisitDate();
	
	public void setLastVisitDate(Date lastVisitDate);
	
	public ProjProject getProject();
	
	public Identity getIdentity();
}
