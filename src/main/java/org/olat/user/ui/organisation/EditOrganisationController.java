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
package org.olat.user.ui.organisation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.OrganisationManagedFlag;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.OrganisationType;
import org.olat.basesecurity.OrganisationTypeToType;
import org.olat.basesecurity.model.OrganisationTypeRefImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class EditOrganisationController extends FormBasicController {

	private RichTextElement descriptionEl;
	private TextElement identifierEl;
	private TextElement locationEl;
	private TextElement displayNameEl;
	private SingleSelection organisationTypeEl;
	
	private Organisation organisation;
	private final Organisation parentOrganisation;
	
	@Autowired
	private OrganisationService organisationService;
	
	public EditOrganisationController(UserRequest ureq, WindowControl wControl, Organisation organisation) {
		super(ureq, wControl);
		parentOrganisation = null;
		this.organisation = organisation;
		initForm(ureq);
	}
	
	public EditOrganisationController(UserRequest ureq, WindowControl wControl, Organisation organisation, Organisation parentOrganisation) {
		super(ureq, wControl);
		this.organisation = organisation;
		this.parentOrganisation = parentOrganisation;
		initForm(ureq);
	}
	
	public Organisation getOrganisation() {
		return organisation;
	}
	
	private List<OrganisationType> getTypes() {
		List<OrganisationType> types = new ArrayList<>();
		if(organisation != null) {
			List<Organisation> parentLine = organisationService.getOrganisationParentLine(organisation);
			for(int i=parentLine.size() - 1; i-->0; ) {
				Organisation parent = parentLine.get(i);
				OrganisationType parentType = parent.getType();
				if(parentType != null) {
					Set<OrganisationTypeToType> typeToTypes = parentType.getAllowedSubTypes();
					for(OrganisationTypeToType typeToType:typeToTypes) {
						if(typeToType != null) {
							types.add(typeToType.getAllowedSubOrganisationType());
						}
					}
					break;
				}
			}
		}
		if(types.isEmpty()) {
			types.addAll(organisationService.getOrganisationTypes());
		} else if(organisation != null && organisation.getType() != null) {
			if(!types.contains(organisation.getType())) {
				types.add(organisation.getType());
			}
		}
		return types;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(organisation != null) {
			String key = organisation.getKey().toString();
			uifactory.addStaticTextElement("organisation.key", key, formLayout);
			String externalId = organisation.getExternalId();
			uifactory.addStaticTextElement("organisation.external.id", externalId, formLayout);
		}
		
		String identifier = organisation == null ? "" : organisation.getIdentifier();
		identifierEl = uifactory.addTextElement("organisation.identifier", "organisation.identifier", 255, identifier, formLayout);
		identifierEl.setEnabled(!OrganisationManagedFlag.isManaged(organisation, OrganisationManagedFlag.identifier));
		identifierEl.setMandatory(true);
		identifierEl.addActionListener(FormEvent.ONCHANGE);

		String displayName = organisation == null ? "" : organisation.getDisplayName();
		displayNameEl = uifactory.addTextElement("organisation.displayName", "organisation.displayName", 255, displayName, formLayout);
		displayNameEl.setEnabled(!OrganisationManagedFlag.isManaged(organisation, OrganisationManagedFlag.displayName));
		displayNameEl.setMandatory(true);
		
		List<OrganisationType> types = getTypes();
		String[] typeKeys = new String[types.size() + 1];
		String[] typeValues = new String[types.size() + 1];
		typeKeys[0] = "";
		typeValues[0] = "-";
		for(int i=types.size(); i-->0; ) {
			typeKeys[i+1] = types.get(i).getKey().toString();
			typeValues[i+1] = types.get(i).getDisplayName();
		}
		organisationTypeEl = uifactory.addDropdownSingleselect("organisation.type", "organisation.type", formLayout, typeKeys, typeValues, null);
		organisationTypeEl.setEnabled(!OrganisationManagedFlag.isManaged(organisation, OrganisationManagedFlag.type));
		boolean typeFound = false;
		if(organisation != null && organisation.getType() != null) {
			String selectedTypeKey = organisation.getType().getKey().toString();
			for(String typeKey:typeKeys) {
				if(typeKey.equals(selectedTypeKey)) {
					organisationTypeEl.select(selectedTypeKey, true);
					typeFound = true;
					break;
				}
			}
		}
		if(!typeFound) {
			organisationTypeEl.select(typeKeys[0], true);
		}
		
		String location = organisation == null ? "" : organisation.getLocation();
		locationEl = uifactory.addTextElement("organisation.location", "organisation.location", 255, location, formLayout);
		locationEl.setEnabled(!OrganisationManagedFlag.isManaged(organisation, OrganisationManagedFlag.location));
		
		String description = organisation == null ? "" : organisation.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringDataCompact("organisation.description", "organisation.description", description, 10, 60, null,
				formLayout, ureq.getUserSession(), getWindowControl());
		descriptionEl.setEnabled(!OrganisationManagedFlag.isManaged(organisation, OrganisationManagedFlag.description));
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateTextfield(displayNameEl, 255, true);
		allOk &= validateTextfield(identifierEl, 64, true);
		allOk &= validateIdentifierField(false);

		return allOk;
	}
	
	private boolean validateTextfield(TextElement textEl, int maxSize, boolean mandatory) {
		boolean allOk = true;
		
		textEl.clearError();
		if(mandatory && !StringHelper.containsNonWhitespace(textEl.getValue())) {
			identifierEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(textEl.getValue().length() >= maxSize) {
			textEl.setErrorKey("form.error.toolong", Integer.toString(maxSize));
			allOk &= false;
		}
		
		return allOk;
	}

	private boolean validateIdentifierField(boolean showWarnings) {
		boolean allOk = true;

		String identifier = identifierEl.getValue().trim();
		identifierEl.clearError();
		identifierEl.clearWarning();

		if (!StringHelper.containsNonWhitespace(identifier)) return true;

		List<Organisation> existing = organisationService.findOrganisationByIdentifier(identifier);
		if (organisation != null) {
			existing.removeIf(o -> o.getKey().equals(organisation.getKey()));
		}

		if (OrganisationService.DEFAULT_ORGANISATION_IDENTIFIER.equals(identifier)) {
			if (!existing.isEmpty()) {
				identifierEl.setErrorKey("error.defaultorg.exists", OrganisationService.DEFAULT_ORGANISATION_IDENTIFIER);
				allOk = false;
			}
		} else {
			if (!existing.isEmpty() && showWarnings) {
				identifierEl.setWarningKey("warning.identifier.duplicate", identifier);
			}
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		OrganisationType organisationType = null;
		String selectedTypeKey = organisationTypeEl.getSelectedKey();
		if(StringHelper.containsNonWhitespace(selectedTypeKey)) {
			organisationType = organisationService
					.getOrganisationType(new OrganisationTypeRefImpl(Long.valueOf(selectedTypeKey)));
		}

		if(organisation == null) {
			//create a new one
			organisation = organisationService
					.createOrganisation(displayNameEl.getValue(), identifierEl.getValue(), descriptionEl.getValue(), parentOrganisation,
							organisationType, getIdentity());
			if (StringHelper.containsNonWhitespace(locationEl.getValue())) {
				organisation.setLocation(locationEl.getValue());
				organisation = organisationService.updateOrganisation(organisation);
			}
		} else {
			organisation = organisationService.getOrganisation(organisation);
			organisation.setIdentifier(identifierEl.getValue());
			organisation.setLocation(locationEl.getValue());
			organisation.setDisplayName(displayNameEl.getValue());
			organisation.setDescription(descriptionEl.getValue());
			organisation.setType(organisationType);
			organisation = organisationService.updateOrganisation(organisation);
		}

		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == identifierEl) {
			validateIdentifierField(true);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}