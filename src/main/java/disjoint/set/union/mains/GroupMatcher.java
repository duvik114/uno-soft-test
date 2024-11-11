package disjoint.set.union.mains;

import disjoint.set.union.data.Group;
import disjoint.set.union.exception.GroupMatchException;
import disjoint.set.union.utils.DSU;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GroupMatcher {

    private final String inputFile;
    private final String outputFile;
    private final LinkedHashSet<String> strings;
    private final ArrayList<HashMap<Double, Integer>> wordsMaps;

    public GroupMatcher(String inputFile) {
        this.inputFile = inputFile;
        this.outputFile = getOutputFileName(inputFile);
        this.strings = new LinkedHashSet<>();
        this.wordsMaps = new ArrayList<>();
    }

    private String getOutputFileName(String inputFile) {
        int dotIndex = inputFile.lastIndexOf('.') < 0 ? inputFile.length() : inputFile.lastIndexOf('.');
        return inputFile.substring(0, dotIndex) + "-out" + inputFile.substring(dotIndex);
    }

    private Double[] splitString(String s) throws GroupMatchException {
        String[] tokens = s.split(";", -1);

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].isEmpty()) {
                tokens[i] = "\"\"";
            }
            else if (tokens[i].length() < 2) {
                throw new GroupMatchException("Wrong string in input file: " + s);
            }
        }

        try {
            return Arrays.stream(tokens)
                    .map(t -> t.substring(1, t.length() - 1))
                    .map(t -> {
                        if (t.isEmpty()) {
                            return null;
                        } else {
                            return Double.valueOf(t);
                        }
                    })
                    .toArray(Double[]::new);
        } catch (NumberFormatException e) {
            throw new GroupMatchException("Cannot format string to numbers: " + s);
        }
    }

    private void readToSNM(DSU dsu) throws GroupMatchException {
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile, StandardCharsets.UTF_8))) {
            String line;
            int lineNum = 0;
            while ((line = br.readLine()) != null) {

                if (line.isEmpty()) {
                    continue;
                }

                Double[] values;
                try {
                    values = splitString(line);
                } catch (GroupMatchException e) {
                    System.err.println(e.getMessage());
                    continue;
                }

                if (strings.contains(line)) {
                    continue;
                } else {
                    addNewLine(dsu, line, lineNum);
                }

                checkColumns(dsu, lineNum, values);

                lineNum++;
            }
        } catch (FileNotFoundException e) {
            throw new GroupMatchException("Input file " + inputFile + " not found");
        } catch (IOException e) {
            throw new GroupMatchException("Error while reading input file: " + inputFile);
        }
    }

    private void addNewLine(DSU dsu, String line, int lineNum) {
        strings.add(line);
        dsu.addString(lineNum);
    }

    private void checkColumns(DSU dsu, int lineNum, Double[] values) {
        for (int i = 0; i < values.length; i++) {
            Double value = values[i];

            if (value == null) {
                continue;
            }

            while (i >= wordsMaps.size()) {
                wordsMaps.add(new HashMap<>());
            }

            if (wordsMaps.get(i).containsKey(value)) {
                dsu.union(wordsMaps.get(i).get(value), lineNum);
            } else {
                wordsMaps.get(i).put(value, lineNum);
            }
        }
    }

    private int[] printGroups(DSU dsu, long timeStart) throws GroupMatchException {
        int i = 1;
        int[] res = new int[2];
        Group[] groups = dsu.getGroupsAndPos(res);
        String[] stringsArray = strings.toArray(String[]::new);

        try {
            File output = new File(outputFile);
            output.createNewFile();
        } catch (IOException e) {
            throw new GroupMatchException("Error creating output file");
        }

        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(outputFile, StandardCharsets.UTF_8));

            writer.write(String.valueOf(res[0]));
            writer.newLine();

            for (Group g : groups) {
                writer.write("Group " + i++ + ":");
                writer.newLine();
                for (int num : g.getStrings()) {
                    writer.write(stringsArray[num]);
                    writer.newLine();
                }
            }
            writer.close();
        } catch (IOException e) {
            throw new GroupMatchException("Error writing to output file: " + e.getMessage());
        }
        res[1] = groups.length;
        return res;
    }

    private static void printUsage() {
        System.out.println("One argument expected: <INPUT_FILE_NAME>");
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            printUsage();
            return;
        }

        long timeStart = System.currentTimeMillis();

        DSU dsu = new DSU();
        GroupMatcher groupMatcher = new GroupMatcher(args[0]);

        try {
            groupMatcher.readToSNM(dsu);
        } catch (GroupMatchException e) {
            System.err.println(e.getMessage());
            return;
        }

        int[] groupsCountAndPos;
        try {
            groupsCountAndPos = groupMatcher.printGroups(dsu, timeStart);
        } catch (GroupMatchException e) {
            System.err.println(e.getMessage());
            return;
        }

        System.out.println("================================================================");
        System.out.println("Total number of groups: " + groupsCountAndPos[1]);
        System.out.println("Number of groups containing more than 1 string: " + groupsCountAndPos[0]);
        System.out.println("Done in " + ((System.currentTimeMillis() - timeStart) / 1000.0) + " seconds!");
    }
}
