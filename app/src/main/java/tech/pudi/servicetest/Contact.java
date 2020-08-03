package tech.pudi.servicetest;

import java.util.ArrayList;
import java.util.HashMap;

public class Contact {
    private String id;
    private String name;
    private ArrayList<String> numbers;
    private HashMap<String,String> rawContactIdMap;
    public Contact(String id, String name, ArrayList<String> numbers, HashMap<String, String> rawContactIdMap)
    {
        this.numbers=numbers;
        this.id=id;
        this.name=name;
        this.rawContactIdMap=rawContactIdMap;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getNumbers() {
        return numbers;
    }

    public void setNumbers(ArrayList<String> numbers) {
        this.numbers = numbers;
    }

    public HashMap<String, String> getRawContactIdMap() {
        return rawContactIdMap;
    }

    public void setRawContactIdMap(HashMap<String, String> rawContactIdMap) {
        this.rawContactIdMap = rawContactIdMap;
    }
}
