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
package org.olat.course.nodes.iq;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipOutputStream;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NamedFileMediaResource;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.QTICourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.AssessmentTestInfos;
import org.olat.ims.qti21.model.DigitalSignatureOptions;
import org.olat.ims.qti21.ui.AssessmentEntryOutcomesListener;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingAssignmentStatus;
import org.olat.modules.grading.GradingService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This controller will make an archive of the current test entry results
 * and propose some informations.
 * 
 * 
 * Initial date: 24 mai 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmChangeResourceController extends FormBasicController {
	
	private static final String KEY_CONTROLLED_REPLACEMENT = "controlled";
	private static final String KEY_REPLACEMENT_ONLY = "only";
	
	private SingleSelection optionsEl;
	private StaticTextElement impactEl;
	private MultipleSelectionElement acknowledgeEl;
	
	private final ICourse course;
	private final QTICourseNode courseNode;
	private final RepositoryEntry newTestEntry;
	private final RepositoryEntry currentTestEntry;
	private final List<Identity> assessedIdentities;
	private final int numOfAssessedIdentities;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private GradingService gradingService;
	@Autowired
	private InstantMessagingService imService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public ConfirmChangeResourceController(UserRequest ureq, WindowControl wControl, ICourse course, QTICourseNode courseNode,
			RepositoryEntry newTestEntry, RepositoryEntry currentTestEntry, List<Identity> assessedIdentities, int numOfAssessedIdentities) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.course = course;
		this.courseNode = courseNode;
		this.newTestEntry = newTestEntry;
		this.currentTestEntry = currentTestEntry;
		this.assessedIdentities = assessedIdentities;
		this.numOfAssessedIdentities = numOfAssessedIdentities;
		initForm(ureq);
	}
	
	public RepositoryEntry getNewTestEntry() {
		return newTestEntry;
	}
	
	public RepositoryEntry getCurrentTestEntry() {
		return currentTestEntry;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Tests names
		FormLayoutContainer entriesCont = uifactory.addDefaultFormLayout("entries", null, formLayout);
		String currentNames = buildNames(currentTestEntry);
		uifactory.addStaticTextElement("change.current.test", currentNames, entriesCont);
		String newNames = buildNames(newTestEntry);
		uifactory.addStaticTextElement("change.new.test", newNames, entriesCont);
		
		// Options
		initFormOptions(formLayout);
		// Table of properties
		initFormProperties(formLayout);
		// Confirmation
		initFormConfirmation(formLayout, ureq);
	}
	
	private void initFormOptions(FormItemContainer formLayout) {
		FormLayoutContainer optionsCont = uifactory.addDefaultFormLayout("options", null, formLayout);
		optionsCont.setFormTitle(translate("change.options.title"));
		
		SelectionValues optionsPK = new SelectionValues();
		optionsPK.add(SelectionValues.entry(KEY_CONTROLLED_REPLACEMENT, translate("change.option.controlled")));
		optionsPK.add(SelectionValues.entry(KEY_REPLACEMENT_ONLY, translate("change.option.replacement.only")));
		optionsEl = uifactory.addCardSingleSelectHorizontal("change.options", "change.options", optionsCont, optionsPK);
		optionsEl.addActionListener(FormEvent.ONCHANGE);
		optionsEl.select(KEY_CONTROLLED_REPLACEMENT, true);
		
		impactEl = uifactory.addStaticTextElement("change.impact", "", optionsCont);
		impactEl.setDomWrapperElement(DomWrapperElement.div);
		updateUI();
	}
	
	private void initFormProperties(FormItemContainer formLayout) {
		final RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		
		String page = velocity_root + "/confirm_change_properties.html";
		FormLayoutContainer propertiesCont = uifactory.addCustomFormLayout("properties", null, page, formLayout);
		propertiesCont.setFormTitle(translate("change.properties.title"));
		
		// Headers
		propertiesCont.contextPut("current", translate("change.properties.current",
				StringHelper.escapeHtml(currentTestEntry.getDisplayname())));
		propertiesCont.contextPut("new", translate("change.properties.new",
				StringHelper.escapeHtml(newTestEntry.getDisplayname())));
		
		AssessmentTestInfos currentTestInfos = qtiService.getAssessmentTestInfos(currentTestEntry);
		AssessmentTestInfos newTestInfos = qtiService.getAssessmentTestInfos(newTestEntry);
		
		// Manual scoring
		String currentScoring = manualValue(currentTestInfos.manualCorrections());
		String newScoring = manualValue(newTestInfos.manualCorrections());
		propertiesCont.contextPut("currentScoring", currentScoring);
		propertiesCont.contextPut("newScoring", newScoring);
		if(!Objects.equals(currentScoring, newScoring)) {
			propertiesCont.contextPut("messageScoring", translate("warning.change.scoring"));
			propertiesCont.setFormWarning(translate("warning.change.important"));
		}
		
		// Max score
		String currentMaxScore = roundedValue(currentTestInfos.maxScore());
		propertiesCont.contextPut("currentMaxScore", translateRoundedValue(currentMaxScore, "change.max.score"));
		String newMaxScore = roundedValue(newTestInfos.maxScore());
		propertiesCont.contextPut("newMaxScore", translateRoundedValue(newMaxScore, "change.max.score"));
		if(!Objects.equals(currentMaxScore, newMaxScore)) {
			propertiesCont.contextPut("messageMaxScore", translate("warning.change.max.score"));
		}
		
		// Cut value
		String currentCutValue = roundedValue(currentTestInfos.cutValue());
		propertiesCont.contextPut("currentCutValue", translateRoundedValue(currentCutValue, "score.passed.cut.value"));
		String newCutValue = roundedValue(newTestInfos.cutValue());
		propertiesCont.contextPut("newCutValue", translateRoundedValue(newCutValue, "score.passed.cut.value"));
		if(!Objects.equals(currentCutValue, newCutValue)) {
			propertiesCont.contextPut("messageCutValue", translate("warning.change.cut.value"));
		}
		
		// Runs
		long currentRuns = countActiveRuns(courseEntry, currentTestEntry);
		propertiesCont.contextPut("currentRuns", Long.toString(currentRuns));
		long newRuns = countActiveRuns(courseEntry, newTestEntry);
		propertiesCont.contextPut("newRuns", Long.toString(newRuns));
	}
	
	private long countActiveRuns(RepositoryEntry courseEntry, RepositoryEntry testEntry) {
		List<AssessmentTestSession> sessions = qtiService.getAssessmentTestSessions(courseEntry, courseNode.getIdent(), testEntry);
		
		long runs = 0l;
		for(AssessmentTestSession session:sessions) {
			if(!session.isCancelled() && !session.isExploded() && session.getFinishTime() == null
					&& session.getTerminationTime() == null && session.getFinishTime() == null) {
				runs++;
			}
		}
		return runs;
	}

	private String manualValue(boolean manualScoring) {
		return manualScoring ? translate("correction.test.entry.manually") : translate("correction.test.entry.auto");
	}
	
	private String roundedValue(Double val) {
		return val == null ? "" : AssessmentHelper.getRoundedScore(val);
	}
	
	private String translateRoundedValue(String val, String i18nKey) {
		return StringHelper.containsNonWhitespace(val) ? translate(i18nKey, val) : "";
	}
	
	private void initFormConfirmation(FormItemContainer formLayout, UserRequest ureq) {
		FormLayoutContainer confirmCont = uifactory.addDefaultFormLayout("confirmation", null, formLayout);
		
		SelectionValues onKV = new SelectionValues();
		onKV.add(SelectionValues.entry("on", translate("confirmation.change.acknowledge")));
		acknowledgeEl = uifactory.addCheckboxesHorizontal("acknowledge", "confirmation.change", confirmCont, onKV.keys(), onKV.values());
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, confirmCont);
		uifactory.addFormSubmitButton("replace.publish", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private String buildNames(RepositoryEntry entry) {
		StringBuilder sb = new StringBuilder();
		sb.append(StringHelper.escapeHtml(entry.getDisplayname()));
		if(StringHelper.containsNonWhitespace(entry.getExternalRef())) {
			sb.append("<span class='mute'> \u00B7 ")
			  .append(StringHelper.escapeHtml(entry.getExternalRef()))
			  .append("</span>");
		}
		return sb.toString();
	}
	
	private File prepareArchive(UserRequest ureq) {
		File exportDir = CourseFactory.getOrCreateDataExportDirectory(ureq.getIdentity(), course.getCourseTitle());
		String label = StringHelper.transformDisplayNameToFileSystemName(courseNode.getShortName())
				+ "_" + Formatter.formatDatetimeWithMinutes(new Date()) + ".zip";
		String urlEncodedLabel = StringHelper.urlEncodeUTF8(label);
		File archiveFile = new File(exportDir, urlEncodedLabel);
		
		try(OutputStream out= new FileOutputStream(archiveFile);
				ZipOutputStream exportStream = new ZipOutputStream(out)) {
			courseNode.archiveNodeData(getLocale(), course, null, exportStream, "", "UTF8");
			return archiveFile;
		} catch(Exception e) {
			logError("", e);
			return null;
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		acknowledgeEl.clearError();
		if(!acknowledgeEl.isAtLeastSelected(1)) {
			acknowledgeEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		optionsEl.clearError();
		if(!optionsEl.isOneSelected()) {
			optionsEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}
	
	private void updateUI() {
		if(optionsEl.isOneSelected() && KEY_REPLACEMENT_ONLY.equals(optionsEl.getSelectedKey())) {
			impactEl.setValue(translate("change.impact.replacement.only", Integer.toString(numOfAssessedIdentities)));
		} else {
			impactEl.setValue(translate("change.impact.controlled", Integer.toString(numOfAssessedIdentities)));
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(optionsEl.isOneSelected()) {
			if(KEY_REPLACEMENT_ONLY.equals(optionsEl.getSelectedKey())) {
				doReplacementOnly(ureq);
			} else if(KEY_CONTROLLED_REPLACEMENT.equals(optionsEl.getSelectedKey())) {
				doControlledReplace(ureq);
			}
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(optionsEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doReplacementOnly(UserRequest ureq) {
		// pull running test sessions and set as cancelled
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();

		for(Identity assessedIdentity:assessedIdentities) {
			IdentityEnvironment ienv = new IdentityEnvironment(assessedIdentity, Roles.userRoles());
			UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, courseEnv);
			
			// Cancel test sessions and grading assignment
			List<AssessmentTestSession> sessions = qtiService.getAssessmentTestSessions(courseEntry, courseNode.getIdent(), assessedIdentity, true);
			if(!sessions.isEmpty()) {
				AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, uce);
				for (AssessmentTestSession session:sessions) {
					if (!newTestEntry.equals(session.getTestEntry())
							&& !session.isCancelled() && !session.isExploded()
							&& session.getFinishTime() == null && session.getTerminationTime() == null) {
						session = pullSession(session, assessedIdentity, courseEnv);
						session.setCancelled(true);
						session = qtiService.updateAssessmentTestSession(session);
						deactivateGradingAssignment(assessmentEntry, session);
					}
				}
			}
			dbInstance.commitAndCloseSession();
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doControlledReplace(UserRequest ureq) {
		// reset the data
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		File downloadArchiveFile = prepareArchive(ureq);
		
		for(Identity assessedIdentity:assessedIdentities) {
			IdentityEnvironment ienv = new IdentityEnvironment(assessedIdentity, Roles.userRoles());
			UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, courseEnv);
			
			// Cancel test sessions and grading assignment
			List<AssessmentTestSession> sessions = qtiService.getAssessmentTestSessions(courseEntry, courseNode.getIdent(), assessedIdentity, true);
			if(!sessions.isEmpty()) {
				AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, uce);
				for (AssessmentTestSession session:sessions) {
					if (!newTestEntry.equals(session.getTestEntry())
							&& !session.isCancelled() && !session.isExploded()) {
						if(session.getFinishTime() == null && session.getTerminationTime() == null) {
							session = pullSession(session, assessedIdentity, courseEnv);
						}
						session.setCancelled(true);
						session = qtiService.updateAssessmentTestSession(session);
						deactivateGradingAssignment(assessmentEntry, session);
					}
				}
			}
			
			ScoreEvaluation scoreEval = new ScoreEvaluation(null, null, null, null, null, null, null,
					AssessmentEntryStatus.notStarted, Boolean.FALSE, null, 0.0d, AssessmentRunStatus.notStarted, null);
			courseAssessmentService.updateScoreEvaluation(courseNode, scoreEval, uce, getIdentity(), false,
					Role.coach);
			courseAssessmentService.updateCurrentCompletion(courseNode, uce, null, null, AssessmentRunStatus.notStarted,
					Role.coach);
			courseAssessmentService.updateAttempts(courseNode, 0, null, uce, getIdentity(),
					Role.coach);
			dbInstance.commitAndCloseSession();
		}
		
		// replacement is done by the parent controller
		fireEvent(ureq, Event.DONE_EVENT);

		if(downloadArchiveFile != null) {
			MediaResource archiveResource = new NamedFileMediaResource(downloadArchiveFile, downloadArchiveFile.getName(), downloadArchiveFile.getName(), false);
			Command downloadCmd = CommandFactory.createDownloadMediaResource(ureq, archiveResource);
			getWindowControl().getWindowBackOffice().sendCommandTo(downloadCmd);
		}
	}
	
	private AssessmentTestSession pullSession(AssessmentTestSession session, Identity assessedIdentity, CourseEnvironment courseEnv) {
		//reload it to prevent lazy loading issues
		session = qtiService.pullSession(session, getSignatureOptions(session, courseEnv), getIdentity());
		
		String channel = assessedIdentity == null ? session.getAnonymousIdentifier() : assessedIdentity.getKey().toString();
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		imService.endChannel(getIdentity(), courseEntry.getOlatResource(), courseNode.getIdent(), channel);

		return session;
	}
	
	private DigitalSignatureOptions getSignatureOptions(AssessmentTestSession session, CourseEnvironment courseEnv) {
		RepositoryEntry testEntry = session.getTestEntry();
		QTI21DeliveryOptions deliveryOptions = qtiService.getDeliveryOptions(testEntry);
		
		boolean digitalSignature = deliveryOptions.isDigitalSignature();
		boolean sendMail = deliveryOptions.isDigitalSignatureMail();
		if(courseNode != null) {
			ModuleConfiguration config = courseNode.getModuleConfiguration();
			digitalSignature = config.getBooleanSafe(IQEditController.CONFIG_DIGITAL_SIGNATURE,
					deliveryOptions.isDigitalSignature());
			sendMail = config.getBooleanSafe(IQEditController.CONFIG_DIGITAL_SIGNATURE_SEND_MAIL,
					deliveryOptions.isDigitalSignatureMail());
		}
		
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		DigitalSignatureOptions options = new DigitalSignatureOptions(digitalSignature, sendMail, courseEntry, testEntry);
		if(digitalSignature) {
			if(courseNode == null) {
				 AssessmentEntryOutcomesListener.decorateResourceConfirmation(courseEntry, testEntry, session, options, null, getLocale());
			} else {
				QTI21AssessmentRunController.decorateCourseConfirmation(session, options, courseEnv, courseNode, testEntry, null, getLocale());
			}
		}
		return options;
	}
	
	private void deactivateGradingAssignment(AssessmentEntry assessmentEntry, AssessmentTestSession session) {
		if (gradingService.isGradingEnabled(session.getTestEntry(), null)) {
			GradingAssignment assignment = gradingService.getGradingAssignment(session.getTestEntry(), assessmentEntry);
			if (assignment != null && session.getKey().equals(assessmentEntry.getAssessmentId())) {
				GradingAssignmentStatus assignmentStatus = assignment.getAssignmentStatus();
				if (assignmentStatus == GradingAssignmentStatus.assigned
						|| assignmentStatus == GradingAssignmentStatus.inProcess
						|| assignmentStatus == GradingAssignmentStatus.done) {
					gradingService.deactivateAssignment(assignment);
				}
			}
		}
	}
}