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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;

public class HttpClient
{
	private SSLFactoryManager sslFactoryManager = new SSLFactoryManager();
	private Pattern filePattern;

	public HttpClient() throws NoSuchAlgorithmException, KeyManagementException
	{
		filePattern = Pattern.compile("^attachment;\\s*filename=\"(.*)\"$");
	}
	
	public File downloadFile(String uri) throws IOException
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
		if (result instanceof HttpsURLConnection)
		{
			((HttpsURLConnection)result).setSSLSocketFactory(sslFactoryManager.getSslSocketFactory());
			((HttpsURLConnection)result).setHostnameVerifier(sslFactoryManager.getHostnameVerifier());
		}
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

}
