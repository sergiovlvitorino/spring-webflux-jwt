package com.sergiovitorino.springwebfluxjwt.application.command;

public record FindAllCommand(int page, int size) {
    public FindAllCommand() {
        this(0, 20);
    }
}
