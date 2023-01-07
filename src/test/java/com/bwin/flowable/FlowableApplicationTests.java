package com.bwin.flowable;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
class FlowableApplicationTests {
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    private final String staffId = "1000";
    private final String zuzhangId = "90";
    private final String managerId = "100";

    /**
     * 列出所有待执行的任务
     */
    @Test
    void allTasks() {
        List<Task> tasks = taskService.createTaskQuery().orderByTaskId().desc().list();
        for (Task task : tasks) {
            log.info("任务 ID：{}；任务处理人：{}；任务是否挂起：{}", task.getId(), task.getAssignee(), task.isSuspended());
        }
    }

    /**
     * 开启一个流程
     */
    @Test
    void askForLeave() {
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("leaveTask", staffId);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("ask_for_leave", variables);
        runtimeService.setVariable(processInstance.getId(), "name", "javaboy");
        runtimeService.setVariable(processInstance.getId(), "reason", "休息一下");
        runtimeService.setVariable(processInstance.getId(), "days", 10);
        log.info("创建请假流程 processId：{}", processInstance.getId());
    }

    /**
     * 提交给组长审批
     */
    @Test
    void submitToZuzhang() {
        //员工查找到自己的任务，然后提交给组长审批
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(staffId).orderByTaskId().desc().list();
        for (Task task : tasks) {
            log.info("任务 ID：{}；任务处理人：{}；任务是否挂起：{}", task.getId(), task.getAssignee(), task.isSuspended());
            Map<String, Object> variables = new HashMap<>();
            //提交给组长的时候，需要指定组长的 id
            variables.put("zuzhangTask", zuzhangId);
            taskService.complete(task.getId(), variables);
        }
    }

    /**
     * 组长查看自己的任务
     */
    @Test
    void zuzhangTaskList() {
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(zuzhangId).orderByTaskId().desc().list();
        for (Task task : tasks) {
            Map<String, Object> variables = taskService.getVariables(task.getId());
            log.info("请假人：{};请假时间：{} 天；请假理由：{}",variables.get("name"),variables.get("days"),variables.get("reason"));
            log.info("任务 ID：{}；任务处理人：{}；任务是否挂起：{}", task.getId(), task.getAssignee(), task.isSuspended());
        }
    }

    /**
     * 组长审批-批准
     */
    @Test
    void zuZhangApprove() {
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(zuzhangId).orderByTaskId().desc().list();
        for (Task task : tasks) {
            log.info("组长 {} 在审批 {} 任务", task.getAssignee(), task.getId());
            Map<String, Object> variables = new HashMap<>();
            //组长审批的时候，如果是同意，需要指定经理的 id
            variables.put("managerTask", managerId);
            variables.put("checkResult", "通过");
            taskService.complete(task.getId(), variables);
        }
    }

    /**
     * 组长审批-拒绝
     */
    @Test
    void zuZhangReject() {
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(zuzhangId).orderByTaskId().desc().list();
        for (Task task : tasks) {
            log.info("组长 {} 在审批 {} 任务", task.getAssignee(), task.getId());
            Map<String, Object> variables = new HashMap<>();
            //组长审批的时候，如果是拒绝，就不需要指定经理的 id
            variables.put("checkResult", "拒绝");
            taskService.complete(task.getId(), variables);
        }
    }

    /**
     * 经理查看自己的任务
     */
    @Test
    void managerTaskList() {
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(managerId).orderByTaskId().desc().list();
        for (Task task : tasks) {
            log.info("经理 {} 待审批的任务 ID：{}", task.getAssignee(), task.getId());
        }
    }

    /**
     * 经理审批自己的任务-批准
     */
    @Test
    void managerApprove() {
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(managerId).orderByTaskId().desc().list();
        for (Task task : tasks) {
            log.info("经理 {} 在审批 {} 任务", task.getAssignee(), task.getId());
            Map<String, Object> variables = new HashMap<>();
            variables.put("checkResult", "通过");
            taskService.complete(task.getId(), variables);
        }
    }

    /**
     * 经理审批自己的任务-拒绝
     */
    @Test
    void managerReject() {
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(managerId).orderByTaskId().desc().list();
        for (Task task : tasks) {
            log.info("经理 {} 在审批 {} 任务", task.getAssignee(), task.getId());
            Map<String, Object> variables = new HashMap<>();
            variables.put("checkResult", "拒绝");
            taskService.complete(task.getId(), variables);
        }
    }
    
}
