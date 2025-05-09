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
package org.olat.course.nodes;

import java.io.File;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.info.InfoCourseNodeEditController;
import org.olat.course.nodes.members.MembersCourseNodeEditController;
import org.olat.course.nodes.members.MembersCourseNodeRunController;
import org.olat.course.nodes.members.MembersPeekViewController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;


/**
 * 
 * Description:<br>
 *  The course node show all members of the course
 * 
 * <P>
 * Initial Date:  11 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 * @autohr dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 */
public class MembersCourseNode extends AbstractAccessableCourseNode {
	
	private static final long serialVersionUID = -8404722446386415061L;
	
	public static final String TYPE = "cmembers";
	
	//Config keys
	public static final String CONFIG_KEY_SHOWOWNER = "showOwner";
	public static final String CONFIG_KEY_SHOWCOACHES = "showCoaches";
	public static final String CONFIG_KEY_SHOWPARTICIPANTS = "showParticpants";
	
	public static final String CONFIG_KEY_COACHES_ALL = "members_CoachesAll";
	public static final String CONFIG_KEY_PARTICIPANTS_ALL = "members_ParticipantsAll";
	
	public static final String CONFIG_KEY_EMAIL_FUNCTION = "emailFunction";
	public static final String CONFIG_KEY_DOWNLOAD_FUNCTION = "downloadFunction";
	public static final String EMAIL_FUNCTION_ALL = "all";
	public static final String EMAIL_FUNCTION_COACH_ADMIN = "coachAndAdmin";
	
	public static final String CONFIG_KEY_COACHES_COURSE = "members_CourseCoaches";
	public static final String CONFIG_KEY_COACHES_GROUP = "members_GroupCoaches";
	public static final String CONFIG_KEY_COACHES_GROUP_ID = "members_GroupCoachesIds";
	public static final String CONFIG_KEY_COACHES_AREA = "members_AreaCoaches";
	public static final String CONFIG_KEY_COACHES_AREA_IDS = "members_AreaCoachesIds";
	public static final String CONFIG_KEY_COACHES_CUR_ELEMENT = "members_CurriculumElementCoaches";
	public static final String CONFIG_KEY_COACHES_CUR_ELEMENT_ID = "members_CurriculumElementCoachesIds";
	
	public static final String CONFIG_KEY_PARTICIPANTS_COURSE = "members_CourseParticipants";
	public static final String CONFIG_KEY_PARTICIPANTS_GROUP = "members_GroupParticipants";
	public static final String CONFIG_KEY_PARTICIPANTS_GROUP_ID = "members_GroupParticipantsIds";
	public static final String CONFIG_KEY_PARTICIPANTS_AREA = "members_AreaParticipants";
	public static final String CONFIG_KEY_PARTICIPANTS_AREA_ID = "members_AreaParticipantsIds";
	public static final String CONFIG_KEY_PARTICIPANTS_CUR_ELEMENT = "members_CurriculumElementParticipants";
	public static final String CONFIG_KEY_PARTICIPANTS_CUR_ELEMENT_ID = "members_CurriculumElementParticipantsIds";


	public MembersCourseNode() {
		super(TYPE);
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}

	@Override
	public StatusDescription isConfigValid() {
		return StatusDescription.NOERROR;
	}
	
	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		String translatorStr = Util.getPackageName(InfoCourseNodeEditController.class);
		List<StatusDescription> statusDescs =isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		statusDescs.forEach(s -> s.setActivateableViewIdentifier(MembersCourseNodeEditController.PANE_TAB_MEMBERSCONFIG));
		oneClickStatusCache = StatusDescriptionHelper.sort(statusDescs);
		return oneClickStatusCache;
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		MembersCourseNodeEditController childTabCntrllr = new MembersCourseNodeEditController(ureq, wControl, euce, this.getModuleConfiguration());
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, childTabCntrllr);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd, VisibilityFilter visibilityFilter) {
		Controller controller;
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			controller = MessageUIFactory.createGuestNoAccessMessage(ureq, wControl, null);
		} else {
			controller = new MembersCourseNodeRunController(ureq, wControl, userCourseEnv, this.getModuleConfiguration());
		}
		Controller titledCtrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, userCourseEnv, this, "o_cmembers_icon");
		return new NodeRunConstructionResult(titledCtrl);
	}

	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, boolean small) {
		return new MembersPeekViewController(ureq, wControl, userCourseEnv, getModuleConfiguration());
	}

	
	@Override
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCourse, CopyCourseContext context) {
		super.postCopy(envMapper, processType, course, sourceCourse, context);
		postImportCopy(envMapper);
	}
	
	@Override
	public void postImport(File importDirectory, ICourse course, CourseEnvironmentMapper envMapper, Processing processType) {
		super.postImport(importDirectory, course, envMapper, processType);
		postImportCopy(envMapper);
	}
	
	private void postImportCopy(CourseEnvironmentMapper envMapper) {
		ModuleConfiguration mc = getModuleConfiguration();
		//remap group keys
		List<Long> coachesGroupKeys = mc.getList(CONFIG_KEY_COACHES_GROUP_ID, Long.class);
		if(coachesGroupKeys == null || coachesGroupKeys.isEmpty()) {
			String coachesGroupNames = (String)mc.get(CONFIG_KEY_COACHES_GROUP);
			coachesGroupKeys = envMapper.toGroupKeyFromOriginalNames(coachesGroupNames);
		} else {
			coachesGroupKeys = envMapper.toGroupKeyFromOriginalKeys(coachesGroupKeys);
		}
		mc.set(CONFIG_KEY_COACHES_GROUP_ID, coachesGroupKeys);

		List<Long> participantsGroupKeys = mc.getList(CONFIG_KEY_PARTICIPANTS_GROUP_ID, Long.class);
		if(participantsGroupKeys == null || participantsGroupKeys.isEmpty()) {
			String participantsGroupNames = (String)mc.get(CONFIG_KEY_PARTICIPANTS_GROUP);
			participantsGroupKeys = envMapper.toGroupKeyFromOriginalNames(participantsGroupNames);
		} else {
			participantsGroupKeys = envMapper.toGroupKeyFromOriginalKeys(participantsGroupKeys);
		}
		mc.set(CONFIG_KEY_PARTICIPANTS_GROUP_ID, participantsGroupKeys);
		
		//remap area keys
		String coachesAreaNames = (String)mc.get(CONFIG_KEY_COACHES_AREA);
		List<Long> coachesAreaKeys = mc.getList(CONFIG_KEY_COACHES_AREA_IDS, Long.class);
		if(coachesAreaKeys == null || coachesAreaKeys.isEmpty()) {
			coachesAreaKeys = envMapper.toAreaKeyFromOriginalNames(coachesAreaNames);
		} else {
			coachesAreaKeys = envMapper.toAreaKeyFromOriginalKeys(coachesAreaKeys);
		}
		mc.set(CONFIG_KEY_COACHES_AREA_IDS, coachesAreaKeys);
		
		String participantsAreaNames = (String)mc.get(CONFIG_KEY_PARTICIPANTS_AREA);
		List<Long> participantsAreaKeys = mc.getList(CONFIG_KEY_PARTICIPANTS_AREA_ID, Long.class);
		if(participantsAreaKeys == null || participantsAreaKeys.isEmpty()) {
			participantsAreaKeys = envMapper.toAreaKeyFromOriginalNames(participantsAreaNames);
		} else {
			participantsAreaKeys = envMapper.toAreaKeyFromOriginalKeys(participantsAreaKeys);
		}
		mc.set(CONFIG_KEY_PARTICIPANTS_AREA_ID, participantsAreaKeys);
	}

	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent, NodeAccessType nodeAccessType, Identity doer) {
		super.updateModuleConfigDefaults(isNewNode, parent, nodeAccessType, doer);
		
		ModuleConfiguration config = getModuleConfiguration();
		int version = config.getConfigurationVersion();
		if(isNewNode){
			config.setBooleanEntry(CONFIG_KEY_SHOWOWNER, false);
			config.setBooleanEntry(CONFIG_KEY_SHOWCOACHES, true);
			config.setBooleanEntry(CONFIG_KEY_SHOWPARTICIPANTS, true);
			config.setStringValue(CONFIG_KEY_EMAIL_FUNCTION, EMAIL_FUNCTION_COACH_ADMIN);
			config.setStringValue(CONFIG_KEY_DOWNLOAD_FUNCTION, EMAIL_FUNCTION_COACH_ADMIN);
			config.setConfigurationVersion(3);
		} 
		
		/*else*/ {
			if(version < 2) {
				//update old config versions
				config.setBooleanEntry(CONFIG_KEY_SHOWOWNER, true);
				config.setBooleanEntry(CONFIG_KEY_SHOWCOACHES, true);
				config.setBooleanEntry(CONFIG_KEY_SHOWPARTICIPANTS, true);
				config.setConfigurationVersion(2);
			}
			if(version < 3) {
				config.setStringValue(CONFIG_KEY_EMAIL_FUNCTION, EMAIL_FUNCTION_COACH_ADMIN);
				config.setConfigurationVersion(3);
			}
			if(version < 4) {
				if(config.getBooleanEntry(CONFIG_KEY_SHOWCOACHES)) {
					config.set(CONFIG_KEY_COACHES_ALL, true);
					config.remove(CONFIG_KEY_SHOWCOACHES);
				}
				if(config.getBooleanEntry(CONFIG_KEY_SHOWPARTICIPANTS)) {
					config.set(CONFIG_KEY_PARTICIPANTS_ALL, true);
					config.remove(CONFIG_KEY_SHOWPARTICIPANTS);
				}
				config.setConfigurationVersion(4);
			}
		}
	}
}