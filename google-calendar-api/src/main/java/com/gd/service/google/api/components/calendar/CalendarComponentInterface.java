/**
 * All rights reserved under gd  
 */
package com.gd.service.google.api.components.calendar;

import com.google.api.client.auth.oauth2.Credential;
/**
 * This component class using for handling all authorization and connection details.
 * This class have different type of authorizing method for handling the scenario based on different approaches. 
 * 
 * @author gcc
 *
 */
public interface CalendarComponentInterface 
{
	/**
	 * Method for calling google calendar connection and creating event.
	 * After process this method, will return the Google Meet url, otherwise error information.
	 * 
	 * @param code
	 * @return
	 */
	String addEventToCalendar(final String code);

	/**
	 * Method for handling authorization.
	 * 
	 * @return
	 * @throws Exception
	 */
	String authorize() throws Exception;

	/**
	 * Method for handling authorization.
	 * 			
	 * @return
	 * @throws Exception
	 */
	String autoAuthorize() throws Exception;

	/**
	 * Method for handling authorization.
	 * 
	 * @return
	 * @throws Exception
	 */
	Credential autoTokenAuthorize() throws Exception;
	
	public String getDoctEmailId();

	public void setDoctEmailId(String doctEmailId);

	public String getPatEmailId();

	public void setPatEmailId(String patEmailId);

	public String getSummery();

	public void setSummery(String summery);

	public String getLocation();

	public void setLocation(String location);

	public String getDescription();

	public void setDescription(String description);

	public String getStartDateTime();

	public void setStartDateTime(String startDateTime);

	public String getEndDateTime();

	public void setEndDateTime(String endDateTime);
}
