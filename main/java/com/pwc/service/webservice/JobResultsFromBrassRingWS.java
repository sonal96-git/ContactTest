/*
 * 
 */
package com.pwc.service.webservice;

public interface JobResultsFromBrassRingWS {

	/**
	 * Creates & Sends Input Xml based on passed parameters to Brass Ring Web
	 * Service and returns the Response Xml containg Job Results.
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
	public String generateInputAndGetJobResultsFromBrassRingWS(final String clientId, final String siteId, final String country,
			final String location, final String service, final String specialism, final String industry, final String keyword, final String grade,
			final String countryCode, final String locationCode, final String serviceCode, String specialismCode, String industryCode,
			String keywordCode, String gradeCode);

	public String generateInputAndGetJobResultsFromBrassRingWS(final String clientId, final String siteId, final String country,
			final String location, final String service, final String specialism, final String keyword, final String localregion,
			final String applicationdeadline, final String entryroute, final String entryroutegroup, final String intakeyear,
			final String programmetype, final String startseason, final String countryCode, final String locationCode, final String serviceCode,
			final String specialismCode, final String keywordCode, final String localregionCode, final String applicationdeadlineCode,
			final String entryrouteCode, final String entryroutegroupCode, final String intakeyearCode, final String programmetypeCode,
			final String startseasonCode);

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
	public String generateInputAndGetJobDescriptionFromBrassRingWS(String clientId, String siteId, String jobId, String keywordCode);

	/**
	 * Creates & Sends Input Xml based on passed parameters to Brass Ring Web
	 * Service and returns the Response Xml containing Job Description.
	 * Overloaded for jobDetailQues parameter
	 * 
	 * @param clientId
	 * @param siteId
	 * @param jobId
	 * @param keywordCode
	 * @param jobDetailQues
	 * @return
	 */
	public String generateInputAndGetJobDescriptionFromBrassRingWS(String clientId, String siteId, String jobId, String keywordCode,String jobDetailQues);

	/**
	 * Create Input Xml For Job Description.
	 *
	 * @param clientId {@link String}
	 * @param siteId {@link String}
	 * @param jobId {@link String}
	 * @param keywordCode {@link String}
	 * @return {@link String}
	 */
	public String createInputXmlForJobDescription(final String clientId, final String siteId, final String jobId, final String keywordCode);

	/**
	 * Create Input Xml For Job Description.
	 * Overloaded for jobDetailQues parameter
	 *
	 * @param clientId {@link String}
	 * @param siteId {@link String}
	 * @param jobId {@link String}
	 * @param keywordCode {@link String}
	 * @return {@link String}
	 */
	public String createInputXmlForJobDescription(final String clientId, final String siteId, final String jobId, final String keywordCode,final String jobDetailQues);

}
