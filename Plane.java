public class Plane {

    private long positions;
    private String name;

    private Plane(String name) {
        this.positions = 0;
        this.name = name;
    }

    private Plane(long positions) {
        this.positions = positions;
        this.name = "";
    }

    private Plane(){
        this.positions = 0;
        this.name = "";
    }

    public long positions() {
        return this.positions;
    }

    public String name() {
        return this.name;
    }

    public boolean contains(int position) {
        return Bit.isSet(this.positions, position);
    }

    public boolean contains(Line line) {
        return Bit.countOnes(this.positions & line.positions()) == N;
    }

    public boolean intersects(Plane plane) {
        return (this.positions & plane.positions) != 0;
    }

    public boolean intersects(Line line) {
        return (this.positions & line.positions()) != 0;
    }

    public Line intersection(Plane plane) {
        // Returns the Line of intersection of two planes
        // Provided that they are not parallel
        if (this.intersects(plane) && !this.equals(plane)) {
            return Line.find(this.positions & plane.positions);
        } else {
            return null;
        }
    }

    public int intersection(Line line) {
        // Returns the position of the intersection of this plane and a line
        // provided that the line does not lie within the plane
        if (this.intersects(line) && !this.contains(line)) {
            return Bit.leadingOne(line.positions() & this.positions);
        } else {
            return -1;
        }
    }

    public boolean equals(Plane other) {
        return this.positions == other.positions;
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other instanceof Plane) {
            return this.equals((Plane) other);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        String result = "{";
        String separator = "";
        for (int position = 0; position < Coordinate.NCubed; position++) {
            if (this.contains(position)) {
                result += separator;
                result += position;
                separator = ", ";
            }
        }
        result += "}";
        return result;
    }


    private static enum Axis { X, Y, Z }
    private static final int N = Coordinate.N;

    private void set(int x, int y, int z) {
        this.positions = Bit.set(this.positions, Coordinate.position(x, y, z));
    }

    private static Plane Straight(Axis axis, int value) {
        String name = "";
        switch (axis) {
            case X: name = "YZ-Plane, X = " + value; break;
            case Y: name = "XZ-Plane, Y = " + value; break;
            case Z: name = "XY-Plane, Z = " + value; break;
        }

        Plane plane = new Plane(name);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                switch (axis) {
                    case X: plane.set(value, i, j); break;
                    case Y: plane.set(i, value, j); break;
                    case Z: plane.set(i, j, value); break;
                }
            }
        }
        return plane;
    }

    private static Plane ForwardDiagonal(Axis axis) {
        String name = "";
        switch (axis) {
            case X: name = "YZ-Forward Diagonal"; break;
            case Y: name = "XZ-Forward Diagonal"; break;
            case Z: name = "XY-Forward Diagonal"; break;
        }

        Plane plane = new Plane(name);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                switch (axis) {
                    case X: plane.set(i, j, j); break;
                    case Y: plane.set(j, i, j); break;
                    case Z: plane.set(j, j, i); break;
                }
            }
        }
        return plane;
    }

    private static Plane ReverseDiagonal(Axis axis) {
        String name = "";
        switch (axis) {
            case X: name = "YZ-Reverse Diagonal"; break;
            case Y: name = "XZ-Reverse Diagonal"; break;
            case Z: name = "XY-Reverse Diagonal"; break;
        }

        Plane plane = new Plane(name);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                switch (axis) {
                    case X: plane.set(i, j, N-1-j); break;
                    case Y: plane.set(j, i, N-1-j); break;
                    case Z: plane.set(j, N-1-j, i); break;
                }
            }
        }
        return plane;
    }

    public static final Plane[] planes = new Plane[18];
    //Borrowed this code from online:
    public static final Plane[] forced_wins = new Plane[144];
    static {
        int count = 0;
        for (Axis axis : Axis.values()) {
            for (int value = 0; value < N; value++) {
                planes[count++] = Straight(axis, value);
            }
            planes[count++] = ForwardDiagonal(axis);
            planes[count++] = ReverseDiagonal(axis);
        }
        assert count == 18;

        // Helper classes for extracting corners and centers
        class CornerExtractor {
            int[][] getCorners(Plane p) {
                String n = p.name();
                if (n.contains("YZ-Plane")) {
                    int v = Integer.parseInt(n.substring(n.lastIndexOf('=')+2));
                    return new int[][] {
                            {v,0,0}, {v,0,3}, {v,3,0}, {v,3,3}
                    };
                } else if (n.contains("XZ-Plane")) {
                    int v = Integer.parseInt(n.substring(n.lastIndexOf('=')+2));
                    return new int[][] {
                            {0,v,0}, {3,v,0}, {0,v,3}, {3,v,3}
                    };
                } else if (n.contains("XY-Plane")) {
                    int v = Integer.parseInt(n.substring(n.lastIndexOf('=')+2));
                    return new int[][] {
                            {0,0,v}, {0,3,v}, {3,0,v}, {3,3,v}
                    };
                } else if (n.contains("YZ-Forward Diagonal")) {
                    return new int[][] {
                            {0,0,0}, {0,3,3}, {3,0,0}, {3,3,3}
                    };
                } else if (n.contains("XZ-Forward Diagonal")) {
                    return new int[][] {
                            {0,0,0}, {3,0,3}, {0,3,0}, {3,3,3}
                    };
                } else if (n.contains("XY-Forward Diagonal")) {
                    return new int[][] {
                            {0,0,0}, {3,3,0}, {0,0,3}, {3,3,3}
                    };
                } else if (n.contains("YZ-Reverse Diagonal")) {
                    return new int[][] {
                            {0,0,3}, {0,3,0}, {3,0,0}, {3,3,3}
                    };
                } else if (n.contains("XZ-Reverse Diagonal")) {
                    return new int[][] {
                            {0,0,3}, {3,0,0}, {0,3,0}, {3,3,3}
                    };
                } else if (n.contains("XY-Reverse Diagonal")) {
                    return new int[][] {
                            {0,3,0}, {3,0,0}, {0,3,3}, {3,0,3}
                    };
                } else {
                    return new int[0][];
                }
            }
        }

        class CenterExtractor {

            int[][] getCenters(Plane p) {
                String n = p.name();
                // For straight planes:
                if (n.contains("YZ-Plane")) {
                    int v = Integer.parseInt(n.substring(n.lastIndexOf('=')+2));
                    return new int[][] {
                            {v,1,1}, {v,1,2}, {v,2,1}, {v,2,2}
                    };
                } else if (n.contains("XZ-Plane")) {
                    int v = Integer.parseInt(n.substring(n.lastIndexOf('=')+2));
                    return new int[][] {
                            {1,v,1}, {1,v,2}, {2,v,1}, {2,v,2}
                    };
                } else if (n.contains("XY-Plane")) {
                    int v = Integer.parseInt(n.substring(n.lastIndexOf('=')+2));
                    return new int[][] {
                            {1,1,v}, {1,2,v}, {2,1,v}, {2,2,v}
                    };
                }

                if (n.contains("YZ-Forward Diagonal")) {

                    return new int[][] {
                            {1,1,1}, {1,2,2}, {2,1,1}, {2,2,2}
                    };
                } else if (n.contains("XZ-Forward Diagonal")) {

                    return new int[][] {
                            {1,1,1}, {2,1,2}, {1,2,1}, {2,2,2}
                    };
                } else if (n.contains("XY-Forward Diagonal")) {

                    return new int[][] {
                            {1,1,1}, {2,2,1}, {1,1,2}, {2,2,2}
                    };

                } else if (n.contains("YZ-Reverse Diagonal")) {

                    return new int[][] {
                            {1,1,2}, {1,2,1}, {2,1,2}, {2,2,1}
                    };
                } else if (n.contains("XZ-Reverse Diagonal")) {

                    return new int[][] {
                            {1,1,2}, {2,1,1}, {1,2,2}, {2,2,1}
                    };
                } else if (n.contains("XY-Reverse Diagonal")) {

                    return new int[][] {
                            {1,2,1}, {2,1,1}, {1,2,2}, {2,1,2}
                    };

                } else {
                    return new int[0][];
                }
            }
        }

        CornerExtractor cornerExtractor = new CornerExtractor();
        CenterExtractor centerExtractor = new CenterExtractor();

        // 3-combination indices:
        int[][] tripleCombinations = {
                {0,1,2},
                {0,1,3},
                {0,2,3},
                {1,2,3}
        };

        int forcedPlanesCount = 0;

        for (Plane p : planes) {
            int[][] corners = cornerExtractor.getCorners(p);
            for (int[] triple : tripleCombinations) {
                Plane fw = new Plane();
                for (int idx : triple) {
                    int[] c = corners[idx];
                    fw.set(c[0], c[1], c[2]);
                }
                forced_wins[forcedPlanesCount++] = fw;
            }

            int[][] centers = centerExtractor.getCenters(p);
            if (centers.length == 4) {
                for (int[] triple : tripleCombinations) {
                    Plane fw = new Plane();
                    for (int idx : triple) {
                        int[] c = centers[idx];
                        fw.set(c[0], c[1], c[2]);
                    }
                    forced_wins[forcedPlanesCount++] = fw;
                }
            }
        }
    }
}
