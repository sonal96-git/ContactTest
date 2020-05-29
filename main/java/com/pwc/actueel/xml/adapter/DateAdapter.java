package com.pwc.actueel.xml.adapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles Date Formatting for Article's Pub date in RSS Feeds.
 */
public class DateAdapter extends XmlAdapter<String, String> {
    private final static Logger LOGGER = LoggerFactory.getLogger(DateAdapter.class);
    private final static DateTimeFormatter pageDateFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSz");
    private final static DateTimeFormatter xmlDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    @Override
    public String unmarshal(final String xmlDate) {
        final LocalDateTime dateTime = LocalDateTime.parse(xmlDate, xmlDateFormatter);
        return dateTime.toString();
    }
    
    @Override
    public String marshal(final String pageDateTime) {
        try {
            if (pageDateTime != null && !pageDateTime.isEmpty()) {
                final LocalDateTime dateTime = LocalDateTime.parse(pageDateTime, pageDateFormatter);
                final String formattedDate = dateTime.format(xmlDateFormatter);
                LOGGER.debug("Date transformation successfull from '" + pageDateTime + "' to '" + formattedDate + "'");
                return formattedDate;
            }
        } catch (final DateTimeParseException dtParseExcep) {
            LOGGER.error("Error received while parsing date: " + pageDateTime + "!", dtParseExcep);
        }
        return "";
    }
}
