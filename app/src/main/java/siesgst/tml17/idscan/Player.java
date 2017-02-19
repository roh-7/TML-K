package siesgst.tml17.idscan;


public class Player {
    String UID;
    String name;
    String Timestamp;
    String Number;

    Player(String uid, String name, String timestamp, String number) {
        UID = uid;
        this.name = name;
        Timestamp = timestamp;
        this.Number = number;
    }

    Player() {

    }

    void setUID(String uid) {
        this.UID = uid;
    }

    void setName(String name) {
        this.name = name;
    }

    void setTimestamp(String time) {
        this.Timestamp = time;
    }

    void setNumber(String number) {
        this.Number = number;
    }

    String getUID() {
        return this.UID;
    }

    String getName() {
        return this.name;
    }

    String getTimestamp() {
        return this.Timestamp;
    }

    String getNumber() {
        return this.Number;
    }

}
