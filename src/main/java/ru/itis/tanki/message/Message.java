package ru.itis.tanki.message;


public class Message {

    public static final int JOIN = 1;
    public static final int INPUT = 2;
    public static final int STATE = 3;
    public static final int MAP = 4;
    public static final int MAP_UPDATE = 5;
    public static final int STATS_REQUEST = 6;
    public static final int STATS_RESPONSE = 7;

    private int type;
    private String data;

    public Message(int type, String data) {
        this.type = type;
        this.data = data;
    }

    public String serialize() {
        return type + "|" + data;
    }

    public static Message deserialize(String str) {
        int split = str.indexOf("|");
        int type = Integer.parseInt(str.substring(0, split));
        String data = str.substring(split + 1);
        return new Message(type, data);
    }

    public int getType() {
        return type;
    }


    public String getData() {
        return data;
    }

}