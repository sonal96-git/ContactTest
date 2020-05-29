package com.pwc.user.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.adobe.cq.social.group.api.GroupUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.pwc.user.Constants;
import com.pwc.wcm.utils.LocaleUtils;

/**
 * The Class UserRegistrationUtil Utility provides methods commonly used in UserReg.
 */
public final class UserRegistrationUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRegistrationUtil.class);
    private static final String PARENT_PAGE_PATH_PREFIX = "/content/pwc/";

    /**
     * Gets the {@param requestedCookie} {@link Cookie}. Returns null if no cookie is found or httpRequest is null.
     *
     * @param httpRequest {@link HttpServletRequest}
     * @param requestedCookie {@link String}
     * @return {@link Cookie}
     */
    public static Cookie getPwCCookie(final HttpServletRequest httpRequest, final String requestedCookie) {
        if (httpRequest != null) {
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equalsIgnoreCase(requestedCookie)) {
                        return cookie;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets the option text from the given option string for the given value. Ex: for the option string '&lt;option
     * value='1'>one&lt;\option>&lt;option value='2'>two&lt;\option>' and value given 1, 'one is returned'. Empty is returned in case the
     * value is null or not found or optionString is null.
     *
     * @param optionsString {@link String}
     * @param value {@link String}
     * @return {@link String}
     */
    public static String getOptionText(final String optionsString, final String value) {
        String optionText = "";
        if (optionsString != null) {
            Pattern pattern = Pattern.compile("value=\"" + value + "\"[^>]*>([^<]+)<\\/option>");
            Matcher matcher = pattern.matcher(optionsString);
            if (matcher.find()) {
                optionText = matcher.group(1);
            }
        }
        return optionText;
    }

    /**
     * Gets the AEM {@link Authorizable} if the given property is found with given value on the {@link Authorizable} profile. Returns null
     * if no user is found.
     *
     * @param propertyName {@link String} property name or can be relative property like 'profile/email'
     * @param value {@link String}
     * @param session {@link Session} must have rights to read the users
     * @return {@link Authorizable}
     */
    public static Authorizable getAEMUserByProperty(final String propertyName, final String value, final Session session) {
        try {
            // Create a UserManager instance from the session object
            UserManager userManager = ((JackrabbitSession) session).getUserManager();

            Iterator<Authorizable> users = userManager.findAuthorizables(propertyName, value, UserManager.SEARCH_TYPE_USER);
            if (users.hasNext()) {
                return users.next();
            } else {
                LOGGER.info("UserRegistrationUtil :  getAEMUserByProperty() : No User found with property {} and value {}", propertyName,
                        value);
            }
        } catch (RepositoryException repositoryException) {
            LOGGER.error("UserRegistrationUtil :  getAEMUserByProperty() : Respository Exception occured while reading users {}",
                    repositoryException);
        }
        return null;
    }

    /**
     * Gets the AEM {@link Authorizable} for the given email ID. Returns null if no user is found.
     *
     * @param email {@link String}
     * @param session {@link Session} must have rights to read the users
     * @return {@link Authorizable}
     */
    public static Authorizable getAEMUserByEmail(String email, Session session) {
        if(email != null) {
            email = email.toLowerCase();
        }
        return getAEMUserByProperty("profile/email", email, session);
    }

    /**
     * Gets the string from node path.
     *
     * @param nodePath {@link String}
     * @param session {@link Session} must have rights to read the node
     * @return {@link String}
     */
    public static String getStringFromNodePath(String nodePath, Session session) {
        try {
            if (nodePath != null) {
                Node contentNode = session.getNode(nodePath);
                if (contentNode != null) {
                    InputStream emailStream = contentNode.getProperty(JcrConstants.JCR_DATA).getBinary().getStream();
                    return IOUtils.toString(emailStream, Constants.UTF_8_ENCODING);
                }
            }
        } catch (RepositoryException repositoryException) {
            LOGGER.error(
                    "UserRegistrationUtil :  getStringFromNodePath() : Respository Exception occured while reading the node of path {}",
                    repositoryException);
        } catch (IOException ioException) {
            LOGGER.error("UserRegistrationUtil :  getStringFromNodePath() : IO Exception occured while reading the node of path {}",
                    ioException);
        }
        return null;
    }

    /**
     * Gets {@link JSONObject} from {@link SlingHttpServletRequest}. Returns null if {@link SlingHttpServletRequest} is null.
     *
     * @param request {@link SlingHttpServletRequest}
     * @return {@link JSONObject}
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws JSONException the JSON exception
     */
    public static JSONObject getJSONFromRequest(SlingHttpServletRequest request) throws IOException, JSONException {
        JSONObject jsonObject = null;
        if (request != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), Constants.UTF_8_ENCODING));
            String json = StringUtils.EMPTY;
            if (br != null) {
                json = br.readLine();
            }
            jsonObject = new JSONObject(json);
        }
        return jsonObject;
    }

    public static String getParentPagePath(String territory, String language) {
        return PARENT_PAGE_PATH_PREFIX + territory + "/" + language;
    }

    public static String getParentPagePath(String locale) {
        return locale == null ? null : PARENT_PAGE_PATH_PREFIX + LocaleUtils.getTerritoryFromLocale(locale) + "/" + LocaleUtils.getLanguageFromLocale(locale);
    }

    /**
     * Creates a new csv file of given {@param fileName} {@link String}, under {@param parentNode} {@link Node}. Replaces any existing file with the same name.
     *
     * @param parentNode {@link Node} node under which file wll be created.
     * @param session    {@link Session} session used to create the file.
     * @param is         {@link InputStream} data to be written in the file.
     * @param fileName   {@link String} name of the file to be created.
     * @throws ParseException      Signals that a Parse exception has occurred while creating the file.
     * @throws RepositoryException Signals that a Repository exception has occurred while creating the file.
     */
    public static void createCSVFileReport(Node parentNode, Session session, InputStream is, final String fileName) throws ParseException, RepositoryException {

        if (parentNode.hasNode(fileName)) {
            Node fileNode = parentNode.getNode(fileName);
            fileNode.remove();
        }

        ValueFactory valueFactory = session.getValueFactory();
        Binary contentValue = valueFactory.createBinary(is);

        Node fileNode = parentNode.addNode(fileName, "nt:file");

        fileNode.addMixin("mix:referenceable");

        Node resNode = fileNode.addNode("jcr:content", "nt:resource");

        resNode.setProperty("jcr:mimeType", "text/csv");
        resNode.setProperty("jcr:data", contentValue);

        Calendar lastModified = Calendar.getInstance();

        lastModified.setTimeInMillis(lastModified.getTimeInMillis());
        resNode.setProperty("jcr:lastModified", lastModified);
    }
}
