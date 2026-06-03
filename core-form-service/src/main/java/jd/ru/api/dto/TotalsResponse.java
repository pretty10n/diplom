package jd.ru.api.dto;

public record TotalsResponse(
        Form4Totals form4,
        Form5Totals form5,
        Form6Totals form6
) {
    public record PairTotal(
            double col11,
            double col12
    ) {
    }

    public record Form4Totals(
            PairTotal section1Total,
            PairTotal section2Total,
            PairTotal section1And2Total
    ) {
    }

    public record Form5Totals(
            PairTotal section1Total
    ) {
    }

    public record Form6Totals(
            PairTotal total
    ) {
    }
}
