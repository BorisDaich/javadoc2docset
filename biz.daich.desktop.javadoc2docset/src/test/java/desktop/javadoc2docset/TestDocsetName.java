package desktop.javadoc2docset;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import biz.daich.desktop.javadoc2docset.CoreActions;
import biz.daich.desktop.javadoc2docset.CoreActions.O;

public class TestDocsetName {
	private static final Logger l = LogManager.getLogger(TestDocsetName.class.getName());

	static void parseJarName(O o) {
		Preconditions.checkArgument(o != null);
		Preconditions.checkArgument(o.jarFile != null);
		String jarName = o.jarFile.getName();
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

		// Path p = Paths.get(jarName);
		// p.
		// check and loose the extension

	}

	public static String testParseJarName(String jarName) {
		l.debug(jarName);
		String regexp = "(\\S+)-(\\d+\\.\\d*\\.*\\d*)\\.*.*";
		Matcher matcher = Pattern.compile(regexp).matcher(jarName);
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
		l.debug(docsetName);
		return docsetName;
	}

	public static void main(String[] args) {
		String docsetNameFromJavaDocJarName = CoreActions.docsetNameFromJavaDocJarName("commons-io-2.5-javadoc.jar");
		System.out.println("docsetNameFromJavaDocJarName " + docsetNameFromJavaDocJarName);

		// String[] s = new String[] { "gson-2.8.0-javadoc.jar",
		// "hibernate-ehcache-4.3.11.Final-javadoc.jar" };
		List<String> list = Arrays.asList("gson-2.8.0-javadoc.jar", "hibernate-ehcache-4.3.11.Final-javadoc.jar");
		list.stream().forEach(TestDocsetName::testParseJarName);
		// parseJarName("");

	}

}
