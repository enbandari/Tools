package com.piglet.codeanalyzer;

public class Result {
	public String filepath;
	public FileType type;
	public int lines;
	public int wsLines;
	public int noteLines;
	public int refLines;

	public void add(Result r) {
		this.lines += r.lines;
		this.wsLines += r.wsLines;
		this.noteLines += r.noteLines;
		this.refLines += r.refLines;
	}
	
	public static String toHeadString(){
		return "Path\tType\tLines\tWhiteSpace\tNotes\tRefs\n";
	}
	
	public String toLines(){
		return new StringBuilder()
		.append(filepath).append('\t')
		.append(type).append('\t')
		.append(lines).append('\t')
		.append(wsLines).append('\t')
		.append(noteLines).append('\t')
		.append(refLines).append('\n').toString();
	}

	public String toString() {
		return new StringBuilder()
				.append("+----------------------------------------------------------+\n")
				.append("| File:\t\t | ")
				.append(filepath)
				.append("\t\t\t|\n")
				.append("| FileType:\t\t | ")
				.append(type.name())
				.append("\t\t\t|\n")
				.append("| Lines:\t\t\t | ")
				.append(lines)
				.append("\t\t\t|\n")
				.append("| White Space Lines:\t | ")
				.append(wsLines)
				.append("\t\t\t|\n")
				.append("| Note Lines:\t\t | ")
				.append(noteLines)
				.append("\t\t\t|\n")
				.append("| Ref Lines:\t\t | ")
				.append(refLines)
				.append("\t\t\t|\n")
				.append("+----------------------------------------------------------+\n")
				.toString();
	}

	public String toSimpleString() {
		String path = filepath.length()>24? "~"+filepath.substring(filepath.length() - 20): filepath;
		if(path.length() < 8){
			path += "\t\t";
		}else if(path.length() < 16){
			path += "\t";
		}
		return new StringBuilder()
				.append("+----------------------------------------------------------+\n|  ")
				.append(path)
				.append("\t| ")
				.append(type.name())
				.append("\t| ")
				.append(lines)
				.append("\t| ")
				.append(wsLines)
				.append("\t| ")
				.append(noteLines)
				.append("\t| ")
				.append(refLines)
				.append("\t|\n")
				.append("+----------------------------------------------------------+\n")
				.toString();
	}
}
