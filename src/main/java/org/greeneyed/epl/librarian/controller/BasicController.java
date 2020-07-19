package org.greeneyed.epl.librarian.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Controller
@Data
@RequestMapping(value = "/librarian")
@RequiredArgsConstructor(onConstructor = @__({ @Autowired }))
public class BasicController {

	@RequestMapping(value = "/")
	public String main() {
		return "main";
	}
}