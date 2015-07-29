package main;

import java.io.*;
import java.util.*;

public class Main {

    static Leaderboard masterList = new Leaderboard();

    //this is so we can create all the people, then link them together by plus/minus
    //if we didn't have this, it'd be much harder to create and follow all the links
    static HashMap<String, Person> people = new HashMap<String, Person>();

    static Random r = new Random();

    public static void main(String[] args) throws IOException {
        //create initial cluster here
        writeFakeDataToClusterFile("/Users/scottalbertine/Desktop/magneto/SteamRoller/sampleCluster.txt");
        Cluster initialCluster = parseClusterFile("/Users/scottalbertine/Desktop/magneto/SteamRoller/sampleCluster.txt");
        Cluster best = solve(initialCluster);
        //System.out.println("best:");
        //System.out.println(best.toString());
        System.out.println("optimalPerPerson:");
        for (Person person : masterList.optimalClusterPerPerson.keySet()) {
            System.out.println(person.name + ": " + masterList.optimalClusterPerPerson.get(person).toString());
        }
        System.out.println("best clusters overall:");
        for (Cluster nextBest : masterList.highScores) {
            System.out.println(nextBest.toString());
        }

    }

    public static String superTrim(String s) {
        return s.trim().toLowerCase().replaceAll(" ", "");
    }

    public static Cluster parseClusterFile(String fileName) throws IOException {

        String line = null;

        //run through the file twice.  once to get the names of the official participants, and a second time to link them to each other.
        //the more efficient way to do this would be to store the links in memory separately, but the file is so small that O
        //TODO: this close and open technique is messy as shit, clean it up
        final FileReader fr1 = new FileReader(fileName);
        BufferedReader br1 = new BufferedReader(fr1);
        while ((line = br1.readLine()) != null) {
            line = superTrim(line);
            //it's a non-empty line that doesn't contain a :, assume it's a name
            if (line.length() != 0 && !line.contains(":")) {
                people.put(line, new Person(line));
            }
        }
        fr1.close();

        //now that we have the initial name list, go through and link everyone
        Person currentPerson = null;
        final FileReader fr2 = new FileReader(fileName);
        BufferedReader br2 = new BufferedReader(fr2);
        while ((line = br2.readLine()) != null) {
            line = superTrim(line);
            if (line.length() != 0) { //ignore blank lines
                if (line.contains("plus:")) { //it's a list of plusses, add them to the current person
                    if (currentPerson == null) {
                        throw new RuntimeException("BAD FILE FORMAT, PLUS LIST FOR AN UNNAMED PERSON");
                    }
                    final String[] names = line.replace("plus:", "").split(",");
                    for (String name : names) {
                        currentPerson.plusses.add(people.get(name));
                    }

                    continue;
                }
                if (line.contains("minus:")) { //list of minuses
                    if (currentPerson == null) {
                        throw new RuntimeException("BAD FILE FORMAT, MINUS LIST FOR AN UNNAMED PERSON");
                    }
                    final String[] names = line.replace("minus:", "").split(",");
                    for (String name : names) {
                        currentPerson.minuses.add(people.get(name));
                    }
                    continue;
                }

                if (!line.contains(":")) { //it's a new name, set the current name and current person
                    currentPerson = people.get(line);
                    continue;
                }
            }
        }
        fr2.close();

        Cluster cluster = new Cluster();
        for (Person participant : people.values()) {
            cluster.addParticipant(participant);
        }
        return cluster;
    }

    public static Cluster solve(Cluster starter) {
        final HashSet<Person> starterParticipants = starter.getParticipants();
        for (Person a : starterParticipants) {
            for (Person b : a.minuses) {
                if (starterParticipants.contains(b)) { //we have a problem, branch to resolve it, then compare the branch results and return the best one
                    Cluster optionA = new Cluster(); //if we remove A
                    Cluster optionB = new Cluster(); //if we remove B
                    for (Person personToCopy : starterParticipants) {
                        if (!personToCopy.equals(a)) {
                            optionA.addParticipant(personToCopy);
                        }
                        if (!personToCopy.equals(b)) {
                            optionB.addParticipant(personToCopy);
                        }
                    }

                    Cluster solutionA = solve(optionA);
                    Cluster solutionB = solve(optionB);
                    int aPlusValue = solutionA.plusValue();
                    int bPlusValue = solutionB.plusValue();

                    if (aPlusValue > bPlusValue) {
                        return solutionA;
                    } else {
                        return solutionB;
                    }
                }
            }
        }
        //if no problems, we're good, add to the master solution cache and return for comparison
        masterList.add(starter);
        return starter;
    }


    //-----------------
    //TEST CODE
    //-----------------

    //pick a random other person
    //TODO: still have a problem where you can have the same person on both the positive and the negative lists
    //TODO: still have a problem where you can have the same person on a given list twice
    public static String pickOtherPerson(List<String> names, String name) {
        String result = name;
        while (result.equals(name)) {
            result = names.get(r.nextInt(names.size()));
        }
        return result;
    }



    public static void writeFakeDataToClusterFile(String fileName) throws IOException {
        int people = 20;
        int maxPlus = 10;
        int maxMinus = 5;

        StringBuilder sb = new StringBuilder();
        List<String> names = new ArrayList<String>();
        for (int i = 0; i < people; i++) {
            names.add(Integer.toString(i));
        }
        for (String name : names) {
            sb.append(name);
            sb.append("\nplus:");

            int plusses = r.nextInt(maxPlus);
            for (int j = 0; j < plusses; j++) {
                sb.append(pickOtherPerson(names, name));
                sb.append(",");
            }
            sb.append("\nminus:");
            int minuses = r.nextInt(maxMinus);
            for (int j = 0; j < minuses; j++) {
                sb.append(pickOtherPerson(names, name));
                sb.append(",");
            }
            sb.append("\n\n");
        }


        File outputFile = new File(fileName);
        outputFile.delete();
        outputFile.createNewFile();
        FileWriter writer = new FileWriter(outputFile);
        writer.write(sb.toString());
        writer.flush();
        writer.close();
    }
}
