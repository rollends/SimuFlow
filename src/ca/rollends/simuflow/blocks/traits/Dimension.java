package ca.rollends.simuflow.blocks.traits;

public class Dimension  {
    final int nRows;
    final int nCols;

    public static final Dimension Any = new Dimension(-1,-1);
    public static final Dimension AnyColumnVector = new Dimension(-1,1);
    public static final Dimension AnyRowVector = new Dimension(1, -1);
    public static final Dimension Scalar = new Dimension(1, 1);

    public Dimension(int r, int c) {
        nRows = r;
        nCols = c;
    }

    public static Dimension coalesce(Dimension d1, Dimension d2) {
        int newCols = -1;
        int newRows = -1;

        if(d1.nCols == -1 || d2.nCols == -1) {
            newCols = Math.max(d1.nCols, d2.nCols);
        } else if (d1.nCols == d2.nCols) {
            newCols = d1.nCols;
        } else {
            // TODO: Throw exception for dimension mismatch.
        }

        if(d1.nRows == -1 || d2.nRows == -1) {
            newRows = Math.max(d1.nRows, d2.nRows);
        } else if (d1.nRows == d2.nRows) {
            newRows = d1.nRows;
        } else {
            // TODO: Throw exception for dimension mismatch.
        }

        return new Dimension(newRows, newCols);
    }

    public boolean isDetermined() {
        return nCols == -1 && nRows == -1;
    }
}
