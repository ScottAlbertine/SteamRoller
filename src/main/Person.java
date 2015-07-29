package main;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Scott Albertine
 */
public class Person {
    public String name;
    public List<Person> plusses;
    public List<Person> minuses;

    public Person(String name) {
        this.name = name;
        this.plusses = new ArrayList<Person>();
        this.minuses = new ArrayList<Person>();
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
