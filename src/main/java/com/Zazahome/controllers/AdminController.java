package com.Zazahome.controllers;

import java.io.IOException;
import java.security.Principal;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.Zazahome.daos.CategoryRepository;
import com.Zazahome.daos.ProductRepository;
import com.Zazahome.daos.UserRepository;
import com.Zazahome.entities.Category;
import com.Zazahome.entities.Product;
import com.Zazahome.entities.User;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private ProductRepository productRepo;

	@Autowired
	private CategoryRepository categoryRepo;

	@GetMapping(value = { "/", "/home" })
	public String home(Model m, Principal principal) {

		User user = this.userRepo.loadUserByUserName(principal.getName());

		List<User> users = this.userRepo.getUsers();
		List<Category> categories = this.categoryRepo.getCategories();
		List<Product> products = this.productRepo.getProducts();

		m.addAttribute("title", "Admin | Zazahome");
		m.addAttribute("user", user);
		m.addAttribute("users", users);
		m.addAttribute("products", products);
		m.addAttribute("categories", categories);
		m.addAttribute("category", new Category());

		return "admin/index";
	}

	@GetMapping("/action")
	public String action(@RequestParam(value = "user_id", required = false) Integer id,
			@RequestParam(name = "user", required = false) String type,
			@RequestParam(name = "category", required = false) String categoryAction,
			@RequestParam(name = "title", required = false) String categoryTitle, Principal principal,
			HttpSession httpSession) throws IOException {

		/* Suspending or deleting user */

		if (type != null) {

			if (type.equals("suspend")) {

				User user = this.userRepo.getById(id);
				user.setEnable(false);
				this.userRepo.save(user);

				httpSession.setAttribute("status", "suspended");
				return "redirect:/admin/home?userSuspendById=" + id;

			}

			else if (type.equals("unsuspend")) {
				com.Zazahome.entities.User user = this.userRepo.getById(id);
				user.setEnable(true);
				this.userRepo.save(user);

				httpSession.setAttribute("status", "unsuspended");
				return "redirect:/admin/home?userUnsuspendById=" + id;
			}

			if (type.equals("delete")) {

				com.Zazahome.entities.User user = this.userRepo.getById(id);
				this.userRepo.delete(user);

				httpSession.setAttribute("status", "user-deleted");
				return "redirect:/admin/home?userDeleteById=" + id;

			}

		}


		try {
			if (categoryAction.equals("addNew")) {

				if (categoryTitle.equals("") || categoryTitle == null || categoryTitle.isBlank()) {
					httpSession.setAttribute("status", "title-null");
					return "redirect:/admin/home";
				}

				Category category = new Category();
				category.setTitle(categoryTitle);

				this.categoryRepo.save(category);
				httpSession.setAttribute("status", "category-added");
				return "redirect:/admin/home";

			}

		} catch (DataIntegrityViolationException e) {
			httpSession.setAttribute("status", "category-already-exist");
			return "redirect:/admin/home";
		}


		httpSession.setAttribute("status", "went-wrong");
		return "redirect:/admin/home";

	}

}
