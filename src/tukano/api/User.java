package tukano.api;

import jakarta.persistence.Id;
import jakarta.persistence.Entity;

@Entity
public class User {
	
	@Id
	private String userId;
	private String displayName;
	private String pwd;
	private String email;

	public User() {}

	public User(String userId, String pwd, String email, String displayName) {
		this.pwd = pwd;
		this.email = email;
		this.userId = userId;
		this.displayName = displayName;
	}
	
	public void update(User user) {
		this.pwd = user.pwd != null ? user.pwd : this.pwd;
		this.email = user.email != null ? user.email : this.email;
		this.displayName = user.displayName != null ? user.displayName : this.displayName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String userId() {
		return userId;
	}

	public String pwd() {
		return pwd;
	}

	public String email() {
		return email;
	}

	public String displayName() {
		return displayName;
	}

	@Override
	public String toString() {
		return "User [userId=" + userId + ", pwd=" + pwd + ", email=" + email + ", displayName=" + displayName + "]";
	}
}
