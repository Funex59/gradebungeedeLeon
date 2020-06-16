package de.leon.gradebungee.utils;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UserNotFoundException extends Throwable {

    public UserNotFoundException(String message) {
        super(message);
    }
}
