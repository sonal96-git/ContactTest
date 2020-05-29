package com.pwc.wcm.servlet;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.lang.text.StrTokenizer;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import com.cyscape.countryhawk.Country;

//import com.cyscape.countryhawk.Country;

@Component(service = Servlet.class, immediate = true,
property = {
    Constants.SERVICE_DESCRIPTION + "= Country hawk test",
    "sling.servlet.methods=" + HttpConstants.METHOD_GET,
    "sling.servlet.paths=" + "/bin/countryHawk",
})
public class CountryHawkTest extends SlingAllMethodsServlet {

	@Override
	protected void doGet(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServletException,
			IOException {
	    response.setContentType("text/html");
		//String ip = request.getParameter("ip");
		String ip;
		if(request.getParameter("ip") == null) {
			boolean found = false;
			if ((ip = request.getHeader("x-forwarded-for")) != null) {
				StrTokenizer tokenizer = new StrTokenizer(ip, ",");
				while (tokenizer.hasNext()) {
					ip = tokenizer.nextToken().trim();
					if (isIPv4Valid(ip) && !isIPv4Private(ip)) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				ip = request.getRemoteAddr();
			}
			//InetAddress addr = InetAddress.getByName("ndtv.com");
			//response.getWriter().write(Country.getCountry("ndtv.com").getName());
			//String ip = "10.25.35.149";
			Country c = Country.getCountry(ip);
			response.getWriter().write(ip + " " + c.getCode());
		}
		else
		{
			ip = request.getParameter("ip");
			Country c = Country.getCountry(ip);
			response.getWriter().write(ip + " " + c.getCode());
		}

	}
	public static final String _255 = "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
	public static final Pattern pattern = Pattern.compile("^(?:" + _255 + "\\.){3}" + _255 + "$");
	public static String longToIpV4(long longIp) {
		int octet3 = (int) ((longIp >> 24) % 256);
		int octet2 = (int) ((longIp >> 16) % 256);
		int octet1 = (int) ((longIp >> 8) % 256);
		int octet0 = (int) ((longIp) % 256);
		return octet3 + "." + octet2 + "." + octet1 + "." + octet0;
	}
	public static long ipV4ToLong(String ip) {
		String[] octets = ip.split("\\.");
		return (Long.parseLong(octets[0]) << 24) + (Integer.parseInt(octets[1]) << 16) +
				(Integer.parseInt(octets[2]) << 8) + Integer.parseInt(octets[3]);
	}
	public static boolean isIPv4Private(String ip) {
		long longIp = ipV4ToLong(ip);
		return (longIp >= ipV4ToLong("10.0.0.0") && longIp <= ipV4ToLong("10.255.255.255")) ||
				(longIp >= ipV4ToLong("172.16.0.0") && longIp <= ipV4ToLong("172.31.255.255")) ||
				longIp >= ipV4ToLong("192.168.0.0") && longIp <= ipV4ToLong("192.168.255.255");
	}
	public static boolean isIPv4Valid(String ip) {
		return pattern.matcher(ip).matches();
	}


}
