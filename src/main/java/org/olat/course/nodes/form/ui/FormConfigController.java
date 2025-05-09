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
package org.olat.course.nodes.form.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.olat.NewControllerFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailHelper;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.duedate.ui.DueDateConfigFormItem;
import org.olat.course.duedate.ui.DueDateConfigFormatter;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.nodes.SurveyCourseNode;
import org.olat.course.nodes.form.FormManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.04.2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FormConfigController extends FormBasicController {
	
	private static final String[] ON_KEYS = new String[]{ "on" };

	private StaticTextElement evaluationFormNotChoosen;
	private FormLink evaluationFormLink;
	private FormLink chooseLink;
	private FormLink replaceLink;
	private FormLink editLink;
	private FormToggle multiExecutionEl;
	private MultipleSelectionElement relativeDatesEl;
	private DueDateConfigFormItem participationDeadlineEl;
	private MultipleSelectionElement confirmationEl;
	private TextElement externalMailTextEl;
	
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController searchCtrl;
	private LayoutMain3ColsPreviewController previewCtr;
	
	private final FormCourseNode formCourseNode;
	private final ModuleConfiguration config;
	private final List<String> relativeToDates;
	private final EvaluationFormSurveyIdentifier surveyIdent;
	private EvaluationFormSurvey survey;
	private RepositoryEntry formEntry;
	
	@Autowired
	private FormManager formManager;

	public FormConfigController(UserRequest ureq, WindowControl wControl, FormCourseNode formCourseNode,
			RepositoryEntry courseEntry) {
		super(ureq, wControl);
		this.formCourseNode = formCourseNode;
		setTranslator(Util.createPackageTranslator(getTranslator(), DueDateConfigFormItem.class, getLocale()));
		this.config = formCourseNode.getModuleConfiguration();
		this.relativeToDates = formManager.getRelativeToDateTypes(courseEntry);
		this.surveyIdent = formManager.getSurveyIdentifier(formCourseNode, courseEntry);
		this.survey = formManager.loadSurvey(surveyIdent);
		if (this.survey != null) {
			this.formEntry = survey.getFormEntry();
		}
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("edit.title");
		setFormContextHelp("manual_user/learningresources/Course_Element_Form/");
		formLayout.setElementCssClass("o_sel_form_config_form");
		
		evaluationFormNotChoosen = uifactory.addStaticTextElement("edit.evaluation.form.not.choosen", "edit.evaluation.form",
				translate("edit.evaluation.form.not.choosen"), formLayout);
		evaluationFormLink = uifactory.addFormLink("edit.evaluation.form", "", translate("edit.evaluation.form"), formLayout,
				Link.NONTRANSLATED);
		evaluationFormLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("eva.buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		chooseLink = uifactory.addFormLink("edit.choose", buttonsCont, "btn btn-default o_xsmall");
		chooseLink.setElementCssClass("o_sel_form_choose_repofile");
		replaceLink = uifactory.addFormLink("edit.replace", buttonsCont, "btn btn-default o_xsmall");
		editLink = uifactory.addFormLink("edit.edit", buttonsCont, "btn btn-default o_xsmall");
		
		multiExecutionEl = uifactory.addToggleButton("multi.execution", "edit.multi.execution", translate("on"), translate("off"), formLayout);
		multiExecutionEl.toggle(config.getBooleanSafe(FormCourseNode.CONFIG_KEY_MULTI_PARTICIPATION));
		
		relativeDatesEl = uifactory.addCheckboxesHorizontal("relative.dates", "relative.dates", formLayout, ON_KEYS, new String[]{ "" });
		relativeDatesEl.addActionListener(FormEvent.ONCHANGE);
		boolean useRelativeDates = config.getBooleanSafe(FormCourseNode.CONFIG_KEY_RELATIVE_DATES);
		relativeDatesEl.select(ON_KEYS[0], useRelativeDates);
		
		SelectionValues relativeToDatesKV = new SelectionValues();
		DueDateConfigFormatter.create(getLocale()).addCourseRelativeToDateTypes(relativeToDatesKV, relativeToDates);
		participationDeadlineEl = DueDateConfigFormItem.create("edit.participation.deadline", relativeToDatesKV,
				useRelativeDates, formCourseNode.getDueDateConfig(FormCourseNode.CONFIG_KEY_PARTICIPATION_DEADLINE));
		participationDeadlineEl.setLabel("edit.participation.deadline", null);
		formLayout.add(participationDeadlineEl);

		String[] confirmationKeys = new String[]{
				FormCourseNode.CONFIG_KEY_CONFIRMATION_OWNERS,
				FormCourseNode.CONFIG_KEY_CONFIRMATION_COACHES,
				FormCourseNode.CONFIG_KEY_CONFIRMATION_PARTICIPANT,
				FormCourseNode.CONFIG_KEY_CONFIRMATION_EXTERNAL
		};
		String[] confirmationValues = new String[]{
				translate("edit.confirmation.owners"),
				translate("edit.confirmation.coaches"),
				translate("edit.confirmation.participant"),
				translate("edit.confirmation.external")
		};
		
		confirmationEl = uifactory.addCheckboxesVertical("edit.confirmation.enabled", formLayout, confirmationKeys,
				confirmationValues, 1);
		confirmationEl.setHelpTextKey("edit.confirmation.help", null);

		boolean isConfirmationOwnersSelected = config.getBooleanSafe(FormCourseNode.CONFIG_KEY_CONFIRMATION_OWNERS);
		boolean isConfirmationCoachesSelected = config.getBooleanSafe(FormCourseNode.CONFIG_KEY_CONFIRMATION_COACHES);
		boolean isConfirmationParticipantsSelected = config.getBooleanSafe(FormCourseNode.CONFIG_KEY_CONFIRMATION_PARTICIPANT);
		boolean isConfirmationExternalMailsSelected = config.getBooleanSafe(FormCourseNode.CONFIG_KEY_CONFIRMATION_EXTERNAL);
		String confirmationExternalMails = config.getStringValue(FormCourseNode.CONFIG_KEY_CONFIRMATION_EXTERNAL_MAILS);

		confirmationEl.select(confirmationKeys[0], isConfirmationOwnersSelected);
		confirmationEl.select(confirmationKeys[1], isConfirmationCoachesSelected);
		confirmationEl.select(confirmationKeys[2], isConfirmationParticipantsSelected);
		confirmationEl.select(confirmationKeys[3], isConfirmationExternalMailsSelected);

		confirmationEl.addActionListener(FormEvent.ONCHANGE);

		externalMailTextEl = uifactory.addTextElement("external.mails", "", -1, confirmationExternalMails, formLayout);
		externalMailTextEl.setPlaceholderKey("edit.confirmation.external.placeholder", null);
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		uifactory.addFormSubmitButton("save", "save", buttonCont);
	}
	
	private void updateUI() {
		boolean replacePossible = formManager.isFormUpdateable(survey);
		boolean formSelected = formEntry != null;
		
		if (survey != null && formEntry == null) {
			showError("error.repo.entry.missing");
		}
		
		if (formEntry != null) {
			String displayname = StringHelper.escapeHtml(formEntry.getDisplayname());
			evaluationFormLink.setI18nKey(displayname);
			flc.setDirty(true);
		}
		evaluationFormNotChoosen.setVisible(!formSelected);
		chooseLink.setVisible(!formSelected);
		evaluationFormLink.setVisible(formSelected);
		replaceLink.setVisible(formSelected && replacePossible);
		editLink.setVisible(formSelected);
		externalMailTextEl.setVisible(confirmationEl.isKeySelected(FormCourseNode.CONFIG_KEY_CONFIRMATION_EXTERNAL));
	}
	
	private void updateParticipationDeadlineUI() {
		boolean useRelativeDate = relativeDatesEl.isAtLeastSelected(1);
		participationDeadlineEl.setRelative(useRelativeDate);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == chooseLink || source == replaceLink) {
			doChooseEvaluationForm(ureq);
		} else if (source == editLink) {
			doEditEvaluationForm(ureq);
		} else if (source == evaluationFormLink) {
			doPreviewEvaluationForm(ureq);
		} else if(relativeDatesEl == source) {
			updateParticipationDeadlineUI();
		}
		updateUI();
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (searchCtrl == source) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				doReplaceEvaluationForm();
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == previewCtr) {
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(previewCtr);
		removeAsListenerAndDispose(searchCtrl);
		removeAsListenerAndDispose(cmc);
		previewCtr = null;
		searchCtrl = null;
		cmc = null;
	}

	private void doChooseEvaluationForm(UserRequest ureq) {
		searchCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq,
				EvaluationFormResource.TYPE_NAME, translate("edit.choose.evaluation.form"));
		this.listenTo(searchCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				searchCtrl.getInitialComponent(), true, translate("edit.choose.evaluation.form"));
		cmc.activate();
	}

	private void doReplaceEvaluationForm() {
		RepositoryEntry selectedFormEntry = searchCtrl.getSelectedEntry();
		if (selectedFormEntry != null) {
			this.formEntry = selectedFormEntry;
			updateUI();
			markDirty();
		}
	}

	private void doEditEvaluationForm(UserRequest ureq) {
		if (formEntry == null) {
			showError("error.repo.entry.missing");
		} else {
			String bPath = "[RepositoryEntry:" + formEntry.getKey() + "][Editor:0]";
			NewControllerFactory.getInstance().launch(bPath, ureq, getWindowControl());
		}
	}

	private void doPreviewEvaluationForm(UserRequest ureq) {
		File formFile = formManager.getFormFile(formEntry);
		DataStorage storage = formManager.loadStorage(formEntry);
		Controller controller = new EvaluationFormExecutionController(ureq, getWindowControl(), formFile, storage,
				FormCourseNode.EMPTY_STATE);
		
		previewCtr = new LayoutMain3ColsPreviewController(ureq, getWindowControl(), null,
				controller.getInitialComponent(), null);
		previewCtr.addDisposableChildController(controller);
		previewCtr.activate();
		listenTo(previewCtr);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		if (confirmationEl.isKeySelected(FormCourseNode.CONFIG_KEY_CONFIRMATION_EXTERNAL)) {
			allOk &= isValidEMail();
		}

		evaluationFormLink.clearError();
		if (survey != null && formEntry != null) {
			boolean isFormUpdateable = formManager.isFormUpdateable(survey);
			if (!isFormUpdateable && !formEntry.getKey().equals(survey.getFormEntry().getKey())) {
				evaluationFormLink.setErrorKey("error.repo.entry.not.replaceable");
				allOk &= false;
			}
		}
		
		allOk &= participationDeadlineEl.validate();

		return allOk;
	}

	private boolean isValidEMail() {
		boolean allOk = true;

		externalMailTextEl.clearError();
		List<String> externalMails =
				Stream.of(externalMailTextEl.getValue().split(","))
				.collect(Collectors.toCollection(ArrayList<String>::new));

		for (String externalMail : externalMails) {
			if (!MailHelper.isValidEmailAddress(externalMail)) {
				externalMailTextEl.setErrorKey("external.mail.invalid");
				allOk = false;
			}
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (formEntry != null) {
			if (survey == null) {
				survey = formManager.createSurvey(surveyIdent, formEntry);
			} else {
				survey = formManager.updateSurveyForm(survey, formEntry);
			}
			SurveyCourseNode.setEvaluationFormReference(formEntry, config);
		}
		
		boolean relativeDates = relativeDatesEl.isAtLeastSelected(1);
		config.setBooleanEntry(FormCourseNode.CONFIG_KEY_RELATIVE_DATES, relativeDates);
		
		config.setBooleanEntry(FormCourseNode.CONFIG_KEY_MULTI_PARTICIPATION, multiExecutionEl.isOn());
		
		DueDateConfig dueDateConfig = participationDeadlineEl.getDueDateConfig();
		config.setIntValue(FormCourseNode.CONFIG_KEY_PARTICIPATION_DEADLINE_RELATIVE, dueDateConfig.getNumOfDays());
		config.setStringValue(FormCourseNode.CONFIG_KEY_PARTICIPATION_DEADLINE_RELATIVE_TO, dueDateConfig.getRelativeToType());
		config.setDateValue(FormCourseNode.CONFIG_KEY_PARTICIPATION_DEADLINE, dueDateConfig.getAbsoluteDate());

		config.setBooleanEntry(FormCourseNode.CONFIG_KEY_CONFIRMATION_OWNERS, confirmationEl.isKeySelected(FormCourseNode.CONFIG_KEY_CONFIRMATION_OWNERS));
		config.setBooleanEntry(FormCourseNode.CONFIG_KEY_CONFIRMATION_COACHES, confirmationEl.isKeySelected(FormCourseNode.CONFIG_KEY_CONFIRMATION_COACHES));
		config.setBooleanEntry(FormCourseNode.CONFIG_KEY_CONFIRMATION_PARTICIPANT, confirmationEl.isKeySelected(FormCourseNode.CONFIG_KEY_CONFIRMATION_PARTICIPANT));
		config.setBooleanEntry(FormCourseNode.CONFIG_KEY_CONFIRMATION_EXTERNAL, confirmationEl.isKeySelected(FormCourseNode.CONFIG_KEY_CONFIRMATION_EXTERNAL));
		if (confirmationEl.isKeySelected(FormCourseNode.CONFIG_KEY_CONFIRMATION_EXTERNAL)) {
			config.setStringValue(FormCourseNode.CONFIG_KEY_CONFIRMATION_EXTERNAL_MAILS, externalMailTextEl.getValue());
		} else {
			config.setStringValue(FormCourseNode.CONFIG_KEY_CONFIRMATION_EXTERNAL_MAILS, "");
			externalMailTextEl.setValue("");
		}

		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

}
