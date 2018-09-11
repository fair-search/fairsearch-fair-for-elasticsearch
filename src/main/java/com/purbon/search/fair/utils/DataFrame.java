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
        while (position > col1.size() - 1) {
            col1.add(null);
        }
        while (position > col2.size() - 1) {
            col2.add(null);
        }

        col1.set(position, col1Value);
        col2.set(position, col2Value);
    }

    public int getLengthOf(String columnName) {
        if (columnName.equals(column1Name)) {
            return col1.size();
        } else {
            return col2.size();
        }
    }

    public void resolveNullEntries() {
        if (col1.size() == 0 && col2.size() == 0){
            return;
        }
        col1.set(0, 0);
        col2.set(0, 0);
        for (int i = 0; i < col1.size(); i++) {
            if (col1.get(i) == null) {
                for (int j = i; j < col1.size(); j++) {
                    if (col1.get(j) != null) {
                        col1.set(i, col1.get(j));
                        col2.set(i, col2.get(j));
                        col1.remove(j);
                        col2.remove(j);
                        break;
                    }
                }
            }
        }
        for (int k = col1.size() - 1; k >= 0; k--) {
            if (col1.get(k) == null) {
                col1.remove(k);
                col2.remove(k);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(column1Name).append("  ").append(column2Name).append('\n');
        for (int i = 0; i < col1.size(); i++) {
            s.append(i).append("  ").append(col1.get(i)).append("  ").append(col2.get(i));
            s.append('\n');
        }
        return s.toString();
    }

}
