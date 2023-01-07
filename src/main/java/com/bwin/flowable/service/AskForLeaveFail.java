package com.bwin.flowable.service;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * @author McAfee
 */
@Slf4j
public class AskForLeaveFail implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        log.info("请假失败。。。");
    }

}
