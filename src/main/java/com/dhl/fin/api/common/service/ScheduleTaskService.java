package com.dhl.fin.api.common.service;

import cn.hutool.core.collection.CollectionUtil;
import com.dhl.fin.api.common.annotation.SchedulePlan;
import com.dhl.fin.api.common.enums.CacheKeyEnum;
import com.dhl.fin.api.common.enums.TimeUnitEnum;
import com.dhl.fin.api.common.util.MapUtil;
import com.dhl.fin.api.common.util.ObjectUtil;
import com.dhl.fin.api.common.util.SpringContextUtil;
import com.dhl.fin.api.common.util.StringUtil;
import com.dhl.fin.api.common.util.mail.MailUtil;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScheduleTaskService implements SchedulingConfigurer {

    @Autowired
    private RedisService redisService;


    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

        String springEnv = SpringContextUtil.getPropertiesValue("spring.profiles.active").toUpperCase();
        List<Class> timerObjectList = ObjectUtil.getClasses("com.dhl.fin.api.timer")
                .stream().filter(p -> StringUtil.isNotEmpty(p.getSimpleName())).collect(Collectors.toList());


        if (CollectionUtil.isNotEmpty(timerObjectList)) {
            for (Class aClass : timerObjectList) {
                Object timerClass = SpringContextUtil.getBean(aClass);
                Arrays.stream(aClass.getDeclaredMethods())
                        .filter(p -> p.getAnnotation(SchedulePlan.class) != null)
                        .forEach(p -> {
                            String name = p.getAnnotation(SchedulePlan.class).name();
                            String cron = p.getAnnotation(SchedulePlan.class).cron();
                            TimeUnitEnum timeUnit = p.getAnnotation(SchedulePlan.class).timeUnit();
                            long period = p.getAnnotation(SchedulePlan.class).period();
                            String env = p.getAnnotation(SchedulePlan.class).env().toUpperCase();
                            String email = p.getAnnotation(SchedulePlan.class).email();

                            String scheduleName = StringUtil.isEmpty(name) ? p.getName() : name;

                            taskRegistrar.addTriggerTask(
                                    () -> {
                                        try {
                                            if (env.contains(springEnv)) {
                                                log.info(String.format("开始执行调度任务-%s,位置：%s.%s", scheduleName, aClass.getSimpleName(), p.getName()));
                                                p.invoke(timerClass);
                                                log.info("结束调度任务-" + scheduleName);
                                            } else {
                                                log.info(String.format("当前env是%s, 调度配置的env是%s,不一致所以不执行[%s]Task", springEnv, env, name));
                                            }
                                        } catch (Exception e) {
                                            try {
                                                Throwable exception = ((InvocationTargetException) e).getTargetException();
                                                log.error("调度任务执行失败");
                                                List<String> emails = redisService.getList(CacheKeyEnum.SUPER_MANAGER, Map.class).stream().map(user -> MapUtil.getString(user, "email")).collect(Collectors.toList());
                                                InetAddress addr = InetAddress.getLocalHost();
                                                RedisService redisService = SpringContextUtil.getBean(RedisService.class);
                                                String appcode = SpringContextUtil.getPropertiesValue("custom.projectCode");
                                                Map project = redisService.getProject(appcode);
                                                String appName = ObjectUtil.notNull(project) ? MapUtil.getString(project, "enName") : appcode;
                                                String position = aClass.getName() + "." + p.getName();

                                                String rootMessage = e.getCause().getMessage();
                                                StringBuilder errorMsg = new StringBuilder("&nbsp;&nbsp;&nbsp;&nbsp;" + rootMessage + "<br/>");
                                                log.error("    " + rootMessage);
                                                Arrays.stream(exception.getStackTrace()).forEach(msg -> {
                                                    errorMsg.append("&nbsp;&nbsp;&nbsp;&nbsp;" + msg.toString() + "<br/>");
                                                    log.error("    " + msg.toString());
                                                });
                                                emails = StringUtil.isEmpty(email) ? emails : Arrays.stream(email.split(";")).collect(Collectors.toList());

                                                String content = String.format("服务器地址：%s<br/>环境: %s<br/>Module：%s<br/>调度任务：%s<br/>Method位置：%s<br/>失败原因：<br/>%s", addr.getHostAddress(), springEnv, appName, scheduleName, position, errorMsg);
                                                MailUtil.builder()
                                                        .setTitle("[" + springEnv + "] FINTP平台调度任务执行失败")
                                                        .setContent(content)
                                                        .addAllTo(emails)
                                                        .build()
                                                        .send();

                                            } catch (MessagingException e1) {
                                                e1.printStackTrace();
                                            } catch (IOException e1) {
                                                e1.printStackTrace();
                                            } catch (TemplateException e1) {
                                                e1.printStackTrace();
                                            }
                                        }
                                    },
                                    triggerContext -> {
                                        if (StringUtil.isNotEmpty(cron)) {
                                            return new CronTrigger(cron).nextExecutionTime(triggerContext);
                                        } else if (ObjectUtil.notNull(timeUnit) && period > 0) {
                                            switch (timeUnit) {
                                                case DAY:
                                                    return new PeriodicTrigger(period, TimeUnit.DAYS).nextExecutionTime(triggerContext);
                                                case HOUR:
                                                    return new PeriodicTrigger(period, TimeUnit.HOURS).nextExecutionTime(triggerContext);
                                                case MINUTE:
                                                    return new PeriodicTrigger(period, TimeUnit.MINUTES).nextExecutionTime(triggerContext);
                                                case SECOND:
                                                    return new PeriodicTrigger(period, TimeUnit.SECONDS).nextExecutionTime(triggerContext);
                                            }
                                        }
                                        log.info("没有定义cron 或者 period，所以不执行task：" + name);
                                        return null;
                                    });
                        });
            }
        }
    }
}








