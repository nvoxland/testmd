package testmd;

import testmd.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultsReader {

    private enum Section {
        DEFINITION,
        NOTES,
        DATA
    }

    public List<PermutationResult> read(Reader... readers) throws IOException {
        List<PermutationResult> results = new ArrayList<PermutationResult>();

        String testClass = null;
        String testName = null;

        Pattern headerPattern = Pattern.compile("# Test: (\\S*) \"(.*)\" #");
        Pattern permutationStartPattern = Pattern.compile("## Permutation (\\S*) (.*) ##");
        Pattern permutationSeparatorPattern = Pattern.compile("^---------------------------------------");
        Pattern internalKeyValuePattern = Pattern.compile("\\- _(.+):_ (.+)");
        Pattern keyValuePattern = Pattern.compile("\\- \\*\\*(.+):\\*\\* (.*)");
        Pattern multiLineKeyValuePattern = Pattern.compile("\\- \\*\\*(.+) =>\\*\\*");
        Pattern resultDetailsMatcher = Pattern.compile("\\*\\*(.*?)\\*\\*: (.*)");
        Pattern notesDetailsMatcher = Pattern.compile("__(.*?)__: (.*)");

        Set<String> tableColumns = new HashSet<String>();
        List<String> thisTableColumns = null;
        for (Reader reader : readers) {
            BufferedReader bufferedReader = new BufferedReader(reader);

            CurrentDetails currentDetails = null;
            Map<String, String> commonDetails = new HashMap<String, String>();

            String line;
            int lineNumber = 0;
            Section section = null;
            String multiLineKey = null;
            String multiLineValue = null;
            boolean firstTableRow = true;

            while ((line = bufferedReader.readLine()) != null) {
                lineNumber++;

                if (lineNumber == 1) {
                    Matcher headerMatcher = headerPattern.matcher(line);
                    if (headerMatcher.matches()) {
                        testClass = headerMatcher.group(1);
                        testName = headerMatcher.group(2);
                    } else {
                        throw new IOException("Invalid header: " + line);
                    }

                    continue;
                }

                if (line.equals("**NO PERMUTATIONS**")) {
                    return new ArrayList<PermutationResult>();
                }

                if (multiLineKey != null) {
                    if (line.equals("") || line.startsWith("    ")) {
                        multiLineValue += line.replaceFirst("    ", "") + "\n";
                        continue;
                    } else {
                        multiLineValue = multiLineValue.trim();
                        if (section.equals(Section.DEFINITION)) {
                            currentDetails.parameters.put(multiLineKey, multiLineValue);
                            commonDetails.put(multiLineKey, multiLineValue);
                        } else if (section.equals(Section.NOTES)) {
                            currentDetails.notes.put(multiLineKey, multiLineValue);
                        } else if (section.equals(Section.DATA)) {
                            currentDetails.results.put(multiLineKey, multiLineValue);
                        } else {
                            throw new RuntimeException("Unknown multiline section on line " + lineNumber + ": " + section);
                        }
                        multiLineKey = null;
                        multiLineValue = null;
                    }
                }

                if (StringUtils.trimToEmpty(line).equals("")) {
                    continue;
                }

                if (line.equals("#### Notes ####")) {
                    section = Section.NOTES;
                    continue;
                } else if (line.equals("#### Results ####")) {
                    section = Section.DATA;
                    continue;
                }

                Matcher matcher = permutationStartPattern.matcher(line);
                if (matcher.matches()) {
                    saveLastPermutation(currentDetails, results);
                    currentDetails = new CurrentDetails(testClass, testName);
                    commonDetails = new HashMap<String, String>();
                    firstTableRow = true;
                    section = Section.DEFINITION;

                    String verifiedData = matcher.group(2);
                    if (verifiedData.equals("(verified)")) {
                        currentDetails.verified = true;
                    } else {
                        currentDetails.verified = false;
                        String[] split = verifiedData.split(": ", 2);
                        assert split[0].equals("_NOT VERIFIED") : "Did not expect "+split[0];
                        if (split.length == 2) {
                            currentDetails.notRanMessage = split[1].replaceFirst("_$","");
                        }
                    }
                    continue;
                }

                if (permutationSeparatorPattern.matcher(line).matches()) {
                    saveLastPermutation(currentDetails, results);
                    currentDetails = new CurrentDetails(testClass, testName);
                    commonDetails = new HashMap<String, String>();
                    firstTableRow = true;
                    section = Section.DEFINITION;
                    continue;
                }


                Matcher internalKeyValueMatcher = internalKeyValuePattern.matcher(line);
                if (internalKeyValueMatcher.matches()) {
                    String key = internalKeyValueMatcher.group(1);
                    String value = internalKeyValueMatcher.group(2);
                    if (key.equals("VERIFIED")) {
                        currentDetails.setVerified(value);
                    } else {
                        throw new RuntimeException("Unknown internal parameter " + key);
                    }
                    continue;
                }

                Matcher keyValueMatcher = keyValuePattern.matcher(line);
                if (keyValueMatcher.matches()) {
                    String key = keyValueMatcher.group(1);
                    String value = keyValueMatcher.group(2);

                    if (section.equals(Section.DEFINITION)) {
                        currentDetails.parameters.put(key, value);
                        commonDetails.put(key, value);
                    } else if (section.equals(Section.NOTES)) {
                        currentDetails.notes.put(key, value);
                    } else if (section.equals(Section.DATA)) {
                        currentDetails.results.put(key, value);
                    } else {
                        throw new RuntimeException("Unknown section " + section);
                    }
                    continue;
                }

                if (line.startsWith("|")) {
                    String unlikelyStringForSplit = "OIPUGAKJNGAOIUWDEGKJASDG";
                    String lineToSplit = line.replaceFirst("\\|", unlikelyStringForSplit).replaceAll("([^\\\\])\\|", "$1" + unlikelyStringForSplit);
                    String[] values = lineToSplit.split("\\s*" + unlikelyStringForSplit + "\\s*");
                    if (line.startsWith("| Permutation ")) {
                        thisTableColumns = new ArrayList<String>();

                        for (int i = 3; i < values.length - 1; i++) { //ignoring first value that is an empty string and last value that is DETAILS
                            tableColumns.add(values[i]);
                            thisTableColumns.add(values[i]);
                        }
                    } else if (line.startsWith("| :---")) {
                        continue;
                    } else {
                        firstTableRow = false;
                        if (values[1].equals("")) {
                            ; //continuing row
                            System.out.println("");
                        } else {
                            if (!firstTableRow) {
                                saveLastPermutation(currentDetails, results);
                                currentDetails = new CurrentDetails(testClass, testName);
                            }

                            for (Map.Entry<String, String> entry : commonDetails.entrySet()) {
                                currentDetails.parameters.put(entry.getKey(), entry.getValue());
                            }
                            currentDetails.setVerified(values[2]);

                            int columnNum = 0;
                            try {
                                for (int i = 3; i < values.length - 1; i++) {
                                    if (!values[i].equals("")) {
                                        currentDetails.parameters.put(thisTableColumns.get(columnNum), decode(values[i]));
                                    }
                                    columnNum++;
                                }
                            } catch (Throwable e) {
                                throw new RuntimeException("Error parsing line " + line, e);
                            }
                        }
                        String details = values[values.length - 1];
                        Matcher dataMatcher = resultDetailsMatcher.matcher(details);
                        Matcher notesMatcher = notesDetailsMatcher.matcher(details);
                        if (dataMatcher.matches()) {
                            currentDetails.results.put(dataMatcher.group(1), decode(dataMatcher.group(2)));
                        } else if (notesMatcher.matches()) {
                            currentDetails.notes.put(notesMatcher.group(1), decode(notesMatcher.group(2)));
                        } else {
                            throw new RuntimeException("Unknown details column format: " + details);
                        }
                    }
                    continue;
                }

                Matcher multiLineKeyValueMatcher = multiLineKeyValuePattern.matcher(line);
                if (multiLineKeyValueMatcher.matches()) {
                    multiLineKey = multiLineKeyValueMatcher.group(1);
                    multiLineValue = "";
                    continue;
                }

                if (currentDetails == null) {
                    //in the header section describing what the file is for
                } else {
                    throw new RuntimeException("Could not parse line " + lineNumber + ": " + line);
                }
            }
            saveLastPermutation(currentDetails, results);
        }

        for (PermutationResult result : results) {
            result.setTableParameters(tableColumns);
        }
        return results;
    }

    private void saveLastPermutation(CurrentDetails currentDetails, List<PermutationResult> results) {
        if (currentDetails == null || currentDetails.verified == null) {
            return;
        }
        PermutationResult result;
        if (currentDetails.verified) {
            result = new PermutationResult.Verified();
        } else {
            result = new PermutationResult.Unverified(currentDetails.notRanMessage);
        }

        result.setParameters(currentDetails.parameters);
        result.setNotes(currentDetails.notes);
        result.setResults(currentDetails.results);

        results.add(result);
    }

    private String decode(String string) {
        return string.replace("<br>", "\n").replace("&#124;", "|");
    }

    private static class CurrentDetails {
        private final String testClass;
        private final String testName;
        private Map<String, String> parameters = new HashMap<String, String>();
        private Map<String, String> notes = new HashMap<String, String>();
        private Map<String, String> results = new HashMap<String, String>();
        private Boolean verified;
        private String notRanMessage;

        public CurrentDetails(String testClass, String testName) {
            this.testClass = testClass;
            this.testName = testName;
        }

        protected void setVerified(String value) {
            if (value.equals("true")) {
                verified = true;
            } else {
                verified = false;
                if (!value.equals("false")) {
                    notRanMessage = value;
                }
            }
        }

    }
}
