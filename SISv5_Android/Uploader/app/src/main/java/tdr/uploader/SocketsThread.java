package tdr.uploader;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


/**
 * Created by mzhang on 12/1/16.
 */

class SocketsThread {
    static final String SMTP_HOST_NAME = "smtp.ksiresearch.org.ipage.com";
    static final String SMTP_PORT = "587";
    static final String emailFromAddress = "chronobot@ksiresearch.org";
    static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
    private static final int duration = Toast.LENGTH_SHORT;
    static final private String TAG = "SocketsThread";
    static final private String MyName = "AndroidUploader";
    private static UploaderReading reading = new UploaderReading();
    private static int counter = 1;
    private int connectionNumber;
    private MsgDecoder msgDecoder;
    private MsgEncoder msgEncoder;
    private Activity activity;


    private static boolean execute(String query) throws Exception {
        String url = "http://ksiresearch.org/chronobot/PHP_Post_copy.php";
        return PostQuery.PostToPHP(url, query);
    }

    private static String formQuery(long datetime, String source, String type, Object value) {
        return "Insert into `records` (`uid`, `datetime`, `source`, `type`, `value`) values ('"
                + reading.uid
                + "',"
                + "FROM_UNIXTIME(" + datetime / 1000 + ")"
                + ",'"
                + source
                + "','"
                + type
                + "','"
                + value.toString()
                + "')";
    }
    static void processMessage(KeyValueList kvList) {
        final String scope = kvList.getValue("Scope");
        final String messageType = kvList.getValue("MessageType");
        final String sender = kvList.getValue("Sender");
        Log.d(TAG, "Message Received: " + kvList.toString());

        switch (messageType) {
            case "Reading":
                reading.message_sender = sender;
                reading.BP = kvList.getValue("Data_BP");
                reading.ECG = kvList.getValue("Data_ECG");
                reading.EMG = kvList.getValue("Data_EMG");
                reading.Pulse = kvList.getValue("Data_Pulse");

                SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                reading.readable_time = date_format.format(new Date(reading.collection_time));
                Log.d(TAG, "Readable Date: " + reading.readable_time);

                String emailSubject = "Android App Readings from " + sender;
                String emailContent = kvList.toString();
                try {
                    send_msg();
                    //send_email(emailFromAddress, reading.recipients, emailSubject, emailContent);
                    sendSSLMessage(emailFromAddress, reading.recipients, emailSubject, emailContent);
                } catch (MessagingException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "Register":

                break;
        }
    }

    static void send_msg() throws Exception {
        boolean result;
        result = execute(formQuery(reading.collection_time, "Android_BP", "BloodPressure", reading.BP));
        result = result && execute(formQuery(reading.collection_time, "Android_EMG", "Electromyography", reading.EMG));
        result = result && execute(formQuery(reading.collection_time, "Android_ECG", "Electrocardiogram", reading.ECG));
        result = result && execute(formQuery(reading.collection_time, "Android_Pulse", "Heart Rate", reading.Pulse));

        Log.d(TAG, "Update to database completed.");
    }


    static void sendSSLMessage(String from, String recipients[],
                               String subject, String message) throws MessagingException
    {
        boolean debug = false;
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST_NAME);
        props.put("mail.smtp.auth", "true");
        props.put("mail.debug", "true");
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.socketFactory.port", SMTP_PORT);
        props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
        props.put("mail.smtp.socketFactory.fallback", "true");

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator()
                {
                    protected PasswordAuthentication getPasswordAuthentication()
                    {
                        return new PasswordAuthentication(
                                "chronobot@ksiresearch.org", "Health14");
                    }
                });
        session.setDebug(debug);

        Message msg = new MimeMessage(session);
        InternetAddress addressFrom = new InternetAddress(from);
        msg.setFrom(addressFrom);

        InternetAddress[] addressTo = new InternetAddress[recipients.length];
        for (int i = 0; i < recipients.length; i++)
        {
            addressTo[i] = new InternetAddress(recipients[i]);
        }
        msg.setRecipients(Message.RecipientType.TO, addressTo);
        msg.setSubject(subject);
        {
            Multipart multipart = new MimeMultipart("related");
            {
                Multipart newMultipart = new MimeMultipart("alternative");
                BodyPart nestedPart = new MimeBodyPart();
                nestedPart.setContent(newMultipart);
                multipart.addBodyPart(nestedPart);
                {
                    BodyPart part = new MimeBodyPart();
                    part.setText("SIS DATA:");
                    newMultipart.addBodyPart(part);

                    part = new MimeBodyPart();
                    // the first string is email context

                    part.setContent(message, "text/html");
                    newMultipart.addBodyPart(part);
                }
            }

            msg.setContent(multipart);

        }
        Transport.send(msg);
        System.out.println("Successfully Sent mail to All Users, lol.\n");
    }
}

class UploaderReading {
    String uid = "376896";
    String firstName = "Shi-Kuo";
    String lastName = "Chang";
    String[] recipients = {"sisfortest@outlook.com",
            "chronobot@ksiresearch.org"
    };

    long collection_time;
    String readable_time;
    String BP;
    String EMG;
    String ECG;
    String Pulse;
    String message_sender;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("----------------------------------------------\n");
        builder.append("First Name: " + firstName + "\n");
        builder.append("Last Name: " + lastName + "\n");
        builder.append("Email: " + Arrays.toString(recipients) + "\n\n");
        builder.append("Device: " + message_sender + "\n");
        builder.append("Collection Time: " + readable_time + "\n\n");
        builder.append("Blood Pressure: " + BP + "\n");
        builder.append("Electromyography: " + ECG + "\n");
        builder.append("Electrocardiogram: " + EMG + "\n");
        builder.append("Heart Rate: " + Pulse + "\n");
        builder.append("----------------------------------------------\n");
        return builder.toString();
    }
}
//    static void send_email(String from, String[] recipients,
//                           String subject, String content) throws MessagingException {
//        Log.d("Email", "Start to send Email.");
//        boolean debug = false;
//        Properties props = new Properties();
//        props.put("mail.smtp.host", SMTP_HOST_NAME);
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.port", SMTP_PORT);
//        props.put("mail.smtp.ssl.enable", "true");
//        Log.d("Email", "Set parameters successfully.");
//
//        Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication("chronobot@ksiresearch.org", "Health14");
//            }
//        });
//        Log.d("Email", "Sessions created successfully.");
//        session.setDebug(debug);
//
//        Message msg = new MimeMessage(session);
//        InternetAddress addressFrom = new InternetAddress(from);
//        msg.setFrom(addressFrom);
//
//        InternetAddress[] addressTo = new InternetAddress[recipients.length];
//        for (int i = 0; i < recipients.length; i++) {
//            addressTo[i] = new InternetAddress(recipients[i]);
//        }
//        msg.setRecipients(Message.RecipientType.TO, addressTo);
//
//        msg.setSubject(subject);
//
//        msg.setText(reading.toString());
//
//        Log.d("Email", "Ready to send out messages.");
//        Transport.send(msg);
//
//        Log.d("Email", "Send sensor readings to recipients' Email successfully!");
//    }
