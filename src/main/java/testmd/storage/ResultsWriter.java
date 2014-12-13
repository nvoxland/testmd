package testmd.storage;

import testmd.PermutationResult;
import testmd.util.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Writes results to stored markdown-based file.
 */
public class ResultsWriter {

    private static final String SEPARATOR = "---------------------------------------";

    public void write(String testClass, String testName, Collection<PermutationResult> results, Writer out) throws IOException {
        out.append("# Test: ").append(testClass).append(" \"").append(testName).append("\"");
        out.append(" #\n\n");

        out.append("NOTE: This output is generated and parsed by TestMD. Please read it, but DO NOT EDIT MANUALLY\n\n"+SEPARATOR+"\n\n");

        if (results.size() == 0) {
            out.append("**NO PERMUTATIONS**\n");
        } else if (shouldPrintTables(results)) {
            printWithTables(results, out);
        } else {
            printWithoutTables(results, out);
        }

        out.flush();
    }

    protected boolean shouldPrintTables(Collection<PermutationResult> results) {
        if (results == null || results.size() == 0) {
            return false;
        }

        PermutationResult result = results.iterator().next();
        return result.getTableParameters() != null && result.getTableParameters().size() > 0;

    }

    protected void printWithoutTables(Collection<PermutationResult> passedResults, Writer out) throws IOException {
        List<PermutationResult> results = new ArrayList(passedResults);
        Collections.sort(results);

        for (PermutationResult result : results) {
            if (!result.isValid()) {
                continue;
            }

            out.append("## Permutation ").append(result.getKey());
            if (result.isVerified()) {
                out.append(" (verified)");
            } else {
                out.append(" _NOT VERIFIED");
                if (result.getNotVerifiedMessage() != null) {
                    out.append(": ").append(result.getNotVerifiedMessage());
                }
                out.append("_");
            }
            out.append(" ##\n\n");

            for (Map.Entry<String, String> entry : result.getParameters().entrySet()) {
                appendMapEntry(entry, out);
            }

            if (result.getNotes().size() > 0) {
                out.append("\n");
                out.append("#### Notes ####\n");
                out.append("\n");

                for (Map.Entry<String, String> entry : result.getNotes().entrySet()) {
                    appendMapEntry(entry, out);
                }
            }

            if (result.getResults().entrySet().size() > 0) {
                out.append("\n");
                out.append("#### Results ####\n");
                out.append("\n");

                for (Map.Entry<String, String> entry : result.getResults().entrySet()) {
                    appendMapEntry(entry, out);
                }
            }

            out.append("\n"+SEPARATOR+"\n\n");
        }
    }

    protected void printWithTables(Collection<PermutationResult> passedResults, Writer out) throws IOException {
        List<PermutationResult> results = new ArrayList(passedResults);
        Collections.sort(results);

        SortedMap<String, List<PermutationResult>> resultsByTable = new TreeMap<String, List<PermutationResult>>();
        for (PermutationResult result : passedResults) {
            String tableKey = result.getTableKey();
            if (!resultsByTable.containsKey(tableKey)) {
                resultsByTable.put(tableKey, new ArrayList<PermutationResult>());
            }
            resultsByTable.get(tableKey).add(result);
        }

        if (resultsByTable.isEmpty()) {
            return;
        }

        int testCount = 0;
        for (Map.Entry<String, List<PermutationResult>> entry : resultsByTable.entrySet()) {
            List<PermutationResult> tableResults = new ArrayList<PermutationResult>();
            for (PermutationResult result : entry.getValue()) {
                if (result.isValid()) {
                    tableResults.add(result);
                }
            }

            if (tableResults.size() == 0) {
                continue;
            }

            if (testCount++ > 0) {
                out.append(SEPARATOR + "\n\n");
            }

            for (Map.Entry<String, String> descriptionEntry : tableResults.get(0).getParameters().entrySet()) {
                if (!tableResults.get(0).getTableParameters().contains(descriptionEntry.getKey())) {
                    appendMapEntry(descriptionEntry, out);
                }
            }


            SortedMap<String, Integer> maxColumnLengths = new TreeMap<String, Integer>();
            int permutationNameColLength = "Permutation".length();
            int verifiedColLength = "Verified".length();

            Map<String, String> verifiedMessages = new HashMap<String, String>();
            for (PermutationResult result : tableResults) {
                if (result.getKey().length() > permutationNameColLength) {
                    permutationNameColLength = result.getKey().length();
                }

                String verifiedMessage = StringUtils.trimToNull(result.getNotVerifiedMessage());
                if (verifiedMessage == null) {
                    verifiedMessage = String.valueOf(result.isVerified());
                }
                verifiedMessages.put(result.getKey(), verifiedMessage);

                if (verifiedMessage.length() > verifiedColLength) {
                    verifiedColLength = verifiedMessage.length();
                }
                for (String columnName : result.getTableParameters()) {
                    Integer oldMax = maxColumnLengths.get(columnName);
                    if (oldMax == null) {
                        oldMax = columnName.length();
                        maxColumnLengths.put(columnName, oldMax);
                    }
                    String storedValue = result.getParameters().get(columnName);
                    if (storedValue != null) {
                        if (oldMax < storedValue.length()) {
                            maxColumnLengths.put(columnName, storedValue.length());
                        }
                    }
                }
            }

            out.append("\n");

            String headerRow = "| "+StringUtils.pad("Permutation", permutationNameColLength)+" | "+StringUtils.pad("Verified", verifiedColLength)+" |";
            for (Map.Entry<String, Integer> columnEntry : maxColumnLengths.entrySet()) {
                headerRow += " "+StringUtils.pad(columnEntry.getKey(), columnEntry.getValue())+" |";
            }
            headerRow += " RESULTS\n";

            String headerSeparator = "| :"+StringUtils.repeat("-", permutationNameColLength - 1)+" | :"+StringUtils.repeat("-", verifiedColLength - 1)+" |";
            for (Map.Entry<String, Integer> columnEntry : maxColumnLengths.entrySet()) {
                headerSeparator += " :"+StringUtils.repeat("-", columnEntry.getValue() - 1)+" |";
            }
            headerSeparator += " :------\n";

            out.append(headerRow);
            out.append(headerSeparator);

            SortedMap<String, String> permutationRows = new TreeMap<String, String>();
            for (PermutationResult result : tableResults) {
                StringBuilder row = new StringBuilder();
                row.append("| ").append(StringUtils.pad(result.getKey(), permutationNameColLength))
                        .append(" | ")
                        .append(StringUtils.pad(verifiedMessages.get(result.getKey()), verifiedColLength))
                        .append(" |");

                String rowKey = "";
                for (Map.Entry<String, Integer> columnAndLength : maxColumnLengths.entrySet()) {
                    String cellValue = result.getParameters().get(columnAndLength.getKey());
                    String cellString;
                    if (cellValue == null) {
                        cellString = "";
                    } else {
                        cellString = clean(cellValue);
                    }

                    rowKey += " "+StringUtils.pad(cellString, columnAndLength.getValue())+ " |";
                }
                row.append(rowKey);

                List<String> details = new ArrayList<String>();
                for (Map.Entry<String, String> notesEntry : result.getNotes().entrySet()) {
                    details.add(" __"+notesEntry.getKey()+"__: "+clean(notesEntry.getValue()));
                }
                for (Map.Entry<String, String> dataEntry : result.getResults().entrySet()) {
                    details.add(" **" + dataEntry.getKey() + "**: " + clean(dataEntry.getValue()));
                }

                for (int i=0; i<details.size(); i++) {
                    if (i > 0) {
                        row.append("| ").append(StringUtils.pad("", permutationNameColLength)).append(" | ")
                                .append(StringUtils.pad("", verifiedColLength)).append(" |");
                        for (Map.Entry<String, Integer> nameAndLength : maxColumnLengths.entrySet()) {
                            row.append(" ").append(StringUtils.pad("", nameAndLength.getValue())).append(" |");
                        }

                    }
                    row.append(details.get(i)).append("\n");
                }

                permutationRows.put(rowKey, row.toString());
            }
            out.append(StringUtils.join(permutationRows.values(), "", StringUtils.STANDARD_STRING_FORMAT, false));
            out.append("\n\n");
        }

        out.append(SEPARATOR).append("\n\n");
    }

    private String clean(String string) {
        return string.replace("\r\n", "\n").replace("\n", "<br>").replace("|", "&#124;");
    }

    private void appendMapEntry(Map.Entry<String, String> entry, Writer out) throws IOException {
        String value = entry.getValue();
        value  = value.replace("\r\n", "\n");

        boolean multiLine = value.contains("\n");

        out.append("- **").append(entry.getKey());

        if (multiLine) {
            out.append(" =>**\n");
        } else {
            out.append(":** ");
        }
        if (multiLine) {
            out.append(StringUtils.indent(value, 4));
        } else {
            out.append(value);
        }

        out.append("\n");
    }

}
