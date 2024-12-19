import java.util.Iterator;
import java.util.NoSuchElementException;

public class Board implements Iterable<Board> {

    public class InvalidBoardStateException extends RuntimeException {
        public InvalidBoardStateException(String message) {
            super(message);
        }
    }

    public static final int N = Coordinate.N;
    long boardX;
    long boardO;

    public Board() {
        this.boardX = 0L;
        this.boardO = 0L;
    }

    public Board(long boardX, long boardO){
        this.boardX = boardX;
        this.boardO = boardO;
    }

    public Player get(int position) {
        if (Bit.isSet(this.boardX, position)) return Player.X;
        if (Bit.isSet(this.boardO, position)) return Player.O;
        return Player.EMPTY;
    }

    public long getBoard(Player player){
        if (player == Player.X){
            return this.boardX;
        } else {
            return this.boardO;
        }
    }

    public Player get(int x, int y, int z) {
        return this.get(Coordinate.position(x, y, z));
    }

    public void set(int position, Player player) {
        if (player == Player.X){
            boardX = Bit.set(boardX, position);
            boardO = Bit.clear(boardO, position);
        } else if (player == Player.O){
            boardO = Bit.set(boardO, position);
            boardX = Bit.clear(boardX, position);
        } else { // Player.EMPTY
            boardX = Bit.clear(boardX, position);
            boardO = Bit.clear(boardO, position);
        }
    }


    public boolean isEmpty(int position){
        if (this.get(position) == Player.EMPTY){
            return true;
        }
        return false;
    }

    public Board next(int position){
        Player player_turn = this.turn();
        Board next = new Board(this.boardX, this.boardO);
        next.set(position,player_turn);
        return next;
    }

    public boolean done(){
        return this.winner() != null;
    }


    public Player turn(){
        int num_chips_X = Bit.countOnes(boardX);
        int num_chips_O = Bit.countOnes(boardO);
        int difference = num_chips_X - num_chips_O;
        if (difference == 0 ){
            return Player.X;
        }
        if (difference == 1){
            return Player.O;
        }
        if (difference < 0){
            throw new InvalidBoardStateException("Too many Os");
        }
        if (difference > 1){
            throw new InvalidBoardStateException("Too many Xs");
        }
        return null;
    }

    public Player winner(){
        for (Line line : Line.lines){
            if (Bit.contains_line(boardX, line)) {
                return Player.X;
            }
            if (Bit.contains_line(boardO, line)) {
                return Player.O;
            }
        }
        return null; //Returning this means that neither has won.
    }

    public Line winningLine(Player player){
        long board = 0L;
        if (player == Player.X) board = boardX;
        if (player == Player.O) board = boardO;
        for (Line line : Line.lines){
            if (Bit.contains_line(board, line)) return line;
        }
        return null;
    }


    public int move(Board b) {
        long diff;
        if (turn() == Player.X) {
            diff = this.boardX ^ b.boardX;
        } else {
            diff = this.boardO ^ b.boardO;
        }
        if (Bit.countOnes(diff) != 1) {
            throw new IllegalArgumentException("Boards are not consecutive states.");
        }
        return Bit.leadingOne(diff);
    }

    public static Board valueOf(String s) {
        Board board = new Board();
        int position = 0;

        for (int i= 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case 'x':
                case 'X':
                    board.set(position++, Player.X);
                    break;

                case 'o':
                case 'O':
                    board.set(position++, Player.O);
                    break;

                case '.':
                    board.set(position++, Player.EMPTY);
                    break;

                case ' ':
                case '|':
                    break;

                default:
                    throw new IllegalArgumentException("Invalid player: " + c);
            }
        }
        return board;
    }


    @Override
    public Iterator<Board> iterator() {
        return new BoardIterator();
    }


    //TODO: Iterator might be returning null value.
    public class BoardIterator implements Iterator<Board>{
        private int nextPosition;

        public BoardIterator() {
            if (Board.this.done()) {
                nextPosition = -1;
            } else {
                nextPosition = -1;
                advanceToNext();
            }
        }

        private void advanceToNext() {
            int totalPositions = 64;
            nextPosition++;
            while (nextPosition < totalPositions) {
                if (Board.this.isEmpty(nextPosition)) {
                    break;
                }
                nextPosition++;
            }
            if (nextPosition >= totalPositions) {
                nextPosition = -1; // No more positions
            }
        }

        @Override
        public boolean hasNext() {
            return nextPosition != -1;
        }


        @Override
        public Board next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Board newBoard = Board.this.next(nextPosition);
            advanceToNext();
            return newBoard;
        }
    }


    // Image & printing functions.

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String separator = "";

        for (int position = 0; position < 64; position++) {
            result.append(separator);
            result.append(this.get(position).toString());
            if ((position +1 ) % 16 == 0) {
                separator = " | ";
            } else if ((position +1) % 4 == 0) {
                separator = " ";
            } else {
                separator = "";
            }
        }
        return result.toString();
    }

    public void print() {
        for (int y = N-1; y >= 0; y--) {
            for (int z = 0; z < N; z++) {
                for (int x = 0; x < N; x++) {
                    System.out.print(this.get(x, y, z));
                }
                System.out.print("    ");
            }
            System.out.println();
        }
    }
}