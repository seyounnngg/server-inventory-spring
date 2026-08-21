package com.example.serverinventory.exception;

import com.example.serverinventory.web.ServerViewController;
import com.example.serverinventory.web.UserAdminController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(assignableTypes = {ServerViewController.class, UserAdminController.class})
public class WebExceptionHandler {

    @ExceptionHandler(ServerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFound(ServerNotFoundException ex) {
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("message", ex.getMessage());
        return mav;
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleUserNotFound(UserNotFoundException ex) {
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("message", ex.getMessage());
        return mav;
    }

    @ExceptionHandler(InvalidServerRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleInvalid(InvalidServerRequestException ex) {
        ModelAndView mav = new ModelAndView("error/400");
        mav.addObject("message", ex.getMessage());
        return mav;
    }
}
