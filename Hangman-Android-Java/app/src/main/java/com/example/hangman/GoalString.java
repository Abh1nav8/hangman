package com.example.hangman;

class GoalString {
    private String openString; //actual string
    private String closedString; // underscore version

    //constructor
    public GoalString(String openString) {
        this.openString = openString;

        String temp = "";
        for (char letter : openString.toCharArray()) {
            if (Character.isLetter(letter)) {
                temp = temp + "_";
            } else {
                temp = temp + letter;
            }

        }
        this.closedString = temp;
    }

    public String getOpenString() {
        return openString;
    }

    public String getClosedString() {
        return closedString;
    }

    public void setClosedString(String closedString) {
        this.closedString = closedString;
    }

    public float getPercentage(GoalString goalString) {
        String closedString = goalString.getClosedString();
        float stringLength = closedString.length();
        float guessedLength = 0;
        for (int i = 0; i < stringLength; i++) {
            if (closedString.charAt(i) != '_') {
                guessedLength++;
            }
        }
        float percentGuessed = guessedLength / stringLength;
        return percentGuessed;
    }
}
