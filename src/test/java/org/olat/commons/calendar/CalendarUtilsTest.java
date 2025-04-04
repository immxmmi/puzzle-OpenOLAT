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
package org.olat.commons.calendar;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Assert;
import org.junit.Test;


/**
 * 
 * Initial date: 10 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CalendarUtilsTest {
	
	@Test
	public void convertSecondsToMinutes_round() {
		long minutes = CalendarUtils.convertSecondsToMinutes(Long.valueOf(54l));
		Assert.assertEquals(1l, minutes);
	}
	
	@Test
	public void convertSecondsToMinutes_roundAlot() {
		long minutes = CalendarUtils.convertSecondsToMinutes(Long.valueOf(121l));
		Assert.assertEquals(3l, minutes);
	}
	
	@Test
	public void convertSecondsToMinutes_exact() {
		long minutes = CalendarUtils.convertSecondsToMinutes(Long.valueOf(360l));
		Assert.assertEquals(6l, minutes);
	}
	
	@Test
	public void formatRecurrenceDateAllDay() {
		ZonedDateTime date = ZonedDateTime.of(2018, 6, 5, 13, 45, 30, 0, ZoneId.systemDefault());
		String val = CalendarUtils.formatRecurrenceDate(date, true);
		Assert.assertEquals("20180605", val);
	}

	@Test
	public void formatRecurrenceDateTime() {
		ZonedDateTime date = ZonedDateTime.of(2018, 6, 5, 13, 45, 30, 500, ZoneId.systemDefault());
		String val = CalendarUtils.formatRecurrenceDate(date, false);
		Assert.assertEquals("20180605T134530", val);
	}
}
