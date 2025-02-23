package com.chtrembl.petstoreapp.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import com.microsoft.applicationinsights.telemetry.PageViewTelemetry;
import com.microsoft.applicationinsights.TelemetryClient;

/**
 * Session based for each user, each user will also have a unique Telemetry
 * Client instance.
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@SuppressWarnings("serial")
public class User implements Serializable {
	private String name = "Guest";
	private String sessionId = null;
	private String email = null;

	private String jSessionId = null;
	private String csrfToken = null;

	// intentionally avoiding spring cache to ensure service calls are made each
	// time to show Telemetry with APIM requests
	private List<Pet> pets;
	List<Pet> pets = this.webClient.get().uri("/v2/pet/findByStatus?status={status}", "available")
				.header("session-id", this.sessionUser.getSessionId()).accept(MediaType.APPLICATION_JSON)
				.header("Ocp-Apim-Subscription-Key", this.containerEnvironment.getPetStoreServiceSubscriptionKey())
				.header("Ocp-Apim-Trace", "true").retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<Pet>>() {
				}).block();
	// intentionally avoiding spring cache to ensure service calls are made each
	// time to show Telemetry with APIM requests
	private List<Product> products;

	@Autowired(required = false)
	private transient TelemetryClient telemetryClient;

	@Autowired
	private ContainerEnvironment containerEnvironment;

	private int cartCount = 0;

	private boolean initialTelemetryRecorded = false;

	@Autowired
	private User sessionUser;
	...
	this.sessionUser.getTelemetryClient().getContext().getSession().setIsNewSession(true);
	this.sessionUser.getTelemetryClient().getContext().getSession().setId(this.sessionUser.getSessionId());
	// Track an Event
	this.sessionUser.getTelemetryClient()
		.trackEvent(String.format("PetStoreApp %s logged in, container host: %s",
					  this.sessionUser.getName(), this.containerEnvironment.getContainerHostName()));
	...
	// Track a Page View
	PageViewTelemetry pageViewTelemetry = new PageViewTelemetry();
	pageViewTelemetry.setUrl(new URI(request.getRequestURL().toString()));
	pageViewTelemetry.setName("Account Landing Page");
	this.sessionUser.getTelemetryClient().trackPageView(pageViewTelemetry);
	
	...
	// Track an Exception
	this.sessionUser.getTelemetryClient().trackException(new NullPointerException("sample null pointer"));

	@PostConstruct
	private void initialize() {
		if (this.telemetryClient == null) {
			this.telemetryClient = new TelemetryClient();
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setJSessionId(String jSessionId) {
		this.jSessionId = jSessionId;
	}
	
	public String getJSessionId() {
		return jSessionId;
	}
	
	public void setCsrfToken(String csrfToken) {
		this.csrfToken = csrfToken;
	}

	public String getCsrfToken() {
		return csrfToken;
	}
	
	public TelemetryClient getTelemetryClient() {
		return this.telemetryClient;
	}

	public List<Pet> getPets() {
		return pets;
	}

	public synchronized void setPets(List<Pet> pets) {
		this.pets = pets;
	}

	public List<Product> getProducts() {
		return products;
	}

	public synchronized void setProducts(List<Product> products) {
		this.products = products;
	}

	public int getCartCount() {
		return cartCount;
	}

	public void setCartCount(int cartCount) {
		this.cartCount = cartCount;
	}

	public boolean isInitialTelemetryRecorded() {
		return initialTelemetryRecorded;
	}

	public void setInitialTelemetryRecorded(boolean initialTelemetryRecorded) {
		this.initialTelemetryRecorded = initialTelemetryRecorded;
	}

	public Map<String, String> getCustomEventProperties() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("session_Id", this.sessionId);
		properties.put("containerHostName", this.containerEnvironment.getAppDate());
		properties.put("containerHostName", this.containerEnvironment.getAppVersion());
		properties.put("containerHostName", this.containerEnvironment.getContainerHostName());
		return properties;
	}
}
