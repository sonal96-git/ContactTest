package com.pwc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * ExceptionLogger is intended to be used to send exceptions to a common exceptions log.
 */
public final class ExceptionLogger {

	private static Logger log = LoggerFactory.getLogger(ExceptionLogger.class);
	
	private ExceptionLogger(){
		
	}
	
	public static void logException(Exception e){
		log.error("", e);
	}
	
	public static void logExceptionMessage(String message,Exception e){
		log.error(message, e);
	}
}
