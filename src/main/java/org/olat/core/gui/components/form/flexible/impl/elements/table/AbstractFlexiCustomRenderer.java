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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.impl.FormDecorator;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 26 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
abstract class AbstractFlexiCustomRenderer extends AbstractFlexiTableRenderer {

	@Override
	protected void renderHeaders(StringOutput sb, FlexiTableComponent ftC, Translator translator) {
		//do nothing
	}
	
	@Override
	protected void renderUserOptions(Renderer renderer, StringOutput sb, FlexiTableElementImpl ftE, URLBuilder ubu,
			Translator translator, RenderResult renderResult) {
		//
	}
	
	@Override
	protected void renderZeroRow(Renderer renderer, StringOutput sb, FlexiTableComponent ftC, String rowIdPrefix,
			URLBuilder ubu, Translator translator, RenderResult renderResult) {

		FlexiTableElementImpl ftE = ftC.getFormItem();
		FormItem zeroRow = ftE.getZeroRowItem();
		if(zeroRow == null) return;
		
		sb.append("<div class='");
		if(ftC.getFormItem().getCssDelegate() != null) {
			String cssClass = ftC.getFormItem().getCssDelegate().getRowCssClass(FlexiTableRendererType.custom, -1);
			if (cssClass == null) {
				sb.append("o_table_row row");
			} else {
				sb.append(cssClass);				
			}
		} else {
			sb.append("o_table_row row");
		}
		sb.append("'>");
		
		zeroRow.setTranslator(translator);
		if(ftE.getRootForm() != zeroRow.getRootForm()) {
			zeroRow.setRootForm(ftE.getRootForm());
		}
		
		Component cmp = zeroRow.getComponent();		
		cmp.getHTMLRendererSingleton().render(renderer, sb, cmp, ubu, translator, renderResult, null);
		cmp.setDirty(false);

		sb.append("</div>");
	}
	

	
	protected void renderRowContent(Renderer renderer, StringOutput sb, FlexiTableComponent ftC, String rowIdPrefix,
			int row, URLBuilder ubu, Translator translator, RenderResult renderResult) {
		
		FlexiTableElementImpl ftE = ftC.getFormItem();
		VelocityContainer container = ftE.getRowRenderer();
		container.contextPut("f", new FormDecorator(ftE.getRootForm()));

		FlexiTableDataModel<?> dataModel = ftE.getTableDataModel();
		Object rowObject = ftE.getTableDataModel().getObject(row);
		container.contextPut("row", rowObject);
		container.contextPut("rowIndex", row);
		container.contextPut("rowId", rowIdPrefix + row);
		container.contextPut("rowSelected", Boolean.valueOf(ftE.isMultiSelectedIndex(row)));
		
		FlexiTableColumnModel columnsModel = ftE.getTableDataModel().getTableColumnModel();
		int numOfCols = columnsModel.getColumnCount();
		//link to the table element the form elements in the data model	
		for (int j = 0; j<numOfCols; j++) {
			FlexiColumnModel fcm = columnsModel.getColumnModel(j);
			int columnIndex = fcm.getColumnIndex();
			Object cellValue = columnIndex >= 0 ? dataModel.getValueAt(row, columnIndex) : null;
			if (cellValue instanceof FormItem formItem) {
				addFormItem(formItem, ftE, container, translator);
			} else if (cellValue instanceof FormItemCollection collection) {
				for (FormItem formItem : collection.getFormItems()) {
					addFormItem(formItem, ftE, container, translator);
				}
			}
		}
		
		FlexiTableComponentDelegate cmpDelegate = ftE.getComponentDelegate();
		if(cmpDelegate != null) {
			Iterable<Component> cmps = cmpDelegate.getComponents(row, rowObject);
			if(cmps != null) {
				for(Component cmp:cmps) {
					container.put(cmp.getComponentName(), cmp);
				}
			}
			
			if(ftE.hasDetailsRenderer()) {
				boolean hasDetails = ftE.getComponentDelegate().isDetailsRow(row, rowObject);
				boolean expanded = hasDetails && ftE.isDetailsExpended(row);
				container.contextPut("hasDetails", Boolean.valueOf(hasDetails));
				container.contextPut("hasChildren", Boolean.valueOf(expanded));
			}
		}
		
		if(dataModel instanceof FlexiTreeTableDataModel) {
			boolean hasChildren = ((FlexiTreeTableDataModel<?>)dataModel).hasChildren(row);
			container.contextPut("hasChildren", hasChildren);
			if(hasChildren) {
				container.contextPut("isOpen", ((FlexiTreeTableDataModel<?>)dataModel).isOpen(row));
			}
		}

		container.getHTMLRendererSingleton().render(renderer, sb, container, ubu, translator, renderResult, null);
		container.contextRemove("openCloseLink");
		container.contextRemove("hasChildren");
		container.contextRemove("rowSelected");
		container.contextRemove("rowIndex");
		container.contextRemove("rowId");
		container.contextRemove("isOpen");
		container.contextRemove("row");
		container.contextRemove("f");
		container.setDirty(false);
	}
	
	protected void addFormItem(FormItem formItem, FlexiTableElementImpl ftE, VelocityContainer container,
			Translator translator) {
		formItem.setTranslator(translator);
		if(ftE.getRootForm() != formItem.getRootForm()) {
			formItem.setRootForm(ftE.getRootForm());
		}
		ftE.addFormItem(formItem);
		container.put(formItem.getComponent().getComponentName(), formItem.getComponent());
	}
	
	@Override
	protected void renderFooter(Renderer renderer, StringOutput target, FlexiTableComponent ftC, URLBuilder ubu,
			Translator translator, RenderResult renderResult) {
		//
	}
}
