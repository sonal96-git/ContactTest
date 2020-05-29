package com.pwc.util;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to provide Akamai related information from request.
 */
public class AkamaiUtils {
    public static String AKAMAI_REQ_HEADER_EDGESCAPE = "X-Akamai-Edgescape";
    public static String AKAMAI_REQ_HEADER_CLIENT_LOC = "x-client-location";
    public static String AKAMAI_REQ_PARAM_KEY = "userLocation";
    public static String REQ_HEADER_COUNTRY_CODE_REGEX = ".*(?<=country_code=)([a-zA-Z]*).*";
    private static final Logger LOGGER = LoggerFactory.getLogger(AkamaiUtils.class);
    
    /**
     * Fetches and returns client location from Akamai headers, {@value #AKAMAI_REQ_HEADER_CLIENT_LOC} and
     * {@value #AKAMAI_REQ_HEADER_EDGESCAPE} from {@link HttpServletRequest}.
     * 
     * @param request {@link HttpServletRequest}
     * @return {@link String}
     */
    public static String getLocationFromAkamaiRequestHeader(final HttpServletRequest request) {
        logAllRequestHeaders(request);
        String akamaiHeader = request.getHeader(AKAMAI_REQ_HEADER_CLIENT_LOC);
        LOGGER.debug("Fetching User Location from Akamai Header: '" + AKAMAI_REQ_HEADER_CLIENT_LOC + "' = " + akamaiHeader);
        if (StringUtils.isNotBlank(akamaiHeader)) {
            return akamaiHeader.trim();
        }
        akamaiHeader = request.getHeader(AKAMAI_REQ_HEADER_EDGESCAPE);
        LOGGER.debug("Fetching User Location from Akamai Header: '" + AKAMAI_REQ_HEADER_EDGESCAPE + "' = " + akamaiHeader);
        return StringUtils.isBlank(akamaiHeader) || !akamaiHeader.matches(REQ_HEADER_COUNTRY_CODE_REGEX) ? ""
                : akamaiHeader.replaceFirst(REQ_HEADER_COUNTRY_CODE_REGEX, "$1");
    }
    
    /**
     * Returns the value of client's location from Query Param, {@value AKAMAI_REQ_PARAM_KEY}. Used for debugging purposes.
     * 
     * @param request {@link HttpServletRequest}
     * @return {@link String}
     */
    public static String getLocationFromQueryParam(final HttpServletRequest request) {
        return request.getParameter(AKAMAI_REQ_PARAM_KEY);
    }
    
    /**
     * Logs all the request headers in the debug mode.
     * 
     * @param request {@link HttpServletRequest}
     */
    private static void logAllRequestHeaders(final HttpServletRequest request) {
        if (LOGGER.isDebugEnabled()) {
            Enumeration headerNames = request.getHeaderNames();
            String reqHeadersInfo = "";
            while (headerNames.hasMoreElements()) {
                String headerName = (String) headerNames.nextElement();
                reqHeadersInfo += "\n" + headerName + " = " + request.getHeader(headerName);
            }
            LOGGER.debug("Request Headers for the URL '" + request.getRequestURL() + "' :" + reqHeadersInfo);
        }
    }
}
