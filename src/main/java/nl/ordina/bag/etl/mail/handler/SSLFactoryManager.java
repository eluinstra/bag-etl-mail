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

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLFactoryManager
{
	private SSLSocketFactory sslSocketFactory;
	private HostnameVerifier hostnameVerifier;

	public SSLFactoryManager() throws NoSuchAlgorithmException, KeyManagementException
	{
		TrustManager[] trustManagers = 
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

		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null,trustManagers,null);
		sslSocketFactory = sslContext.getSocketFactory();

    hostnameVerifier = new HostnameVerifier()
    {
			@Override
			public boolean verify(String hostname, SSLSession session)
			{
				return true;
			} 
    }; 
	}

	public SSLSocketFactory getSslSocketFactory()
	{
		return sslSocketFactory;
	}
	
	public HostnameVerifier getHostnameVerifier()
	{
		return hostnameVerifier;
	}

}
