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

import java.io.IOException;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;
import javax.xml.bind.JAXBException;

import nl.ordina.bag.etl.mail.handler.MessageHandler;
import nl.ordina.bag.etl.mail.handler.TestMessageHandler;
import nl.ordina.bag.etl.processor.ProcessingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IMAPMutatiesFileLoader implements MailProcessor
{
	protected transient Log logger = LogFactory.getLog(this.getClass());
	private MessageHandler messageHandler;
  private String protocol;
  private String host;
  private int port;
  private String username;
  private String password;
  private String folderName;

  @Override
	public void processMessages()
	{
	  try
		{
			Session session = Session.getDefaultInstance(new Properties(),null);
			Store store = session.getStore(protocol);
			if (port == 0)
				store.connect(host,username,password);
			else
				store.connect(host,port,username,password);

			Folder folder = store.getFolder(folderName);
			if (folder == null)
				throw new RuntimeException("Folder " + folderName + " not found!");
			folder.open(Folder.READ_WRITE);

			try
			{
        FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN),false);
        Message messages[] = folder.search(ft);
				for (Message message : messages)
				{
					messageHandler.handle(message);
					message.setFlags(new Flags(Flags.Flag.SEEN),true);
				}
			}
			finally
			{
				folder.close(true);
				store.close();
			}
		}
		catch (MessagingException | IOException | JAXBException e)
		{
			throw new ProcessingException(e);
		}
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
	
	public void setFolderName(String folderName)
	{
		this.folderName = folderName;
	}

	public static void main(String[] args)
	{
		IMAPMutatiesFileLoader mailProcessor = new IMAPMutatiesFileLoader(); 
		mailProcessor.setMessageHandler(new TestMessageHandler());
		mailProcessor.setProtocol("imaps");
		mailProcessor.setHost("localhost");
		mailProcessor.setPort(993);
		mailProcessor.setUsername("username");
		mailProcessor.setPassword("password");
		mailProcessor.setFolderName("Inbox");
		mailProcessor.processMessages();
	}
}
