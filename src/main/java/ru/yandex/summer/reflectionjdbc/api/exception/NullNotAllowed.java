package ru.yandex.summer.reflectionjdbc.api.exception;

/**
 * Exception throws when trying to save or load {@code null} for fields, which not allowed {@code null} values.
 */
public class NullNotAllowed extends RuntimeException  {

	public NullNotAllowed(){
		super();
	}

	public NullNotAllowed(String message) {
		super(message);
	}


	public NullNotAllowed(String message, Throwable cause) {
		super(message, cause);
	}
}
