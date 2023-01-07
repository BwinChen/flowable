package com.bwin.flowable.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author McAfee
 */
@Slf4j
@AllArgsConstructor
@RestController
public class FlowableController {
    private final RepositoryService repositoryService;
    private final HistoryService historyService;
    private final ProcessEngine processEngine;

    @GetMapping("/diagram")
    public void generateDiagram(HttpServletResponse response, String processInstanceId) throws Exception {
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        if (processInstance == null) {
            return;
        }
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        List<String> highLightedActivities = new ArrayList<>();
        List<String> highLightedFlows = new ArrayList<>();
        List<HistoricActivityInstance> activityInstances = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstance.getId()).list();
        for (HistoricActivityInstance activityInstance : activityInstances) {
            if ("sequenceFlow".equals(activityInstance.getActivityType())) {
                highLightedFlows.add(activityInstance.getActivityId());
            } else {
                highLightedActivities.add(activityInstance.getActivityId());
            }
        }
        ProcessEngineConfiguration configuration = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator generator = configuration.getProcessDiagramGenerator();
        InputStream inputStream = generator.generateDiagram(bpmnModel, "png", highLightedActivities,
                highLightedFlows, configuration.getActivityFontName(), configuration.getLabelFontName(),
                configuration.getAnnotationFontName(), configuration.getClassLoader(), 1.0, true);
        IOUtils.copy(inputStream, response.getOutputStream());
    }

}
