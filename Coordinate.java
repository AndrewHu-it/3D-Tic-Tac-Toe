public class Coordinate {

    public static final int N = 4;
    public static final int NSquared = N * N;
    public static final int NCubed = N * N * N;

    public static int getX(int position) {
        return position % N;
    }

    public static int getY(int position) {
        return (position / N) % N;
    }

    public static int getZ(int position) {
        return position / NSquared;
    }

    public static int position(int x, int y, int z) {
        return NSquared * z + N * y + x;
    }

    public static String toString(int position){
        int x = getX(position);
        int y = getY(position);
        int z = getZ(position);
        return "Coordinate(x=" + x + ", y=" + y + ", z=" + z + ")";
    }
}