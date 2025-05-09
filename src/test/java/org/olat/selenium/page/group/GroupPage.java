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

import org.junit.Assert;
import org.olat.selenium.page.core.BookingPage;
import org.olat.selenium.page.core.CalendarPage;
import org.olat.selenium.page.core.ContactPage;
import org.olat.selenium.page.core.FolderPage;
import org.olat.selenium.page.core.IMPage;
import org.olat.selenium.page.course.InfoMessageCEPage;
import org.olat.selenium.page.forum.ForumPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.portfolio.BinderPage;
import org.olat.selenium.page.wiki.WikiPage;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Drive the group run page
 * 
 * Initial date: 03.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GroupPage {
	
	private static final By showOwners = By.className("o_sel_group_members_mgmt");
	private static final By toolsBy = By.className("o_sel_collab_tools");
	private static final By editDetails = By.className("o_sel_group_edit_title");
	private static final By bookingConfigBy = By.className("o_sel_accesscontrol_create");
	
	private static final Tool calendarTool = new Tool(
			By.cssSelector("li.o_sel_group_calendar a"),
			By.cssSelector(".o_sel_collab_tools label.o_sel_hasCalendar input[type='checkbox']"));
	private static final Tool chatTool = new Tool(
			By.cssSelector("li.o_sel_group_chat a"),
			By.cssSelector(".o_sel_collab_tools label.o_sel_hasChat input[type='checkbox']"));
	private static final Tool contactTool = new Tool(
			By.cssSelector("li.o_sel_group_contact a"),
			By.cssSelector(".o_sel_collab_tools label.o_sel_hasContactForm input[type='checkbox']"));
	private static final Tool membersTool = new Tool(
			By.cssSelector("li.o_sel_group_members a"), null);
	private static final Tool newsTool = new Tool(
			By.cssSelector("li.o_sel_group_news a"),
			By.cssSelector(".o_sel_collab_tools label.o_sel_hasNews input[type='checkbox']"));
	private static final Tool folderTool = new Tool(
			By.cssSelector("li.o_sel_group_folder a"),
			By.cssSelector(".o_sel_collab_tools label.o_sel_hasFolder input[type='checkbox']"));
	private static final Tool forumTool = new Tool(
			By.cssSelector("li.o_sel_group_forum a"),
			By.cssSelector(".o_sel_collab_tools label.o_sel_hasForum input[type='checkbox']"));
	private static final Tool wikiTool = new Tool(
			By.cssSelector("li.o_sel_group_wiki a"),
			By.cssSelector(".o_sel_collab_tools label.o_sel_hasWiki input[type='checkbox']"));
	private static final Tool portfolioTool = new Tool(
			By.cssSelector("li.o_sel_group_portfolio a"),
			By.cssSelector(".o_sel_collab_tools label.o_sel_hasPortfolio input[type='checkbox']"));

	
	private WebDriver browser;
	
	public GroupPage() {
		//
	}
	
	public GroupPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public static GroupPage getGroup(WebDriver browser) {
		return new GroupPage(browser);
	}
	
	public GroupPage openAdministration() {
		By adminBy = By.xpath("//div[contains(@class,'o_tree')]//a[contains(@onclick,'MENU_ADMINISTRATION')]");
		browser.findElement(adminBy).click();
		return assertOnEditDetails();
	}
	
	public GroupPage openEditDetails() {
		By membersBy = By.cssSelector("ul>li.o_sel_group_edit_details>a");
		OOGraphene.waitElement(membersBy, browser).click();
		return assertOnEditDetails();
	}
	
	public GroupPage assertOnEditDetails() {
		OOGraphene.waitElement(editDetails, browser);
		return this;
	}
	
	public GroupPage openAdminMembers() {
		By membersBy = By.cssSelector("ul>li.o_sel_group_edit_members>a");
		OOGraphene.waitElement(membersBy, browser).click();
		OOGraphene.waitElement(showOwners, browser);
		return this;
	}
	
	public GroupPage openAdminTools() {
		By membersBy = By.cssSelector("ul>li.o_sel_group_edit_tools>a");
		OOGraphene.waitElement(membersBy, browser).click();
		OOGraphene.waitElement(toolsBy, browser);
		return this;
	}
	
	public BookingPage openBookingConfig() {
		By membersBy = By.cssSelector("ul>li.o_sel_group_edit_access>a");
		OOGraphene.waitElement(membersBy, browser).click();
		OOGraphene.waitElement(bookingConfigBy, browser);
		return new BookingPage(browser);
	}
	
	public IMPage openChat() {
		openMenuItem(chatTool);
		OOGraphene.waitElement(By.cssSelector("a.o_sel_im_open_tool_chat"), browser);
		return new IMPage(browser);
	}
	
	public CalendarPage openCalendar() {
		openMenuItem(calendarTool);
		OOGraphene.waitElement(CalendarPage.calendarToolbatBy, browser);
		return new CalendarPage(browser);
	}
	
	public ContactPage openContact() {
		openMenuItem(contactTool);
		OOGraphene.waitElement(By.cssSelector("fieldset.o_sel_contact_form"), browser);
		return new ContactPage(browser);
	}
	
	public GroupPage openMembers() {
		openMenuItem(membersTool);
		OOGraphene.waitElement(By.className("o_cmembers"), browser);
		return this;
	}
	
	public InfoMessageCEPage openNews() {
		openMenuItem(newsTool);
		OOGraphene.waitElement(By.className("o_infomsg"), browser);
		return new InfoMessageCEPage(browser);
	}
	
	public FolderPage openFolder() {
		openMenuItem(folderTool);
		return new FolderPage(browser).assertOnFolderTable();
	}
	
	public ForumPage openForum() {
		openMenuItem(forumTool);
		OOGraphene.waitElement(ForumPage.threadTableBy, browser);
		return ForumPage.getGroupForumPage(browser);
	}
	
	public WikiPage openWiki() {
		openMenuItem(wikiTool);
		OOGraphene.waitElement(WikiPage.wikiWrapperBy, browser);
		return WikiPage.getGroupWiki(browser);
	}
	
	public BinderPage openPortfolio() {
		openMenuItem(portfolioTool);
		OOGraphene.waitElement(BinderPage.portfolioBy, browser);
		return new BinderPage(browser);
	}
	
	private GroupPage openMenuItem(Tool tool) {
		OOGraphene.waitElement(tool.getMenuItemBy(), browser)
			.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Grap the REST url of the group (only, you need
	 * to be in administration > description changes.
	 * @return
	 */
	public String getGroupURL() {
		By urlBy = By.cssSelector("p.o_sel_group_url input");
		WebElement urlEl = browser.findElement(urlBy);
		return urlEl.getDomAttribute("value");
	}
	
	public void close() {
		By closeBy = By.xpath("//li[@class='o_breadcrumb_close']/a[i[contains(@class,'o_icon_close_tool')]]");
		OOGraphene.waitElementClickable(closeBy, browser);
		browser.findElement(closeBy).click();
		OOGraphene.waitBusy(browser);
	}
	
	public GroupPage setVisibility(boolean owners, boolean participants, boolean waitingList) {
		OOGraphene.waitElement(By.className("o_sel_group_members_visibility"), browser);
		if(owners) {
			By showOwnersBy = By.cssSelector(".o_sel_group_show_owners input[type='checkbox']");
			browser.findElement(showOwnersBy).click();
			OOGraphene.waitBusy(browser);
			By withOwnersBy = By.cssSelector("li.o_sel_group_members.o_sel_group_owners_members a");
			OOGraphene.waitElement(withOwnersBy, browser);
		}
		
		if(participants) {
			By showParticipants = By.cssSelector(".o_sel_group_show_participants input[type='checkbox']");
			browser.findElement(showParticipants).click();
			OOGraphene.waitBusy(browser);
			By withParticipantsBy = By.cssSelector("li.o_sel_group_members.o_sel_group_participants_members a");
			OOGraphene.waitElement(withParticipantsBy, browser);
		}
		
		if(waitingList) {
			By showWaitingListBy = By.cssSelector(".o_sel_group_show_waiting_list input[type='checkbox']");
			browser.findElement(showWaitingListBy).click();
			OOGraphene.waitBusy(browser);
			By withWaitingBy = By.cssSelector("li.o_sel_group_members.o_sel_group_waiting_members a");
			OOGraphene.waitElement(withWaitingBy, browser);
		}
		return this;
	}
	
	public GroupPage setWaitingList() {
		By waitingListBy = By.xpath("//fieldset[contains(@class,'o_sel_group_edit_group_form')]");
		OOGraphene.scrollBottom(waitingListBy, browser);

		By waitingListCheckBy = By.xpath("//fieldset[contains(@class,'o_sel_group_edit_waiting_list')]//input[@type='checkbox']");
		WebElement waitingListCheckEl = browser.findElement(waitingListCheckBy);
		OOGraphene.check(waitingListCheckEl, Boolean.TRUE);
		return this;
	}
	
	public GroupPage setMaxNumberOfParticipants(int max) {
		By maxBy = By.cssSelector(".o_sel_group_edit_max_members input[type='text']");
		browser.findElement(maxBy).sendKeys(Integer.toString(max));
		return this;
	}
	
	/**
	 * Save the details form.
	 * @return The group page
	 */
	public GroupPage saveDetails() {
		By submitBy = By.cssSelector(".o_sel_group_edit_group_form button.btn-primary");
		browser.findElement(submitBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public GroupPage enableTools() {
		return enableTool(calendarTool)
				.enableTool(chatTool)
				.enableTool(contactTool)
				.enableTool(newsTool)
				.enableTool(folderTool)
				.enableTool(forumTool)
				.enableTool(wikiTool)
				.enableTool(portfolioTool);
	}
	
	public GroupPage enableCalendarTool() {
		return enableTool(calendarTool);
	}
	
	private GroupPage enableTool(Tool tool) {
		By checkToolsBy = tool.getCheckboxBy();
		browser.findElement(checkToolsBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitElement(tool.getMenuItemBy(), browser);
		return this;
	}
	
	public MembersWizardPage addMember() {
		By addMemberBy = By.cssSelector("fieldset.o_sel_group_members_mgmt a.o_sel_group_add_member");
		OOGraphene.waitElement(addMemberBy, browser);
		OOGraphene.click(addMemberBy, browser);
		OOGraphene.waitModalWizard(browser);
		return new MembersWizardPage(browser);
	}
	
	public GroupPage assertOnInfosPage(String name) {
		By groupNameBy = By.xpath("//div[@id='o_main_center_content_inner']//div[contains(@class,'o_name')]//div[contains(text(),'" + name + "')]");
		List<WebElement> groupNameEls = browser.findElements(groupNameBy);
		Assert.assertFalse(groupNameEls.isEmpty());
		return this;
	}
	
	public GroupPage assertNews(String name) {
		By groupNameBy = By.xpath("//div[@id='o_main_center_content_inner']//div[@id='o_msg_info']//p[contains(text(),'" + name+ "')]");
		List<WebElement> groupNameEls = browser.findElements(groupNameBy);
		Assert.assertFalse(groupNameEls.isEmpty());
		return this;
	}
	
	public GroupPage assertOnWaitingList(String name) {
		//check group name
		By groupNameBy = By.cssSelector("#o_main_center_content_inner h4");
		WebElement groupNameEl = browser.findElement(groupNameBy);
		Assert.assertTrue(groupNameEl.getText().contains(name));
		//check the warning
		By warningBy = By.xpath("//div[@id='o_main_center_content_inner']//p[contains(@class,'o_warning')]");
		List<WebElement> warningEls = browser.findElements(warningBy);
		Assert.assertFalse(warningEls.isEmpty());
		return this;
	}
	
	public GroupPage assertParticipantList() {
		By participantListBy = By.cssSelector(".o_header_with_buttons > h2 > i.o_icon_group");
		OOGraphene.waitElement(participantListBy, browser);
		return this;
	}
	
	public GroupPage assertParticipantNotEmptyList() {
		By participantListBy = By.xpath("//div[contains(@class,'o_sel_participants')]//div[contains(@class,'o_cmember_info_wrapper')]");
		OOGraphene.waitElement(participantListBy, browser);
		return this;
	}
	
	public GroupPage assertMembersInOwnerList(UserVO owner) {
		return assertMembers(owner, "o_sel_coaches");
	}
	
	public GroupPage assertMembersInParticipantList(UserVO owner) {
		return assertMembers(owner, "o_sel_participants");
	}
	
	public GroupPage assertMembersInWaitingList(UserVO owner) {
		return assertMembers(owner, "o_sel_waiting_list");
	}
	
	private GroupPage assertMembers(UserVO member, String cssClass) {
		boolean isMember = isMembers( member, cssClass);
		Assert.assertTrue(isMember);
		return this;
	}
	
	public boolean isInMembersOwnerList(UserVO owner) {
		return isMembers(owner, "o_sel_coaches");
	}
	
	public boolean isInMembersParticipantList(UserVO owner) {
		return isMembers(owner, "o_sel_participants");
	}
	
	public boolean isInMembersInWaitingList(UserVO owner) {
		return isMembers(owner, "o_sel_waiting_list");
	}
	
	private boolean isMembers(UserVO member, String cssClass) {
		String firstName = member.getFirstName();
		By longBy = By.xpath("//div[contains(@class,'" + cssClass + "')]//div[contains(@class,'o_cmember_info_wrapper')]/a/span[contains(text(),'" + firstName + "')]");
		List<WebElement> elements = browser.findElements(longBy);
		return elements.size() > 0;
	}
	
	private static class Tool {
		private final By menuItemBy;
		private final By checkboxBy;
		
		public Tool(By menuItemBy, By checkboxBy) {
			this.menuItemBy = menuItemBy;
			this.checkboxBy = checkboxBy;
		}

		public By getMenuItemBy() {
			return menuItemBy;
		}

		public By getCheckboxBy() {
			return checkboxBy;
		}
	}
}
