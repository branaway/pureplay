package controllers

import play._
import play.mvc._
import play.data.validation._

import java.util._

import models._

object Application extends Controller {
    
    def index { 
        render("now" -> new Date)
    }
    
    def list {
        render("contacts" -> Contacts.find("order by name, firstname").fetch)
    }
    
    def form(id: Long) {
        Contacts.findById(id) match {
            case Some(x) => render("contact" -> x)
            case None => render()
        }
    }
    
    def save(@Valid contact: Contact) {
        if (contact.validateAndSave()) {
            list
        }
        if (request.isAjax) error("Invalid Value") else "@form".render(contact)
    }

}