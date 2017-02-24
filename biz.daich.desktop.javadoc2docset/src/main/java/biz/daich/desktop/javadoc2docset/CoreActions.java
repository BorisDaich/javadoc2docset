package biz.daich.desktop.javadoc2docset;

import static com.megatome.j2d.util.LogUtility.setVerbose;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import com.megatome.j2d.DocsetCreator;
import com.megatome.j2d.exception.BuilderException;

import lombok.Data;

public class CoreActions {
	private static final Logger l = LogManager.getLogger(CoreActions.class.getName());

	public static void unpackJar(File destDir, File jarFile) throws IOException {

		final Stopwatch jarUnpack = Stopwatch.createStarted();

		try (JarFile jar = new JarFile(jarFile)) {

			// fist get all directories,
			// then make those directory on the destination Path
			for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
				JarEntry entry = enums.nextElement();

				String fileName = destDir + File.separator + entry.getName();
				File f = new File(fileName);

				if (fileName.endsWith("/")) {
					f.mkdirs();
				}

			}

			// now create all files
			for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
				JarEntry entry = (JarEntry) enums.nextElement();

				String fileName = destDir + File.separator + entry.getName();
				File f = new File(fileName);

				if (!fileName.endsWith("/")) {
					try (InputStream is = jar.getInputStream(entry)) {
						Files.copy(is, f.toPath(), StandardCopyOption.REPLACE_EXISTING);
					}
					// FileOutputStream fos = new FileOutputStream(f);
					//
					// // write contents of 'is' to 'fos'
					// while (is.available() > 0)
					// {
					// fos.write(is.read());
					// }
					//
					// fos.close();
					// is.close();
				}
			}
		}
		l.info("Jar " + jarFile.getAbsolutePath() + " unpacked to " + destDir.getAbsolutePath() + " took " + jarUnpack);
	}

	public static void unpackJar_old(File destDir, File jarFile) throws IOException {
		final Stopwatch jarUnpack = Stopwatch.createStarted();
		try (JarFile jar = new JarFile(jarFile)) {
			Enumeration<JarEntry> enumEntries = jar.entries();
			while (enumEntries.hasMoreElements()) {
				JarEntry file = enumEntries.nextElement();
				File f = new File(destDir, file.getName());
				if (file.isDirectory()) { // if its a directory, create it
					f.mkdir();
					continue;
				}
				try (InputStream is = jar.getInputStream(file)) {
					Files.copy(is, f.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
		l.info("Jar " + jarFile.getAbsolutePath() + " unpacked to " + destDir.getAbsolutePath() + " took " + jarUnpack);
	}

	@Data
	public static class O {
		/**
		 * this is the Jar File of the JavaDoc to start with
		 */
		public File jarFile;
		/**
		 * workDir - this is where the JarFile will be unpacked
		 */
		public Path tmpDir;
		/**
		 * this will be the name of the Docset first param of the
		 * DocsetCreator.Builder()
		 */
		public String docsetName;
		/**
		 * this is the folder created by javadoc jar file unpacking second param
		 * of the DocsetCreator.Builder()
		 */
		public Path javadocRoot;
		/**
		 * this is the outputDirectory of the outputDirectory
		 * DocsetCreator.builder
		 */
		public Path outputLocation;

		public String displayName;
		/**
		 * icon file for the the docset - optional will be used default
		 */
		public File iconFile;

		/**
		 * give more log output about the run
		 */
		public boolean verbose = true;
		/**
		 * if true do not delete the unpacked JAR
		 **/
		public boolean keepTmp = false;

		/**
		 * any string here will be added as a prefix with a single space to a
		 * displayName and docsetName
		 */
		public String prefix = null;

	}

	// public static File defaultIconFile()
	// {
	// File iconFile = null;
	// try
	// {
	// URL iconResourceUrl = Resources.getResource("any.png");
	// // Turn the resource into a File object
	// iconFile = new File(iconResourceUrl.toURI());
	// }
	// catch (URISyntaxException e)
	// {
	// l.error("conversion of icon resource to file failed", e);
	// }
	// return iconFile;
	// }

	public static File unpackDefaultIcon(Path tmpDirPath) throws IOException {
		Preconditions.checkArgument(tmpDirPath != null);
		File tmpDirFile = tmpDirPath.toFile();
		Preconditions.checkArgument(tmpDirFile.exists());
		File iconFile = new File(tmpDirFile, "icon.png");
		if (!iconFile.exists()) {
			URL resourceUrl = Resources.getResource("any.png");
			FileUtils.copyURLToFile(resourceUrl, iconFile);
		}
		l.info("Default icon unpacked to " + iconFile.getAbsolutePath());
		return iconFile;

	}

	public static void defaultIconFile(O o) throws IOException {
		if (o.iconFile != null && o.iconFile.exists() && o.iconFile.isFile()) //
			return;
		Preconditions.checkArgument(o.tmpDir != null);
		URL resourceUrl = Resources.getResource("any.png");
		File iconFile = new File(o.tmpDir.toFile(), "icon.png");
		FileUtils.copyURLToFile(resourceUrl, iconFile);
		o.iconFile = iconFile;

	}

	public static void buildDocset(O o) {
		Stopwatch stopwatch = Stopwatch.createStarted();
		Preconditions.checkArgument(!Strings.isNullOrEmpty(o.docsetName));
		Preconditions.checkArgument(!Strings.isNullOrEmpty(o.displayName));
		Preconditions.checkArgument(o.iconFile != null);
		Preconditions.checkArgument(o.iconFile.exists());
		Preconditions.checkArgument(o.iconFile.isFile());

		Preconditions.checkArgument(o.outputLocation != null);

		final File outputDirectory = o.outputLocation.toFile();
		Preconditions.checkArgument(outputDirectory != null);
		Preconditions.checkArgument(outputDirectory.exists());
		Preconditions.checkArgument(outputDirectory.isDirectory());

		setVerbose(o.verbose);

		final DocsetCreator.Builder builder = new DocsetCreator.Builder(o.docsetName, o.javadocRoot.toFile())
				.displayName(o.displayName)
				// .displayName(o.keyword)
				.iconFile(o.iconFile).outputDirectory(outputDirectory);
		final DocsetCreator docsetCreator = builder.build();
		try {
			docsetCreator.makeDocset();
			l.info("DONE -> [" + o.docsetName + "] at " + outputDirectory.getAbsolutePath() + "  Took: " + stopwatch);
		} catch (BuilderException e) {
			l.error("Failed to create docset: {}", e); //$NON-NLS-1$
		}
	}

	// name something like this hibernate-ehcache-4.3.11.Final-javadoc.jar
	public static String docsetNameFromJavaDocJarName(String jarName) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(jarName));

		final String suffix = ".jar";
		final String javadoc = "-javadoc";
		Preconditions.checkArgument(!Strings.isNullOrEmpty(jarName), "Name must be not null or empty");

		Preconditions.checkArgument(jarName.endsWith(javadoc + suffix), "Name does not end with " + javadoc + suffix);
		jarName = jarName.substring(0, jarName.length() - (javadoc + suffix).length());
		l.debug(jarName);
		String regexp = "(\\S+)-(\\d+\\.\\d*\\.*\\d*)\\.*.*";
		Pattern pattern = Pattern.compile(regexp);
		Matcher matcher = pattern.matcher(jarName);
		boolean isMatch = matcher.matches();
		l.debug(isMatch);
		String match = matcher.group(0);
		l.debug(match);
		String packageName = matcher.group(1);
		String version = matcher.group(2);
		packageName = packageName.replaceAll("-", " ");
		packageName = WordUtils.capitalize(packageName);

		l.debug("jarName >>" + jarName + "     ----   packageName >> " + packageName + " ----  version >>" + version);

		String docsetName = packageName + " " + version;
		l.debug("DocsetName = " + docsetName);
		return docsetName;
	}

	/**
	 * set all values to defaults if not arrive from the input
	 */
	public static void validateInputAndSetDefaults(O o) {
		Preconditions.checkArgument(o.jarFile != null);
		Preconditions.checkArgument(o.jarFile.exists());
		Preconditions.checkArgument(o.jarFile.isFile());
		Preconditions.checkArgument(!Strings.isNullOrEmpty(o.jarFile.getName()));

		final String docsetNameFromJavaDocJarName = docsetNameFromJavaDocJarName(o.jarFile.getName());

		o.docsetName = Strings.isNullOrEmpty(o.docsetName) ? docsetNameFromJavaDocJarName : o.docsetName;
		o.displayName = Strings.isNullOrEmpty(o.displayName) ? o.docsetName : o.displayName;

		Preconditions.checkArgument(!Strings.isNullOrEmpty(o.docsetName));
		Preconditions.checkArgument(!Strings.isNullOrEmpty(o.displayName));

		if (!Strings.isNullOrEmpty(o.prefix)) {
			final String p = o.prefix.trim();
			if (!o.docsetName.startsWith(p)) {
				o.docsetName = p + " " + o.docsetName;
			}
			if (!o.displayName.startsWith(p)) {
				o.displayName = p + " " + o.displayName;
			}
		}
		Preconditions.checkArgument(o.outputLocation != null);
		Preconditions.checkArgument(o.tmpDir != null);
		l.info("Docset [" + o.displayName + "] validated and defaults set");
	}

	/**
	 * create required dirs unpack the default icon
	 *
	 * @throws IOException
	 */
	public static void prepareWorkArea(O o) throws IOException {
		Files.createDirectories(o.tmpDir);
		Preconditions.checkArgument(o.tmpDir.toFile().exists());
		Preconditions.checkArgument(o.tmpDir.toFile().isDirectory());

		String jarFileStr = o.jarFile.getName();
		jarFileStr = jarFileStr.substring(0, jarFileStr.length() - ".jar".length());
		o.javadocRoot = o.tmpDir.resolve(jarFileStr);
		Files.deleteIfExists(o.javadocRoot);
		Files.createDirectories(o.javadocRoot);

		Files.createDirectories(o.outputLocation);

		if (o.iconFile == null || !o.iconFile.exists() || !o.iconFile.isFile()) {
			final File defaultIcon = unpackDefaultIcon(o.tmpDir);
			o.iconFile = defaultIcon;
		}
		l.info("Work Area: " + o.tmpDir + " JavadocRoot: " + o.javadocRoot);
	}

	/**
	 * main flow: check & create folders unpack the Jar create docset clean
	 * unpacked jar done
	 *
	 * @throws IOException
	 */
	public static void go(O o) throws IOException {
		Stopwatch stopwatch = Stopwatch.createStarted();
		l.info("==================  STARTING >>  " + o.jarFile.getAbsolutePath());
		validateInputAndSetDefaults(o);
		prepareWorkArea(o);
		run(o);
		l.info("==================  DONE >>  " + o.jarFile.getAbsolutePath() + "  Took: " + stopwatch);
	}

	static public void run(O o) throws IOException {
		l.debug("O = " + o.toString());
		// unpack the jar
		try {
			final String tmpDirStr = o.javadocRoot.toRealPath().toString();
			unpackJar(o.javadocRoot.toFile(), o.jarFile);
			l.debug("Javadoc Jar " + o.jarFile.getAbsolutePath() + " unplacked to " + tmpDirStr);
			buildDocset(o);
			l.debug("Docset [" + o.docsetName + "] created at " + o.outputLocation.toRealPath().toString());
		} catch (IOException e) {
			l.error("FAILED CoreActions.run() " + o.javadocRoot.toAbsolutePath() + " REASON: " + e.getMessage());
			throw e;
		}
		// delete the unpacked Jar
		finally {
			if (!o.keepTmp) {
				try {
					final File javadocRootDirectory = o.javadocRoot.toFile();
					if (javadocRootDirectory.exists()) {
						FileUtils.deleteDirectory(javadocRootDirectory);
						l.debug("Tmp folder " + o.javadocRoot.toAbsolutePath() + " deleted");
					}
				} catch (IOException e) {
					l.error("FAILED to delete the " + o.javadocRoot.toAbsolutePath());
					throw e;
				}
			} else {
				l.debug("Tmp folder " + o.javadocRoot.toAbsolutePath() + " not deleted");
			}
		}
	}

}
