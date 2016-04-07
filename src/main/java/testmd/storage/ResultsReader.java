package testmd.storage;

import testmd.Permutation;
import testmd.PermutationResult;
import testmd.PreviousResults;
import testmd.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads results from stored markdown file.
 */
public class ResultsReader {

    private enum Section {
        DEFINITION,
        NOTES,
        DATA
    }

    public List<PreviousResults> read(String testClass, Reader reader) throws IOException {

        List<PreviousResults> returnList = new ArrayList<>();
        PreviousResults previousResults = null;

        Pattern testStartPattern = Pattern.compile("# Test: \"(.*)\" #");
        Pattern permutationStartPattern = Pattern.compile("## Permutation (\\S*) (.*) ##");
        Pattern permutationSeparatorPattern = Pattern.compile("^---------------------------------------");
        Pattern internalKeyValuePattern = Pattern.compile("\\- _(.+):_ (.+)");
        Pattern keyValuePattern = Pattern.compile("\\- \\*\\*(.+):\\*\\* (.*)");
        Pattern multiLineKeyValuePattern = Pattern.compile("\\- \\*\\*(.+) =>\\*\\*");
        Pattern resultDetailsMatcher = Pattern.compile("\\*\\*(.*?)\\*\\*: (.*)");
        Pattern notesDetailsMatcher = Pattern.compile("__(.*?)__: (.*)");
        Pattern testVersionPattern = Pattern.compile("# Test Version: \"(.*)\" #");

        List<String> thisTableColumns = null;
        BufferedReader bufferedReader = new BufferedReader(reader);

        CurrentPermutationDetails currentPermutationDetails = null;
        Map<String, String> commonDetails = new HashMap<>();

        String line;
        int lineNumber = 0;
        Section section = null;
        String multiLineKey = null;
        String multiLineValue = null;
        boolean firstTableRow = true;

        Set<String> tableColumns = new HashSet<>();

        String testHash = null;

        while ((line = bufferedReader.readLine()) != null) {
            lineNumber++;

            Matcher headerMatcher = testStartPattern.matcher(line);
            if (headerMatcher.matches()) {
                if (previousResults != null) {
                    saveLastPermutation(currentPermutationDetails, previousResults, tableColumns);
                }

                currentPermutationDetails = new CurrentPermutationDetails();
                commonDetails = new HashMap<>();
                section = Section.DEFINITION;
                tableColumns = new HashSet<>();

                previousResults = new PreviousResults(testClass, headerMatcher.group(1));
                returnList.add(previousResults);
                continue;
            }


            Matcher testVersionMatcher = testVersionPattern.matcher(line);
            if (testVersionMatcher.matches()) {
                testHash = testVersionMatcher.group(1);
                continue;
            }

            if (line.equals("**NO PERMUTATIONS**")) {
                continue;
            }

            if (multiLineKey != null) {
                if (line.equals("") || line.startsWith("    ")) {
                    multiLineValue += line.replaceFirst("    ", "") + "\n";
                    continue;
                } else {
                    multiLineValue = multiLineValue.trim();
                    if (section.equals(Section.DEFINITION)) {
                        currentPermutationDetails.parameters.put(multiLineKey, multiLineValue);
                        commonDetails.put(multiLineKey, multiLineValue);
                    } else if (section.equals(Section.NOTES)) {
                        currentPermutationDetails.notes.put(multiLineKey, multiLineValue);
                    } else if (section.equals(Section.DATA)) {
                        currentPermutationDetails.results.put(multiLineKey, multiLineValue);
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
                saveLastPermutation(currentPermutationDetails, previousResults, tableColumns);
                currentPermutationDetails = new CurrentPermutationDetails();
                commonDetails = new HashMap<>();
                section = Section.DEFINITION;
                tableColumns = new HashSet<>();

                String verifiedData = matcher.group(2);
                if (verifiedData.equals("(verified)")) {
                    currentPermutationDetails.verified = true;
                } else {
                    currentPermutationDetails.verified = false;
                    String[] split = verifiedData.split(": ", 2);
                    if (!split[0].equals("_NOT VERIFIED")) {
                        throw new RuntimeException("Did not expect " + split[0]);
                    }

                    if (split.length == 2) {
                        currentPermutationDetails.notRanMessage = split[1].replaceFirst("_$", "");
                    }
                }
                continue;
            }

            if (permutationSeparatorPattern.matcher(line).matches()) {
                saveLastPermutation(currentPermutationDetails, previousResults, tableColumns);
                currentPermutationDetails = new CurrentPermutationDetails();
                commonDetails = new HashMap<>();
                section = Section.DEFINITION;
                continue;
            }


            Matcher internalKeyValueMatcher = internalKeyValuePattern.matcher(line);
            if (internalKeyValueMatcher.matches()) {
                String key = internalKeyValueMatcher.group(1);
                String value = internalKeyValueMatcher.group(2);
                if (key.equals("VERIFIED")) {
                    currentPermutationDetails.setVerified(value);
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
                    currentPermutationDetails.parameters.put(key, value);
                    commonDetails.put(key, value);
                } else if (section.equals(Section.NOTES)) {
                    currentPermutationDetails.notes.put(key, value);
                } else if (section.equals(Section.DATA)) {
                    currentPermutationDetails.results.put(key, value);
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
                    thisTableColumns = new ArrayList<>();

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
                            saveLastPermutation(currentPermutationDetails, previousResults, tableColumns);
                            currentPermutationDetails = new CurrentPermutationDetails();
                        }

                        for (Map.Entry<String, String> entry : commonDetails.entrySet()) {
                            currentPermutationDetails.parameters.put(entry.getKey(), entry.getValue());
                        }
                        currentPermutationDetails.setVerified(values[2]);

                        int columnNum = 0;
                        try {
                            for (int i = 3; i < values.length - 1; i++) {
                                if (!values[i].equals("")) {
                                    currentPermutationDetails.parameters.put(thisTableColumns.get(columnNum), decode(values[i]));
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
                        currentPermutationDetails.results.put(dataMatcher.group(1), decode(dataMatcher.group(2)));
                    } else if (notesMatcher.matches()) {
                        currentPermutationDetails.notes.put(notesMatcher.group(1), decode(notesMatcher.group(2)));
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

            if (currentPermutationDetails == null) {
                //in the header section describing what the file is for
            } else {
                throw new RuntimeException("Could not parse line " + lineNumber + ": " + line);
            }
        }
        saveLastPermutation(currentPermutationDetails, previousResults, tableColumns);

        for (PreviousResults result : returnList) {
            for (PermutationResult permutation : result.getResults()) {
                permutation.setTestHash(testHash);
            }
        }


        return returnList;
    }

    private void saveLastPermutation(CurrentPermutationDetails currentPermutationDetails, PreviousResults testRun, Set<String> tableColumns) {
        if (currentPermutationDetails == null || currentPermutationDetails.verified == null) {
            return;
        }
        PermutationResult result;
        if (currentPermutationDetails.verified) {
            result = new PermutationResult.Verified();
        } else {
            result = new PermutationResult.Unverified(currentPermutationDetails.notRanMessage);
        }

        result.setParameters(currentPermutationDetails.parameters);
        result.setNotes(currentPermutationDetails.notes);
        result.setResults(currentPermutationDetails.results);

        result.setTableParameters(tableColumns);

        testRun.addResult(result);
    }

    private String decode(String string) {
        return string.replace("<br>", "\n").replace("&#124;", "|");
    }

    private static class CurrentPermutationDetails {
        private Map<String, String> parameters = new HashMap<>();
        private Map<String, String> notes = new HashMap<>();
        private Map<String, String> results = new HashMap<>();
        private Boolean verified;
        private String notRanMessage;

        public CurrentPermutationDetails() {
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
