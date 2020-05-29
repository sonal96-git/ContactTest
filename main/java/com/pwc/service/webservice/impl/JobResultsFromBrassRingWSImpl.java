/*
 * 
 */
package com.pwc.service.webservice.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang.StringUtils;
import org.integrationuri.WebRouter;
import org.integrationuri.WebRouterSoap;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.model.xml.request.Envelope;
import com.pwc.model.xml.request.Group;
import com.pwc.model.xml.request.Groups;
import com.pwc.model.xml.request.InputString;
import com.pwc.model.xml.request.JobDescription;
import com.pwc.model.xml.request.Packet;
import com.pwc.model.xml.request.PacketInfo;
import com.pwc.model.xml.request.Payload;
import com.pwc.model.xml.request.Question;
import com.pwc.model.xml.request.Questions;
import com.pwc.model.xml.request.QuestionsForANDCondition;
import com.pwc.model.xml.request.ReturnJobDetailQues;
import com.pwc.model.xml.request.Sender;
import com.pwc.model.xml.request.Unit;
import com.pwc.service.webservice.JobResultsFromBrassRingWS;

/**
 * Marshalls & Sends a Request-XML to Soap Web Service and receives the Response
 * Xml.
 */
@Component(immediate = true, service = { JobResultsFromBrassRingWS.class }, enabled = true,
property = {
        Constants.SERVICE_DESCRIPTION + "= PwC Get Xml Response From SOAP Web Service" })
@Designate(ocd = JobResultsFromBrassRingWSImpl.Config.class )
public class JobResultsFromBrassRingWSImpl implements JobResultsFromBrassRingWS {

	private WebRouter webRouter;
	private final Logger logger = LoggerFactory.getLogger(JobResultsFromBrassRingWSImpl.class);
	private static final String CONST_STRING_VALUE_RETURN_JOBS_COUNT = "TG_SEARCH_ALL";
	String requestQuestionIDs = "";
	
	@ObjectClassDefinition(name = "PwC Get Xml Response From SOAP Web Service ", description = "")
    @interface Config {
    	@AttributeDefinition(name = "WSDL location", 
    						description = "WSDL location to consume web service.",
    						type = AttributeType.STRING)
    	public String wsdlLocation() default StringUtils.EMPTY;
    }

	@Activate
	protected void activate(final JobResultsFromBrassRingWSImpl.Config context) {
		//final Dictionary<?, ?> properties = context.getProperties();	//TODO: Getting the Property via Config File.
		final String wsdlUrlPath = context.wsdlLocation();
		try {
			final URL wsdlUrl = new URL(wsdlUrlPath);
			webRouter = new WebRouter(wsdlUrl);
		} catch (final MalformedURLException e) {
			logger.error("Can not initialize the default wsdl from " + wsdlUrlPath, e);
		}
	}

	/**
	 * Creates & Sends Input Xml based on passed parameters to Brass Ring Web
	 * Service and returns the Response Xml containing Job Results.
	 *
	 * @param clientId {@link String}
	 * @param siteId {@link String}
	 * @param country {@link String}
	 * @param location {@link String}
	 * @param service {@link String}
	 * @param specialism {@link String}
	 * @param industry {@link String}
	 * @param keyword {@link String}
	 * @param grade {@link String}
	 * @param countryCode {@link String}
	 * @param locationCode {@link String}
	 * @param serviceCode {@link String}
	 * @param keywordCode {@link String}
	 * @param industryCode {@link String}
	 * @param specialismCode {@link String}
	 * @param gradeCode {@link String}
	 * @return {@link String}
	 */
	@Override
	public String generateInputAndGetJobResultsFromBrassRingWS(final String clientId, final String siteId, final String country,
			final String location, final String service, final String specialism, final String industry, final String keyword, final String grade,
			final String countryCode, final String locationCode, final String serviceCode, final String specialismCode, final String industryCode,
			final String keywordCode, final String gradeCode) {
		String result = "";
		if (webRouter != null) {
			final String inputXml = createInputXmlForJobResults(clientId, siteId, country, location, service, specialism, industry, keyword, grade,
					countryCode, locationCode, serviceCode, specialismCode, industryCode, keywordCode, gradeCode);
			result = passInputXmlToGetResultFromBrassRingWS(inputXml);
		}
		return result;
	}

	@Override
	public String generateInputAndGetJobResultsFromBrassRingWS(final String clientId, final String siteId, final String country,
			final String location, final String service, final String specialism, final String keyword, final String localregion,
			final String applicationdeadline, final String entryroute, final String entryroutegroup, final String intakeyear,
			final String programmetype, final String startseason, final String countryCode, final String locationCode, final String serviceCode,
			final String specialismCode, final String keywordCode, final String localregionCode, final String applicationdeadlineCode,
			final String entryrouteCode, final String entryroutegroupCode, final String intakeyearCode, final String programmetypeCode,
			final String startseasonCode) {
		String result = "";
		if (webRouter != null) {
			requestQuestionIDs = "";
			final Questions questions = new Questions(new ArrayList<Question>());
			addQuestion(questions, countryCode, country);
			addQuestion(questions, locationCode, location);
			addQuestion(questions, serviceCode, service);
			addQuestion(questions, specialismCode, specialism);
			addQuestion(questions, localregionCode, localregion);
			addQuestion(questions, keywordCode, keyword);
			addQuestion(questions, applicationdeadlineCode, applicationdeadline);
			addQuestion(questions, entryrouteCode, entryroute);
			addQuestion(questions, entryroutegroupCode, entryroutegroup);
			addQuestion(questions, intakeyearCode, intakeyear);
			addQuestion(questions, programmetypeCode, programmetype);
			addQuestion(questions, startseasonCode, startseason);

			final Envelope envelope = getEnvelopeForGivenParameters(clientId, siteId, "", questions,"");
			final String inputXml = marshalInputXml(envelope);

			result = passInputXmlToGetResultFromBrassRingWS(inputXml);
		}
		return result;
	}

	/**
	 * Creates & Sends Input Xml based on passed parameters to Brass Ring Web
	 * Service and returns the Response Xml containing Job Description.
	 *
	 * @param clientId {@link String}
	 * @param siteId {@link String}
	 * @param jobId {@link String}
	 * @param keywordCode {@link String}
	 * @return {@link String}
	 */
	@Override
	public String generateInputAndGetJobDescriptionFromBrassRingWS(final String clientId, final String siteId, final String jobId,
			final String keywordCode,final String jobDetailQues) {
		String result = "";
		if (webRouter != null) {
			final String inputXml = createInputXmlForJobDescription(clientId, siteId, jobId, keywordCode,jobDetailQues);
			result = passInputXmlToGetResultFromBrassRingWS(inputXml);
		}
		return result;
	}

	/**
	 * Returns result returned from Brass Ring WS for the given Input parameter.
	 *
	 * @param inputXml {@link String}
	 * @return {@link String}
	 */
	private String passInputXmlToGetResultFromBrassRingWS(String inputXml) {
		String result = "";
		final WebRouterSoap proxy = webRouter.getWebRouterSoap();
		if (!inputXml.isEmpty()) {
			/*
			 * Marshalling the XML doesn't encode for UTF-8 properly in case of
			 * '<' and '>'. Thus, to Encode these, following replacements are
			 * necessary.
			 */
			inputXml = inputXml.replace("&gt;", ">");
			inputXml = inputXml.replace("&lt;", "<");
			try {
				if (proxy != null) {
					result = proxy.route(inputXml);
				}
			} catch (final Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return result;
	}

	/**
	 * Create Input Xml For Job Results.
	 *
	 * @param clientId {@link String}
	 * @param siteId {@link String}
	 * @param country {@link String}
	 * @param location {@link String}
	 * @param service {@link String}
	 * @param specialism {@link String}
	 * @param industry {@link String}
	 * @param keyword {@link String}
	 * @param grade {@link String}
	 * @param countryCode {@link String}
	 * @param locationCode {@link String}
	 * @param serviceCode {@link String}
	 * @param keywordCode {@link String}
	 * @param industryCode {@link String}
	 * @param specialismCode {@link String}
	 * @param gradeCode {@link String}
	 * @return {@link String}
	 */
	private String createInputXmlForJobResults(final String clientId, final String siteId, final String country, final String location,
			final String service, final String specialism, final String industry, final String keyword, final String grade, final String countryCode,
			final String locationCode, final String serviceCode, final String specialismCode, final String industryCode, final String keywordCode,
			final String gradeCode) {
		requestQuestionIDs = "";
		final Questions questions = new Questions(new ArrayList<Question>());
		addQuestion(questions, countryCode, country);
		addQuestion(questions, locationCode, location);
		addQuestion(questions, serviceCode, service);
		addQuestion(questions, specialismCode, specialism);
		addQuestion(questions, industryCode, industry);
		addQuestion(questions, keywordCode, keyword);
		addQuestion(questions, gradeCode, grade);

		final Envelope envelope = getEnvelopeForGivenParameters(clientId, siteId, "", questions,"");
		return marshalInputXml(envelope);
	}

	/**
	 * Create Input Xml For Job Description.
	 *
	 * @param clientId {@link String}
	 * @param siteId {@link String}
	 * @param jobId {@link String}
	 * @param keywordCode {@link String}
	 * @return {@link String}
	 */
	@Override
	public String createInputXmlForJobDescription(final String clientId, final String siteId, final String jobId, final String keywordCode,final String jobDetailQues) {
		requestQuestionIDs = "";
		final Questions questions = new Questions(new ArrayList<Question>());
		addQuestion(questions, keywordCode, jobId);
		final Envelope envelope = getEnvelopeForGivenParameters(clientId, siteId, "YES", questions,jobDetailQues);
		return marshalInputXml(envelope);
	}

	private void addQuestion(final Questions questions, final String id, final String value) {
		if (value != null && !value.isEmpty() && !value.equals("null")) {
			final Question question = new Question(id, cdataOf(value), "ASC", "No");
			questions.getQuestions().add(question);
			requestQuestionIDs += requestQuestionIDs.isEmpty() ? id : "," + id;
		}
	}

	/**
	 * Returns the Envelope object according to passed parameters.
	 *
	 * @param clientId {@link String}
	 * @param siteId {@link String}
	 * @param jobDescriptionValue {@link String}
	 * @param questions {@link Questions}
	 *
	 * @return {@link String}
	 */
	private Envelope getEnvelopeForGivenParameters(final String clientId, final String siteId, final String jobDescriptionValue,
			final Questions questions,final String jobDetailQues) {
		final int parsedClientId = Integer.parseInt(clientId);
		final Group group = new Group(cdataOf(requestQuestionIDs));
		final List<Group> groupsList = new ArrayList<Group>();
		groupsList.add(group);
		final Groups groups = new Groups(groupsList);
		final QuestionsForANDCondition questionsForANDCondition = new QuestionsForANDCondition(groups);
		final JobDescription jobDescription = new JobDescription(jobDescriptionValue);
		final ReturnJobDetailQues returnJobDetailQues=new ReturnJobDetailQues(jobDetailQues);
		final InputString inputString = new InputString(parsedClientId, Integer.parseInt(siteId), 1, 0, "", "", "", "",
		CONST_STRING_VALUE_RETURN_JOBS_COUNT, questionsForANDCondition, jobDescription, questions,returnJobDetailQues);
		final Payload payload = new Payload(inputString);
		final PacketInfo packetInfo = new PacketInfo(1, "data");
		final Packet packet = new Packet(payload, packetInfo);
		final Unit unit = new Unit(packet, "SearchAPI");
		final Sender sender = new Sender(1234, parsedClientId);
		return new Envelope(sender, unit, "01.00");
	}

	/**
	 * Marshalls the given Envelope and returns the resulting XML as String.
	 *
	 * @param envelope {@link Envelope}
	 * @return {@link String}
	 */
	private String marshalInputXml(final Envelope envelope) {
		String inputXml = "";
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(Envelope.class);
			final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
			final File file = File.createTempFile("input_xml_temp", ".xml");
			jaxbMarshaller.marshal(envelope, file);
			final Scanner scanner = new Scanner(file);
			inputXml = scanner.useDelimiter("\\Z").next();
			scanner.close();
		} catch (final IOException ioExp) {
			logger.error(ioExp.getMessage(), ioExp);
		} catch (final JAXBException jaxbExp) {
			logger.error(jaxbExp.getMessage(), jaxbExp);
		}
		return inputXml;
	}

	/**
	 * Adds Xml's CDATA block to the given string and returns it.
	 *
	 * @param str {@link String}
	 * @return {@link String}
	 */
	private String cdataOf(final String str) {
		return "<![CDATA[" + str + "]]>";
	}

	@Override
	public String generateInputAndGetJobDescriptionFromBrassRingWS(String clientId, String siteId, String jobId,
			String keywordCode) {
		String result = "";
		if (webRouter != null) {
			final String inputXml = createInputXmlForJobDescription(clientId, siteId, jobId, keywordCode);
			result = passInputXmlToGetResultFromBrassRingWS(inputXml);
		}
		return result;
	}

	@Override
	public String createInputXmlForJobDescription(String clientId, String siteId, String jobId, String keywordCode) {
		requestQuestionIDs = "";
		final Questions questions = new Questions(new ArrayList<Question>());
		addQuestion(questions, keywordCode, jobId);
		final Envelope envelope = getEnvelopeForGivenParameters(clientId, siteId, "YES", questions,"");
		return marshalInputXml(envelope);
	}
}
