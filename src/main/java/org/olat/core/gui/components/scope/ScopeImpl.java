/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.gui.components.scope;

/**
 * 
 * Initial date: 24 Nov 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class ScopeImpl implements Scope {

	private final String key;
	private final String displayName;
	private final String hint;
	private final String iconLeftCss;

	public ScopeImpl(String key, String displayName, String hint, String iconLeftCss) {
		this.key = key;
		this.displayName = displayName;
		this.hint = hint;
		this.iconLeftCss = iconLeftCss;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String getHint() {
		return hint;
	}

	@Override
	public String getIconLeftCSS() {
		return iconLeftCss;
	}
}
