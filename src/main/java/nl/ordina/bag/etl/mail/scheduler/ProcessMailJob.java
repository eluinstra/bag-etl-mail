package nl.ordina.bag.etl.mail.scheduler;

import nl.ordina.bag.etl.loader.MutatiesLoader;
import nl.ordina.bag.etl.mail.loader.MailProcessor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProcessMailJob extends JobScheduler
{
	protected transient Log logger = LogFactory.getLog(this.getClass());
	private MailProcessor mutatiesFileLoader;
	private MutatiesLoader mutatiesLoader;

	@Override
	public void execute()
	{
		logger.info("ProcessMailJob started");
		mutatiesFileLoader.processMessages();
		mutatiesLoader.execute();
		logger.info("ProcessMailJob finished");
	}

	public void setMutatiesFileLoader(MailProcessor mutatiesFileLoader)
	{
		this.mutatiesFileLoader = mutatiesFileLoader;
	}
	
	public void setMutatiesLoader(MutatiesLoader mutatiesLoader)
	{
		this.mutatiesLoader = mutatiesLoader;
	}
}
