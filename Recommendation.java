import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class Recommendation {

    Map<Long, HashMap<String, Integer>> userData;

    public Recommendation(String filePath) {
        this.userData = new HashMap<>();
        parseCsvFile(filePath);
    }

    private void parseCsvFile(String csvFilePath) {
        try {
            Scanner scanner = new Scanner(new File(csvFilePath));
            scanner.nextLine();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                long userId = Long.parseLong(parts[0]);
                String artistName = parts[1];
                int minutesListened = Integer.parseInt(parts[3]);

                if (!userData.containsKey(userId)) {
                    userData.put(userId, new HashMap<>());
                }

                HashMap<String, Integer> artistMap = userData.get(userId);
                if (!artistMap.containsKey(artistName)) {
                    artistMap.put(artistName, 0);
                }
                int prevMinutesListened = artistMap.get(artistName);
                artistMap.put(artistName, prevMinutesListened + minutesListened);
            }
            scanner.close();


            for (Map.Entry<Long, HashMap<String, Integer>> entry : userData.entrySet()) {
                long userId = entry.getKey();
                HashMap<String, Integer> artistMap = entry.getValue();

                List<Map.Entry<String, Integer>> sortedArtists
                        = new ArrayList<>(artistMap.entrySet());
                sortedArtists.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

                HashMap<String, Integer> newArtistMap = new HashMap<>();
                for (Map.Entry<String, Integer> artist
                        : sortedArtists.subList(0, Math.min(sortedArtists.size(), 5))) {
                    newArtistMap.put(artist.getKey(), artist.getValue());
                }
                entry.setValue(newArtistMap);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] recommendNewArtists(List<String> artistList) {
        Map<Long, Set<String>> userArtists = new HashMap<>();

        for (Map.Entry<Long, HashMap<String, Integer>> userEntry : userData.entrySet()) {
            Set<String> artists = userEntry.getValue().keySet();
            userArtists.put(userEntry.getKey(), artists);
        }

        Map<Long, Double> similarityScores = new HashMap<>();
        for (Map.Entry<Long, Set<String>> userEntry : userArtists.entrySet()) {
            Set<String> userArtistList = userEntry.getValue();
            Set<String> intersection = new HashSet<>(artistList);
            intersection.retainAll(userArtistList);
            Set<String> union = new HashSet<>(artistList);
            union.addAll(userArtistList);
            double similarity = union.size()
                    != 0 ? (double) intersection.size() / union.size() : 0;
            similarityScores.put(userEntry.getKey(), similarity);
        }

        List<Long> topUsers = similarityScores.keySet().stream().
                sorted(Comparator.comparingDouble(similarityScores::get).reversed()).
                limit(3).collect(Collectors.toList());

        Set<String> recommendedArtists = new HashSet<>();
        for (Long userId : topUsers) {
            Set<String> artists = userArtists.get(userId);
            recommendedArtists.addAll(artists);
        }
        recommendedArtists.removeAll(artistList);
        return recommendedArtists.toArray(new String[0]);
    }

}
