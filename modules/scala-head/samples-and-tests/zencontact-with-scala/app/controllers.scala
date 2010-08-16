package controllers

import play._
import play.mvc._
import play.data.validation._

import java.util._

import models._

object Application extends Controller {
    
    def index {
        val now = new Date
        render(now)
    }
    
    def list {
        val contacts = Contact.find("order by name, firstname").fetch()
        render(contacts)
    }
    
    def form(id: Long) {
        val contact = Contact.findById(id)
        render(contact)
    }
    
    def save(@Valid contact: Contact) {
        Validation.hasErrors match {
            case true  => if (request isAjax) error("Invalid Value") else render("@form", contact)
            case false => contact.save(); list
        }
    }

}