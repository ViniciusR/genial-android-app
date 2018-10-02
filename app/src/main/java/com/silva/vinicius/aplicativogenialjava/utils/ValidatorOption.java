package com.silva.vinicius.aplicativogenialjava.utils;

import com.silva.vinicius.aplicativogenialjava.enums.TextValidatorTypes;

public class ValidatorOption {

    private TextValidatorTypes type;
    private int value;

    public ValidatorOption(TextValidatorTypes type) {
        this.type = type;
    }

    public ValidatorOption(TextValidatorTypes type, int value) {
        this.type = type;
        this.value = value;
    }

    public TextValidatorTypes getType() {
        return type;
    }

    public void setType(TextValidatorTypes type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
