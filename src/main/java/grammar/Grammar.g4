grammar Grammar;

prog: expr EOF;

expr:
      listing                        # lstg
     | mail                          # mail_op
     | calendar                      # calendar_op
     | tasklist                      # tsk_lst_tok
     | task                          # task_op
     | help                          #help_op
;

help:
    object=(MAIL | CALENDAR) 'help' #help_specific_op
    | 'help' #help_general_op
    ;

listing:
     object=(MAIL | CALENDAR) LIST num=INT #list_service_last_n
     ;

tasklist:
    TASKLIST CREATE name=STRING  #create_tasklist //stworzenie nowej listy tasków
    | TASKLIST DELETE name=STRING  #delete_tasklist //usunięcie listy tasków
    | TASKLIST UPDATE name=STRING  #update_tasklist //zmiana listy tasków
    | TASKLIST ALL #list_all_tasklists //wypisanie wszystkich list tasków
    | TASKLIST SHOW name=STRING #show_tasklist // pokazanie konkretnej listy tasków
    | TASKLIST REMOVE tasklist_name=STRING task_name=STRING #remove_task_from_tasklist     //Usunięcie taska z konkretnej listy
    | TASKLIST LIST name=STRING #list_tasklist_tasks     //Listowanie wszystkich tasków z listy
    | TASKLIST CLEAR name=STRING #clear_tasklist     //Usunięcie wszystkich ukończonych tasków
    ;

task:
     TASK MOVE tasklist_name=STRING task_name=STRING dest=STRING? new_parent=STRING? after_task=STRING?  #move_task     // Moves task from one list to another or just moves it within particlar list
    | TASK CREATE tasklist_name=STRING task_title=STRING parent=STRING? after_task=STRING? #create_task     //Creates task in tasklist with some optional parameters
    | TASK SHOW tasklist_name=STRING task_name=STRING #show_task    //Shows particular task
    | TASK DELETE tasklist_name=STRING task_name=STRING #remove_task    //Removes task from specified list
    | TASK UPDATE tasklist_name=STRING task_name=STRING (SUMMARY sum=STRING)? (TITLE sum=STRING)? ('status' sum=STATUS)? #update_task    //Removes task from specified list
    ;

mail:
     base=MAIL do=SHOW num=INT #show_mail //można wypisać zawartość maila
     | base=MAIL do=CREATE (dest=EMAIL title=STRING mailBody=(STRING | TXT)) #send_mail//można wysłać maila od zadanej treści z pliku lub z komendy na email z tematem
     ;


calendar:
    CALENDAR SHOW date=DATE arg=calendar_objects #show_events_date //wypisanie wszystkich wydarzeń na dany dzień ze szczegółami np. czas, lokacja, summary
    | CALENDAR CREATE start=DATE startTime=HOUR_MINUTE (end=DATE)? endTime=HOUR_MINUTE sum=STRING arg=event_objects #create_event  //stworzenie nowego wydarzenia, które można spersonalizować
     ;

event_objects:
         (occur=occurance? ('before:' bef=STRING)? color=COLOR_TYPE? ('desc:' desc=STRING)?  ('loc:' loc =STRING)?)
    ;


calendar_objects:
    (DURATION? DESCRIPTION? COLOR?) //można użyć tylko raz, są to informacje do spersonalizowania przez użytkownika do wyświetlenia dla maili
    ;


occurance:
     per=PERIOD count=INT
     ;

// Keywords for services

MAIL: 'mail';
CALENDAR: 'calendar';
NAME: 'name';
TASKLIST: 'tasklist';
TASK: 'task';


//Keywords for operations
LIST  : 'list';
CREATE: 'create';
DELETE: 'delete';
ADD: 'add';
UPDATE: 'update';
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

TXT: ('/' TXT_ID (('/' | '\\') TXT_ID)*)? TXT_ID '.txt'; // Ścieżka zaczynająca się od / + nazwa pliku .txt
TXT_ID: [a-zA-Z_][a-zA-Z0-9_]*; // Identyfikator


 // Tokens
INT: [0-9]+;
STRING: '"' ~["\r\n]* '"' ;