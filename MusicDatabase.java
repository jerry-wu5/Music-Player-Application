import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;
import java.io.File;
import java.util.TreeMap;
import java.util.Comparator;
import java.util.List;
import java.io.IOException;

public class MusicDatabase {

    private Hashtable<String, ArrayList<PlayableItem>> data;
    private TreeMap<String, ArrayList<PlayableItem>> artists;
    private Recommendation recommender;
    private int size;

    public MusicDatabase() {
        this.data = new Hashtable<>();
        this.artists = new TreeMap<>();
        this.recommender = new Recommendation("UserData.csv");
        this.size = 0;
    }

    public boolean addSongs(File inputFile) {
        if (inputFile == null || !inputFile.exists()) {
            return false;
        }

        try {
            Scanner scanner = new Scanner(inputFile);
            scanner.nextLine();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");

                String name = parts[2];
                String artist = parts[3];
                int duration = Integer.parseInt(parts[4]);
                int popularity = Integer.parseInt(parts[5]);
                String endpoint = parts[7];

                addSongs(name, artist, duration, popularity, endpoint);
            }

            scanner.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void addSongs(String name, String artist, int duration, int popularity,
                         String endpoint) {
        PlayableItem newItem = new PlayableItem(0, duration, endpoint, name, artist, popularity);
        ArrayList<PlayableItem> itemsByName = data.getOrDefault(name, new ArrayList<>());
        boolean a = true;
        for (PlayableItem item : itemsByName) {
            if (item.equals(newItem)) {
                a = false;
            }
        }
        if (a) {
            itemsByName.add(newItem);
            size++;
        }

        ArrayList<PlayableItem> itemsByArtist = artists.getOrDefault(artist, new ArrayList<>());
        a = true;
        for (PlayableItem item : itemsByArtist) {
            if (item.equals(newItem)) {
                item.setPopularity(popularity);
                a = false;
            }
        }
        if (a) {
            itemsByArtist.add(newItem);
        }
        artists.put(artist, itemsByArtist);
        data.put(name, itemsByName);

    }

    public ArrayList<PlayableItem> partialSearchBySongName(String name) {
        ArrayList<PlayableItem> result = new ArrayList<>();
        for (String key : data.keySet()) {
            if (key.contains(name)) {
                result.addAll(data.get(key));
            }
        }
        return result;
    }

    public ArrayList<PlayableItem> partialSearchByArtistName(String name) {
        ArrayList<PlayableItem> result = new ArrayList<>();
        for (String key : artists.keySet()) {
            if (key.contains(name)) {
                result.addAll(artists.get(key));
            }
        }
        result.sort(Comparator.comparingInt(PlayableItem::getPopularity).reversed());
        return result;
    }

    public ArrayList<PlayableItem> searchHighestPopularity(int threshold) {
        ArrayList<PlayableItem> result = new ArrayList<>();
        for (ArrayList<PlayableItem> items : data.values()) {
            for (PlayableItem item : items) {
                if (item.getPopularity() >= threshold) {
                    result.add(item);
                }
            }
        }
        result.sort(Comparator.comparingInt(PlayableItem::getPopularity).reversed());
        return result;
    }

    public ArrayList<PlayableItem> getRecommendedSongs(List<String> fiveArtists) {
        ArrayList<PlayableItem> recommendedSongs = new ArrayList<>();
        String[] recommendedArtists = recommender.recommendNewArtists(fiveArtists);
        for (String artist : recommendedArtists) {
            recommendedSongs.addAll(artists.getOrDefault(artist, new ArrayList<>()));
        }

//        Comparator.comparingInt(PlayableItem::getPopularity)
        recommendedSongs.sort((a, b) -> {
            int i;
            if ((i = Integer.compare(b.getPopularity(), a.getPopularity())) == 0) {
                return a.getTitle().compareTo(b.getTitle());
            }
            return i;
        });

        return new ArrayList<>(recommendedSongs.subList(0, Math.min(recommendedSongs.size(), 10)));
    }

    public int size() {
        return size;
    }
}
