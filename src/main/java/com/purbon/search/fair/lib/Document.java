package com.purbon.search.fair.lib;

import java.util.Random;

public class Document {

    public float score() {
        return 1.0f;
    }

    public boolean isProtected() {
        return new Random(System.currentTimeMillis()).nextInt(2) == 1;
    }
}
