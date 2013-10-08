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
package nl.ordina.bag.etl.mail.loader;

import nl.ordina.bag.etl.mail.handler.MessageHandler;

import org.springframework.beans.factory.FactoryBean;

public class MutatiesFileLoaderFactory implements FactoryBean<MailProcessor>
{
	private MessageHandler messageHandler;
  private String protocol;
  private String host;
  private int port;
  private String username;
  private String password;
	private String folder;
	private String backupPath;

	@Override
	public MailProcessor getObject() throws Exception
	{
		if ("pop3".equals(protocol) || "pop3s".equals(protocol))
		{
			POP3MutatiesFileLoader mailProcessor = new POP3MutatiesFileLoader(); 
			mailProcessor.setMessageHandler(messageHandler);
			mailProcessor.setProtocol(protocol);
			mailProcessor.setHost(host);
			mailProcessor.setPort(port);
			mailProcessor.setUsername(username);
			mailProcessor.setPassword(password);
			mailProcessor.setBackupPath(backupPath);
			return mailProcessor;
		}
		else if ("imap".equals(protocol) || "imaps".equals(protocol))
		{
			IMAPMutatiesFileLoader mailProcessor = new IMAPMutatiesFileLoader(); 
			mailProcessor.setMessageHandler(messageHandler);
			mailProcessor.setProtocol(protocol);
			mailProcessor.setHost(host);
			mailProcessor.setPort(port);
			mailProcessor.setUsername(username);
			mailProcessor.setPassword(password);
			mailProcessor.setFolderName(folder);
			return mailProcessor;
		}
		throw new RuntimeException("Mail Protocol " + protocol + " not recognized!");
	}

	@Override
	public Class<?> getObjectType()
	{
		return MailProcessor.class;
	}

	@Override
	public boolean isSingleton()
	{
		return true;
	}
	
	public void setMessageHandler(MessageHandler messageHandler)
	{
		this.messageHandler = messageHandler;
	}

	public void setProtocol(String protocol)
	{
		this.protocol = protocol;
	}
	
	public void setHost(String host)
	{
		this.host = host;
	}
	
	public void setPort(int port)
	{
		this.port = port;
	}
	
	public void setUsername(String username)
	{
		this.username = username;
	}
	
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	public void setFolder(String folder)
	{
		this.folder = folder;
	}
	
	public void setBackupPath(String backupPath)
	{
		this.backupPath = backupPath;
	}

}
