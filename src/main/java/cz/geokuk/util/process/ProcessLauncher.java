package cz.geokuk.util.process;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Třída spouštějící process korektním způsobem. Doporučuji si pročíst odkaz.
 *
 * @author http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=3
 * @author polakm
 * @since 2008-06-04
 */
public class ProcessLauncher {

	private static class StreamGobbler extends Thread {
		private final InputStream		is;
		private final List<ResultItem>	content;

		StreamGobbler(final InputStream aIs) {
			is = aIs;
			content = new ArrayList<>();
		}

		@Override
		public void run() {
			try {
				final BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String line = null;
				while ((line = br.readLine()) != null) {
					content.add(new ResultItem(System.currentTimeMillis(), line));
				}
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		private List<ResultItem> getContent() {
			return content;
		}
	}

	/**
	 * Dvojice časová známka, řádek textu
	 *
	 * @author polakm
	 */
	public static class ResultItem {

		private final long		timeStamp;
		private final String	text;

		ResultItem(final long aTs, final String aText) {

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
	 *
	 * @author polakm
	 */
	public static class Result {

		private final String			executedCommand;
		private final List<ResultItem>	error;
		private final List<ResultItem>	output;
		private final int				exitValue;

		Result(final String aExecutedCommand, final int aExitValue, final List<ResultItem> aError, final List<ResultItem> aOutput) {

			executedCommand = aExecutedCommand;
			exitValue = aExitValue;
			error = aError;
			output = aOutput;
		}

		private List<String> _textFromResItem(final List<ResultItem> aData) {

			final List<String> result = new ArrayList<>();
			if (aData != null) {
				for (final ResultItem item : aData) {

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
	 *
	 * @param aPrgFileName
	 * @param aParams
	 * @return
	 * @exception RuntimeException
	 *                při libovolné chybě
	 */
	public static Result exec(final String aPrgFileName, final String... aParams) {

		final StringBuilder cmd = new StringBuilder();
		cmd.append(aPrgFileName.trim());
		final String delimiter = " ";
		for (final String s : aParams) {

			if (s == null) {
				continue;
			}
			final String arg = s.trim();
			if (arg.equals("")) {
				continue;
			}
			cmd.append(delimiter);
			cmd.append(arg);
		}
		try {
			final Runtime rt = Runtime.getRuntime();

			final String command = cmd.toString();
			final Process proc = rt.exec(command);

			final StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream());
			final StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream());

			errorGobbler.start();
			outputGobbler.start();

			final int exitVal = proc.waitFor();

			final Result r = new Result(command, exitVal, errorGobbler.getContent(), outputGobbler.getContent());
			return r;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}

	}

}
