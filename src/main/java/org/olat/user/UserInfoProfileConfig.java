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
package org.olat.user;

/**
 * 
 * Initial date: 15 Aug 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class UserInfoProfileConfig {
	
	private boolean chatEnabled;
	private boolean userManagementLinkEnabled = true;
	private UserAvatarMapper avatarMapper;
	private String avatarMapperBaseURL;

	public boolean isChatEnabled() {
		return chatEnabled;
	}

	public void setChatEnabled(boolean chatEnabled) {
		this.chatEnabled = chatEnabled;
	}

	public UserAvatarMapper getAvatarMapper() {
		return avatarMapper;
	}

	public void setAvatarMapper(UserAvatarMapper avatarMapper) {
		this.avatarMapper = avatarMapper;
	}

	public String getAvatarMapperBaseURL() {
		return avatarMapperBaseURL;
	}

	public void setAvatarMapperBaseURL(String avatarMapperBaseURL) {
		this.avatarMapperBaseURL = avatarMapperBaseURL;
	}

	public void setUserManagementLinkEnabled(boolean userManagementLinkEnabled) {
		this.userManagementLinkEnabled = userManagementLinkEnabled;
	}

	public boolean isUserManagementLinkEnabled() {
		return userManagementLinkEnabled;
	}
	
}
