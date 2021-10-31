package edu.puc.core.parser.visitors;

import edu.puc.core.parser.COREBaseVisitor;
import edu.puc.core.parser.COREParser;
import edu.puc.core.parser.exceptions.MissingValueException;

public class TimeSpanVisitor extends COREBaseVisitor<Long> {

    @Override
    public Long visitTime_span(COREParser.Time_spanContext ctx) {
        if (ctx.children == null || ctx.children.size() == 0) {
            throw new MissingValueException("TimeWindow must state a value", ctx);
        }
        long totalSeconds = 0;

        if (ctx.second_span() != null) {
            totalSeconds += Long.parseLong(ctx.second_span().integer().getText());
        }

        if (ctx.minute_span() != null) {
            totalSeconds += Long.parseLong(ctx.minute_span().integer().getText()) * 60;
        }

        if (ctx.hour_span() != null) {
            totalSeconds += Long.parseLong(ctx.hour_span().integer().getText()) * 3600;
        }

        return totalSeconds;
    }
}