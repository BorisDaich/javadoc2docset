package biz.daich.desktop.javadoc2docset;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Stopwatch;

import biz.daich.desktop.javadoc2docset.CoreActions.O;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DocsetTask extends O implements Callable<DocsetTask>//, Runnable
{
	private static final Logger		l				= LogManager.getLogger(DocsetTask.class.getName());

	protected Future<DocsetTask>	future			= null;

	protected Throwable				lastException	= null;
	protected String				took			= null;

	public boolean isDone()
	{
		return (future != null && future.isDone());
	}

	/**
	 * NOTE THAT this method does not do the preparation tasks!!!
	 */
	@Override
	public DocsetTask call()
	{
		l.info("  >>>>  Started [" + docsetName + "]");
		Stopwatch stopwatch = Stopwatch.createStarted();
		try
		{
			CoreActions.run(this);
			stopwatch.stop();
			took = stopwatch.toString();
			l.info("  <<<<  Done [" + docsetName + "] took: " + stopwatch);
			return this;
		}
		catch (IOException e)
		{
			l.error("call()", e); //$NON-NLS-1$
		}
		return this;
	}

	//	@Override
	//	public void run()
	//	{
	//		call();
	//	}

}
