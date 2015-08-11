package main;

import java.util.*;

/**
 * so, this is going to use insertion sort because we want a limited size and it's the quickest way given that these are only inserted once and never modified
 *
 * @author Scott Albertine
 */
public class Leaderboard {
    TreeMap<Integer, HashSet<Cluster>> highScores; //so, we want this to have very fast insertion sorting by plus value, but for a given plus value we may have multiple different clusters, and we want deduplication of clusters to be by their participant list, not by their plus value, so you have to do this monstrosity of a data structure
    HashMap<Person, Cluster> optimalClusterPerPerson;
    int maxListLength = 10;

    public Leaderboard() {
        highScores = new TreeMap<Integer, HashSet<Cluster>>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });
        optimalClusterPerPerson = new HashMap<Person, Cluster>();
    }

    public void add(Cluster newCluster) {

        int newPlusValue = newCluster.plusValue();

        //update optimal clusters
        for (Person person : newCluster.getParticipants()) {
            Cluster existingOptimalCluster = optimalClusterPerPerson.get(person);
            if (existingOptimalCluster == null) {
                optimalClusterPerPerson.put(person, newCluster);
            } else if (newPlusValue > existingOptimalCluster.plusValue()) {
                optimalClusterPerPerson.put(person, newCluster);
            }
        }

        //then add to the master high score list
        if (highScores.get(newPlusValue) == null) {
            highScores.put(newPlusValue, new HashSet<Cluster>());
        }
        highScores.get(newPlusValue).add(newCluster);
        //making this a while is kind of redundant, but I'm doing so just in case we get a bunch of extra additions
        while (highScores.size() > maxListLength) {
            highScores.remove(highScores.lastKey()); //first is the lowest score in the set
        }
    }
}
