package com.echsylon.optimus;

import android.support.annotation.NonNull;
import android.util.Log;

import com.annimon.stream.Stream;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * This class offers a simple interface to the native JQ library.
 */
public class Optimus {
    public static final int SUCCESS = 0;
    public static final int ERROR_UNKNOWN = -1;

    private static final String LOG_TAG = Optimus.class.getName();
    private static final String INPUT_FILE_NAME = "3e2c462526a80674b459b14431415eaf798cb09aa3ef27cf7b0f82ce419bf9bb";
    private static final String OUTPUT_FILE_NAME = "9a1cc60ab308107d6b991479389014a943374066b92e1ecb6dc48682caa76180";
    private static final String FILTER_FILE_NAME = "bb6d233fa757f8fab0f36993f3ee666d81151b47984edddbee98413d507bce65";


    static {
        System.loadLibrary("optimus-lib");
    }

    native int transformJson(String inputFile, String filterFile, String outputFile);


    /**
     * Transforms the given JSON structure to something else, as described by
     * the provided JQ syntax filter.
     * <p>
     * This method will operate on the calling thread and is not guaranteed to
     * terminate in a timely manner. The caller is responsible for any thread
     * management.
     * <p>
     * This method will NOT close any provided streams.
     *
     * @param workingDirectory The temporary cache directory.
     * @param inputStream      The input stream to read the transformation input
     *                         JSON.
     * @param filterStream     The input stream to read the JQ transformation
     *                         blueprint from.
     * @param outputStream     The output stream to write the produced JSON to.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored") // ignore file delete lint
    public int transformJson(@NonNull final File workingDirectory,
                             @NonNull final InputStream inputStream,
                             @NonNull final InputStream filterStream,
                             @NonNull final OutputStream outputStream) {

        int status = ERROR_UNKNOWN;
        File inputFile = new File(workingDirectory, INPUT_FILE_NAME);
        File filterFile = new File(workingDirectory, FILTER_FILE_NAME);
        File outputFile = new File(workingDirectory, OUTPUT_FILE_NAME);

        try {
            writeControlDataToFile(inputStream, filterStream, inputFile, filterFile);
            status = performTransformation(filterFile, inputFile, outputFile);
            if (status == SUCCESS)
                copyResultToStream(outputFile, outputStream);
        } catch (IOException e) {
            Log.d(LOG_TAG, "Couldn't transform json: ", e);
            status = ERROR_UNKNOWN;
        } finally {
            inputFile.delete();
            outputFile.delete();
            filterFile.delete();
        }

        return status;
    }

    /**
     * Writes the JQ input data to temporary files. Since this data may be of
     * considerable size we don't want to push it over a JNI binding. It also
     * seems that JQ prefers to operate on files.
     *
     * @param inputStream  The input JSON data source stream.
     * @param filterStream The JQ transformation filter stream.
     * @param inputFile    The file to write the input JSON to.
     * @param filterFile   The file to write the control filter to.
     * @throws IOException If the file I/O would fail for any reason.
     */
    private void writeControlDataToFile(@NonNull final InputStream inputStream,
                                        @NonNull final InputStream filterStream,
                                        @NonNull final File inputFile,
                                        @NonNull final File filterFile) throws IOException {

        Source inputSource = null;
        Source filterSource = null;
        BufferedSink inputSink = null;
        BufferedSink filterSink = null;

        try {
            inputSource = Okio.source(inputStream);
            inputSink = Okio.buffer(Okio.sink(inputFile));
            inputSink.writeAll(inputSource);

            filterSource = Okio.source(filterStream);
            filterSink = Okio.buffer(Okio.sink(filterFile));
            filterSink.writeAll(filterSource);
        } finally {
            closeSilently(inputSource, inputSink, filterSource, filterSink);
        }
    }

    /**
     * Delegates the transformation task to the native JQ library.
     *
     * @param inputFile  The file containing the JSON to transform.
     * @param filterFile The file containing the JQ control filter.
     * @param outputFile The file JQ should write the result to.
     */
    private int performTransformation(@NonNull final File inputFile,
                                      @NonNull final File filterFile,
                                      @NonNull final File outputFile) {

        // Let the native library do its magic.
        return transformJson(inputFile.getAbsolutePath(),
                filterFile.getAbsolutePath(),
                outputFile.getAbsolutePath());
    }

    /**
     * Copies the produced transformation from the working directory to the
     * supplied output stream.
     *
     * @param cacheFile    The internal working copy to read the content from.
     * @param outputStream The output stream to write the transformation to.
     * @throws IOException If the file I/O would fail for any reason.
     */
    private void copyResultToStream(@NonNull final File cacheFile,
                                    @NonNull final OutputStream outputStream) throws IOException {

        if (!cacheFile.exists())
            return;

        Source outputSource = null;
        BufferedSink outputSink = null;

        try {
            outputSource = Okio.source(cacheFile);
            outputSink = Okio.buffer(Okio.sink(outputStream));
            outputSink.writeAll(outputSource);
        } finally {
            closeSilently(outputSource, outputSink);
        }
    }

    /**
     * Tries to gracefully close a closeable. This method will silently consume
     * any thrown exceptions.
     *
     * @param closeables The closeables to close.
     */
    private void closeSilently(Closeable... closeables) {
        if (closeables != null)
            Stream.of(closeables)
                    .filter(closeable -> closeable != null)
                    .forEach(closeable -> {
                        try {
                            closeable.close();
                        } catch (Exception e) {
                            // Shhhh! Close your eyes and let it happen...
                        }
                    });
    }

}
