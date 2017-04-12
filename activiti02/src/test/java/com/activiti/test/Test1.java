package com.activiti.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngineInfo;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.junit.Test;

import com.entity.TestVo;

public class Test1 {
	
	//创建用户组
		public static Group createGroup(IdentityService identityService, String id,
				String name, String type) {
			Group group = identityService.newGroup(id);
			group.setName(name);
			group.setType(type);
			identityService.saveGroup(group);
			return group;
		}
		//创建用户
		public static User createUser(IdentityService identityService,String id,String last,String first,
				String password,String email){
			User user=identityService.newUser(id);
			user.setFirstName(first);
			user.setLastName(last);
			user.setEmail(email);
			user.setPassword(password);
			identityService.saveUser(user);
			return user;
		}
	@org.junit.Test
	public void test1() {
		// 初始化ProcessEngines的Map
		// 加载Activiti默认的配置文件activiti.cfg.xml
		// 如果和spring整合，则读取classpath路径下的activiti-content.xml文件
		ProcessEngines.init();
		Map<String, ProcessEngine> engines = ProcessEngines.getProcessEngines();
		// 获取ProcessEngine
		System.out.println(engines.get("default"));
		System.out.println(engines.size());
	}

	@org.junit.Test
	public void test2() {
		// 注册和注销流程引擎实例
		ProcessEngineConfiguration config = ProcessEngineConfiguration
				.createProcessEngineConfigurationFromResource("activiti.cfg.xml");
		ProcessEngine engine = config.buildProcessEngine();
		Map<String, ProcessEngine> engines = ProcessEngines.getProcessEngines();
		System.out.println("流程实例个数：" + engines.size());
		// unregister注销流程实例,只是单纯的移除Map中的流程实例，不会调用ProcessEngines的close方法
		ProcessEngines.unregister(engine);
		System.out.println("注销后流程实例:" + engines.size());
	}

	@Test
	public void test3() {
		// 如果Activi加载配置文件出现异常，采用retry方法重新加载配置文件，重新创建ProcessEngine实例加入到Map中
		ClassLoader cl = Test.class.getClassLoader();
		URL url = cl.getResource("activiti.cfg.xml");
		ProcessEngineInfo engineinfo = ProcessEngines.retry(url.toString());
		Map<String, ProcessEngine> engines = ProcessEngines.getProcessEngines();
		System.out.println("流程实例个数：" + engines.size());
	}

	// 调用destory方法
	@Test
	public void test4() {
		ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
		Map<String, ProcessEngine> engines = ProcessEngines.getProcessEngines();
		System.out.println("调用destory之前流程实例个数：" + engines.size());
		// 销毁流程实例并调用close方法
		ProcessEngines.destroy();
		engines = ProcessEngines.getProcessEngines();
		System.out.println("调用destory之后流程实例个数：" + engines.size());
	}

	// 设置流程实例名称,注意设置的流程名称一定要在默认创建的流程引擎之前被调用，否则为空.输出结果为同一对象，说明bulidProceeEngine方法是完成register
	@Test
	public void test5() {
		ProcessEngineConfiguration config = ProcessEngineConfiguration
				.createProcessEngineConfigurationFromResource("activiti.cfg.xml");
		config.setProcessEngineName("test");// 设置流程实例名称
		ProcessEngine engine = config.buildProcessEngine();
		ProcessEngine engineTest = ProcessEngines.getProcessEngine("test");
		System.out.println("engine流程实例：" + engine);
		System.out.println("engineTest流程实例：" + engineTest);
		System.out.println(engine == engineTest);
	}

	// 添加用户组
	@Test
	public void test6() {
		ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
		// 获取身份信息服务
		IdentityService identityService = engine.getIdentityService();
		// 创建用户组,id不能为空，否则抛异常,版本号为0则会做插入处理
		// Group group=identityService.newGroup(null);
		Group group = identityService.newGroup("2");
		group.setName("人事组");
		group.setType("人事管理");
		identityService.saveGroup(group);
	}

	// 修改用户组
	@Test
	public void test7() {
		ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
		// 获取身份信息服务
		IdentityService identityService = engine.getIdentityService();
		Group group = identityService.createGroupQuery().groupId("2")
				.singleResult();
		group.setName("人事2组");
		identityService.saveGroup(group);
	}

	// 删除用户组，服务组件可以使用createXXXQuery查询对象
	@Test
	public void test8() {
		ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
		IdentityService identityService = engine.getIdentityService();
		System.out.println("用户组未删除前记录条数："
				+ identityService.createGroupQuery().count());
		identityService.deleteGroup("1");
		System.out.println("用户组删除后记录条数："
				+ identityService.createGroupQuery().count());
	}

	// 查询list,分页listPage
	@Test
	public void test9() {
		ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
		IdentityService identityService = engine.getIdentityService();
		createGroup(identityService, "1", "name1", "group1");
		createGroup(identityService, "2", "name2", "group2");
		createGroup(identityService, "3", "name3", "group3");
		createGroup(identityService, "4", "name4", "group4");
		createGroup(identityService, "5", "name5", "group5");
		List<Group> groups = identityService.createGroupQuery().list();
		for (Group group : groups) {
			System.out.println(group.getId() + "-->" + group.getName());
		}
		System.out.println("----------------------");
		// 分页，第一个参数是起始记录主键，第二个参数是记录数
		List<Group> groups1 = identityService.createGroupQuery().listPage(1, 2);
		for (Group group : groups1) {
			System.out.println(group.getId() + "-->" + group.getName());
		}
	}

	// 排序
	@Test
	public void test10() {
		ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
		IdentityService identityService = engine.getIdentityService();
		List<Group> groups = identityService.createGroupQuery()
				.orderByGroupId().desc().list();
		for (Group group : groups) {
			System.out.println(group.getId() + "->" + group.getName());
		}
	}

	// 查询唯一结果集,不存在抛空指针异常
	@Test
	public void test11() {
		ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
		IdentityService identityService = engine.getIdentityService();
		Group group = identityService.createGroupQuery().groupId("1")
				.singleResult();
		System.out.println(group.getId());
	}

	// 模糊查询
	@Test
	public void test12() {
		ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
		IdentityService identityService = engine.getIdentityService();
		List<Group> groups = identityService.createGroupQuery()
				.groupNameLike("%name%").list();
		for (Group group : groups) {
			System.out.println(group.getId() + "->" + group.getName());
		}
	}

	//用户的增删改查和用户组差不多，不做累述
	@Test
	public void test13(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		IdentityService identityService=engine.getIdentityService();
		User user=identityService.newUser("2");
		user.setFirstName("李");
		user.setLastName("三");
		user.setPassword("123");
		identityService.saveUser(user);
	}
	
	//添加和删除用户表信息
	@Test
	public void test14(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		IdentityService identityService=engine.getIdentityService();
		createUser(identityService,"4","钱","四","123456","1234@qq.com");
		//创建用户信息:setUserInfo(String userId,String key,String value)
		identityService.setUserInfo("3", "age", "40");
		identityService.setUserInfo("3", "weight", "60KG");
		//删除用户信息：deleteUserInfo(String userId,String key)
		identityService.deleteUserInfo("3", "age");
	}
	
	//查询用户信息
	@Test
	public void test15(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		IdentityService identityService=engine.getIdentityService();
		String value=identityService.getUserInfo("3", "weight");
		System.out.println("用户体重："+value);
	}
	
	
	//用户和用户组的绑定关系(用户和用户组关系表act_id_membership)
	@Test
	public void test16(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		IdentityService identityService=engine.getIdentityService();
		User user=createUser(identityService,"5","Ast","Lou","123","");
		Group group=createGroup(identityService,"3","xxxx","yyyy");
		identityService.createMembership(user.getId(), group.getId());
	}
	
	//解除用户组和用户的绑定关系
	@Test
	public void test17(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		IdentityService identityService=engine.getIdentityService();
		identityService.deleteMembership("5", "3");
	}
	
	//查询用户组下的用户
	@Test
	public void test18(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		IdentityService identityService=engine.getIdentityService();
		identityService.createMembership("1", "1");
		identityService.createMembership("2", "1");
		List<User>users=identityService.createUserQuery().memberOfGroup("1").list();
		for(User user:users){
			System.out.println(user.getFirstName());
		}
	}
	
	//查询用户所属的用户组
	@Test
	public void test19(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		IdentityService identityService=engine.getIdentityService();
		List<Group>groups=identityService.createGroupQuery().groupMember("1").list();
		for (Group group : groups) {
			System.out.println(group.getName());
		}
	}
	
	//部署流程1:addClasspathResource
	@Test
	public void test20(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService=engine.getRepositoryService();
		DeploymentBuilder deploymentBuilder=repositoryService.createDeployment();
		//DeploymentBuilder对象包含了一系列处理部署流程资源的方法
		deploymentBuilder.addClasspathResource("diagrams/first.bpmn");
		//执行部署
		deploymentBuilder.deploy();
	}
	//部署流程2:addInputStream
	@Test
	public void test21() throws FileNotFoundException{
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService=engine.getRepositoryService();
		DeploymentBuilder deploymentBuilder=repositoryService.createDeployment();
		InputStream inA=new FileInputStream(new File("E:\\work\\myeclipse_workspace\\activiti02\\src\\main\\resources\\diagrams\\two.png"));
		InputStream inB=new FileInputStream(new File("E:\\work\\myeclipse_workspace\\activiti02\\src\\main\\resources\\diagrams\\two.png"));
		deploymentBuilder.addInputStream("inputA", inA);
		deploymentBuilder.addInputStream("inputB", inB);
		//执行部署
		deploymentBuilder.deploy();
	}
	
	//流程部署3：addString,保存流程定义的属性或公用变量
	@Test
	public void test22(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService=engine.getRepositoryService();
		DeploymentBuilder deploymentBuilder=repositoryService.createDeployment();
		deploymentBuilder.addString("add String", "add deployment string.......");
		deploymentBuilder.deploy();
	}
	
	//流程部署：ZipInputStream
	@Test
	public void test23() throws FileNotFoundException{
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService=engine.getRepositoryService();
		DeploymentBuilder deploymentBuilder=repositoryService.createDeployment();
		FileInputStream in=new FileInputStream(new File("E:\\work\\myeclipse_workspace\\activiti02\\src\\main\\resources\\diagrams\\highlight.js-master.zip"));
		deploymentBuilder.addZipInputStream(new ZipInputStream(in));
		deploymentBuilder.deploy();
	}
	//修改部署名称
	@Test
	public void test24(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService=engine.getRepositoryService();
		DeploymentBuilder deploymentBuilder=repositoryService.createDeployment();
		deploymentBuilder.name("deploymentName");
		deploymentBuilder.deploy();
	}
	//过滤重复部署，包括部署名和内容
	@Test
	public void test25() throws FileNotFoundException{
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService=engine.getRepositoryService();
		DeploymentBuilder deploymentBuilderA=repositoryService.createDeployment();
		InputStream inB=new FileInputStream(new File("E:\\work\\myeclipse_workspace\\activiti02\\src\\main\\resources\\diagrams\\two.png"));
		deploymentBuilderA.addInputStream("inputB", inB);
		deploymentBuilderA.name("chongfu");
		//过滤重复部署
		deploymentBuilderA.enableDuplicateFiltering();
		deploymentBuilderA.deploy();
		DeploymentBuilder deploymentBuilderB=repositoryService.createDeployment();
		deploymentBuilderB.addInputStream("inputB", inB);
		deploymentBuilderB.name("chongfu");
		//过滤重复部署
		deploymentBuilderB.enableDuplicateFiltering();
		//执行部署
		deploymentBuilderB.deploy();
	}
	
	//流程定义
	@Test
	public void test26(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService=engine.getRepositoryService();
		Deployment dep=repositoryService.createDeployment().addClasspathResource("diagrams/first.bpmn")
		.addClasspathResource("diagrams/two.png").deploy();
		ProcessDefinition def=repositoryService.createProcessDefinitionQuery().deploymentId(dep.getId()).singleResult();
		System.out.println(def.getDiagramResourceName());
	}
	
	//中止和激活流程定义：suspension_state=1(激活)|2(中止),如果一个流程已经为中止状态，在调用中止方法会抛异常
	@Test
	public void test27(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService=engine.getRepositoryService();
		Deployment dep=repositoryService.createDeployment().addClasspathResource("diagrams/first.bpmn").deploy();
		ProcessDefinition def=repositoryService.createProcessDefinitionQuery().deploymentId(dep.getId()).singleResult();
		//中止流程定义
		repositoryService.suspendProcessDefinitionById(def.getId());
		//激活流程定义
		repositoryService.activateProcessDefinitionById(def.getId());
		//中止流程定义
		repositoryService.suspendProcessDefinitionByKey(def.getKey());
		//激活流程定义
		repositoryService.activateProcessDefinitionByKey(def.getKey());
	}
	
	//设置流程定义权限,绑定用户或用户组与流程定义,对应表是act_ru_identitylink,字段type的值是candidate
	@Test
	public void test28(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService=engine.getRepositoryService();
		//IdentityService identityService=engine.getIdentityService();
		Deployment dep=repositoryService.createDeployment().addClasspathResource("diagrams/first.bpmn").deploy();
		ProcessDefinition def=repositoryService.createProcessDefinitionQuery().deploymentId(dep.getId()).singleResult();
		//绑定用户权限
		//User user=createUser(identityService,"1","first","last","123",null);
		//repositoryService.addCandidateStarterUser(def.getId(), user.getId());
		//绑定用户组权限
		repositoryService.addCandidateStarterGroup(def.getId(), "1");
	}
	
	//查询权限数据
	@Test
	public void test29(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService=engine.getRepositoryService();
		IdentityService identityService=engine.getIdentityService();
		Deployment dep=repositoryService.createDeployment().addClasspathResource("diagrams/first.bpmn").deploy();
		ProcessDefinition def=repositoryService.createProcessDefinitionQuery().deploymentId(dep.getId()).singleResult();
		repositoryService.addCandidateStarterGroup(def.getId(), "1");
		repositoryService.addCandidateStarterUser(def.getId(), "1");
		//查询用户有权限的流程定义数据
		List<ProcessDefinition>processDefinitions=repositoryService.createProcessDefinitionQuery().startableByUser("1")
				.list();
		for (ProcessDefinition processDefinition : processDefinitions) {
			System.out.println(processDefinition.getDiagramResourceName());
		}
		//根据流程数据查询权限用户组
		List<Group>group=identityService.createGroupQuery().potentialStarter(def.getId()).list();
		for (Iterator iterator = group.iterator(); iterator.hasNext();) {
			Group group2 = (Group) iterator.next();
			System.out.println(group2.getName());
		}
		//根据流程数据查询权限用户
		List<User>users=identityService.createUserQuery().potentialStarter(def.getId()).list();
		for (User user : users) {
			System.out.println(user.getId());
		}
		//根据流程定义查询所有相关的权限用户和用户组
		List<IdentityLink>links=repositoryService.getIdentityLinksForProcessDefinition(def.getId());
		System.out.println(links.size());
	}
	
	//查询部署资源,实际是根据部署流程id和流程部署名从act_ge_bytearray表中查询部署资源
	//getResourceAsStream(String deploymentId,String resourceName)
	@Test
	public void test30() throws IOException{
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService=engine.getRepositoryService();
		Deployment dep=repositoryService.createDeployment().addClasspathResource("diagrams/first.bpmn").deploy();
		InputStream in=repositoryService.getResourceAsStream(dep.getId(), "diagrams/first.bpmn");
		System.out.println(dep.getId());
		int len=in.available();
		byte[] content=new byte[len];
		in.read(content);
		String result=new String(content);
		System.out.println(result);
		in.close();
	}
	
	
	//根据act_re_procdef表的id(流程定义ID)查询出流程定义文件(XML文件)
	@Test
	public void test31() throws IOException{
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService=engine.getRepositoryService();
		Deployment dep=repositoryService.createDeployment().addClasspathResource("diagrams/two.bpmn").addClasspathResource("diagrams/two.png").deploy();
		ProcessDefinition def=repositoryService.createProcessDefinitionQuery().deploymentId(dep.getId()).singleResult();
		InputStream in=repositoryService.getProcessModel(def.getId());
		System.out.println(dep.getId());
		int len=in.available();
		byte[] content=new byte[len];
		in.read(content);
		String result=new String(content);
		System.out.println(result);
		in.close();
	}
	
	
	//查询流程图
	@Test
	public void test32() throws IOException{
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService=engine.getRepositoryService();
		Deployment dep=repositoryService.createDeployment().addClasspathResource("diagrams/two.bpmn").addClasspathResource("diagrams/two.png").deploy();
		ProcessDefinition def=repositoryService.createProcessDefinitionQuery().deploymentId(dep.getId()).singleResult();
		System.out.println(dep.getId());
		InputStream in=repositoryService.getProcessDiagram(def.getId());
		BufferedImage image=ImageIO.read(in);
		File file=new File("src/main/resources/image/result.png");
		if(!file.exists()){
			file.createNewFile();
		}
		FileOutputStream out=new FileOutputStream(file);
		ImageIO.write(image, "png", out);
		in.close();
		out.close();
	}
	
	//查询部署资源名称
	@Test
	public void test33(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService=engine.getRepositoryService();
		Deployment dep=repositoryService.createDeployment().addClasspathResource("diagrams/two.bpmn").addClasspathResource("diagrams/two.png").name("two").deploy();
		System.out.println(dep.getName());//查询部署名
		//查询部署资源名称
		List<String>deploymentNames=repositoryService.getDeploymentResourceNames(dep.getId());
		for (String deploymentName : deploymentNames) {
			System.out.println(deploymentName);
		}
	}
	
	//删除部署资源，无论是级联还是非级联都会删除部署数据，无论是身份认证还是流程定义等
	@Test
	public void test34(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService=engine.getRepositoryService();
		Deployment dep=repositoryService.createDeployment().addClasspathResource("diagrams/two.bpmn").name("two1").deploy();
		System.out.println(dep.getId());
		//删除部署资源，不会进行级联删除
		repositoryService.deleteDeployment(dep.getId());
		//级联删除
		//repositoryService.deleteDeployment(dep.getId(),true);
	}
	
	//查询部署对象Deployment
	@Test
	public void test35(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService=engine.getRepositoryService();
		List<Deployment>deps=repositoryService.createDeploymentQuery().list();
		System.out.println(deps.size());
		//排序查询:desc()、asc()
		List<Deployment>deps2=repositoryService.createDeploymentQuery().orderByDeploymentId().asc().list();
		for (Deployment deployment : deps2) {
			System.out.println(deployment.getId()+":"+deployment.getName());
		}
		List<ProcessDefinition>processDefinitions=repositoryService.createProcessDefinitionQuery().list();
		System.out.println(processDefinitions.size());
	}
	
	//创建任务
	@Test
	public void test36(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		TaskService taskService=engine.getTaskService();
		//创建第一个无id的任务
		Task task1=taskService.newTask();
		taskService.saveTask(task1);
		//创建有ID的任务
		Task task2=taskService.newTask("审批流程");
		taskService.saveTask(task2);
	}
	
	//删除任务
	@Test
	public void test37(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		TaskService taskService=engine.getTaskService();
		//Task task1=taskService.newTask("1");
		//taskService.saveTask(task1);
		//创建有ID的任务
		//Task task2=taskService.newTask("2");
		//taskService.saveTask(task2);
		//taskService.deleteTask("1");//根据ID删除任务
		//taskService.deleteTask("2", true);//级联删除
		List<String>ids=new ArrayList<String>();
		ids.add("1");
		ids.add("2");
		taskService.deleteTasks(ids);;//根据ID集合删除任务
		//根据ID集合级联删除任务，包括act_ru_task表和act_hi_taskinst表中的记录，记录不存在则忽略
		taskService.deleteTasks(ids,true);
		
	}
	
	//查询任务
	@Test
	public void test38(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		TaskService taskService=engine.getTaskService();
		List<Task>tasks=taskService.createTaskQuery().list();
		System.out.println(tasks.size());
		Task task=taskService.createTaskQuery().taskId("审批流程").singleResult();
		System.out.println(task.getCreateTime());
	}
	
	//设置任务权限，activiti只提供绑定用户组和用户的接口，不会对权限进行拦截。任务的持有用户和受理用户只能有一个，而候选用户或候选用户组可以有多个
	@Test
	public void test39(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		TaskService taskService=engine.getTaskService();
		Task task1=taskService.newTask("task3");
		Task task2=taskService.newTask("task4");
		taskService.saveTask(task1);
		taskService.saveTask(task2);
		//绑定任务与用户组(设置候选用户组)
		taskService.addCandidateGroup("task3", "2");
		//绑定任务与用户(设置候选用户)
		taskService.addCandidateUser("task3", "1");
	}
	
	//查询权限任务数据
	@Test
	public void test40(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		TaskService taskService=engine.getTaskService();
		//根据候选用户ID查询任务
		List<Task>tasks1=taskService.createTaskQuery().taskCandidateUser("1").list();
		System.out.println(tasks1.size());
		//根据候选用户组ID查询任务
		List<Task>tasks2=taskService.createTaskQuery().taskCandidateGroup("2").list();
		System.out.println(tasks2.size());
		
		//查询任务权限数据
		List<IdentityLink>links=taskService.getIdentityLinksForTask(tasks2.get(0).getId());
		System.out.println(links.size());
	}
	
	//设置任务持有者和查询持有者任务，有关表act_ru_task和act_hi_taskinsk
	@Test
	public void test41(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		TaskService taskService=engine.getTaskService();
		IdentityService identityService=engine.getIdentityService();
		User user=createUser(identityService,"user22","last","first","123",null);
		Task task=taskService.newTask("task22");
		task.setName("请假流程");
		//设置任务持有者
		//task.setOwner(user.getId());
		taskService.saveTask(task);
		//设置任务持有者
		taskService.setOwner(task.getId(), user.getId());
		//查询任务持有者
		System.out.println(user.getLastName()+"任务个数:"+taskService.createTaskQuery().taskOwner(user.getId()).count());
	}
	//设置任务受理人
	@Test
	public void test42(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		TaskService taskService=engine.getTaskService();
		Task task=taskService.createTaskQuery().taskId("task22").singleResult();
		//task.setAssignee("1");//设置任务受理人
		taskService.setAssignee(task.getId(), "1");
		System.out.println("任务受理人任务个数："+taskService.createTaskQuery().taskAssignee("1").list().size());
	}
	
	
	//添加任务权限数据
	@Test
	public void test43(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		TaskService taskService=engine.getTaskService();
		//任务task id="102501",user id="1",group id="1"
		//addGroupIdentityLink(String taskId, String groupId,String IdentityLinkType)
		//等同于addCandidateGroup(String takId, String groupId)
		//addGroupIdentityLink的IdentityLinkType的值只能是IdentityLinkType.CANDIATE权限类型标识
		taskService.addGroupIdentityLink("102501", "1", IdentityLinkType.CANDIDATE);
		
		//等同于addCandidateUser(String taskId,St"102501"ring userId),IdentityLinkType的值有三种
		taskService.addUserIdentityLink("102501", "1", IdentityLinkType.CANDIDATE);
		//taskService.addUserIdentityLink("102501", "1", IdentityLinkType.ASSIGNEE);
		//taskService.addUserIdentityLink("102501", "1", IdentityLinkType.OWNER);
	}
	
	//删除用户组和用户权限:数据表act_ru_identitylink
	@Test
	public void test44(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		TaskService taskService=engine.getTaskService();
		taskService.deleteCandidateGroup("102501", "1");
		//与taskService.deleteCandidateGroup("102501", "1")等同效果
		//taskService.deleteGroupIdentityLink("102501", "1", IdentityLinkType.CANDIDATE);
		
		taskService.deleteCandidateUser("102501", "1");
		//taskService.deleteUserIdentityLink("102501", "1", IdentityLinkType.CANDIDATE);
	}
	
	//设置变量参数，相关表act_ru_variable
	@Test
	public void test45(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		short s=1;
		Date d=new Date();
		TaskService taskService=engine.getTaskService();
		taskService.setVariable("task22", "arg0", 1);
		taskService.setVariable("task22", "arg1", s);
		taskService.setVariable("task22", "arg2", d);
		taskService.setVariable("task22", "arg3", 10L);
		taskService.setVariable("task22", "arg4", false);
		taskService.setVariable("task22", "arg5", "test");
		taskService.setVariable("task22", "arg6", 2.3D);
		taskService.setVariable("task22", "arg7", null);
	}
	
	//序列化任务参数：setVariable(String taskId,String variableName,Object obj),会将obj对象序列化，同时act_ru_variable
	//表中保存的Type=serializable,BYTEARRAY_ID=(ACT_GE_BYTEARRAY中的ID).
	@Test
	public void test46(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		TaskService taskService=engine.getTaskService();
		taskService.setVariable("task22", "arg0", new TestVo("ghj"));
	}
	
	//获取任务参数
	@Test
	public void test47(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		TaskService taskService=engine.getTaskService();
		Object obj=taskService.getVariable("task22", "arg0");
		System.out.println(obj);
	}
	//设置任务参数的作用域
	@Test
	public void test48(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		TaskService taskService=engine.getTaskService();
		//局部作用域参数，仅在当前任务中使用
		taskService.setVariableLocal("task22", "arg8", 12);
		//全局参数
		taskService.setVariable("task22", "arg9", 112);
		System.out.println(taskService.getVariable("task22", "arg8"));
		System.out.println(taskService.getVariableLocal("task22", "arg9"));
	}
	
	//设置任务附件
	//createAttachment(String attachmentType,String taskId,String processInstanceId,String attachmentName,String  attachmentDescription,String url)
	//createAttachment(String attachmentType,String taskId,String processInstanceId,String attachmentName,String  attachmentDescription,InputStream content)
	@Test
	public void test49() throws FileNotFoundException{
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		TaskService taskService=engine.getTaskService();
		RepositoryService repositoryService=engine.getRepositoryService();
		Deployment dep=repositoryService.createDeployment().addClasspathResource("diagrams/first.bpmn").deploy();
		ProcessDefinition def=repositoryService.createProcessDefinitionQuery().deploymentId(dep.getId()).singleResult();
		//启动流程实例
		ProcessInstance pi=engine.getRuntimeService().startProcessInstanceById(def.getId());
		Task task=taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
		//创建任务附件
		taskService.createAttachment("web url", task.getId(), pi.getId(), "附件1", "附件描述", "http://baidu.com");
		FileInputStream in=new FileInputStream(new File("src/main/resources/diagrams/two.png"));
		//以输入流的方式创建任务附件
		taskService.createAttachment("web url1", task.getId(), pi.getId(), "附件11", "附件描述11",in);
	}
	
	//查询附件集合
	@Test
	public void test50(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		TaskService taskService=engine.getTaskService();
		Attachment attachment=taskService.getAttachment("142509");//根据附件id查询附件
		System.out.println(attachment);
		List<Attachment>attachments=taskService.getTaskAttachments("142508");//根据taskId查询附件
		System.out.println(attachments.size());
		List<Attachment>attachments1=taskService.getProcessInstanceAttachments("142505");//根据流程实例ID查询
		System.out.println(attachments1.size());
		InputStream in=taskService.getAttachmentContent("142511");//根据附件ID查询到输入流
		System.out.println(in);
	}
	
	//删除附件
	@Test
	public void test51(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		TaskService taskService=engine.getTaskService();
		taskService.deleteAttachment("142511");//根据附件ID删除
	}
	
	//添加任务评论和查询任务评论
	@Test
	public void test52(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		TaskService taskService=engine.getTaskService();
		RepositoryService repositoryService=engine.getRepositoryService();
		Deployment dep=repositoryService.createDeployment().addClasspathResource("diagrams/first.bpmn").deploy();
		ProcessDefinition def=repositoryService.createProcessDefinitionQuery().deploymentId(dep.getId()).singleResult();
		//启动流程实例
		ProcessInstance pi=engine.getRuntimeService().startProcessInstanceById(def.getId());
		Task task=taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
		taskService.addComment(task.getId(), pi.getId(), "任务评论！");
		taskService.addComment(task.getId(), pi.getId(), "任务评论1！");
		List<Comment>comments=taskService.getTaskComments(task.getId());
		System.out.println(comments.size());
	}
	
	//简单的请假流程：任务完成complete(taskId)和complete(taskId,map)
	//任务声明：使用claim(taskId,userId) 任务受理人
	@Test
	public void test53(){
		ProcessEngine engine=ProcessEngines.getDefaultProcessEngine();
		TaskService taskService=engine.getTaskService();
		RepositoryService repositoryService=engine.getRepositoryService();
		Deployment dep=repositoryService.createDeployment().addClasspathResource("diagrams/Leave.bpmn").deploy();
		ProcessDefinition def=repositoryService.createProcessDefinitionQuery().deploymentId(dep.getId()).singleResult();
		//启动流程实例
		ProcessInstance pi=engine.getRuntimeService().startProcessInstanceById(def.getId());
		Task task=taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
		//调用complete
		Map<String,Object>map=new HashMap<String,Object>();
		map.put("days", 5);
		taskService.complete(task.getId(), map);
		//再次查询
		task=taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
		Integer days=(Integer) taskService.getVariable(task.getId(), "days");
		if(days>4){
			System.out.println("大于5天不批准！");
		}else{
			//小于5天完成审批流程
			taskService.complete(task.getId());
			System.out.println("批准！");
		}
	}
	

}