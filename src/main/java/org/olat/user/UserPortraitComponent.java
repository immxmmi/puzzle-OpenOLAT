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

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: Feb 3, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class UserPortraitComponent extends AbstractComponent {
	
	private static final ComponentRenderer RENDERER = new UserPortraitRenderer();
	private static final UserPortraitMapper MAPPER = new UserPortraitMapper();
	
	private final Translator compTranslator;
	private String portraitMapperUrl;
	private PortraitUser portraitUser;
	private PortraitSize size = PortraitSize.medium;
	private boolean displayPresence = false;

	protected UserPortraitComponent(String name, Locale locale) {
		super(name);
		compTranslator = Util.createPackageTranslator(UserPortraitComponent.class, locale);
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	Translator getCompTranslator() {
		return compTranslator;
	}
	
	String getPortraitMapperUrl() {
		if (portraitMapperUrl == null) {
			// Small optimization to prevent unnecessary creations and registrations of the mapper.
			// Can also be changed without implications if required (e.g. registration in the constructor of the component).
			portraitMapperUrl = CoreSpringFactory.getImpl(MapperService.class).register(null, "user-portrait-mapper", MAPPER).getUrl();
		}
		return portraitMapperUrl;
	}

	public PortraitUser getPortraitUser() {
		return portraitUser;
	}

	public void setPortraitUser(PortraitUser portraitUser) {
		this.portraitUser = portraitUser;
		setDirty(true);
	}

	public PortraitSize getSize() {
		return size;
	}

	public void setSize(PortraitSize size) {
		this.size = size;
		setDirty(true);
	}

	public boolean isDisplayPresence() {
		return displayPresence;
	}

	public void setDisplayPresence(boolean displayPresence) {
		this.displayPresence = displayPresence;
		setDirty(true);
	}

}
