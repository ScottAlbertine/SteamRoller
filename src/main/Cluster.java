package main;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author Scott Albertine
 */
public class Cluster {

    private HashSet<Person> participants;
    private int plusValue; //cache the plus value to speed things up
    private boolean plusValueDirty; //flag whether the cache is plusValueDirty or not
    private ArrayList<Person> firstNoticedMinusLink;

    public Cluster() {
        participants = new HashSet<Person>();
        plusValue = 0;
        plusValueDirty = false;
        firstNoticedMinusLink = null;
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
        if (this.plusValueDirty) {
            calculatePlusValue();
            this.plusValueDirty = false;
        }
        return plusValue;
    }

    private int mutualPlusses() {
        int total = 0;
        for (Person a : participants) {
            for (Person b : a.plusses) {
                if (b.plusses.contains(a)) {
                    total++;
                }
            }
        }
        return total;
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
        sb.append(" mutualPlusses=");
        sb.append(this.mutualPlusses());
        sb.append(" count=");
        sb.append(this.participants.size());
        return sb.toString();
    }

    public HashSet<Person> getParticipants() {
        return participants;
    }

    public void addParticipant(Person newParticipant) {
        this.participants.add(newParticipant);
        this.plusValueDirty = true;
    }

    public ArrayList<Person> getMinusLink() {
        //set the first found minus link, but only if we haven't found one yet
        if (firstNoticedMinusLink == null) {
            for (Person a : participants) {
                for (Person b : a.minuses) {
                    if (participants.contains(b)) { //we have a problem, branch to resolve it, then compare the branch results and return the best one
                        firstNoticedMinusLink = new ArrayList<Person>();
                        firstNoticedMinusLink.add(a);
                        firstNoticedMinusLink.add(b);
                        return firstNoticedMinusLink;
                    }
                }
            }
        }
        return firstNoticedMinusLink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cluster cluster = (Cluster) o;

        return participants.equals(cluster.participants);

    }

    //THIS ONLY WORKS BECAUSE WE DON'T INSTANTIATE NEW PEOPLE OTHER THAN AT BOOT TIME
    //if it ever changes, you're going to see super fast but very suboptimal results
    @Override
    public int hashCode() {
        int total = 0;
        for (Person person : participants) {
            total += System.identityHashCode(person);
        }
        return total;
    }
}
