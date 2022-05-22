Command for running application.
----------------------------------------------
1.	Move the command prompt(DOS) to location …….\google-calendar-api\target
java -jar gd-google-service-api-0.0.1-SNAPSHOT.jar


2.	Type http://localhost:8080/calendar/login/google?doctEmailId=tojikgeorge@gmail.com&patEmailId=tojikgeorge@gmail.com&summery=Follow up status&location=Home&description=Come with records&startDateTime=2021-07-31T20:30:00&endDateTime=2021-07-31T21:30:00

In above url, you have to change date and time accordingly.

3.	At first time will ask google login details for submitting data, then use 
emailid
password



All settings are mentioned in …..\google-calendar-api\src\main\resources\application.properties
