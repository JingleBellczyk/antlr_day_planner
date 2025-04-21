package interpreter;

import com.google.api.client.util.DateTime;
import com.google.api.services.gmail.model.Message;
import grammar.GrammarBaseVisitor;
import grammar.GrammarParser;
import jdk.jshell.execution.Util;
import logic.UserCalendarOperations;
import logic.UserMailOperations;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;
import services.CalendarColor;
import services.MailService;

import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static grammar.GrammarParser.*;
import static services.Utils.*;


public class PlannerVisitor extends GrammarBaseVisitor<Object> {

    private final TokenStream tokenStream;
    private final CharStream inputStream;
    private UserMailOperations userMailOperations;
    private UserCalendarOperations userCalendarOperations;

    public PlannerVisitor(CharStream input, TokenStream tokens) {
        super();
        this.inputStream = input;
        this.tokenStream = tokens;
        this.userMailOperations = new UserMailOperations();
        this.userCalendarOperations = new UserCalendarOperations();
    }

    @Override
    public String visitTerminal(TerminalNode node) {
        if (node.getSymbol().getType() == GrammarParser.EOF) {
            return null;
        } else if (node.getText().equals(";")) {
            return null; // Pomijaj średniki
        }
        return "Terminal node:<n>" + node.getText(); // Diagnostyka
    }

    // Helper method to get text from context
    private String getText(ParserRuleContext ctx) {
        if (inputStream == null) {
            throw new IllegalStateException("Input stream is undefined");
        }
        int start = ctx.start.getStartIndex();
        int stop = ctx.stop.getStopIndex();
        return inputStream.getText(new Interval(start, stop));
    }
//    listing:
//    object=LISTABLE_OBJECT LIST n=INT #list_service_last_n;

    @Override
    public Object visitList_service_last_n(GrammarParser.List_service_last_nContext ctx) {
        Long number = Long.parseLong(ctx.num.getText());

        switch (ctx.object.getType()) {
            case MAIL:
                userMailOperations.getLastEmails(number);
                break;
            case CALENDAR:
                userCalendarOperations.listUpcomingEvents(number);
                break;
        }
        return null;
    }

    @Override
    public Object visitShow_mail(GrammarParser.Show_mailContext ctx) {
        Integer index = Integer.parseInt(ctx.num.getText());
        userMailOperations.showEmailOnIndex(index);
        return null;
    }
    // base=MAIL do=CREATE (dest=EMAIL title=STRING mailBody=(STRING | TXT))


    @Override
    public Object visitSend_mail(GrammarParser.Send_mailContext ctx) {

        String destination = ctx.dest.getText();
        String title = stripQuotes(ctx.title.getText());
        String mailBody = stripQuotes(ctx.mailBody.getText());

        switch (ctx.mailBody.getType()) {
            case TXT:
                userMailOperations.sendEmailFromFile(destination, title, mailBody);
                break;
            case STRING:
                userMailOperations.sendEmail(destination, title, mailBody);
                break;
        }
        return null;
    }

    @Override
    public Object visitShow_events_date(GrammarParser.Show_events_dateContext ctx) {
        GrammarParser.Calendar_objectsContext args = ctx.arg;

        boolean hasColor = args.COLOR() != null;
        boolean hasTime = args.TIME() != null;
        boolean hasDescription = args.DESCRIPTION() != null;

        Map<String, Boolean> options = new HashMap<>();
        options.put("description", hasDescription);
        options.put("time", hasTime);
        options.put("color", hasColor);

        String dateString = ctx.date.getText();
        Date date = parseAndValidateDate(dateString);

        if (date == null) {
            System.out.printf("Bad date format: %s, required format dd.mm.yyyy%n", dateString);
            return null;
        }
        userCalendarOperations.getEventsForDay(date, options);
        return null;
    }

    @Override
    public Object visitCreate_event(Create_eventContext ctx) {

        String startDateString = ctx.start.getText();
        String startTimeString = ctx.startTime.getText();
        String endDateString = ctx.end != null ? ctx.end.getText() : startDateString; // jeśli brak – użyj tej samej daty
        String endTimeString = ctx.endTime.getText();

        Date startDate = parseAndValidateDate(startDateString);
        Date endDate = parseAndValidateDate(endDateString);

        if (startDate == null || endDate == null) {
            System.out.printf("Bad date format: %s or %s, required format dd.mm.yyyy%n", startDateString, endDateString);
            return null;
        }

        LocalTime startTime = startTimeString != null ? parseAndValidateTime(startTimeString) : null;
        if (startTime == null) startTime = LocalTime.MIDNIGHT;

        LocalTime endTime = endTimeString != null ? parseAndValidateTime(endTimeString) : null;
        if (endTime == null) endTime = LocalTime.MIDNIGHT;

        DateTime startDatetime = mergeDateAndTime(startDate, startTime);
        DateTime endDatetime = mergeDateAndTime(endDate, endTime);

        String summary = ctx.sum.getText();

        // ------------------- OPCJONALNE ARGUMENTY -------------------
        Map<String, Object> options = new HashMap<>();
        GrammarParser.Event_objectsContext objects = ctx.event_objects();

        if (objects != null) {
            if (objects.occur != null) {
                String period = objects.occur.per.getText();
                int count = Integer.parseInt(objects.occur.count.getText());

                Map<String, Object> recurrence = new HashMap<>();
                recurrence.put("period", period);
                recurrence.put("count", count);
                options.put("recurrence", recurrence);
            }

            if (objects.bef != null) {
                String beforeSummary = objects.bef.getText();
                options.put("before", beforeSummary);
            }

            if (objects.color != null) {
                String color = objects.color.getText();
                options.put("color", color);
            }

            if (objects.desc != null) {
                String desc = objects.desc.getText();
                options.put("description", desc);
            }

            if (objects.loc != null) {
                String loc = objects.loc.getText();
                options.put("location", loc);
            }
        }

        try {
            userCalendarOperations.createEventWithOptions(startDatetime, endDatetime, summary, options);
        } catch (Exception e) {
            System.err.println("Failed to create event: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

}
