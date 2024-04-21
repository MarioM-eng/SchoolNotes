package org.schools.databaselogic;

public class NonexistentEntityException extends Exception{
    public NonexistentEntityException() {
        super();
    }

    public NonexistentEntityException(String message) {
        super(message);
    }
}
