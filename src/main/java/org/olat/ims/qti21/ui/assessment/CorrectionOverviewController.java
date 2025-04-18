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
package org.olat.ims.qti21.ui.assessment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.ButtonGroupComponent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.course.archiver.ScoreAccountingHelper;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.group.BusinessGroupService;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.xml.ManifestBuilder;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.modules.assessment.ui.ReferenceHistoryFilterController;
import org.olat.modules.assessment.ui.event.CompleteAssessmentTestSessionEvent;
import org.olat.modules.assessment.ui.event.ReferencesHistorySelectionEvent;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * The overview to correct all the users of a course element.
 * 
 * 
 * Initial date: 26 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CorrectionOverviewController extends BasicController implements TooledController {
	
	private RepositoryEntry courseEntry;
	private CorrectionOverviewModel model;
	private final CourseEnvironment courseEnv;
	private final AssessmentToolOptions asOptions;
	private final IQTESTCourseNode courseNode;
	
	private final Link identitiesLink;
	private final Link assessmentItemsLink;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackPanel;
	private ButtonGroupComponent segmentButtonsCmp;
	
	private CorrectionIdentityListController identityListCtrl;
	private ReferenceHistoryFilterController referencesHistoryCtrl;
	private CorrectionAssessmentItemListController assessmentItemsCtrl;
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	public CorrectionOverviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			CourseEnvironment courseEnv, AssessmentToolOptions asOptions, IQTESTCourseNode courseNode) {
		super(ureq, wControl);
		
		this.courseEnv = courseEnv;
		this.asOptions = asOptions;
		this.stackPanel = stackPanel;
		this.courseNode = courseNode;
		RepositoryEntry testEntry = courseNode.getReferencedRepositoryEntry();
		courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		initModel(testEntry);
		
		segmentButtonsCmp = new ButtonGroupComponent("segments");
		assessmentItemsLink = LinkFactory.createLink("correction.assessment.items", getTranslator(), this);
		assessmentItemsLink.setElementCssClass("o_sel_correction_assessment_items");
		segmentButtonsCmp.addButton(assessmentItemsLink, true);
		identitiesLink = LinkFactory.createLink("correction.assessed.identities", getTranslator(), this);
		identitiesLink.setElementCssClass("o_sel_correction_identities");
		segmentButtonsCmp.addButton(identitiesLink, false);

		mainVC = createVelocityContainer("overview_corrections");
		
		if(courseNode != null) {
			referencesHistoryCtrl = new ReferenceHistoryFilterController(ureq, getWindowControl(),
					courseEntry, courseNode, testEntry);
			listenTo(referencesHistoryCtrl);
			mainVC.put("referencesHistory", referencesHistoryCtrl.getInitialComponent());
		}
	
		putInitialPanel(mainVC);
		doOpenAssessmentItemList(ureq);
	}
	
	private void initModel(RepositoryEntry testEntry) {
		File fUnzippedDirRoot = FileResourceManager.getInstance()
				.unzipFileResource(testEntry.getOlatResource());
		ResolvedAssessmentTest resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(fUnzippedDirRoot, false, false);
		ManifestBuilder manifestBuilder = ManifestBuilder.read(new File(fUnzippedDirRoot, "imsmanifest.xml"));

		Map<Identifier, AssessmentItemRef> identifierToRefs = new HashMap<>();
		for(AssessmentItemRef itemRef:resolvedAssessmentTest.getAssessmentItemRefs()) {
			identifierToRefs.put(itemRef.getIdentifier(), itemRef);
		}

		List<Identity> assessedIdentities = initializeAssessedIdentities();
		model = new CorrectionOverviewModel(courseEntry, courseNode, testEntry,
				resolvedAssessmentTest, manifestBuilder, assessedIdentities, getTranslator());
	}
	
	@Override
	public void initTools() {
		stackPanel.addTool(segmentButtonsCmp, Align.segment, true);
	}
	
	public int getNumOfAssessmentTestSessions() {
		Map<Identity, AssessmentTestSession> sessions = model.getLastSessions();
		return sessions == null ? 0 : sessions.size();
	}

	public int getNumberOfAssessedIdentities() {
		return model.getNumberOfAssessedIdentities();
	}
	
	public List<Identity> getAssessedIdentities() {
		return model.getAssessedIdentities();
	}
	
	private List<Identity> initializeAssessedIdentities() {
		Set<Identity> identitiesSet;
		if(asOptions.getGroup() != null) {
			List<Identity> identities = businessGroupService.getMembers(asOptions.getGroup(), GroupRoles.participant.name());
			identitiesSet = new HashSet<>(identities);
		} else if(asOptions.getIdentities() != null) {
			identitiesSet = new HashSet<>(asOptions.getIdentities());
		} else {
			identitiesSet = new HashSet<>(ScoreAccountingHelper.loadUsers(courseEnv));
		}
		return new ArrayList<>(identitiesSet);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(assessmentItemsCtrl == source || identityListCtrl == source) {
			if(event instanceof CompleteAssessmentTestSessionEvent) {
				fireEvent(ureq, event);
			} else if(event == Event.CHANGED_EVENT) {
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if(referencesHistoryCtrl == source) {
			if(event instanceof ReferencesHistorySelectionEvent rhse) {
				doSelectReference(ureq, rhse.getEntry());
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(assessmentItemsLink == source) {
			segmentButtonsCmp.setSelectedButton(assessmentItemsLink);
			doOpenAssessmentItemList(ureq);
		} else if(identitiesLink == source) {
			segmentButtonsCmp.setSelectedButton(identitiesLink);
			doOpenIdentityList(ureq);
		}
	}
	
	private void doSelectReference(UserRequest ureq, RepositoryEntry testEntry) {
		removeAsListenerAndDispose(assessmentItemsCtrl);
		removeAsListenerAndDispose(identityListCtrl);
		assessmentItemsCtrl = null;
		identityListCtrl = null;
		
		initModel(testEntry);
		
		if(segmentButtonsCmp.getSelectedButton() == identitiesLink) {
			doOpenIdentityList(ureq);
		} else {
			doOpenAssessmentItemList(ureq);
			segmentButtonsCmp.setSelectedButton(assessmentItemsLink);
		}
	}
	
	private void doOpenAssessmentItemList(UserRequest ureq) {
		if(assessmentItemsCtrl == null) {
			assessmentItemsCtrl = new CorrectionAssessmentItemListController(ureq, getWindowControl(), stackPanel, model);
			listenTo(assessmentItemsCtrl);
		} else {
			assessmentItemsCtrl.reloadModel();
		}
		stackPanel.popUpToController(this);
		mainVC.put("content", assessmentItemsCtrl.getInitialComponent());
	}
	
	private void doOpenIdentityList(UserRequest ureq) {
		if(identityListCtrl == null) {
			identityListCtrl = new CorrectionIdentityListController(ureq, getWindowControl(), stackPanel, model);
			listenTo(identityListCtrl);
		} else {
			identityListCtrl.reloadModel();
		}
		stackPanel.popUpToController(this);
		mainVC.put("content", identityListCtrl.getInitialComponent());
	} 
}
