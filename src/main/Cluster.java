package main;

import java.util.HashSet;

/**
 * @author Scott Albertine
 */
public class Cluster {

    private HashSet<Person> participants;
    private int plusValue; //cache the plus value to speed things up
    private boolean dirty; //flag whether the cache is dirty or not

    public Cluster() {
        participants = new HashSet<Person>();
        plusValue = 0;
        dirty = false;
    }

    private void calculatePlusValue() {
        int total = 0;
        for (Person a : participants) {
            for (Person b : a.plusses) {
                if (participants.contains(b)) {
                    total++;
                }
            }
        }
        this.plusValue = total;
    }

    public int plusValue() {
        if(this.dirty){
            calculatePlusValue();
            this.dirty = false;
        }
        return plusValue;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        for (Person participant : participants) {
            sb.append(participant.name);
            sb.append(",");
        }
        sb.append("} value=");
        sb.append(this.plusValue());
        sb.append(" count=");
        sb.append(this.participants.size());
        return sb.toString();
    }

    public HashSet<Person> getParticipants() {
        return participants;
    }

    public void addParticipant(Person newParticipant) {
        this.participants.add(newParticipant);
        this.dirty = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cluster cluster = (Cluster) o;

        return participants.equals(cluster.participants);

    }

    @Override
    public int hashCode() {
        return participants.hashCode();
    }
}
