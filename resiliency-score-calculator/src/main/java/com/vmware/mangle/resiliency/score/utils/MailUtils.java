/*
 * Copyright (c) 2016-2019 VMware, Inc. All Rights Reserved.
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with separate copyright notices
 * and license terms. Your use of these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package com.vmware.mangle.resiliency.score.utils;

import java.util.Date;
import java.util.Map;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.vmware.mangle.exception.MangleMailerException;

import lombok.extern.log4j.Log4j2;

/**
 * @author ranjans
 *
 */
@Log4j2
public class MailUtils {
    private static Properties properties = ReadProperty.readProperty("smtp_config.properties");
    public static String SMTP_HOST = properties.getProperty("smtp.host");
    public static String IS_AUTH_REQUIRED = properties.getProperty("smtp.auth.required");
    public static String SMTP_USERNAME = properties.getProperty("smtp.username");
    public static String SMTP_PASSWORD = properties.getProperty("smtp.password");
    public static String EMAIL_FROM = properties.getProperty("email.from");
    public static String IS_TLS_ENABLED = properties.getProperty("smtp.tls.enabled");

    public static String getHTMLTabletext(String firstColoumn, String secondColoumn, Map<String, String> testResults) {
        String messageText =
                "<TABLE border=\"2\" cellpadding=\"1\" cellspacing=\"0\" width=\"1000\"><tr><th width=\"50%\" align=\"center\" bgcolor=\"yellow\"><font color=\"black\">"
                        + firstColoumn
                        + "</font></th><th width=\"50%\" align=\"center\" bgcolor=\"yellow\"><font color=\"black\">"
                        + secondColoumn + "</font></th></tr>";
        for (String key : testResults.keySet()) {
            messageText = messageText
                    + "<tr><td align=\"center\"><font size=\"2\" face=\"times new roman\" color=\"purple\" >" + key
                    + "</font></td> <td align=\"center\"><font size=\"1\" face=\"verdana\" color=\"green\">"
                    + testResults.get(key) + "</font></td></tr>";
        }
        messageText = messageText + "</table>";
        return messageText;
    }

    public static String getHTMLHeadertext(String inputText, int headerLevel) {
        return "<H" + headerLevel + " align=\"center\">" + inputText + "</H" + headerLevel + ">";
    }

    public static boolean mail(String[] recepientAddresses, String messageText, String subject,
            String[] attachmentPaths) throws MangleMailerException {
        // SUBSTITUTE YOUR EMAIL ADDRESSES HERE!!!
        InternetAddress[] addresses = null;
        if (recepientAddresses == null) {
            log.info("No recepients specified");
            return false;
        } else {
            addresses = convertToInternetAddresses(recepientAddresses);
        }
        // Create properties for the Session
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.debug", "false");
        // Get a session
        Session session = Session.getInstance(props);
        try {
            // Get a Transport object to send e-mail
            Transport bus = session.getTransport("smtp");
            bus.connect();
            // Instantiate a message
            Message msg = new MimeMessage(session);
            // Set message attributes
            msg.setFrom(new InternetAddress(EMAIL_FROM));
            msg.setRecipients(Message.RecipientType.TO, addresses);
            // Parse a comma-separated list of email addresses. Be strict.
            // msg.setRecipients(Message.RecipientType.CC, addresses);
            msg.setSubject(subject);
            msg.setSentDate(new Date());
            setMailBody(msg, messageText);
            for (int i = 0; i < attachmentPaths.length; i++) {
                setFileAsAttachment(msg, attachmentPaths[i]);
            }
            msg.saveChanges();
            bus.sendMessage(msg, addresses);
        } catch (MessagingException mex) {
            // Prints all nested (chained) exceptions as well
            log.error(mex.getMessage());
            while (mex.getNextException() != null) {
                Exception ex = mex.getNextException();
                log.error(mex.getMessage());
                if (!(ex instanceof MessagingException)) {
                    break;
                } else {
                    mex = (MessagingException) ex;
                }
            }
        }
        return true;
    }

    public static boolean mail(String[] recepientAddresses, String messageText, String subject,
            String[] attachmentPaths, Map<String, String> bodyImagesLocationAndId) throws MangleMailerException {
        InternetAddress[] addresses = null;
        if (recepientAddresses == null) {
            log.info("No recepients specified");
            return false;
        } else {
            addresses = convertToInternetAddresses(recepientAddresses);
        }
        Properties props = new Properties();
        if (IS_TLS_ENABLED.equals("YES")) {
            props.put("mail.smtp.starttls.enable", "true");
        }
        if (IS_AUTH_REQUIRED.equals("YES")) {
            props.put("mail.smtp.auth", "true");
        } else {
            props.put("mail.smtp.host", SMTP_HOST);
        }
        Session session = Session.getInstance(props);
        try {
            Transport bus = session.getTransport("smtp");
            if (IS_AUTH_REQUIRED.equals("YES")) {
                bus.connect(SMTP_HOST, SMTP_USERNAME, SMTP_PASSWORD);
            } else {
                bus.connect();
            }
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(EMAIL_FROM));
            msg.setRecipient(Message.RecipientType.TO, addresses[0]);
            msg.setSubject(subject);
            msg.setSentDate(new Date());
            setMailBody(msg, messageText, bodyImagesLocationAndId);
            for (int i = 0; i < attachmentPaths.length; i++) {
                setFileAsAttachment(msg, attachmentPaths[i]);
            }
            msg.saveChanges();
            bus.sendMessage(msg, addresses);

        } catch (MessagingException mex) {
            log.error(mex.getMessage());
            while (mex.getNextException() != null) {
                Exception ex = mex.getNextException();
                log.error(mex.getMessage());
                if (!(ex instanceof MessagingException)) {
                    break;
                } else {
                    mex = (MessagingException) ex;
                }
            }
        }
        return true;
    }

    private static InternetAddress[] convertToInternetAddresses(String[] recepientAddresses)
            throws MangleMailerException {
        try {
            InternetAddress[] addresses = new InternetAddress[recepientAddresses.length];
            for (int i = 0; i < recepientAddresses.length; i++) {
                addresses[i] = new InternetAddress(recepientAddresses[i]);
            }
            return addresses;
        } catch (Exception e) {
            throw new MangleMailerException("Failed to covert given String Input to InternetAddress", e);
        }
    }

    private static void setMailBody(Message msg, String messageText) throws MangleMailerException {
        try {
            // Create and fill first part
            MimeBodyPart p1 = new MimeBodyPart();
            p1.setContent(messageText, "text/html");
            // Create second part
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(p1);
            // Set Multipart as the message's content
            msg.setContent(mp);
        } catch (Exception e) {
            throw new MangleMailerException("Failed to set Mail Body", e);
        }
    }

    private static void setMailBody(Message msg, String messageText, Map<String, String> bodyImagesLocationAndId)
            throws MangleMailerException {
        try {
            MimeBodyPart p1 = new MimeBodyPart();
            p1.setContent(messageText, "text/html");
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(p1);
            for (Map.Entry<String, String> entry : bodyImagesLocationAndId.entrySet()) {
                mp.addBodyPart(getMimeBodyPartForImage(entry.getKey(), entry.getValue()));
            }
            msg.setContent(mp);
        } catch (Exception e) {
            throw new MangleMailerException("Failed to set Mail Body", e);
        }
    }

    private static MimeBodyPart getMimeBodyPartForImage(String imageLocation, String imageId) {
        MimeBodyPart mimeBodyPartImage = new MimeBodyPart();
        DataSource fds = new FileDataSource(imageLocation);
        try {
            mimeBodyPartImage.setDataHandler(new DataHandler(fds));
            mimeBodyPartImage.setHeader("Content-ID", imageId);
        } catch (MessagingException e) {

        }
        return mimeBodyPartImage;
    }

    // Set a file as an attachment. Uses JAF FileDataSource.
    private static void setFileAsAttachment(Message msg, String filename) throws MangleMailerException {
        try {
            // Create second part
            MimeBodyPart p2 = new MimeBodyPart();
            // Put a file in the second part
            FileDataSource fds = new FileDataSource(filename);
            p2.setDataHandler(new DataHandler(fds));
            p2.setFileName(fds.getName());
            // Create the Multipart. Add BodyParts to it.
            ((Multipart) msg.getContent()).addBodyPart(p2);
        } catch (Exception e) {
            throw new MangleMailerException("Failed to attach file", e);
        }
    }

    public String getContentType() {
        return "text/html";
    }

    public String getName() {
        return "JAF text/html dataSource to send e-mail only";
    }

}
