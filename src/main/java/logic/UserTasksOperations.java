package logic;

import com.google.api.services.tasks.model.TaskList;
import services.MailService;
import services.TasksService;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public class UserTasksOperations {

    TasksService tasksService;

    public UserTasksOperations() {
        this.tasksService = new TasksService();
    }

    public List<String> createTasklist(String name) {
        return tasksService.createTaskList(name);
    }
    public List<String> deleteTasklist(String name) {
        return tasksService.deleteTaskList(name);
    }
    public List<String> renameTaskList(String currentName, String newName) {
        return tasksService.renameTaskList(currentName, newName);
    }
    public List<String> listAllTasklists() {
        return tasksService.listAllTaskList();
    }

    public List<String> listTasksFromTasklist(String tasklistName) {
        return tasksService.listTasksFromTasklist(tasklistName);
    }

    public List<String> removeTaskFromTasklist(String tasklistName, String taskName) {
        return tasksService.removeTaskFromTasklist(tasklistName, taskName);
    }

    public List<String> createTask(String tasklistName, String taskName, String parentName) {
        return tasksService.createTask(tasklistName, taskName, parentName);
    }
    public List<String> showTask(String tasklistName, String taskName) {
        return tasksService.showTask(tasklistName, taskName);
    }
    public List<String> deleteTask(String tasklistName, String taskName) {
        return tasksService.removeTaskFromTasklist(tasklistName, taskName);
    }

    public List<String> updateTask(String tasklistName, String taskName, String newTaskName, String newSummary, String newStatus)
    {
        return tasksService.updateTask(tasklistName, taskName, newTaskName, newSummary, newStatus);
    }






}
