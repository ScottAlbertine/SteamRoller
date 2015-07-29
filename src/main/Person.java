package main;

import java.util.HashSet;

/**
 * @author Scott Albertine
 */
public class Person {
    public String name;
    public HashSet<Person> plusses;
    public HashSet<Person> minuses;

    public Person(String name) {
        this.name = name;
        this.plusses = new HashSet<Person>();
        this.minuses = new HashSet<Person>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        return name.equals(person.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
