#! /usr/bin/env groovy
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.util.regex.Matcher
import java.util.regex.Pattern

void printHelpAndExit() {
	println "Usage:"
	println "./rename.groovy directory [pattern1 replace1 pattern2 replace2 ...]"
	System.exit(1);
}

if (this.args.length < 1) {
	println "directory must be defined";
	printHelpAndExit();
}

def String rootDirPath = this.args[0];
def File rootDir= new File(rootDirPath);
if (!rootDir.exists() || !rootDir.isDirectory()) {
	println "directory must be exists and be directory";
	printHelpAndExit();
}

def List<PatternReplace> replacements = new ArrayList<>();
for (int i = 1; i < this.args.length; i += 2) {
	def String patternStr = this.args[i];
	def String replaceStr = "";
	if ((i + 1) < this.args.length) {
		replaceStr = this.args[i + 1];
	}
	def PatternReplace replacement = new PatternReplace();
	replacement.pattern = Pattern.compile(patternStr);
	replacement.replace = replaceStr;
	replacements.add(replacement);
}

if (replacements.isEmpty()) {
	println "warn: There are not any pattern and replace strings. Nothing to do.";
	System.exit(0);
}

def Set<String> walkedFiles = new HashSet<>();
renameRecurs(rootDir, replacements, walkedFiles);

def void renameRecurs(File file, List<PatternReplace> replacements, Set<String> walkedFiles) {
	def String canonicalPath = file.getCanonicalPath();
	if (walkedFiles.contains(canonicalPath)) {
		return;
	}

	file = renameOneFile(file, replacements);

	def String renamedCanonicalPath = file.getCanonicalPath();
	walkedFiles.add(renamedCanonicalPath);

	if (file.isDirectory()) {
		File[] list = file.listFiles();
		if (list != null && list.length > 0) {
			for (File nextFile : list) {
				renameRecurs(nextFile, replacements, walkedFiles);
			}
		}
	}
}

def File renameOneFile(File file, List<PatternReplace> replacements) {
	for (PatternReplace replacement : replacements) {
		file = renameOneFileOneReplacement(file, replacement);
	}
	return file;
}

def File renameOneFileOneReplacement(File file, PatternReplace replacement) {
	def String name = file.getName();
	def Matcher matcher = replacement.pattern.matcher(name);
	if (matcher.find()) {
		String newName = matcher.replaceAll(replacement.replace);
		if (newName != null && newName.length() > 0) {
			File newFile = new File(file.getParent(), newName);
			if (file.renameTo(newFile)) {
				file = newFile;
			}
		}
	}
	return file;
}

def class PatternReplace {
	def Pattern pattern;
	def String replace;
}
