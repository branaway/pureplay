package controllers

import cn.bran.play.JapidController
import play.db.jpa.QueryOn
import play._
import play.mvc._
import play.data.validation._

import java.util._

import models._

object Application extends JapidController {
    
    def index { 
        render("now" -> new Date)
    }
    
    def list {
        renderJapid(QueryOn[Contact].find("order by name, firstname").fetch())
    }
    
    def form(id: Long) {
        Contact.findById(id) match {
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