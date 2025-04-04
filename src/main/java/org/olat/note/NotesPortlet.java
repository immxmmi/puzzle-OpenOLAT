/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.note;

import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.portal.AbstractPortlet;
import org.olat.core.gui.control.generic.portal.Portlet;
import org.olat.core.gui.control.generic.portal.PortletToolController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * Description:<br>
 * Displays the list of most used notes
 * <P>
 * Initial Date:  08.07.2005 <br>
 * @author gnaegi
 */
public class NotesPortlet extends AbstractPortlet {
	private NotesPortletRunController runCtr;
	

	@Override
	public Portlet createInstance(WindowControl wControl, UserRequest ureq, Map<String,String> configuration) {
		Translator translator = Util.createPackageTranslator(NotesPortlet.class, ureq.getLocale());
		NotesPortlet p = new NotesPortlet();
		p.setName(this.getName());
		p.setConfiguration(configuration);
		p.setDefaultMaxEntries(getDefaultMaxEntries());
		p.setTranslator(translator);
		return p;
	}

	@Override
	public String getTitle() {
		return getTranslator().translate("notesPortlet.title");
	}

	@Override
	public String getDescription() {
		return getTranslator().translate("notesPortlet.description");
	}

	@Override
	public Component getInitialRunComponent(WindowControl wControl, UserRequest ureq) {
		if(runCtr != null) runCtr.dispose();
		runCtr = new NotesPortletRunController(wControl, ureq, getTranslator(), getName(), getDefaultMaxEntries());
		return runCtr.getInitialComponent();
	}

	@Override
	public void dispose() {
		disposeRunComponent();
	}

	@Override
	public String getCssClass() {
		return "o_portlet_notes";
	}

	@Override
	public void disposeRunComponent() {
		if (this.runCtr != null) {
			this.runCtr.dispose();
			this.runCtr = null;
		}
	}

	@Override
	public PortletToolController<Note> getTools(UserRequest ureq, WindowControl wControl) {
		if (runCtr == null) {
			runCtr = new NotesPortletRunController(wControl, ureq, getTranslator(), getName(), getDefaultMaxEntries());
		}
	  return runCtr.createSortingTool(ureq, wControl);
	}
}
