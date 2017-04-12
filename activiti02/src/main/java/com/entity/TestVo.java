package com.entity;

import java.io.Serializable;

//测试setVariable的序列化参数
public class TestVo implements Serializable{
	private static final long serialVersionUID = -4298055080783504358L;
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public TestVo(String name) {
		super();
		this.name = name;
	}
	
}
