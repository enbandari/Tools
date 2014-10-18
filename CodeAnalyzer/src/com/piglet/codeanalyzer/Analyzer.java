package com.piglet.codeanalyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Analyzer {

	public static BlockingQueue<Result> messageQueue;
	public static ExecutorService worker;
	public static CountDownLatch countDown;
	public static int fileCount = 0;
	public static HashMap<String, Result> total;
	public static String CURRENT_DIR;
	public static int curdirLen;

	public static void main(String[] args) throws IOException {
		total = new HashMap<>();
		messageQueue = new LinkedBlockingQueue<>();
		worker = Executors.newCachedThreadPool();

		File dir = new File(".");
		CURRENT_DIR = dir.getAbsolutePath();
		curdirLen = CURRENT_DIR.length() + 1;
		// 遍历文件
		listFiles(dir);
		// 输出结果
//		printReport(new PrintWriter(System.out));
		PrintWriter writer = new PrintWriter(new File("report"
				+ System.currentTimeMillis()));
		printReport(writer);
		writer.close();
	}

	public static void listFiles(File dir) {
		// System.out.println(dir.getName());
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File f : files) {
				listFiles(f);
			}
		} else {
			// analyze
			fileCount++;
			worker.submit(new AnalyzeTask(dir));
		}
	}

	public static void printReport(PrintWriter writer) {
		writer.write(Result.toHeadString());
		writer.println("********************************************************************");
		while (fileCount-- > 0) {
			Result r;
			try {
				r = messageQueue.take();
				if (r.type != FileType.UNKNOWN) {
					writer.print(r.toLines());
					Result result = total.get(r.type.name());
					if (result == null) {
						r.filepath = "Total";
						total.put(r.type.name(), r);
					} else {
						result.add(r);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		writer.println("********************************************************************");
		for (Map.Entry<String, Result> entry : total.entrySet()) {
			writer.print(entry.getValue().toLines());
		}
		writer.flush();
		worker.shutdown();
	}

	static class ReportTask implements Runnable {
		private PrintWriter writer;

		public ReportTask(PrintWriter writer) {
			this.writer = writer;
		}

		@Override
		public void run() {
			while (true) {
				Result r = messageQueue.poll();
				// write..
				if (r.type != FileType.UNKNOWN) {
					writer.print(r);
					Result result = total.get(r.type.name());
					if (result == null) {
						r.filepath = "Total";
						total.put(r.type.name(), r);
					} else {
						result.add(r);
					}
				}
			}

		}

	}

	static class AnalyzeTask implements Runnable {
		private File file;

		public AnalyzeTask(File file) {
			this.file = file;
		}

		@Override
		public void run() {
			Result r = new Result();
			r.filepath = file.getAbsolutePath();
			r.filepath = r.filepath.substring(curdirLen);
			r.type = FileType.getFileType(file.getName());
			if (r.type == FileType.UNKNOWN) {
				// ....
				r.lines = 0;

			} else {
				r.lines = 0;
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new FileReader(file));
					String content = null;
					boolean isNote = false;
					boolean isRef = false;
					while ((content = reader.readLine()) != null) {
						if (content.matches("\\s*")) {
							r.wsLines++;
						} else if (isNote) {
							if (NoteSpeci.isMultiEnd(content)) {
								isNote = false;
							}
							r.noteLines++;

						} else if (isRef) {
							if (NoteSpeci.isRefEnd(content)) {
								isRef = false;
							}
							r.refLines++;
						} else {
							if (NoteSpeci.isSingleLine(content)) {
								r.noteLines++;
							} else if (NoteSpeci.isMultiBegin(content)) {
								r.noteLines++;
								isNote = true;
							} else if (NoteSpeci.isRefBegin(content)) {
								r.refLines++;
								isRef = true;
							} else {
								r.lines++;
							}
						}
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
			messageQueue.offer(r);
		}
	}
}
