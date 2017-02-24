package biz.daich.desktop.javadoc2docset;

import static biz.daich.desktop.javadoc2docset.CoreActions.go;
import static biz.daich.desktop.javadoc2docset.CoreActions.unpackDefaultIcon;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import biz.daich.desktop.javadoc2docset.CoreActions.O;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

public class MainJavadoc2docset
{
	private static final Logger l = LogManager.getLogger(MainJavadoc2docset.class.getName());

	/**
	 * plan :
	 * get from command line path of the tmpdir , output dir , javadoc Jar, icon
	 * if no icon - use default any.png
	 * out dir - this dir
	 * tmp dir - system tmp
	 *
	 * javadoc jar
	 * resolve to full path
	 * check that file exists - or error
	 * get javadoc.jar filename
	 * check ends javadoc.jar - warning
	 * get the Docset Name with version
	 *
	 * @throws IOException
	 *
	 *
	 */

	public static void main(String[] args) throws IOException
	{
		MainJavadoc2docset x = new MainJavadoc2docset();
		O o = x.cmdOptions(args);
		go(o);

	}

	protected O cmdOptions(String[] args)
	{
		final OptionParser parser = new OptionParser();
		final OptionSpec<File> javadocJar = parser.accepts("javadocJar", "Javadoc Jar file to bundle in the docset.").withRequiredArg().ofType(File.class).required();

		final OptionSpec<String> docsetName = parser.accepts("name", "Name of the generated docset").withRequiredArg().ofType(String.class);
		final OptionSpec<String> docsetNamePrefix = parser.accepts("prefix", "prefix for the Name of the generated docset").withRequiredArg().ofType(String.class);
		final OptionSpec<String> displayName = parser.accepts("displayName", "Name to show for the docset in Dash. Defaults to value of 'name' if not specified.").withRequiredArg().ofType(
																																															String.class);

		final OptionSpec<File> outDir = parser.accepts("outDir", "Directory where the docset will be created.").withRequiredArg().ofType(File.class).defaultsTo(FileUtils.getFile("."));
		//		FileUtils.getFile(".")
		final OptionSpec<File> tmpDir = parser.accepts("tmpDir", "Where the Jar will be unpacked for processing").withRequiredArg().ofType(File.class).defaultsTo(FileUtils.getFile("."));

		final OptionSpec<File> iconFile = parser
												.accepts("icon", "Icon file to use for the docset. default icon will be used if not specified.")//
													.withRequiredArg()
													.ofType(File.class)
													.describedAs("32x32 PNG");

		final OptionSpec<Void> keepTmp = parser.accepts("keepTmp", "Do not delete the unpacked Jar");
		final OptionSpec<Void> verbose = parser.accepts("verbose", "Show more information");
		final OptionSpec<Void> help = parser.acceptsAll(Arrays.asList("h", "?"), "Show help").forHelp();

		final OptionSet options;
		try
		{
			options = parser.parse(args);
			if (options.has(help))
			{
				usage(parser);
				return null;
			}
			else
			{
				//////// do the thing
				O o = new O();
				o.jarFile = options.valueOf(javadocJar);
				o.prefix = options.valueOf(docsetNamePrefix);
				o.docsetName = options.valueOf(docsetName);
				o.displayName = options.valueOf(displayName);

				o.outputLocation = options.valueOf(outDir).toPath(); // it has default to the current dir
				o.tmpDir = options.valueOf(tmpDir).toPath();
				o.iconFile = options.has(iconFile) ? options.valueOf(iconFile) : unpackDefaultIcon(o.tmpDir);

				o.keepTmp = options.has(keepTmp);
				o.verbose = options.has(verbose);
				return o;
			}
		}
		catch (Exception e)
		{
			l.error("cmdOptions(String[])", e); //$NON-NLS-1$

			usage(parser);
			return null;
		}
	}

	private static void usage(OptionParser parser)
	{
		try
		{
			parser.printHelpOn(System.out);
		}
		catch (IOException e)
		{
			l.error("usage(OptionParser)", e); //$NON-NLS-1$
		}
	}

}
