package com.medua.apostlesbridgenext.config;

import com.medua.apostlesbridgenext.types.IgnoredType;

public class Ignored {

    String name;
    IgnoredType type;

    public Ignored(String name, IgnoredType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IgnoredType getType() {
        return type;
    }

    public void setType(IgnoredType type) {
        this.type = type;
    }
}
