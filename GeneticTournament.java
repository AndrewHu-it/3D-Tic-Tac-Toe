import java.util.Random;
import java.util.Arrays;

public class GeneticTournament {

    //Statistics:

    private static int totalGames = 0;
    private static int num_won_first = 0;


    private static final int AGENT_COUNT_PER_BRACKET = 8;
    private static final int BRACKET_COUNT = 4;
    private static final int GENERATIONS = 20;
    private static final int WEIGHT_LENGTH = 8;

    private static final int RANDOM_AGENTS_PER_GENERATION = 4; // How many random agents per generation
    private static final int RANDOM_AGENT_MIN_WEIGHT = 0;
    private static final int RANDOM_AGENT_MAX_WEIGHT = 300000;



    private static int[][] savedBracketWinners = new int[GENERATIONS * BRACKET_COUNT][WEIGHT_LENGTH];
    private static int savedIndex = 0;

    private static Random rand = new Random();




    public static RationalAgent play_game(RationalAgent agent_one, RationalAgent agent_two) {

        System.out.println("Agent One (X): " + Arrays.toString(agent_one.getWeights())
                + "\nAgent Two (O): " + Arrays.toString(agent_two.getWeights()));

        // Ensure the agents have the correct moves.
        RationalAgent xAgent = new RationalAgent(Player.X, agent_one.getWeights());
        RationalAgent oAgent = new RationalAgent(Player.O, agent_two.getWeights());

        Board board = new Board();
        Player currentPlayer = Player.X;

        while (!board.done()) {
            int move;
            try {
                if (currentPlayer == Player.X) {
                    move = xAgent.getMove(board);
                } else {
                    move = oAgent.getMove(board);
                }

                board = board.next(move);
                System.out.println("=============================");
                board.print();
                System.out.println("=============================");
                currentPlayer = currentPlayer.other();
            } catch (Exception e) {
                System.out.println("Error occurred for " + currentPlayer + " agent. Opponent wins by default.");
                if (currentPlayer == Player.X) {
                    return agent_two; // Agent two (O) wins
                } else {
                    return agent_one; // Agent one (X) wins
                }
            }
        }

        Player winner = board.winner();

        RationalAgent gameWinnerAgent;
        if (winner == Player.X) {
            gameWinnerAgent = agent_one;
            num_won_first++;
        } else if (winner == Player.O) {
            gameWinnerAgent = agent_two;
        } else {
            gameWinnerAgent = agent_two;
        }

        if (winner == Player.X) {
            System.out.println("Winner: Agent One (X) " + Arrays.toString(gameWinnerAgent.getWeights()) + " \n");
        } else if (winner == Player.O) {
            System.out.println("Winner: Agent Two (O) " + Arrays.toString(gameWinnerAgent.getWeights()) + " \n");
        } else {
            System.out.println("Tie, Winner by default: Agent Two " + Arrays.toString(gameWinnerAgent.getWeights()) + " \n");
        }

        totalGames++;

        return gameWinnerAgent;
    }



    public static void main(String[] args) {

        RationalAgent[][] population = new RationalAgent[BRACKET_COUNT][AGENT_COUNT_PER_BRACKET];

        for (int i = 0; i < BRACKET_COUNT; i++) {
            for (int j = 0; j < AGENT_COUNT_PER_BRACKET; j++) {
                int[] initialWeights = randomWeights();
                // By default, let's choose Player.X for all. In a real scenario,
                // we might randomize or alternate.
                population[i][j] = new RationalAgent(Player.X, initialWeights);
            }
        }

        RationalAgent bestAgentSoFar = null;
        int bestScoreSoFar = Integer.MIN_VALUE;

        for (int gen = 1; gen <= GENERATIONS; gen++) {
            if(totalGames != 0){
                System.out.println("Percent of games won when going first: " + (double )num_won_first/ (double)totalGames );

            }
            System.out.println("=== GENERATION " + gen + " ===");

            RationalAgent[] genWinners = new RationalAgent[BRACKET_COUNT];

            for (int b = 0; b < BRACKET_COUNT; b++) {
                System.out.println("---- Bracket " + (b+1) + " of Generation " + gen + " ----");

                RationalAgent[] bracketAgents = population[b];

                // Round 1: 8 -> 4
                RationalAgent[] round2 = new RationalAgent[4];
                for (int i = 0; i < 4; i++) {
                    round2[i] = play_game(bracketAgents[i*2], bracketAgents[i*2+1]);
                }

                RationalAgent[] round3 = new RationalAgent[2];
                for (int i = 0; i < 2; i++) {
                    round3[i] = play_game(round2[i*2], round2[i*2+1]);
                }

                RationalAgent bracketWinner = play_game(round3[0], round3[1]);

                System.out.println("Bracket Winner (Generation " + gen + ", Bracket " + (b+1) + "): "
                        + Arrays.toString(bracketWinner.getWeights()));

                System.arraycopy(bracketWinner.getWeights(), 0, savedBracketWinners[savedIndex], 0, WEIGHT_LENGTH);
                savedIndex++;

                genWinners[b] = bracketWinner;
            }

            // Evaluate the bracket winners to find the best in this generation
            // Let's use sum of weights as a measure of "goodness" again, or we can keep track
            // from the actual matches.
            for (RationalAgent winner : genWinners) {
                int score = sumArray(winner.getWeights());
                if (score > bestScoreSoFar) {
                    bestScoreSoFar = score;
                    bestAgentSoFar = winner;
                }
            }

            // Create next generation from these 4 winners
            population = createNextGeneration(genWinners);
        }

        // After all generations, specify the best agent
        System.out.println("=== END OF ALL GENERATIONS ===");
        System.out.println("Best parameters found: " + Arrays.toString(bestAgentSoFar.getWeights()));
    }




    ///---------ASSUME THAT THIS ALL WORKS FINE-----------


    // In the createNextGeneration method, after we fill the new population normally,
// we will replace a few agents in random places with totally random parameter agents.

    private static RationalAgent[][] createNextGeneration(RationalAgent[] parents) {
        RationalAgent[][] newPopulation = new RationalAgent[BRACKET_COUNT][AGENT_COUNT_PER_BRACKET];

        for (int i = 0; i < BRACKET_COUNT; i++) {
            newPopulation[i][0] = copyAgent(parents[i]);
        }

        int childCountPerBracket = AGENT_COUNT_PER_BRACKET - 1;
        for (int i = 0; i < BRACKET_COUNT; i++) {
            int fillIndex = 1;
            for (int c = 0; c < childCountPerBracket; c++) {
                int mateIndex = rand.nextInt(BRACKET_COUNT);
                while (mateIndex == i) {
                    mateIndex = rand.nextInt(BRACKET_COUNT);
                }

                RationalAgent child = mate(parents[i], parents[mateIndex]);
                newPopulation[i][fillIndex++] = child;
            }
        }


        for (int r = 0; r < RANDOM_AGENTS_PER_GENERATION; r++) {
            int bracketIndex = rand.nextInt(BRACKET_COUNT);
            int agentIndex = rand.nextInt(AGENT_COUNT_PER_BRACKET);
            // Generate random weights
            int[] randomWeights = new int[WEIGHT_LENGTH];
            for (int w = 0; w < WEIGHT_LENGTH; w++) {
                randomWeights[w] = rand.nextInt(RANDOM_AGENT_MAX_WEIGHT - RANDOM_AGENT_MIN_WEIGHT + 1)
                        + RANDOM_AGENT_MIN_WEIGHT;
            }
            newPopulation[bracketIndex][agentIndex] = new RationalAgent(Player.X, randomWeights);
        }


        return newPopulation;
    }


    // The mating function:
    // We do:
    // 1. Single point crossover:
    //    Choose a random index for crossover;
    //    child's weights from 0 to crossoverIndex come from parentA,
    //    and the rest come from parentB.
    // 2. Random mutation with small probability:
    //    With probability 10%, mutate one random weight by a small amount.
    private static RationalAgent mate(RationalAgent parentA, RationalAgent parentB) {
        int[] pA = parentA.getWeights();
        int[] pB = parentB.getWeights();
        int[] childWeights = new int[WEIGHT_LENGTH];

        int crossoverPoint = rand.nextInt(WEIGHT_LENGTH);
        for (int i = 0; i < WEIGHT_LENGTH; i++) {
            if (i <= crossoverPoint) {
                childWeights[i] = pA[i];
            } else {
                childWeights[i] = pB[i];
            }
        }

        // Mutation
        double mutationChance = 0.1;
        if (rand.nextDouble() < mutationChance) {
            int idx = rand.nextInt(childWeights.length);
            double mutationFactor = 1.0 + ((rand.nextDouble() * 0.2) - 0.1); // Range: 0.9 to 1.1
            // Apply the mutation and ensure the value remains at least 1
            childWeights[idx] = Math.max(1, (int) Math.round(childWeights[idx] * mutationFactor));
        }
        return new RationalAgent(Player.X, childWeights);
    }





    private static RationalAgent copyAgent(RationalAgent agent) {
        int[] w = agent.getWeights();
        int[] copied = Arrays.copyOf(w, w.length);
        return new RationalAgent(Player.X, copied);
    }

    private static int[] randomWeights() {
        int[] w = new int[RationalAgent.weights_1_static.length];
        for (int i = 0; i < RationalAgent.weights_1_static.length; i++) {
            // Generate a random multiplier between 0.25 and 5.0
            double multiplier = 0.25 + (rand.nextDouble() * (5.0 - 0.25));

            w[i] = (int) Math.round(RationalAgent.weights_1_static[i] * multiplier);
        }
        return w;
    }

    private static int sumArray(int[] arr) {
        int sum = 0;
        for (int val : arr) sum += val;
        return sum;
    }
}
