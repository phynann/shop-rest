package edu.itc.shop_rest.domain;

import java.math.BigDecimal;

public record Money(BigDecimal amount, String currency) {

    public static Money parse(String s) {
        if (s == null) throw bad(s);
        String[] p = s.trim().split("\\s+");
        if (p.length != 2 || !p[1].matches("[A-Z]{3}")) throw bad(s);
        try {
            return new Money(new BigDecimal(p[0]), p[1]);
        } catch (NumberFormatException e) {
            throw bad(s);
        }
    }

    private static IllegalArgumentException bad(String s) {
        return new IllegalArgumentException("Cannot parse Money from '" + s + "'");
    }

    @Override
    public String toString() {
        return amount.toPlainString() + " " + currency;
    }
}