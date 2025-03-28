import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

class Instance {
    private int id;
    private Party party;
    private boolean active;

    public Instance(int id) {
        this.id = id;
        this.active = false;
    }

    public synchronized void assignParty(Party party) {
        this.party = party;
        this.active = true;
    }

    public synchronized void releaseInstance() {
        this.party = null;
        this.active = false;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return "Instance " + id + " is " + (active ? "active with Party " + party.getPartyId() : "empty");
    }
}

class Party implements Runnable {
    private int id;
    int duration;
    private Instance assignedInstance;

    public Party(int id, int duration, Instance assignedInstance) {
        this.id = id;
        this.duration = duration;
        this.assignedInstance = assignedInstance;
    }

    public int getPartyId() {
        return id;
    }

    @Override
    public void run() {
        try {
            System.out.println("Party " + id + " is running for " + duration + " seconds...");
            Thread.sleep(duration * 1000);
            assignedInstance.releaseInstance();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class DungeonManager {
    public static void main(String[] args) {
        try {
            File file = new File("input1.txt");
            Scanner scanner = new Scanner(file);
            Map<String, Integer> inputs = new HashMap<>();

            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(" = ");
                if (parts.length == 2) {
                    try {
                        long value = Long.parseLong(parts[1].trim());
                        if (value <= 0 || value > Integer.MAX_VALUE) {
                            throw new IllegalArgumentException("Invalid input: Values must be positive integers and within range.");
                        }
                        inputs.put(parts[0].trim(), (int) value);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid input: Non-numeric values are not allowed.");
                    }
                }
            }
            scanner.close();

            int n = inputs.getOrDefault("n", -1);
            int t = inputs.getOrDefault("t", -1);
            int h = inputs.getOrDefault("h", -1);
            int d = inputs.getOrDefault("d", -1);
            int t1 = inputs.getOrDefault("t1", -1);
            int t2 = inputs.getOrDefault("t2", -1);

            if (n == -1 || t == -1 || h == -1 || d == -1 || t1 == -1 || t2 == -1) {
                throw new IllegalArgumentException("Missing required input values.");
            }
            if (t2 > 15) {
                throw new IllegalArgumentException("t2 cannot be greater than 15.");
            }
            if (t1 > 15) {
                throw new IllegalArgumentException("t1 cannot exceed 15.");
            }
            if (t2 <= t1) {
                throw new IllegalArgumentException("t2 must be greater than t1.");
            }

            List<Instance> instances = new ArrayList<>();
            for (int i = 1; i <= n; i++) {
                instances.add(new Instance(i));
            }

            System.out.println("Initial Instance Status:");
            for (Instance instance : instances) {
                System.out.println(instance);
            }
            System.out.println();

            List<String> tanks = new ArrayList<>(), healers = new ArrayList<>(), dps = new ArrayList<>();
            for (int i = 1; i <= t; i++) tanks.add("Tank " + i);
            for (int i = 1; i <= h; i++) healers.add("Healer " + i);
            for (int i = 1; i <= d; i++) dps.add("DPS " + i);

            System.out.println("Available Tanks: " + String.join(", ", tanks));
            System.out.println("Available Healers: " + String.join(", ", healers));
            System.out.println("Available DPS: " + String.join(", ", dps));
            System.out.println();

            List<Party> parties = new ArrayList<>();
            int partyId = 1;
            while (t >= 1 && h >= 1 && d >= 3) {
                int duration = new Random().nextInt(t2 - t1 + 1) + t1;
                Party party = new Party(partyId, duration, instances.get((partyId - 1) % n));
                parties.add(party);
                partyId++;
                t--; h--; d -= 3;
            }

            System.out.println("Formed Parties:");
            for (int i = 0; i < parties.size(); i++) {
                Party p = parties.get(i);
                System.out.println("Party " + p.getPartyId() + ": " + tanks.remove(0) + ", " + healers.remove(0) + ", " + dps.remove(0) + ", " + dps.remove(0) + ", " + dps.remove(0));
            }
            System.out.println();

            if (!tanks.isEmpty()) System.out.println("Unmatched Tanks: " + String.join(", ", tanks));
            if (!healers.isEmpty()) System.out.println("Unmatched Healers: " + String.join(", ", healers));
            if (!dps.isEmpty()) System.out.println("Unmatched DPS: " + String.join(", ", dps));
            System.out.println();

            System.out.println("Current Instance Status:");
            for (int i = 0; i < parties.size(); i++) {
                Instance instance = instances.get(i % n);
                instance.assignParty(parties.get(i));
                System.out.println(instance);
            }
            System.out.println();

            List<Thread> threads = new ArrayList<>();
            for (Party party : parties) {
                Thread thread = new Thread(party);
                threads.add(thread);
                thread.start();
            }
            for (Thread thread : threads) {
                thread.join();
            }

            System.out.println("\nSummary:");
            System.out.println("Total parties served: " + parties.size());
            int totalTime = parties.stream().mapToInt(p -> p.duration).sum();
            System.out.println("Total time served: " + totalTime + " seconds");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
