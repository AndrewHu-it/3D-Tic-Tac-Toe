import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final Board[] boards = new Board[64];
    private static int current = 0;

    private static class Redo extends Exception {}
    private static class Undo extends Exception {}
    public static class Done extends Exception {}

    private static boolean isValid(int coordinate) {
        return coordinate >= 0 && coordinate <= 3;
    }

    private static int getCoordinate(String prompt) throws Redo, Undo, Done {
        while (true) {
            System.out.print(prompt);
            String s = scanner.nextLine().trim();
            switch (s.toLowerCase()) {
                case "":
                case "redo": // Restart entering this move
                    throw new Redo();

                case "undo": // Go back to previous board position
                    throw new Undo();

                case "done":
                case "exit": // Quit the program
                    throw new Done();

                default:
                    try {
                        int value = Integer.parseInt(s);
                        if (isValid(value)) return value;
                    } catch (NumberFormatException e) {
                    }
                    System.err.println("Invalid coordinate: " + s);
            }
        }
    }

    public static Board getYourMove(Board board) throws Done {

        // Prompt the user to enter a move as XYZ coordinates
        // Check the entry for validity (retry if invalid)
        // Returns an updated board with the user's move.

        // Entering a blank line or "redo" for any coordinate
        // will restart the with entering the X coordinate.

        // Entering "undo" will back up the game state to
        // the previous move entered by the user.

        // Entering "done" will quit the game.

        boards[current++] = board;
        System.out.println("Your move");
        while (true) {
            try {
                int x = getCoordinate("Enter X: ");
                int y = getCoordinate("Enter Y: ");
                int z = getCoordinate("Enter Z: ");
                int position = Coordinate.position(x, y, z);
                if (board.isEmpty(position)) return board.next(position);
                System.err.println("Position is not empty");

            } catch (Redo e) {
                // Try again
                //Should be able to just use the code above no?

            } catch (Undo e) {
                // Revert to previous board configuration
                if (current > 0) {
                    current = current -2;
                    board = boards[current];
                    board.print();
                } else {
                    System.err.println("Start of game:");
                    board.print();
                }
            }
        }
    }

    private static int getMyMove(Board board, RationalAgent rational_agent) {
        int move = rational_agent.getMove(board);
        return move;
    }

    public static void main(String[] args) {
        int[] weights = RationalAgent.weights_1_static;
        Board board = new Board();
        boolean first = true;
        int plies = 4;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-first":
                    first = true;
                    break;

                case "-second":
                    first = false;
                    break;

                case "-plies":
                    try {
                        arg = args[++i];
                        plies = Integer.parseInt(arg);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid value for -plies: " + arg + ". Using default value: 4.");
                    } catch (IndexOutOfBoundsException e) {
                        System.err.println("No value provided for -plies. Using default value: 4.");
                    }
                    break;
                case "-w":
                    try {
                        for (int j = 0; j < weights.length; j++) {
                            weights[j] = Integer.parseInt(args[++i]);
                        }
                    } catch (NumberFormatException | IndexOutOfBoundsException e) {
                        System.err.println("Invalid or missing values for weights. Using default weights.");
                    }
                    break;
                default:
                    System.err.println("Invalid option: " + arg);
            }
        }

        boolean me = first;
        Player player = Player.O; //goes second by defualt.
        if (me == first){
            player = Player.X;
        }
        RationalAgent rational_agent = new RationalAgent(player, plies, weights);


        do {
            if (me) {
                int position = getMyMove(board, rational_agent);
                board = board.next(position);
                System.out.println("My move: " + Coordinate.toString(position));
                board.print();
            } else {
                try {
                    board = getYourMove(board);
                } catch (Done e) {
                    return; // Game abandonned
                }
            }
            me = !me;
        } while (!board.done());

        switch (board.winner()) {
            case X:
                System.out.println(first ? "I won" : "You won");
                System.out.println(board.winningLine(Player.X));
                break;

            case O:
                System.out.println(first ? "You won" : "I won");
                System.out.println(board.winningLine(Player.O));
                break;

            default:
                System.out.println("Tie");
                break;
        }
    }
}
