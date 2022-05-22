/**
 *  All rights reserved under gd 
 */
package com.gd.service.google.api.components.calendar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar.Events;
import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.ConferenceSolutionKey;
import com.google.api.services.calendar.model.CreateConferenceRequest;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

/**
 * @see CalendarEventMakerComponentInterface
 * @author gcc
 *
 */
@Component
@Scope("prototype")
public class CalendarEventMakerComponent implements CalendarEventMakerComponentInterface 
{
	private final static String EMAIL = "email";
	private final static String POPUP = "popup";
	private final static String MEET = "hangoutsMeet";
	private final static String RRULE = "RRULE:FREQ=DAILY;COUNT=1";
	

	/**
	 * @see CalendarEventMakerComponentInterface#prepareCalendarEvent(DateTime, DateTime, String, String, String, String)
	 * 
	 */
	@Override
	public Event prepareCalendarEvent(final DateTime startDateTime,  final DateTime endDateTime,final String summary, final String description, final String location, final String timeZone, List<String> emailsId) 
	{
		Event event = new Event().setSummary(summary).setLocation(location)
				.setDescription(description);

		/** Setting event start date and time along with time zone **/
		EventDateTime start = new EventDateTime().setTimeZone(timeZone==""?TimeZone.getDefault().getID():timeZone).setDateTime(startDateTime);
		event.setStart(start);

		/** Setting event end date and time along with time zone **/
		EventDateTime end = new EventDateTime().setTimeZone(timeZone==""?TimeZone.getDefault().getID():timeZone).setDateTime(endDateTime);
		event.setEnd(end);
		
	
		

		/** Setting event frequent and its count**/
		String[] recurrence = new String[] { RRULE };
		event.setRecurrence(Arrays.asList(recurrence));

		/** Adding participants, this also can customize based on requirement 
		EventAttendee[] attendees = new EventAttendee[] {
				new EventAttendee().setEmail("talat@gdsolutions.com"),
				new EventAttendee().setEmail("meet@unisisengineering.com"),
				new EventAttendee().setEmail("tojikgeorge@gmail.com"),
				new EventAttendee().setEmail("greencodeconsultancy@gmail.com") };
		
		event.setAttendees(Arrays.asList(attendees));
		**/
		List <EventAttendee> emailList = new ArrayList<>();
		for(String email : emailsId)
		{
			emailList.add(new EventAttendee().setEmail(email).setOrganizer(true).setDisplayName(email));
		}
		emailList.add(new EventAttendee().setEmail("meet@unisisengineering.com"));
		event.setAttendees(emailList).setGuestsCanModify(true);
		/** Setting reminder as mail and pop up. Participants settings will override this settings. **/
		EventReminder[] reminderOverrides = new EventReminder[] {
				new EventReminder().setMethod(EMAIL).setMinutes(24*60),
				new EventReminder().setMethod(EMAIL).setMinutes(30),
				new EventReminder().setMethod(POPUP).setMinutes(10) 
				};
		Event.Reminders reminders = new Event.Reminders().setUseDefault(false)
				.setOverrides(Arrays.asList(reminderOverrides));
		event.setReminders(reminders);

		/** Google meet settings **/
		ConferenceSolutionKey conferenceSKey = new ConferenceSolutionKey();
		conferenceSKey.setType(MEET); 
		CreateConferenceRequest createConferenceReq = new CreateConferenceRequest();
		createConferenceReq.setRequestId(UUID.randomUUID().toString()); 
		createConferenceReq.setConferenceSolutionKey(conferenceSKey);
		ConferenceData conferenceData = new ConferenceData();
		conferenceData.setCreateRequest(createConferenceReq);
		event.setConferenceData(conferenceData);
			
		return event;
	}

}
