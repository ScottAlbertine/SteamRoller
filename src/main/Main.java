package main;

import java.io.*;
import java.util.*;

public class Main {

    static Leaderboard masterList = new Leaderboard();

    //this is so we can create all the people, then link them together by plus/minus
    //if we didn't have this, it'd be much harder to create and follow all the links
    static HashMap<String, Person> people = new HashMap<String, Person>();

    /**
     * This is a map of unsolved to solved clusters, so that we can prune entire branches of the tree rather than repeat a ton of calculations.
     */
    static HashMap<Integer, Cluster> cheatSheet = new HashMap<Integer, Cluster>();
    static int cheats = 0;

    static Random r = new Random();

    public static void main(String[] args) throws IOException {
        //create initial cluster here
        writeFakeDataToClusterFile("/Users/scottalbertine/Desktop/magneto/SteamRoller/sampleCluster.txt");
        Cluster initialCluster = parseClusterFile("/Users/scottalbertine/Desktop/magneto/SteamRoller/sampleCluster.txt");
        Cluster best = solve(initialCluster);
        System.out.println("best:");
        System.out.println(best.toString());
        System.out.println("cheat sheet size: " + cheatSheet.size());
        System.out.println("cheats used: " + cheats);
        System.out.println();
        System.out.println("optimalPerPerson:");
        //sort this, because we can and why not?
        TreeSet<Person> sortedOptimalClusters = new TreeSet<Person>(new Comparator<Person>() {
            @Override
            public int compare(Person o1, Person o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        sortedOptimalClusters.addAll(masterList.optimalClusterPerPerson.keySet());
        for (Person person : sortedOptimalClusters) {
            System.out.println(person.name + ": " + masterList.optimalClusterPerPerson.get(person).toString());
        }
        System.out.println();
        System.out.println("best clusters overall:");
        for (Integer score : masterList.highScores.keySet()) {
            for (Cluster cluster : masterList.highScores.get(score)) {
                System.out.println(cluster.toString());
            }
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
                        Person plus = people.get(name);
                        if(plus != null) { //if they plus someone who's not invited, this'll catch it
                            currentPerson.plusses.add(plus);
                        }
                    }

                    continue;
                }
                if (line.contains("minus:")) { //list of minuses
                    if (currentPerson == null) {
                        throw new RuntimeException("BAD FILE FORMAT, MINUS LIST FOR AN UNNAMED PERSON");
                    }
                    final String[] names = line.replace("minus:", "").split(",");
                    for (String name : names) {
                        Person minus = people.get(name);
                        if(minus != null) { //if they plus someone who's not invited, this'll catch it
                            currentPerson.minuses.add(minus);
                        }
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
        int starterHash = starter.hashCode();
        if (cheatSheet.containsKey(starterHash)) { //skip everything and return the answer if we already know it
            cheats++;
            return cheatSheet.get(starterHash);
        }
        ArrayList<Person> minusLink = starter.getMinusLink();
        if (minusLink == null) { //if no problems, we're good, add to the master solution cache and return for comparison
            masterList.add(starter);
            return starter;
        }
        Person a = minusLink.get(0);
        Person b = minusLink.get(1);
        Cluster optionA = new Cluster(); //if we remove A
        Cluster optionB = new Cluster(); //if we remove B
        for (Person personToCopy : starter.getParticipants()) {
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
            cheatSheet.put(starterHash, solutionA);
            return solutionA;
        } else {
            cheatSheet.put(starterHash, solutionB);
            return solutionB;
        }
    }


    //-----------------
    //TEST CODE
    //-----------------

    //pick a random other person
    //TODO: still have a problem where you can have the same person on both the positive and the negative lists, which is NOT handled in the code but shouldn't cause problems
    //TODO: still have a problem where you can have the same person on a given list twice, which is handled in the code but is still altering our list amounts
    public static String pickOtherPerson(List<String> names, String name) {
        String result = name;
        while (result.equals(name)) {
            result = names.get(r.nextInt(names.size()));
        }
        return result;
    }


    public static void writeFakeDataToClusterFile(String fileName) throws IOException {
        int people = 32;
        int maxPlus = 10;
        int maxMinus = 10;

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
