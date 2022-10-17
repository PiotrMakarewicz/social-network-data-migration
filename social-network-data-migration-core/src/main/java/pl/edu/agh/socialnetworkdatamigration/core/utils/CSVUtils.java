package pl.edu.agh.socialnetworkdatamigration.core.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.io.IOException;
import java.util.stream.Collectors;

public class CSVUtils {

    public static List<String> getHeaders(String csvInputPath, char fieldTerminator) {
        return List.of(readFirstLine(csvInputPath).split(Pattern.quote(String.valueOf(fieldTerminator))));
    }

    public static int getColumnCnt(String csvInputPath, char fieldTerminator) {
        return (int) (readFirstLine(csvInputPath).chars().filter(c -> c == fieldTerminator).count() + 1);
    }

    private static String readFirstLine(String csvInputPath) {
        try (BufferedReader file = new BufferedReader(new FileReader(csvInputPath))) {
            return file.readLine();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error while reading file: %s%n", csvInputPath), e);
        }
    }

    public static Map<Integer, String> keysToInt(Map<String, String> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(e -> Integer.parseInt(e.getKey()), Map.Entry::getValue));
    }

    public static Map<Integer, String> headersToIndexes(Map<String, String> map, List<String> headers) {

        return map.entrySet().stream()
                .collect(Collectors.toMap(e ->
                        {
                            String searchedKey = e.getKey();
                            var index = headers.indexOf(searchedKey);
                            for (int i = 0; i < headers.size(); i++){
                                if (((String) headers.get(i)).equals(searchedKey)){
                                    index = i;
                                }
                            }
                            if (index == -1) {
                                System.out.println(headers);
                                throw new RuntimeException(String.format("There is no header %s in the CSV file", searchedKey));
                            }
                                return index;
                        },
                        Map.Entry::getValue));
    }

}
