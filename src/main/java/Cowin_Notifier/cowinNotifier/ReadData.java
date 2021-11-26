package Cowin_Notifier.cowinNotifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ReadData {

	public static HttpURLConnection hr;
	public static Long available;
	public static String name;
	public static Long minAge;
	
	public static void main(String[] args) throws ParseException, MessagingException  {
		URL url;
		
		BufferedReader breader;
		String line;
		StringBuffer responseContent = new StringBuffer();
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");  
		Date date = new Date(); 
		
		//System.out.println(formatter.format(date));
		
		try {
			url = new URL("https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByDistrict?district_id=194&date=" + formatter.format(date));
			hr = (HttpURLConnection) url.openConnection();
			
			hr.setRequestMethod("GET");
			hr.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36");
			hr.setConnectTimeout(5000);
			hr.setReadTimeout(5000);
			
			int status = hr.getResponseCode();
			
			//System.out.print(status);
			
			if (status > 299) {
				breader = new BufferedReader(new InputStreamReader(hr.getErrorStream()));
				while((line = breader.readLine()) != null) {
					responseContent.append(line);
			}
				
				breader.close();
				
			}else {
				breader = new BufferedReader(new InputStreamReader(hr.getInputStream()));
				while((line = breader.readLine()) != null) {
					responseContent.append(line);
			}
				
				breader.close();
				
			}
			
			
			String json = responseContent.toString();
			
			JSONParser jsonparser = new JSONParser();
			
			Object obj = jsonparser.parse(json);
			
			JSONObject jsonObject = (JSONObject) obj;
			
			JSONArray centers = (JSONArray) jsonObject.get("centers");
			
			while(true) {	
				
				for(int i=0; i<centers.size(); i++) {
				
					 JSONObject j1 = (JSONObject) centers.get(i);
					 
					 String center_id = (String) j1.get("center_id").toString();
					 name = (String) j1.get("name").toString();
					 String address = (String) j1.get("address").toString();
					 String state = (String) j1.get("state_name").toString();
					 String district = (String) j1.get("district_name").toString();
					 String pincode = (String) j1.get("pincode").toString();
					 JSONArray sessions = (JSONArray) j1.get("sessions");
					 
					 
					 System.out.println("Center ID: " + center_id);
					 System.out.println("Name: " + name);
					 System.out.println("Address: " + address);
					 System.out.println("State: " + state);
					 System.out.println("District: " + district);
					 System.out.println("Pincode: " + pincode);
				
					 	for(int j=0; j<sessions.size(); j++) {
					 		JSONObject j2 = (JSONObject) sessions.get(j);
					 		
					 		minAge = (Long) j2.get("min_age_limit");
					 		String vaccine = (String) j2.get("vaccine");
					 		available = (Long) j2.get("available_capacity");
					 		
					 		System.out.println("Min Age: " + minAge);
					 		System.out.println("Vaccine: " + vaccine);
					 		System.out.println("Available Capacity: " + available);
					 		
					 			if(available > 0) {
					 				sendEmail("Recepient's email address");
					 			}
					 			else {
					 				System.out.println("\n\nSorry, No slots are available for booking");
					 				System.out.println("------------------------------------------");
					 			}		
					 	}
					}

				Thread.currentThread();
				Thread.sleep(10000); //10 secs
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			hr.disconnect();
		}
	}

	//Method to send emails as a notification whenever slots are available

	private static void sendEmail(String recepient) throws MessagingException {
		System.out.println("Preparing to send email..\n");
		Properties properties = new Properties();
		
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");
		
		final String myEmail = "Your email address";
		final String password = "Your password";
		
		Session s = Session.getInstance(properties, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication(){
				return new PasswordAuthentication(myEmail, password);	
			}
		});
		
		Message message = prepareMessage(s, myEmail, recepient, available, name);
		
		Transport.send(message);
		System.out.println("Mail sent successfully!");
		System.out.println("-----------------------------------------");
	}
	
	private static Message prepareMessage(Session s, String myEmail, String recepient, Long available, String name) {
		
		try {
		
			Message message = new MimeMessage(s);
			message.setFrom(new InternetAddress(myEmail));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(recepient));
			message.setSubject("Vaccination slots available!");
			String htmlCode = "<h3>SLOTS OPENED </h3> <br/> <p>Hey There, <br/> Vaccination slots are available for booking in " + name + "</p> <b> Available Capacity: </b>" + available.toString() + "<br /><b> Minimum Age: </b>" + minAge.toString() + "<br/><br/><br/><p><center> www.cowin.gov.in/home <br /> Click Here to book your slot now.</center></p>";
			message.setContent(htmlCode, "text/html");
			
		//	message.setText("Hey There, \n Slots are available for booking");
				
			return message;
			
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
