/**
 * Copyright 2011 Clockwork
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
package nl.ordina.bag.etl.mail.scheduler;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class JobScheduler
{
  protected transient Log logger = LogFactory.getLog(getClass());
	private Timer timer;
	private boolean enabled;
	private long delay;
	private long period;

	public void init()
	{
		if (enabled)
		{
			Timer timer = new Timer();
			timer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					execute();
				}
			},
			delay,
			period);
		}
	}
	
	public abstract void execute();
	
	public void destroy()
	{
		if (timer != null)
			timer.cancel();
	}
	
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public void setDelay(long delay)
	{
		this.delay = delay;
	}
	
	public void setPeriod(long period)
	{
		this.period = period;
	}
	
}
