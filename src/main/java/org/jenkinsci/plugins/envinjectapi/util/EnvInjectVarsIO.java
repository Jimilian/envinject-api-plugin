package org.jenkinsci.plugins.envinjectapi.util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.jenkinsci.lib.envinject.EnvInjectException;

/**
 * Provides methods for disk persistency of environment variables.
 * @author Gregory Boissinot
 * @author Oleg Nenashev
 */
public class EnvInjectVarsIO {

    private static final String ENVINJECT_TXT_FILENAME = "injectedEnvVars.txt";
    private static final String TOKEN = "=";

    private EnvInjectVarsIO() {
        // Cannot be instantinated
    }
    
    @CheckForNull
    public static Map<String, String> getEnvironment(@Nonnull File envInjectBaseDir) throws EnvInjectException {
        File f = new File(envInjectBaseDir, ENVINJECT_TXT_FILENAME);
        if (!f.exists()) {
            return null;
        }

        try (Reader fileReader = Files.newBufferedReader(f.toPath(), Charset.defaultCharset())) {
            Map<String, String> result = new HashMap<>();
            fromTxt(fileReader, result);
            return result;
        } catch (IOException | InvalidPathException fne) {
            throw new EnvInjectException(fne);
        }
    }

    public static void saveEnvironment(@Nonnull File rootDir, @Nonnull Map<String, String> envMap) throws EnvInjectException { 
        File f = new File(rootDir, ENVINJECT_TXT_FILENAME);
        try (Writer wr = Files.newBufferedWriter(f.toPath(), Charset.defaultCharset())) {
            Map<String, String> map2Write = new TreeMap<>();
            map2Write.putAll(envMap);
            toTxt(map2Write, wr);
        } catch (IOException | InvalidPathException ex) {
            throw new EnvInjectException(ex);
        }
    }

    private static void fromTxt(@Nonnull Reader reader, @Nonnull Map<String, String> result) throws EnvInjectException {
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                StringTokenizer tokenizer = new StringTokenizer(line, TOKEN);
                int tokens = tokenizer.countTokens();
                if (tokens == 2) {
                    result.put(String.valueOf(tokenizer.nextElement()), String.valueOf(tokenizer.nextElement()));
                }
            }
        } catch (IOException ioe) {
            throw new EnvInjectException(ioe);
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException ioe) {
                throw new EnvInjectException(ioe);
            }
        }
    }

    private static void toTxt(@Nonnull Map<String, String> envMap, @Nonnull Writer fw) throws IOException {
        for (Map.Entry<String, String> entry : envMap.entrySet()) {
            fw.write(String.format("%s%s%s%n", entry.getKey(), TOKEN, entry.getValue()));
        }
    }

}
