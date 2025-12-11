
package com.example.bean;

import java.util.List;

//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;

//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeAll;

public class MockitoMockAnnotationExample {

	@Mock
	List<String> mockList;
	
	@BeforeEach
	public void setup() {
		//if we don't call below, we will get NullPointerException
////		MockitoAnnotations.initMocks(this);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test() {
		when(mockList.get(0)).thenReturn("JournalDev");
		assertEquals("JournalDev", mockList.get(0));
	}
	
}