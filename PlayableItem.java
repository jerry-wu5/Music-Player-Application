/**
 * <b>May not add any accessor/mutator for this class</b>
 */
public class PlayableItem implements Comparable<PlayableItem> {
    private int lastPlayedTime;
    private int totalPlayTime;
    private String endpoint;
    private String title;
    private String artist;
    private int popularity;
    private int playedCounts; // How many times this song has been played, initially to be 0

    public PlayableItem(int lastPlayedTime, int totalPlayTime, String endpoint,
                        String title, String artist, int popularity) {
        this.lastPlayedTime = lastPlayedTime;
        this.totalPlayTime = totalPlayTime;
        this.endpoint = endpoint;
        this.title = title;
        this.artist = artist;
        this.popularity = popularity;
        this.playedCounts = 0;

    }

    public String getArtist() {
        return this.artist;
    }

    public String getTitle() {
        return this.title;
    }

    public int getPopularity() {
        return this.popularity;
    }

    public void setPopularity(int pop) {
        this.popularity = pop;
    }

    public boolean playable() {
        if (this.totalPlayTime - this.lastPlayedTime == 0) {
            return false;
        }
        return true;
    }

    public boolean play() {
        this.lastPlayedTime++;
        if (!playable()) {
            this.playedCounts++;
            this.lastPlayedTime = 0;
            return false;
        }
        return true;
    }

    public boolean equals(PlayableItem another) {
        boolean a = this.title.equals(another.getTitle());
        boolean b = this.artist.equals(another.getArtist());
        boolean c = (this.totalPlayTime == another.totalPlayTime);
        boolean d = (this.endpoint.equals(another.endpoint));
        return (a && b) && (c && d);
    }

    public String toString() {
        return this.title + "," + this.endpoint + "," + this.lastPlayedTime + ","
                + this.totalPlayTime + "," + this.artist + "," + this.popularity + ","
                + this.playedCounts;
    }

    public int compareTo(PlayableItem o) {
        return Integer.compare(o.playedCounts, this.playedCounts);
    }
}
