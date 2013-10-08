/**
 * Copyright 2013 Ordina
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.ordina.bag.etl.mail.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.xml.bind.JAXBException;

import nl.ordina.bag.etl.loader.MutatiesFileLoader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MessageHandler
{
	protected transient Log logger = LogFactory.getLog(this.getClass());
	private HttpClient httpClient;
	private MutatiesFileLoader mutatiesFileLoader;
	private String fromAddressRegEx;
	private String subjectRegEx;
	private String urlRegEx;
	private Pattern urlPattern;

	public void init() throws NoSuchAlgorithmException, KeyManagementException
	{
		urlPattern = Pattern.compile("(?im)^.*(" + urlRegEx + ").*$",Pattern.CASE_INSENSITIVE + Pattern.MULTILINE);
	}
	
	public void handle(Message message) throws FileNotFoundException, IOException, MessagingException, JAXBException
	{
		if (message.getFrom()[0].toString().matches(fromAddressRegEx) && message.getSubject().matches(subjectRegEx))
		{
			String content = IOUtils.toString(message.getInputStream());
			String url = getURL(content);
			if (url != null)
			{
				File mutatiesFile = httpClient.downloadFile(url);
				mutatiesFileLoader.execute(mutatiesFile);
				//mutatiesFile.delete();
			}
			else
				logger.warn("Could not retreive url from message '" + message.getSubject() + "' [" + message.getSentDate() + "]");
		}
		else
			logger.debug("Skipping message '" + message.getSubject() + "' [" + message.getSentDate() + "]");
	}
	
	private String getURL(String content)
	{
		Matcher matcher = urlPattern.matcher(content);
	  return matcher.find() ? matcher.group(1) : null;
	}

	public void setHttpClient(HttpClient httpClient)
	{
		this.httpClient = httpClient;
	}

	public void setMutatiesFileLoader(MutatiesFileLoader mutatiesFileLoader)
	{
		this.mutatiesFileLoader = mutatiesFileLoader;
	}
	
	public void setFromAddressRegEx(String fromAddressRegEx)
	{
		this.fromAddressRegEx = fromAddressRegEx;
	}
	
	public void setSubjectRegEx(String subjectRegEx)
	{
		this.subjectRegEx = subjectRegEx;
	}
	
	public void setUrlRegEx(String urlRegEx)
	{
		this.urlRegEx = urlRegEx;
	}
	
}	
