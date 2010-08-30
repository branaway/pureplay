package models;

import java.sql.Date;

import javax.persistence.Entity;

import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Contact extends Model {
	@Required
	public String firstname;
	@Required
	public String name;

	@Required
	public Date birthdate;

	@Required
	@Email
	public String email;
}