package net.codejava;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

public class SendEmailThread implements Runnable{
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger( this.getClass() );
	
	public String content;
	public String subject;
	public String listRec;
	Properties commonConfig;
	public SendEmailThread(String content, Properties commonConfig) {
		this.content = content;
		this.commonConfig = commonConfig;
	}
	
	public SendEmailThread(String content, String subject,String listRec, Properties commonConfig) {
		this.content = content;
		this.subject = subject;
		this.commonConfig = commonConfig;
		this.listRec = listRec;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			log.info("Send alert mail: START");
			Properties mailProp = new Properties();
			mailProp.put("mail.smtp.host", commonConfig.getProperty("email.host"));
			mailProp.put("mail.smtp.port", commonConfig.getProperty("email.port"));
			mailProp.put("mail.smtp.auth", commonConfig.getProperty("email.auth"));
			mailProp.put("mail.smtp.socketFactory.port",commonConfig.getProperty("email.socketFactory.port"));
			mailProp.put("mail.smtp.timeout", commonConfig.getProperty("email.timeout"));

			mailProp.put("mail.smtp.ssl.enable", commonConfig.getProperty("email.ssl.enable"));
			mailProp.put("mail.smtp.starttls.enable", commonConfig.getProperty("email.starttls.enable"));
			
			mailProp.put("mail.smtp.starttls.required", "true");
			mailProp.put("mail.smtp.ssl.protocols", "TLSv1.2");
			
			Session session = null;
			session = Session.getInstance(mailProp, null);
		
			MimeMessage mymsg = new MimeMessage(session);
			mymsg.setSentDate(new Date());
			mymsg.setFrom(new InternetAddress(commonConfig.getProperty("email.from")));
			
			if(subject == null) {
				mymsg.setSubject(commonConfig.getProperty("email.subject"));
			}else {
				mymsg.setSubject(subject);
			}
			
			mymsg.setContent(content,"text/html; charset=UTF-8");
			String recipient_ls = listRec;
			if (recipient_ls == null)
				recipient_ls = commonConfig.getProperty("email.recipient");
			
			
			String[] recipientList = recipient_ls.split(",");
			InternetAddress[] recipientAddress = new InternetAddress[recipientList.length];
			int counter = 0;
			
			for (String recipient : recipientList) {
			    recipientAddress[counter] = new InternetAddress(recipient.trim());
			    counter++;
			}
			
			mymsg.setRecipients(Message.RecipientType.TO, recipientAddress);
			
			mymsg.saveChanges();
			Transport trans = null;
			
			trans = session.getTransport("smtp");
			final String username = commonConfig.getProperty("email.username");
	        final String password = commonConfig.getProperty("email.password");
			trans.connect(username, password);
			trans.sendMessage(mymsg, mymsg.getAllRecipients());
			
			log.info("Send alert mail DONE");
		} catch (Exception e) {
			log.info("Send alert email exception: ",e);
		}
	}
}
