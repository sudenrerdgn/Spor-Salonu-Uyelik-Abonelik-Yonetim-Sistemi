import java.util.Properties;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class TestMail {
    public static void main(String[] args) {
        String username = "fitzonedestek@gmail.com"; 
        String password = "srtrfiadbvtqldlk"; 
        String toEmail = "fitzonedestek@gmail.com"; // self-send for test

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(prop,
            new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, "FitZone Pro"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Test Mail");
            message.setText("This is a test mail");

            Transport.send(message);
            System.out.println("✅ OK");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
