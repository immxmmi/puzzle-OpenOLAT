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
package org.olat.modules.curriculum.ui.wizard;

import java.util.List;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.modules.curriculum.ui.member.MembershipModification;
import org.olat.modules.curriculum.ui.wizard.MembersContext.AccessInfos;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.OrderAdditionalInfos;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AddMemberFinishCallback extends AbstractMemberCallback {
	
	private final MembersContext membersContext;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureService lectureService;

	public AddMemberFinishCallback(MembersContext membersContext) {
		super();
		this.membersContext = membersContext;
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		final List<Identity> identities = membersContext.getSelectedIdentities();
		AccessInfos offer = membersContext.getSelectedOffer();
		
		MailerResult result = new MailerResult();
		MailTemplate template = membersContext.getMailTemplate();
		MailPackage mailPackage = new MailPackage(template, result, (MailContext)null, template != null);
		
		if(offer != null) {
			OrderStatus orderStatus = getOrderStatus(offer);
			String adminNote = membersContext.getAdminNote();
			for(Identity identity:identities) {
				OrderAdditionalInfos orderInfos = membersContext.createOrderInfos(identity, true);
				acService.accessResource(identity, offer.offerAccess(), orderStatus, orderInfos, mailPackage,
						ureq.getIdentity(), adminNote);
			}
		} else {
			List<MembershipModification> modifications = membersContext.getModifications();
			List<CurriculumElement> curriculumElements = membersContext.getAllCurriculumElements();
			List<CurriculumElementMembershipChange> changes = applyModification(identities, curriculumElements, modifications);
			if(!changes.isEmpty()) {
				curriculumService.updateCurriculumElementMemberships(ureq.getIdentity(), ureq.getUserSession().getRoles(), changes, mailPackage);
			}
			if(membersContext.isAddAsTeacher()) {
				addAsTeacher(identities, modifications);
			}
		}
		return StepsMainRunController.DONE_MODIFIED;
	}
	
	private void addAsTeacher(List<Identity> identities, List<MembershipModification> modifications) {
		List<CurriculumElement> elements = modifications.stream()
				.map(MembershipModification::curriculumElement)
				.toList();
		
		for(CurriculumElement element:elements) {
			List<LectureBlock> blocks = lectureService.getLectureBlocks(element, true);
			for(LectureBlock block:blocks) {
				if(block.getStatus() == LectureBlockStatus.active) {
					for(Identity identity:identities) {
						lectureService.addTeacher(block, identity);
					}
				}
				dbInstance.commitAndCloseSession();
			}
		}
	}
	
	private OrderStatus getOrderStatus(AccessInfos offer) {
		AccessMethod method = offer.offerAccess().getMethod();
		if(method != null && method.isPaymentMethod()) {
			return OrderStatus.PREPAYMENT;
		}
		return OrderStatus.PAYED;
	}
	
	@Override
	protected boolean allowModification(MembershipModification modification, CurriculumElementMembership membership, ResourceReservation reservation) {
		return (membership == null || !membership.getRoles().contains(modification.role()))
				&& (reservation == null || modification.nextStatus() != GroupMembershipStatus.reservation);
	}
}
