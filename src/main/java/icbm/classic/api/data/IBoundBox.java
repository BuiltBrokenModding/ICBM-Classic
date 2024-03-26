package icbm.classic.api.data;

public interface IBoundBox<POS> {
    POS lowerBound();

    POS upperBound();
}
