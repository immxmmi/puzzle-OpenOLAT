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
package org.olat.selenium.page.group;

import java.util.List;

import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * Drive the wizard to add members
 * 
 * Initial date: 03.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MembersWizardPage {
	
	private WebDriver browser;
	
	public MembersWizardPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public MembersWizardPage importList() {
		By importBy = By.xpath("//div[contains(@class,'o_sel_import_type')]/label/input[@name='import.type' and @value='list']");
		OOGraphene.waitElement(importBy, browser).click();
		By listBy = By.cssSelector("div.o_sel_user_import textarea");
		OOGraphene.waitElement(listBy, browser);
		return this;
	}
	
	public MembersWizardPage nextUsers() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_user_import_overview"), browser);
		return this;
	}
	
	public MembersWizardPage nextOverview() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("div.o_sel_edit_permissions"), browser);
		return this;
	}
	
	public MembersWizardPage nextPermissions() {
		OOGraphene.nextStep(browser);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_contact_form"), browser);
		return this;
	}
	
	public MembersWizardPage finish() {
		try {
			By contactFormBy = By.cssSelector("fieldset.o_sel_contact_form");
			OOGraphene.waitElement(contactFormBy, browser);
			OOGraphene.waitBusy(browser);
			OOGraphene.finishStep(browser, false);
		} catch (Exception e) {
			OOGraphene.finishStep(browser);
		}
		return this;
	}
	
	/**
	 * Search member and select them, flow to select several
	 * identities at once.
	 * 
	 * @param user The user to search for
	 * @param admin If administrator search with user name
	 * @return Itself
	 */
	public MembersWizardPage searchMember(UserVO user, boolean admin) {
		searchMemberForm(user, admin);

		// select all
		By selectAll = By.xpath("//dialog[contains(@class,'modal')]//th[contains(@class,'o_table_checkall')]/a[i[contains(@class,'o_icon_check_off')]]");
		OOGraphene.waitElement(selectAll, browser).click();
		OOGraphene.waitBusy(browser);
		By selectedAll = By.xpath("//dialog[contains(@class,'modal')]//th[contains(@class,'o_table_checkall')]/a[i[contains(@class,'o_icon_check_on')]]");
		OOGraphene.waitElement(selectedAll, browser);
		return this;
	}
	
	/**
	 * Search and select a member, flow to select a single identity.
	 * 
	 * @param user The user to search for
	 * @param admin If administrator search with user name
	 * @return Itself
	 */
	public MembersWizardPage searchOneMember(UserVO user, boolean admin) {
		searchMemberForm(user, admin);

		// select the identity
		try {
			By selectAll = By.xpath("//div[contains(@class,'modal')]//div[contains(@class,'o_table_flexi')]//table//tr[td[text()[contains(.,'" + user.getFirstName() + "')]]]/td/a[contains(@onclick,'select')]");
			OOGraphene.waitElement(selectAll, browser);
			if(browser instanceof FirefoxDriver) {
				OOGraphene.waitingALittleLonger();// link is obscured by the scroll bar
			}
			browser.findElement(selectAll).click();
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Search one member", browser);
			throw e;
		}
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	private MembersWizardPage searchMemberForm(UserVO user, boolean admin) {
		//Search by username or first name
		By firstFieldBy = By.xpath("//fieldset[contains(@class,'o_sel_usersearch_searchform')]//input[@type='text'][1]");
		OOGraphene.waitElement(firstFieldBy, browser);
		By typeheadBy = By.xpath("//span[contains(@class,'twitter-typeahead')]/div[contains(@class,'tt-menu')]");
		OOGraphene.waitElementPresence(typeheadBy, 5, browser);
		
		String search = admin ? user.getLogin() : user.getFirstName();
		browser.findElement(firstFieldBy).sendKeys(search);
		OOGraphene.waitingALittleLonger();

		try {
			By searchBy = By.cssSelector(".o_sel_usersearch_searchform a.btn-default");
			OOGraphene.click(searchBy, browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Search member", browser);
			throw e;
		}
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	
	public MembersWizardPage setMembers(UserVO... users) {
		StringBuilder sb = new StringBuilder();
		for(UserVO user:users) {
			if(sb.length() > 0) sb.append("\\n");
			sb.append(user.getLogin());
		}
		By importAreaBy = By.cssSelector(".modal-content fieldset.o_sel_user_import_by_username textarea");
		WebElement importAreaEl = OOGraphene.waitElement(importAreaBy, browser);
		OOGraphene.textarea(importAreaEl, sb.toString(), browser);
		return this;
	}
	
	public MembersWizardPage selectRepositoryEntryRole(boolean owner, boolean coach, boolean participant) {
		if(owner) {
			By ownerBy = By.cssSelector("label input[name='repoRights'][type='checkbox'][value='owner']");
			WebElement ownerEl = browser.findElement(ownerBy);
			OOGraphene.check(ownerEl, Boolean.valueOf(owner));
			OOGraphene.waitBusyAndScrollTop(browser);
		}
		
		if(coach) {
			By coachBy = By.cssSelector("label input[name='repoRights'][type='checkbox'][value='tutor']");
			WebElement coachEl = browser.findElement(coachBy);
			OOGraphene.check(coachEl, Boolean.valueOf(coach));
			OOGraphene.waitBusyAndScrollTop(browser);
		}
		
		By participantBy = By.cssSelector("label input[name='repoRights'][type='checkbox'][value='participant']");
		WebElement participantEl = browser.findElement(participantBy);
		OOGraphene.check(participantEl, Boolean.valueOf(participant));
		OOGraphene.waitBusyAndScrollTop(browser);
		return this;
	}
	
	public MembersWizardPage selectRepositoryEntryRoleNG(boolean owner, boolean coach, boolean participant) {
		if(owner) {
			select("owner", true);
		}
		if(coach) {
			select("coach", true);
		}
		select("participant", participant);
		return this;
	}
	
	private void select(String role, boolean add) {
		String cmpXpath = "//div[contains(@class,'o_sel_role_" + role + "')]//div[contains(@class,'o_addremove')]/a";	
		if(add) {
			By activeAddBy = By.xpath(cmpXpath + "[contains(@class,'o_addremove_add_active')]");
			List<WebElement> activeAddEls = browser.findElements(activeAddBy);
			if(activeAddEls.isEmpty()) {
				By addBy = By.xpath(cmpXpath + "[contains(@class,'o_sel_add')]");
				browser.findElement(addBy).click();
				OOGraphene.waitBusyAndScrollTop(browser);
				OOGraphene.waitElement(activeAddBy, browser);
			}
		} else {
			By activeRemoveBy = By.xpath(cmpXpath + "[contains(@class,'o_addremove_remove_active')]");
			List<WebElement> activeRemoveEls = browser.findElements(activeRemoveBy);
			if(activeRemoveEls.isEmpty()) {
				By removeBy = By.xpath(cmpXpath + "[contains(@class,'o_sel_remove')]");
				browser.findElement(removeBy).click();
				OOGraphene.waitBusyAndScrollTop(browser);
				OOGraphene.waitElement(activeRemoveBy, browser);
			}
		}
	}
	
	public MembersWizardPage selectGroupAsParticipant(String groupName) {
		By rolesBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr[td[text()='" + groupName + "']]//label[contains(@class,'o_sel_role_participant')]/input");
		OOGraphene.waitElement(rolesBy, browser).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
}
