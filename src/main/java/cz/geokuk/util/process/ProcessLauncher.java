package cz.geokuk.util.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Třída spouštějící process korektním způsobem.
 * Doporučuji si pročíst odkaz.
 * @author http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=3
 * @author polakm
 * @since 2008-06-04
 */
public class ProcessLauncher {

	private static class StreamGobbler extends Thread {
		private final InputStream is;
		private final List<ResultItem> content;

		StreamGobbler(InputStream aIs) {
			is = aIs;
			content = new ArrayList<>();
		}

		@Override
		public void run() {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String line = null;
				while ((line = br.readLine()) != null) {
					content.add(new ResultItem( System.currentTimeMillis(),line));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		private List<ResultItem> getContent() {
			return content;
		}
	}

	/**
	 * Dvojice časová známka, řádek textu
	 * @author polakm
	 */
	public static class ResultItem {

		private final long timeStamp;
		private final String text;
		ResultItem(long aTs, String aText) {

			timeStamp = aTs;
			text = aText;
		}
		public long getTimeStamp() {
			return timeStamp;
		}
		public String getText() {
			return text;
		}
	}

	/**
	 * Výsledek spuštění.
	 * @author polakm
	 */
	public static class Result {

		private final String executedCommand;
		private final List<ResultItem> error;
		private final List<ResultItem> output;
		private final int exitValue;

		Result(String aExecutedCommand, int aExitValue, List<ResultItem> aError, List<ResultItem> aOutput) {

			executedCommand = aExecutedCommand;
			exitValue = aExitValue;
			error = aError;
			output = aOutput;
		}

		private List<String> _textFromResItem(List<ResultItem> aData) {

			List<String> result = new ArrayList<>();
			if (aData != null) {
				for (ResultItem item : aData) {

					result.add(item.getText());
				}
			}
			return result;
		}

		public List<ResultItem> getErrorWithTimestamps() {
			return error;
		}

		public List<ResultItem> getOutputWithTimestamps() {
			return output;
		}

		public List<String> getError() {
			return _textFromResItem(error);
		}

		public List<String> getOutput() {
			return _textFromResItem(output);
		}

		public int getExitValue() {
			return exitValue;
		}

		public String getExecutedCommand() {
			return executedCommand;
		}

	}

	/**
	 * Spustí daný program s parametry
	 * @param aPrgFileName
	 * @param aParams
	 * @return
	 * @exception RuntimeException při libovolné chybě
	 */
	public static Result exec(String aPrgFileName, String... aParams) {

		StringBuilder cmd = new StringBuilder();
		cmd.append(aPrgFileName.trim());
		String delimiter = " ";
		for (String s : aParams) {

			if (s == null) {
				continue;
			}
			String arg = s.trim();
			if (arg.equals("")) {
				continue;
			}
			cmd.append(delimiter);
			cmd.append(arg);
		}
		try {
			Runtime rt = Runtime.getRuntime();

			String command = cmd.toString();
			Process proc = rt.exec(command);

			StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream());
			StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream());

			errorGobbler.start();
			outputGobbler.start();

			int exitVal = proc.waitFor();

			Result r = new Result(command, exitVal, errorGobbler.getContent(), outputGobbler.getContent());
			return r;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
