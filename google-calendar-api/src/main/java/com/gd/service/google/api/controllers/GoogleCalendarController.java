/**
 * All rights reserved under gd  
 */
package com.gd.service.google.api.controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.servlet.view.RedirectView;

import com.google.api.client.util.DateTime;
import com.gd.service.google.api.components.calendar.CalendarComponentInterface;


/**
 * This controller class using for taking user input for creating new event in calendar and getting back Google Meeting url for accessing meeting.
 * Google Meeting url for accessing meeting.
 * 
 * Can customize this class for accepting all needed data from calling application.
 * 
 * This class mainly having two methods with same request url but differentiating through parameters.
 * Make suer that, there should not be any url calendar/login/google and with parameter code.
 * At present user accessing url calendar/login/google without parameter and this class calling google 
 * API for authorization and after that recalling same url with parameter code.
 * 
 * @author gcc
 *
 */
@Controller
@SessionScope
public class GoogleCalendarController 
{

	
	@Autowired
	CalendarComponentInterface calendarComponent;
	
	@Value("${auth.token.value}")
	private String savedToken;
	
	@Value("${google.client.time.zone.gmt:+04:00}")
	private String timeZoneGMT;

	/**
	 * This method is the default method using for calling calendar API from other application.
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "calendar/login/google", method = RequestMethod.GET)
	public RedirectView googleConnectionStatus(HttpServletRequest request/*, @RequestHeader(value="authToken") String authToken*/) throws Exception 
	{
		calendarComponent.setDoctEmailId(getValueAsStringAfterNullCheck(request.getParameter("doctEmailId")));
		calendarComponent.setPatEmailId(getValueAsStringAfterNullCheck(request.getParameter("patEmailId")));
		calendarComponent.setSummery(getValueAsStringAfterNullCheck(request.getParameter("summery")));
		calendarComponent.setDescription(getValueAsStringAfterNullCheck(request.getParameter("description")));
		calendarComponent.setLocation(getValueAsStringAfterNullCheck(request.getParameter("location")));
		calendarComponent.setStartDateTime(getValueAsStringAfterNullCheck(request.getParameter("startDateTime")));
		calendarComponent.setEndDateTime(getValueAsStringAfterNullCheck(request.getParameter("endDateTime")));
		System.out.println("##############  --> doctEmailId  "+calendarComponent.getDoctEmailId());
		
		timeZoneGMT=getValueAsStringAfterNullCheck(request.getParameter("timeZoneGMT"))==""?timeZoneGMT:getValueAsStringAfterNullCheck(request.getParameter("timeZoneGMT"));
		
		//checkAuthentication(getValueAsStringAfterNullCheck(authToken));
		
		return new RedirectView(calendarComponent.autoAuthorize());
	}

	/**
	 * This method calling by Google API after successfully completed authorization.
	 * 
	 * @param code
	 * @return
	 */
	@RequestMapping(value = "calendar/login/google", method = RequestMethod.GET, params = "code")
	public ResponseEntity<String> oauth2Callback(HttpServletRequest request, @RequestParam(value = "code") String code) 
	{
		
		String message="";
		
		/*
		calendarComponent.setDoctEmailId(getValueAsStringAfterNullCheck(request.getParameter("doctEmailId")));
		calendarComponent.setPatEmailId(getValueAsStringAfterNullCheck(request.getParameter("patEmailId")));
		calendarComponent.setSummery(getValueAsStringAfterNullCheck(request.getParameter("summery")));
		calendarComponent.setDescription(getValueAsStringAfterNullCheck(request.getParameter("description")));
		calendarComponent.setLocation(getValueAsStringAfterNullCheck(request.getParameter("location")));
		calendarComponent.setStartDateTime(getValueAsStringAfterNullCheck(request.getParameter("startDateTime")));
		calendarComponent.setEndDateTime(getValueAsStringAfterNullCheck(request.getParameter("endDateTime")));
		*/
	
		message=calendarComponent.addEventToCalendar(code);		
		
		return new ResponseEntity<>(message, HttpStatus.OK);
	}
	
	
	
	/**
	 * This method calling by Google API through direct Auth02 method.
	 * 
	 * @param code
	 * @return
	 */
	@RequestMapping(value = "calendar/login/google", method = RequestMethod.GET, params = "method")
	public ResponseEntity<String> directApiCall(@RequestParam(value = "method") String method) 
	{
		
		String message="";
		/**
		 * Here added the date and time manually for testing purpose. Can make changes based on requirement. 
		 */
		// #######################################################################################
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, 5);
		date = calendar.getTime();
		calendar.add(Calendar.HOUR_OF_DAY, 1);
		Date date2 = calendar.getTime();
        // ########################################################################################
		
		String dateAsISOString = getFormatedDate(date); 
		String dateAsISOString2 = getFormatedDate(date2); 
		DateTime startDateTime = new DateTime(dateAsISOString);
		DateTime endDateTime = new DateTime(dateAsISOString2);
		System.out.println("##############  --> dateAsISOString  "+dateAsISOString);
		message=calendarComponent.addEventToCalendar(method);		
		
		return new ResponseEntity<>(message, HttpStatus.OK);
	}
	
	
	/**
	 * We need to set Time Zone with date before adding into Google DateTime object. 
	 * This method is using for adding time zone with date.
	 * Currently added the time zone based on Indian time.
	 * 
	 * @param date
	 * @return
	 */
	private String getFormatedDate(Date date)
	{ 		
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"+timeZoneGMT);
			df.setTimeZone(TimeZone.getTimeZone("GMT"+timeZoneGMT));
			return df.format(date);		
	}
	
	private String getValueAsStringAfterNullCheck(String val)
	{
		return val==null?"":val;
	}
	
	private void checkAuthentication(String authValue) throws AuthenticationException
	{
		if(!this.savedToken.equals(authValue))
		{
			throw new AuthenticationException("Use valid token !");
		}
	}

	
	
}