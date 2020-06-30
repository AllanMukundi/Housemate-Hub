package com.cs446.housematehub.expenses;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Expense {
    public int id;
    public String title;
    public long total;
    public String date;
    List<ExpenseDivision> division;

    public Expense(int id, String title, long total, String date, List<ExpenseDivision> division) {
        this.id = id;
        this.title = title;
        this.total = total;
        this.date = date;
        this.division = division;
    }

    public static class ExpenseDivision {
        public long amount;
        public String from;
        public String to;

        public ExpenseDivision(long amount, String from, String to) {
            this.amount = amount;
            this.from = from;
            this.to = to;
        }
    }
}
