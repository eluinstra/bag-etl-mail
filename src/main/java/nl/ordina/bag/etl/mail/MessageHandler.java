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
package nl.ordina.bag.etl.mail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXBException;

import nl.ordina.bag.etl.service.MutatiesFileService;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class MessageHandler
{
	protected transient Log logger = LogFactory.getLog(this.getClass());
	private MutatiesFileService mutatiesFileService;
	private String fromAddressRegEx;
	private String subjectRegEx;
	private String urlRegEx;
	private Pattern urlPattern;
	private Pattern filePattern;

	public void init() throws NoSuchAlgorithmException, KeyManagementException
	{
		TrustManager[] trustAllCerts = 
			new TrustManager[]{
				new X509TrustManager()
				{
					public java.security.cert.X509Certificate[] getAcceptedIssuers()
					{
						return null;
					}
		
					@Override
					public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
					{
					}
		
					@Override
					public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
					{
					}
				}
			}
		;
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    HostnameVerifier allHostsValid = new HostnameVerifier()
    {
			@Override
			public boolean verify(String hostname, SSLSession session)
			{
				return true;
			} 
    }; 
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

		urlPattern = Pattern.compile("(?im)^.*(" + urlRegEx + ").*$",Pattern.CASE_INSENSITIVE + Pattern.MULTILINE);
		filePattern = Pattern.compile("^attachment;\\s*filename=\"(.*)\"$");
	}
	
	public void handle(Message message) throws FileNotFoundException, IOException, MessagingException, JAXBException
	{
		if (message.getFrom()[0].toString().matches(fromAddressRegEx) && message.getSubject().matches(subjectRegEx))
		{
			String content = IOUtils.toString(message.getInputStream());
			String url = getURL(content);
			if (url != null)
			{
				File mutatiesFile = downloadFile(url);
				mutatiesFileService.importMutatiesFile(mutatiesFile);
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

	private File downloadFile(String uri) throws IOException
	{
		HttpURLConnection connection = null;
		try
		{
			connection = getConnection(uri);
			connection.connect();
			return handleResponse(connection);
		}
		finally
		{
			if (connection != null)
				connection.disconnect();
		}
	}

	private HttpURLConnection getConnection(String uri) throws IOException
	{
		URL url = new URL(uri);
		HttpURLConnection result = (HttpURLConnection)url.openConnection();
		result.setRequestMethod("GET");
		result.setDoOutput(true);
		return result;
	}

	private File handleResponse(HttpURLConnection connection) throws IOException
	{
		if (connection.getResponseCode() == 200)
		{
			String filename = getFilename(connection);
			return writeToFile(filename,connection.getInputStream());
		}
		else
			throw new IOException ("Could not read file from " + connection.getURL().toString() + ". Received response code " + connection.getResponseCode());
	}

	private String getFilename(HttpURLConnection connection)
	{
		String s = connection.getHeaderField("Content-Disposition");
		Matcher matcher = filePattern.matcher(s);
	  return matcher.find() ? matcher.group(1) : UUID.randomUUID().toString();
	}

	private File writeToFile(String filename, InputStream inputStream) throws IOException
	{
		File result = File.createTempFile(filename,null);
		result.deleteOnExit();
		try (FileOutputStream out = new FileOutputStream(result))
		{
			IOUtils.copy(inputStream,out);
		}
		return result;
	}

	public void setMutatiesFileService(MutatiesFileService mutatiesFileService)
	{
		this.mutatiesFileService = mutatiesFileService;
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
