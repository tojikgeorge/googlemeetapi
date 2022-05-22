/**
 *  All rights reserved under gd 
 */
package com.gd.service.google.api.components.calendar;

import java.util.List;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

/**
 * This component class using for creating calendar event.
 * Can be add more overriding method as per request for creating customized events.
 * 
 * @author gcc
 *
 */
public interface CalendarEventMakerComponentInterface 
{
	/**
	 * This method using for creating calendar event.
	 * 
	 * @param startDateTime
	 * @param endDateTime
	 * @param summary
	 * @param description
	 * @param location
	 * @param timeZone
	 * @return event
	 */
	Event prepareCalendarEvent(final DateTime startDateTime,  final DateTime endDateTime,final String summary, final String description, final String location, final String timeZone, List<String> emailsId);

}
