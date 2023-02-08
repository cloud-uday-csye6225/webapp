package com.neu.cloud.cloudapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "username", unique = true)
	private String username;

	@Column(name = "password")
	private String password;

	@Column(name = "account_created")
	private String accountCreated;

	@Column(name = "account_updated")
	private String accountUpdated;

//	@OneToMany(cascade = CascadeType.ALL)
//	@JoinColumn(name = "owner_user_id", referencedColumnName = "id")
//	private List<Product> products = new ArrayList<>();

//	@OneToMany
//	@JoinColumn(name = "id") // we need to duplicate the physical information
//	private List<Product> products;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAccountCreated() {
		return accountCreated;
	}

	public void setAccountCreated(String accountCreated) {
		this.accountCreated = accountCreated;
	}

	public String getAccountUpdated() {
		return accountUpdated;
	}

	public void setAccountUpdated(String accountUpdated) {
		this.accountUpdated = accountUpdated;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", username=" + username
				+ ", password=" + password + ", accountCreated=" + accountCreated + ", accountUpdated=" + accountUpdated
				+ "]";
	}

}
