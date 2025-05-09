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
package org.olat.selenium.page.user;

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 30.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserAdminPage {

	public static final By menuTreeeBy = By.className("o_sel_useradmin_search");
	
	private WebDriver browser;
	
	public UserAdminPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public static UserAdminPage getUserAdminPage(WebDriver browser) {
		OOGraphene.waitElement(menuTreeeBy, browser);
		WebElement main = browser.findElement(By.id("o_main"));
		Assert.assertTrue(main.isDisplayed());
		return new UserAdminPage(browser);
	}
	
	public UserAdminPage openCreateUser() {
		By createBy = By.cssSelector("ul.o_tools a.o_sel_useradmin_create");
		browser.findElement(createBy).click();
		By createFormBy = By.cssSelector("form>fieldset.o_sel_id_create");
		OOGraphene.waitElement(createFormBy, browser);
		return this;
	}
	
	public UserAdminPage openSearchUser() {
		//In case it stay in the way
		By createBy = By.cssSelector(".o_tree li.o_sel_useradmin_search>div>span.o_tree_link>a");
		OOGraphene.waitElement(createBy, browser).click();
		OOGraphene.waitBusyAndScrollTop(browser);
		return assertOnSearchUser();
	}
	
	public UserAdminPage assertOnSearchUser() {
		By searchFormBy = By.cssSelector("form>fieldset.o_sel_user_search_form");
		OOGraphene.waitElement(searchFormBy, browser);
		return this;
	}
	
	public UserAdminPage openDirectDeleteUser() {
		By createBy = By.cssSelector("ul.o_tools a.o_sel_useradmin_direct_delete");
		OOGraphene.waitElement(createBy, browser).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Search a user in the search form of the direct delete
	 * workflow.
	 * 
	 * @param username
	 * @return
	 */
	public UserAdminPage searchUserToDelete(String username) {
		By createBy = By.cssSelector("fieldset.o_sel_user_search_form div.o_sel_user_search_username input[type='text']");
		OOGraphene.waitElement(createBy, browser).sendKeys(username);
		
		//search
		By searchBy = By.cssSelector("fieldset.o_sel_user_search_form a.o_sel_user_search_button");
		OOGraphene.clickAndWait(searchBy, browser);
		OOGraphene.scrollTop(browser);	
		return this;
	}
	
	/**
	 * After searching a user, you can select it and delete it.
	 * 
	 * @param lastName
	 * @return
	 */
	public UserAdminPage selectAndDeleteUser(String lastName) {
		By checkBy = By.cssSelector("fieldset.o_sel_usersearch_searchform table input[type='checkbox']");
		browser.findElement(checkBy).click();
		OOGraphene.waitBusy(browser);
		
		//select
		By selectBy = By.cssSelector("fieldset.o_sel_usersearch_searchform div.o_table_wrapper div.o_table_buttons button.btn.btn-default");
		browser.findElement(selectBy).click();
		OOGraphene.waitModalDialog(browser);
		
		//confirm
		By usernameBy = By.xpath("//div[contains(@class,'modal-dialog')]//div[@class='o_error']/strong[text()[contains(.,'" + lastName + "')]]");
		List<WebElement> confirmUserEls = browser.findElements(usernameBy);
		Assert.assertFalse(confirmUserEls.isEmpty());
		
		By confirmCheckBy = By.cssSelector("div.o_sel_confirm_delete_user input[type='checkbox']");
		WebElement confirmCheckEl = browser.findElement(confirmCheckBy);
		OOGraphene.check(confirmCheckEl, Boolean.TRUE);
		
		By buttonsBy = By.cssSelector("div.modal-dialog div.modal-body a.btn.o_sel_delete_user");
		browser.findElement(buttonsBy).click();
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	public UserAdminPage openImportUsers() {
		By importBy = By.cssSelector("ul.o_tools a.o_sel_useradmin_import");
		OOGraphene.waitElement(importBy, browser).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ImportUserPage startImport() {
		By importBy = By.cssSelector("a.o_sel_id_start_import_user_button.btn-primary");
		OOGraphene.waitElement(importBy, browser).click();
		OOGraphene.waitModalDialog(browser);
		By dataBy = By.cssSelector("fieldset.o_sel_import_users_data");
		OOGraphene.waitElement(dataBy, browser);
		return new ImportUserPage(browser);
	}
	
	public static UserVO createUserVO(String username, String firstName, String lastName, String email, String password) {
		UserVO userVo = new UserVO();
		userVo.setLogin(username);
		userVo.setFirstName(firstName);
		userVo.setLastName(lastName);
		userVo.setEmail(email);
		userVo.setPassword(password);
		return userVo;
	}
	
	public UserViewPage fillUserForm(String username, String firstName, String lastName, String email, String password) {
		UserVO userVo = createUserVO(username, firstName, lastName, email, password);
		return fillUserForm(userVo);
	}
	
	public UserViewPage fillUserForm(UserVO userVo) {
		By usernameBy = By.cssSelector(".o_sel_id_create .o_sel_id_username input[type='text']");
		OOGraphene.waitElement(usernameBy, browser).sendKeys(userVo.getLogin());
		
		By firstNameBy = By.cssSelector(".o_sel_id_create .o_sel_id_firstname input[type='text']");
		browser.findElement(firstNameBy).sendKeys(userVo.getFirstName());
		
		By lastNameBy = By.cssSelector(".o_sel_id_create .o_sel_id_lastname input[type='text']");
		browser.findElement(lastNameBy).sendKeys(userVo.getLastName());
		
		By emailBy = By.cssSelector(".o_sel_id_create .o_sel_id_email input[type='text']");
		browser.findElement(emailBy).sendKeys(userVo.getEmail());

		By password1By = By.cssSelector(".o_sel_id_create .o_sel_id_password1 input[type='password']");
		browser.findElement(password1By).sendKeys(userVo.getPassword());

		By password2By = By.cssSelector(".o_sel_id_create .o_sel_id_password2 input[type='password']");
		browser.findElement(password2By).sendKeys(userVo.getPassword());
		
		By saveBy = By.cssSelector(".o_sel_id_create button.btn-primary");
		OOGraphene.click(saveBy, browser);
		OOGraphene.scrollTop(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return new UserViewPage(browser);
	}
	
	public UserAdminPage searchByUsername(String username) {
		By usernameBy = By.cssSelector(".o_sel_user_search_form .o_sel_user_search_username input[type='text']");
		OOGraphene.waitElement(usernameBy, browser).sendKeys(username);
		
		By searchBlockBy = By.xpath("//fieldset[contains(@class,'o_sel_user_search_form')]//div[contains(@class,'form-group')][div/p/a]");
		OOGraphene.scrollBottom(searchBlockBy, browser);
		
		By searchBy = By.cssSelector(".o_sel_user_search_form a.btn-default.o_sel_user_search_button");
		browser.findElement(searchBy).click();
		OOGraphene.waitElement(By.cssSelector(".o_sel_user_search_table"), browser);
		return this;
	}
	
	public UserAdminPage searchByEmail(String email) {
		By emailBy = By.cssSelector(".o_sel_user_search_form .o_sel_user_search_email input[type='text']");
		OOGraphene.waitElement(emailBy, browser).sendKeys(email);
		
		By searchBy = By.cssSelector(".o_sel_user_search_form a.btn-default");
		browser.findElement(searchBy).click();
		OOGraphene.waitElement(By.cssSelector(".o_sel_user_search_table"), browser);
		return this;
	}
	
	public UserAdminPage assertOnUserInList(String username) {
		By userLinksBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr/td/a[text()[contains(.,'" + username + "')]]");
		OOGraphene.waitElement(userLinksBy, browser);
		return this;
	}
	
	public UserAdminPage assertOnInactiveUserInList(String username) {
		By userLinksBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr[td/span/i[contains(@class,'o_icon_identity_inactive')]]/td/a[text()[contains(.,'" + username + "')]]");
		OOGraphene.waitElement(userLinksBy, browser);
		return this;
	}
	
	public UserAdminPage assertNotInUserList(String username) {
		By userLinksBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//table//tr//td//a[text()[contains(.,'" + username + "')]]");
		OOGraphene.waitElementDisappears(userLinksBy, 5, browser);
		return this;
	}
	
	/**
	 * Select the check box of a row.
	 * 
	 * @param username The username
	 * @return itself
	 */
	public UserAdminPage selectRowByUsername(String username) {
		By selectBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//tr[td/a[contains(text(),'" + username + "')]]/td/input[@name='tb_ms']");
		OOGraphene.waitElement(selectBy, browser).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Select a user and open the details view.
	 * 
	 * @param username The username
	 * @return Itself
	 */
	public UserViewPage selectByUsername(String username) {
		By selectBy = By.xpath("//div[contains(@class,'o_table_wrapper')]//td/a[text()[contains(.,'" + username + "')]]");
		OOGraphene.waitElement(selectBy, browser).click();
		OOGraphene.waitBusy(browser);
		return new UserViewPage(browser);
	}
	
	public UserAdminPage modifyStatusBatch(int status) {
		By modifyButtonBy = By.cssSelector("a.o_sel_user_bulk_change_status");
		OOGraphene.waitElement(modifyButtonBy, browser).click();
		OOGraphene.waitModalDialog(browser);
		
		By statusChoiceBy = By.xpath("//fieldset[contains(@class,'o_sel_user_bulk_change_status_form')]//input[@name='status'][@type='radio'][@value='" + status + "']");
		browser.findElement(statusChoiceBy).click();
		OOGraphene.waitBusy(browser);
		
		By changeStatusBy = By.cssSelector("fieldset.o_sel_user_bulk_change_status_form button.btn.btn-primary");
		browser.findElement(changeStatusBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public UserAttributesWizardPage modifyAttributesBatch() {
		By modifyButtonBy = By.cssSelector("a.o_sel_user_bulk_change_attributes");
		OOGraphene.waitElement(modifyButtonBy, browser).click();
		OOGraphene.waitModalDialog(browser);
		return new UserAttributesWizardPage(browser);
	}
}
