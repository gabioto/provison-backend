package com.sinbugs.contacts.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.dozer.Mapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import pe.telefonica.provision.api.ProvisionRequest;
import pe.telefonica.provision.dto.Provision;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class MappingTest {
	
//	@Autowired
	Mapper mapper;
	
//	@Test
	public void fromRequestToEntity() {
		Provision c = new Provision(); 
		ProvisionRequest req = mapper.map(c, ProvisionRequest.class);
		
		/*assertThat(req)
			.hasFieldOrPropertyWithValue("id", c.getId())
			.hasFieldOrPropertyWithValue("firstName", c.getFirstName())
			.hasFieldOrPropertyWithValue("lastName", c.getLastName())
			.hasFieldOrPropertyWithValue("phoneNumber", c.getPhoneNumber())
			.hasFieldOrPropertyWithValue("email", c.getEmail());*/
	}

}
