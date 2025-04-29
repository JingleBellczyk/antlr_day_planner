package services;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

public class TasksService {

    private final Tasks service;

    private static Tasks getTasksService() {
        final NetHttpTransport HTTP_TRANSPORT;
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        try {
            return new Tasks.Builder(HTTP_TRANSPORT, Utils.JSON_FACTORY, Utils.getCredentials(HTTP_TRANSPORT))
                    .setApplicationName("PLANNER APP")
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TasksService(){
       this.service = getTasksService();
    }

    public List<String> createTaskList(String name) {
        List<String> output = new ArrayList<>();
        TaskList taskList = new TaskList();
        taskList.setTitle(name);


        try {

            service.tasklists().insert(taskList).execute();
            output.add("Task list created: " + name);
            return output;
        } catch (IOException e) {
            output.add("Task list creation failed: " + e.getMessage());
            return output;
        }
    }
    private String getTaskListId(String name) {
        List<TaskList> taskListsFromService = getAllTaskLists();
        List<TaskList> taskLists = new ArrayList<>(taskListsFromService);
        for (TaskList taskList : taskLists) {
            if (taskList.getTitle().equals(name)) {
                return taskList.getId();
            }
        }
        return null;
    }

    public List<String> deleteTaskList(String name)
    {
        List<String> output = new ArrayList<>();
        try {
            service.tasklists().delete(getTaskListId(name)).execute();
            output.add("Task list deleted: " + name);
            return output;
        } catch (IOException e) {
            output.add("Task list deletion failed: " + e.getMessage());
            return output;
        }
    }

    public List<String> renameTaskList(String currentName, String newName)
    {
        TaskList taskList = new TaskList();
        taskList.setId(getTaskListId(currentName));
        taskList.setTitle(newName);

        List<String> output = new ArrayList<>();
        try {
            service.tasklists().update(getTaskListId(currentName), taskList).execute();
            output.add("Task list renamed from " + currentName + " to " + newName);
        } catch (IOException e) {
            output.add("Task list renaming failed: " + e.getMessage());
            return output;
        }
        return output;
    }

    private List<TaskList> getAllTaskLists()
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
    public List<String> listAllTaskList()
    {
        List<String> output = new ArrayList<>();
        List<TaskList> taskLists = getAllTaskLists();
        output.add("All task lists:");
        for (TaskList taskList : taskLists) {
            output.add("- "+taskList.getTitle());
        }
        return output;
    }
//
    public List<String> listTasksFromTasklist(String tasklistName)
    {
        List<String> output = new ArrayList<>();
        String tasklistId = getTaskListId(tasklistName);
        try {
            service.tasklists().get(tasklistId).execute();
            List<Task> tasks = service.tasks().list(tasklistId).execute().getItems();
            if (tasks != null) {
                for (Task task : tasks) {
                    output.add("- " + task.getTitle());
                }
            }
            return output;
        } catch (IOException e) {
            output.add("Task list not found: " + e.getMessage());
        }
        return output;

    }

    private String getTaskId(String tasklistName, String taskName)
    {
        String tasklistId = getTaskListId(tasklistName);
        try {
            service.tasklists().get(tasklistId).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<Task> tasks;
        try {
            tasks = service.tasks().list(tasklistId).execute().getItems();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (tasks != null) {
            for (Task task : tasks) {
                if (task.getTitle().equals(taskName)) {
                    return task.getId();
                }
            }
        }
        return null;

    }

    public List<String> removeTaskFromTasklist(String tasklistName, String taskName)
    {
        List<String> output = new ArrayList<>();
        String tasklistId = getTaskListId(tasklistName);
        String taskId = getTaskId(tasklistName, taskName);

        try {
            service.tasks().delete(tasklistId,taskId).execute();
            output.add("Task removed from task list: " + taskName);

            return output;
        } catch (IOException e) {
            output.add("Task removal failed: " + e.getMessage());
            return output;
        }

    }

    public List<String> createTask(String tasklistName, String taskName, String parentName)
    {
        List<String> output = new ArrayList<>();
        String tasklistId = getTaskListId(tasklistName);
        Task task = new Task();
        task.setTitle(taskName);
        if(parentName != null)
        {
            String parentId = getTaskId(tasklistName, parentName);
            task.setParent(parentId);
        }

        try {
            service.tasks().insert(tasklistId, task).execute();
            output.add("Task created and added to task list: " + taskName);
            return output;
        } catch (IOException e) {
            output.add("Task creation failed: " + e.getMessage());
            return output;
        }
    }


    public List<String> showTask(String tasklistName, String taskName)
    {
        List<String> output = new ArrayList<>();
        String tasklistId = getTaskListId(tasklistName);
        String taskId = getTaskId(tasklistName, taskName);
        try {
            Task task = service.tasks().get(tasklistId, taskId).execute();
            output.add("Task details:");
            output.add("- Title: " + task.getTitle());
            output.add("- Status: " + task.getStatus());
            output.add("- Due date: " + task.getDue());
            output.add("- Completed date: " + task.getCompleted());
            return output;
        } catch (IOException e) {
            output.add("Task not found: " + e.getMessage());
            return output;
        }
    }

    public List<String> updateTask(String tasklistName, String taskName, String newTaskName, String newSummary, String newStatus)
    {
        List<String> output = new ArrayList<>();
        String tasklistId = getTaskListId(tasklistName);
        String taskId = getTaskId(tasklistName, taskName);
        Task task;
        try {
            task = service.tasks().get(tasklistId,taskId).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(newTaskName == null && newSummary == null && newStatus == null)
        {
            output.add("No changes to update.");
            return output;
        }

        if(newStatus != null)
        {
            task.setStatus(newStatus);
            output.add("Status updated to: " + newStatus);
        }
        if(newTaskName != null)
        {
            task.setTitle(newTaskName);
            output.add("Task name updated to: " + newTaskName);
        }
        if(newSummary != null)
        {
            task.setNotes(newSummary);
            output.add("Summary updated to: " + newSummary);
        }
        return output;




    }









    //TODO: the rest of methods















}

