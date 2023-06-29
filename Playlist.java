import java.util.Stack;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashSet;
import java.util.Collections;
import java.util.stream.Collectors;

public class Playlist {

    private String name;
    private int playingMode = 0;
    private int playingIndex = -1;
//    private ArrayList<PlayableItem> curList;
    private PlayableItem cur;
    private Stack<PlayableItem> history;
    private PriorityQueue<PlayableItem> freqListened;
    private PriorityQueue<PlayableItem> freqListenedCopy;
    private ArrayList<PlayableItem> playlist;
    private boolean recommended;
    private boolean played;

    private Random rng = new Random(42);

    public Playlist() {
        this.playlist = new ArrayList<>();
        this.history = new Stack<>();
        this.freqListened = new PriorityQueue<>((a, b) -> {
            int i;
            if ((i = a.compareTo(b)) == 0) {
                return Integer.compare(playlist.indexOf(b), playlist.indexOf(a));
            }
            return i;
        });
        this.name = "Default";
    }

    public Playlist(String name) {
        this();
        setName(name);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int size() {
        return this.playlist.size();
    }

    public String toString() {
        return this.name + "," + this.playlist.size() + " songs";
    }

    public void addPlayableItem(PlayableItem newItem) {
        playlist.add(newItem);
//        if (playlist.size() == 1) {
//            cur = getNextPlayable();
//        }

        // add to history
//        history.push(newItem);

        freqListened.offer(newItem);
        if (playingMode == 2) {
            freqListenedCopy.offer(newItem);
        }


        // set current song to the newly added song
//        cur = newItem;
    }

    public void addPlayableItem(ArrayList<PlayableItem> newItem) {
        if (playingMode == 3 && !recommended) {
            recommended = true;
            newItem.sort(PlayableItem::compareTo);
//            playingIndex = playlist.size();
            playlist.addAll(newItem);
            freqListened.addAll(newItem);
//            cur = playlist.get(playingIndex);
            return;
        }
        for (PlayableItem item : newItem) {
            addPlayableItem(item);
        }
    }

    public boolean removePlayableItem(int number) {
        // Check if the number is valid
        if (number <= 0 || number > playlist.size()) {
            return false;
        }

        // Get the PlayableItem to remove
        PlayableItem toRemove = playlist.get(number - 1);

        if (cur != null && toRemove.equals(cur)) {
            playingIndex--;
//            getNextPlayable();
        }

        // Remove the item from the playlist
        playlist.remove(number - 1);

        // Remove the item from the history stack
        history.remove(toRemove);

        // Remove the item from the frequency list
        freqListened.remove(toRemove);
        if (playingMode == 2) {
            freqListenedCopy.remove(toRemove);
        }
//        bin.add(toRemove);

        return true;
    }

    public void switchPlayingMode(int newMode) {
        // Clear the history and frequency list for all modes except normal mode
        history.clear();
        switch (newMode) {
            case 1:
//                Collections.shuffle(curList = new ArrayList<>(playlist));
            case 0:
                playingIndex = -1;
                break;
            case 2:
                freqListenedCopy = new PriorityQueue<>((a, b) -> {
                    int i;
                    if ((i = a.compareTo(b)) == 0) {
                        return Integer.compare(playlist.indexOf(b), playlist.indexOf(a));
                    }
                    return i;
                });
                freqListenedCopy.addAll(playlist);
                played = false;
                break;
            case 3:
                recommended = false;
//                playingIndex = playlist.size() - 1;
                break;
            default:
                throw new IllegalArgumentException();
        }
        playingMode = newMode;
    }

    public ArrayList<String> getFiveMostPopular() {
        PlayableItem last = null;
        HashSet<String> artists = new HashSet<>();
        for (PlayableItem item : freqListened.stream().sorted().collect(Collectors.toList())) {
            if (last != null && item.compareTo(last) == 0) {
                artists.add(item.getArtist());
            } else {
                if (artists.size() >= 5) {
                    break;
                }
                artists.add(item.getArtist());
                last = item;
            }
        }
        ArrayList<String> result = new ArrayList<>(artists);
        Collections.sort(result);
        return new ArrayList<>(result.subList(0, Math.min(5, result.size())));
    }

    /**
     * Go to the last playing item
     */
    public void goBack() {
        // Check if there is any item to go back to
        if (history.isEmpty()) {
            System.out.println("No more step to go back");
            return;
        }

        // Pop the top item from the stack
        cur = history.pop();

        // Update the playing index to the current item
        playingIndex = playlist.indexOf(cur) - 1;
    }

    public void play(int seconds) {
        int remaining = seconds;
        if (playingIndex == -1) {
            cur = getNextPlayable();
        }
        while (cur != null) {
            System.out.println("Seconds " + (seconds - remaining) + " : " + cur.getTitle()
                    + " start.");
            while (remaining-- > 0) {
                if (!cur.play()) {
                    System.out.println("Seconds " + (seconds - remaining) + " : "
                            + cur.getTitle() + " complete.");
                    cur = getNextPlayable();
                    if (cur != null) {
                        freqListened.remove(cur);
                        freqListened.offer(cur);
                        history.push(cur);
                    }
                    break;
                }

            }
            if (remaining <= 0) {
                return;
            }
        }
        System.out.println("No more music to play.");
    }

    public String showPlaylistStatus() {
        String result = "";
        for (int i = 0; i < playlist.size(); i++) {
            PlayableItem item = playlist.get(i);
            String itemString = i + 1 + ". " + item.toString();
            if (i == playingIndex) {
                itemString += " - Currently play";
            }
            if (i < playlist.size() - 1) {
                itemString += "\n";
            }
            result += itemString;
        }
        return result;
    }

    public PlayableItem getNextPlayable() {
        switch (playingMode) {
            case 3:
//                if (!curList.isEmpty()) {
//                    return curList.poll();
//                }
            case 0:
                if (++playingIndex == playlist.size()) {
                    return null;
                }
                return playlist.get(playingIndex);
            case 1:
                playingIndex = rng.nextInt(playlist.size());
                return playlist.get(playingIndex);
            case 2:
//                if (!played) {
//                    played = true;
//                    if (++playingIndex == playlist.size()) {
//                        return null;
//                    }
//                    PlayableItem temp = playlist.get(playingIndex);
//                    freqListenedCopy.remove(temp);
//                    return temp;
//                }
                return freqListenedCopy.poll();
            default:
                throw new IllegalArgumentException();
        }
    }
}
