package services;

import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.TaskList;
import java.io.IOException;
import java.util.*;

import static services.Utils.getCredentials;
import static services.Utils.getGoogleService;

public class TasksService {

    private final Tasks service;

    public TasksService(){
       this.service = getGoogleService(Tasks.class);;
    }

    public Boolean createTaskList(String name) {
        TaskList taskList = new TaskList();

        taskList.setTitle(name);
        try {

            service.tasklists().insert(taskList);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean deleteTaskList(String name)
    {
        try {
            service.tasklists().delete(name).execute();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateTaskList(TaskList taskList)
    {
        try {
            service.tasklists().update(taskList.getId(), taskList).execute();
        } catch (IOException e) {}
    }

    public List<TaskList> getAllTaskLists()
    {
        List<TaskList> taskLists = new ArrayList<>();
        try {
            List<TaskList> taskListsFromService = service.tasklists().list().execute().getItems();
            if (taskListsFromService != null) {
                taskLists.addAll(taskListsFromService);
            }
        } catch (IOException e) {}
        return taskLists;
    }

    //TODO: the rest of methods















}

