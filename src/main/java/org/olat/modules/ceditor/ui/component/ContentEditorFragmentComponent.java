/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.ceditor.ui.component;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.ui.ModalInspectorController;
import org.olat.modules.ceditor.ui.PageElementTarget;
import org.olat.modules.ceditor.ui.event.CloneElementEvent;
import org.olat.modules.ceditor.ui.event.DeleteElementEvent;
import org.olat.modules.ceditor.ui.event.DropToPageElementEvent;
import org.olat.modules.ceditor.ui.event.EditElementEvent;
import org.olat.modules.ceditor.ui.event.EditPageElementEvent;
import org.olat.modules.ceditor.ui.event.MoveDownElementEvent;
import org.olat.modules.ceditor.ui.event.MoveUpElementEvent;
import org.olat.modules.ceditor.ui.event.OpenAddElementEvent;
import org.olat.modules.ceditor.ui.event.PositionEnum;
import org.olat.modules.ceditor.ui.event.SaveElementEvent;

/**
 * 
 * Initial date: 6 déc. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContentEditorFragmentComponent extends AbstractComponent implements ContentEditorFragment, ComponentEventListener, ControllerEventListener {
	
	private static final Logger log = Tracing.createLoggerFor(ContentEditorFragmentComponent.class);
	private static final ContentEditorFragmentComponentRenderer RENDERER = new ContentEditorFragmentComponentRenderer();
	
	private boolean create = false;
	private boolean editMode = false;
	private boolean moveable = false;
	private boolean cloneable = false;
	private boolean deleteable = false;
	private boolean inForm;

	private final PageElement pageElement;
	private final Link toggleInspectorButton;
	private InspectorPanelComponent inspectorPanel;
	
	private final Controller editorPart;
	private final PageRunElement viewPart;
	private final Controller inspectorPart;
	
	public ContentEditorFragmentComponent(String name, PageElement pageElement, PageRunElement viewPart, Controller editorPart, Controller inspectorPart, Translator translator) {
		super(name);
		this.editorPart = editorPart;
		this.viewPart = viewPart;
		this.inspectorPart = inspectorPart;
		this.pageElement = pageElement;
		setDomReplacementWrapperRequired(false);
		
		toggleInspectorButton = LinkFactory.createCustomLink("toggle", "toggle", "", Link.NONTRANSLATED + Link.LINK_CUSTOM_CSS, null, this);
		toggleInspectorButton.setIconLeftCSS("o_icon o_icon-fw o_icon_inspect");
		toggleInspectorButton.setAriaRole("button");
		toggleInspectorButton.setTitle(translator.translate("edit.settings"));

		if(inspectorPart != null) {
			inspectorPanel = new InspectorPanelComponent(inspectorPart.getInitialComponent());
			toggleInspectorButton.setElementCssClass("o_sel_elementinspector active");
			toggleInspectorButton.setEnabled(inspectorPart != null);
		} else {
			toggleInspectorButton.setEnabled(false);
		}
	}

	@Override
	public boolean isEditMode() {
		return editMode;
	}
	
	@Override
	public void setEditMode(boolean editMode) {
		doEditFragment(null, editMode);
	}

	@Override
	public boolean isInspectorVisible() {
		return inspectorPanel != null && inspectorPanel.isInspectorVisible();
	}

	@Override
	public void setInspectorVisible(boolean inspectorVisible, boolean silently) {
		if (inspectorVisible) {
			positionInspector(null, null);
		}
		if(isInspectorVisible() != inspectorVisible && inspectorPanel != null) {
			inspectorPanel.setInspectorVisible(inspectorVisible);
			toggleInspectorButton.setElementCssClass("o_sel_elementinspector" + (inspectorVisible ? " active" : ""));
			toggleInspectorButton.setDirty(true);
			if(!silently) {
				setDirty(true);
			}
		}
	}

	@Override
	public boolean isCloneable() {
		return cloneable;
	}

	@Override
	public void setCloneable(boolean cloneable) {
		this.cloneable = cloneable;
	}

	@Override
	public boolean isDeleteable() {
		return deleteable;
	}

	@Override
	public void setDeleteable(boolean enable) {
		deleteable = enable;
	}

	@Override
	public boolean isDeleteLinkDisabled() {
		return false;
	}

	@Override
	public void setDeleteLinkDisabled(boolean deleteLinkDisabled) {
		//
	}

	@Override
	public boolean isMoveable() {
		return moveable;
	}

	@Override
	public void setMoveable(boolean enable) {
		this.moveable = enable;
	}

	@Override
	public boolean isCreate() {
		return create;
	}

	@Override
	public void setCreate(boolean enable) {
		create = enable;
	}

	@Override
	public boolean isEditable() {
		return editorPart != null || inspectorPanel != null;
	}

	@Override
	public String getElementId() {
		return pageElement.getId();
	}
	
	@Override
	public PageElement getElement() {
		return pageElement;
	}
	
	public boolean isDropppable() {
		return false;
	}
	
	public Component getViewPageElementComponent() {
		return viewPart == null ? null : viewPart.getComponent();
	}
	
	public Component getEditorPageElementComponent() {
		return editorPart == null ? null : editorPart.getInitialComponent();
	}
	
	public Link getToggleInspectorButton() {
		return toggleInspectorButton;
	}
	
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		String cmd = ureq.getParameter(VelocityContainer.COMMAND_ID);
		String fragment = ureq.getParameter("fragment");
		if(cmd != null && fragment != null && getComponentName().equals(fragment)) {
			switch(cmd) {
				case "edit_fragment":
					if(isEditMode()) {
						doCloseEditFragment();
					} else {
						doEditFragment(ureq, true);
						fireEvent(ureq, new EditElementEvent(pageElement.getId()));
					}
					break;
				case "inspect_fragment":
					break;
				case "add_element_above":
					String aboveLinkId = "o_cmore_".concat(getDispatchID());
					fireEvent(ureq, new OpenAddElementEvent(aboveLinkId, this, PageElementTarget.above));
					break;
				case "add_element_below":
					String belowLinkId = "o_cmore_".concat(getDispatchID());
					fireEvent(ureq, new OpenAddElementEvent(belowLinkId, this, PageElementTarget.below));
					break;
				case "save_element":
					doCloseEditFragment();
					fireEvent(ureq, new SaveElementEvent(this));
					break;
				case "clone_element":
					fireEvent(ureq, new CloneElementEvent(this));
					break;
				case "delete_element":
					fireEvent(ureq, new DeleteElementEvent(this));
					break;
				case "move_up":
					fireEvent(ureq, new MoveUpElementEvent(this));
					break;
				case "move_down":
					fireEvent(ureq, new MoveDownElementEvent(this));
					break;
				case "drop_fragment":
					doDropFragment(ureq);
					break;
				default:
					log.info("Dispatch fragment: {} {}", fragment, cmd);
					break;
			}
		}
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		//
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
		if(toggleInspectorButton == source) {
			if(isInspectorVisible()) {
				setInspectorVisible(false, false);
			} else {
				setInspectorVisible(true, false);
			}
		}
	}
	
	private void doDropFragment(UserRequest ureq) {
		String sourceId = ureq.getParameter("source");
		String position = ureq.getParameter("position");
		String content = ureq.getParameter("content");
		fireEvent(ureq, new DropToPageElementEvent(sourceId, this,
				PositionEnum.valueOf(position, PositionEnum.bottom), content));
	}
	
	private void doEditFragment(UserRequest ureq, boolean editFragmentMode) {
		boolean changed = this.editMode != editFragmentMode;
		this.editMode = editFragmentMode;
		if(changed) {
			setDirty(true);
			if(editFragmentMode) {
				setInspectorVisible(true, false);
				fireEvent(ureq, new EditPageElementEvent(this));
			}
		} else if(this.editMode && !isInspectorVisible()) {
			setInspectorVisible(true, false);
		}
	}
	
	private void doCloseEditFragment() {
		this.editMode = false;
		setDirty(true);
	}
	
	@Override
	public boolean hasInspector() {
		return inspectorPanel != null;
	}

	public Component getInspectorComponent() {
		return inspectorPanel;
	}

	@Override
	public Component getComponent(String name) {
		for(Component cmp:getComponents()) {
			if(name.equals(cmp.getComponentName())) {
				return cmp;
			}
		}
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		List<Component> components = new ArrayList<>(5);
		if(editorPart != null) {
			components.add(editorPart.getInitialComponent());
		}
		if(viewPart != null) {
			components.add(viewPart.getComponent());
		}
		if(inspectorPanel != null) {
			components.add(inspectorPanel);
		}
		components.add(toggleInspectorButton);
		return components;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	public void dispatchToEditor(UserRequest ureq, Event event) {
		if (editorPart != null) {
			editorPart.dispatchEvent(ureq, this, event);
		} else if (viewPart instanceof Controller controller) {
			controller.dispatchEvent(ureq, this, event);
		}
	}

	@Override
	public boolean isInForm() {
		return inForm;
	}

	@Override
	public void setInForm(boolean inForm) {
		this.inForm = inForm;
	}

	@Override
	public void positionInspector(String offsetLeft, String offsetTop) {
		if (inspectorPart instanceof ModalInspectorController modalInspectorController) {
			modalInspectorController.positionInspector(offsetLeft, offsetTop);
		}
	}
}
