package io.github.im2back.usermicroservice.service.exceptions;

public class NotificationException extends RuntimeException{
	private static final long serialVersionUID = 1L;
	
	public NotificationException(String msg) {
		super(msg);
	}

}
