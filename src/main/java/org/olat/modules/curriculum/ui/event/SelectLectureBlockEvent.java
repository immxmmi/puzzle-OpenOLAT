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
package org.olat.modules.curriculum.ui.event;

import org.olat.core.gui.control.Event;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.ui.CurriculumElementRow;
import org.olat.modules.lecture.LectureBlock;

/**
 * 
 * Initial date: 18 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectLectureBlockEvent extends Event {

	private static final long serialVersionUID = -8268720078288121230L;

	public static final String SELECT_REF = "select-lecture-block";
	
	private final LectureBlock lectureBlock;
	private final CurriculumElementRow curriculumElement;
	
	public SelectLectureBlockEvent(LectureBlock lectureBlock, CurriculumElementRow curriculumElement) {
		super(SELECT_REF);
		this.lectureBlock = lectureBlock;
		this.curriculumElement = curriculumElement;
	}
	
	public CurriculumElementRow getCurriculumElementRow() {
		return curriculumElement;
	}
	
	public CurriculumElement getCurriculumElement() {
		return curriculumElement.getCurriculumElement();
	}
	
	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}
}
