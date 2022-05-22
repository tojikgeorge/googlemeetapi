/**
 * All rights reserved under gd  
 */
package com.gd.service.google.api.components.calendar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.Details;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.calendar.Calendar.Events;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;

/**
 * @see CalendarComponentInterface
 * 
 * @author gcc
 *
 */
@Component
@Scope("prototype")
public class CalendarComponent implements CalendarComponentInterface
{
	private static final String APPLICATION_NAME = "Calendar";
	private HttpTransport httpTransport;
	private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private com.google.api.services.calendar.Calendar client;

	private GoogleClientSecrets clientSecrets;
	private GoogleAuthorizationCodeFlow flow;
	private Credential credential;
	
	private String doctEmailId;
	private String patEmailId;
	private String summery;
	private String location;
	private String description;
	private String startDateTime;
	private String endDateTime;
	
	@Autowired
	CalendarEventMakerComponentInterface calendarEventMakerComponent;

	/**
	 * Settings are calling from application property file.
	 */
	@Value("${google.client.client-id}")
	private String clientId;
	@Value("${google.client.client-secret}")
	private String clientSecret;
	@Value("${google.client.redirectUri}")
	private String redirectURI;
	@Value("${google.client.time.zone.gmt:+04:00}")
	private String timeZoneGMT;
	@Value("${google.client.time.zone:Asia/Dubai}")
	private String timeZone;

	
	
	
	public String getDoctEmailId() 
	{
		return doctEmailId;
	}

	public void setDoctEmailId(String doctEmailId) 
	{
		this.doctEmailId = doctEmailId;
	}

	public String getPatEmailId() 
	{
		return patEmailId;
	}

	public void setPatEmailId(String patEmailId) 
	{
		this.patEmailId = patEmailId;
	}

	public String getSummery() 
	{
		return summery;
	}

	public void setSummery(String summery) 
	{
		this.summery = summery;
	}

	public String getLocation() 
	{
		return location;
	}

	public void setLocation(String location) 
	{
		this.location = location;
	}

	public String getDescription() 
	{
		return description;
	}

	public void setDescription(String description) 
	{
		this.description = description;
	}

	public String getStartDateTime() 
	{
		return startDateTime;
	}

	public void setStartDateTime(String startDateTime) 
	{
		this.startDateTime = startDateTime;
	}

	public String getEndDateTime() 
	{
		return endDateTime;
	}

	public void setEndDateTime(String endDateTime) 
	{
		this.endDateTime = endDateTime;
	}

	
	/**
	 * @see CalendarComponentInterface#addEventToCalendar(String, DateTime, DateTime)
	 */
	@Override
	public String addEventToCalendar(final String code)
	{
		String message = "";
		//String calendarId = "c_qgcknhbmsab04f682rcfqkb2a8@group.calendar.google.com";
		String calendarId = "primary";

		try 
		{
			System.out.println("##############  --> doctEmailId  "+this.doctEmailId);
			/** Accessing google calendar object with help of token code **/
			TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectURI).execute();
			//credential = getCredential();
			credential = flow.createAndStoreCredential(response, "userID");
			
			client = new com.google.api.services.calendar.Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
					.setApplicationName(APPLICATION_NAME).build();
			//client = getCredential();
		

			/** Calling event component class for creating event.
			 * Here most of the information hard coded, can make changes based on calling request.
			 **/
			System.out.println("##############  --> getStartDateTime  "+getStartDateTime()  +" --> "+getFormatedDateAsString(getDateFromString(getStartDateTime())));
			DateTime googleStartDateTime = new DateTime(getFormatedDateAsString(getDateFromString(getStartDateTime())));
			DateTime googleEndDateTime = new DateTime(getFormatedDateAsString(getDateFromString(getEndDateTime())));
			List <String> emailIDList = new ArrayList<>();
			emailIDList.add(getDoctEmailId());
			emailIDList.add(getPatEmailId());
			
			Event event = calendarEventMakerComponent.prepareCalendarEvent(googleStartDateTime, googleEndDateTime,getSummery(), 
					getDescription(),getLocation(),
					timeZone, emailIDList);
			
			
			/** Inserting the created event into calendar **/
			event = client.events().insert(calendarId, event).setConferenceDataVersion(1).execute();
			
			/** Collecting google meet link **/
			message = event.getHangoutLink();				
					
		} 
		catch (Exception e) 
		{

			message = "Exception while handling OAuth2 callback : addEventToCalendar (" + e.getMessage() + ")."
					+ " Redirecting to google connection status page.";
			e.printStackTrace();
		}
		return message;
	}
	
	/**
	 * @see CalendarComponentInterface#authorize()
	 * 
	 */
	@Override
	public String authorize() throws Exception 
	{
		AuthorizationCodeRequestUrl authorizationUrl;
		
		if (flow == null) 
		{
			Details web = new Details();
			web.setClientId(clientId);
			web.setClientSecret(clientSecret);
			clientSecrets = new GoogleClientSecrets().setWeb(web);
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets,
					Collections.singleton(CalendarScopes.CALENDAR)).build();
		}
		authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectURI);
		System.out.println("cal authorizationUrl->" + authorizationUrl);
		return authorizationUrl.build();
	}
	
	/**
	 * @see CalendarComponentInterface#autoAuthorize()
	 * 
	 */
	@Override
	public String autoAuthorize() throws Exception 
	{		
		AuthorizationCodeRequestUrl authorizationUrl;
		if (flow == null) {
			Details web = new Details();
			web.setClientId(clientId);
			web.setClientSecret(clientSecret);
			clientSecrets = new GoogleClientSecrets().setWeb(web);
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
						
			flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets,
					Collections.singleton(CalendarScopes.CALENDAR)).setAccessType("offline").setApprovalPrompt("auto").build();
			flow.newAuthorizationUrl().setRedirectUri(redirectURI).build();	
			
		}
		authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectURI);
		return authorizationUrl.build();			
		
	}
	
	/**
	 * @see CalendarComponentInterface#autoTokenAuthorize()
	 * 
	 */
	@Override
	public Credential autoTokenAuthorize() throws Exception 
	{
		Details web = new Details();
		web.setClientId(clientId);
		web.setClientSecret(clientSecret);
		clientSecrets = new GoogleClientSecrets().setWeb(web);
		httpTransport = GoogleNetHttpTransport.newTrustedTransport();

		flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets,
				Collections.singleton(CalendarScopes.CALENDAR))
						.setCredentialDataStore(new MemoryDataStoreFactory().getDataStore("tokens")).build();

		String userId = "userID";
		Credential credential = flow.loadCredential(userId);
		if (credential == null) 
		{
			GoogleAuthorizationCodeRequestUrl authorizationUrl = flow.newAuthorizationUrl();
			authorizationUrl.setRedirectUri(GoogleOAuthConstants.OOB_REDIRECT_URI);
			System.out.println("Please, authorize application. Visit {}" + authorizationUrl);
			Scanner s = new Scanner(System.in);
			String code = s.nextLine();
			
			GoogleAuthorizationCodeTokenRequest tokenRequest = flow.newTokenRequest(code);
			tokenRequest.setRedirectUri(GoogleOAuthConstants.OOB_REDIRECT_URI);
			GoogleTokenResponse tokenResponse = tokenRequest.execute();
			
			credential = flow.createAndStoreCredential(tokenResponse, userId);
		}
		return credential;

	}
	
	
	public com.google.api.services.calendar.Calendar getCredential()
	{
		try 
		{
			HttpRequestInitializer httpRequestInitializer;
			
			GoogleCredential googleCredential = GoogleCredential.fromStream(
					new FileInputStream(new File("D:/gd/google-calendar-api/login-credentials/sacred-catfish-320906-21e75750665c.json"))
					,GoogleNetHttpTransport.newTrustedTransport(),JSON_FACTORY).createScoped(Collections.singleton(CalendarScopes.CALENDAR));
			
			System.out.println("############# getAccessToken -->  "+googleCredential.getRefreshToken()+" "
			+ googleCredential.getServiceAccountId()+"  "
					+googleCredential.getServiceAccountPrivateKeyId()+"  "
			+googleCredential.getServiceAccountProjectId()+ "  "
					+googleCredential.getServiceAccountUser());
			
			
			
			return new com.google.api.services.calendar.Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, googleCredential)
			.setApplicationName(APPLICATION_NAME)
            .setHttpRequestInitializer(googleCredential).build();
			
			
			/*return new GoogleCredential.Builder()
			        .setServiceAccountId(googleCredential.getServiceAccountId())
			        .setServiceAccountScopes(Collections.singleton(CalendarScopes.CALENDAR))
			        .setServiceAccountPrivateKey(googleCredential.getServiceAccountPrivateKeyId())
			        .setTransport(GoogleNetHttpTransport.newTrustedTransport())
			        .setJsonFactory(JSON_FACTORY)
			        .setRequestInitializer(httpRequestInitializer)
			        .build();
			
			
			credential = new GoogleCredential.Builder()
			        .setTransport(httpTransport)
			        .setJsonFactory(JSON_FACTORY)
			        .setServiceAccountId("110950618982008861338")
			        .setServiceAccountPrivateKeyFromP12File(new File("D:\\gd\\google-calendar-api\\login-credentials\\sacred-catfish-320906-21e75750665c.json"))
			        .setServiceAccountUser("meet@unisisenginnering.com")
			        .build();*/
		} 
		catch ( GeneralSecurityException | IOException e) 
		{
			System.out.println("#############  -->  "+e);
			e.printStackTrace();
		}
		return null;
	}
	
	
	private Date getDateFromString(final String strDate) throws ParseException
	{
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone(timeZone));
		Date date = format.parse(strDate);
		return date;
	}
	
	/**
	 * We need to set Time Zone with date before adding into Google DateTime object. 
	 * This method is using for adding time zone with date.
	 * Currently added the time zone based on Indian time.
	 * 
	 * @param date
	 * @return
	 */
	private String getFormatedDateAsString(Date date)
	{
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"+timeZoneGMT);
		df.setTimeZone(TimeZone.getTimeZone(timeZone));
		return df.format(date);
	}

}
