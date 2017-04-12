
package com.activit;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;

public class First {

	public static void main(String[] args) {
		//创建默认流程引擎
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		//得到流程存储服务组
		RepositoryService repositoryService=engine.getRepositoryService();
		//得到流程运行时的服务组件
		RuntimeService runtimeService=engine.getRuntimeService();
		//得到流程任务组件
		TaskService taskService=engine.getTaskService();
		//部署流程文件
		repositoryService.createDeployment().addClasspathResource("diagrams/first.bpmn").deploy();
		//启动流程
		runtimeService.startProcessInstanceByKey("myProcess");
		//查询第一个任务
		Task task=taskService.createTaskQuery().singleResult();
		System.out.println("完成第一个任务前,第一个任务名称："+task.getName());
		//完成第一个任务
		taskService.complete(task.getId());
		//查询第二个任务
		task=taskService.createTaskQuery().singleResult();
		System.out.println("完成第二个任务前,第二个任务名称："+task.getName());
		//完成第二个任务
		taskService.complete(task.getId());
		task=taskService.createTaskQuery().singleResult();
		System.out.println("流程结束："+task);

	}

}
