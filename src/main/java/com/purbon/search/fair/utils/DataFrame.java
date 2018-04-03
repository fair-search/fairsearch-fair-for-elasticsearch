package com.purbon.search.fair.utils;

import java.util.ArrayList;

public class DataFrame {


    private String column1Name;
    private ArrayList<Integer> col1;
    private String column2Name;
    private ArrayList<Integer> col2;

    public DataFrame(String column1Name, String column2Name) {
        this.column1Name = column1Name;
        this.column2Name = column2Name;
        this.col1 = new ArrayList<>();
        this.col2 = new ArrayList<>();
    }

    public Integer at(int position, String columnName) {
        if (columnName.equals(column1Name)) {
            return col1.get(position);
        } else {
            return col2.get(position);
        }
    }

    public void put(int position, int col1Value, int col2Value) {
        if (position >= col1.size()) {
            for (int i = 0; i <= position - col1.size() + 1; i++) {
                col1.add(null);
            }
        }
        if (position >= col2.size()) {
            for (int i = 0; i <= position - col2.size() + 1; i++) {
                col2.add(null);
            }
        }
        col1.add(position, col1Value);
        col2.add(position, col2Value);
    }

    public int getLengthOf(String columnName) {
        if (columnName.equals(column1Name)) {
            return col1.size();
        } else {
            return col2.size();
        }
    }

    public void removeNullEntries() {
        for (int i = 0; i < col1.size(); i++) {
            if (col1.get(i) == null) {
                col1.remove(i);
            }
            if (col2.get(i) == null) {
                col2.remove(i);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(column1Name).append("  ").append(column2Name);
        for (int i = 0; i < col1.size(); i++) {
            s.append(i).append("  ").append(col1.get(i)).append("  ").append(col2.get(i));
            s.append('\n');
        }
        return s.toString();
    }

}
