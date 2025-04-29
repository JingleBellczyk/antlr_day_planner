grammar Grammar;

prog: expr EOF;

expr:
        help                         #help_op
     | listing                       # lstg
     | mail                          # mail_op
     | calendar                      # calendar_op
     | tasklist                      # tsk_lst_tok
     | task                          # tsk_tok
;

listing:
     object=(MAIL | CALENDAR) LIST num=INT #list_service_last_n;


help:
    object=(MAIL | CALENDAR | TASK | TASKLIST) 'help' #help_specific_op
    | 'help' #help_general_op
    ;

mail:
     base=MAIL do=SHOW num=INT #show_mail
     | base=MAIL do=CREATE (dest=EMAIL title=STRING mailBody=(STRING | TXT)) #send_mail
     | base=MAIL 'help' #help_mail_op // Dodana reguła
 ;

calendar:
    CALENDAR SHOW date=DATE arg=calendar_objects #show_events_date
    | CALENDAR CREATE start=DATE startTime=HOUR_MINUTE (end=DATE)? endTime=HOUR_MINUTE SUMMARY':' sum=STRING arg=event_objects #create_event
    | CALENDAR 'help' #help_calendar_op // Dodana reguła
    ;

task:
    TASK CREATE tasklist_name=STRING task_title=STRING parent=STRING? #create_task
    | TASK SHOW tasklist_name=STRING task_name=STRING #show_task
    | TASK DELETE tasklist_name=STRING task_name=STRING #delete_task
    | TASK 'help' #help_task_op // Dodana reguła
    ;

tasklist:
    TASKLIST CREATE name=STRING  #create_tasklist
    | TASKLIST DELETE name=STRING  #delete_tasklist
    | TASKLIST RENAME current_name=STRING new_name=STRING  #rename_tasklist
    | TASKLIST ALL #list_all_tasklists
    | TASKLIST REMOVE tasklist_name=STRING task_name=STRING #remove_task_from_tasklist
    | TASKLIST LIST name=STRING #list_tasklist_tasks
    | TASKLIST 'help' #help_tasklist_op // Dodana reguła
    ;
event_objects:
         (occur=occurance? ('before:' bef=STRING)? color=COLOR_TYPE? ('desc:' desc=STRING)?  ('loc:' loc =STRING)?)
 ;


calendar_objects:
    (DURATION? DESCRIPTION? COLOR?)
 ;


occurance:
     per=PERIOD count=INT;


MAIL: 'mail';
CALENDAR: 'calendar';
NAME: 'name';
TASKLIST: 'tasklist';
TASK: 'task';


LIST  : 'list';
CREATE: 'create';
DELETE: 'delete';
ADD: 'add';
UPDATE: 'update';
RENAME: 'rename';
SHOW: 'show';
ALL: 'all';
CLEAR: 'clear';
REMOVE: 'remove';
MOVE: 'move';

DATE : ('0'[1-9] | '1'[0-9] | '2'[0-9] | '3'[01]) '.' ('0'[1-9] | '1'[0-2]) '.' '20'[0-9][0-9] ;

HOUR_MINUTE: (('0'? [0-9]) | ('1' [0-9]) | ('2' [0-3])) ':' ([0-5] [0-9]);

// Listing
LISTABLE_OBJECT: CALENDAR | MAIL;
LISTING_TYPE: 'upcoming' | 'previous';

WS: [ \t\r\n]+ -> skip; // Pomijanie białych znaków

EMAIL: [a-zA-Z0-9._]+ '@' [a-zA-Z0-9.-]+;

// task
STATUS: 'needsAction' | 'completed';

TITLE: 'title';

// calendar
SUMMARY: 'summ';
DURATION: 'dur';
DESCRIPTION: 'desc';
COLOR: 'color';

COLOR_TYPE: 'red' | 'orange' | 'yellow' | 'basil' | 'green' | 'grape' | 'flamingo' | 'blueberry' | 'blue' | 'graphite' | 'violet';
PERIOD: 'daily' | 'weekly' | 'monthly' | 'yearly';

// Whitespace and comments
NEWLINE: [\r\n]+ -> channel(HIDDEN);

TXT: ('/' TXT_ID (('/' | '\\') TXT_ID)*)? TXT_ID '.txt'; // Ścieżka zaczynająca się od / + nazwa pliku .txt
TXT_ID: [a-zA-Z_][a-zA-Z0-9_]*; // Identyfikator

 // Tokens
INT: [0-9]+;
STRING: '"' ~["\r\n]* '"' ;