package io.tab4u.tab4u.dto;

public class TablatureResponse {
    public String jsonArray;
    public String[][] tablature;

    public TablatureResponse(String jsonArray, String[][] tablature) {
        this.jsonArray = jsonArray;
        this.tablature = tablature;
    }
}
