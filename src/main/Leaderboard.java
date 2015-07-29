package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * so, this is going to use insertion sort because we want a limited size and it's the quickest way given that these are only inserted once and never modified
 *
 * @author Scott Albertine
 */
public class Leaderboard {
    List<Cluster> highScores;
    HashMap<Person, Cluster> optimalClusterPerPerson;
    int maxListLength = 30;

    public Leaderboard() {
        highScores = new ArrayList<Cluster>();
        optimalClusterPerPerson = new HashMap<Person, Cluster>();
    }

    public void add(Cluster newCluster) {

        //update optimal clusters
        for (Person person : newCluster.getParticipants()) {
            Cluster existingOptimalCluster = optimalClusterPerPerson.get(person);
            if (existingOptimalCluster == null) {
                optimalClusterPerPerson.put(person, newCluster);
            } else if (newCluster.plusValue() > existingOptimalCluster.plusValue()) {
                optimalClusterPerPerson.put(person, newCluster);
            }

        }

        //then add to the master high score list
        int newScore = newCluster.plusValue();
        if(highScores.isEmpty()){
            highScores.add(newCluster);
            return;
        }
        for (int i = 0; i < highScores.size(); i++) {
            Cluster existingCluster = highScores.get(i);
            if(existingCluster.equals(newCluster)){
                break; //no duplicates in the high score list, thank you
            }
            if (newScore > existingCluster.plusValue()) {
                highScores.add(i, newCluster);
                break;
            }
        }
        //making this a while is kind of redundant, but I'm doing so just in case we get a bunch of extra additions
        while(highScores.size() > maxListLength){
            highScores.remove(maxListLength);
        }
    }
}
