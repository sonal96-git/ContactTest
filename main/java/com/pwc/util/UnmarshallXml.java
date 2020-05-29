package com.pwc.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pwc.model.xml.response.Envelope;
import com.pwc.model.xml.response.Job;

/**
 * Unmarshalls the given XML and returns a list of Jobs.
 */
public class UnmarshallXml {
    private final static Logger logger = LoggerFactory.getLogger(UnmarshallXml.class);

    /**
     * Unmarshalls the given XML and returns a list of Jobs.
     *
     * @param responseXml {@link String}
     * @return {@link List}
     */
    public static List<Job> unmarshallXml(final String responseXml) {
	List<Job> listOfJobs = null;
	try {
	    final File tempResponseXml = File.createTempFile("response", ".xml");
	    final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriterWithEncoding(tempResponseXml, StandardCharsets.UTF_8.name()));
	    bufferedWriter.write(responseXml);
	    bufferedWriter.close();
	    final JAXBContext jaxbContext = JAXBContext.newInstance(Envelope.class);
	    final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	    final Envelope envelope = (Envelope) jaxbUnmarshaller.unmarshal(tempResponseXml);
	    listOfJobs = envelope.getUnit().getPacket().getPayload().getResultSet().getJobs().getJobs();

	} catch (final IOException ioExp) {
	    logger.error(ioExp.getMessage(), ioExp);
	} catch (final JAXBException jaxbExp) {
	    logger.error(jaxbExp.getMessage(), jaxbExp);
	}
	return listOfJobs;
    }
}
