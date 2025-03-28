package com.tastytreat.backend.tasty_treat_express_backend.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tastyTreatExpress.DTO.MenuItemDTO;
import com.tastyTreatExpress.DTO.MenuItemMapper;
import com.tastyTreatExpress.DTO.RestaurantDTO;
import com.tastyTreatExpress.DTO.RestaurantMapper;
import com.tastytreat.backend.tasty_treat_express_backend.exceptions.EmailAlreadyExistsException;
import com.tastytreat.backend.tasty_treat_express_backend.exceptions.ReportNotFoundException;
import com.tastytreat.backend.tasty_treat_express_backend.models.Feedback;
import com.tastytreat.backend.tasty_treat_express_backend.models.MenuItem;
import com.tastytreat.backend.tasty_treat_express_backend.models.Report;
import com.tastytreat.backend.tasty_treat_express_backend.models.Restaurant;
import com.tastytreat.backend.tasty_treat_express_backend.services.RestaurantService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

	@Autowired
	private RestaurantService restaurantService;

	// Add a restaurant
	@PostMapping("/register")
	public ResponseEntity<RestaurantDTO> saveRestaurant(@Valid @RequestBody Restaurant restaurant) {
		if (restaurantService.existsByEmail(restaurant.getEmail())) {
			throw new EmailAlreadyExistsException("Email is already registered.");
		} else {
			Restaurant savedRestaurant = restaurantService.saveRestaurant(restaurant);
			RestaurantDTO savedRestaurantDTO = RestaurantMapper.toRestaurantDTO(savedRestaurant);
			return new ResponseEntity<>(savedRestaurantDTO, HttpStatus.CREATED);
		}
	}

	// Authenticate a restaurant
	@PostMapping("/authenticate")
	public ResponseEntity<String> authenticateRestaurant(@RequestParam String email, @RequestParam String password) {
		boolean isAuthenticated = restaurantService.authenticateRestaurant(email, password);
		if (isAuthenticated) {
			return new ResponseEntity<>("Authentication successful", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
		}
	}

	// Check if a restaurant email exists
	@GetMapping("/exists")
	public ResponseEntity<Boolean> existsByEmail(@RequestParam String email) {
		boolean exists = restaurantService.existsByEmail(email);
		return new ResponseEntity<>(exists, HttpStatus.OK);
	}

	// Get all restaurants
	@GetMapping
	public ResponseEntity<List<RestaurantDTO>> findAll() {
		List<Restaurant> restaurants = restaurantService.findAll();
		List<RestaurantDTO> restaurantDTOs = restaurants.stream()
				.map(RestaurantMapper::toRestaurantDTO)
				.collect(Collectors.toList());
		return new ResponseEntity<>(restaurantDTOs, HttpStatus.OK);
	}

	// Get a restaurant by ID
	@GetMapping("/{restaurantId}")
	public ResponseEntity<RestaurantDTO> getRestaurantById(@PathVariable String restaurantId) {
		Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
		if (restaurant != null) {
			RestaurantDTO restaurantDTO = RestaurantMapper.toRestaurantDTO(restaurant);
			return new ResponseEntity<>(restaurantDTO, HttpStatus.OK);
		} else {
			throw new ReportNotFoundException("Restaurant doesnt exist with the given Id.");
			//return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	// Update restaurant details
	@PutMapping("/{restaurantId}")
	public ResponseEntity<RestaurantDTO> updateRestaurant(@PathVariable String restaurantId,
			@Valid @RequestBody Restaurant restaurant) {
		restaurant.setRestaurantId(restaurantId);
		Restaurant updatedRestaurant = restaurantService.updateRestaurant(restaurant);
		RestaurantDTO updatedRestaurantDTO = RestaurantMapper.toRestaurantDTO(updatedRestaurant);
		return new ResponseEntity<>(updatedRestaurantDTO, HttpStatus.OK);
	}

	// Delete a restaurant
	@DeleteMapping("/{restaurantId}")
	public ResponseEntity<String> deleteRestaurant(@PathVariable String restaurantId) {
		restaurantService.deleteRestaurant(restaurantId);
		return new ResponseEntity<>("Restaurant deleted successfully", HttpStatus.NO_CONTENT);
	}

	// Get a restaurant's menu
	@GetMapping("/{restaurantId}/menu")
	public ResponseEntity<List<MenuItem>> getRestaurantMenu(@PathVariable String restaurantId) {
		List<MenuItem> menuItems = restaurantService.getRestaurantMenu(restaurantId);
		return new ResponseEntity<>(menuItems, HttpStatus.OK);
	}

	// Add a menu item to a restaurant
	@PostMapping("/{restaurantId}/menu")
	public ResponseEntity<List<MenuItemDTO>> addMenuItem(@PathVariable String restaurantId,
			@RequestBody MenuItem menuItem) {
		Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
		if (restaurant == null) {
			throw new ReportNotFoundException("Restaurant does not exist with the given Id.");
		}
		menuItem.setRestaurant(restaurant);
		List<MenuItem> createdMenuItems = restaurantService.addMenuItem(restaurantId, menuItem);
		List<MenuItemDTO> createdMenuItemDTOs = MenuItemMapper.toMenuItemDTOList(createdMenuItems);
		return new ResponseEntity<>(createdMenuItemDTOs, HttpStatus.CREATED);
	}

	// Get a restaurant's feedbacks
	@GetMapping("/{restaurantId}/feedbacks")
	public ResponseEntity<List<Feedback>> getRestaurantFeedback(@PathVariable String restaurantId) {
		List<Feedback> feedbacks = restaurantService.getRestaurantFeedback(restaurantId);
		return new ResponseEntity<>(feedbacks, HttpStatus.OK);
	}

	// Add feedback to a restaurant
	@PostMapping("/{restaurantId}/feedbacks")
	public ResponseEntity<List<Feedback>> addFeedback(@PathVariable String restaurantId,
			@Valid @RequestBody Feedback feedback) {
		List<Feedback> updatedFeedbacks = restaurantService.addFeedback(restaurantId, feedback);
		return new ResponseEntity<>(updatedFeedbacks, HttpStatus.CREATED);
	}

	// Calculate the average rating of a restaurant
	@GetMapping("/{restaurantId}/rating")
	public ResponseEntity<Double> calculateAverageRating(@PathVariable String restaurantId) {
		double averageRating = restaurantService.calculateAverageRating(restaurantId);
		return new ResponseEntity<>(averageRating, HttpStatus.OK);
	}

	// Find restaurants by location
	@GetMapping("/search/location")
	public ResponseEntity<List<RestaurantDTO>> findRestaurantsByLocation(@RequestParam String location) {
		List<Restaurant> restaurants = restaurantService.findRestaurantsByLocation(location);
		List<RestaurantDTO> restaurantDTOs = restaurants.stream()
				.map(RestaurantMapper::toRestaurantDTO)
				.collect(Collectors.toList());
		return new ResponseEntity<>(restaurantDTOs, HttpStatus.OK);
	}

	// Find restaurants nearby
	@GetMapping("/search/nearby")
	public ResponseEntity<List<RestaurantDTO>> findRestaurantsNearby(
			@RequestParam double latitude,
			@RequestParam double longitude,
			@RequestParam double radiusKm) {
		List<Restaurant> nearbyRestaurants = restaurantService.findRestaurantsNearby(latitude, longitude, radiusKm);
		List<RestaurantDTO> nearbyRestaurantDTOs = nearbyRestaurants.stream()
				.map(RestaurantMapper::toRestaurantDTO)
				.collect(Collectors.toList());
		return new ResponseEntity<>(nearbyRestaurantDTOs, HttpStatus.OK);
	}

	// Get reports for a restaurant
	@GetMapping("/{restaurantId}/reports")
	public ResponseEntity<List<Report>> getReportsByRestaurant(@PathVariable String restaurantId) {
		List<Report> reports = restaurantService.getRestaurantReport(restaurantId);
		return new ResponseEntity<>(reports, HttpStatus.OK);
	}
}
