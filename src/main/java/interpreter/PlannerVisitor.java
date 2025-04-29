package interpreter;

import com.google.api.client.util.DateTime;
import grammar.GrammarBaseVisitor;
import grammar.GrammarParser;
import logic.Constants;
import logic.UserCalendarOperations;
import logic.UserMailOperations;
import logic.UserTasksOperations;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenStream;
import services.Utils;

import java.time.LocalTime;
import java.util.*;

import static grammar.GrammarParser.*;
import static logic.Constants.*;
import static services.Utils.*;


public class PlannerVisitor extends GrammarBaseVisitor<List<String>> {

    private final UserMailOperations userMailOperations;
    private final UserCalendarOperations userCalendarOperations;
    private final UserTasksOperations userTasksOperations;


    public PlannerVisitor(CharStream input, TokenStream tokens) {
        super();
        this.userMailOperations = new UserMailOperations();
        this.userCalendarOperations = new UserCalendarOperations();
        this.userTasksOperations = new UserTasksOperations();
    }


    @Override
    public List<String> visitList_service_last_n(GrammarParser.List_service_last_nContext ctx) {

        long number = Long.parseLong(ctx.num.getText());

        return switch (ctx.object.getType()) {
            case MAIL -> userMailOperations.getLastEmails(number);
            case CALENDAR -> userCalendarOperations.listUpcomingEvents(number);
            default -> List.of("Nieobsługiwany obiekt do listowania.");
        };
    }

    @Override
    public List<String> visitShow_mail(GrammarParser.Show_mailContext ctx) {

        int index = Integer.parseInt(ctx.num.getText());
        return userMailOperations.showEmailOnIndex(index);
    }

    @Override
    public List<String> visitSend_mail(GrammarParser.Send_mailContext ctx) {

        String destination = ctx.dest.getText();
        String title = stripQuotes(ctx.title.getText());
        String mailBody = stripQuotes(ctx.mailBody.getText());

        String result = switch (ctx.mailBody.getType()) {
            case TXT -> userMailOperations.sendEmailFromFile(destination, title, mailBody);
            case STRING -> userMailOperations.sendEmail(destination, title, mailBody);
            default -> "Nieznany typ treści wiadomości.";
        };
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

        assert startTime != null;
        DateTime startDatetime = mergeDateAndTime(startDate, startTime);
        assert endTime != null;
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

        return visit(ctx.expr());
    }

    @Override
    public List<String> visitHelp_specific_op(Help_specific_opContext ctx) {
        // Obsługa komendy help dla konkretnego serwisu
        String helpPath = switch (ctx.object.getType()) {
            case MAIL -> HELP_PATH_MAIL;
            case CALENDAR -> HELP_PATH_CALENDAR;
            case TASK, TASKLIST -> HELP_PATH_TASKS;
            default -> HELP_PATH;
        };

        return readFileTxt(helpPath);
    }

    @Override
    public List<String> visitHelp_general_op(Help_general_opContext ctx) {
        // Obsługa ogólnej komendy help
        return Utils.readFileTxt(HELP_PATH);
    }

    // Dodane metody do obsługi komend help dla poszczególnych serwisów

    @Override
    public List<String> visitHelp_mail_op(GrammarParser.Help_mail_opContext ctx) {
        // Obsługa komendy "mail help"
        return Utils.readFileTxt(HELP_PATH_MAIL);
    }

    @Override
    public List<String> visitHelp_calendar_op(GrammarParser.Help_calendar_opContext ctx) {
        // Obsługa komendy "calendar help"
        return Utils.readFileTxt(HELP_PATH_CALENDAR);
    }

    @Override
    public List<String> visitHelp_task_op(GrammarParser.Help_task_opContext ctx) {
        // Obsługa komendy "task help"
        return Utils.readFileTxt(HELP_PATH_TASKS);
    }

    @Override
    public List<String> visitHelp_tasklist_op(GrammarParser.Help_tasklist_opContext ctx) {
        // Obsługa komendy "tasklist help"
        return Utils.readFileTxt(HELP_PATH_TASKS);
    }

    @Override
    public List<String> visitCreate_tasklist(Create_tasklistContext ctx) {
        String tasklistName = stripQuotes(ctx.name.getText());
        return userTasksOperations.createTasklist(tasklistName);
    }

    @Override
    public List<String> visitDelete_tasklist(Delete_tasklistContext ctx) {
        String tasklistName = stripQuotes(ctx.name.getText());
        return userTasksOperations.deleteTasklist(tasklistName);
    }

    @Override
    public List<String> visitRename_tasklist(Rename_tasklistContext ctx)
    {
        String currentName = stripQuotes(ctx.current_name.getText());
        String newName = stripQuotes(ctx.new_name.getText());

        return userTasksOperations.renameTaskList(currentName, newName);
    }

    @Override
    public List<String> visitList_all_tasklists(List_all_tasklistsContext ctx) {
        return userTasksOperations.listAllTasklists();
    }

    @Override
    public List<String> visitRemove_task_from_tasklist(Remove_task_from_tasklistContext ctx) {
        String taskListName = stripQuotes(ctx.tasklist_name.getText());
        String taskName = stripQuotes(ctx.task_name.getText());
        return userTasksOperations.removeTaskFromTasklist(taskListName, taskName);

    }

    @Override
    public List<String> visitList_tasklist_tasks(List_tasklist_tasksContext ctx) {
        String tasklistName = stripQuotes(ctx.name.getText());
        return userTasksOperations.listTasksFromTasklist(tasklistName);
    }

    @Override
    public List<String> visitCreate_task(Create_taskContext ctx) {
        String taskListName = stripQuotes(ctx.tasklist_name.getText());
        String taskName = stripQuotes(ctx.task_title.getText());
        if (ctx.parent != null) {
            String parentName = stripQuotes(ctx.parent.getText());
            return userTasksOperations.createTask(taskListName, taskName, parentName);
        }
        return userTasksOperations.createTask(taskListName, taskName,null);

    }

    @Override
    public List<String> visitShow_task(Show_taskContext ctx) {
        String taskListName = stripQuotes(ctx.tasklist_name.getText());
        String taskName = stripQuotes(ctx.task_name.getText());
        return userTasksOperations.showTask(taskListName, taskName);
    }

    @Override
    public List<String> visitDelete_task(Delete_taskContext ctx) {
        String taskListName = stripQuotes(ctx.tasklist_name.getText());
        String taskName = stripQuotes(ctx.task_name.getText());
        return userTasksOperations.deleteTask(taskListName, taskName);
    }

//    @Override
//    public List<String> visitUpdateOption(UpdateOptionContext ctx) {
//        String newTitle =  ctx.STATUS().getText() != null ? stripQuotes(ctx.tit.getText()) : null;
//        return super.visitUpdateOption(ctx);
//    }
//
//    @Override
//    public List<String> visitUpdate_task(Update_taskContext ctx) {
//        String taskListName = stripQuotes(ctx.tasklist_name.getText());
//        String taskName = stripQuotes(ctx.task_name.getText());
//        String newTitle =  ctx.updateOption(). != null ? stripQuotes(ctx.tit.getText()) : null;
//        String newStatus = ctx.sta != null ? ctx.sta.getText() : null;
//        String newSummary = ctx.sum != null ? stripQuotes(ctx.sum.getText()) : null;


//        return userTasksOperations.updateTask(taskListName,taskName,newTitle,newSummary,newStatus);
//    }
}