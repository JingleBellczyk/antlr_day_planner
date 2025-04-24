package interpreter;

import com.google.api.client.util.DateTime;
import grammar.GrammarBaseVisitor;
import grammar.GrammarParser;
import logic.Constants;
import logic.UserCalendarOperations;
import logic.UserMailOperations;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenStream;
import services.Utils;

import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static grammar.GrammarParser.*;
import static services.Utils.*;


public class PlannerVisitor extends GrammarBaseVisitor<List<String>> {

    private UserMailOperations userMailOperations;
    private UserCalendarOperations userCalendarOperations;

    public PlannerVisitor(CharStream input, TokenStream tokens) {
        super();
        this.userMailOperations = new UserMailOperations();
        this.userCalendarOperations = new UserCalendarOperations();
    }


    @Override
    public List<String> visitList_service_last_n(GrammarParser.List_service_last_nContext ctx) {

        Long number = Long.parseLong(ctx.num.getText());

        switch (ctx.object.getType()) {
            case MAIL:
                return userMailOperations.getLastEmails(number);
            case CALENDAR:
                return userCalendarOperations.listUpcomingEvents(number);
        }
        return List.of("Nieobsługiwany obiekt do listowania.");
    }

    @Override
    public List<String> visitShow_mail(GrammarParser.Show_mailContext ctx) {

        Integer index = Integer.parseInt(ctx.num.getText());
        return userMailOperations.showEmailOnIndex(index);
    }

    @Override
    public List<String> visitSend_mail(GrammarParser.Send_mailContext ctx) {

        String destination = ctx.dest.getText();
        String title = stripQuotes(ctx.title.getText());
        String mailBody = stripQuotes(ctx.mailBody.getText());

        String result;
        switch (ctx.mailBody.getType()) {
            case TXT:
                result = userMailOperations.sendEmailFromFile(destination, title, mailBody);
                break;
            case STRING:
                result = userMailOperations.sendEmail(destination, title, mailBody);
                break;
            default:
                result = "Nieznany typ treści wiadomości.";
        }
        return List.of(result);
    }

    @Override
    public List<String> visitShow_events_date(GrammarParser.Show_events_dateContext ctx) {

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
            return List.of(String.format("Bad date format: %s, required format dd.mm.yyyy", dateString));
        }
        return userCalendarOperations.getEventsForDay(date, options);
    }

    @Override
    public List<String> visitCreate_event(Create_eventContext ctx) {

        String startDateString = ctx.start.getText();
        String startTimeString = ctx.startTime.getText();
        String endDateString = ctx.end != null ? ctx.end.getText() : startDateString;
        String endTimeString = ctx.endTime.getText();

        Date startDate = parseAndValidateDate(startDateString);
        Date endDate = parseAndValidateDate(endDateString);

        if (startDate == null || endDate == null) {
            return List.of(String.format("Bad date format: %s or %s, required format dd.mm.yyyy", startDateString, endDateString));
        }

        LocalTime startTime = startTimeString != null ? parseAndValidateTime(startTimeString) : LocalTime.MIDNIGHT;
        LocalTime endTime = endTimeString != null ? parseAndValidateTime(endTimeString) : LocalTime.MIDNIGHT;

        DateTime startDatetime = mergeDateAndTime(startDate, startTime);
        DateTime endDatetime = mergeDateAndTime(endDate, endTime);

        String summary = ctx.sum.getText();

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
            if (objects.bef != null) options.put("before", objects.bef.getText());
            if (objects.color != null) options.put("color", objects.color.getText());
            if (objects.desc != null) options.put("description", objects.desc.getText());
            if (objects.loc != null) options.put("location", objects.loc.getText());
        }

        try {
            return userCalendarOperations.createEventWithOptions(startDatetime, endDatetime, summary, options);
        } catch (Exception e) {
            return List.of("Failed to create event: " + e.getMessage());
        }
    }

    @Override
    public List<String> visitProg(ProgContext ctx) {

        List<String> output = visit(ctx.expr());
        return output;
    }

    @Override
    public List<String> visitHelp_specific_op(Help_specific_opContext ctx) {

        System.out.println("TUTAJ");
        String path = null;
        switch (ctx.object.getType()) {
            case MAIL:
                path = Constants.HELP_PATH_MAIL;
                break;
            case CALENDAR:
                path = Constants.HELP_PATH_CALENDAR;
                break;
        }
        return readFileTxt(path);
    }

    @Override
    public List<String> visitHelp_general_op(Help_general_opContext ctx) {

        String path = Constants.HELP_PATH;
        return Utils.readFileTxt(path);
    }

}

